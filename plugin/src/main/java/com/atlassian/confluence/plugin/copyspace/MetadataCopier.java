package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.core.ConfluenceEntityObject;

/**
 * Simple utility class to copy metadata from one CEO to another.
 */
class MetadataCopier {
    /**
     * Copies the creationDate, modificationDate, creatorName and modifierName from one entity to
     * another.
     *
     * @param from a non-null ConfluenceEntityObject
     * @param to   a non-null ConfluenceEntityObject
     */
    public static void copyEntityMetadata(ConfluenceEntityObject from, ConfluenceEntityObject to) {
        to.setCreationDate(from.getCreationDate());
        to.setLastModificationDate(from.getLastModificationDate());
        to.setCreator(from.getCreator());
        to.setLastModifier(from.getLastModifier());
    }
}
