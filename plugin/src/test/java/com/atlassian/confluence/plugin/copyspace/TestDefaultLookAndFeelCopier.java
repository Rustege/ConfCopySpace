package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.setup.settings.SpaceSettings;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceDescription;
import com.atlassian.confluence.themes.ColourSchemeManager;
import com.atlassian.confluence.themes.ThemeManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the look and feel copier works correctly.
 */
public class TestDefaultLookAndFeelCopier {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private DefaultLookAndFeelCopier lookAndFeelCopier;

    @Mock
    private ThemeManager themeManager;
    @Mock
    private SettingsManager settingsManager;
    @Mock
    private ColourSchemeManager colourSchemeManager;


    @Before
    public void setUp() {
        lookAndFeelCopier = new DefaultLookAndFeelCopier(themeManager, settingsManager, colourSchemeManager);
    }

    @Test
    public void testCopyLookAndFeelWithNoThemesSet() {
        Space source = createSpace("SRC");
        Space destination = createSpace("DST");
        when(themeManager.getSpaceThemeKey(eq("SRC"))).thenReturn(null);
        when(settingsManager.getSpaceSettings(eq(source.getKey()))).thenReturn(new SpaceSettings("SRC"));
        when(colourSchemeManager.getColourSchemeSetting(eq(source))).thenReturn(null);
        when(colourSchemeManager.getSpaceColourSchemeIsolated(eq(source.getKey()))).thenReturn(null);

        lookAndFeelCopier.copyLookAndFeel(source, destination);

        verify(themeManager).setSpaceTheme(eq("DST"), isNull());
        verify(settingsManager).updateSpaceSettings(isNotNull());
        verify(colourSchemeManager).setColourSchemeSetting(eq(destination), isNull());
        verify(colourSchemeManager).saveSpaceColourScheme(eq(destination),isNull());
    }

    private Space createSpace(String key) {
        Space space = new Space(key);
        space.setDescription(new SpaceDescription(space));
        return space;
    }
}
