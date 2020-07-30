package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceDescription;

/**
 * Copy a logo from one space to another by copying and renaming the attachments
 * attached to the space description.
 */
public class AttachmentBasedLogoCopier implements LogoCopier {
    private AttachmentCopier attachmentCopier;
    private AttachmentManager attachmentManager;

    public AttachmentBasedLogoCopier(
            AttachmentCopier attachmentCopier,
            AttachmentManager attachmentManager
    ) {
        this.attachmentCopier = attachmentCopier;
        this.attachmentManager = attachmentManager;
    }

    public void copyLogo(Space source, Space destination, CopySpaceOptions options) throws CopySpaceException {
        SpaceDescription originalDescription = source.getDescription();
        SpaceDescription newDescription = destination.getDescription();
        // Space Description attachments are copied regardless of whether other attachments are.
        attachmentCopier.copyAttachments(originalDescription, newDescription, options);
        Attachment logoAttachment = attachmentManager.getAttachment(newDescription, source.getKey());
        if (logoAttachment != null)
            attachmentManager.moveAttachment(logoAttachment, destination.getKey(), newDescription);
    }
}
