package top.smartduck.ducktodo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.model.entity.UserLlmConfig;
import top.smartduck.ducktodo.service.LlmService;
import top.smartduck.ducktodo.modelService.UserLlmConfigService;

@Slf4j
@Service
public class LlmServiceImpl implements LlmService {

    @Autowired
    private UserLlmConfigService userLlmConfigService;

    @Override
    public ChatModel getChatModel(String userLlmConfigId) {
        UserLlmConfig config = userLlmConfigService.getById(userLlmConfigId);
        if (config == null) {
            throw new RuntimeException("LLM 配置不存在或已删除");
        }

        String apiKey = config.getLlmApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("该配置未设置 LLM API Key");
        }

        String baseUrl = config.getLlmApiUrl();
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = "https://api.openai.com"; // 默认地址
        }

        // 构建 OpenAiApi
        OpenAiApi openAiApi = new OpenAiApi(baseUrl, apiKey);

        // 构建 Options
        String model = config.getLlmModelName();
        if (model == null || model.trim().isEmpty()) {
            model = "gpt-3.5-turbo";
        }

        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(model)
                .withTemperature(config.getLlmModelTemperature() != null ? config.getLlmModelTemperature().floatValue() : 0.7f)
                .build();

        return new OpenAiChatModel(openAiApi, options);
    }
}
