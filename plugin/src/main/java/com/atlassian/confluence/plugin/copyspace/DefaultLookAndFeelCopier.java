package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.setup.settings.SpaceSettings;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.themes.ColourSchemeManager;
import com.atlassian.confluence.themes.ThemeManager;

/**
 * Manager responsible for copying a space's look and feel.
 */
public class DefaultLookAndFeelCopier implements LookAndFeelCopier {
    private ThemeManager themeManager;
    private SettingsManager settingsManager;
    private ColourSchemeManager colourSchemeManager;

    public DefaultLookAndFeelCopier(
            ThemeManager themeManager,
            SettingsManager settingsManager,
            ColourSchemeManager colourSchemeManager
    ) {
        this.themeManager = themeManager;
        this.settingsManager = settingsManager;
        this.colourSchemeManager = colourSchemeManager;
    }

    public void copyLookAndFeel(Space source, Space destination) {
        String originalThemeKey = themeManager.getSpaceThemeKey(source.getKey());
        themeManager.setSpaceTheme(destination.getKey(), originalThemeKey);

        SpaceSettings spaceSettings = settingsManager.getSpaceSettings(source.getKey());
        SpaceSettings newSpaceSettings = new SpaceSettings(destination.getKey());
        newSpaceSettings.setColourSchemesSettings(spaceSettings.getColourSchemesSettings());
        newSpaceSettings.setDisableLogo(spaceSettings.isDisableLogo());
        settingsManager.updateSpaceSettings(newSpaceSettings);

        String colourSchemeSetting = colourSchemeManager.getColourSchemeSetting(source);
        colourSchemeManager.setColourSchemeSetting(destination, colourSchemeSetting);
        colourSchemeManager.saveSpaceColourScheme(destination, colourSchemeManager.getSpaceColourSchemeIsolated(source.getKey()));
    }
}
