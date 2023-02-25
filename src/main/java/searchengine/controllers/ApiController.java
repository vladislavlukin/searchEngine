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
import searchengine.services.delete.DeleteSite;
import searchengine.services.indexing.AddSite;
import searchengine.services.indexing.LemmaIndexing;
import searchengine.services.indexing.StartIndexing;
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
    @Autowired
    private SearchService searchService;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    private Set<String> sites = new HashSet<>();
    private List<Thread> thread;
    private String nameURL;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    public void addURL(){
        AddSite addSite = new AddSite(siteRepository, nameURL);
        DeleteSite deleteSite = new DeleteSite(siteRepository, pageRepository, lemmaRepository, indexRepository);
         siteRepository.findAll().forEach(site -> {
            sites.add(site.getUrl());
        });
         String regex = "http://";
         String regex1 = "https://";

        if (sites.contains(nameURL)) {
            deleteSite.delete(nameURL);
            sites.remove(nameURL);
        }
        if (!sites.contains(nameURL) && (nameURL.startsWith(regex) || nameURL.startsWith(regex1))) {
            addSite.add();
            sites.add(nameURL);
        }
    }
    @PostMapping("/indexPage")
    public void addSite(Site site) {
        nameURL = site.getUrl();
        if (!site.getUrl().isEmpty()) {
            if (thread == null) {
                addURL();
            } else {
                int i = 0;
                for (Thread newThread : thread) {
                    if (newThread.isAlive()) {
                        i++;
                    }
                }
                if (i == 0){
                    addURL();
                }
            }
        }
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<RequestResponse> startIndexing() throws Exception {
        if (nameURL == null || nameURL.isEmpty()) {
            return ResponseEntity.ok(new RequestResponse(false, "Добавтье не менее одного сайта или обновите текущий"));
        }

        if (thread != null) {
            for (Thread newThread : thread) {
                if (newThread.isAlive()) {
                    return ResponseEntity.ok(new RequestResponse(false, "Индексикация уже запущена"));
                }
            }
        }
        LemmaIndexing lemmaIndexing = new LemmaIndexing(pageRepository, lemmaRepository, indexRepository);
        StartIndexing startIndexing = new StartIndexing(siteRepository, pageRepository, lemmaIndexing);

        int stop = 0;
        for (String nameSite : sites){
            if(!startIndexing.statusIndexing(nameSite)){
                stop++;
            }
        }
        if(stop == sites.size()){
            return ResponseEntity.ok(new RequestResponse(false, "Все сайты проиндексированы или с ошибкой!" +
                    " Если только добавили сайт, то поробуйте запустить индаксацию позднее."));
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
        AtomicInteger i = new AtomicInteger();
        siteRepository.findAll().forEach(site -> {
            sites.forEach(s -> {
                if (site.getUrl().equals(s) && site.getStatus().equals(Status.INDEXING)) {
                    site.setStatus(Status.INDEXED);
                    site.setError("Индексация остановлена пользователем");
                    site.setCreationTime(null);
                    siteRepository.save(site);
                    i.getAndIncrement();
                }
            });
        });
        if (i.get() > 0) {
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
