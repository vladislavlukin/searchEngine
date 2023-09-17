package searchengine.service.search;

import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.io.IOException;

public interface SearchService {
    SearchResponse getResponse(SearchFormat searchFormat) throws IOException;
}
