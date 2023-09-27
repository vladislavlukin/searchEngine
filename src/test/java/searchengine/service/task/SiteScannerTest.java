package searchengine.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.service.task.indexing.SiteScanner;

import java.time.LocalDateTime;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SiteScannerTest {

    private SiteScanner siteScanner;

    @BeforeEach
    public void setUp() {
        Site site = Site.builder()
                .url("https://example.com")
                .creationTime(LocalDateTime.now())
                .build();
        siteScanner = new SiteScanner(site.getUrl(), site);
    }

    @Test
    public void testSiteScanner() {
        CopyOnWriteArrayList<Page> result = siteScanner.compute();

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (Page page : result) {
            assertNotNull(page.getContent());
            assertNotNull(page.getPath());
            assertNotNull(page.getSite());
            assertTrue(page.getCode() >= 200 && page.getCode() < 400);
        }
    }
}


