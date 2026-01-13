package top.smartduck.ducktodo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.UserMapper;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.modelService.UserService;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
