package top.smartduck.ducktodo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.smartduck.ducktodo.model.entity.ChildTask;

@Mapper
public interface ChildTaskMapper extends BaseMapper<ChildTask> {
}
