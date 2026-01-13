package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务族实体，对应表 `task_group`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_group")
public class TaskGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务族id（UUID）
     */
    @TableId(value = "task_group_id", type = IdType.ASSIGN_UUID)
    private String taskGroupId;

    /**
     * 所属项目团队id（UUID）
     */
    private String teamId;

    /**
     * 任务族名称，要求2位以上
     */
    private String groupName;

    /**
     * 任务族描述
     */
    private String groupDescription;

    /**
     * 任务族状态，0-禁用，1-正常
     */
    private Integer groupStatus;

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
