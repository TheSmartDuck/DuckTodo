package top.smartduck.ducktodo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.smartduck.ducktodo.common.enums.TaskAuditActionEnum;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.mapper.ChildTaskMapper;
import top.smartduck.ducktodo.model.entity.ChildTask;
import top.smartduck.ducktodo.model.entity.Task;
import top.smartduck.ducktodo.model.entity.TaskAudit;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.modelService.TaskAuditService;
import top.smartduck.ducktodo.util.CommonUtil;
import top.smartduck.ducktodo.model.response.TaskDetailResponse;

import java.lang.reflect.Method;

@Aspect
@Component
public class TaskAuditAspect {

    @Autowired
    private TaskAuditService taskAuditService;

    @Autowired
    private ChildTaskMapper childTaskMapper;

    @Pointcut("@annotation(top.smartduck.ducktodo.aspect.TaskAuditLog)")
    public void auditPointcut() {}

    @Around("auditPointcut() && @annotation(taskAuditLog)")
    public Object aroundAudit(ProceedingJoinPoint pjp, TaskAuditLog taskAuditLog) throws Throwable {
        Object result = null;
        Throwable error = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            try {
                recordAudit(pjp, taskAuditLog, result, error);
            } catch (Exception ignore) {
            }
        }
    }

    private void recordAudit(ProceedingJoinPoint pjp, TaskAuditLog taskAuditLog, Object result, Throwable error) {
        String taskId = extractTaskId(pjp.getArgs(), result);
        if (taskId == null || taskId.isEmpty()) {
            return;
        }
        User current = CommonUtil.getCurrentUser(null);
        String operatorId = (current == null) ? null : current.getUserId();
        String actionType = (taskAuditLog.action() == null ? TaskAuditActionEnum.UPDATE : taskAuditLog.action()).getCode();
        String desc = taskAuditLog.description();
        if (desc == null || desc.isEmpty()) {
            desc = pjp.getSignature().toShortString();
        }
        if (error != null) {
            desc = desc + " - ERROR: " + error.getClass().getSimpleName();
        }
        TaskAudit audit = TaskAudit.builder()
                .taskId(taskId)
                .operatorId(operatorId)
                .actionType(actionType)
                .actionDescription(desc)
                .build();
        taskAuditService.save(audit);
    }

    private String extractTaskId(Object[] args, Object result) {
        // Try from result first (preferred)
        String fromResult = extractTaskIdFromResult(result);
        if (fromResult != null && !fromResult.isEmpty()) {
            return fromResult;
        }
        // Fallback: search from args
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) continue;
                if (arg instanceof Task) {
                    Task t = (Task) arg;
                    if (t.getTaskId() != null && !t.getTaskId().isEmpty()) {
                        return t.getTaskId();
                    }
                } else if (arg instanceof ChildTask) {
                    ChildTask ct = (ChildTask) arg;
                    if (ct.getTaskId() != null && !ct.getTaskId().isEmpty()) {
                        return ct.getTaskId();
                    }
                    if (ct.getChildTaskId() != null && !ct.getChildTaskId().isEmpty()) {
                        ChildTask full = childTaskMapper.selectById(ct.getChildTaskId());
                        if (full != null && full.getTaskId() != null && !full.getTaskId().isEmpty()) {
                            return full.getTaskId();
                        }
                    }
                } else if (arg instanceof String) {
                    String id = (String) arg;
                    if (id != null && !id.trim().isEmpty()) {
                        ChildTask maybeChild = childTaskMapper.selectById(id.trim());
                        if (maybeChild != null && maybeChild.getTaskId() != null && !maybeChild.getTaskId().isEmpty()) {
                            return maybeChild.getTaskId();
                        }
                        return id.trim();
                    }
                }
            }
        }
        return null;
    }

    private String extractTaskIdFromResult(Object result) {
        if (result == null) return null;
        // Try typed R<T>
        if (result instanceof R<?>) {
            Object data = ((R<?>) result).getData();
            String id = extractTaskIdFromData(data);
            if (id != null) return id;
        } else {
            // Try reflective getData()
            try {
                Method m = result.getClass().getMethod("getData");
                Object data = m.invoke(result);
                String id = extractTaskIdFromData(data);
                if (id != null) return id;
            } catch (Exception ignored) {}
        }
        // If result itself is entity
        return extractTaskIdFromData(result);
    }

    private String extractTaskIdFromData(Object data) {
        if (data == null) return null;
        if (data instanceof Task) {
            Task t = (Task) data;
            return t.getTaskId();
        }
        if (data instanceof TaskDetailResponse) {
            TaskDetailResponse resp = (TaskDetailResponse) data;
            return resp.getTaskId();
        }
        if (data instanceof ChildTask) {
            ChildTask ct = (ChildTask) data;
            if (ct.getTaskId() != null && !ct.getTaskId().isEmpty()) return ct.getTaskId();
            if (ct.getChildTaskId() != null && !ct.getChildTaskId().isEmpty()) {
                ChildTask full = childTaskMapper.selectById(ct.getChildTaskId());
                return full == null ? null : full.getTaskId();
            }
        }
        return null;
    }
}
