package searchengine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"/site.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PageRepositoryTests {
    private final String PATH = "/example-page";

    private Site site;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private TestEntityManager testEntityManager;


    @BeforeEach
    void setUp() {
        site = siteRepository.findAll().iterator().next();
        this.testEntityManager.persistAndFlush(Page.builder().site(site).path(PATH).build());
    }

    @Test
    public void testFindByPathAndSite(){
        List<Page> pages = pageRepository.findByPathAndSite(PATH, site);
        assertThat(pages.stream().anyMatch(page -> page.getPath().equals(PATH))).isTrue();
    };

    @Test
    public void testDeletePageBySite(){
        pageRepository.deletePageBySite(site);
        assertThat(pageRepository.count()).isEqualTo(0);
    };

}
