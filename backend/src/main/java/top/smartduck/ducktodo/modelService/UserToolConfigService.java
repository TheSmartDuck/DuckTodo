package top.smartduck.ducktodo.modelService;

import com.baomidou.mybatisplus.extension.service.IService;
import top.smartduck.ducktodo.model.entity.UserToolConfig;

public interface UserToolConfigService extends IService<UserToolConfig> {
    /**
     * 根据用户ID获取工具配置
     * @param userId 用户ID
     * @return 工具配置
     */
    UserToolConfig getByUserId(String userId);
}
