package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户钉钉机器人配置实体类，对应表 `user_dingtalk_robot`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_dingtalk_robot")
public class UserDingtalkRobot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户钉钉机器人配置id（UUID）
     */
    @TableId(value = "user_dingtalk_robot_id", type = IdType.ASSIGN_UUID)
    private String userDingtalkRobotId;

    /**
     * 用户id（UUID）
     */
    private String userId;

    /**
     * 机器人名称
     */
    private String robotName;

    /**
     * 钉钉机器人Token
     */
    private String dingtalkRobotToken;

    /**
     * 钉钉机器人Secret
     */
    private String dingtalkRobotSecret;

    /**
     * 钉钉机器人关键字
     */
    private String dingtalkRobotKeyword;

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
