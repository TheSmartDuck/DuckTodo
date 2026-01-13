package top.smartduck.ducktodo.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import top.smartduck.ducktodo.model.entity.User;

import java.util.Collection;
import java.util.Collections;

/**
 * 登录用户 DTO，封装 User 实体与 Security 所需信息
 */
@Data
public class LoginUserDto implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户基本信息
     */
    private User user;

    /**
     * 密码（来自 UserSecurity）
     */
    @JsonIgnore
    private String password;

    public LoginUserDto(User user, String password) {
        this.user = user;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 暂无角色权限设计，返回空
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 根据 User 中的 isDelete 逻辑，如果已删除则不可用，但通常查询时已过滤
        // 这里默认可用，或可结合 team/user_status 判断
        return true;
    }
}
