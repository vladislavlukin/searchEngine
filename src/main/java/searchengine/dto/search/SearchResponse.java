package searchengine.dto.search;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private String error;
    private boolean result;
    private int count;
    private List<SearchData> data;
}
