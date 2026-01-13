package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTrendResponse {
    private List<DayTrend> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayTrend {
        private String date;
        private int created;
        private int completed;
    }
}

