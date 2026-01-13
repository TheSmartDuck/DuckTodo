package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务节点状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskNodeStatusEnum {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
