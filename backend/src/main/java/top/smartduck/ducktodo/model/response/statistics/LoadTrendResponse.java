package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadTrendResponse {
    private List<DayLoad> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayLoad {
        private String date;
        private int tasks;
        private int childTasks;
    }
}

