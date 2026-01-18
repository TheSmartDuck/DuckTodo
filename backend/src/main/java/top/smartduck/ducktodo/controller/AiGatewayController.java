package top.smartduck.ducktodo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;

/**
 * AI 后端网关控制器
 * 将 /api/ai/** 的请求反向代理到 AI 后端服务
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiGatewayController {

    @Autowired
    private WebClient aiBackendWebClient;

    /**
     * 代理所有请求到 AI 后端
     *
     * @param request HTTP 请求对象
     * @return 响应实体
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public Mono<ResponseEntity<Object>> proxyRequest(HttpServletRequest request) {
        try {
            // 保留完整路径，包括 /api/ai 前缀
            String requestPath = request.getRequestURI();
            
            // 如果路径不是以 /api/ai 开头，则添加前缀
            String path = requestPath.startsWith("/api/ai") 
                    ? requestPath 
                    : "/api/ai" + requestPath;
            
            if (path.isEmpty()) {
                path = "/api/ai/";
            }

            // 获取查询字符串
            String queryString = request.getQueryString();
            String fullPath = queryString != null ? path + "?" + queryString : path;

            log.debug("Proxying {} request to AI backend: {}", request.getMethod(), fullPath);

            // 获取请求体
            String body = null;
            if (request.getContentLength() > 0 && 
                (request.getMethod().equals("POST") || 
                 request.getMethod().equals("PUT") || 
                 request.getMethod().equals("PATCH"))) {
                try {
                    body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.error("Error reading request body", e);
                }
            }

            // 构建 WebClient 请求
            WebClient.RequestBodySpec requestSpec = aiBackendWebClient
                    .method(HttpMethod.valueOf(request.getMethod()))
                    .uri(fullPath);

            // 复制请求头（排除 Host 和 Content-Length）
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!headerName.equalsIgnoreCase("host") && 
                    !headerName.equalsIgnoreCase("content-length") &&
                    !headerName.equalsIgnoreCase("connection")) {
                    Enumeration<String> headerValues = request.getHeaders(headerName);
                    while (headerValues.hasMoreElements()) {
                        headers.add(headerName, headerValues.nextElement());
                    }
                }
            }

            requestSpec.headers(httpHeaders -> httpHeaders.addAll(headers));

            // 设置请求体
            if (body != null && !body.isEmpty()) {
                requestSpec.contentType(MediaType.APPLICATION_JSON);
                requestSpec.bodyValue(body);
            }

            // 发送请求并处理响应
            return requestSpec
                    .retrieve()
                    .toEntity(Object.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("Error proxying request to AI backend: {} - {}", ex.getStatusCode(), ex.getMessage());
                        return Mono.just(ResponseEntity
                                .status(ex.getStatusCode())
                                .headers(ex.getHeaders())
                                .body(ex.getResponseBodyAsString()));
                    })
                    .onErrorResume(Exception.class, ex -> {
                        log.error("Unexpected error proxying request to AI backend", ex);
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", "Internal server error", "message", ex.getMessage())));
                    });

        } catch (Exception e) {
            log.error("Error processing proxy request", e);
            return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "message", e.getMessage())));
        }
    }
}
