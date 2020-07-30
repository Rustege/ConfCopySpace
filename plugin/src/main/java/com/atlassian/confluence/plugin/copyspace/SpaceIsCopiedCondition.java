package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.plugin.descriptor.web.conditions.BaseConfluenceCondition;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.User;

public class SpaceIsCopiedCondition extends BaseConfluenceCondition {
    private PermissionManager permissionManager;
    private BandanaManager bandanaManager;

    @Override
    protected boolean shouldDisplay(WebInterfaceContext context) {
        final Space space = context.getSpace();
        final User user = context.getCurrentUser();
        if (user == null || space == null)
            return false;

        if (!permissionManager.hasPermission(user, Permission.ADMINISTER, space)) {
            return false;
        }

        // Couldn't get CopySpaceManager so forced to ugly stuff up :-(
        return DefaultCopySpaceManager.getCopierSpaceKey(bandanaManager, space) != null;
    }

    // Setter and getters does not need to be tested
    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }
}
