package top.smartduck.ducktodo.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建团队请求体 VO。
 * 包含团队基础信息与被邀请成员列表（ID 与角色）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {
    /** 团队名称（至少2位） */
    private String teamName;

    /** 团队描述（可选） */
    private String teamDescription;

    /** 团队头像（URL或对象存储路径，可选） */
    private String teamAvatar;

    /** 团队状态（0禁用、1进行中、2已结束），默认1 */
    private Integer teamStatus;

    /**
     * 被邀请成员列表（可选）。
     * 每个元素同时包含被邀请用户ID与其在团队中的角色。
     * 角色建议为：manager / member；owner 仅限创建者。
     */
    private List<InvitedMember> invitedMemberList;

    /**
     * 被邀请成员条目。
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvitedMember {
        /** 被邀请用户ID */
        private String userId;
        /** 被邀请用户角色（manager/member） */
        private Integer memberRole;
    }
}
