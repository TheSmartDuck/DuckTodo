package top.smartduck.ducktodo.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建任务请求体 VO。
 * 包含任务基础信息、协助者列表、子任务列表、附件列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    /** 任务族ID（可选，若提供则以其 teamId 为任务团队ID） */
    private String taskGroupId;

    /** 任务名称（至少2位） */
    private String taskName;
    /** 任务描述（可选） */
    private String taskDescription;
    /** 任务状态（tinyint：0禁用、1未开始、2进行中、3已完成、4已取消），默认1 */
    private Integer taskStatus;
    /** 任务优先级（tinyint；参考数据库 task.task_priority 枚举），可选 */
    private Integer taskPriority;
    /** 开始时间（ISO格式：yyyy-MM-ddTHH:mm:ss，可选） */
    private LocalDate startTime;
    /** 截止时间（ISO格式：yyyy-MM-ddTHH:mm:ss，可选） */
    private LocalDate dueTime;

    /** 协助者用户ID列表（可选） */
    private List<String> helperUserIdList;

    /** 子任务列表（可选） */
    private List<ChildTaskCreate> childTaskList;


    /** 子任务条目 */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildTaskCreate {
        private String childTaskName;
        private Integer childTaskStatus; // 对应 child_task.child_task_status；默认 1 未开始
        private LocalDate dueTime;         // ISO 格式，可选
        private String assigneeUserId;  // 可选
    }
}
