package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.RequestResponse;
import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistic.StatisticsResponse;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.Site;
import searchengine.model.site.SiteRepository;
import searchengine.service.indexing.IndexingService;
import searchengine.service.indexing.LemmaService;
import searchengine.service.indexing.SiteService;
import searchengine.service.search.SearchService;
import searchengine.service.statistic.StatisticsService;
import searchengine.service.task.indexing.indexing.ErrorsHandler;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SearchService searchService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private List<Thread> threads = new ArrayList<>();

    public ApiController(StatisticsService statisticsService, SearchService searchService,
                         SiteRepository siteRepository, PageRepository pageRepository,
                         LemmaRepository lemmaRepository, IndexRepository indexRepository) {

        this.statisticsService = statisticsService;
        this.searchService = searchService;
        this.siteRepository = siteRepository;
        this.indexRepository = indexRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
    }
    @PostMapping("/indexPage")
    public void addSite(Site site) {
        SiteService siteService = new SiteService(siteRepository,pageRepository);
        siteService.addSite(site.getUrl(), threads);
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<RequestResponse> startIndexing() throws Exception {
        LemmaService lemmaService = new LemmaService(pageRepository, lemmaRepository, indexRepository);
        IndexingService indexingService = new IndexingService(siteRepository, pageRepository, lemmaService);
        if(ErrorsHandler.returnError(siteRepository, threads)){
            return ResponseEntity.ok(new RequestResponse(false, ErrorsHandler.textError));
        }
        indexingService.startIndexing();
        threads = indexingService.getThreads();
        return ResponseEntity.ok(new RequestResponse(true, ""));
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<RequestResponse> stopIndexing() {
        IndexingService indexingService = new IndexingService(siteRepository);
        if (indexingService.isIndexing()) {
            indexingService.stopIndexing(threads);
            return ResponseEntity.ok(new RequestResponse(true, ""));
        }
        return ResponseEntity.ok(new RequestResponse(false, "Индексация не запущена"));
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics(siteRepository, pageRepository, lemmaRepository));
    }
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(SearchFormat searchFormat) throws IOException {
        return ResponseEntity.ok(searchService.getResponse(searchFormat, siteRepository, pageRepository, lemmaRepository, indexRepository));
    }
}
