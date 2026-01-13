package top.smartduck.ducktodo.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.smartduck.ducktodo.common.enums.ResultCode;

/**
 * 通用返回类 R（支持泛型数据载荷）。
 *
 * 统一后端返回结构，约定字段：
 * - success：是否成功
 * - code：业务/HTTP 状态码（默认 200/400/500 等）
 * - message：提示信息
 * - data：业务数据（泛型）
 * - timestamp：服务器时间戳（毫秒）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    /** 是否成功 */
    private boolean success;
    /** 业务/HTTP 状态码 */
    private int code;
    /** 提示信息 */
    private String message;
    /** 业务数据 */
    private T data;
    /** 服务器时间戳（毫秒） */
    private long timestamp;

    // ===== 工厂方法（成功） =====
    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> success(T data) {
        return R.<T>builder()
                .success(true)
                .code(ResultCode.OK.getCode())
                .message("OK")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> R<T> successMsg(String message) {
        return R.<T>builder()
                .success(true)
                .code(ResultCode.OK.getCode())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    public static <T> R<T> success(T data, String message) {
        return R.<T>builder()
                .success(true)
                .code(ResultCode.OK.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    // ===== 工厂方法（失败/异常） =====
    public static <T> R<T> fail(String message) {
        return fail(ResultCode.BAD_REQUEST.getCode(), message);
    }

    public static <T> R<T> fail(int code, String message) {
        return R.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败（枚举版）：使用统一的 ResultCode 管理码值。
     */
    public static <T> R<T> fail(ResultCode resultCode, String message) {
        return fail(resultCode.getCode(), message);
    }

    public static <T> R<T> error(String message) {
        return fail(ResultCode.INTERNAL_ERROR, message);
    }

    public static <T> R<T> unauthorized(String message) {
        return fail(ResultCode.UNAUTHORIZED, message);
    }

    /**
     * 权限不足（已废弃，禁止使用 403）
     * 权限相关错误统一使用 fail() 返回 BAD_REQUEST(400)
     * @deprecated 使用 R.fail(message) 替代
     */
    @Deprecated
    public static <T> R<T> forbidden(String message) {
        // 改为返回 BAD_REQUEST(400) 而不是 FORBIDDEN(403)
        return fail(ResultCode.BAD_REQUEST, message);
    }

    public static <T> R<T> notFound(String message) {
        return fail(ResultCode.NOT_FOUND, message);
    }

    // ===== 链式补充 =====
    public R<T> withData(T data) {
        this.data = data;
        return this;
    }

    public R<T> withMessage(String message) {
        this.message = message;
        return this;
    }

    public R<T> withCode(int code) {
        this.code = code;
        return this;
    }
}