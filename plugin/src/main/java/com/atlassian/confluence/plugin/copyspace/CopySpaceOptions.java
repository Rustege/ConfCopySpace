package com.atlassian.confluence.plugin.copyspace;

/**
 * Encapsulates the various options available to a CopySpaceAction.
 */
public class CopySpaceOptions {
    private boolean copyComments;
    private boolean copyPersonalLabels;
    private boolean copyAttachments;
    private boolean keepMetaData;

    // All setter and getters does not need to be tested
    public boolean isCopyComments() {
        return copyComments;
    }

    public void setCopyComments(boolean copyComments) {
        this.copyComments = copyComments;
    }

    public boolean isCopyAttachments() {
        return copyAttachments;
    }

    public void setCopyAttachments(boolean copyAttachments) {
        this.copyAttachments = copyAttachments;
    }

    public boolean isKeepMetaData() {
        return keepMetaData;
    }

    public void setKeepMetaData(boolean keepMetaData) {
        this.keepMetaData = keepMetaData;
    }

    public boolean isCopyPersonalLabels() {
        return copyPersonalLabels;
    }

    public void setCopyPersonalLabels(boolean copyPersonalLabels) {
        this.copyPersonalLabels = copyPersonalLabels;
    }
}
