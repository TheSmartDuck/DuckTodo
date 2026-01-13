package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.smartduck.ducktodo.common.constant.SystemConstant;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.model.entity.UserSecurity;
import top.smartduck.ducktodo.modelService.UserService;
import top.smartduck.ducktodo.modelService.UserSecurityService;
import top.smartduck.ducktodo.util.CommonUtil;
import top.smartduck.ducktodo.model.request.UpdatePasswordRequest;
import top.smartduck.ducktodo.model.response.AccessKeyResponse;
import top.smartduck.ducktodo.model.request.AccessKeyRequest;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import top.smartduck.ducktodo.util.MinioUtil;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 用户相关接口（需 JWT 鉴权）。
 *
 * <p>接口列表：</p>
 * <ol>
 *     <li><b>GET /api/user/me</b>：获取当前登录用户信息。</li>
 *     <li><b>GET /api/user/{userId}</b>：根据用户ID查询指定用户信息。</li>
 *     <li><b>PUT /api/user/me</b>：修改当前用户个人信息（邮箱、手机号、性别、备注）。</li>
 *     <li><b>PUT /api/user/me/password</b>：修改当前用户密码（需验证旧密码）。</li>
 *     <li><b>GET /api/user/me/access-keys</b>：获取当前用户的 API 访问密钥（AK/SK）。</li>
 *     <li><b>PUT /api/user/me/access-keys</b>：更新或设置当前用户的 API 访问密钥。</li>
 *     <li><b>DELETE /api/user/me/access-keys</b>：删除当前用户的 API 访问密钥。</li>
 *     <li><b>POST /api/user/me/avatar</b>：上传并更新当前用户头像（自动清理旧头像）。</li>
 *     <li><b>GET /api/user</b>：分页查询用户列表（支持用户名、邮箱、手机号模糊搜索）。</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;



    /**
     * 获取当前登录用户信息。
     * <p>作用：从认证上下文识别当前用户并返回其最新资料。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>通过认证上下文获取当前用户。</li>
     *     <li>校验 userId 合法性。</li>
     *     <li>查询数据库并返回用户信息。</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @return 当前用户的详细信息
     */
    @GetMapping("/me")
    public R<User> getMe(HttpServletRequest request) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String userId = currentUser.getUserId();
        if (userId == null || userId.trim().isEmpty()) {
            return R.fail("用户ID无效");
        }
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return R.notFound("用户不存在");
        }
        return R.success(dbUser);
    }

    /**
     * 根据用户ID获取用户信息。
     * <p>作用：查询指定用户的基础资料，常用于成员资料查看。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>校验 userId 非空。</li>
     *     <li>按主键查询用户。</li>
     *     <li>返回查询结果。</li>
     * </ol>
     *
     * @param userId 目标用户ID
     * @return 指定用户的详细信息
     */
    @GetMapping("/{userId}")
    public R<User> getUserById(@PathVariable("userId") String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return R.fail("缺少用户ID");
        }
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return R.notFound("用户不存在");
        }
        return R.success(dbUser);
    }

    /**
     * 获取当前用户的 API 密钥（AK/SK）。
     * <p>作用：获取用于外部签名访问的凭证。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>识别当前用户。</li>
     *     <li>查询用户安全信息。</li>
     *     <li>封装 AK/SK 返回。</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @return 包含 AK/SK 的响应对象
     */
    @GetMapping("/me/access-keys")
    public R<AccessKeyResponse> getAccessKeys(HttpServletRequest request) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String userId = currentUser.getUserId();
        UserSecurity security = userSecurityService.getById(userId);
        if (security == null) {
            return R.fail("用户安全信息不存在");
        }

        AccessKeyResponse resp = AccessKeyResponse.builder()
                .userAccesskey(security.getUserAccesskey())
                .userSecretkey(security.getUserSecretkey())
                .build();
        return R.success(resp);
    }

    /**
     * 更新或设置当前用户的 API 密钥。
     * <p>作用：变更当前用户的 AK/SK 凭证。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>识别当前用户并获取安全信息。</li>
     *     <li>提取新 AK/SK，进行非空校验。</li>
     *     <li>保存更新后的安全信息。</li>
     *     <li>返回最新 AK/SK。</li>
     * </ol>
     *
     * @param request          HTTP 请求对象
     * @param accessKeyRequest 新 AK/SK 请求体
     * @return 更新后的密钥信息
     */
    @PutMapping("/me/access-keys")
    public R<AccessKeyResponse> updateAccessKeys(HttpServletRequest request, @RequestBody AccessKeyRequest accessKeyRequest) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String userId = currentUser.getUserId();
        UserSecurity security = userSecurityService.getById(userId);
        if (security == null) {
            return R.fail("用户安全信息不存在");
        }

        String newAk = CommonUtil.trim(accessKeyRequest.getUserAccesskey());
        String newSk = CommonUtil.trim(accessKeyRequest.getUserSecretkey());

        if (newAk != null && newAk.isEmpty()) {
            return R.fail("AccessKey 不能为空");
        }
        if (newSk != null && newSk.isEmpty()) {
            return R.fail("SecretKey 不能为空");
        }

        if (newAk != null) security.setUserAccesskey(newAk);
        if (newSk != null) security.setUserSecretkey(newSk);

        security.setUpdateTime(LocalDateTime.now());
        boolean ok = userSecurityService.updateById(security);
        if (!ok) {
            log.error("用户更新密钥失败: userId={}", userId);
            return R.error("更新密钥失败");
        }

        log.info("用户更新密钥成功: userId={}", userId);
        AccessKeyResponse resp = AccessKeyResponse.builder()
                .userAccesskey(security.getUserAccesskey())
                .userSecretkey(security.getUserSecretkey())
                .build();
        return R.success(resp, "密钥更新成功");
    }

    /**
     * 删除当前用户的 API 密钥。
     * <p>作用：清空 AK/SK，禁止继续通过密钥访问。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>识别当前用户并获取安全信息。</li>
     *     <li>将 AK/SK 置为空（null）。</li>
     *     <li>保存并返回删除结果。</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @return 删除后的响应对象（AK/SK 为空）
     */
    @DeleteMapping("/me/access-keys")
    public R<AccessKeyResponse> deleteAccessKeys(HttpServletRequest request) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String userId = currentUser.getUserId();
        UserSecurity security = userSecurityService.getById(userId);
        if (security == null) {
            return R.fail("用户安全信息不存在");
        }

        security.setUserAccesskey("");
        security.setUserSecretkey("");
        security.setUpdateTime(LocalDateTime.now());
        boolean ok = userSecurityService.updateById(security);
        if (!ok) {
            log.error("用户删除密钥失败: userId={}", userId);
            return R.error("删除密钥失败");
        }
        log.info("用户删除密钥成功: userId={}", userId);
        AccessKeyResponse resp = AccessKeyResponse.builder()
                .userAccesskey(null)
                .userSecretkey(null)
                .build();
        return R.success(resp, "密钥已删除");
    }


    /**
     * 修改当前用户的个人信息。
     * <p>作用：更新邮箱、手机号、性别、备注等基础资料。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>识别当前用户并获取数据库记录。</li>
     *     <li>校验输入格式（邮箱、手机号、性别）。</li>
     *     <li>进行唯一性校验（邮箱、手机号）。</li>
     *     <li>应用变更并持久化。</li>
     *     <li>返回更新后的用户信息。</li>
     * </ol>
     * <p>注意：用户名不可修改；头像需通过专用接口修改。</p>
     *
     * @param request HTTP 请求对象
     * @param user    待更新字段的用户对象
     * @return 更新后的用户信息
     */
    @PutMapping("/me")
    public R<User> updateMe(HttpServletRequest request, @RequestBody User user) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String userId = currentUser.getUserId();
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return R.notFound("用户不存在");
        }

        // 禁止修改用户名
        // 忽略 user.getUserName()

        String newEmail = CommonUtil.trim(user.getUserEmail());
        String newPhone = CommonUtil.trim(user.getUserPhone());
        Integer newSex = user.getUserSex();
        // 忽略 user.getUserAvatar()，头像通过 /me/avatar 接口修改
        String newRemark = CommonUtil.trim(user.getUserRemark());

        // 校验
        if (newEmail != null) {
            Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            if (!emailPattern.matcher(newEmail).matches()) {
                return R.fail("邮箱格式不正确");
            }
        }
        if (newPhone != null && !newPhone.isEmpty()) {
            Pattern phonePattern = Pattern.compile("^\\d{11}$");
            if (!phonePattern.matcher(newPhone).matches()) {
                return R.fail("手机号格式不正确");
            }
        }
        if (newSex != null) {
            // 假设性别枚举：0-女, 1-男, 2-保密 (需与 Register 逻辑保持一致)
            if (newSex != 0 && newSex != 1 && newSex != 2) {
                return R.fail("性别参数无效 (0-女, 1-男, 2-保密)");
            }
        }

        // 唯一性校验
        if (newEmail != null) {
            long emailExists = userService.count(new LambdaQueryWrapper<User>()
                    .eq(User::getUserEmail, newEmail)
                    .ne(User::getUserId, dbUser.getUserId()));
            if (emailExists > 0) {
                return R.fail("邮箱已存在");
            }
            if (!newEmail.equals(dbUser.getUserEmail())) {
                dbUser.setUserEmail(newEmail);
            }
        }

        if (newPhone != null && !newPhone.isEmpty()) {
            long phoneExists = userService.count(new LambdaQueryWrapper<User>()
                    .eq(User::getUserPhone, newPhone)
                    .ne(User::getUserId, dbUser.getUserId()));
            if (phoneExists > 0) {
                return R.fail("手机号已存在");
            }
            if (!newPhone.equals(dbUser.getUserPhone())) {
                dbUser.setUserPhone(newPhone);
            }
        }

        if (newSex != null) {
            dbUser.setUserSex(newSex);
        }
        // 不修改 userAvatar
        if (newRemark != null) {
            dbUser.setUserRemark(newRemark);
        }

        dbUser.setUpdateTime(LocalDateTime.now());
        boolean ok = userService.updateById(dbUser);
        if (!ok) {
            log.error("用户更新个人信息失败: userId={}", userId);
            return R.error("更新失败");
        }
        log.info("用户更新个人信息成功: userId={}", userId);
        return R.success(dbUser, "更新成功");
    }


    /**
     * 修改当前用户登录密码。
     * <p>作用：验证原密码并更新为新密码（Argon2 加密）。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>识别当前用户并获取安全信息。</li>
     *     <li>校验原密码与新密码的非空与长度规则。</li>
     *     <li>验证原密码是否正确。</li>
     *     <li>加密并保存新密码，清理旧盐。</li>
     *     <li>返回成功提示。</li>
     * </ol>
     *
     * @param request               HTTP 请求对象
     * @param updatePasswordRequest 原密码与新密码请求体
     * @return 成功提示
     */
    @PutMapping("/me/password")
    public R<String> updatePassword(HttpServletRequest request, @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String userId = currentUser.getUserId();
        UserSecurity security = userSecurityService.getById(userId);
        if (security == null) {
            return R.fail("用户安全信息不存在");
        }

        String originalPassword = CommonUtil.trim(updatePasswordRequest.getOriginalPassword());
        String newPassword = CommonUtil.trim(updatePasswordRequest.getNewPassword());

        if (originalPassword == null || originalPassword.isEmpty()) {
            return R.fail("原始密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return R.fail("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return R.fail("新密码至少 6 位");
        }

        // 校验原始密码 (Argon2)
        if (!passwordEncoder.matches(originalPassword, security.getUserPassword())) {
            return R.fail("原始密码错误");
        }

        // 加密新密码
        String newHashed = passwordEncoder.encode(newPassword);
        security.setUserPassword(newHashed);
        // Argon2 自带盐，清空旧盐字段
        security.setUserPasswordSalt(""); 
        security.setUpdateTime(LocalDateTime.now());

        boolean ok = userSecurityService.updateById(security);
        if (!ok) {
            log.error("用户修改密码失败: userId={}", userId);
            return R.error("密码更新失败");
        }

        log.info("用户修改密码成功: userId={}", userId);
        return R.success("更改成功");
    }

    /**
     * 上传并更新当前用户头像。
     * <p>作用：接受图片文件并上传至对象存储，更新用户头像URL。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>识别当前用户并获取数据库记录。</li>
     *     <li>校验文件类型与大小。</li>
     *     <li>删除旧头像（非默认）。</li>
     *     <li>上传新头像并获取 URL。</li>
     *     <li>更新用户信息并返回。</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @param file    头像文件（jpg/png/gif/webp 等）
     * @return 更新后的用户信息（包含新头像 URL）
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<User> updateAvatar(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String userId = currentUser.getUserId();
        User dbUser = userService.getById(userId);
        if (dbUser == null) {
            return R.notFound("用户不存在");
        }

        try {
            if (file == null || file.isEmpty()) {
                return R.fail("文件为空");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return R.fail("仅支持图片类型");
            }
            if (file.getSize() > 1024 * 1024) {
                return R.fail("文件大小不得超过1MB");
            }

            String original = file.getOriginalFilename();
            String suffix = "";
            if (original != null && original.contains(".")) {
                suffix = original.substring(original.lastIndexOf('.'));
            } else {
                String ct = contentType.toLowerCase();
                if (ct.contains("jpeg")) suffix = ".jpg";
                else if (ct.contains("jpg")) suffix = ".jpg";
                else if (ct.contains("png")) suffix = ".png";
                else if (ct.contains("gif")) suffix = ".gif";
                else if (ct.contains("webp")) suffix = ".webp";
                else suffix = ".bin";
            }

            String objectName = "avatars/" + userId + "avatar-" + System.currentTimeMillis() + suffix;

            // 删除旧头像
            String oldAvatarUrl = dbUser.getUserAvatar();
            if (oldAvatarUrl != null && !oldAvatarUrl.trim().isEmpty() && !isDefaultAvatarUrl(oldAvatarUrl)) {
                String oldObject = extractObjectNameFromUrl(oldAvatarUrl);
                if (oldObject != null) {
                    try { minioUtil.remove(oldObject); } catch (Exception e) {}
                }
            }

            String url;
            try (java.io.InputStream in = file.getInputStream()) {
                url = minioUtil.uploadKnownSize(objectName, in, contentType, file.getSize());
            }

            dbUser.setUserAvatar(url);
            dbUser.setUpdateTime(LocalDateTime.now());
            boolean ok = userService.updateById(dbUser);
            if (!ok) {
                try { minioUtil.remove(objectName); } catch (Exception ignore) {}
                log.error("用户头像更新失败: userId={}", userId);
                return R.error("头像更新失败");
            }
            log.info("用户头像更新成功: userId={}", userId);
            return R.success(dbUser, "头像更新成功");
        } catch (Exception e) {
            log.error("用户头像上传异常: userId={}, error={}", userId, e.getMessage());
            return R.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询用户列表。
     * <p>作用：支持用户名、邮箱、手机号模糊搜索并按用户名升序。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>识别当前用户。</li>
     *     <li>规范分页参数（page/size）。</li>
     *     <li>构建模糊查询条件（用户名/邮箱/手机号）。</li>
     *     <li>分页查询并返回结果。</li>
     * </ol>
     *
     * @param request   HTTP 请求对象
     * @param page      页码（默认1）
     * @param size      每页条数（默认10，最大100）
     * @param userName  用户名关键字（可选）
     * @param userEmail 邮箱关键字（可选）
     * @param userPhone 手机号关键字（可选）
     * @return 用户列表的分页结果
     */
    @GetMapping
    public R<Page<User>> listUsers(HttpServletRequest request,
                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                             @RequestParam(value = "size", defaultValue = "10") Integer size,
                                             @RequestParam(value = "userName", required = false) String userName,
                                             @RequestParam(value = "userEmail", required = false) String userEmail,
                                             @RequestParam(value = "userPhone", required = false) String userPhone) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        page = (page == null || page < 1) ? 1 : page;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        String matchUserName = CommonUtil.trim(userName);
        if (matchUserName != null && !matchUserName.isEmpty()) {
            wrapper.like(User::getUserName, matchUserName);
        }
        String matchEmail = CommonUtil.trim(userEmail);
        if (matchEmail != null && !matchEmail.isEmpty()) {
            wrapper.like(User::getUserEmail, matchEmail);
        }
        String matchPhone = CommonUtil.trim(userPhone);
        if (matchPhone != null && !matchPhone.isEmpty()) {
            wrapper.like(User::getUserPhone, matchPhone);
        }
        wrapper.orderByAsc(User::getUserName);

        Page<User> userPage = new Page<>(page, size);
        Page<User> result = userService.page(userPage, wrapper);

        return R.success(result, "查询成功");
    }


    private boolean isDefaultAvatarUrl(String url) {
        if (url == null) return true;
        String u = url.toLowerCase();
        return u.endsWith("/" + SystemConstant.DEFAULT_USER_AVATAR) || u.contains(SystemConstant.DEFAULT_USER_AVATAR);
    }

    private String extractObjectNameFromUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        String base = minioUtil.getObjectUrl("");
        if (base == null) return null;
        if (!base.endsWith("/")) base = base + "/";
        if (url.startsWith(base)) {
            return url.substring(base.length());
        }
        return null;
    }
}
