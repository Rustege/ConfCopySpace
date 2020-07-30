package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.core.ContentPermissionManager;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.Labelling;
import com.atlassian.confluence.pages.CommentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.confluence.pages.templates.PageTemplateManager;
import com.atlassian.confluence.search.ConfluenceIndexer;
import com.atlassian.confluence.security.SpacePermission;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceDescription;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.user.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCaseDefaultCopySpaceManager {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private DefaultCopySpaceManager defaultCopySpaceManager;

    // Mock objects
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private ConfluenceIndexer indexer;
    @Mock
    private ContentPermissionManager contentPermissionManager;
    @Mock
    private SpaceManager spaceManager;
    @Mock
    private PageManager pageManager;
    @Mock
    private DefaultLabelCopier labelCopier;
    @Mock
    private PageTemplateManager pageTemplateManager;
    @Mock
    private CommentManager commentManager;
    @Mock
    private BandanaManager bandanaManager;
    @Mock
    private LookAndFeelCopier lookAndFeelCopier;
    @Mock
    private LogoCopier logoCopier;
    @Mock
    private AttachmentCopier attachmentCopier;
    @Mock
    private DecoratorCopier decoratorCopier;
    @Mock
    private CopySpaceOptions options;
    @Mock
    private User user;

    // Global test variables
    private Space originalSpace;
    private Space newSpace;

    private String newSpaceKey = "new";
    private String newSpaceName = "New Space";

    @Before
    public void setUp() {

        SpaceDescription spaceDescription = new SpaceDescription();
        String description = "This is description";
        spaceDescription.setBodyAsString(description);

        originalSpace = new Space();
        String originalSpaceKey = "tst";
        originalSpace.setKey(originalSpaceKey);
        String originalSpaceName = "Test";
        originalSpace.setName(originalSpaceName);
        originalSpace.setDescription(spaceDescription);

        newSpace = new Space();
        newSpace.setKey(newSpaceKey);
        newSpace.setName(newSpaceName);
        newSpace.setDescription(spaceDescription);

        when(spaceManager.createSpace(newSpaceKey, newSpaceName, originalSpace.getDescription().getBodyAsString(), user)).thenReturn(newSpace);
        when(options.isCopyPersonalLabels()).thenReturn(true);
        when(options.isKeepMetaData()).thenReturn(true);

        defaultCopySpaceManager = new DefaultCopySpaceManager(eventPublisher, indexer, contentPermissionManager, spaceManager, pageManager, labelCopier, pageTemplateManager, commentManager, bandanaManager, lookAndFeelCopier, logoCopier, attachmentCopier, decoratorCopier);
    }

    @Test
    public void testCopySpaceWithoutAnySpacePermissionsAndPageTemplates() throws IOException, CopySpaceException {
        Space resultSpace = defaultCopySpaceManager.copySpace(originalSpace, newSpaceKey, newSpaceName, user, options);

        assertThat(resultSpace.getKey(), is(newSpaceKey));
        assertThat(resultSpace.getName(), is(newSpaceName));

        verify(spaceManager, atLeastOnce()).saveSpace(newSpace);
        verify(lookAndFeelCopier, atLeastOnce()).copyLookAndFeel(originalSpace, newSpace);
        verify(decoratorCopier, atLeastOnce()).copyDecorators(originalSpace, newSpace, options);
        verify(logoCopier, atLeastOnce()).copyLogo(originalSpace, newSpace, options);
        verify(labelCopier, atLeastOnce()).copySpaceLabels(originalSpace, newSpace, true);
    }

    @Test
    public void testCopySpaceWithoutAnySpacePermissionButWithPageTemplates() throws IOException, CopySpaceException {
        String templateName = "Template 123";
        String templateDescription = "This is template 123";
        String templateContent = "123";
        String templateLabels = "templateLabel";

        PageTemplate pageTemplate = new PageTemplate();
        pageTemplate.setName(templateName);
        pageTemplate.setDescription(templateDescription);
        pageTemplate.setContent(templateContent);
        Label label = new Label(templateLabels);
        label.setId(1);
        pageTemplate.addLabelling(new Labelling(label, pageTemplate, (String) null));

        originalSpace.addPageTemplate(pageTemplate);

        Space resultSpace = defaultCopySpaceManager.copySpace(originalSpace, newSpaceKey, newSpaceName, user, options);

        assertThat(resultSpace.getKey(), is(newSpaceKey));
        assertThat(resultSpace.getName(), is(newSpaceName));
        assertTrue(resultSpace.getPageTemplates().contains(pageTemplate));

        verify(pageTemplateManager).savePageTemplate(pageTemplate, null);
        verify(labelCopier).copyLabels(eq(pageTemplate), any(PageTemplate.class), eq(true));
    }

    @Test
    public void testCopySpaceWithSpacePermissionsAndWithoutPageTemplates() throws IOException, CopySpaceException {
        SpacePermission originalPermission = new SpacePermission();
        originalPermission.setSpace(originalSpace);

        originalSpace.addPermission(originalPermission);

        Space resultSpace = defaultCopySpaceManager.copySpace(originalSpace, newSpaceKey, newSpaceName, user, options);

        assertThat(resultSpace.getKey(), is(newSpaceKey));
        assertThat(resultSpace.getName(), is(newSpaceName));

        List<SpacePermission> newSpacePermissions = resultSpace.getPermissions();

        assertThat(newSpacePermissions, hasSize(1));
        assertThat(newSpacePermissions.get(0).getSpace(), is(newSpace));
    }

    @Test
    public void testCopySpacePagesWithoutParentPage() throws IOException, CopySpaceException {
        Page pageOne = new Page();
        pageOne.setTitle("Page One");
        pageOne.setBodyAsString("This is page one content");
        pageOne.setPosition(1);
        pageOne.setSpace(originalSpace);

        List<Page> oldPages = new ArrayList<>();
        oldPages.add(pageOne);

        when(pageManager.getPages(originalSpace, true)).thenReturn(oldPages);

        defaultCopySpaceManager = new DefaultCopySpaceManager(eventPublisher, indexer, contentPermissionManager, spaceManager, pageManager, labelCopier, pageTemplateManager, commentManager, bandanaManager, lookAndFeelCopier, logoCopier, attachmentCopier, decoratorCopier);

        Space resultSpace = defaultCopySpaceManager.copySpace(originalSpace, newSpaceKey, newSpaceName, user, options);

        assertThat(resultSpace.getKey(), is(newSpaceKey));
        assertThat(resultSpace.getName(), is(newSpaceName));

        verify(pageManager, atLeastOnce()).saveContentEntity(anyObject(), Matchers.eq(null));
    }
}
