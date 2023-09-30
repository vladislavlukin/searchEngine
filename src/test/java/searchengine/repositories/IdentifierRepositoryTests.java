package searchengine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"/sql_script/indexedSite.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class IdentifierRepositoryTests {
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private IdentifierRepository identifierRepository;

    private Site site;
    private Page page;
    private Lemma lemma;


    @BeforeEach
    void setUp() {
        site = siteRepository.findAll().iterator().next();
        page = pageRepository.findAll().iterator().next();
        lemma = lemmaRepository.findAll().iterator().next();
    }

    @Test
    public void findPagesByLemmaTest() {
        List<Page> pages = identifierRepository.findPagesByLemma(lemma);

        assertThat(pages).extracting(Page::getPath)
                .contains("/page1", "/page2");

        assertThat(pages).extracting(Page::getPath)
                .doesNotContain("/page3");
    };

    @Test
    public void countLemmaNameInPageTest() {
        int countLemmaNameInPage = identifierRepository.countLemmaNameInPage(lemma, page);

        assertThat(countLemmaNameInPage).isEqualTo(1);
    };

    @Test
    public void testDeleteIndexBySite() {
        int idSite = site.getId();
        identifierRepository.deleteIndexBySite(site);
        assertThat(identifierRepository.findById(idSite)).isEmpty();
    };
}
