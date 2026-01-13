package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 项目团队与成员关系实体，对应表 `team_user_relation`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("team_user_relation")
public class TeamUserRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目团队与成员关系id（UUID）
     */
    @TableId(value = "team_user_relation_id", type = IdType.ASSIGN_UUID)
    private String teamUserRelationId;

    /**
     * 项目团队id（UUID）
     */
    private String teamId;

    /**
     * 用户id（UUID）
     */
    private String userId;

    /**
     * 用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）
     */
    private Integer userRole;

    /**
     * 用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝
     */
    private Integer userStatus;

    /**
     * 团队索引，用于用户界面排序
     */
    private Integer teamIndex;

    /**
     * 团队颜色，用于用户界面显示
     */
    private String teamColor;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 加入时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
