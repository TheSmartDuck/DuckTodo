package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.UserToolConfigMapper;
import top.smartduck.ducktodo.model.entity.UserToolConfig;
import top.smartduck.ducktodo.modelService.UserToolConfigService;

@Service
public class UserToolConfigServiceImpl extends ServiceImpl<UserToolConfigMapper, UserToolConfig> implements UserToolConfigService {

    @Override
    public UserToolConfig getByUserId(String userId) {
        return this.getOne(new LambdaQueryWrapper<UserToolConfig>()
                .eq(UserToolConfig::getUserId, userId)
                .last("LIMIT 1"));
    }
}
