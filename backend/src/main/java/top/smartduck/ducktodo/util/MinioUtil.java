package top.smartduck.ducktodo.util;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类
 * 提供对象存储的基本操作：上传、下载、删除、获取链接等。
 */
@Component
public class MinioUtil {

    private static final Logger log = LoggerFactory.getLogger(MinioUtil.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.ensure-bucket:true}")
    private boolean ensureBucketEnabled;

    private MinioClient client;


    @PostConstruct
    public void init() {
        try {
            this.client = MinioClient.builder()
                    .endpoint(normalizeEndpoint(endpoint))
                    .credentials(accessKey, secretKey)
                    .build();

            if (ensureBucketEnabled) {
                ensureBucket();
            } else {
                log.info("MinIO bucket ensure disabled by config; skipping ensure for bucket '{}'.", bucket);
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO client init failed: " + e.getMessage(), e);
        }
    }

    /**
     * 确保 Bucket 存在，不存在则创建
     */
    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            } else {
                log.info("MinIO bucket exists: {}", bucket);
            }
        } catch (Exception e) {
            // 网络错误等不应阻塞应用启动，记录警告
            log.warn("MinIO ensure bucket failed: {}. Continuing without ensure.", e.getMessage());
        }
    }

    /**
     * 上传文件（简单模式，适用于未知大小流）
     *
     * @param objectName 对象名（可含路径）
     * @param stream     输入流
     * @param contentType MIME 类型
     * @return 对象访问 URL（直链）
     */
    public String upload(String objectName, InputStream stream, String contentType) {
        try {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(stream, -1, 10 * 1024 * 1024) // 分片大小 10MB
                            .contentType(contentType)
                            .build()
            );
            return getObjectUrl(objectName);
        } catch (Exception e) {
            throw new RuntimeException("MinIO Upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件（已知大小模式，优化网络传输）
     * <p>
     * 针对小文件（<=10MB）会尝试内存缓冲，遇到网络重置错误时可自动降级为文件上传模式。
     * </p>
     *
     * @param objectName 对象名
     * @param stream     输入流
     * @param contentType MIME 类型
     * @param size       文件大小（字节）
     * @return 对象访问 URL（直链）
     */
    public String uploadKnownSize(String objectName, InputStream stream, String contentType, long size) {
        try {
            // 内存缓冲阈值：10MB
            final long maxBuffer = 10L * 1024 * 1024;
            byte[] buffer = null;

            // 若文件较小，先读入内存，以便在网络异常时重试
            if (size >= 0 && size <= maxBuffer) {
                buffer = stream.readAllBytes(); // JDK 9+ 方法
            }

            // MinIO 要求分片至少 5MB，这里设为 10MB
            long partSize = 10 * 1024 * 1024;

            try {
                InputStream in = (buffer != null) ? new ByteArrayInputStream(buffer) : stream;
                client.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(objectName)
                                .stream(in, size, partSize)
                                .contentType(contentType)
                                .build()
                );
                return getObjectUrl(objectName);
            } catch (Exception e) {
                // 针对“连接重置”等网络错误，且有缓冲区的情况下，尝试降级到临时文件上传
                if (buffer != null && isNetworkResetError(e)) {
                    log.warn("MinIO stream upload failed ({}), falling back to file upload for object: {}", e.getMessage(), objectName);
                    return uploadViaTempFile(objectName, buffer, contentType);
                }
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO Upload (KnownSize) failed: " + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名
     * @return 输入流（调用方需关闭）
     */
    public InputStream download(String objectName) {
        try {
            return client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO Download failed: " + e.getMessage(), e);
        }
    }

    /**
     * 删除对象
     *
     * @param objectName 对象名
     */
    public void remove(String objectName) {
        try {
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO Remove failed: " + e.getMessage(), e);
        }
    }

    /**
     * 获取对象直链 URL（不含签名，适合公开读的 Bucket）
     *
     * @param objectName 对象名
     * @return 完整 URL
     */
    public String getObjectUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) return null;
        // 去除 endpoint 末尾斜杠
        String base = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        // 确保 objectName 不以斜杠开头（避免双斜杠）
        String cleanName = objectName.startsWith("/") ? objectName.substring(1) : objectName;
        return base + "/" + bucket + "/" + cleanName;
    }

    /**
     * 获取带签名的临时访问 URL（适合私有 Bucket）
     *
     * @param objectName    对象名
     * @param expirySeconds 过期时间（秒），最长 7 天
     * @return 带签名的完整 URL
     */
    public String getPresignedObjectUrl(String objectName, int expirySeconds) {
        try {
            return client.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(expirySeconds, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("MinIO Presign URL failed: " + e.getMessage(), e);
        }
    }

    /**
     * 检查对象是否存在
     *
     * @param objectName 对象名
     * @return true 存在, false 不存在
     */
    public boolean statObject(String objectName) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build());
            return true;
        } catch (ErrorResponseException e) {
            // 404 Not Found
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            throw new RuntimeException("MinIO StatObject failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("MinIO StatObject failed: " + e.getMessage(), e);
        }
    }

    // ================= 内部工具方法 =================

    /**
     * 规范化 Endpoint 地址，确保包含协议头
     */
    private String normalizeEndpoint(String ep) {
        if (ep == null || ep.trim().isEmpty()) {
            throw new IllegalArgumentException("MinIO endpoint is missing");
        }
        String e = ep.trim();
        if (!(e.startsWith("http://") || e.startsWith("https://"))) {
            e = "http://" + e;
        }
        return e;
    }

    /**
     * 判断是否为网络重置类错误
     */
    private boolean isNetworkResetError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        msg = msg.toLowerCase();
        return msg.contains("connection reset")
                || msg.contains("socket write error")
                || msg.contains("broken pipe")
                || msg.contains("econnreset");
    }

    /**
     * 降级方案：写入临时文件后上传
     */
    private String uploadViaTempFile(String objectName, byte[] data, String contentType) throws Exception {
        File tmp = File.createTempFile("ducktodo-minio-", ".upload");
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(data);
            fos.flush();
        }
        try {
            client.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .filename(tmp.getAbsolutePath())
                            .contentType(contentType)
                            .build()
            );
            return getObjectUrl(objectName);
        } finally {
            // 尝试删除临时文件，失败则标记退出时删除
            if (!tmp.delete()) {
                tmp.deleteOnExit();
            }
        }
    }
}
