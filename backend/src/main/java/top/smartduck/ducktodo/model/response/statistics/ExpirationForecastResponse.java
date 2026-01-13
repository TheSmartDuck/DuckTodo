package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpirationForecastResponse {
    private List<Bucket> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bucket {
        private String bucket;
        private int count;
    }
}

