package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.ChildTaskMapper;
import top.smartduck.ducktodo.model.entity.ChildTask;
import top.smartduck.ducktodo.modelService.ChildTaskService;

@Service
public class ChildTaskServiceImpl extends ServiceImpl<ChildTaskMapper, ChildTask> implements ChildTaskService {
}
