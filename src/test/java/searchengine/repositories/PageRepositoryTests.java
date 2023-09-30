package searchengine.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"/sql_script/pageTest.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PageRepositoryTests {
    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

    private Site site;


    @BeforeEach
    void setUp() {
        site = siteRepository.findAll().iterator().next();
    }

    @Test
    public void testFindByPathAndSite(){
        List<Page> pages = pageRepository.findByPathAndSite("/page1", site);

        assertThat(pages.stream().anyMatch(page -> page.getPath().equals("/page1"))).isTrue();
        assertThat(pages.stream().anyMatch(page -> page.getSite().equals(site))).isTrue();
    };

    @Test
    public void testDeletePageBySite(){
        pageRepository.deletePageBySite(site);

        assertThat(pageRepository.count()).isEqualTo(1);

        Site site2 = pageRepository.findAll().iterator().next().getSite();

        assertThat(site2).isNotEqualTo(site);

        pageRepository.deletePageBySite(site2);

        assertThat(pageRepository.count()).isEqualTo(0);
    };

}
