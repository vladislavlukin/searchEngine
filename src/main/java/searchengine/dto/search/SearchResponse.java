package searchengine.dto.search;

@lombok.Data
public class SearchResponse {
    private String error;
    private boolean result;
    private int count;
    private SearchData data;
}
