package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TaskMapper;
import top.smartduck.ducktodo.model.entity.Task;
import top.smartduck.ducktodo.modelService.TaskService;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {
}
