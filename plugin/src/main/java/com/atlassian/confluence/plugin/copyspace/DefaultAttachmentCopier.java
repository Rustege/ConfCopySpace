package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.attachments.AttachmentDataStreamSizeMismatchException;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Copies attachments from one CEO to another using the {@link com.atlassian.confluence.pages.AttachmentManager}.
 */
public class DefaultAttachmentCopier implements AttachmentCopier {
    private final AttachmentManager attachmentManager;
    private final LabelManager labelManager;

    public DefaultAttachmentCopier(AttachmentManager attachmentManager, LabelManager labelManager) {
        this.attachmentManager = attachmentManager;
        this.labelManager = labelManager;
    }

    /**
     * Note: Doesn't take care of saving the page to which the attachments are added.
     */
    public void copyAttachments(ContentEntityObject from, ContentEntityObject to, CopySpaceOptions options) throws CopySpaceException {
        List<Attachment> attachments = attachmentManager.getLatestVersionsOfAttachments(from);
        for (Attachment attachment : attachments) {
            Attachment attachmentCopy = new Attachment();
            attachmentCopy.setFileName(attachment.getFileName());
            attachmentCopy.setFileSize(attachment.getFileSize());
            attachmentCopy.setMediaType(attachment.getMediaType());
            attachmentCopy.setVersionComment(attachment.getVersionComment());
            attachmentCopy.setVersion(1);
            attachmentCopy.setContainer(to);

            // Grab the InputStream from the original attachment
            InputStream data = attachmentManager.getAttachmentData(attachment);

            try {
                attachmentManager.saveAttachment(attachmentCopy, null, data);
                for (Label label : attachment.getLabels()) {
                    labelManager.addLabel((Labelable) attachmentCopy, label);
                }

                if (options.isKeepMetaData())
                    MetadataCopier.copyEntityMetadata(attachment, attachmentCopy);
            } catch (AttachmentDataStreamSizeMismatchException e) {
                throw new CopySpaceException("The attachment's size property does not match the" +
                        " attachment's data size: " + attachment, attachment, e);
            } catch (Exception e) {
                throw new CopySpaceException("An error occurred while copying an attachment.  Page: " +
                        from + " attachment: " + attachment, attachment, e);
            } finally {
                closeQuietly(data);
            }
        }
    }
}
