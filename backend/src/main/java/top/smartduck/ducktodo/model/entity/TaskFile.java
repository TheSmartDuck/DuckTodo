package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务附件实体，对应表 `task_file`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_file")
public class TaskFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务附件id（UUID）
     */
    @TableId(value = "task_file_id", type = IdType.ASSIGN_UUID)
    private String taskFileId;

    /**
     * 所属任务id（UUID）
     */
    private String taskId;

    /**
     * 上传者用户id（UUID）
     */
    private String uploaderUserId;

    /**
     * 附件名称
     */
    private String taskFileName;

    /**
     * 附件存储路径（URL或本地路径）
     */
    private String taskFilePath;

    /**
     * 附件类型（MIME类型或分类）
     */
    private String taskFileType;

    /**
     * 附件大小（字节）
     */
    private Long taskFileSize;

    /**
     * 附件状态，0-禁用，1-正常
     */
    private Integer taskFileStatus;

    /**
     * 附件备注
     */
    private String taskFileRemark;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

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
