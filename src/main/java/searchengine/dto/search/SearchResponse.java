package searchengine.dto.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {
    private String error;
    private boolean result;
    private int count;
    private int offset;
    private int limit;
    private List<SearchData> data;
}
