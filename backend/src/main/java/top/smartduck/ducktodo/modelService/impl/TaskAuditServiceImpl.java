package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TaskAuditMapper;
import top.smartduck.ducktodo.model.entity.TaskAudit;
import top.smartduck.ducktodo.modelService.TaskAuditService;

@Service
public class TaskAuditServiceImpl extends ServiceImpl<TaskAuditMapper, TaskAudit> implements TaskAuditService {
}
