package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import top.smartduck.ducktodo.common.enums.*;
import top.smartduck.ducktodo.model.dto.LoginUserDto;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.model.entity.UserSecurity;
import top.smartduck.ducktodo.model.entity.TaskGroup;
import top.smartduck.ducktodo.model.entity.TaskGroupUserRelation;
import top.smartduck.ducktodo.model.request.LoginRequest;
import top.smartduck.ducktodo.model.request.RegisterRequest;
import top.smartduck.ducktodo.modelService.UserService;
import top.smartduck.ducktodo.modelService.UserSecurityService;
import top.smartduck.ducktodo.modelService.TaskGroupService;
import top.smartduck.ducktodo.modelService.TaskGroupUserRelationService;
import top.smartduck.ducktodo.util.CommonUtil;
import top.smartduck.ducktodo.util.JwtUtil;
import top.smartduck.ducktodo.util.MinioUtil;
import top.smartduck.ducktodo.common.exception.BusinessException;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.common.constant.SystemConstant;
import top.smartduck.ducktodo.model.response.LoginResponse;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * 无需 JWT 鉴权的公共接口控制器。
 * 提供健康检查、登录、注册与登出等基础能力。
 *
 * <p>接口列表：</p>
 * <ol>
 *     <li><b>GET /api/base/health</b>：系统健康检查，返回应用信息、服务器时间与存储服务连通性。</li>
 *     <li><b>POST /api/base/login</b>：用户登录，支持“用户名/邮箱 + 密码”，认证成功后返回 JWT Token 与用户信息。</li>
 *     <li><b>POST /api/base/register</b>：用户注册，创建用户与默认任务族，并自动登录返回 Token。</li>
 *     <li><b>POST /api/base/logout</b>：用户登出（JWT 无状态），记录审计日志，提示前端丢弃 Token。</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/base")
public class BaseController {
    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private TaskGroupUserRelationService taskGroupUserRelationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 系统健康检查。
     * <p>作用：用于探测服务状态与关键依赖的连通性。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>构建基础状态信息（状态、应用名、版本、服务器时间、时间戳）。</li>
     *     <li>尝试获取 MinIO 中健康检查文件的访问 URL，校验存储服务连通性并解析根路径。</li>
     *     <li>返回汇总的系统健康状态。</li>
     * </ol>
     *
     * @return 包含系统状态、应用信息、服务器时间与存储服务路径的响应对象
     */
    @GetMapping("/health")
    public R<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("appName", appName);
        status.put("version", appVersion);
        status.put("serverTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        status.put("timestamp", System.currentTimeMillis());
        
        String sampleUrl = minioUtil.getObjectUrl("health-check.txt");
        if (sampleUrl != null) {
            // 截取到 bucket 路径，例如 http://minio:9000/ducktodo/
            int lastSlash = sampleUrl.lastIndexOf('/');
            if (lastSlash > 0) {
                 status.put("storagePath", sampleUrl.substring(0, lastSlash + 1));
            } else {
                 status.put("storagePath", sampleUrl);
            }
        } else {
             status.put("storagePath", "MinIO Config Error");
        }

        return R.success(status);
    }

