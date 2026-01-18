package top.smartduck.ducktodo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Gateway 配置类
 * 配置 WebClient 用于反向代理到 AI 后端
 */
@Slf4j
@Configuration
public class GatewayConfig {

    @Value("${ai-backend.url:http://localhost:8000}")
    private String aiBackendUrl;

    /**
     * 配置 WebClient Bean，用于向 AI 后端发送请求
     *
     * @return WebClient 实例
     */
    @Bean
    public WebClient aiBackendWebClient() {
        log.info("Initializing AI Backend WebClient with URL: {}", aiBackendUrl);
        return WebClient.builder()
                .baseUrl(aiBackendUrl)
                .build();
    }
}
