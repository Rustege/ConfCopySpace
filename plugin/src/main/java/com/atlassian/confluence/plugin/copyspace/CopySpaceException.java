package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.core.ConfluenceEntityObject;

/**
 * The exception thrown when an attempt to copy a space fails.
 */
public class CopySpaceException extends Exception {
    private final ConfluenceEntityObject failedContent;

    public CopySpaceException(String message) {
        this(message, null, null);
    }

    public CopySpaceException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public CopySpaceException(String message, ConfluenceEntityObject failedContent, Throwable cause) {
        super(message, cause);
        this.failedContent = failedContent;
    }

    public ConfluenceEntityObject getFailedContent() {
        return failedContent;
    }
}
