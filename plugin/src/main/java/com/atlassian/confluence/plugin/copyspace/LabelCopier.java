package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.spaces.Space;

/**
 * Responsible for copying labels between ConfluenceEntityObjects.
 */
public interface LabelCopier {
    /**
     * Copies labels from one space to another.  Copying personal labels is optional.
     *
     * @param originalSpace      from which to copy labels.
     * @param newSpace           to which the labels should be copied.
     * @param copyPersonalLabels true if personal labels, eg those used for favourites should be copied.
     * @see com.atlassian.confluence.labels.Namespace#PERSONAL
     */
    void copySpaceLabels(Space originalSpace, Space newSpace, boolean copyPersonalLabels);

    /**
     * Copies labels from one ContentEntityObject to another.  Copying personal labels is optional.
     *
     * @param original           page from which labels are copied.
     * @param copy               to which labels are copied.
     * @param copyPersonalLabels true if personal labels, eg those used for favourites should be copied.
     * @see com.atlassian.confluence.labels.Namespace#PERSONAL
     */
    void copyLabels(Labelable original, Labelable copy, boolean copyPersonalLabels);
}
