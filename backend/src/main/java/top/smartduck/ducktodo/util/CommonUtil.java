package top.smartduck.ducktodo.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.model.dto.LoginUserDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.lang.reflect.Method;

public final class CommonUtil {

    /**
     * 获取当前登录用户 (从 SecurityContextHolder 中获取)。
     * 注意：此方法依赖于 Spring Security 上下文，确保请求已通过认证过滤器。
     *
     * @param request HttpServletRequest 对象（虽然参数中包含了 request，但实际是从 SecurityContextHolder 获取，保留参数是为了兼容旧代码接口习惯）
     * @return 当前登录用户 User 对象；如果未登录或获取失败，返回 null
     */
    public static User getCurrentUser(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof LoginUserDto) {
                LoginUserDto loginUser = (LoginUserDto) authentication.getPrincipal();
                return loginUser.getUser();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 基于输入文本与当前时间生成 UUID（v3，name-based）。
     * 通过将文本与当前毫秒时间拼接为种子，生成低碰撞概率的 UUID。
     *
     * @param text 输入文本（可为 null）
     * @return UUID 字符串（带连字符）
     */
    public static String generateUuid(String text) {
        String seed = (text == null ? "" : text) + ":" + Instant.now().toEpochMilli();
        UUID uuid = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

    /**
     * 生成随机盐（默认长度 8），仅包含小写字母与数字。
     * @return 随机盐字符串
     */
    public static String generateSalt() {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        return RandomUtil.randomString(base, 8);
    }

    /**
     * 生成文本的 MD5 十六进制摘要（不含盐）。
     * 注意：MD5 不适合密码存储，建议使用 bcrypt/argon2。
     * @param text 文本
     * @return MD5 十六进制摘要
     */
    public static String md5(String text) {
        return DigestUtil.md5Hex(text == null ? "" : text);
    }

    /**
     * 去除字符串首尾空白字符。
     * 备注：该方法从 UserController 中的私有工具方法解耦提取，
     * 为统一在各处使用的通用工具。行为保持一致：
     * - 入参为 null 时返回 null；
     * - 入参非 null 时返回 {@code s.trim()}（不将空字符串转为 null）。
     *
     * @param s 输入字符串
     * @return 去除首尾空白后的字符串；若入参为 null 则返回 null
     */
    public static String trim(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * 解析日期时间字符串为 LocalDateTime。
     * 支持 ISO-8601 格式（如：yyyy-MM-ddTHH:mm:ss），以及将空格替换为 'T' 的情况；
     * 若包含时区（如以 'Z' 或偏移量结尾），将按 OffsetDateTime 解析并转换为本地时间。
     * 非法或为空返回 null。
     *
     * @param s 日期时间字符串
     * @return 解析得到的 LocalDateTime，若无法解析则返回 null
     */
    public static LocalDateTime parseDateTime(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e1) {
            String v2 = v.replace(' ', 'T');
            if (!v.equals(v2)) {
                try {
                    return LocalDateTime.parse(v2);
                } catch (DateTimeParseException ignored) {}
            }
            try {
                return OffsetDateTime.parse(v).toLocalDateTime();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    /**
     * 判断给定值是否为指定枚举中的值。
     *
     * @param values 枚举值数组
     * @param code 待判断的值
     * @return 是否为指定枚举中的值
     */
    public static <E extends Enum<E>> boolean inEnumCodes(E[] values, Integer code) {
        if (code == null || values == null || values.length == 0) return false;
        try {
            Method m = values[0].getClass().getMethod("getCode");
            for (E e : values) {
                Object c = m.invoke(e);
                if (code.equals(c)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
