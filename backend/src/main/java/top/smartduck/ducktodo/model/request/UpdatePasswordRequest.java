package top.smartduck.ducktodo.model.request;

import lombok.Data;

/**
 * 更新密码请求体 VO。
 * 存放旧密码与新密码字段，用于 UserController.updatePassword。
 *
 * 字段说明：
 * - originalPassword：旧密码/原始密码
 * - newPassword：新密码
 */
@Data
public class UpdatePasswordRequest {
    private String originalPassword;
    private String newPassword;
}