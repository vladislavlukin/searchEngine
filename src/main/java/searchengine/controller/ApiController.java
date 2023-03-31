package searchengine.controller;

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
import searchengine.service.indexingService.IndexingService;
import searchengine.service.indexingService.LemmaService;
import searchengine.service.indexingService.SiteService;
import searchengine.service.searchService.SearchService;
import searchengine.service.statisticService.StatisticsService;
import searchengine.service.task.indexing.Errors;

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
    private Set<String> sites = new HashSet<>();
    private List<Thread> thread = new ArrayList<>();
    private String nameURL;

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
        nameURL = site.getUrl();
        sites = siteService.addSite(nameURL, thread, sites);
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<RequestResponse> startIndexing() throws Exception {
        LemmaService lemmaService = new LemmaService(pageRepository, lemmaRepository, indexRepository);
        IndexingService indexingService = new IndexingService(siteRepository, pageRepository, lemmaService);
        if(Errors.responseError(sites, thread, indexingService)){
            return ResponseEntity.ok(new RequestResponse(false, Errors.textError));
        }
        thread = indexingService.startThread(sites);
        return ResponseEntity.ok(new RequestResponse(true, ""));
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<RequestResponse> stopIndexing() {
        IndexingService indexingService = new IndexingService(siteRepository);
        if (indexingService.isIndexing(thread, sites)) {
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
        return ResponseEntity.ok(searchService.getSearch(searchFormat, siteRepository, pageRepository, lemmaRepository, indexRepository));
    }
}
