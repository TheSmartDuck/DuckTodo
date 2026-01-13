package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.model.entity.UserLlmConfig;
import top.smartduck.ducktodo.modelService.UserLlmConfigService;
import top.smartduck.ducktodo.util.CommonUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * LLM 配置接口（需 JWT 鉴权）
 *
 * <p>接口列表：</p>
 * <ol>
 *     <li>
 *         <b>GET /api/llm-configs</b>
 *         <br/>简介：获取当前用户的 LLM 配置列表。
 *     </li>
 *     <li>
 *         <b>POST /api/llm-configs</b>
 *         <br/>简介：添加当前用户的 LLM 配置。
 *     </li>
 *     <li>
 *         <b>PUT /api/llm-configs/{configId}</b>
 *         <br/>简介：更改当前用户的 LLM 配置。
 *     </li>
 *     <li>
 *         <b>DELETE /api/llm-configs/{configId}</b>
 *         <br/>简介：删除当前用户的 LLM 配置。
 *     </li>
 *     <li>
 *         <b>GET /api/llm-configs/{configId}</b>
 *         <br/>简介：基于配置ID查询单个 LLM 配置。
 *     </li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/llm-configs")
public class LlmConfigController {

    @Autowired
    private UserLlmConfigService userLlmConfigService;

    /**
     * 获取当前用户的 LLM 配置列表
     *
     * @param request HTTP请求对象
     * @return LLM 配置列表
     */
    @GetMapping
    public R<List<UserLlmConfig>> listConfigs(HttpServletRequest request) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        List<UserLlmConfig> list = userLlmConfigService.list(
                new LambdaQueryWrapper<UserLlmConfig>().eq(UserLlmConfig::getUserId, currentUser.getUserId())
        );
        return R.success(list);
    }

    /**
     * 添加 LLM 配置
     *
     * @param request HTTP请求对象
     * @param config  LLM 配置
     * @return 创建成功后的配置
     */
    @PostMapping
    public R<UserLlmConfig> createConfig(HttpServletRequest request, @RequestBody UserLlmConfig config) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        config.setUserId(currentUser.getUserId());
        if (config.getUserLlmConfigId() == null || config.getUserLlmConfigId().trim().isEmpty()) {
            config.setUserLlmConfigId(UUID.randomUUID().toString());
        }
        String provider = CommonUtil.trim(config.getLlmProvider());
        String apiKey = CommonUtil.trim(config.getLlmApiKey());
        String apiUrl = CommonUtil.trim(config.getLlmApiUrl());
        String modelName = CommonUtil.trim(config.getLlmModelName());
        if (provider != null) config.setLlmProvider(provider);
        if (apiKey != null) config.setLlmApiKey(apiKey);
        if (apiUrl != null) config.setLlmApiUrl(apiUrl);
        if (modelName != null) config.setLlmModelName(modelName);
        boolean ok = userLlmConfigService.save(config);
        if (!ok) return R.error("创建失败");
        return R.success(config, "创建成功");
    }

    /**
     * 更改 LLM 配置
     *
     * @param request  HTTP请求对象
     * @param configId 配置ID
     * @param config   变更的字段
     * @return 更新后的配置
     */
    @PutMapping("/{configId}")
    public R<UserLlmConfig> updateConfig(HttpServletRequest request,
                                   @PathVariable("configId") String configId,
                                   @RequestBody UserLlmConfig config) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        UserLlmConfig db = userLlmConfigService.getById(configId);
        if (db == null) return R.notFound("配置不存在");
        if (!currentUser.getUserId().equals(db.getUserId())) return R.unauthorized("无权操作该配置");
        String provider = CommonUtil.trim(config.getLlmProvider());
        String apiKey = CommonUtil.trim(config.getLlmApiKey());
        String apiUrl = CommonUtil.trim(config.getLlmApiUrl());
        String modelName = CommonUtil.trim(config.getLlmModelName());
        if (provider != null) db.setLlmProvider(provider);
        if (apiKey != null) db.setLlmApiKey(apiKey);
        if (apiUrl != null) db.setLlmApiUrl(apiUrl);
        if (modelName != null) db.setLlmModelName(modelName);
        if (config.getLlmModelTemperature() != null) db.setLlmModelTemperature(config.getLlmModelTemperature());
        if (config.getLlmModelThinking() != null) db.setLlmModelThinking(config.getLlmModelThinking());
        db.setUpdateTime(LocalDateTime.now());
        boolean ok = userLlmConfigService.updateById(db);
        if (!ok) return R.error("更新失败");
        return R.success(db, "更新成功");
    }

    /**
     * 删除 LLM 配置
     *
     * @param request  HTTP请求对象
     * @param configId 配置ID
     * @return 删除是否成功
     */
    @DeleteMapping("/{configId}")
    public R<Boolean> deleteConfig(HttpServletRequest request, @PathVariable("configId") String configId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        UserLlmConfig db = userLlmConfigService.getById(configId);
        if (db == null) return R.notFound("配置不存在");
        if (!currentUser.getUserId().equals(db.getUserId())) return R.unauthorized("无权操作该配置");
        boolean ok = userLlmConfigService.removeById(configId);
        if (!ok) return R.error("删除失败");
        return R.success(true, "删除成功");
    }

    /**
     * 基于ID查询 LLM 配置
     *
     * @param request  HTTP请求对象
     * @param configId 配置ID
     * @return 单个配置详情
     */
    @GetMapping("/{configId}")
    public R<UserLlmConfig> getConfigById(HttpServletRequest request, @PathVariable("configId") String configId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        UserLlmConfig db = userLlmConfigService.getById(configId);
        if (db == null) return R.notFound("配置不存在");
        if (!currentUser.getUserId().equals(db.getUserId())) return R.unauthorized("无权访问该配置");
        return R.success(db);
    }
}
