package top.smartduck.ducktodo.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.smartduck.ducktodo.model.entity.Task;
import top.smartduck.ducktodo.model.entity.ChildTask;
import top.smartduck.ducktodo.modelService.TaskService;
import top.smartduck.ducktodo.modelService.ChildTaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BaseTask {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ChildTaskService childTaskService;

    @Scheduled(cron = "0 1 0 * * ?")
    public void startTasksBySchedule() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> toStart = taskService.list(new LambdaQueryWrapper<Task>()
                .eq(Task::getTaskStatus, "1")
                .isNotNull(Task::getStartTime)
                .le(Task::getStartTime, now));
        if (toStart == null || toStart.isEmpty()) return;
        List<String> taskIds = toStart.stream().map(Task::getTaskId).filter(id -> id != null && !id.trim().isEmpty()).collect(Collectors.toList());
        if (taskIds.isEmpty()) return;
        taskService.update(new LambdaUpdateWrapper<Task>()
                .in(Task::getTaskId, taskIds)
                .set(Task::getTaskStatus, "2")
                .set(Task::getUpdateTime, now));
        childTaskService.update(new LambdaUpdateWrapper<ChildTask>()
                .in(ChildTask::getTaskId, taskIds)
                .eq(ChildTask::getChildTaskStatus, "1")
                .set(ChildTask::getChildTaskStatus, "2")
                .set(ChildTask::getUpdateTime, now));
    }
}
