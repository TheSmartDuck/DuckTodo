package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户安全信息实体类，对应表 `user_security`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_security")
public class UserSecurity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id（UUID）
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    /**
     * 密码（Argon2(原始密码+密码加盐)，原始密码需8位以上且包含英文）
     */
    private String userPassword;

    /**
     * 密码加盐（四位数字+英文小写）
     */
    private String userPasswordSalt;

    /**
     * 用户AK
     */
    private String userAccesskey;

    /**
     * 用户SK
     */
    private String userSecretkey;

    /**
     * 单点登录来源（如：google, github, wechat等）
     */
    private String ssoSource;

    /**
     * 单点登录唯一标识
     */
    private String ssoUid;

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
