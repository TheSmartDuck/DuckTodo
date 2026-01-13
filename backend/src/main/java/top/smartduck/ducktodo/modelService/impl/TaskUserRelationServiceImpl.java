package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TaskUserRelationMapper;
import top.smartduck.ducktodo.model.entity.TaskUserRelation;
import top.smartduck.ducktodo.modelService.TaskUserRelationService;

@Service
public class TaskUserRelationServiceImpl extends ServiceImpl<TaskUserRelationMapper, TaskUserRelation> implements TaskUserRelationService {
}
