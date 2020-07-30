package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.spaces.Space;

/**
 * Component to copy decorators, aka layouts from the old space to the new space.
 */
public interface DecoratorCopier {
    /**
     * Copy any specifically defined decorators from the source space to
     * the destination space.
     */
    void copyDecorators(Space source, Space destination, CopySpaceOptions options);
}
