package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务族与用户关系实体，对应表 `task_group_user_relation`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_group_user_relation")
public class TaskGroupUserRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务族与用户关系id（UUID）
     */
    @TableId(value = "task_group_user_relation_id", type = IdType.ASSIGN_UUID)
    private String taskGroupUserRelationId;

    /**
     * 任务族id（UUID）
     */
    private String taskGroupId;

    /**
     * 用户id（UUID）
     */
    private String userId;

    /**
     * 用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）
     */
    private Integer userRole;

    /**
     * 用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝
     */
    private Integer userStatus;

    /**
     * 任务族索引，用于用户界面排序
     */
    private Integer groupIndex;

    /**
     * 任务族颜色，用于用户界面显示
     */
    private String groupColor;

    /**
     * 任务族别名（用户自定义显示）
     */
    private String groupAlias;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 加入时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
