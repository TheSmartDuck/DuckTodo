package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 我的任务族列表项：融合 TaskGroup、TaskGroupUserRelationship 以及 Team 的常用字段。
 * 便于前端直接渲染任务族基础信息、与当前用户的关系信息、以及关联团队信息。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTaskGroupResponse {
    // 任务族信息（来自 task_group）
    private String taskGroupId;
    private String teamId; // 为空或空串表示私有任务族
    private String groupName;
    private String groupDescription;
    private Integer groupStatus; // 0禁用、1正常

    // 关系信息（来自 task_group_user_relationship）
    private Integer userRole;
    private String groupAlias;
    private String groupColor;
    private Integer groupIndex;
    private LocalDateTime joinTime;
    private LocalDateTime updateTime; // 关系更新时间

    // 派生信息
    private boolean isPrivate; // 是否为私有任务族

    // 团队信息（来自 team），当为私有任务族时这些字段可能为空
    private String teamName;
    private String teamDescription;
    private String teamAvatar;
    private Integer teamStatus; // 0禁用、1运行中、2已结束
}
