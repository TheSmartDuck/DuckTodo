package top.smartduck.ducktodo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.model.dto.LoginUserDto;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.model.entity.UserSecurity;
import top.smartduck.ducktodo.modelService.UserSecurityService;
import top.smartduck.ducktodo.modelService.UserService;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSecurityService userSecurityService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 支持用户名或邮箱登录
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, username)
                .or()
                .eq(User::getUserEmail, username);
        
        User user = userService.getOne(queryWrapper);
        if (user == null) {
            log.debug("登录失败，用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 查询密码信息
        UserSecurity security = userSecurityService.getById(user.getUserId());
        if (security == null) {
            log.error("数据异常：用户存在但安全信息丢失, userId={}", user.getUserId());
            throw new UsernameNotFoundException("用户安全信息不存在: " + username);
        }

        return new LoginUserDto(user, security.getUserPassword());
    }
}
