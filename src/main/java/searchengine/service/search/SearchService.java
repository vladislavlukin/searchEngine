package searchengine.service.search;

import searchengine.dto.search.SearchFormat;
import searchengine.dto.search.SearchResponse;

import java.io.IOException;

public interface SearchService {
    SearchResponse getResponse(SearchFormat searchFormat) throws IOException;
}
