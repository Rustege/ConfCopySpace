package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.spaces.Space;
import com.atlassian.user.User;

import java.io.IOException;

public interface CopySpaceManager {
    /**
     * Make a deep copy of a space, including the pages within the space.
     *
     * @param originalSpace the space to be copied
     * @param newKey        key to give the new space
     * @param newName       the name to give the new space.
     * @param user          the user to create objects as.  Where possible the original users will be used if specified in
     *                      <code>options</code>
     * @param options       the configuration of this copying operation.
     * @return the newly created space.
     * @throws IOException        if a failure to read or write occurs
     * @throws CopySpaceException if a semi-expected error occurs, such as a failure to read or write attachments.
     */
    Space copySpace(Space originalSpace, String newKey,
                    String newName, User user, CopySpaceOptions options)
            throws IOException, CopySpaceException;
}
