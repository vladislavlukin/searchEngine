package searchengine.dto.indexing;

import lombok.Data;

@Data
public class RequestResponse {

    private boolean result;
    private String error;

    public RequestResponse(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
    public RequestResponse(boolean result) {
        this.result = result;
    }

}
