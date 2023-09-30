package searchengine.service.indexing;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import searchengine.dto.indexing.Status;
import searchengine.model.Site;
import searchengine.repositories.IdentifierRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;


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

    private SiteService siteService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        siteService = new SiteService(siteRepository, pageRepository, lemmaRepository, identifierRepository);
    }

    @Test
    public void testAddValidSite() throws IOException {
        String validUrl = "http://example.com";

        when(siteRepository.existsByURL(validUrl)).thenReturn(false);

        siteService.addSite(validUrl);

        verify(siteRepository).save(argThat(site -> site.getStatus() == Status.INDEXING));
    }

    @Test
    public void testAddInvalidSite() {
        String invalidUrl = "http://examplecom";

        when(siteRepository.existsByURL(invalidUrl)).thenReturn(false);

        siteService.addSite(invalidUrl);

        verify(siteRepository).save(argThat(site -> site.getStatus() == Status.FAILED));
    }

    @Test
    public void testAddSiteWithShortUrl() {
        String shortUrl = "example.com";
        String url = "http://example.com";

        when(siteRepository.existsByURL(url)).thenReturn(false);

        siteService.addSite(shortUrl);

        verify(siteRepository).save(argThat(site -> url.equals(site.getUrl())));
    }


    @Test
    public void testDeleteSite() {
        String existingUrl = "https://example.com";

        Site existingSite = new Site();
        existingSite.setUrl(existingUrl);;

        when(siteRepository.findByUrl(existingUrl)).thenReturn(existingSite);
        when(siteRepository.existsByURL(existingUrl)).thenReturn(true);

        siteService.addSite(existingUrl);

        verify(pageRepository, times(1)).deletePageBySite(any(Site.class));
        verify(lemmaRepository, times(1)).deleteLemmaBySite(any(Site.class));
        verify(identifierRepository, times(1)).deleteIndexBySite(any(Site.class));
        verify(siteRepository, times(1)).delete(any(Site.class));
    }
}


