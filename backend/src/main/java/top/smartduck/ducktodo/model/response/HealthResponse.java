package top.smartduck.ducktodo.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * 心跳检测响应实体
 */
@Data
@Builder
public class HealthResponse {
    /**
     * 服务状态 (如 "UP")
     */
    private String status;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 服务器当前时间 (格式化字符串)
     */
    private String serverTime;

    /**
     * 服务器当前时间戳
     */
    private Long timestamp;

    /**
     * 对象存储基础路径 (Bucket URL)
     */
    private String storagePath;
}
