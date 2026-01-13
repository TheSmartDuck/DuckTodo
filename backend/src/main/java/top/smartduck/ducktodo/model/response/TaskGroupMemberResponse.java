package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskGroupMemberResponse {
    private String taskGroupUserRelationId;
    private String taskGroupId;
    private Integer userRole;
    private Integer userStatus;
    private Integer groupIndex;
    private String groupColor;
    private String groupAlias;
    private LocalDateTime joinTime;
    private LocalDateTime updateTime;
    private String userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private Integer userSex;
    private String userAvatar;
    private String userRemark;
    private LocalDateTime lastLoginTime;
}

