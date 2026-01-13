package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TaskGroupMapper;
import top.smartduck.ducktodo.model.entity.TaskGroup;
import top.smartduck.ducktodo.modelService.TaskGroupService;

@Service
public class TaskGroupServiceImpl extends ServiceImpl<TaskGroupMapper, TaskGroup> implements TaskGroupService {
}
