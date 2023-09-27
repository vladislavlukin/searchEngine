package searchengine.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.RequestResponse;
import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistic.StatisticsResponse;
import searchengine.service.indexing.IndexingService;
import searchengine.service.indexing.SiteService;
import searchengine.service.search.SearchService;
import searchengine.service.statistic.StatisticsService;

import java.io.IOException;


@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiController {
    private final StatisticsService statisticsService;
    private final SearchService searchService;
    private final SiteService siteService;
    private final IndexingService indexingService;

    @PostMapping("/indexPage")
    public void addSite(String url) {
        siteService.addSite(url);
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<RequestResponse> startIndexing() {
        try {
            indexingService.startIndexing();
            return ResponseEntity.ok(new RequestResponse(true));
        }catch (IllegalArgumentException e){
            return ResponseEntity.ok(new RequestResponse(false, e.getMessage()));
        }
    }
    @GetMapping("/stopIndexing")
    public ResponseEntity<RequestResponse> stopIndexing() {
        try {
            indexingService.stopIndexing();
            return ResponseEntity.ok(new RequestResponse(true));
        }catch (IllegalArgumentException e){
            return ResponseEntity.ok(new RequestResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(SearchFormat searchFormat) throws IOException {
        return ResponseEntity.ok(searchService.getResponse(searchFormat));
    }
}
