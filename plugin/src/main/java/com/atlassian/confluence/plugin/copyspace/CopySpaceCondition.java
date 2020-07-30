package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.plugin.descriptor.web.conditions.BaseConfluenceCondition;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.User;

import static com.atlassian.confluence.security.Permission.ADMINISTER;
import static com.atlassian.confluence.security.PermissionManager.TARGET_APPLICATION;

public class CopySpaceCondition extends BaseConfluenceCondition {
    private PermissionManager permissionManager;

    @Override
    protected boolean shouldDisplay(WebInterfaceContext context) {
        final Space space = context.getSpace();
        final User user = context.getCurrentUser();
        if (user == null || space == null)
            return false;

        final boolean canCreateSpace = permissionManager.hasCreatePermission(user, TARGET_APPLICATION, Space.class);
        final boolean canAdministerSpace = permissionManager.hasPermission(user, ADMINISTER, space);

        return canAdministerSpace && canCreateSpace;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }
}
