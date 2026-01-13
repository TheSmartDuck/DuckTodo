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
 * 子任务实体，对应表 `child_task`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("child_task")
public class ChildTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 子任务id（UUID）
     */
    @TableId(value = "child_task_id", type = IdType.ASSIGN_UUID)
    private String childTaskId;

    /**
     * 父任务id（UUID）
     */
    private String taskId;

    /**
     * 子任务标题，要求2位以上（标题即为描述）
     */
    private String childTaskName;

    /**
     * 子任务状态，0-已禁用，1-未开始，2-进行中，3-已完成，4-已取消
     */
    private Integer childTaskStatus;

    /**
     * 子任务索引，用于用户界面排序
     */
    private Integer childTaskIndex;

    /**
     * 子任务指派成员用户id
     */
    private String childTaskAssigneeId;

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
