package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.admin.actions.lookandfeel.DefaultDecorator;
import com.atlassian.confluence.core.PersistentDecorator;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.themes.persistence.PersistentDecoratorDao;

import java.util.Date;
import java.util.List;

/**
 * Default implementation of {@link DecoratorCopier}
 */
public class DefaultDecoratorCopier implements DecoratorCopier {
    private PersistentDecoratorDao persistentDecoratorDao;

    public DefaultDecoratorCopier(PersistentDecoratorDao persistentDecoratorDao) {
        this.persistentDecoratorDao = persistentDecoratorDao;
    }

    public void copyDecorators(Space source, Space destination, CopySpaceOptions options) {
        // This seems to be the only way to get a list of all defined decorator names.
        List<DefaultDecorator> decorators = DefaultDecorator.getDecorators();
        for (DefaultDecorator decorator : decorators) {
            String spaceKey = source.getKey();
            String decoratorName = decorator.getFileName();
            PersistentDecorator persistentDecorator = persistentDecoratorDao.get(spaceKey, decoratorName);
            if (persistentDecorator != null)
                copyDecorator(destination, options, persistentDecorator);
        }
    }

    private void copyDecorator(Space destination, CopySpaceOptions options, PersistentDecorator original) {
        String name = original.getName();
        String body = original.getBody();
        Date date = options.isKeepMetaData() ? original.getLastModificationDate() : new Date();
        PersistentDecorator copy = new PersistentDecorator(destination.getKey(), name, body, date);
        persistentDecoratorDao.saveOrUpdate(copy);
    }
}
