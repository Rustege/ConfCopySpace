package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.core.ContentEntityObject;

/**
 * Component to copy attachments from one {@link com.atlassian.confluence.core.ContentEntityObject} to another.
 */
public interface AttachmentCopier {

    void copyAttachments(ContentEntityObject from, ContentEntityObject to, CopySpaceOptions options) throws CopySpaceException;
}
