package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常"),
    INVITING(2, "邀请中"),
    REJECTED(3, "已拒绝");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
