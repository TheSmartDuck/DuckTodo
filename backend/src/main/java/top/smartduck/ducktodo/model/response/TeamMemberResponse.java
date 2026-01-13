package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 团队成员分页项：融合 TeamUserRelationship 与 User 的常用字段。
 * 仅包含非敏感信息，便于前端直接渲染成员与其在团队中的关系。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    // 成员关系信息（来自 team_user_relationship）
    private String teamUserRelationshipId;
    private String teamId;
    private Integer memberRole;      // 用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）
    private Integer memberStatus;    // 用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝
    private Integer teamIndex;      // 用于界面排序
    private String teamColor;       // 成员在该团队的配色
    private LocalDateTime joinTime; // 加入时间
    private LocalDateTime updateTime; // 关系更新时间

    // 用户信息（来自 user），不包含密码/盐/AK/SK 等敏感字段
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private Integer userSex;  //用户性别，0-女，1-男
    private String userAvatar;
    private String userRemark;
    private LocalDateTime lastLoginTime;
}