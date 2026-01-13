package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.UserLlmConfigMapper;
import top.smartduck.ducktodo.model.entity.UserLlmConfig;
import top.smartduck.ducktodo.modelService.UserLlmConfigService;

@Service
public class UserLlmConfigServiceImpl extends ServiceImpl<UserLlmConfigMapper, UserLlmConfig> implements UserLlmConfigService {

    @Override
    public UserLlmConfig getByUserId(String userId) {
        return this.getOne(new LambdaQueryWrapper<UserLlmConfig>()
                .eq(UserLlmConfig::getUserId, userId)
                .last("LIMIT 1"));
    }
}
