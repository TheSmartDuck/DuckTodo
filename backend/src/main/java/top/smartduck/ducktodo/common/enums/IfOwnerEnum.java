package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IfOwnerEnum {
    IS_NOT_OWNER(0, "否"),
    IS_OWNER(1, "是");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
