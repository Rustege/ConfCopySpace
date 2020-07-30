package com.atlassian.confluence.plugin.copyspace.actions;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.plugin.copyspace.CopySpaceManager;
import com.atlassian.confluence.plugin.copyspace.DefaultCopySpaceManager;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;

import static com.atlassian.confluence.security.Permission.ADMINISTER;

public class GoToCreatorByCopySpace extends AbstractSpaceAction {
    private String copierKey;
    private CopySpaceManager copySpaceManager;
    private BandanaManager bandanaManager;

    @Override
    public boolean isPermitted() {
        return permissionManager.hasPermission(getAuthenticatedUser(), ADMINISTER, getSpace());
    }

    @Override
    public String execute() throws Exception {
        super.execute();
        setCopierKey(DefaultCopySpaceManager.getCopierSpaceKey(bandanaManager, getSpace()));

        return SUCCESS;
    }

    public String getCopierKey() {
        return copierKey;
    }

    public void setCopierKey(String copierKey) {
        this.copierKey = copierKey;
    }

    public void setCopySpaceManager(CopySpaceManager copySpaceManager) {
        this.copySpaceManager = copySpaceManager;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }
}
