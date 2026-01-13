package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.UserSecurityMapper;
import top.smartduck.ducktodo.model.entity.UserSecurity;
import top.smartduck.ducktodo.modelService.UserSecurityService;

@Service
public class UserSecurityServiceImpl extends ServiceImpl<UserSecurityMapper, UserSecurity> implements UserSecurityService {
}
