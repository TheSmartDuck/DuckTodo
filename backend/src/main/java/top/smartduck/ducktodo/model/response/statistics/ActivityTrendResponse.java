package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTrendResponse {
    private List<DayActivity> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayActivity {
        private String date;
        private int count;
    }
}

