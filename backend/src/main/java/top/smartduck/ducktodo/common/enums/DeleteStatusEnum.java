package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 删除状态枚举
 */
@Getter
@AllArgsConstructor
public enum DeleteStatusEnum {

    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
