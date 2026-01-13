package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.smartduck.ducktodo.common.enums.TaskPriorityEnum;
import top.smartduck.ducktodo.common.enums.TaskStatusEnum;
import top.smartduck.ducktodo.common.enums.UserStatusEnum;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.model.entity.*;
import top.smartduck.ducktodo.model.response.statistics.*;
import top.smartduck.ducktodo.modelService.*;
import top.smartduck.ducktodo.util.CommonUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ChildTaskService childTaskService;

    @Autowired
    private TaskUserRelationService taskUserRelationService;

    @Autowired
    private TeamUserRelationService teamUserRelationService;

    @Autowired
    private TaskGroupUserRelationService taskGroupUserRelationService;

    @Autowired
    private TaskAuditService taskAuditService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private TaskNodeService taskNodeService;

    @Autowired
    private TaskEdgeService taskEdgeService;

    @Autowired
    private UserService userService;

    @GetMapping("/me/overview/joined")
    /**
     * 个人概览统计（加入的团队/任务族）
     *
     * 接口：GET `/api/stats/me/overview/joined`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计当前用户加入且状态为 Normal 的团队数量
     * - 统计当前用户加入且状态为 Normal 的任务族数量
     *
     * 返回：`R<JoinedCountResponse>`
     * - `teamCount`：团队数量
     * - `taskGroupCount`：任务族数量
     *
     * 异常/失败：
     * - 401：未认证（无法识别当前用户）
     */
    public R<JoinedCountResponse> getJoinedCount(HttpServletRequest request) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        int teamCount = (int) teamUserRelationService.count(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TeamUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()));
        int groupCount = (int) taskGroupUserRelationService.count(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TaskGroupUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TaskGroupUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()));
        return R.success(new JoinedCountResponse(teamCount, groupCount), "查询成功");
    }

    @GetMapping("/me/overview/in_progress")
    /**
     * 个人概览统计（进行中）
     *
     * 接口：GET `/api/stats/me/overview/in_progress`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计与当前用户有关的主任务中状态为“进行中”的数量
     * - 统计当前用户作为子任务执行者且状态为“进行中”的数量
     *
     * 返回：`R<InProgressCountResponse>`
     * - `tasks`：进行中主任务数量
     * - `childTasks`：进行中子任务数量
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<InProgressCountResponse> getInProgressOverview(HttpServletRequest request) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        List<TaskUserRelation> myRels = taskUserRelationService.list(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getUserId, cu.getUserId()));
        Set<String> taskIds = new HashSet<>();
        for (TaskUserRelation r : myRels) {
            if (r != null && r.getTaskId() != null) taskIds.add(r.getTaskId());
        }
        int tasksInProgress = 0;
        if (!taskIds.isEmpty()) {
            tasksInProgress = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .in(Task::getTaskId, taskIds)
                    .eq(Task::getTaskStatus, TaskStatusEnum.IN_PROGRESS.getCode()));
        }
        int childInProgress = (int) childTaskService.count(new LambdaQueryWrapper<ChildTask>()
                .eq(ChildTask::getChildTaskAssigneeId, cu.getUserId())
                .eq(ChildTask::getChildTaskStatus, TaskStatusEnum.IN_PROGRESS.getCode()));
        return R.success(new InProgressCountResponse(tasksInProgress, childInProgress), "查询成功");
    }

    @GetMapping("/me/overview/completed/week")
    /**
     * 个人概览统计（本周完成）
     *
     * 接口：GET `/api/stats/me/overview/completed/week`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计当前周内（周一至周日）用户相关任务的完成数量（按`finishTime`）
     *
     * 返回：`R<CompletedCountResponse>`
     * - `count`：完成数量
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<CompletedCountResponse> getCompletedWeek(HttpServletRequest request) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue());
        LocalDate weekEnd = weekStart.plusDays(6);
        int count = completedCountBetween(cu.getUserId(), weekStart, weekEnd);
        return R.success(new CompletedCountResponse(count), "查询成功");
    }

    @GetMapping("/me/overview/completed/month")
    /**
     * 个人概览统计（本月完成）
     *
     * 接口：GET `/api/stats/me/overview/completed/month`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计当前月份内用户相关任务的完成数量（按`finishTime`）
     *
     * 返回：`R<CompletedCountResponse>`
     * - `count`：完成数量
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<CompletedCountResponse> getCompletedMonth(HttpServletRequest request) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int count = completedCountBetween(cu.getUserId(), start, end);
        return R.success(new CompletedCountResponse(count), "查询成功");
    }

    @GetMapping("/me/overview/completed/total")
    /**
     * 个人概览统计（累计完成）
     *
     * 接口：GET `/api/stats/me/overview/completed/total`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计历史累计完成的用户相关任务数量（按`taskStatus=COMPLETED`）
     *
     * 返回：`R<CompletedCountResponse>`
     * - `count`：完成数量
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<CompletedCountResponse> getCompletedTotal(HttpServletRequest request) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        List<TaskUserRelation> myRels = taskUserRelationService.list(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getUserId, cu.getUserId()));
        Set<String> taskIds = myRels.stream().map(TaskUserRelation::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        int count = 0;
        if (!taskIds.isEmpty()) {
            count = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .in(Task::getTaskId, taskIds)
                    .eq(Task::getTaskStatus, TaskStatusEnum.COMPLETED.getCode()));
        }
        return R.success(new CompletedCountResponse(count), "查询成功");
    }

    @GetMapping("/me/overview/overdue")
    /**
     * 个人概览统计（逾期总计/中度/严重）
     *
     * 接口：GET `/api/stats/me/overview/overdue`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计用户相关任务中（未开始或进行中）且`dueTime < today`的逾期数量
     * - 分桶：`moderate`（≤3天）、`severe`（>3天）
     *
     * 返回：`R<OverdueCountResponse>`
     * - `total`/`moderate`/`severe`：逾期数量
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<OverdueCountResponse> getOverdueOverview(HttpServletRequest request) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        LocalDate today = LocalDate.now();
        List<TaskUserRelation> myRels = taskUserRelationService.list(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getUserId, cu.getUserId()));
        Set<String> taskIds = myRels.stream().map(TaskUserRelation::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (taskIds.isEmpty()) {
            return R.success(new OverdueCountResponse(0, 0, 0), "查询成功");
        }
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>()
                .in(Task::getTaskId, taskIds)
                .isNotNull(Task::getDueTime)
                .lt(Task::getDueTime, today)
                .in(Task::getTaskStatus, Arrays.asList(TaskStatusEnum.NOT_STARTED.getCode(), TaskStatusEnum.IN_PROGRESS.getCode())));
        int total = 0, moderate = 0, severe = 0;
        for (Task t : tasks) {
            if (t == null || t.getDueTime() == null) continue;
            long days = ChronoUnit.DAYS.between(t.getDueTime(), today);
            if (days >= 1) {
                total++;
                if (days <= 3) moderate++;
                else severe++;
            }
        }
        return R.success(new OverdueCountResponse(total, moderate, severe), "查询成功");
    }

    @GetMapping("/me/trend/load")
    /**
     * 个人负载趋势（未来任务/子任务近 N 天每日"进行中"数量）
     *
     * 接口：GET `/api/stats/me/trend/load?days=14`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 请求参数：
     * - `days`：统计天数（默认14，上限60）
     *
     * 行为：
     * - 每日统计处于“进行中”的主任务数量与子任务数量
     *
     * 返回：`R<LoadTrendResponse>`
     * - `items`：每日数据列表（`date,tasks,childTasks`）
     *
     * 异常/失败：
     * - 401：未认证
     *
     * @param days 统计天数
     */
    public R<LoadTrendResponse> getLoadTrend(HttpServletRequest request,
                                             @RequestParam(value = "days", required = false) Integer days) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        int n = (days == null || days <= 0) ? 14 : Math.min(days, 60);
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(n - 1); // 未来N天的最后一天
        
        // 获取用户相关的任务ID集合（只查询一次）
        Set<String> taskIds = taskIdsOfUser(cu.getUserId());
        
        // 一次性查询所有未来任务（dueTime在统计区间内）且状态为进行中的任务
        // 查询条件：任务属于该用户，截止时间在[today, endDate]范围内，状态为进行中
        List<Task> futureTasksInProgress = taskService.list(new LambdaQueryWrapper<Task>()
                .in(Task::getTaskId, taskIds)
                .eq(Task::getTaskStatus, TaskStatusEnum.IN_PROGRESS.getCode())
                .ge(Task::getDueTime, today)
                .le(Task::getDueTime, endDate));
        
        // 一次性查询所有未来子任务（dueTime在统计区间内）且状态为进行中的子任务
        // 查询条件：分配给该用户，截止时间在[today, endDate]范围内，状态为进行中
        List<ChildTask> futureChildTasksInProgress = childTaskService.list(new LambdaQueryWrapper<ChildTask>()
                .eq(ChildTask::getChildTaskAssigneeId, cu.getUserId())
                .eq(ChildTask::getChildTaskStatus, TaskStatusEnum.IN_PROGRESS.getCode())
                .ge(ChildTask::getDueTime, today)
                .le(ChildTask::getDueTime, endDate));
        
        // 按日期统计任务数量：使用Map存储每一天的任务和子任务数量
        Map<String, Integer> tasksByDate = new HashMap<>();
        Map<String, Integer> childTasksByDate = new HashMap<>();
        
        // 初始化未来N天所有日期的计数为0
        for (int i = 0; i < n; i++) {
            LocalDate d = today.plusDays(i);
            String dateStr = d.toString();
            tasksByDate.put(dateStr, 0);
            childTasksByDate.put(dateStr, 0);
        }
        
        // 统计任务：对于每个任务，统计它在未来N天哪些日期处于"进行中"状态
        // 如果任务的dueTime在未来N天内，则从今天到dueTime（或endDate，取较小值）都处于"进行中"状态
        for (Task task : futureTasksInProgress) {
            if (task.getDueTime() != null && !task.getDueTime().isBefore(today)) {
                // 任务从今天开始，到dueTime或endDate（取较小值）都处于"进行中"状态
                LocalDate taskEndDate = task.getDueTime().isAfter(endDate) ? endDate : task.getDueTime();
                for (LocalDate d = today; !d.isAfter(taskEndDate); d = d.plusDays(1)) {
                    String dateStr = d.toString();
                    tasksByDate.put(dateStr, tasksByDate.getOrDefault(dateStr, 0) + 1);
                }
            }
        }
        
        // 统计子任务：对于每个子任务，统计它在未来N天哪些日期处于"进行中"状态
        for (ChildTask childTask : futureChildTasksInProgress) {
            if (childTask.getDueTime() != null && !childTask.getDueTime().isBefore(today)) {
                // 子任务从今天开始，到dueTime或endDate（取较小值）都处于"进行中"状态
                LocalDate taskEndDate = childTask.getDueTime().isAfter(endDate) ? endDate : childTask.getDueTime();
                for (LocalDate d = today; !d.isAfter(taskEndDate); d = d.plusDays(1)) {
                    String dateStr = d.toString();
                    childTasksByDate.put(dateStr, childTasksByDate.getOrDefault(dateStr, 0) + 1);
                }
            }
        }
        
        // 构建返回结果（按时间顺序：今天、明天、后天...）
        List<LoadTrendResponse.DayLoad> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LocalDate d = today.plusDays(i);
            String dateStr = d.toString();
            int tasksCount = tasksByDate.getOrDefault(dateStr, 0);
            int childTasksCount = childTasksByDate.getOrDefault(dateStr, 0);
            items.add(new LoadTrendResponse.DayLoad(dateStr, tasksCount, childTasksCount));
        }
        
        return R.success(new LoadTrendResponse(items), "查询成功");
    }

    @GetMapping("/me/trend/tasks")
    /**
     * 个人任务趋势（创建/完成）
     *
     * 接口：GET `/api/stats/me/trend/tasks?range=week|month&from=YYYY-MM-DD&to=YYYY-MM-DD`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 请求参数：
     * - `range`：预设范围（`week`=近7天，`month`=近30天）；如指定`from/to`则忽略
     * - `from,to`：自定义日期区间（含端点）
     *
     * 行为：
     * - 按日统计用户相关任务的创建数与完成数
     *
     * 返回：`R<TaskTrendResponse>`
     * - `items`：每日`created/completed`数据
     *
     * 异常/失败：
     * - 401：未认证
     *
     * @param range 预设范围
     * @param from 起始日期
     * @param to 结束日期
     */
    public R<TaskTrendResponse> getTaskTrend(HttpServletRequest request,
                                             @RequestParam(value = "range", required = false) String range,
                                             @RequestParam(value = "from", required = false) String from,
                                             @RequestParam(value = "to", required = false) String to) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        LocalDate start;
        LocalDate end;
        if (from != null && to != null) {
            start = LocalDate.parse(from.trim());
            end = LocalDate.parse(to.trim());
        } else if ("month".equalsIgnoreCase(range)) {
            LocalDate today = LocalDate.now();
            start = today.minusDays(29);
            end = today;
        } else {
            LocalDate today = LocalDate.now();
            start = today.minusDays(6);
            end = today;
        }
        Set<String> taskIds = taskIdsOfUser(cu.getUserId());
        List<TaskTrendResponse.DayTrend> items = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int created = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .in(Task::getTaskId, taskIds)
                    .between(Task::getCreateTime, d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
            int completed = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .in(Task::getTaskId, taskIds)
                    .eq(Task::getTaskStatus, TaskStatusEnum.COMPLETED.getCode())
                    .between(Task::getFinishTime, d, d));
            items.add(new TaskTrendResponse.DayTrend(d.toString(), created, completed));
        }
        return R.success(new TaskTrendResponse(items), "查询成功");
    }

    @GetMapping("/me/trend/completion")
    /**
     * 完成率趋势
     *
     * 接口：GET `/api/stats/me/trend/completion?range=week|month&from=YYYY-MM-DD&to=YYYY-MM-DD`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 请求参数：同任务趋势
     *
     * 行为：
     * - 每日计算完成率 `completed/created`（无创建则为0）
     *
     * 返回：`R<CompletionTrendResponse>`
     * - `items`：每日`rate/created/completed`
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<CompletionTrendResponse> getCompletionTrend(HttpServletRequest request,
                                                         @RequestParam(value = "range", required = false) String range,
                                                         @RequestParam(value = "from", required = false) String from,
                                                         @RequestParam(value = "to", required = false) String to) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        LocalDate start;
        LocalDate end;
        if (from != null && to != null) {
            start = LocalDate.parse(from.trim());
            end = LocalDate.parse(to.trim());
        } else if ("month".equalsIgnoreCase(range)) {
            LocalDate today = LocalDate.now();
            start = today.minusDays(29);
            end = today;
        } else {
            LocalDate today = LocalDate.now();
            start = today.minusDays(6);
            end = today;
        }
        Set<String> taskIds = taskIdsOfUser(cu.getUserId());
        List<CompletionTrendResponse.DayCompletion> items = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int created = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .in(Task::getTaskId, taskIds)
                    .between(Task::getCreateTime, d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
            int completed = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .in(Task::getTaskId, taskIds)
                    .eq(Task::getTaskStatus, TaskStatusEnum.COMPLETED.getCode())
                    .between(Task::getFinishTime, d, d));
            double rate = created <= 0 ? 0.0 : (double) completed / (double) created;
            items.add(new CompletionTrendResponse.DayCompletion(d.toString(), rate, created, completed));
        }
        return R.success(new CompletionTrendResponse(items), "查询成功");
    }

    @GetMapping("/me/trend/activity")
    /**
     * 活跃度趋势（审计）
     *
     * 接口：GET `/api/stats/me/trend/activity?range=week|month&from=YYYY-MM-DD&to=YYYY-MM-DD`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 基于`task_audit`统计每日操作事件数量
     *
     * 返回：`R<ActivityTrendResponse>`
     * - `items`：每日`date,count`
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<ActivityTrendResponse> getActivityTrend(HttpServletRequest request,
                                                     @RequestParam(value = "range", required = false) String range,
                                                     @RequestParam(value = "from", required = false) String from,
                                                     @RequestParam(value = "to", required = false) String to) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        LocalDate start;
        LocalDate end;
        if (from != null && to != null) {
            start = LocalDate.parse(from.trim());
            end = LocalDate.parse(to.trim());
        } else if ("month".equalsIgnoreCase(range)) {
            LocalDate today = LocalDate.now();
            start = today.minusDays(29);
            end = today;
        } else {
            LocalDate today = LocalDate.now();
            start = today.minusDays(6);
            end = today;
        }
        List<ActivityTrendResponse.DayActivity> items = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int count = (int) taskAuditService.count(new LambdaQueryWrapper<TaskAudit>()
                    .eq(TaskAudit::getOperatorId, cu.getUserId())
                    .between(TaskAudit::getCreateTime, d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
            items.add(new ActivityTrendResponse.DayActivity(d.toString(), count));
        }
        return R.success(new ActivityTrendResponse(items), "查询成功");
    }

    @GetMapping("/me/distribution/priority")
    /**
     * 优先级分布（任务）
     *
     * 接口：GET `/api/stats/me/distribution/priority?scope=tasks|subs`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计用户相关主任务的优先级占比（子任务不含优先级，返回空列表）
     *
     * 返回：`R<PriorityDistributionResponse>`
     * - `items`：各优先级桶（`priority,name,count`）
     *
     * 异常/失败：
     * - 401：未认证
     *
     * @param scope 范围（任务|子任务）
     */
    public R<PriorityDistributionResponse> getPriorityDistribution(HttpServletRequest request,
                                                                   @RequestParam(value = "scope", required = false) String scope) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        boolean isSubs = "subs".equalsIgnoreCase(scope);
        if (isSubs) {
            return R.success(new PriorityDistributionResponse(Collections.emptyList()), "子任务不支持优先级");
        } else {
            Set<String> taskIds = taskIdsOfUser(cu.getUserId());
            List<Task> tasks = taskIds.isEmpty() ? Collections.emptyList() :
                    taskService.list(new LambdaQueryWrapper<Task>().in(Task::getTaskId, taskIds));
            Map<Integer, Integer> counter = new HashMap<>();
            for (Task t : tasks) {
                int p = t == null || t.getTaskPriority() == null ? 0 : t.getTaskPriority();
                counter.put(p, counter.getOrDefault(p, 0) + 1);
            }
            List<PriorityDistributionResponse.Bucket> items = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : counter.entrySet()) {
                String name = null;
                for (TaskPriorityEnum en : TaskPriorityEnum.values()) {
                    if (en.getCode().equals(e.getKey())) { name = en.getDesc(); break; }
                }
                items.add(new PriorityDistributionResponse.Bucket(e.getKey(), name, e.getValue()));
            }
            return R.success(new PriorityDistributionResponse(items), "查询成功");
        }
    }

    @GetMapping("/me/distribution/status")
    /**
     * 状态分布（任务/子任务）
     *
     * 接口：GET `/api/stats/me/distribution/status?scope=tasks|subs`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - `tasks`：用户相关主任务的状态占比
     * - `subs`：当前用户为执行者的子任务状态占比
     *
     * 返回：`R<StatusDistributionResponse>`
     * - `items`：各状态桶（`status,name,count`）
     *
     * 异常/失败：
     * - 401：未认证
     *
     * @param scope 范围（任务|子任务）
     */
    public R<StatusDistributionResponse> getStatusDistribution(HttpServletRequest request,
                                                               @RequestParam(value = "scope", required = false) String scope) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        boolean isSubs = "subs".equalsIgnoreCase(scope);
        List<StatusDistributionResponse.Bucket> items = new ArrayList<>();
        if (isSubs) {
            Map<Integer, Integer> counter = new HashMap<>();
            List<ChildTask> subs = childTaskService.list(new LambdaQueryWrapper<ChildTask>()
                    .eq(ChildTask::getChildTaskAssigneeId, cu.getUserId()));
            for (ChildTask ct : subs) {
                int s = ct == null || ct.getChildTaskStatus() == null ? 0 : ct.getChildTaskStatus();
                counter.put(s, counter.getOrDefault(s, 0) + 1);
            }
            for (Map.Entry<Integer, Integer> e : counter.entrySet()) {
                String name = null;
                for (TaskStatusEnum en : TaskStatusEnum.values()) {
                    if (en.getCode().equals(e.getKey())) { name = en.getDesc(); break; }
                }
                items.add(new StatusDistributionResponse.Bucket(e.getKey(), name, e.getValue()));
            }
        } else {
            Set<String> taskIds = taskIdsOfUser(cu.getUserId());
            List<Task> tasks = taskIds.isEmpty() ? Collections.emptyList() :
                    taskService.list(new LambdaQueryWrapper<Task>().in(Task::getTaskId, taskIds));
            Map<Integer, Integer> counter = new HashMap<>();
            for (Task t : tasks) {
                int s = t == null || t.getTaskStatus() == null ? 0 : t.getTaskStatus();
                counter.put(s, counter.getOrDefault(s, 0) + 1);
            }
            for (Map.Entry<Integer, Integer> e : counter.entrySet()) {
                String name = null;
                for (TaskStatusEnum en : TaskStatusEnum.values()) {
                    if (en.getCode().equals(e.getKey())) { name = en.getDesc(); break; }
                }
                items.add(new StatusDistributionResponse.Bucket(e.getKey(), name, e.getValue()));
            }
        }
        return R.success(new StatusDistributionResponse(items), "查询成功");
    }

    @GetMapping("/me/expiration/forecast")
    /**
     * 任务DDL分析（到期预报）
     *
     * 接口：GET `/api/stats/me/expiration/forecast?buckets=7d,3d,today&scope=tasks|subs`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 请求参数：
     * - `buckets`：桶定义（如 `7d,3d,today`）
     * - `scope`：任务或子任务
     *
     * 行为：
     * - 按桶统计未来到期数量
     *
     * 返回：`R<ExpirationForecastResponse>`
     * - `items`：各桶（`bucket,count`）
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<ExpirationForecastResponse> getExpirationForecast(HttpServletRequest request,
                                                               @RequestParam(value = "buckets", required = false) String buckets,
                                                               @RequestParam(value = "scope", required = false) String scope) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String s = buckets == null ? "7d,3d,today" : buckets.trim();
        String[] arr = s.split(",");
        LocalDate today = LocalDate.now();
        List<ExpirationForecastResponse.Bucket> items = new ArrayList<>();
        boolean isSubs = "subs".equalsIgnoreCase(scope);
        for (String b : arr) {
            String key = b.trim();
            int count = 0;
            if (isSubs) {
                if ("today".equalsIgnoreCase(key)) {
                    count = (int) childTaskService.count(new LambdaQueryWrapper<ChildTask>()
                            .eq(ChildTask::getChildTaskAssigneeId, cu.getUserId())
                            .eq(ChildTask::getDueTime, today));
                } else if (key.endsWith("d")) {
                    int d = Integer.parseInt(key.substring(0, key.length() - 1));
                    count = (int) childTaskService.count(new LambdaQueryWrapper<ChildTask>()
                            .eq(ChildTask::getChildTaskAssigneeId, cu.getUserId())
                            .between(ChildTask::getDueTime, today, today.plusDays(d)));
                }
            } else {
                Set<String> taskIds = taskIdsOfUser(cu.getUserId());
                if ("today".equalsIgnoreCase(key)) {
                    count = (int) taskService.count(new LambdaQueryWrapper<Task>()
                            .in(Task::getTaskId, taskIds)
                            .eq(Task::getDueTime, today));
                } else if (key.endsWith("d")) {
                    int d = Integer.parseInt(key.substring(0, key.length() - 1));
                    count = (int) taskService.count(new LambdaQueryWrapper<Task>()
                            .in(Task::getTaskId, taskIds)
                            .between(Task::getDueTime, today, today.plusDays(d)));
                }
            }
            items.add(new ExpirationForecastResponse.Bucket(key, count));
        }
        return R.success(new ExpirationForecastResponse(items), "查询成功");
    }

    @GetMapping("/me/overdue")
    /**
     * 逾期统计（区间）
     *
     * 接口：GET `/api/stats/me/overdue?from=YYYY-MM-DD&to=YYYY-MM-DD`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 请求参数：
     * - `from,to`：日期区间（含端点）
     *
     * 行为：
     * - 统计区间内且已逾期（`dueTime < today`）任务的总量与分桶
     *
     * 返回：`R<OverdueCountResponse>`
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<OverdueCountResponse> getOverdueInRange(HttpServletRequest request,
                                                     @RequestParam(value = "from", required = false) String from,
                                                     @RequestParam(value = "to", required = false) String to) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        LocalDate start = from == null ? LocalDate.now().minusDays(6) : LocalDate.parse(from.trim());
        LocalDate end = to == null ? LocalDate.now() : LocalDate.parse(to.trim());
        Set<String> taskIds = taskIdsOfUser(cu.getUserId());
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>()
                .in(Task::getTaskId, taskIds)
                .isNotNull(Task::getDueTime)
                .between(Task::getDueTime, start, end)
                .in(Task::getTaskStatus, Arrays.asList(TaskStatusEnum.NOT_STARTED.getCode(), TaskStatusEnum.IN_PROGRESS.getCode())));
        int total = 0, moderate = 0, severe = 0;
        LocalDate today = LocalDate.now();
        for (Task t : tasks) {
            if (t == null || t.getDueTime() == null) continue;
            if (t.getDueTime().isBefore(today)) {
                long days = ChronoUnit.DAYS.between(t.getDueTime(), today);
                total++;
                if (days <= 3) moderate++; else severe++;
            }
        }
        return R.success(new OverdueCountResponse(total, moderate, severe), "查询成功");
    }

    @GetMapping("/me/mttr")
    /**
     * 平均响应时间（MTTR）
     *
     * 接口：GET `/api/stats/me/mttr?from=YYYY-MM-DD&to=YYYY-MM-DD`
     * 权限：需 JWT 鉴权，且仅本人
     *
     * 行为：
     * - 统计区间内任务`startTime→finishTime`的平均耗时（小时）
     *
     * 返回：`R<MttrResponse>`
     * - `averageHours`：平均小时数
     * - `sampleSize`：样本数量
     *
     * 异常/失败：
     * - 401：未认证
     */
    public R<MttrResponse> getMttr(HttpServletRequest request,
                                   @RequestParam(value = "from", required = false) String from,
                                   @RequestParam(value = "to", required = false) String to) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        LocalDate start = from == null ? LocalDate.now().minusDays(29) : LocalDate.parse(from.trim());
        LocalDate end = to == null ? LocalDate.now() : LocalDate.parse(to.trim());
        Set<String> taskIds = taskIdsOfUser(cu.getUserId());
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>()
                .in(Task::getTaskId, taskIds)
                .isNotNull(Task::getStartTime)
                .isNotNull(Task::getFinishTime)
                .between(Task::getFinishTime, start, end));
        long totalHours = 0;
        int sample = 0;
        for (Task t : tasks) {
            if (t.getStartTime() == null || t.getFinishTime() == null) continue;
            LocalDateTime startDt = t.getStartTime().atStartOfDay();
            LocalDateTime finishDt = t.getFinishTime().atStartOfDay();
            long hours = ChronoUnit.HOURS.between(startDt, finishDt);
            if (hours >= 0) {
                totalHours += hours;
                sample++;
            }
        }
        double avg = sample <= 0 ? 0.0 : (double) totalHours / (double) sample;
        return R.success(new MttrResponse(avg, sample), "查询成功");
    }

    @GetMapping("/teams/{teamId}/overview")
    /**
     * 团队概览统计
     *
     * 接口：GET `/api/stats/teams/{teamId}/overview`
     * 权限：需为团队正常成员（owner/manager/member）
     *
     * 行为：
     * - 汇总团队任务总量、进行中、已完成、逾期数
     * - 返回状态/优先级分布桶
     *
     * 返回：`R<TeamOverviewResponse>`
     *
     * 异常/失败：
     * - 401：未认证
     * - 403：非团队成员
     * - 404：团队不存在（由上游接口保证）
     *
     * @param teamId 团队ID
     */
    public R<TeamOverviewResponse> getTeamOverview(HttpServletRequest request,
                                                   @org.springframework.web.bind.annotation.PathVariable("teamId") String teamId) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少团队ID");
        var rel = teamUserRelationService.getOne(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TeamUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getTeamId, tid)
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该团队统计");
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, tid));
        int total = tasks == null ? 0 : tasks.size();
        int inProgress = 0, completed = 0, overdue = 0;
        LocalDate today = LocalDate.now();
        Map<Integer, Integer> statusCounter = new HashMap<>();
        Map<Integer, Integer> priorityCounter = new HashMap<>();
        if (tasks != null) {
            for (Task t : tasks) {
                if (t == null) continue;
                Integer s = t.getTaskStatus();
                Integer p = t.getTaskPriority();
                if (s != null) {
                    statusCounter.put(s, statusCounter.getOrDefault(s, 0) + 1);
                    if (TaskStatusEnum.IN_PROGRESS.getCode().equals(s)) inProgress++;
                    if (TaskStatusEnum.COMPLETED.getCode().equals(s)) completed++;
                    if ((TaskStatusEnum.NOT_STARTED.getCode().equals(s) || TaskStatusEnum.IN_PROGRESS.getCode().equals(s))
                            && t.getDueTime() != null && t.getDueTime().isBefore(today)) overdue++;
                }
                if (p != null) priorityCounter.put(p, priorityCounter.getOrDefault(p, 0) + 1);
            }
        }
        List<StatusDistributionResponse.Bucket> statusBuckets = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : statusCounter.entrySet()) {
            String name = null;
            for (TaskStatusEnum en : TaskStatusEnum.values()) {
                if (en.getCode().equals(e.getKey())) { name = en.getDesc(); break; }
            }
            statusBuckets.add(new StatusDistributionResponse.Bucket(e.getKey(), name, e.getValue()));
        }
        List<PriorityDistributionResponse.Bucket> priorityBuckets = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : priorityCounter.entrySet()) {
            String name = null;
            for (TaskPriorityEnum en : TaskPriorityEnum.values()) {
                if (en.getCode().equals(e.getKey())) { name = en.getDesc(); break; }
            }
            priorityBuckets.add(new PriorityDistributionResponse.Bucket(e.getKey(), name, e.getValue()));
        }
        return R.success(new TeamOverviewResponse(total, inProgress, completed, overdue, statusBuckets, priorityBuckets), "查询成功");
    }

    @GetMapping("/teams/{teamId}/distribution")
    /**
     * 团队任务分布（状态/优先级）
     *
     * 接口：GET `/api/stats/teams/{teamId}/distribution?by=status|priority`
     * 权限：需为团队正常成员
     *
     * 请求参数：
     * - `by`：分布维度（`status`或`priority`）
     *
     * 返回：
     * - `by=status` → `R<StatusDistributionResponse>`
     * - `by=priority` → `R<PriorityDistributionResponse>`
     *
     * 异常/失败：
     * - 401：未认证
     * - 403：非团队成员
     */
    public R<?> getTeamDistribution(HttpServletRequest request,
                                    @org.springframework.web.bind.annotation.PathVariable("teamId") String teamId,
                                    @RequestParam(value = "by", required = false) String by) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少团队ID");
        var rel = teamUserRelationService.getOne(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TeamUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getTeamId, tid)
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该团队统计");
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, tid));
        if ("priority".equalsIgnoreCase(by)) {
            Map<Integer, Integer> counter = new HashMap<>();
            for (Task t : tasks) {
                Integer p = (t == null) ? null : t.getTaskPriority();
                if (p != null) counter.put(p, counter.getOrDefault(p, 0) + 1);
            }
            List<PriorityDistributionResponse.Bucket> items = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : counter.entrySet()) {
                String name = null;
                for (TaskPriorityEnum en : TaskPriorityEnum.values()) {
                    if (en.getCode().equals(e.getKey())) { name = en.getDesc(); break; }
                }
                items.add(new PriorityDistributionResponse.Bucket(e.getKey(), name, e.getValue()));
            }
            return R.success(new PriorityDistributionResponse(items), "查询成功");
        } else {
            Map<Integer, Integer> counter = new HashMap<>();
            for (Task t : tasks) {
                Integer s = (t == null) ? null : t.getTaskStatus();
                if (s != null) counter.put(s, counter.getOrDefault(s, 0) + 1);
            }
            List<StatusDistributionResponse.Bucket> items = new ArrayList<>();
            for (Map.Entry<Integer, Integer> e : counter.entrySet()) {
                String name = null;
                for (TaskStatusEnum en : TaskStatusEnum.values()) {
                    if (en.getCode().equals(e.getKey())) { name = en.getDesc(); break; }
                }
                items.add(new StatusDistributionResponse.Bucket(e.getKey(), name, e.getValue()));
            }
            return R.success(new StatusDistributionResponse(items), "查询成功");
        }
    }

    @GetMapping("/teams/{teamId}/workload")
    /**
     * 团队成员负载统计
     *
     * 接口：GET `/api/stats/teams/{teamId}/workload?scope=in_progress|all`
     * 权限：需为团队正常成员
     *
     * 请求参数：
     * - `scope`：统计范围（进行中|全部）
     *
     * 返回：`R<TeamWorkloadResponse>`
     * - `items`：成员维度的`inProgress/total`
     *
     * 异常/失败：
     * - 401：未认证
     * - 403：非团队成员
     *
     * @param teamId 团队ID
     * @param scope 统计范围
     */
    public R<TeamWorkloadResponse> getTeamWorkload(HttpServletRequest request,
                                                   @org.springframework.web.bind.annotation.PathVariable("teamId") String teamId,
                                                   @RequestParam(value = "scope", required = false) String scope) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少团队ID");
        var rel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, tid)
                .eq(TeamUserRelation::getUserId, cu.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该团队统计");
        List<TeamUserRelation> members = teamUserRelationService.list(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, tid)
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()));
        Set<String> memberIds = new HashSet<>();
        for (var r : members) if (r.getUserId() != null) memberIds.add(r.getUserId());
        List<TeamWorkloadResponse.MemberWorkload> items = new ArrayList<>();
        for (String uid : memberIds) {
            int inProgress = 0;
            int total = 0;
            Set<String> taskIds = taskIdsOfUser(uid);
            if (!taskIds.isEmpty()) {
                inProgress = (int) taskService.count(new LambdaQueryWrapper<Task>()
                        .eq(Task::getTeamId, tid)
                        .eq(Task::getTaskStatus, TaskStatusEnum.IN_PROGRESS.getCode())
                        .in(Task::getTaskId, taskIds));
                total = (int) taskService.count(new LambdaQueryWrapper<Task>()
                        .eq(Task::getTeamId, tid)
                        .in(Task::getTaskId, taskIds));
            }
            var u = userService.getById(uid);
            items.add(new TeamWorkloadResponse.MemberWorkload(uid, u == null ? null : u.getUserName(),
                    "in_progress".equalsIgnoreCase(scope) ? inProgress : inProgress, "in_progress".equalsIgnoreCase(scope) ? 0 : total));
        }
        return R.success(new TeamWorkloadResponse(items), "查询成功");
    }

    @GetMapping("/teams/{teamId}/burndown")
    /**
     * 团队燃尽图
     *
     * 接口：GET `/api/stats/teams/{teamId}/burndown?from=YYYY-MM-DD&to=YYYY-MM-DD`
     * 权限：需为团队正常成员
     *
     * 请求参数：
     * - `from,to`：日期区间（含端点）
     *
     * 行为：
     * - 按日返回剩余与新增完成数量
     *
     * 返回：`R<BurndownResponse>`
     *
     * 异常/失败：
     * - 401：未认证
     * - 403：非团队成员
     */
    public R<BurndownResponse> getTeamBurndown(HttpServletRequest request,
                                               @org.springframework.web.bind.annotation.PathVariable("teamId") String teamId,
                                               @RequestParam(value = "from", required = false) String from,
                                               @RequestParam(value = "to", required = false) String to) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少团队ID");
        var rel = teamUserRelationService.getOne(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TeamUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getTeamId, tid)
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该团队统计");
        LocalDate start = from == null ? LocalDate.now().minusDays(6) : LocalDate.parse(from.trim());
        LocalDate end = to == null ? LocalDate.now() : LocalDate.parse(to.trim());
        List<BurndownResponse.DayBurn> items = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int completed = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .eq(Task::getTeamId, tid)
                    .eq(Task::getTaskStatus, TaskStatusEnum.COMPLETED.getCode())
                    .eq(Task::getFinishTime, d));
            int createdBefore = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .eq(Task::getTeamId, tid)
                    .le(Task::getCreateTime, d.atStartOfDay()));
            int completedBefore = (int) taskService.count(new LambdaQueryWrapper<Task>()
                    .eq(Task::getTeamId, tid)
                    .eq(Task::getTaskStatus, TaskStatusEnum.COMPLETED.getCode())
                    .le(Task::getFinishTime, d));
            int remaining = Math.max(0, createdBefore - completedBefore);
            items.add(new BurndownResponse.DayBurn(d.toString(), remaining, completed));
        }
        return R.success(new BurndownResponse(items), "查询成功");
    }

    @GetMapping("/teams/{teamId}/overdue")
    /**
     * 团队逾期统计（总量、按成员、按任务族）
     *
     * 接口：GET `/api/stats/teams/{teamId}/overdue?from=YYYY-MM-DD&to=YYYY-MM-DD`
     * 权限：需为团队正常成员
     *
     * 行为：
     * - 统计区间内逾期任务总量
     * - 聚合成员维度与任务族维度的逾期分布
     *
     * 返回：`R<TeamOverdueResponse>`
     *
     * 异常/失败：
     * - 401：未认证
     * - 403：非团队成员
     */
    public R<TeamOverdueResponse> getTeamOverdue(HttpServletRequest request,
                                                 @org.springframework.web.bind.annotation.PathVariable("teamId") String teamId,
                                                 @RequestParam(value = "from", required = false) String from,
                                                 @RequestParam(value = "to", required = false) String to) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少团队ID");
        var rel = teamUserRelationService.getOne(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TeamUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getTeamId, tid)
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该团队统计");
        LocalDate start = from == null ? LocalDate.now().minusDays(29) : LocalDate.parse(from.trim());
        LocalDate end = to == null ? LocalDate.now() : LocalDate.parse(to.trim());
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>()
                .eq(Task::getTeamId, tid)
                .isNotNull(Task::getDueTime)
                .between(Task::getDueTime, start, end)
                .in(Task::getTaskStatus, Arrays.asList(TaskStatusEnum.NOT_STARTED.getCode(), TaskStatusEnum.IN_PROGRESS.getCode())));
        LocalDate today = LocalDate.now();
        int total = 0;
        Map<String, Integer> byMember = new HashMap<>();
        Map<String, Integer> byGroup = new HashMap<>();
        for (Task t : tasks) {
            if (t == null || t.getDueTime() == null) continue;
            if (t.getDueTime().isBefore(today)) {
                total++;
                List<TaskUserRelation> rels = taskUserRelationService.list(new LambdaQueryWrapper<TaskUserRelation>()
                        .eq(TaskUserRelation::getTaskId, t.getTaskId()));
                for (TaskUserRelation r : rels) {
                    if (r != null && r.getUserId() != null) {
                        byMember.put(r.getUserId(), byMember.getOrDefault(r.getUserId(), 0) + 1);
                    }
                }
                String gid = t.getTaskGroupId();
                if (gid != null) byGroup.put(gid, byGroup.getOrDefault(gid, 0) + 1);
            }
        }
        List<TeamOverdueResponse.MemberBucket> memberBuckets = new ArrayList<>();
        for (Map.Entry<String, Integer> e : byMember.entrySet()) {
            var u = userService.getById(e.getKey());
            memberBuckets.add(new TeamOverdueResponse.MemberBucket(e.getKey(), u == null ? null : u.getUserName(), e.getValue()));
        }
        List<TeamOverdueResponse.TaskGroupBucket> groupBuckets = new ArrayList<>();
        for (Map.Entry<String, Integer> e : byGroup.entrySet()) {
            var g = taskGroupService.getById(e.getKey());
            groupBuckets.add(new TeamOverdueResponse.TaskGroupBucket(e.getKey(), g == null ? null : g.getGroupName(), e.getValue()));
        }
        return R.success(new TeamOverdueResponse(total, memberBuckets, groupBuckets), "查询成功");
    }

    @GetMapping("/teams/{teamId}/trend/activity")
    /**
     * 团队活跃度趋势（审计）
     *
     * 接口：GET `/api/stats/teams/{teamId}/trend/activity?range=week|month`
     * 权限：需为团队正常成员
     *
     * 行为：
     * - 基于`task_audit`统计团队每日操作事件数量
     *
     * 返回：`R<ActivityTrendResponse>`
     *
     * 异常/失败：
     * - 401：未认证
     * - 403：非团队成员
     *
     * @param teamId 团队ID
     * @param range 预设范围
     */
    public R<ActivityTrendResponse> getTeamActivityTrend(HttpServletRequest request,
                                                         @org.springframework.web.bind.annotation.PathVariable("teamId") String teamId,
                                                         @RequestParam(value = "range", required = false) String range) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少团队ID");
        var rel = teamUserRelationService.getOne(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TeamUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getTeamId, tid)
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该团队统计");
        LocalDate today = LocalDate.now();
        LocalDate start;
        LocalDate end;
        if ("month".equalsIgnoreCase(range)) {
            start = today.minusDays(29);
            end = today;
        } else {
            start = today.minusDays(6);
            end = today;
        }
        Set<String> teamTaskIds = taskService.list(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, tid))
                .stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<ActivityTrendResponse.DayActivity> items = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            int count = (int) taskAuditService.count(new LambdaQueryWrapper<TaskAudit>()
                    .in(TaskAudit::getTaskId, teamTaskIds)
                    .between(TaskAudit::getCreateTime, d.atStartOfDay(), d.plusDays(1).atStartOfDay()));
            items.add(new ActivityTrendResponse.DayActivity(d.toString(), count));
        }
        return R.success(new ActivityTrendResponse(items), "查询成功");
    }

    @GetMapping("/teams/{teamId}/graph/summary")
    /**
     * 团队图谱摘要
     *
     * 接口：GET `/api/stats/teams/{teamId}/graph/summary`
     * 权限：需为团队正常成员
     *
     * 行为：
     * - 统计团队任务图谱的节点/边数量与类型分布
     * - 边集合通过团队节点集合的`source/target`归属推断
     *
     * 返回：`R<GraphSummaryResponse>`
     *
     * 异常/失败：
     * - 401：未认证
     * - 403：非团队成员
     *
     * @param teamId 团队ID
     */
    public R<GraphSummaryResponse> getTeamGraphSummary(HttpServletRequest request,
                                                       @org.springframework.web.bind.annotation.PathVariable("teamId") String teamId) {
        var cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少团队ID");
        var rel = teamUserRelationService.getOne(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TeamUserRelation>()
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getTeamId, tid)
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserId, cu.getUserId())
                .eq(top.smartduck.ducktodo.model.entity.TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该团队统计");
        Map<String, Integer> nodeTypes = new HashMap<>();
        Map<String, Integer> edgeTypes = new HashMap<>();
        List<top.smartduck.ducktodo.model.entity.TaskNode> nodes = taskNodeService.list(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TaskNode>().eq(top.smartduck.ducktodo.model.entity.TaskNode::getTeamId, tid));
        for (var n : nodes) {
            String t = n.getNodeType();
            if (t != null) nodeTypes.put(t, nodeTypes.getOrDefault(t, 0) + 1);
        }
        int nodeCount = nodes == null ? 0 : nodes.size();
        Set<String> nodeIds = nodes == null ? Collections.emptySet() : nodes.stream().map(top.smartduck.ducktodo.model.entity.TaskNode::getTaskNodeId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<top.smartduck.ducktodo.model.entity.TaskEdge> edges = nodeIds.isEmpty() ? Collections.emptyList() :
                taskEdgeService.list(new LambdaQueryWrapper<top.smartduck.ducktodo.model.entity.TaskEdge>()
                        .in(top.smartduck.ducktodo.model.entity.TaskEdge::getSourceNodeId, nodeIds)
                        .or().in(top.smartduck.ducktodo.model.entity.TaskEdge::getTargetNodeId, nodeIds));
        int edgeCount = edges == null ? 0 : edges.size();
        for (var e : edges) {
            String t = e.getEdgeType();
            if (t != null) edgeTypes.put(t, edgeTypes.getOrDefault(t, 0) + 1);
        }
        List<GraphSummaryResponse.NodeTypeBucket> nodeBuckets = new ArrayList<>();
        for (Map.Entry<String, Integer> e : nodeTypes.entrySet()) {
            nodeBuckets.add(new GraphSummaryResponse.NodeTypeBucket(e.getKey(), e.getValue()));
        }
        List<GraphSummaryResponse.EdgeTypeBucket> edgeBuckets = new ArrayList<>();
        for (Map.Entry<String, Integer> e : edgeTypes.entrySet()) {
            edgeBuckets.add(new GraphSummaryResponse.EdgeTypeBucket(e.getKey(), e.getValue()));
        }
        return R.success(new GraphSummaryResponse(nodeCount, edgeCount, nodeBuckets, edgeBuckets), "查询成功");
    }

    private int completedCountBetween(String userId, LocalDate start, LocalDate end) {
        List<TaskUserRelation> myRels = taskUserRelationService.list(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getUserId, userId));
        Set<String> taskIds = myRels.stream().map(TaskUserRelation::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (taskIds.isEmpty()) return 0;
        return (int) taskService.count(new LambdaQueryWrapper<Task>()
                .in(Task::getTaskId, taskIds)
                .eq(Task::getTaskStatus, TaskStatusEnum.COMPLETED.getCode())
                .between(Task::getFinishTime, start, end));
    }

    private Set<String> taskIdsOfUser(String userId) {
        List<TaskUserRelation> myRels = taskUserRelationService.list(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getUserId, userId));
        Set<String> taskIds = new HashSet<>();
        for (TaskUserRelation r : myRels) {
            if (r != null && r.getTaskId() != null) taskIds.add(r.getTaskId());
        }
        return taskIds;
    }
}