    /**
     * 用户登录（支持用户名或邮箱 + 密码）。
     * <p>作用：基于 Spring Security 完成用户名或邮箱口令认证，并签发访问令牌。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>解析请求体，提取用户名/邮箱与密码，确定认证主体。</li>
     *     <li>调用 AuthenticationManager 执行认证，失败返回错误提示。</li>
     *     <li>认证成功后更新用户最后登录时间。</li>
     *     <li>生成 JWT Token，并封装用户信息。</li>
     *     <li>返回包含 Token 与用户信息的响应对象。</li>
     * </ol>
     *
     * @param loginRequest 登录请求体
     * @return 包含 JWT Token 与用户信息的响应对象
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        String userName = loginRequest.getUserName();
        String email = loginRequest.getUserEmail();
        String password = loginRequest.getUserPassword();

        String principal = (userName != null && !userName.isEmpty()) ? userName : email;

        if (principal == null || principal.trim().isEmpty()) {
            return R.fail("缺少用户名或邮箱");
        }
        if (password == null || password.trim().isEmpty()) {
            return R.fail("缺少密码");
        }

        // 1. 使用 AuthenticationManager 进行认证
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(principal, password);
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            log.warn("用户登录失败: {}, 原因: {}", principal, e.getMessage());
            return R.fail("用户不存在或密码错误");
        }

        // 2. 获取认证后的用户信息
        LoginUserDto loginUser = (LoginUserDto) authenticate.getPrincipal();
        User user = loginUser.getUser();

        // 3. 更新最后登入时间
        user.setLastLoginTime(LocalDateTime.now());
        userService.updateById(user);

        // 4. 生成 Token
        String token = jwtUtil.generateToken(user);

        LoginResponse loginResponse = LoginResponse.fromUser(user, token);

        log.info("用户登录成功: {}", user.getUserName());
        return R.success(loginResponse);
    }

    /**
     * 用户注册并初始化默认任务族。
     * <p>作用：创建用户基础与安全信息，初始化默认任务族与关系，并自动登录。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>校验必填参数（用户名、邮箱、密码、手机号、性别）。</li>
     *     <li>校验格式（邮箱、密码复杂度、手机号、性别枚举值）。</li>
     *     <li>执行唯一性检查（用户名、邮箱、手机号）。</li>
     *     <li>创建用户基础信息并保存。</li>
     *     <li>创建用户安全信息（Argon2 加密密码）并保存。</li>
     *     <li>创建默认任务族与用户关系（Owner，正常状态）。</li>
     *     <li>生成 JWT Token 并返回登录响应。</li>
     * </ol>
     * <p>注意：方法在事务中执行，任一步骤失败将触发回滚。</p>
     *
     * @param request 注册请求体
     * @return 包含 JWT Token 与用户信息的响应对象
     */
    @PostMapping("/register")
    @Transactional(rollbackFor = Exception.class)
    public R<LoginResponse> register(@RequestBody RegisterRequest request) {
        String userName = request.getUserName();
        String email = request.getUserEmail();
        String password = request.getUserPassword();
        String phone = request.getUserPhone();
        Integer sex = request.getUserSex();

        // 1. 基础非空校验
        if (userName == null || userName.trim().isEmpty()) {
            return R.fail("用户名不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            return R.fail("邮箱不能为空");
        }
        if (password == null || password.trim().isEmpty()) {
            return R.fail("密码不能为空");
        }
        if (phone == null || phone.trim().isEmpty()) {
            return R.fail("手机号不能为空");
        }
        if (sex == null) {
            return R.fail("性别不能为空");
        }

        // 2. 格式校验
        // 邮箱
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        if (!emailPattern.matcher(email).matches()) {
            return R.fail("邮箱格式不正确");
        }
        
        // 密码 (8位以上且包含英文)
        // (?=.*[a-zA-Z]) 表示至少包含一个字母
        // .{8,} 表示长度至少8位
        if (password.length() < 8 || !Pattern.matches(".*[a-zA-Z].*", password)) {
            return R.fail("密码需8位以上且包含英文");
        }

        // 手机号 (11位数字)
        Pattern phonePattern = Pattern.compile("^\\d{11}$");
        if (!phonePattern.matcher(phone).matches()) {
            return R.fail("手机号格式不正确");
        }

        // 性别 (0-女, 1-男, 2-保密)
        if (!CommonUtil.inEnumCodes(UserSexEnum.values(), sex)) {
            return R.fail("性别参数无效");
        }

        // 3. 唯一性检查
        if (userService.count(new LambdaQueryWrapper<User>().eq(User::getUserName, userName)) > 0) {
            return R.fail("用户名已存在");
        }
        if (userService.count(new LambdaQueryWrapper<User>().eq(User::getUserEmail, email)) > 0) {
            return R.fail("邮箱已存在");
        }
        if (userService.count(new LambdaQueryWrapper<User>().eq(User::getUserPhone, phone)) > 0) {
            return R.fail("手机号已存在");
        }

        // 4. 创建 User (基础信息)
        User user = new User();
        user.setUserName(userName);
        user.setUserEmail(email);
        user.setUserPhone(phone);
        user.setUserSex(sex);
        user.setUserAvatar(minioUtil.getObjectUrl(SystemConstant.DEFAULT_USER_AVATAR));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        if (!userService.save(user)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "注册失败：保存用户信息失败");
        }

        // 5. 创建 UserSecurity (密码信息)
        UserSecurity security = new UserSecurity();
        security.setUserId(user.getUserId());
        // 使用 Argon2 加密，无需手动生成盐
        security.setUserPassword(passwordEncoder.encode(password));
        security.setUserPasswordSalt(""); // Argon2 内置盐，字段保留为空或废弃
        security.setCreateTime(LocalDateTime.now());
        security.setUpdateTime(LocalDateTime.now());

        if (!userSecurityService.save(security)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "注册失败：保存安全信息失败");
        }

