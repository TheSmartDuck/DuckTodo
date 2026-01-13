package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务审计实体，对应表 `task_audit`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_audit")
public class TaskAudit implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 审计日志id（UUID）
     */
    @TableId(value = "audit_id", type = IdType.ASSIGN_UUID)
    private String auditId;

    /**
     * 关联的主任务id（UUID）
     */
    private String taskId;

    /**
     * 操作人id（UUID）
     */
    private String operatorId;

    /**
     * 操作类型（CREATE, UPDATE, DELETE, COMPLETE...）
     */
    private String actionType;

    /**
     * 操作详情
     */
    private String actionDescription;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
