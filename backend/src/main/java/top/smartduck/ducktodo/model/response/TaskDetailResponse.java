package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.smartduck.ducktodo.model.entity.ChildTask;
import top.smartduck.ducktodo.model.entity.Task;
import top.smartduck.ducktodo.model.entity.TaskFile;
import top.smartduck.ducktodo.model.entity.Team;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务详情响应：包含任务基础信息、绑定团队信息、融合协助者列表、子任务列表、附件列表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailResponse {
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

    private List<TaskHelper> taskHelperList;

    private List<ChildTask> childTaskList;

    private List<TaskFile> attachments;

    /**
     * 任务协助者详情（内部类）：融合 User 与 TaskUserRelationship 的主要字段。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskHelper {
        // 来自 TaskUserRelationship 的字段
        private String taskUserRelationshipId;
        private String userId;
        private Boolean ifOwner;
        private String userName;
        private String userEmail;
        private String userPhone;
        private Integer userSex;
        private String userAvatar;
        private String userRemark;
    }
}