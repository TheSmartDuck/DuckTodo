package top.smartduck.ducktodo.modelService;

import com.baomidou.mybatisplus.extension.service.IService;
import top.smartduck.ducktodo.model.entity.UserLlmConfig;

public interface UserLlmConfigService extends IService<UserLlmConfig> {
    /**
     * 根据用户ID获取LLM配置
     * @param userId 用户ID
     * @return LLM配置
     */
    UserLlmConfig getByUserId(String userId);
}
