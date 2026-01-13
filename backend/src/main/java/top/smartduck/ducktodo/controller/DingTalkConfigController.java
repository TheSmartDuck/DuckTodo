package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.model.entity.UserDingtalkRobot;
import top.smartduck.ducktodo.modelService.UserDingtalkRobotService;
import top.smartduck.ducktodo.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 钉钉机器人配置接口（需 JWT 鉴权）
 *
 * <p>接口列表：</p>
 * <ol>
 *     <li>
 *         <b>GET /api/dingtalk-robot-configs</b>
 *         <br/>简介：获取当前用户的钉钉机器人配置列表。
 *     </li>
 *     <li>
 *         <b>POST /api/dingtalk-robot-configs</b>
 *         <br/>简介：添加当前用户的钉钉机器人配置。
 *     </li>
 *     <li>
 *         <b>PUT /api/dingtalk-robot-configs/{robotId}</b>
 *         <br/>简介：更改当前用户的钉钉机器人配置。
 *     </li>
 *     <li>
 *         <b>DELETE /api/dingtalk-robot-configs/{robotId}</b>
 *         <br/>简介：删除当前用户的钉钉机器人配置。
 *     </li>
 *     <li>
 *         <b>GET /api/dingtalk-robot-configs/{robotId}</b>
 *         <br/>简介：基于配置ID查询单个钉钉机器人配置。
 *     </li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/dingtalk-robot-configs")
public class DingTalkConfigController {

    @Autowired
    private UserDingtalkRobotService userDingtalkRobotService;

    /**
     * 获取当前用户的钉钉机器人配置列表
     *
     * @param request HTTP请求对象
     * @return 钉钉机器人配置列表
     */
    @GetMapping
    public R<List<UserDingtalkRobot>> listConfigs(HttpServletRequest request) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        List<UserDingtalkRobot> list = userDingtalkRobotService.list(
                new LambdaQueryWrapper<UserDingtalkRobot>().eq(UserDingtalkRobot::getUserId, currentUser.getUserId())
        );
        return R.success(list);
    }

    /**
     * 添加钉钉机器人配置
     *
     * @param request HTTP请求对象
     * @param robot   钉钉机器人配置
     * @return 创建成功后的配置
     */
    @PostMapping
    public R<UserDingtalkRobot> createConfig(HttpServletRequest request, @RequestBody UserDingtalkRobot robot) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        robot.setUserId(currentUser.getUserId());
        String name = CommonUtil.trim(robot.getRobotName());
        String token = CommonUtil.trim(robot.getDingtalkRobotToken());
        String secret = CommonUtil.trim(robot.getDingtalkRobotSecret());
        String keyword = CommonUtil.trim(robot.getDingtalkRobotKeyword());
        if (name != null) robot.setRobotName(name);
        if (token != null) robot.setDingtalkRobotToken(token);
        if (secret != null) robot.setDingtalkRobotSecret(secret);
        if (keyword != null) robot.setDingtalkRobotKeyword(keyword);
        boolean ok = userDingtalkRobotService.save(robot);
        if (!ok) return R.error("创建失败");
        return R.success(robot, "创建成功");
    }

    /**
     * 更改钉钉机器人配置
     *
     * @param request HTTP请求对象
     * @param robotId 配置ID
     * @param robot   变更的字段
     * @return 更新后的配置
     */
    @PutMapping("/{robotId}")
    public R<UserDingtalkRobot> updateConfig(HttpServletRequest request,
                                       @PathVariable("robotId") String robotId,
                                       @RequestBody UserDingtalkRobot robot) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        UserDingtalkRobot db = userDingtalkRobotService.getById(robotId);
        if (db == null) return R.notFound("配置不存在");
        if (!currentUser.getUserId().equals(db.getUserId())) return R.unauthorized("无权操作该配置");
        String name = CommonUtil.trim(robot.getRobotName());
        String token = CommonUtil.trim(robot.getDingtalkRobotToken());
        String secret = CommonUtil.trim(robot.getDingtalkRobotSecret());
        String keyword = CommonUtil.trim(robot.getDingtalkRobotKeyword());
        if (name != null) db.setRobotName(name);
        if (token != null) db.setDingtalkRobotToken(token);
        if (secret != null) db.setDingtalkRobotSecret(secret);
        if (keyword != null) db.setDingtalkRobotKeyword(keyword);
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = userDingtalkRobotService.updateById(db);
        if (!ok) return R.error("更新失败");
        return R.success(db, "更新成功");
    }

    /**
     * 删除钉钉机器人配置
     *
     * @param request HTTP请求对象
     * @param robotId 配置ID
     * @return 删除是否成功
     */
    @DeleteMapping("/{robotId}")
    public R<Boolean> deleteConfig(HttpServletRequest request, @PathVariable("robotId") String robotId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        UserDingtalkRobot db = userDingtalkRobotService.getById(robotId);
        if (db == null) return R.notFound("配置不存在");
        if (!currentUser.getUserId().equals(db.getUserId())) return R.unauthorized("无权操作该配置");
        boolean ok = userDingtalkRobotService.removeById(robotId);
        if (!ok) return R.error("删除失败");
        return R.success(true, "删除成功");
    }

    /**
     * 基于ID查询钉钉机器人配置
     *
     * @param request HTTP请求对象
     * @param robotId 配置ID
     * @return 单个配置详情
     */
    @GetMapping("/{robotId}")
    public R<UserDingtalkRobot> getConfigById(HttpServletRequest request, @PathVariable("robotId") String robotId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        UserDingtalkRobot db = userDingtalkRobotService.getById(robotId);
        if (db == null) return R.notFound("配置不存在");
        if (!currentUser.getUserId().equals(db.getUserId())) return R.unauthorized("无权访问该配置");
        return R.success(db);
    }
}
