package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务节点实体，对应表 `task_node`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_node")
public class TaskNode implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务节点id（UUID）
     */
    @TableId(value = "task_node_id", type = IdType.ASSIGN_UUID)
    private String taskNodeId;

    /**
     * 所属团队id（UUID）
     */
    private String teamId;

    /**
     * 所属任务组id（UUID）
     */
    private String taskGroupId;

    /**
     * 所属任务id（UUID）
     */
    private String taskId;

    /**
     * 所属子任务id（UUID）
     */
    private String childTaskId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型
     */
    private String nodeType;

    /**
     * 节点描述
     */
    private String nodeDescription;

    /**
     * 节点数据
     */
    private String extraData;

    /**
     * 节点状态，0-禁用，1-正常
     */
    private Integer nodeStatus;

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
