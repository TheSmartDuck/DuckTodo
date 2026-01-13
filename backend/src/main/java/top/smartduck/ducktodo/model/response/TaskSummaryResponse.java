package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.smartduck.ducktodo.model.entity.ChildTask;
import top.smartduck.ducktodo.model.entity.TaskFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务列表项摘要响应（精简版）：
 * - task：仅必要字段
 * - taskGroup：仅 groupName
 * - taskGroupUserRelationship：仅当前用户的关系ID、颜色、别名
 * - team：仅 teamId
 * - taskUserRelationship：仅当前用户与任务的关系（ID、是否拥有者、状态）
 * - childTasks：精简子任务列表（含执行者ID与姓名）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryResponse {
    private String taskId;

    private String taskGroupId;
    private String taskGroupName;

    private String teamId;
    private String teamName;

    private String taskName;
    private String taskDescription;
    private Integer taskStatus;
    private Integer taskPriority;

    private LocalDate startTime;
    private LocalDate dueTime;
    private LocalDate finishTime;

    private Integer isOwner;

    private List<ChildTask> childTaskList;
}