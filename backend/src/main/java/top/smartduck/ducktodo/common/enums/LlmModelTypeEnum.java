package top.smartduck.ducktodo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * LLM 模型类型枚举
 */
@Getter
@AllArgsConstructor
public enum LlmModelTypeEnum {

    CHAT(1, "chat模型"),
    EMBEDDING(2, "embedding模型"),
    RERANK(3, "rerank模型");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
