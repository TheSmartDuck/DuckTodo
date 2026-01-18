package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户工具配置实体类，对应表 `user_tool_config`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_tool_config")
public class UserToolConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户工具配置id（UUID）
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户id（UUID）
     */
    private String userId;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具配置JSON数据
     */
    private String configJson;

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
