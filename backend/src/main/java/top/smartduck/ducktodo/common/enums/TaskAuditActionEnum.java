package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务审计操作类型枚举
 */
@Getter
@AllArgsConstructor
public enum TaskAuditActionEnum {

    CREATE("CREATE", "创建"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除"),
    COMPLETE("COMPLETE", "完成"),
    CANCEL("CANCEL", "取消"),
    ARCHIVE("ARCHIVE", "归档"),
    RESTORE("RESTORE", "恢复");

    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;
}
