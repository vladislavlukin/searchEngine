package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.RequestResponse;
import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.Site;
import searchengine.model.site.SiteRepository;
import searchengine.model.site.Status;
import searchengine.services.indexing.*;
import searchengine.services.search.SearchService;
import searchengine.services.statistics.StatisticsService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

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
    private List<Thread> thread;
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
        nameURL = site.getUrl();
        AddSite addSite = new AddSite(siteRepository,pageRepository, lemmaRepository,
                indexRepository, nameURL, sites, thread);
        addSite.addUpdate();
        sites = new HashSet<>(addSite.returnSites());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<RequestResponse> startIndexing() throws Exception {
        LemmaIndexing lemmaIndexing = new LemmaIndexing(pageRepository, lemmaRepository, indexRepository);
        StartIndexing startIndexing = new StartIndexing(siteRepository, pageRepository, lemmaIndexing);
        if(ResponseIndexing.responseError(sites, thread, startIndexing)){
            return ResponseEntity.ok(new RequestResponse(false, ResponseIndexing.textError));
        }
        thread = new ArrayList<>();
        for (String nameSite : sites) {
            thread.add(new Thread(() -> {
                startIndexing.startIndexing(nameSite);
            }));
        }
        sleep(1000);
        thread.forEach(Thread::start);

        return ResponseEntity.ok(new RequestResponse(true, ""));
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<RequestResponse> stopIndexing() {
        if (StopIndexing.stop(sites, siteRepository) > 0) {
            thread.forEach(Thread::stop);
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
