package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.spaces.Space;

/**
 * Responsible for copying the look and feel configuration between spaces.
 */
public interface LookAndFeelCopier {
    /**
     * Copy the look and feel of one space to another.
     *
     * @param source      the space whose look and feel are cpied.
     * @param destination the space to which the look and feel are copied.
     */
    void copyLookAndFeel(Space source, Space destination);
}
