package top.smartduck.ducktodo.modelService.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.smartduck.ducktodo.mapper.TaskFileMapper;
import top.smartduck.ducktodo.model.entity.TaskFile;
import top.smartduck.ducktodo.modelService.TaskFileService;

@Service
public class TaskFileServiceImpl extends ServiceImpl<TaskFileMapper, TaskFile> implements TaskFileService {
}
