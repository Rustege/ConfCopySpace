package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.ConfluenceUserImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

public class TestCaseCopySpaceCondition {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private CopySpaceCondition copySpaceCondition;

    @Mock
    private WebInterfaceContext webInterfaceContext;
    @Mock
    private PermissionManager permissionManager;

    @Before
    public void setUp() {
        copySpaceCondition = new CopySpaceCondition();
        copySpaceCondition.setPermissionManager(permissionManager);
    }

    @Test
    public void testWhenUserCannotCreateSpaceButCanAdministerSpace() {
        Space space = new Space();

        ConfluenceUser user = new ConfluenceUserImpl();

        when(webInterfaceContext.getSpace()).thenReturn(space);
        when(webInterfaceContext.getCurrentUser()).thenReturn(user);

        when(permissionManager.hasCreatePermission(user, PermissionManager.TARGET_APPLICATION, Space.class)).thenReturn(false);
        when(permissionManager.hasPermission(user, Permission.ADMINISTER, space)).thenReturn(true);

        assertFalse(copySpaceCondition.shouldDisplay(webInterfaceContext));
    }
}
