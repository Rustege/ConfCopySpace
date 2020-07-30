package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.ContentPermissionManager;
import com.atlassian.confluence.event.events.security.ContentPermissionEvent;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.CommentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.confluence.pages.templates.PageTemplateManager;
import com.atlassian.confluence.search.ConfluenceIndexer;
import com.atlassian.confluence.security.ContentPermission;
import com.atlassian.confluence.security.ContentPermissionSet;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.setup.bandana.ConfluenceBandanaContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.user.User;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.confluence.security.ContentPermission.createGroupPermission;
import static com.atlassian.confluence.security.ContentPermission.createUserPermission;

/**
 * Manager responsible for making a complete copy of a space.
 */
public class DefaultCopySpaceManager implements CopySpaceManager {
    private static final Logger log = Logger.getLogger(DefaultCopySpaceManager.class);

    private static final String BANDANA_KEY_COPYING_SPACE_KEY = "copyspace.copier.spacekey";

    private final EventPublisher eventPublisher;
    private final ConfluenceIndexer indexer;
    private final ContentPermissionManager contentPermissionManager;
    private final SpaceManager spaceManager;
    private final PageManager pageManager;
    private final LabelCopier labelCopier;
    private final PageTemplateManager pageTemplateManager;
    private final CommentManager commentManager;
    private final BandanaManager bandanaManager;
    private final LookAndFeelCopier lookAndFeelCopier;
    private final LogoCopier logoCopier;
    private final AttachmentCopier attachmentCopier;
    private final DecoratorCopier decoratorCopier;

    public DefaultCopySpaceManager(EventPublisher eventPublisher, ConfluenceIndexer indexer,
                                   ContentPermissionManager contentPermissionManager, SpaceManager spaceManager,
                                   PageManager pageManager, DefaultLabelCopier labelCopier,
                                   PageTemplateManager pageTemplateManager, CommentManager commentManager,
                                   BandanaManager bandanaManager, LookAndFeelCopier lookAndFeelCopier,
                                   LogoCopier logoCopier, AttachmentCopier attachmentCopier,
                                   DecoratorCopier decoratorCopier) {
        log.debug("CopySpaceManager.CopySpaceManager: " + System.currentTimeMillis() + " " + toString());

        this.eventPublisher = eventPublisher;
        this.indexer = indexer;
        this.contentPermissionManager = contentPermissionManager;
        this.spaceManager = spaceManager;
        this.pageManager = pageManager;
        this.labelCopier = labelCopier;
        this.pageTemplateManager = pageTemplateManager;
        this.commentManager = commentManager;
        this.bandanaManager = bandanaManager;
        this.lookAndFeelCopier = lookAndFeelCopier;
        this.logoCopier = logoCopier;
        this.attachmentCopier = attachmentCopier;
        this.decoratorCopier = decoratorCopier;
    }

