package searchengine.service.indexing;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import searchengine.model.Site;
import searchengine.repositories.IdentifierRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;


@SpringBootTest
public class SiteServiceTests {
    @Mock
    private SiteRepository siteRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private LemmaRepository lemmaRepository;

    @Mock
    private IdentifierRepository identifierRepository;

    @Mock
    private ThreadManager threadManager;

    @Mock
    private SiteService siteService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        siteService = new SiteService(siteRepository, pageRepository, lemmaRepository, identifierRepository, threadManager);
    }

    @Test
    public void testAddValidSite() {
        String validUrl = "https://example.com";

        when(siteRepository.existsByURL(validUrl)).thenReturn(false);

        siteService.addSite(validUrl);

        verify(siteRepository, times(1)).save(any(Site.class));
    }


    @Test
    @Rollback(value = false)
    public void testAddExistingSiteWithDeadThreads() {
        String existingUrl = "https://example.com";

        Site existingSite = new Site();
        existingSite.setUrl(existingUrl);;

        when(siteRepository.findByUrl(existingUrl)).thenReturn(existingSite);
        when(siteRepository.existsByURL(existingUrl)).thenReturn(true);
        when(threadManager.areThreadsNotAlive()).thenReturn(true);

        siteService.addSite(existingUrl);

        verify(pageRepository, times(1)).deletePageBySite(any(Site.class));
        verify(lemmaRepository, times(1)).deleteLemmaBySite(any(Site.class));
        verify(identifierRepository, times(1)).deleteIndexBySite(any(Site.class));
        verify(siteRepository, times(1)).delete(any(Site.class));
    }


    @Test
    public void testAddInvalidURL() {
        String invalidUrl = "invalid_url";

        siteService.addSite(invalidUrl);

        verify(siteRepository, never()).save(any(Site.class));
    }
}


