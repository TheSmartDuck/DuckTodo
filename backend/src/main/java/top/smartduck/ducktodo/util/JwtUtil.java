package top.smartduck.ducktodo.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import top.smartduck.ducktodo.model.entity.User;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 基于 Hutool JWT 实现，负责 Token 的生成、解析与校验。
 * 封装了用户信息的序列化与反序列化，确保 Token 中仅包含非敏感业务字段（排除密码、盐值、AK/SK 等）。
 * </p>
 *
 * @author DuckTodo Team
 * @version 1.0
 */
@Component
public class JwtUtil {

    /**
     * JWT 签名密钥
     */
    @Value("${jwt.secret:ducktodo-secret}")
    private String secret;

    /**
     * 默认过期时间（秒），默认 1 天 (86400秒)
     */
    @Value("${jwt.expire-seconds:86400}")
    private long expireSeconds;

    /**
     * 生成 Token (使用默认过期时间)
     *
     * @param user 用户实体（需包含 userId, userName 等基础信息）
     * @return 签名的 JWT Token 字符串
     * @throws IllegalArgumentException if user is null
     */
    public String generateToken(User user) {
        return generateToken(user, expireSeconds);
    }

    /**
     * 生成 Token (指定过期时间)
     * <p>
     * Payload 包含字段：
     * <ul>
     *     <li>uid: 用户ID</li>
     *     <li>name: 用户名</li>
     *     <li>email: 邮箱</li>
     *     <li>phone: 手机号</li>
     *     <li>sex: 性别</li>
     *     <li>avatar: 头像URL</li>
     *     <li>iat: 签发时间</li>
     *     <li>exp: 过期时间</li>
     * </ul>
     * </p>
     *
     * @param user       用户实体
     * @param ttlSeconds 过期时间（秒）
     * @return 签名的 JWT Token 字符串
     * @throws IllegalArgumentException if user is null
     */
    public String generateToken(User user, long ttlSeconds) {
        if (user == null) {
            throw new IllegalArgumentException("User is null");
        }
        long nowSeconds = System.currentTimeMillis() / 1000;
        Map<String, Object> claims = new HashMap<>();
        // 业务字段
        claims.put("uid", user.getUserId());
        claims.put("name", user.getUserName());
        claims.put("email", user.getUserEmail());
        claims.put("phone", user.getUserPhone());
        claims.put("sex", user.getUserSex());
        claims.put("avatar", user.getUserAvatar());
        // 标准声明 (Standard Claims)
        claims.put("iat", nowSeconds);
        claims.put("exp", nowSeconds + ttlSeconds);

        return JWTUtil.createToken(claims, getKey());
    }

    /**
     * 解析并验证 Token
     * <p>
     * 会校验签名有效性及是否过期。
     * 解析成功后，返回仅包含 Payload 中非敏感信息的 User 对象。
     * </p>
     *
     * @param token JWT Token 字符串
     * @return User 对象（仅包含 userId, userName, email, phone, sex, avatar）
     * @throws IllegalArgumentException 若 Token 签名无效或已过期
     */
    public User parseToken(String token) {
        JWT jwt = JWTUtil.parseToken(token).setKey(getKey());
        // 1. 校验签名
        if (!jwt.verify()) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }
        // 2. 校验过期时间 (leeway = 0)
        if (!jwt.validate(0)) {
            throw new IllegalArgumentException("JWT is expired or not yet valid");
        }
        // 3. 提取 Payload
        JSONObject claims = jwt.getPayload().getClaimsJson();
        User user = new User();
        user.setUserId(claims.getStr("uid"));
        user.setUserName(claims.getStr("name"));
        user.setUserEmail(claims.getStr("email"));
        user.setUserPhone(claims.getStr("phone"));
        user.setUserSex(claims.getInt("sex"));
        user.setUserAvatar(claims.getStr("avatar"));
        return user;
    }

    /**
     * 简单校验 Token 是否有效
     * <p>
     * 检查项：
     * 1. 格式是否正确
     * 2. 签名是否正确
     * 3. 是否在有效期内
     * </p>
     *
     * @param token JWT Token 字符串
     * @return true 若有效，否则 false
     */
    public boolean isValid(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token).setKey(getKey());
            return jwt.verify() && jwt.validate(0);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取签名密钥字节数组
     *
     * @return 密钥字节数组
     */
    private byte[] getKey() {
        return secret.getBytes(StandardCharsets.UTF_8);
    }
}
