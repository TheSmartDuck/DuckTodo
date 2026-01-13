package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 任务实体，对应表 `task`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务id（UUID）
     */
    @TableId(value = "task_id", type = IdType.ASSIGN_UUID)
    private String taskId;

    /**
     * 所属任务族id（UUID）
     */
    private String taskGroupId;

    /**
     * 所属项目团队id（UUID）
     */
    private String teamId;

    /**
     * 任务标题，要求2位以上
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String taskDescription;

    /**
     * 任务状态，0为已禁用，1为未开始，2为进行中，3为已完成，5为已取消
     */
    private Integer taskStatus;

    /**
     * 任务优先级，0为无，1为P3|低优先级，2为P2|中优先级，3为P1|高优先级，4为P0|紧急优先级
     */
    private Integer taskPriority;

    /**
     * 开始时间
     */
    private LocalDate startTime;

    /**
     * 截止时间
     */
    private LocalDate dueTime;

    /**
     * 完成时间
     */
    private LocalDate finishTime;

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
