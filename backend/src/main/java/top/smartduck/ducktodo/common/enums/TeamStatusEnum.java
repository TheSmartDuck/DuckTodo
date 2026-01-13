package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 团队状态枚举
 */
@Getter
@AllArgsConstructor
public enum TeamStatusEnum {

    DISABLED(0, "已禁用"),
    IN_PROGRESS(1, "进行中"),
    FINISHED(2, "已结束");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
