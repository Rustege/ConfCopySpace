package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.spaces.Space;

/**
 * Copies a logo from one space to another.
 */
public interface LogoCopier {
    /**
     * If the source space has a logo, copy it to the destination space.
     */
    void copyLogo(Space source, Space destination, CopySpaceOptions options) throws CopySpaceException;
}
