package top.smartduck.ducktodo.model.request;

import lombok.Data;

@Data
public class LoginRequest {
    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户邮箱 (与用户名二选一)
     */
    private String userEmail;

    /**
     * 用户密码
     */
    private String userPassword;
}
