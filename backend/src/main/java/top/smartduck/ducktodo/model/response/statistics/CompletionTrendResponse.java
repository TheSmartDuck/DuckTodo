package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompletionTrendResponse {
    private List<DayCompletion> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayCompletion {
        private String date;
        private double rate;
        private int created;
        private int completed;
    }
}

