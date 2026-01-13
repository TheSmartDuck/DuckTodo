package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务优先级枚举
 */
@Getter
@AllArgsConstructor
public enum TaskPriorityEnum {

    P0(0, "P0|紧急优先级"),
    P1(1, "P1|高优先级"),
    P2(2, "P2|中优先级"),
    P3(3, "P3|低优先级"),
    P4(4, "P4|最低优先级");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
