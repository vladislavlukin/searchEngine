package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchFormat {
    private String query;
    private String site;
    private int offset;
    private int limit;
}
