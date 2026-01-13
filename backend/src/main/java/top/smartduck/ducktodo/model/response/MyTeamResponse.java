package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 融合 Team 与当前用户在该团队的 TeamUserRelationship 的输出模型。
 * 将常用字段扁平化，方便前端直接渲染。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyTeamResponse {
    // 团队信息
    private String teamId;
    private String teamName;
    private String teamAvatar;
    private String teamDescription;
    private Integer teamStatus;

    // 当前用户与该团队的关系信息
    private Integer memberRole;
    private Integer memberStatus;
    private Integer teamIndex;
    private String teamColor;
    private LocalDateTime joinTime;
    private LocalDateTime updateTime;
}
