package com.atlassian.confluence.plugin.copyspace.actions;

import com.atlassian.bandana.BandanaManager;
import com.atlassian.confluence.pages.actions.AbstractPageAction;
import com.atlassian.confluence.plugin.copyspace.CopySpaceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for page that links to various translations.
 *
 * @author don.willis@atlassian.com
 */
public class ListPageTranslationsAction extends AbstractPageAction {
    private BandanaManager bandanaManager;
    private CopySpaceManager copySpaceManager;

    private List translationLinks = new ArrayList();

    private long mainPageId;

    // No code worth to test in this class at the moment
///CLOVER:OFF
    public String execute() throws Exception {
//        ConfluenceBandanaContext spaceContext = new ConfluenceBandanaContext(getPage().getSpace());
//        if (!copySpaceManager.isMainSpace(spaceContext))
//        {
//            mainPageId = copySpaceManager.getMainPageId(getPage(), spaceContext, spaceManager).longValue();
//            return "notMain";
//        }
//
//        for (Iterator it = copySpaceManager.getTranslationLanguages().iterator(); it.hasNext();)
//        {
//            TranslationLanguage language = (TranslationLanguage) it.next();
//            copySpaceManager.getSpaceForLanguage(getPage().getSpace(), spaceManager, language.getKey());
//            Long translationId = copySpaceManager.getPageIdForLanguage(getPage(), language.getKey());
//            Page translatedPage = (translationId == null) ? null : pageManager.getPage(translationId.longValue());
//            translationLinks.add(new TranslationLink(language, translatedPage));
//        }
        return SUCCESS;
    }


    public List getTranslationLinks() {
        return translationLinks;
    }

    //    public AbstractPage getPage()
//    {
//        System.out.println("page = " + page);
//        return page;
//    }
//
//    public void setPage(AbstractPage page)
//    {
//        System.out.println("page = " + page);
//        this.page = page;
//    }
//
//    public boolean isPageRequired()
//    {
//        return true;
//    }
//
//    public boolean isLatestVersionRequired()
//    {
//        return false;
//    }
//
//    public boolean isViewPermissionRequired()
//    {
//        return true;
//    }
//
    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    public BandanaManager getBandanaManager() {
        return bandanaManager;
    }

    public CopySpaceManager getTransfluenceManager() {
        return copySpaceManager;
    }

    public void setTransfluenceManager(CopySpaceManager copySpaceManager) {
        this.copySpaceManager = copySpaceManager;
    }

    public long getMainPageId() {
        return mainPageId;
    }

    public static class TranslationLink {
//        private TranslationLanguage language;
//        private Page page;
//
//        public TranslationLink(TranslationLanguage language, Page pageId)
//        {
//
//            this.language = language;
//            this.page = pageId;
//        }
//
//        public TranslationLanguage getLanguage()
//        {
//            return language;
//        }
//
//        public Page getPage()
//        {
//            return page;
//        }
//
//        public boolean linkExists()
//        {
//            return page != null;
//        }
    }

///CLOVER:ON
}
