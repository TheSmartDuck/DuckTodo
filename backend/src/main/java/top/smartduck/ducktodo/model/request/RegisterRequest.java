package top.smartduck.ducktodo.model.request;

import lombok.Data;

@Data
public class RegisterRequest {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 性别 (0-女, 1-男, 2-保密)
     */
    private Integer userSex;
}
