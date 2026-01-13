package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目团队实体，对应表 `team`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("team")
public class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队id（UUID）
     */
    @TableId(value = "team_id", type = IdType.ASSIGN_UUID)
    private String teamId;

    /**
     * 团队名称，要求2位以上，不可重复
     */
    private String teamName;

    /**
     * 团队描述
     */
    private String teamDescription;

    /**
     * 团队头像，存储路径
     */
    private String teamAvatar;

    /**
     * 团队状态，0-已禁用，1-进行中，2-已结束
     */
    private Integer teamStatus;

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
