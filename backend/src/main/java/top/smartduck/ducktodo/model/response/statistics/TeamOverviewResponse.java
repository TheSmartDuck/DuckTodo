package top.smartduck.ducktodo.model.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamOverviewResponse {
    private int totalTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int overdueTotal;
    private List<StatusDistributionResponse.Bucket> statusDistribution;
    private List<PriorityDistributionResponse.Bucket> priorityDistribution;
}