        // 6. 创建默认任务族
        TaskGroup defaultGroup = new TaskGroup();
        defaultGroup.setTeamId("");
        defaultGroup.setGroupName("默认任务族");
        defaultGroup.setGroupStatus(TaskGroupStatusEnum.NORMAL.getCode());
        defaultGroup.setCreateTime(LocalDateTime.now());
        defaultGroup.setUpdateTime(LocalDateTime.now());
        if (!taskGroupService.save(defaultGroup)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "注册失败：默认任务族创建失败");
        }

        TaskGroupUserRelation rel = new TaskGroupUserRelation();
        rel.setTaskGroupId(defaultGroup.getTaskGroupId());
        rel.setUserId(user.getUserId());
        rel.setUserRole(UserRoleEnum.OWNER.getCode()); // 0-owner
        rel.setUserStatus(UserStatusEnum.NORMAL.getCode()); // 1-正常
        rel.setGroupIndex(1);
        rel.setGroupColor(SystemConstant.DEFAULT_GROUP_COLOR);
        rel.setGroupAlias(defaultGroup.getGroupName());
        rel.setJoinTime(LocalDateTime.now());
        rel.setUpdateTime(LocalDateTime.now());
        if (!taskGroupUserRelationService.save(rel)) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "注册失败：默认任务族关系创建失败");
        }

        // 7. 生成 Token 并返回
        String token = jwtUtil.generateToken(user);
        LoginResponse resp = LoginResponse.fromUser(user, token);
        log.info("用户注册成功: {}", userName);
        return R.success(resp);
    }

    /**
     * 用户登出（JWT 无状态，需由前端丢弃 Token）。
     * <p>作用：记录登出审计日志并提示前端清除本地令牌。</p>
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>尝试获取当前登录用户信息。</li>
     *     <li>若存在用户，记录登出日志。</li>
     *     <li>返回成功响应，提示前端清除本地 Token。</li>
     * </ol>
     *
     * @param request HTTP 请求
     * @return 成功提示
     */
    @PostMapping("/logout")
    public R<String> logout(jakarta.servlet.http.HttpServletRequest request) {
        // 尝试获取当前用户，用于记录日志
        try {
            // 注意：CommonUtil.getCurrentUser 可能会依赖 SecurityContext，
            // 如果 Token 无效或过期，这里可能获取不到用户，这是正常的。
            User user = CommonUtil.getCurrentUser(request);
            if (user != null) {
                log.info("用户登出: userId={}, userName={}", user.getUserId(), user.getUserName());
            }
        } catch (Exception e) {
            // 忽略异常，确保登出接口总是返回成功
            log.debug("登出时获取用户信息失败: {}", e.getMessage());
        }

        return R.successMsg("已成功登出");
    }
}
