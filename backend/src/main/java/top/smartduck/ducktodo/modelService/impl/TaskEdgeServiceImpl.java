package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TaskEdgeMapper;
import top.smartduck.ducktodo.model.entity.TaskEdge;
import top.smartduck.ducktodo.modelService.TaskEdgeService;

@Service
public class TaskEdgeServiceImpl extends ServiceImpl<TaskEdgeMapper, TaskEdge> implements TaskEdgeService {
}
