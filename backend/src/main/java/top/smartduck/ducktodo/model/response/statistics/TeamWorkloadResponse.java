package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamWorkloadResponse {
    private List<MemberWorkload> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberWorkload {
        private String userId;
        private String userName;
        private int inProgress;
        private int total;
    }
}

