package top.smartduck.ducktodo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.smartduck.ducktodo.model.entity.TaskFile;

@Mapper
public interface TaskFileMapper extends BaseMapper<TaskFile> {
}
