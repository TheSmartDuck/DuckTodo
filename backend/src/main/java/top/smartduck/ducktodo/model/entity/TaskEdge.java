package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务边实体，对应表 `task_edge`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_edge")
public class TaskEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务边id（UUID）
     */
    @TableId(value = "task_edge_id", type = IdType.ASSIGN_UUID)
    private String taskEdgeId;

    /**
     * 源节点id（UUID）
     */
    private String sourceNodeId;

    /**
     * 目标节点id（UUID）
     */
    private String targetNodeId;

    /**
     * 边类型
     */
    private String edgeType;

    /**
     * 边描述
     */
    private String edgeDescription;

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
