package top.smartduck.ducktodo.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.smartduck.ducktodo.model.entity.User;
import java.time.LocalDateTime;

/**
 * 登录/注册响应 VO：用于向前端返回结构化的登录结果。
 * 包含后端签发的 token 与用户信息（扁平化结构）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /** JWT 令牌 */
    private String token;

    /** 用户id */
    private String userId;

    /** 用户名 */
    private String userName;

    /** 用户邮箱 */
    private String userEmail;

    /** 用户手机 */
    private String userPhone;

    /** 用户性别，0-女，1-男 */
    private Integer userSex;

    /** 用户头像 */
    private String userAvatar;

    /** 用户备注 */
    private String userRemark;

    /** 上次登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 静态构建方法：根据 User 和 token 构建响应对象
     */
    public static LoginResponse fromUser(User user, String token) {
        if (user == null) return null;
        return LoginResponse.builder()
                .token(token)
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userPhone(user.getUserPhone())
                .userSex(user.getUserSex())
                .userAvatar(user.getUserAvatar())
                .userRemark(user.getUserRemark())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }
}