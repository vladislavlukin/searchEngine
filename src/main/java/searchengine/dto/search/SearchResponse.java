package searchengine.dto.search;

import java.util.List;

@lombok.Data
public class SearchResponse {
    private String error;
    private boolean result;
    private int count;
    private List<SearchData> data;
}
