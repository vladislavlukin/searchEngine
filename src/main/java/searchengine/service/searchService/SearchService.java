package searchengine.service.searchService;

import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;
import searchengine.model.lemma.IndexRepository;
import searchengine.model.lemma.LemmaRepository;
import searchengine.model.site.PageRepository;
import searchengine.model.site.SiteRepository;

import java.io.IOException;

public interface SearchService {
    SearchResponse getSearch(SearchFormat searchFormat, SiteRepository siteRepository, PageRepository pageRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) throws IOException;
}
