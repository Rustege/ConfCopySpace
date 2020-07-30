package it.webdriver;

import com.atlassian.confluence.api.model.Expansions;
import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.content.ContentRepresentation;
import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.api.model.longtasks.LongTaskSubmission;
import com.atlassian.confluence.test.rest.api.ConfluenceRestClient;
import com.atlassian.confluence.test.stateless.ConfluenceStatelessTestRunner;
import com.atlassian.confluence.test.stateless.fixtures.Fixture;
import com.atlassian.confluence.test.stateless.fixtures.PageFixture;
import com.atlassian.confluence.test.stateless.fixtures.SpaceFixture;
import com.atlassian.confluence.test.stateless.fixtures.UserFixture;
import com.atlassian.confluence.webdriver.pageobjects.ConfluenceTestedProduct;
import com.atlassian.confluence.webdriver.pageobjects.page.content.ViewPage;
import com.atlassian.pageobjects.elements.query.Conditions;
import it.webdriver.pageobjects.CopySpacePage;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Collection;

import static com.atlassian.confluence.test.rpc.api.permissions.SpacePermission.ADMIN_PERMISSIONS;
import static com.atlassian.confluence.test.stateless.fixtures.PageFixture.pageFixture;
import static com.atlassian.confluence.test.stateless.fixtures.SpaceFixture.spaceFixture;
import static com.atlassian.confluence.test.stateless.fixtures.UserFixture.userFixture;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilEquals;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(ConfluenceStatelessTestRunner.class)
public class CopySpaceAcceptanceTest {

    private static final String NEW_SPACE_KEY = "NEW";

    @Inject
    private static ConfluenceTestedProduct product;
    @Inject
    private static ConfluenceRestClient restClient;

    @Fixture
    private static UserFixture user = userFixture().build();
    @Fixture
    private static SpaceFixture space = spaceFixture()
            .permission(user, ADMIN_PERMISSIONS)
            .build();
    @Fixture
    private static PageFixture grandParent = pageFixture()
            .space(space)
            .author(user)
            .title("Grand Parent")
            .content("Grand Parent")
            .build();
    @Fixture
    private static PageFixture parent = pageFixture()
            .space(space)
            .author(user)
            .title("Parent")
            .content("Parent")
            .build();
    @Fixture
    private static PageFixture child = pageFixture()
            .space(space)
            .author(user)
            .title("Child")
            .content("Child")
            .build();

    @After
    public void clearSpaces() {
        LongTaskSubmission taskSubmission = restClient.getAdminSession().spaceService().delete(Space.builder().key(NEW_SPACE_KEY).build());
        waitUntilTrue(
                Conditions.forSupplier(
                        () -> restClient.getAdminSession()
                                .longTaskService()
                                .get(taskSubmission.getId())
                                .map(task -> task != null && task.isSuccessful())
                                .getOrElse(false)
                )
        );
    }

    @Test
    public void copySpaceAndVerifyCopy() {
        final CopySpacePage copySpacePage = product.login(user.get(), CopySpacePage.class, space.get());
        copySpacePage.setCopyKey(NEW_SPACE_KEY).
                setCopyName("Copy of Space")
                .submit();


        final Space newSpace = restClient.createSession(user.get())
                .spaceService()
                .find(Expansions.of("homepage").toArray())
                .withKeys("NEW")
                .fetchOneOrNull();

        assertNotNull("New copied space should exist", newSpace);

        final ViewPage newSpaceHomePage = product.visit(ViewPage.class, newSpace.getHomepageRef().get().getId());

        assertThat(
                "Space key of new space home page should be correct",
                newSpaceHomePage.getSpaceKey(),
                is("NEW")
        );
        assertThat(

                "New space homepage title should be same as original space homepage title",
                newSpaceHomePage.getTitle(),
                is(space.get().getHomepageRef().get().getTitle())
        );

        final Collection<Content> originalPages = asList(grandParent.get(), parent.get(), child.get());

        for (Content originalPage : originalPages) {
            final Content newPage = restClient.getAdminSession()
                    .contentService()
                    .find()
                    .withTitle(originalPage.getTitle())
                    .withSpace(newSpace)
                    .fetchOneOrNull();
            assertNotNull("New space should contain a copied page with title: '" + originalPage.getTitle() + "'", newPage);
            final ViewPage viewNewPage = product.visit(ViewPage.class, newPage.getId());
            assertThat("Space key of new page should be correct", viewNewPage.getSpaceKey(), is("NEW"));
            assertThat("New space page title should be same as original space page title", viewNewPage.getTitle(), is(originalPage.getTitle()));
            waitUntilEquals(
                    "New space page content should be same as original space page content",
                    originalPage.getBody().get(ContentRepresentation.STORAGE).getValue(),
                    viewNewPage.getMainContent().timed().getText()
            );
        }

    }
}
