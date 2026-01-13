package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusDistributionResponse {
    private List<Bucket> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Bucket {
        private int status;
        private String name;
        private int count;
    }
}

