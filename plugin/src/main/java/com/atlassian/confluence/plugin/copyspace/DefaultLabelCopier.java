package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.labels.Namespace;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceDescription;

import java.util.List;

/**
 * Default implementation of {@link LabelCopier} uses an injected {@link LabelManager}.
 */
public class DefaultLabelCopier implements LabelCopier {
    private LabelManager labelManager;

    public void copySpaceLabels(Space originalSpace, Space newSpace, boolean copyPersonalLabels) {
        SpaceDescription originalSpaceDescription = originalSpace.getDescription();
        SpaceDescription newSpaceDescription = newSpace.getDescription();
        copyLabels(originalSpaceDescription, newSpaceDescription, copyPersonalLabels);
    }

    public void copyLabels(Labelable original, Labelable copy, boolean copyPersonalLabels) {
		if (labelManager == null || copy == null) {
           return;
        }
        List<Label> labels = original.getLabels();
        for (Label label : labels) {
            if (copyPersonalLabels || !(label.getNamespace().equals(Namespace.PERSONAL)))
                labelManager.addLabel(copy, label);
        }
    }

    public void setLabelManager(LabelManager labelManager) {
        this.labelManager = labelManager;
    }
}
