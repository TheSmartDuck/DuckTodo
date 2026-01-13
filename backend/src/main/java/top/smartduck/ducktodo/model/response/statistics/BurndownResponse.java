package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BurndownResponse {
    private List<DayBurn> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayBurn {
        private String date;
        private int remaining;
        private int completed;
    }
}

