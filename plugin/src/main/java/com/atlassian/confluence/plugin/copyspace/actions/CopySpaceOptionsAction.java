package com.atlassian.confluence.plugin.copyspace.actions;

import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.confluence.spaces.actions.SpaceAware;

import static com.atlassian.confluence.security.Permission.ADMINISTER;

public class CopySpaceOptionsAction extends AbstractSpaceAction implements SpaceAware {
    @Override
    public boolean isPermitted() {
        return permissionManager.hasPermission(getAuthenticatedUser(), ADMINISTER, getSpace());
    }

    @Override
    public boolean isSpaceRequired() {
        return true;
    }

    @Override
    public boolean isViewPermissionRequired() {
        return true;
    }

    /**
     * Default value.
     */
    public boolean isCopyComments() {
        return true;
    }

    /**
     * Default value.
     */
    public boolean isCopyAttachments() {
        return true;
    }
}