    /* (non-Javadoc)
    * @see com.atlassian.confluence.plugin.copyspace.CopySpaceManager#copySpace(com.atlassian.confluence.spaces.Space,
    * java.lang.String, java.lang.String, com.atlassian.user.User, com.atlassian.confluence.plugin.copyspace.CopySpaceOptions)
    */
    public Space copySpace(Space originalSpace, String newKey, String newName, User user, CopySpaceOptions options) throws IOException, CopySpaceException {
        Space newSpace;

        // todo: Handle space being created after successful validation.
        newSpace = spaceManager.createSpace(newKey, newName, originalSpace.getDescription().getBodyAsString(), user);
        copySpacePermissions(originalSpace, newSpace, options.isKeepMetaData());
        final Page homePage = newSpace.getHomePage();
        if (homePage != null)  // Based on CPSP-31 it sometimes can be.
            homePage.remove(pageManager);
        spaceManager.saveSpace(newSpace);
        if (options.isKeepMetaData())
            MetadataCopier.copyEntityMetadata(originalSpace, newSpace);
        lookAndFeelCopier.copyLookAndFeel(originalSpace, newSpace);
        decoratorCopier.copyDecorators(originalSpace, newSpace, options);
        logoCopier.copyLogo(originalSpace, newSpace, options);
        labelCopier.copySpaceLabels(originalSpace, newSpace, options.isCopyPersonalLabels());
        copyPageTemplates(originalSpace, newSpace, options.isKeepMetaData(), options.isCopyPersonalLabels());

        List<Page> oldPages = pageManager.getPages(originalSpace, true);
        List<PagePair> copiedPages = new ArrayList<>();
        for (Page page : oldPages) {
            // copy the parentless pages and their children.
            // can't iterate straight through all pages as children must be added after their parent.
            if (page.getParent() == null)
                copyPagesRecursive(page, null, newSpace, options, copiedPages);
        }

        for (PagePair pair : copiedPages) {
            // Need to get this out as soon as possible before we mess with the id and therefore hashcode and therefore
            // break the map.
            Page original = pair.original;
            Page copy = pair.copy;
            rebuildAncestors(copy);
            eventPublisher.publish(new ContentPermissionEvent(this, copy, null));
            indexer.reIndex(copy);
            pageManager.saveContentEntity(copy, null);
            if (options.isKeepMetaData())
                MetadataCopier.copyEntityMetadata(original, copy);
        }
        spaceManager.saveSpace(newSpace);

        for (PagePair pair : copiedPages) {
            if (options.isKeepMetaData())
                MetadataCopier.copyEntityMetadata(pair.original, pair.copy);

            labelCopier.copyLabels(pair.original, pair.copy, options.isCopyPersonalLabels());
        }

        recordCopyingSpaceAgainstCopiedSpace(newSpace, originalSpace);
        return newSpace;
    }

    /**
     * Record in Bandana that <code>newSpace</code> was generated from <code>originalSpace</code>.  Note that only
     * one space can be recorded as being the generator of a space, although many spaces can be generated by the
     * same space.
     *
     * @param newSpace      the space to be marked as having been generated by a copy.
     * @param originalSpace the generating space to which the record points.
     */
    private void recordCopyingSpaceAgainstCopiedSpace(Space newSpace, Space originalSpace) {
        ConfluenceBandanaContext bandanaContext = new ConfluenceBandanaContext(newSpace);
        bandanaManager.setValue(bandanaContext, BANDANA_KEY_COPYING_SPACE_KEY, originalSpace.getKey());
    }

    /**
     * Helper method that retrieves the key of the space from which this one was created by copying.
     * If this space wasn't created by copying another space it returns null.  I think it had to be
     * static because this manager isn't instantiated until after the {@link SpaceIsCopiedCondition} is autowired.
     *
     * @param bandanaManager from which to look up the copying record.
     * @param space          the space that may have been copied
     * @return the key of the space from which the given space was copied, or null if not applicable.
     */
    public static String getCopierSpaceKey(BandanaManager bandanaManager, Space space) {
        ConfluenceBandanaContext bandanaContext = new ConfluenceBandanaContext(space);
        return (String) bandanaManager.getValue(bandanaContext, BANDANA_KEY_COPYING_SPACE_KEY);
    }

    private void copySpacePermissions(Space originalSpace, Space newSpace, boolean keepMetaData) {
        List<SpacePermission> originalPermissions = originalSpace.getPermissions();
        for (SpacePermission originalPermission : originalPermissions) {
            SpacePermission copiedPermission = new SpacePermission(originalPermission);
            copiedPermission.setSpace(newSpace);

            newSpace.addPermission(copiedPermission);
            if (keepMetaData)
                MetadataCopier.copyEntityMetadata(originalPermission, copiedPermission);
        }
    }

    private void copyPagesRecursive(Page original, Page newParent, Space newSpace, CopySpaceOptions options, List<PagePair> copiedPages) throws IOException, CopySpaceException {
        Page copy;
        copy = copyPage(original, newSpace);
        copiedPages.add(new PagePair(original, copy));

        copy.setSpace(newSpace);
        if (original.equals(original.getSpace().getHomePage()))
            newSpace.setHomePage(copy);

        if (newParent != null)
            newParent.addChild(copy);

        if (options.isCopyAttachments())
            attachmentCopier.copyAttachments(original, copy, options);

        if (options.isCopyComments())
            copyPageComments(original, copy);

        // now deal with children and recurse
        // Grab all child pages, and copy them.  User is space admin so restrictions are irrelevant.
        List<Page> children = original.getChildren();

        for (Page child : children) {
            copyPagesRecursive(child, copy, newSpace, options, copiedPages);
        }
    }

