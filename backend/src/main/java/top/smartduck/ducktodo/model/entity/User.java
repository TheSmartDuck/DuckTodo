package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类，对应表 `user`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id（UUID）
     */
    @TableId(value = "user_id", type = IdType.ASSIGN_UUID)
    private String userId;

    /**
     * 用户名，要求2位以上，不可重复
     */
    private String userName;

    /**
     * 用户邮箱，要求邮箱格式，不可重复
     */
    private String userEmail;

    /**
     * 用户手机，要求11位，不可重复
     */
    private String userPhone;

    /**
     * 用户性别，0-女，1-男
     */
    private Integer userSex;

    /**
     * 用户头像，存储路径
     */
    private String userAvatar;

    /**
     * 用户备注
     */
    private String userRemark;

    /**
     * 上次登录时间
     */
    private LocalDateTime lastLoginTime;

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
