package it.webdriver.pageobjects;

import com.atlassian.confluence.api.model.content.Space;
import com.atlassian.confluence.webdriver.pageobjects.page.ConfluenceAbstractPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

import static java.util.Objects.requireNonNull;

public class CopySpacePage extends ConfluenceAbstractPage {

    private final Space space;

    @ElementBy(id = "newName")
    PageElement newName;

    @ElementBy(id = "newKey")
    PageElement newKey;

    @ElementBy(name = "confirm")
    PageElement submitButton;

    public CopySpacePage(final Space space) {
        this.space = requireNonNull(space);
    }

    @Override
    public String getUrl() {
        return "/spaces/copyspaceoptions.action?key=" + space.getKey();
    }

    public CopySpacePage setCopyName(String name) {
        newName.clear().type(name);
        return this;
    }

    public CopySpacePage setCopyKey(String key) {
        newKey.clear().type(key);
        return this;
    }

    public void submit() {
        submitButton.click();
    }
}
