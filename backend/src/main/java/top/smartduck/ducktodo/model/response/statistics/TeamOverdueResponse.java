package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamOverdueResponse {
    private int total;
    private List<MemberBucket> byMember;
    private List<TaskGroupBucket> byTaskGroup;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberBucket {
        private String userId;
        private String userName;
        private int count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskGroupBucket {
        private String taskGroupId;
        private String taskGroupName;
        private int count;
    }
}

