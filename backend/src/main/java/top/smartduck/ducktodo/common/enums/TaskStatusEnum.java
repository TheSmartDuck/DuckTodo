package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {

    DISABLED(0, "已禁用"),
    NOT_STARTED(1, "未开始"),
    IN_PROGRESS(2, "进行中"),
    COMPLETED(3, "已完成"),
    CANCELED(4, "已取消");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
