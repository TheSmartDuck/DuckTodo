package top.smartduck.ducktodo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.smartduck.ducktodo.model.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
