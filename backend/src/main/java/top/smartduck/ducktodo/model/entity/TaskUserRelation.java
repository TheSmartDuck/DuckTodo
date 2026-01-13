package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务与用户关系实体，对应表 `task_user_relation`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_user_relation")
public class TaskUserRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务协助者关系id（UUID）
     */
    @TableId(value = "task_user_relation_id", type = IdType.ASSIGN_UUID)
    private String taskUserRelationId;

    /**
     * 任务id（UUID）
     */
    private String taskId;

    /**
     * 协助者用户id（UUID）
     */
    private String userId;

    /**
     * 是否为任务创建者，0-否，1-是
     */
    private Integer ifOwner;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
