package top.smartduck.ducktodo.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户LLM配置实体类，对应表 `user_llm_config`。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_llm_config")
public class UserLlmConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户LLM配置id（UUID）
     */
    @TableId(value = "user_llm_config_id", type = IdType.ASSIGN_UUID)
    private String userLlmConfigId;

    /**
     * 用户id（UUID）
     */
    private String userId;

    /**
     * LLM提供商（如：openai, modelscope, ollama等）
     */
    private String llmProvider;

    /**
     * LLM API Key
     */
    private String llmApiKey;

    /**
     * LLM API URL（用于自建或兼容接口）
     */
    private String llmApiUrl;

    /**
     * LLM 模型名称
     */
    private String llmModelName;

    /**
     * LLM 模型温度（0.0-1.0）
     */
    private BigDecimal llmModelTemperature;

    /**
     * 是否支持Thinking，0-不支持，1-支持
     */
    private Integer llmModelThinking;

    /**
     * LLM 模型类型，1-chat模型，2-embedding模型，3-rerank模型
     */
    private Integer llmModelType;

    /**
     * 是否删除 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
