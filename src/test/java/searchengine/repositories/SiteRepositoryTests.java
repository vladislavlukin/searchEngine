package searchengine.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import searchengine.model.Site;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = {"/site.sql"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SiteRepositoryTests {
    private final String URL = "https://example.com";

    @Autowired
    private SiteRepository siteRepository;

    @Test
    public void testIndexingStatus() {
        boolean isIndexing = siteRepository.isIndexingStatus();
        assertThat(isIndexing).isTrue();
    }

    @Test
    public void testExistsByUrl() {
        boolean existsByUrl = siteRepository.existsByURL(URL);
        assertThat(existsByUrl).isTrue();
    }

    @Test
    public void testFindByUrl() {
        Site foundSite = siteRepository.findByUrl(URL);
        assertThat(foundSite.getUrl()).isEqualTo(URL);
    }
}
