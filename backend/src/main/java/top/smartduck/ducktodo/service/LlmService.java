package top.smartduck.ducktodo.service;

import org.springframework.ai.chat.model.ChatModel;

public interface LlmService {
    /**
     * 获取指定 LLM 配置 ID 的客户端
     * @param userLlmConfigId LLM 配置 ID
     * @return ChatModel 实例
     */
    ChatModel getChatModel(String userLlmConfigId);
}
