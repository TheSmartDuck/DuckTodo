package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    OWNER(0, "创建者"),
    MANAGER(1, "管理者"),
    MEMBER(2, "普通成员");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
