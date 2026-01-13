package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TaskNodeMapper;
import top.smartduck.ducktodo.model.entity.TaskNode;
import top.smartduck.ducktodo.modelService.TaskNodeService;

@Service
public class TaskNodeServiceImpl extends ServiceImpl<TaskNodeMapper, TaskNode> implements TaskNodeService {
}
