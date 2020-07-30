package com.atlassian.confluence.plugin.copyspace.actions;

import com.atlassian.confluence.plugin.copyspace.CopySpaceManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.ConfluenceUserImpl;
import com.atlassian.user.impl.DefaultUser;
import com.opensymphony.xwork.Action;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestCaseDoCopySpaceAction {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private DoCopySpaceAction doCopySpaceAction;

    private Space originalSpace;
    private String newKey = "NEW";
    private String newName = "New Space";
    private ConfluenceUser user;

    @Mock
    private CopySpaceManager copySpaceManager;
    @Mock
    private PermissionManager permissionManager;

    @Before
    public void setUp() {
        originalSpace = new Space();
        user = new ConfluenceUserImpl(new DefaultUser("bob"));

        AuthenticatedUserThreadLocal.set(user);

        doCopySpaceAction = new DoCopySpaceAction();
        doCopySpaceAction.setCopySpaceManager(copySpaceManager);
        doCopySpaceAction.setSpace(originalSpace);
        doCopySpaceAction.setNewKey(newKey);
        doCopySpaceAction.setNewName(newName);
    }

    @Test
    public void testSpaceCopiedUnderNewKey() throws Exception {
        Space newSpace = new Space();
        newSpace.setKey(newKey);
        newSpace.setName(newName);

        doCopySpaceAction.setSpace(originalSpace);
        doCopySpaceAction.setNewKey(newKey);
        doCopySpaceAction.setNewName(newName);

        assertThat(doCopySpaceAction.execute(), is(Action.SUCCESS));
        assertThat(doCopySpaceAction.getKey(), is(newKey));

        verify(copySpaceManager, atLeastOnce()).copySpace(eq(originalSpace), eq(newKey), eq(newName), eq(user), anyObject());
    }

    @Test
    public void testAdminPrivilegeRequiredToCopySpace() {

        doCopySpaceAction.setPermissionManager(permissionManager);

        when(permissionManager.hasCreatePermission(user, PermissionManager.TARGET_APPLICATION, Space.class)).thenReturn(true);
        when(permissionManager.hasPermission(user, Permission.ADMINISTER, originalSpace)).thenReturn(false);

        assertFalse(doCopySpaceAction.isPermitted());
    }
}
