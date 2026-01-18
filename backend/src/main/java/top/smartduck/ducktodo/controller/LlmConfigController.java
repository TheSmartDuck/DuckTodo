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

import java.math.BigDecimal;
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
        if (currentUser == null) {
            return R.unauthorized("未能识别当前用户");
        }

        // 设置用户ID
        config.setUserId(currentUser.getUserId());
        
        // 生成配置ID（如果未提供）
        if (config.getUserLlmConfigId() == null || config.getUserLlmConfigId().trim().isEmpty()) {
            config.setUserLlmConfigId(UUID.randomUUID().toString());
        }

        // ========== 参数验证 ==========
        
        // 1. 提供商验证（必填）
        String provider = CommonUtil.trim(config.getLlmProvider());
        if (provider == null || provider.isEmpty()) {
            log.warn("创建LLM配置失败：提供商不能为空，用户ID={}", currentUser.getUserId());
            return R.fail("LLM提供商不能为空");
        }
        config.setLlmProvider(provider);

        // 2. API Key验证（必填）
        String apiKey = CommonUtil.trim(config.getLlmApiKey());
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("创建LLM配置失败：API Key不能为空，用户ID={}", currentUser.getUserId());
            return R.fail("LLM API Key不能为空");
        }
        config.setLlmApiKey(apiKey);

        // 3. API URL验证（仅在OpenAi兼容时必填）
        String apiUrl = CommonUtil.trim(config.getLlmApiUrl());
        if ("openai-compatible".equals(provider)) {
            // OpenAi兼容时，API URL必填
            if (apiUrl == null || apiUrl.isEmpty()) {
                log.warn("创建LLM配置失败：OpenAi兼容时API地址不能为空，用户ID={}", currentUser.getUserId());
                return R.fail("LLM API地址不能为空");
            }
            config.setLlmApiUrl(apiUrl);
        } else if (apiUrl != null && !apiUrl.isEmpty()) {
            // 非OpenAi兼容时，如果提供了URL也保存（可选）
            config.setLlmApiUrl(apiUrl);
        }

        // 4. 模型名称验证（必填）
        String modelName = CommonUtil.trim(config.getLlmModelName());
        if (modelName == null || modelName.isEmpty()) {
            log.warn("创建LLM配置失败：模型名称不能为空，用户ID={}", currentUser.getUserId());
            return R.fail("模型名称不能为空");
        }
        config.setLlmModelName(modelName);

        // 5. 模型类型验证（必填）
        Integer modelType = config.getLlmModelType();
        if (modelType == null) {
            log.warn("创建LLM配置失败：模型类型不能为空，用户ID={}", currentUser.getUserId());
            return R.fail("模型类型不能为空");
        }
        if (modelType != 1 && modelType != 2 && modelType != 3) {
            log.warn("创建LLM配置失败：模型类型参数无效，必须是1-chat/2-embedding/3-rerank，用户ID={}, modelType={}", 
                    currentUser.getUserId(), modelType);
            return R.fail("模型类型参数无效，必须是 1-chat, 2-embedding, 3-rerank");
        }

        // 6. 温度验证（可选，仅CHAT模型，范围0.0-1.0）
        if (config.getLlmModelTemperature() != null) {
            BigDecimal temperature = config.getLlmModelTemperature();
            if (temperature.compareTo(BigDecimal.ZERO) < 0 || temperature.compareTo(BigDecimal.ONE) > 0) {
                log.warn("创建LLM配置失败：温度参数无效，必须在0.0-1.0之间，用户ID={}, temperature={}", 
                        currentUser.getUserId(), temperature);
                return R.fail("温度参数无效，必须在0.0-1.0之间");
            }
            // 仅CHAT模型支持温度参数
            if (modelType != 1) {
                log.warn("创建LLM配置失败：温度参数仅支持CHAT模型，用户ID={}, modelType={}", 
                        currentUser.getUserId(), modelType);
                return R.fail("温度参数仅支持CHAT模型");
            }
        }

        // 7. 思考模式验证（可选，仅CHAT模型，0或1）
        if (config.getLlmModelThinking() != null) {
            Integer thinking = config.getLlmModelThinking();
            if (thinking != 0 && thinking != 1) {
                log.warn("创建LLM配置失败：思考模式参数无效，必须是0-否或1-是，用户ID={}, thinking={}", 
                        currentUser.getUserId(), thinking);
                return R.fail("思考模式参数无效，必须是 0-否 或 1-是");
            }
            // 仅CHAT模型支持思考模式
            if (modelType != 1) {
                log.warn("创建LLM配置失败：思考模式仅支持CHAT模型，用户ID={}, modelType={}", 
                        currentUser.getUserId(), modelType);
                return R.fail("思考模式仅支持CHAT模型");
            }
        }

        // ========== 保存配置 ==========
        config.setCreateTime(LocalDateTime.now());
        boolean ok = userLlmConfigService.save(config);
        if (!ok) {
            log.error("创建LLM配置失败：数据库保存失败，用户ID={}", currentUser.getUserId());
            return R.error("创建失败");
        }
        
        log.info("创建LLM配置成功，用户ID={}, 配置ID={}, 模型类型={}, 提供商={}", 
                currentUser.getUserId(), config.getUserLlmConfigId(), modelType, provider);
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
