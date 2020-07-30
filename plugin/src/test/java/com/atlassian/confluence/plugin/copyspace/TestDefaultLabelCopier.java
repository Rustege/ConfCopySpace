package com.atlassian.confluence.plugin.copyspace;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.labels.Labelling;
import com.atlassian.confluence.labels.Namespace;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceDescription;
import com.atlassian.confluence.user.ConfluenceUserImpl;
import com.atlassian.user.impl.DefaultUser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests the methods in the DefaultLabelCopier using a Mock LabelManager.
 */
public class TestDefaultLabelCopier {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private int idCounter = 1;
    @Mock
    private LabelManager mockLabelManager;
    private DefaultLabelCopier labelCopier;


    @Before
    public void setUp() {
        labelCopier = new DefaultLabelCopier();
        labelCopier.setLabelManager(mockLabelManager);
    }

    @Test
    public void testCopyingFromContentWithNoLabelList() {
        final ContentEntityObject originalUnlabelledContent = new Page();
        final ContentEntityObject destinationPage = new Page();

        labelCopier.copyLabels(originalUnlabelledContent, destinationPage, false);
        assertThat(destinationPage.getLabelCount(), is(0));

        labelCopier.copyLabels(originalUnlabelledContent, destinationPage, true);
        assertThat(destinationPage.getLabelCount(), is(0));
    }

    @Test
    public void testCopyingFromContentWithGlobalLabels() {
        final ContentEntityObject labelledContent = createPage();
        final ContentEntityObject destinationPage = createPage();
        Label label1 = createTestLabelOnContent(labelledContent, "foo1", Namespace.GLOBAL);
        Label label2 = createTestLabelOnContent(labelledContent, "foo2", Namespace.GLOBAL);
        Label label3 = createTestLabelOnContent(labelledContent, "foo3", Namespace.GLOBAL);

        when(mockLabelManager.addLabel(eq(destinationPage), eq(label1))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationPage), eq(label2))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationPage), eq(label3))).thenReturn(1);

        labelCopier.copyLabels(labelledContent, destinationPage, false);

        // since we are mocking the mockLabelManager no labels should get added
        assertThat(destinationPage.getLabelCount(), is(0));
    }

    @Test
    public void testCopyingFromContentWithPersonalLabelsAllowed() {
        final ContentEntityObject labelledContent = createPage();
        final ContentEntityObject destinationPage = createPage();
        Label label1 = createTestLabelOnContent(labelledContent, "foo1", Namespace.GLOBAL);
        Label label2 = createTestLabelOnContent(labelledContent, "foo2", Namespace.PERSONAL);
        Label label3 = createTestLabelOnContent(labelledContent, "foo3", Namespace.TEAM);

        when(mockLabelManager.addLabel(eq(destinationPage), eq(label1))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationPage), eq(label2))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationPage), eq(label3))).thenReturn(1);

        labelCopier.copyLabels(labelledContent, destinationPage, true);

        // since we are mocking the mockLabelManager no labels should get added
        assertThat(destinationPage.getLabelCount(), is(0));
    }

    @Test
    public void testCopyingFromContentWithPersonalLabelsDisAllowed() {
        final ContentEntityObject labelledContent = createPage();
        final ContentEntityObject destinationPage = createPage();
        Label label1 = createTestLabelOnContent(labelledContent, "foo1", Namespace.GLOBAL);
        Label label2 = createTestLabelOnContent(labelledContent, "foo2", Namespace.PERSONAL);
        Label label3 = createTestLabelOnContent(labelledContent, "foo3", Namespace.TEAM);

        when(mockLabelManager.addLabel(eq(destinationPage), eq(label1))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationPage), eq(label3))).thenReturn(1);

        labelCopier.copyLabels(labelledContent, destinationPage, false);

        assertThat(destinationPage.getLabelCount(), is(0));
    }

    @Test
    public void testCopyingSpacesWithNoLabelList() {
        final Space originalUnlabelledSpace = createSpace("FROM");
        final Space destinationSpace = createSpace("TO");

        labelCopier.copySpaceLabels(originalUnlabelledSpace, destinationSpace, false);
        assertThat(destinationSpace.getDescription().getLabelCount(), is(0));

        labelCopier.copySpaceLabels(originalUnlabelledSpace, destinationSpace, true);
        assertThat(destinationSpace.getDescription().getLabelCount(), is(0));
    }

    @Test
    public void testCopyingSpacesWithPersonalLabelsAllowed() {
        final Space originalSpace = createSpace("FROM");
        final Space destinationSpace = createSpace("TO");

        Label label1 = createTestLabelOnContent(originalSpace.getDescription(), "foo1", Namespace.GLOBAL);
        Label label2 = createTestLabelOnContent(originalSpace.getDescription(), "foo2", Namespace.PERSONAL);
        Label label3 = createTestLabelOnContent(originalSpace.getDescription(), "foo3", Namespace.TEAM);

        when(mockLabelManager.addLabel(eq(destinationSpace.getDescription()), eq(label1))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationSpace.getDescription()), eq(label2))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationSpace.getDescription()), eq(label3))).thenReturn(1);

        labelCopier.copySpaceLabels(originalSpace, destinationSpace, true);

        // since we are mocking the mockLabelManager no labels should get added
        assertThat(destinationSpace.getDescription().getLabelCount(), is(0));
    }

    @Test
    public void testCopyingSpacesWithPersonalLabelsDisAllowed() {
        final Space originalSpace = createSpace("FROM");
        final Space destinationSpace = createSpace("TO");

        Label label1 = createTestLabelOnContent(originalSpace.getDescription(), "foo1", Namespace.GLOBAL);
        Label label2 = createTestLabelOnContent(originalSpace.getDescription(), "foo2", Namespace.PERSONAL);
        Label label3 = createTestLabelOnContent(originalSpace.getDescription(), "foo3", Namespace.TEAM);

        when(mockLabelManager.addLabel(eq(destinationSpace.getDescription()), eq(label1))).thenReturn(1);
        when(mockLabelManager.addLabel(eq(destinationSpace.getDescription()), eq(label3))).thenReturn(1);

        labelCopier.copySpaceLabels(originalSpace, destinationSpace, false);

        // since we are mocking the mockLabelManager no labels should get added
        assertThat(destinationSpace.getDescription().getLabelCount(), is(0));
    }

    private Space createSpace(String key) {
        Space space = new Space(key);
        space.setDescription(new SpaceDescription(space));
        space.getDescription().setId(idCounter++);
        space.setId(idCounter++);
        return space;
    }


    private ContentEntityObject createPage() {
        final ContentEntityObject labelledContent = new Page();
        labelledContent.setId(idCounter++); // id is set to make the object look persistent
        labelledContent.setTitle("page " + (idCounter++));
        return labelledContent;
    }

    private Label createTestLabelOnContent(ContentEntityObject labelledContent, String labelText, Namespace namespace) {
        Label label = new Label(labelText, namespace);
        label.setId(idCounter++); // id is set to make the object look persistent
        labelledContent.addLabelling(new Labelling(label, labelledContent, new ConfluenceUserImpl(new DefaultUser("bob"))));
        return label;
    }
}
