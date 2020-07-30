package com.atlassian.confluence.plugin.copyspace.actions;

import com.atlassian.confluence.core.ConfluenceEntityObject;
import com.atlassian.confluence.plugin.copyspace.CopySpaceException;
import com.atlassian.confluence.plugin.copyspace.CopySpaceManager;
import com.atlassian.confluence.plugin.copyspace.CopySpaceOptions;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.actions.AbstractSpaceAction;
import com.atlassian.user.User;
import org.slf4j.Logger;

import static com.atlassian.confluence.security.Permission.ADMINISTER;
import static com.atlassian.confluence.security.PermissionManager.TARGET_APPLICATION;
import static com.atlassian.confluence.spaces.Space.isValidGlobalSpaceKey;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class DoCopySpaceAction extends AbstractSpaceAction {
    private static final Logger log = getLogger(DoCopySpaceAction.class);

    private String newName;
    private String newKey;
    private CopySpaceOptions options = new CopySpaceOptions();
    private CopySpaceManager copySpaceManager;
    private ConfluenceEntityObject failedContent;
    private String errorMessage;

    /**
     * Validates for field correctness only.  Permission checking is done according to
     * {@link AbstractSpaceAction#getPermissionTypes()}, which takes not of this action being SpaceAdministrative.
     */
    public void validate() {
        super.validate();
        if (isBlank(newName))
            addFieldError("newName", "New name was not specified");

        if (!isValidGlobalSpaceKey(newKey))
            addFieldError("newKey", getText("space.key.invalid"));

        if (spaceManager.getSpace(newKey) != null)
            addFieldError("newKey", getText("space.key.exists"));

        if (isBlank(newKey))
            addFieldError("newKey", getText("space.name.empty"));
    }

    @Override
    public boolean isPermitted() {
        User remoteUser = getAuthenticatedUser();
        boolean canCreateSpaces = permissionManager.hasCreatePermission(remoteUser, TARGET_APPLICATION, Space.class);
        boolean canAdministerThisSpace = permissionManager.hasPermission(remoteUser, ADMINISTER, getSpace());
        return canCreateSpaces && canAdministerThisSpace;
    }

    @Override
    public String execute() throws Exception {
        super.execute();

        Space newSpace;
        try {
            newSpace = copySpaceManager.copySpace(getSpace(), getNewKey(), getNewName(), getAuthenticatedUser(), options);
        } catch (CopySpaceException e) {
            log.error("Failed to copy the space: " + getSpace(), e);
            failedContent = e.getFailedContent();
            errorMessage = e.getMessage();
            return ERROR;
        }
        setSpace(newSpace);
        setKey(getNewKey());

        return SUCCESS;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getNewKey() {
        return newKey;
    }

    public void setNewKey(String newKey) {
        this.newKey = newKey;
    }

    public CopySpaceManager getCopySpaceManager() {
        return copySpaceManager;
    }

    public void setCopySpaceManager(CopySpaceManager copySpaceManager) {
        this.copySpaceManager = copySpaceManager;
    }

    public boolean isCopyComments() {
        return options.isCopyComments();
    }

    public void setCopyComments(boolean copyComments) {
        this.options.setCopyComments(copyComments);
    }

    public boolean isCopyPersonalLabels() {
        return options.isCopyPersonalLabels();
    }

    public void setCopyPersonalLabels(boolean copyPersonalLabels) {
        this.options.setCopyPersonalLabels(copyPersonalLabels);
    }

    public boolean isCopyAttachments() {
        return options.isCopyAttachments();
    }

    public void setCopyAttachments(boolean copyAttachments) {
        this.options.setCopyAttachments(copyAttachments);
    }

    public boolean isKeepMetaData() {
        return options.isKeepMetaData();
    }

    public void setKeepMetaData(boolean keepMetaData) {
        this.options.setKeepMetaData(keepMetaData);
    }


    public ConfluenceEntityObject getFailedContent() {
        return failedContent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