    private void copyPageComments(Page from, Page to) {
        List<Comment> originalComments = from.getComments();
        Map<Long, Comment> oldIdToCopiedComment = new HashMap<>();
        for (Comment oldComment : originalComments) {
            Comment newParent = null;
            if (oldComment.getParent() != null) {
                Long oldId = oldComment.getParent().getId();
                newParent = oldIdToCopiedComment.get(oldId);
                if (newParent != null) {
                    log.warn("Comments are out of creation date order.  Old parent id = " + oldId +
                            ", old child id = " + oldComment.getId());
                }
            }
            Comment newComment = commentManager.addCommentToObject(to, newParent, oldComment.getBodyContent().getBody());
            // Note that we ignore the keep meta-data option for comments.
            MetadataCopier.copyEntityMetadata(oldComment, newComment);
            // I'm hoping that will get magically committed when they go over the transaction boundary.
            oldIdToCopiedComment.put(oldComment.getId(), newComment);
        }
    }

    private Page copyPage(Page original, Space newSpace) {
        Page copy;
        copy = new Page();
        copy.setTitle(original.getTitle());
        copy.setBodyContent(original.getBodyContent());
        copy.setPosition(original.getPosition());
        copy.setSpace(newSpace);
        pageManager.saveContentEntity(copy, null);
        copyContentPermissionSets(original, copy);

        return copy;
    }

    public void copyContentPermissionSets(Page from, Page to) {
        ContentPermissionSet permissionSet = from.getContentPermissionSet(ContentPermission.VIEW_PERMISSION);
        copyContentPermissionSet(permissionSet, to);
        permissionSet = from.getContentPermissionSet(ContentPermission.EDIT_PERMISSION);
        copyContentPermissionSet(permissionSet, to);
    }

    private void copyContentPermissionSet(ContentPermissionSet permissionSet, Page copy) {
        if (permissionSet == null)
            return;
        for (ContentPermission originalPermission : permissionSet) {
            ContentPermission newPermission;
            if (originalPermission.isUserPermission())
                newPermission = createUserPermission(originalPermission.getType(), originalPermission.getUserSubject());
            else
                newPermission = createGroupPermission(originalPermission.getType(), originalPermission.getGroupName());

            contentPermissionManager.addContentPermission(newPermission, copy);
        }
    }

    private void copyPageTemplates(Space originalSpace, Space newSpace, boolean keepMetaData, boolean copyPersonalLabels) {
        List<PageTemplate> pageTemplates = originalSpace.getPageTemplates();
        for (PageTemplate pageTemplate : pageTemplates) {
            PageTemplate copy = copyPageTemplate(pageTemplate);
            newSpace.addPageTemplate(copy);
            pageTemplateManager.savePageTemplate(copy, null);
            labelCopier.copyLabels(pageTemplate, copy, copyPersonalLabels);

            if (keepMetaData)
                MetadataCopier.copyEntityMetadata(pageTemplate, copy);
        }
    }

    private PageTemplate copyPageTemplate(PageTemplate originalPageTemplate) {
        PageTemplate template = new PageTemplate();

        template.setName(originalPageTemplate.getName());
        template.setDescription(originalPageTemplate.getDescription());
        template.setContent(originalPageTemplate.getContent());
        return template;
    }


    private void rebuildAncestors(Page page) {
        page.getAncestors().clear();

        if (page.getParent() != null) {
            page.getAncestors().addAll(page.getParent().getAncestors());
            page.getAncestors().add(page.getParent());
        }
    }

    private static class PagePair {
        public final Page original;
        public final Page copy;

        public PagePair(Page oldPage, Page newPage) {
            this.original = oldPage;
            this.copy = newPage;
        }
    }
}