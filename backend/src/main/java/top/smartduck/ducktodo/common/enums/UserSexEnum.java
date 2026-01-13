package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户性别枚举
 */
@Getter
@AllArgsConstructor
public enum UserSexEnum {

    FEMALE(0, "女"),
    MALE(1, "男");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
