package searchengine.service.indexing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import searchengine.dto.indexing.Status;
import searchengine.model.Site;
import searchengine.repositories.IdentifierRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.service.task.indexing.LemmaIndexer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class IndexingServiceTest {

    private IndexingService indexingService;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private LemmaIndexer lemmaIndexer;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private LemmaRepository lemmaRepository;

    @Mock
    private IdentifierRepository identifierRepository;

    @Mock
    private ThreadManager threadManager;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        indexingService = new IndexingService(
                siteRepository, lemmaIndexer, pageRepository, lemmaRepository, identifierRepository, threadManager
        );
    }

    @Test
    public void testStartIndexing() {
        Site site1 = Site.builder().status(Status.INDEXED).url("https://example.com").creationTime(LocalDateTime.now()).build();
        Site site2 = Site.builder().status(Status.INDEXING).url("https://noexample.com").creationTime(LocalDateTime.now()).build();
        List<Site> sites = new ArrayList<>();
        sites.add(site1);
        sites.add(site2);

        when(siteRepository.findAll()).thenReturn(sites);
        when(siteRepository.isIndexingStatus()).thenReturn(true);

        indexingService.startIndexing();

        ArgumentCaptor<Thread> threadCaptor = ArgumentCaptor.forClass(Thread.class);

        verify(threadManager, times(1)).addThread(threadCaptor.capture());
        verify(siteRepository, times(1)).findAll();
        verify(threadManager, times(1)).startAllThreads();
        verify(siteRepository, times(1)).isIndexingStatus();

    }

}

