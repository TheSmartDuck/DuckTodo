package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.smartduck.ducktodo.aspect.TaskAuditLog;
import top.smartduck.ducktodo.common.enums.*;
import top.smartduck.ducktodo.common.exception.BusinessException;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.model.entity.*;
import top.smartduck.ducktodo.modelService.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;
import top.smartduck.ducktodo.util.MinioUtil;
import top.smartduck.ducktodo.util.CommonUtil;
import top.smartduck.ducktodo.model.request.CreateTaskRequest;
import top.smartduck.ducktodo.model.response.TaskDetailResponse;
import top.smartduck.ducktodo.model.response.TaskSummaryResponse;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 任务相关接口（需 JWT 鉴权）。
 * 提供：
 * - 列出指定任务族所有任务（按组的 teamId 回退）
 * - 列出与自己相关的任务族
 * - 创建任务（并建立拥有者 TaskHelper）
 * - 指派任务 / 删除指派任务
 * - 接受 / 拒绝被指派的任务
 * - 修改任务
 * - 添加子任务
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskUserRelationService taskUserRelationService;

    @Autowired
    private ChildTaskService childTaskService;

    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private TaskGroupUserRelationService taskGroupUserRelationService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskFileService taskFileService;

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TaskNodeService taskNodeService;

    @Autowired
    private TaskEdgeService taskEdgeService;

    @Autowired
    private TaskAuditService taskAuditService;

    // ================= 创建任务（并建立拥有者 TaskHelper） =================
    /**
     * 创建任务（并建立拥有者与协助者、子任务）
     *
     * 接口：POST `/api/tasks`
     * 权限：需登录；私人任务族不可添加协助者
     *
     * 入参：CreateTaskRequest
     * - taskGroupId：所属任务族ID（必填）
     * - taskName：任务名称（≥2）
     * - taskDescription：任务描述（可选）
     * - taskStatus：任务状态（默认 1 未开始）
     * - taskPriority：任务优先级（默认 3）
     * - startTime：开始时间（默认今天）
     * - dueTime：截止时间（需 ≥ 今天，且 > 开始时间）
     * - helperUserIdList：协助者ID列表（仅团队任务允许）
     * - childTaskList：子任务列表（执行者需在协助者或为拥有者；子任务截止时间 ≤ 主任务）
     *
     * 行为：
     * - 创建 Task 记录
     * - 建立当前用户为拥有者的 TaskUserRelation
     * - 批量建立协助者关系（若提供）
     * - 批量创建子任务（若提供）
     *
     * 返回：
     * - R<TaskDetailResponse>：包含任务详情、任务族名称、团队名称、协助者融合列表、子任务列表、附件空列表
     *
     * 审计：
     * - 记录 @TaskAuditLog(action = CREATE)
     *
     * 异常/失败：
     * - 400：参数错误；401：未认证；403：无权限；404：资源不存在；500：内部错误
     *
     * @param request 当前请求（用于解析登录用户）
     * @param createTaskRequest 创建任务请求体
     * @return R<TaskDetailResponse>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.CREATE, description = "创建任务")
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<TaskDetailResponse> createTask(HttpServletRequest request, @RequestBody(required = false) CreateTaskRequest createTaskRequest) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        // 1. 读取并校验请求参数（名称/描述/状态/优先级/时间/团队/任务族）
        TaskGroup taskGroup = taskGroupService.getById(createTaskRequest.getTaskGroupId());
        if (taskGroup == null) {
            return R.fail("任务族不存在");
        }
        String taskGroupId = CommonUtil.trim(createTaskRequest.getTaskGroupId());
        String teamId = CommonUtil.trim(taskGroup.getTeamId());
        String taskName = CommonUtil.trim(createTaskRequest.getTaskName());
        String taskDescription = CommonUtil.trim(createTaskRequest.getTaskDescription());
        Integer taskStatus = createTaskRequest.getTaskStatus(); // 默认 1 未开始
        Integer taskPriority = createTaskRequest.getTaskPriority();
        LocalDate startTime = createTaskRequest.getStartTime();
        LocalDate dueTime = createTaskRequest.getDueTime();

        // 参数核验
        if (taskName == null || taskName.length() < 2) {
            return R.fail("任务名称至少 2 个字符");
        }
        if (taskStatus == null || !CommonUtil.inEnumCodes(TaskStatusEnum.values(), taskStatus)) {
            taskStatus = 1;
        }
        if (taskPriority == null || !CommonUtil.inEnumCodes(TaskPriorityEnum.values(), taskPriority)) {
            taskPriority = 3;
        }
        if (startTime == null) {
            startTime = LocalDate.now();
        }
        if (dueTime == null || dueTime.isBefore(LocalDate.now())) {
            return R.fail("截止时间不能为空且截止时间不能小于当日");
        }
        if (startTime.isAfter(dueTime)){
            return R.fail("开始时间必须小于截止时间");
        }

        // 3. 协助者校验
        if(taskGroup.getTeamId().isEmpty() && !createTaskRequest.getHelperUserIdList().isEmpty()){
            if(createTaskRequest.getHelperUserIdList().size() > 1 || !currentUser.getUserId().equals(createTaskRequest.getHelperUserIdList().getFirst())){
                return R.fail("私人任务族无法添加协助者");
            }
        }else if(createTaskRequest.getHelperUserIdList().size() != taskGroupUserRelationService.count(new LambdaQueryWrapper<TaskGroupUserRelation>().eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId).in(TaskGroupUserRelation::getUserId, createTaskRequest.getHelperUserIdList()))){
            return R.fail("添加的协助者中存在非本团队的协助者");
        }


        // 2. 构建存储Task
        Task task = new Task();
        task.setTaskGroupId(taskGroupId);
        task.setTeamId(teamId);
        task.setTaskName(taskName);
        task.setTaskDescription(taskDescription);
        task.setTaskStatus(taskStatus);
        task.setTaskPriority(taskPriority);
        task.setStartTime(startTime);
        task.setDueTime(dueTime);
        // 如果状态为已完成（3）且 finish_time 为空或 null，则设置为当前日期
        // 如果状态为非已完成（不是3），则确保 finish_time 为 null
        if (taskStatus != null && taskStatus.equals(TaskStatusEnum.COMPLETED.getCode())) {
            task.setFinishTime(LocalDate.now());
        } else {
        task.setFinishTime(null);
        }
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());


        if(!taskService.save(task)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建主任务记录失败");
        }

        // 3.构建任务与拥有者关系
        TaskUserRelation ownerRelation = new TaskUserRelation();
        ownerRelation.setTaskId(task.getTaskId());
        ownerRelation.setUserId(currentUser.getUserId());
        ownerRelation.setIfOwner(IfOwnerEnum.IS_OWNER.getCode());
        ownerRelation.setCreateTime(LocalDateTime.now());
        ownerRelation.setUpdateTime(LocalDateTime.now());

        if(!taskUserRelationService.save(ownerRelation)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "添加任务与拥有者关系失败");
        }
        List<TaskUserRelation> helperRelations = new ArrayList<>();
        helperRelations.add(ownerRelation);

        // 4. 构建任务与协助者关系
        for(String userId : createTaskRequest.getHelperUserIdList()){
            if(userId.equals(currentUser.getUserId())){
                continue;
            }
            TaskUserRelation userRelation = new TaskUserRelation();
            userRelation.setTaskId(task.getTaskId());
            userRelation.setUserId(userId);
            userRelation.setIfOwner(IfOwnerEnum.IS_NOT_OWNER.getCode());
            userRelation.setCreateTime(LocalDateTime.now());
            userRelation.setUpdateTime(LocalDateTime.now());

            if(!taskUserRelationService.save(userRelation)){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "添加任务与协助者关系失败");
            }
            helperRelations.add(userRelation);
        }

        // 5. 构建子任务
        List<ChildTask> childTasks = new ArrayList<>();
        int childTaskIndex = 1;
        for(CreateTaskRequest.ChildTaskCreate childTaskCreate: createTaskRequest.getChildTaskList()){
            String childTaskName = CommonUtil.trim(childTaskCreate.getChildTaskName());
            Integer childTaskStatus = childTaskCreate.getChildTaskStatus();
            LocalDate childTaskDucTime = childTaskCreate.getDueTime();
            String childTaskAssigneeUserId = CommonUtil.trim(childTaskCreate.getAssigneeUserId());

            // 子任务数据核验
            if (childTaskName == null || childTaskName.length() < 2) {
                return R.fail("子任务: " + childTaskName + " 名称至少 2 个字符");
            }
            if (!CommonUtil.inEnumCodes(TaskStatusEnum.values(), childTaskStatus)){
                return R.fail("子任务: " + childTaskName + " 任务状态未在合法范围内");
            }
            if (childTaskDucTime.isAfter(task.getDueTime())){
                return R.fail("子任务: " + childTaskName + " 的截止日期不能大于主任务的截止日期");
            }
            if (childTaskAssigneeUserId == null){
                return R.fail("子任务: " + childTaskName + " 的执行者不能为空");
            }else if (!createTaskRequest.getHelperUserIdList().contains(childTaskAssigneeUserId) && !childTaskAssigneeUserId.equals(currentUser.getUserId())){
                return R.fail("子任务: " + childTaskName + " 的执行者不在协助者列表中且不为拥有者");
            }

            ChildTask childTask = new ChildTask();
            childTask.setTaskId(task.getTaskId());
            childTask.setChildTaskName(childTaskName);
            childTask.setChildTaskStatus(childTaskStatus);
            childTask.setChildTaskIndex(childTaskIndex);
            childTask.setChildTaskAssigneeId(childTaskAssigneeUserId);
            childTask.setDueTime(childTaskDucTime);
            // 如果状态为已完成（3）且 finish_time 为空或 null，则设置为当前日期
            // 如果状态为非已完成（不是3），则确保 finish_time 为 null
            if (childTaskStatus != null && childTaskStatus.equals(TaskStatusEnum.COMPLETED.getCode())) {
                childTask.setFinishTime(LocalDate.now());
            } else {
            childTask.setFinishTime(null);
            }
            childTask.setCreateTime(LocalDateTime.now());
            childTask.setUpdateTime(LocalDateTime.now());

            if(!childTaskService.save(childTask)){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "添加子任务: " + childTaskName + " 失败");
            }

            childTaskIndex = childTaskIndex + 1;
            childTasks.add(childTask);
        }

        Team team = (teamId == null || teamId.isEmpty()) ? null : teamService.getById(teamId);
        Set<String> uidSet = new HashSet<>();
        for (TaskUserRelation r : helperRelations) {
            if (r.getUserId() != null) uidSet.add(r.getUserId());
        }
        List<User> userList = uidSet.isEmpty() ? Collections.emptyList() : userService.listByIds(uidSet);
        Map<String, User> userMap = new HashMap<>();
        for (User u : userList) {
            if (u.getUserId() != null) userMap.put(u.getUserId(), u);
        }
        List<TaskDetailResponse.TaskHelper> helperList = new ArrayList<>();
        for (TaskUserRelation r : helperRelations) {
            User u = userMap.get(r.getUserId());
            helperList.add(new TaskDetailResponse.TaskHelper(
                    r.getTaskUserRelationId(),
                    r.getUserId(),
                    r.getIfOwner() != null && r.getIfOwner() == 1,
                    u == null ? null : u.getUserName(),
                    u == null ? null : u.getUserEmail(),
                    u == null ? null : u.getUserPhone(),
                    u == null || u.getUserSex() == null ? null : u.getUserSex(),
                    u == null ? null : u.getUserAvatar(),
                    u == null ? null : u.getUserRemark()
            ));
        }
        TaskDetailResponse resp = new TaskDetailResponse();
        resp.setTaskId(task.getTaskId());
        resp.setTaskGroupId(taskGroupId);
        resp.setTaskGroupName(taskGroup.getGroupName());
        resp.setTeamId(teamId);
        resp.setTeamName(team == null ? null : team.getTeamName());
        resp.setTaskName(task.getTaskName());
        resp.setTaskDescription(task.getTaskDescription());
        resp.setTaskStatus(task.getTaskStatus());
        resp.setTaskPriority(task.getTaskPriority());
        resp.setStartTime(task.getStartTime());
        resp.setDueTime(task.getDueTime());
        resp.setFinishTime(task.getFinishTime());
        resp.setIsOwner(1);
        resp.setTaskHelperList(helperList);
        resp.setChildTaskList(childTasks);
        resp.setAttachments(Collections.emptyList());
        return R.success(resp, "创建成功");
    }


    /**
     * 添加子任务
     *
     * 接口：POST `/api/tasks/{taskId}/children`
     * 权限：需为该任务的协助者或拥有者（存在 TaskUserRelation 记录）
     *
     * 规则：
     * - 子任务索引 `childTaskIndex = 当前任务的子任务数量 + 1`
     * - 子任务名称 ≥ 2 个字符
     * - 子任务状态需在合法枚举范围内（默认 1 未开始）
     * - 子任务截止日期不得晚于父任务截止日期
     * - 子任务执行者必须为当前任务的协助者或当前拥有者
     *
     * 返回：R<ChildTask>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.CREATE, description = "创建子任务")
    @PostMapping("/children")
    public R<ChildTask> addChildTask(HttpServletRequest request, @RequestBody ChildTask childTask) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String taskId = CommonUtil.trim(childTask != null ? childTask.getTaskId() : null);
        if (taskId == null || taskId.isEmpty()) {
            return R.fail("缺少父任务ID taskId");
        }
        Task parent = taskService.getById(taskId);
        if (parent == null) return R.notFound("父任务不存在");

        long relCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, taskId)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId()));
        if (relCount == 0) {
            return R.fail("无权添加子任务，你需要在该任务的协助者列表中或为拥有者");
        }

        String childTaskName = CommonUtil.trim(childTask != null ? childTask.getChildTaskName() : null);
        Integer childTaskStatus = childTask != null ? childTask.getChildTaskStatus() : null; // 默认 1 未开始
        LocalDate dueTime = childTask != null ? childTask.getDueTime() : null;
        String assigneeUserId = CommonUtil.trim(childTask != null ? childTask.getChildTaskAssigneeId() : null);

        if (childTaskName == null || childTaskName.length() < 2) {
            return R.fail("子任务名称至少 2 个字符");
        }
        if (childTaskStatus == null || !CommonUtil.inEnumCodes(TaskStatusEnum.values(), childTaskStatus)) {
            childTaskStatus = 1;
        }
        if (dueTime == null) {
            return R.fail("子任务截止日期不能为空");
        }
        if (dueTime.isAfter(parent.getDueTime())) {
            return R.fail("子任务的截止日期不能大于主任务的截止日期");
        }
        if (assigneeUserId == null || assigneeUserId.isEmpty()) {
            return R.fail("子任务的执行者不能为空");
        }
        long assigneeRelCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, taskId)
                .eq(TaskUserRelation::getUserId, assigneeUserId));
        boolean isOwner = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, taskId)
                .eq(TaskUserRelation::getUserId, assigneeUserId)
                .eq(TaskUserRelation::getIfOwner, IfOwnerEnum.IS_OWNER.getCode())) > 0;
        if (assigneeRelCount == 0 && !isOwner) {
            return R.fail("子任务的执行者不在协助者列表中且不为拥有者");
        }

        int index = (int) childTaskService.count(new LambdaQueryWrapper<ChildTask>().eq(ChildTask::getTaskId, taskId)) + 1;
        LocalDateTime now = LocalDateTime.now();

        ChildTask ct = new ChildTask();
        ct.setTaskId(taskId);
        ct.setChildTaskName(childTaskName);
        ct.setChildTaskStatus(childTaskStatus);
        ct.setChildTaskIndex(index);
        ct.setChildTaskAssigneeId(assigneeUserId);
        ct.setDueTime(dueTime);
        // 如果状态为已完成（3）且 finish_time 为空或 null，则设置为当前日期
        // 如果状态为非已完成（不是3），则确保 finish_time 为 null
        if (childTaskStatus != null && childTaskStatus.equals(TaskStatusEnum.COMPLETED.getCode())) {
            ct.setFinishTime(LocalDate.now());
        } else {
        ct.setFinishTime(null);
        }
        ct.setCreateTime(now);
        ct.setUpdateTime(now);

        if (!childTaskService.save(ct)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建子任务失败");
        }

        return R.success(ct, "创建成功");
    }


    /**
     * 上传任务附件
     *
     * 接口：POST `/api/tasks/{taskId}/files`（multipart/form-data）
     * 权限：需为该任务的协助者或拥有者（存在 TaskUserRelation 记录）
     *
     * 入参：
     * - path：`taskId`（父任务ID）
     * - form：`file`（必须），`remark`（可选）
     *
     * 规则：
     * - 对象存储路径：`tasks/{taskId}/attachments/{uuid}{ext}`
     * - 记录上传者 `uploaderUserId`，附件基础信息（名称、类型、大小、备注）
     *
     * 返回：R<TaskFile>
     */
    @PostMapping(value = "/{taskId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @TaskAuditLog(action = TaskAuditActionEnum.UPDATE, description = "上传任务附件")
    public R<TaskFile> uploadTaskFile(HttpServletRequest request,
                                      @PathVariable("taskId") String taskId,
                                      @RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "remark", required = false) String remark) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        taskId = CommonUtil.trim(taskId);
        Task task = taskService.getById(taskId);
        if (task == null) return R.notFound("任务不存在");

        long relCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, taskId)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId()));
        if (relCount == 0) {
            return R.fail("无权上传附件：需为该任务的协助者或拥有者");
        }
        if (file == null || file.isEmpty()) {
            return R.fail("文件为空");
        }
        String original = file.getOriginalFilename();
        if (original == null || original.isEmpty()) {
            original = "upload-" + System.currentTimeMillis();
        }
        String suffix = "";
        if (original != null && original.contains(".")) {
            suffix = original.substring(original.lastIndexOf('.'));
        }
        String objectName = "tasks/" + taskId + "/attachments/" + CommonUtil.generateUuid(original) + (suffix == null ? "" : suffix);
        String contentType = file.getContentType();
        String url;
        try {
            url = minioUtil.uploadKnownSize(objectName, file.getInputStream(), (contentType == null ? "application/octet-stream" : contentType), file.getSize());
        } catch (Exception e) {
            return R.error("上传失败: " + e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        TaskFile tf = new TaskFile();
        tf.setTaskId(taskId);
        tf.setUploaderUserId(currentUser.getUserId());
        tf.setTaskFileName(original);
        tf.setTaskFilePath(url); // 存储MinIO路径或完整URL（视具体实现，这里假设是URL）
        tf.setTaskFileType(suffix);
        tf.setTaskFileSize(file.getSize());
        tf.setTaskFileStatus(1);
        tf.setTaskFileRemark(remark);
        tf.setUploadTime(now);
        tf.setCreateTime(now);
        tf.setUpdateTime(now);
        
        boolean ok = taskFileService.save(tf);
        if (!ok) return R.error("保存附件记录失败");
        return R.success(tf, "上传成功");
    }


    /**
     * 添加协助者
     *
     * 接口：POST `/api/tasks/followers`
     * 权限：仅任务拥有者可添加协助者
     *
     * 入参：TaskUserRelation（请求体）
     * - taskId：目标任务ID（必填）
     * - userId：协助者用户ID（必填）
     *
     * 行为：
     * - 校验当前用户为任务拥有者（`ifOwner=1`）
     * - 校验任务与协助者用户存在
     * - 若协助者已存在关系则直接返回已存在记录
     * - 创建 `task_user_relation` 记录，设置 `ifOwner=0`
     *
     * 返回：R<TaskUserRelation>
     *
     * 审计：@TaskAuditLog(action = UPDATE)
     *
     * 异常/失败：
     * - 400：缺少必填参数
     * - 401：未认证
     * - 403：无权限（非拥有者）
     * - 404：任务或用户不存在
     * - 500：保存失败
     *
     * @param request 当前请求（解析登录用户）
     * @param relationBody 协助者关系请求体
     * @return R<TaskUserRelation>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.UPDATE, description = "添加协助者")
    @PostMapping("/{taskId}/followers")
    public R<TaskUserRelation> addTaskHelper(HttpServletRequest request, @RequestBody TaskUserRelation relationBody) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        String targetTaskId = CommonUtil.trim(relationBody != null ? relationBody.getTaskId() : null);
        if (targetTaskId == null || targetTaskId.isEmpty()) {
            return R.fail("需提供 taskId");
        }
        String helperUserId = CommonUtil.trim(relationBody != null ? relationBody.getUserId() : null);
        if (helperUserId == null || helperUserId.isEmpty()) {
            return R.fail("需提供协助者 userId");
        }
        Task targetTask = taskService.getById(targetTaskId);
        if (targetTask == null) return R.notFound("任务不存在");
        boolean isCurrentUserOwner = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, targetTaskId)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                .eq(TaskUserRelation::getIfOwner, IfOwnerEnum.IS_OWNER.getCode())) > 0;
        if (!isCurrentUserOwner) {
            return R.fail("仅任务拥有者可添加协助者");
        }
        User helperUser = userService.getById(helperUserId);
        if (helperUser == null) return R.notFound("协助者用户不存在");
        TaskUserRelation existingRelation = taskUserRelationService.getOne(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, targetTaskId)
                .eq(TaskUserRelation::getUserId, helperUserId), false);
        if (existingRelation != null) {
            return R.success(existingRelation, "该用户已在协助者列表中");
        }
        LocalDateTime now = LocalDateTime.now();
        TaskUserRelation newRelation = new TaskUserRelation();
        newRelation.setTaskId(targetTaskId);
        newRelation.setUserId(helperUserId);
        newRelation.setIfOwner(IfOwnerEnum.IS_NOT_OWNER.getCode());
        newRelation.setCreateTime(now);
        newRelation.setUpdateTime(now);
        boolean ok = taskUserRelationService.save(newRelation);
        if (!ok) return R.error("添加协助者失败");
        return R.success(newRelation, "添加成功");
    }


    /**
     * 删除任务（级联清理）
     *
     * 接口：DELETE `/api/tasks{taskId}`
     * 权限：仅任务拥有者可删除
     *
     * 行为：
     * - 校验登录、任务存在与拥有者权限
     * - 删除附件：先移除对象存储，再删除附件记录
     * - 删除子任务：清理子任务协助者关系、图谱边与节点，最后删除子任务记录
     * - 删除主任务协助者关系
     * - 删除主任务图谱边与节点
     * - 删除任务审计日志
     * - 删除主任务记录
     *
     * 事务：
     * - 任一步骤失败将回滚事务并返回错误
     *
     * 返回：R<?>（data=null，message="删除成功"）
     *
     * 异常/失败：
     * - 400：缺少 taskId
     * - 401：未认证
     * - 403：无权限（非拥有者）
     * - 404：任务不存在
     * - 500：级联清理某步骤失败
     *
     * @param request 当前请求（解析登录用户）
     * @param taskId  任务ID
     * @return R<?>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.DELETE, description = "删除任务")
    @DeleteMapping("/{taskId}")
    @Transactional(rollbackFor = Exception.class)
    public R<?> deleteTask(HttpServletRequest request, @PathVariable("taskId") String taskId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        taskId = CommonUtil.trim(taskId);
        if (taskId == null || taskId.isEmpty()) {
            return R.fail("缺少 taskId");
        }
        Task task = taskService.getById(taskId);
        if (task == null) return R.notFound("任务不存在");
        boolean isOwner = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, taskId)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                .eq(TaskUserRelation::getIfOwner, IfOwnerEnum.IS_OWNER.getCode())) > 0;
        if (!isOwner) {
            return R.fail("仅任务拥有者可删除任务");
        }
        List<TaskFile> filesToDelete = taskFileService.list(new LambdaQueryWrapper<TaskFile>().eq(TaskFile::getTaskId, taskId));
        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            for (TaskFile tf : filesToDelete) {
                if (tf == null) continue;
                String objectName = extractObjectNameFromUrl(tf.getTaskFilePath());
                if (objectName != null && !objectName.isEmpty()) {
                    try {
                        minioUtil.remove(objectName);
                    } catch (Exception e) {
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return R.error("删除任务附件文件失败: " + e.getMessage());
                    }
                }
            }
            boolean filesRemoved = taskFileService.remove(new LambdaQueryWrapper<TaskFile>().eq(TaskFile::getTaskId, taskId));
            if (!filesRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除任务附件失败");
            }
        }
        List<ChildTask> childTasks = childTaskService.list(new LambdaQueryWrapper<ChildTask>().eq(ChildTask::getTaskId, taskId));
        List<String> childIds = new ArrayList<>();
        for (ChildTask ct : childTasks) {
            childIds.add(ct.getChildTaskId());
        }
        if (!childIds.isEmpty()) {
            long relCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>().in(TaskUserRelation::getTaskId, childIds));
            boolean relRemoved = (relCount == 0) || taskUserRelationService.remove(new LambdaQueryWrapper<TaskUserRelation>().in(TaskUserRelation::getTaskId, childIds));
            if (relCount > 0 && !relRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除子任务与用户关系失败");
            }
            List<TaskNode> childNodes = taskNodeService.list(new LambdaQueryWrapper<TaskNode>().in(TaskNode::getChildTaskId, childIds));
            List<String> childNodeIds = new ArrayList<>();
            for (TaskNode n : childNodes) {
                childNodeIds.add(n.getTaskNodeId());
            }
            if (!childNodeIds.isEmpty()) {
                boolean edgesRemoved = taskEdgeService.remove(new LambdaQueryWrapper<TaskEdge>()
                        .in(TaskEdge::getSourceNodeId, childNodeIds).or().in(TaskEdge::getTargetNodeId, childNodeIds));
                if (!edgesRemoved) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return R.error("删除子任务图谱边失败");
                }
                boolean nodesRemoved = taskNodeService.remove(new LambdaQueryWrapper<TaskNode>().in(TaskNode::getTaskNodeId, childNodeIds));
                if (!nodesRemoved) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return R.error("删除子任务图谱节点失败");
                }
            }
            boolean childRemoved = childTaskService.remove(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getChildTaskId, childIds));
            if (!childRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除子任务失败");
            }
        }
        long helperCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>().eq(TaskUserRelation::getTaskId, taskId));
        boolean helperRemoved = (helperCount == 0) || taskUserRelationService.remove(new LambdaQueryWrapper<TaskUserRelation>().eq(TaskUserRelation::getTaskId, taskId));
        if (helperCount > 0 && !helperRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除任务与用户关系失败");
        }
        List<TaskNode> mainNodes = taskNodeService.list(new LambdaQueryWrapper<TaskNode>().eq(TaskNode::getTaskId, taskId));
        List<String> mainNodeIds = new ArrayList<>();
        for (TaskNode n : mainNodes) {
            mainNodeIds.add(n.getTaskNodeId());
        }
        if (!mainNodeIds.isEmpty()) {
            boolean edgesRemoved = taskEdgeService.remove(new LambdaQueryWrapper<TaskEdge>()
                    .in(TaskEdge::getSourceNodeId, mainNodeIds).or().in(TaskEdge::getTargetNodeId, mainNodeIds));
            if (!edgesRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除任务图谱边失败");
            }
            boolean nodesRemoved = taskNodeService.remove(new LambdaQueryWrapper<TaskNode>().in(TaskNode::getTaskNodeId, mainNodeIds));
            if (!nodesRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除任务图谱节点失败");
            }
        }
        boolean auditsRemoved = taskAuditService.remove(new LambdaQueryWrapper<TaskAudit>().eq(TaskAudit::getTaskId, taskId));
        if (!auditsRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除审计日志失败");
        }
        boolean taskRemoved = taskService.removeById(taskId);
        if (!taskRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除任务失败");
        }
        return R.success(null, "删除成功");
    }


    /**
     * 删除子任务（级联清理）
     *
     * 接口：DELETE `/api/tasks/{taskId}/children/{childTaskId}`
     * 权限：仅父任务拥有者可删除
     *
     * 行为：
     * - 校验登录、子任务存在与父任务权限
     * - 删除与子任务关联的协助者关系（若以子任务ID作为 taskId 存储）
     * - 删除子任务图谱边与节点（`task_node.child_task_id = childTaskId`）
     * - 删除子任务记录
     *
     * 事务：
     * - 任一步骤失败将回滚事务并返回错误
     *
     * 返回：R<?>（data=null，message="删除成功"）
     *
     * 异常/失败：
     * - 400：缺少 childTaskId
     * - 401：未认证
     * - 403：无权限（非父任务拥有者）
     * - 404：子任务不存在
     * - 500：级联清理某步骤失败
     */
    @TaskAuditLog(action = TaskAuditActionEnum.DELETE, description = "删除子任务")
    @DeleteMapping("/{taskId}/children/{childTaskId}")
    @Transactional(rollbackFor = Exception.class)
    public R<?> deleteChildTask(HttpServletRequest request, @PathVariable("childTaskId") String childTaskId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        String id = CommonUtil.trim(childTaskId);
        if (id == null || id.isEmpty()) {
            return R.fail("缺少 childTaskId");
        }
        ChildTask child = childTaskService.getById(id);
        if (child == null) return R.notFound("子任务不存在");
        String parentTaskId = CommonUtil.trim(child.getTaskId());
        if (parentTaskId == null || parentTaskId.isEmpty()) {
            return R.fail("子任务缺少父任务ID");
        }

        if (taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>().eq(TaskUserRelation::getTaskId, parentTaskId).eq(TaskUserRelation::getUserId, currentUser.getUserId())) == 0) {
            return R.fail("仅父任务拥有者或协助者可删除该子任务");
        }

        List<TaskNode> childNodes = taskNodeService.list(new LambdaQueryWrapper<TaskNode>().eq(TaskNode::getChildTaskId, id));
        List<String> childNodeIds = new ArrayList<>();
        for (TaskNode n : childNodes) {
            childNodeIds.add(n.getTaskNodeId());
        }
        if (!childNodeIds.isEmpty()) {
            boolean edgesRemoved = taskEdgeService.remove(new LambdaQueryWrapper<TaskEdge>()
                    .in(TaskEdge::getSourceNodeId, childNodeIds).or().in(TaskEdge::getTargetNodeId, childNodeIds));
            if (!edgesRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除子任务图谱边失败");
            }
            boolean nodesRemoved = taskNodeService.remove(new LambdaQueryWrapper<TaskNode>().in(TaskNode::getTaskNodeId, childNodeIds));
            if (!nodesRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除子任务图谱节点失败");
            }
        }

        boolean childRemoved = childTaskService.removeById(id);
        if (!childRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除子任务失败");
        }
        return R.success(null, "删除成功");
    }

    /**
     * 删除协助者关系
     *
     * 接口：DELETE `/api/tasks/{taskId}/followers/{taskHelperId}`
     *
     * 权限：
     * - 协助者本人或任务拥有者
     *
     * 行为：
     * - 校验登录状态
     * - 校验协助者关系存在
     * - 禁止删除拥有者关系（`ifOwner = IS_OWNER`）
     * - 权限校验：仅协助者本人或该任务拥有者可删除该关系
     * - 子任务校验：被删除用户在该任务下不得作为任何子任务的执行者
     * - 删除协助者关系记录
     *
     * 返回：
     * - 成功：`R.success(null, "删除成功")`
     *
     * 异常/失败：
     * - 401：未认证（无法识别当前用户）
     * - 400：缺少 `taskHelperId`
     * - 404：协助者关系不存在
     * - 403：尝试删除拥有者关系或无删除权限
     * - 400：该协助者仍被指派为子任务执行者
     * - 500：删除协助者关系失败
     */
    @TaskAuditLog(action = TaskAuditActionEnum.DELETE, description = "删除协助者关系")
    @DeleteMapping("/{taskId}/followers/{taskHelperId}")
    public R<?> deleteTaskHelper(HttpServletRequest request, @PathVariable("taskId") String taskId, @PathVariable("taskHelperId") String taskHelperId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        if (taskId == null || taskId.isEmpty()) {
            return R.fail("缺少 taskId");
        }
        if (taskHelperId == null || taskHelperId.isEmpty()) {
            return R.fail("缺少 taskHelperId");
        }

        TaskUserRelation relation = taskUserRelationService.getOne(new LambdaQueryWrapper<TaskUserRelation>().eq(TaskUserRelation::getTaskId,taskId).eq(TaskUserRelation::getUserId,taskHelperId));
        if (relation == null) return R.notFound("协助者关系不存在");
        if (relation.getIfOwner() != null && relation.getIfOwner().equals(IfOwnerEnum.IS_OWNER.getCode())) {
            return R.fail("不能删除任务拥有者关系");
        }
        String targetTaskId = CommonUtil.trim(relation.getTaskId());
        String targetUserId = CommonUtil.trim(relation.getUserId());
        boolean isSelf = currentUser.getUserId().equals(targetUserId);
        boolean isOwner = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, targetTaskId)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                .eq(TaskUserRelation::getIfOwner, IfOwnerEnum.IS_OWNER.getCode())) > 0;
        if (!isSelf && !isOwner) {
            return R.fail("仅协助者本人或任务拥有者可删除");
        }

        long assignedChildCount = childTaskService.count(new LambdaQueryWrapper<ChildTask>()
                .eq(ChildTask::getTaskId, targetTaskId)
                .eq(ChildTask::getChildTaskAssigneeId, targetUserId));
        if (assignedChildCount > 0) {
            return R.fail("该协助者仍被指派为子任务的执行者，请先变更或删除相关子任务");
        }

        boolean removed = taskUserRelationService.removeById(relation.getTaskUserRelationId());
        if (!removed) return R.error("删除协助者失败");
        return R.success(null, "删除成功");
    }


    /**
     * 删除任务附件
     *
     * 接口：DELETE `/api/tasks/{taskId}/files/{taskFileId}`
     *
     * 权限：
     * - 任务拥有者或协助者（存在任务-用户关系）
     *
     * 行为：
     * - 校验登录状态与附件存在性
     * - 路径任务ID与附件所属任务ID一致性校验
     * - 权限校验：当前用户需为该任务拥有者或协助者
     * - 先删除对象存储中的文件（MinIO）
     * - 逻辑删除数据库附件记录（`@TableLogic isDelete`）
     *
     * 返回：
     * - 成功：`R.success(null, "删除成功")`
     *
     * 异常/失败：
     * - 401：未认证
     * - 400：缺少附件ID、缺少任务ID、路径任务ID与附件所属任务不一致
     * - 404：附件不存在
     * - 403：无权限（非拥有者或协助者）
     * - 500：删除对象存储失败或删除记录失败
     *
     * 事务：
     * - 任一步骤失败将回滚事务
     *
     * @param request 当前请求（解析登录用户）
     * @param tid 路径中的任务ID
     * @param taskFileId 附件记录ID
     * @return R<?>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.DELETE, description = "删除任务附件")
    @DeleteMapping("/{taskId}/files/{taskFileId}")
    @Transactional(rollbackFor = Exception.class)
    public R<?> deleteTaskFile(HttpServletRequest request, @PathVariable("taskId") String tid, @PathVariable("taskFileId") String taskFileId) {
        User cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");

        String id = CommonUtil.trim(taskFileId);
        if (id == null || id.isEmpty()) {
            return R.fail("缺少附件ID");
        }

        TaskFile tf = taskFileService.getById(id);
        if (tf == null) return R.notFound("附件不存在");

        String taskId = CommonUtil.trim(tf.getTaskId());
        if (taskId == null || taskId.isEmpty()) {
            return R.fail("附件缺少任务ID");
        }
        String pathTaskId = CommonUtil.trim(tid);
        if (pathTaskId == null || pathTaskId.isEmpty()) {
            return R.fail("缺少任务ID");
        }
        if (!taskId.equals(pathTaskId)) {
            return R.fail("路径任务ID与附件所属任务不一致");
        }

        long relCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, taskId)
                .eq(TaskUserRelation::getUserId, cu.getUserId()));
        if (relCount == 0) {
            return R.fail("无权删除该附件：仅协助者或拥有者可删除");
        }

        // 先删除对象存储中的文件
        String objectName = extractObjectNameFromUrl(tf.getTaskFilePath());
        if (objectName != null && !objectName.isEmpty()) {
            try {
                minioUtil.remove(objectName);
            } catch (Exception e) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除附件文件失败: " + e.getMessage());
            }
        }

        // 逻辑删除数据库记录（@TableLogic）
        boolean ok = taskFileService.removeById(id);
        if (!ok) {
            return R.error("删除附件记录失败");
        }

        return R.success(null, "删除成功");
    }


    /**
     * 更新任务基础信息
     *
     * 接口：PUT `/api/tasks`
     *
     * 权限：
     * - 任务拥有者或协助者（需存在 `task_user_relation` 关系）
     *
     * 行为：
     * - 校验登录与任务存在性
     * - 权限校验：当前用户需与该任务存在关系
     * - 仅更新请求体中非空字段
     * - 校验规则：
     *   - `taskName`：至少 2 个字符
     *   - `taskStatus`：仅允许 `TaskStatusEnum` 范围值
     *   - `taskPriority`：仅允许 `TaskPriorityEnum` 范围值
     *   - 时间维度：若同时提供 `startTime` 与 `dueTime/finishTime`，则需满足不早于 `startTime`
     *
     * 返回：
     * - 成功：`R<Task>`（更新后的任务）
     *
     * 异常/失败：
     * - 401：未认证
     * - 400：缺少 `taskId`、字段不合法或无权限
     * - 404：任务不存在
     * - 500：更新失败
     *
     * @param request 当前请求（解析登录用户）
     * @param taskToUpdate 待更新的任务实体（仅非空字段生效）
     * @return R<Task>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.UPDATE, description = "更新任务")
    @PutMapping()
    public R<Task> updateTask(HttpServletRequest request, @RequestBody Task taskToUpdate) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        if (taskToUpdate == null || taskToUpdate.getTaskId() == null || CommonUtil.trim(taskToUpdate.getTaskId()).isEmpty()) {
            return R.fail("缺少 taskId");
        }
        String taskId = CommonUtil.trim(taskToUpdate.getTaskId());
        Task task = taskService.getById(taskId);
        if (task == null) return R.notFound("任务不存在");

        long relCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, taskId)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId()));
        if (relCount == 0) return R.fail("无权修改该任务");

        // 仅更新 Task 本身的字段：非空即更新；其余保持原值
        if (taskToUpdate.getTaskName() != null) {
            String name = CommonUtil.trim(taskToUpdate.getTaskName());
            if (name == null || name.length() < 2) return R.fail("任务名称至少 2 个字符");
            task.setTaskName(name);
        }
        if (taskToUpdate.getTaskDescription() != null) {
            String desc = CommonUtil.trim(taskToUpdate.getTaskDescription());
            task.setTaskDescription(desc);
        }
        // 记录状态是否被更新，以及 finish_time 是否需要设置为 null
        boolean statusUpdated = false;
        boolean needSetFinishTimeNull = false;
        LocalDate finishTimeToSet = null;
        
        if (taskToUpdate.getTaskStatus() != null) {
            if (!CommonUtil.inEnumCodes(TaskStatusEnum.values(),taskToUpdate.getTaskStatus())) return R.fail("任务状态不合法");
            task.setTaskStatus(taskToUpdate.getTaskStatus());
            statusUpdated = true;
            // 如果状态为已完成（3）且 finish_time 为空或 null，则设置为当前日期
            // 如果状态为非已完成（不是3），则确保 finish_time 为 null
            if (taskToUpdate.getTaskStatus().equals(TaskStatusEnum.COMPLETED.getCode())) {
                if (task.getFinishTime() == null) {
                    finishTimeToSet = LocalDate.now();
                }
            } else {
                // 状态更新为非已完成，需要置空 finish_time
                needSetFinishTimeNull = true;
            }
        }
        if (taskToUpdate.getTaskPriority() != null) {
            if (!CommonUtil.inEnumCodes(TaskPriorityEnum.values(),taskToUpdate.getTaskPriority())) return R.fail("任务优先级不合法");
            task.setTaskPriority(taskToUpdate.getTaskPriority());
        }
        if (taskToUpdate.getStartTime() != null) task.setStartTime(taskToUpdate.getStartTime());
        if (taskToUpdate.getDueTime() != null) {
            if (taskToUpdate.getStartTime() != null && taskToUpdate.getDueTime().isBefore(taskToUpdate.getStartTime())) {
                return R.fail("截止日期不能早于开始日期");
            }
            task.setDueTime(taskToUpdate.getDueTime());
        }
        // 如果状态为已完成（3）且 finish_time 为空或 null，则设置为当前日期
        // 如果状态为非已完成（不是3），则确保 finish_time 为 null
        // 注意：如果状态已更新，finish_time 的处理已在状态更新时完成，这里只处理单独更新 finish_time 的情况
        if (taskToUpdate.getFinishTime() != null && !statusUpdated) {
            if (taskToUpdate.getStartTime() != null && taskToUpdate.getFinishTime().isBefore(taskToUpdate.getStartTime())) {
                return R.fail("完成日期不能早于开始日期");
            }
            // 只有在状态为已完成时才允许设置 finish_time
            if (task.getTaskStatus() != null && task.getTaskStatus().equals(TaskStatusEnum.COMPLETED.getCode())) {
                finishTimeToSet = taskToUpdate.getFinishTime();
            } else {
                // 状态不是已完成，需要置空 finish_time
                needSetFinishTimeNull = true;
            }
        }

        LocalDateTime updateTime = LocalDateTime.now();
        task.setUpdateTime(updateTime);
        
        // 如果需要设置 finish_time 为 null，使用 UpdateWrapper 显式更新（因为 updateById 不会更新 null 值）
        if (needSetFinishTimeNull) {
            LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Task::getTaskId, taskId)
                    .set(Task::getFinishTime, null)  // 显式设置 null
                    .set(Task::getUpdateTime, updateTime);
            // 同时更新其他已修改的字段
            if (taskToUpdate.getTaskName() != null) {
                String name = CommonUtil.trim(taskToUpdate.getTaskName());
                if (name != null && name.length() >= 2) {
                    updateWrapper.set(Task::getTaskName, name);
                }
            }
            if (taskToUpdate.getTaskDescription() != null) {
                updateWrapper.set(Task::getTaskDescription, CommonUtil.trim(taskToUpdate.getTaskDescription()));
            }
            if (statusUpdated) {
                updateWrapper.set(Task::getTaskStatus, taskToUpdate.getTaskStatus());
            }
            if (taskToUpdate.getTaskPriority() != null) {
                updateWrapper.set(Task::getTaskPriority, taskToUpdate.getTaskPriority());
            }
            if (taskToUpdate.getStartTime() != null) {
                updateWrapper.set(Task::getStartTime, taskToUpdate.getStartTime());
            }
            if (taskToUpdate.getDueTime() != null) {
                updateWrapper.set(Task::getDueTime, taskToUpdate.getDueTime());
            }
            boolean ok = taskService.update(updateWrapper);
            if (!ok) return R.error("更新失败");
            // 重新查询更新后的任务
            task = taskService.getById(taskId);
            return R.success(task, "更新成功");
        } else if (finishTimeToSet != null) {
            // 需要设置 finish_time 为具体日期，使用 UpdateWrapper 确保更新
            task.setFinishTime(finishTimeToSet);
            // 如果还有其他字段更新，使用 updateById；否则也使用 UpdateWrapper 确保 finish_time 被更新
            boolean ok = taskService.updateById(task);
            if (!ok) return R.error("更新失败");
            return R.success(task, "更新成功");
        }
        
        // 普通更新，不需要特殊处理 finish_time
        boolean ok = taskService.updateById(task);
        if (!ok) return R.error("更新失败");
        return R.success(task, "更新成功");
    }


    /**
     * 更新子任务
     *
     * 接口：PUT `/api/tasks/{taskId}/children`
     *
     * 权限：
     * - 父任务拥有者或协助者（需与父任务存在 `task_user_relation` 关系）
     *
     * 行为：
     * - 校验登录与子任务存在性
     * - 通过父任务ID校验权限（请求体携带 `childTaskId` 作为更新对象）
     * - 仅更新请求体中非空字段
     * - 校验规则：
     *   - `childTaskName`：至少 2 个字符
     *   - `childTaskStatus`：仅允许 `TaskStatusEnum` 范围值
     *   - `dueTime`：不得晚于父任务的 `dueTime`（若父任务截止存在）
     *   - `childTaskAssigneeId`：若不为空则需为协助者或拥有者；允许置空
     *
     * 返回：
     * - 成功：`R<ChildTask>`（更新后的子任务）
     *
     * 异常/失败：
     * - 401：未认证
     * - 400：缺少 `childTaskId`、字段不合法或无权限
     * - 404：子任务或指派用户不存在
     * - 500：更新失败
     *
     * @param request 当前请求（解析登录用户）
     * @param childTask 待更新的子任务实体（需包含 `childTaskId`，仅非空字段生效）
     * @return R<ChildTask>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.UPDATE, description = "更新子任务")
    @PutMapping("/{taskId}/children")
    public R<ChildTask> updateChildTask(HttpServletRequest request, @RequestBody ChildTask childTask) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String childTaskId = CommonUtil.trim(childTask != null ? childTask.getChildTaskId() : null);
        if (childTaskId == null || childTaskId.isEmpty()) {
            return R.fail("缺少 childTaskId");
        }

        ChildTask ct = childTaskService.getById(childTaskId);
        if (ct == null) return R.notFound("子任务不存在");

        String parentTaskId = CommonUtil.trim(ct.getTaskId());
        if (parentTaskId == null || parentTaskId.isEmpty()) {
            return R.fail("子任务缺少父任务ID");
        }

        long relCountChild = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, parentTaskId)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId()));
        if (relCountChild == 0) return R.fail("无权修改该子任务：仅协助者或拥有者可修改");

        // 按照“仅更新非空字段”的策略进行更新
        if (childTask.getChildTaskName() != null) {
            String name = CommonUtil.trim(childTask.getChildTaskName());
            if (name == null) {
                // 若传入为空字符串，忽略对名称的更新，避免写入空值
            } else if (name.length() < 2) {
                return R.fail("子任务名称至少 2 个字符");
            } else {
                ct.setChildTaskName(name);
            }
        }

        // 记录状态是否被更新，以及 finish_time 是否需要设置为 null
        boolean statusUpdated = false;
        boolean needSetFinishTimeNull = false;
        LocalDate finishTimeToSet = null;

        if (childTask.getChildTaskStatus() != null) {
            if (!CommonUtil.inEnumCodes(TaskStatusEnum.values(),childTask.getChildTaskStatus())) {
                return R.fail("子任务状态不合法");
            }
            ct.setChildTaskStatus(childTask.getChildTaskStatus());
            statusUpdated = true;
            // 如果状态为已完成（3）且 finish_time 为空或 null，则设置为当前日期
            // 如果状态为非已完成（不是3），则确保 finish_time 为 null
            if (childTask.getChildTaskStatus().equals(TaskStatusEnum.COMPLETED.getCode())) {
                if (ct.getFinishTime() == null) {
                    finishTimeToSet = LocalDate.now();
                }
            } else {
                // 状态更新为非已完成，需要置空 finish_time
                needSetFinishTimeNull = true;
            }
        }

        if (childTask.getDueTime() != null) {
            Task parent = taskService.getById(parentTaskId);
            if (parent != null && parent.getDueTime() != null && childTask.getDueTime().isAfter(parent.getDueTime())) {
                return R.fail("子任务的截止日期不能晚于主任务的截止日期");
            }
            ct.setDueTime(childTask.getDueTime());
        }
        // 如果状态为已完成（3）且 finish_time 为空或 null，则设置为当前日期
        // 如果状态为非已完成（不是3），则确保 finish_time 为 null
        // 注意：如果状态已更新，finish_time 的处理已在状态更新时完成，这里只处理单独更新 finish_time 的情况
        if (childTask.getFinishTime() != null && !statusUpdated) {
            // 只有在状态为已完成时才允许设置 finish_time
            if (ct.getChildTaskStatus() != null && ct.getChildTaskStatus().equals(TaskStatusEnum.COMPLETED.getCode())) {
                finishTimeToSet = childTask.getFinishTime();
            } else {
                // 状态不是已完成，需要置空 finish_time
                needSetFinishTimeNull = true;
            }
        }

        if (childTask.getChildTaskAssigneeId() != null) {
            String newAssigneeId = CommonUtil.trim(childTask.getChildTaskAssigneeId());
            if (newAssigneeId != null && !newAssigneeId.isEmpty()) {
                User assignee = userService.getById(newAssigneeId);
                if (assignee == null) return R.notFound("被指派用户不存在");
                long assigneeRelCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                        .eq(TaskUserRelation::getTaskId, parentTaskId)
                        .eq(TaskUserRelation::getUserId, newAssigneeId));
                if (assigneeRelCount == 0) return R.error("被指派用户非协助者或拥有者");
            }
            // 支持清空执行者：允许设置为 null 或空字符串
            ct.setChildTaskAssigneeId(newAssigneeId);
        }

        LocalDateTime updateTime = LocalDateTime.now();
        ct.setUpdateTime(updateTime);
        
        // 如果需要设置 finish_time 为 null，使用 UpdateWrapper 显式更新（因为 updateById 不会更新 null 值）
        if (needSetFinishTimeNull) {
            LambdaUpdateWrapper<ChildTask> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ChildTask::getChildTaskId, childTaskId)
                    .set(ChildTask::getFinishTime, null)  // 显式设置 null
                    .set(ChildTask::getUpdateTime, updateTime);
            // 同时更新其他已修改的字段
            if (childTask.getChildTaskName() != null) {
                String name = CommonUtil.trim(childTask.getChildTaskName());
                if (name != null && name.length() >= 2) {
                    updateWrapper.set(ChildTask::getChildTaskName, name);
                }
            }
            if (statusUpdated) {
                updateWrapper.set(ChildTask::getChildTaskStatus, childTask.getChildTaskStatus());
            }
            if (childTask.getDueTime() != null) {
                updateWrapper.set(ChildTask::getDueTime, childTask.getDueTime());
            }
            if (childTask.getChildTaskAssigneeId() != null) {
                updateWrapper.set(ChildTask::getChildTaskAssigneeId, CommonUtil.trim(childTask.getChildTaskAssigneeId()));
            }
            boolean ok = childTaskService.update(updateWrapper);
            if (!ok) return R.error("更新子任务失败");
            // 重新查询更新后的子任务
            ct = childTaskService.getById(childTaskId);
            return R.success(ct, "更新成功");
        } else if (finishTimeToSet != null) {
            // 需要设置 finish_time 为具体日期
            ct.setFinishTime(finishTimeToSet);
            boolean ok = childTaskService.updateById(ct);
            if (!ok) return R.error("更新子任务失败");
            return R.success(ct, "更新成功");
        }
        
        // 普通更新，不需要特殊处理 finish_time
        boolean ok = childTaskService.updateById(ct);
        if (!ok) return R.error("更新子任务失败");
        return R.success(ct, "更新成功");
    }

    /**
     * 调整子任务排序
     *
     * 接口：PUT `/api/tasks/{taskId}/children/order`
     *
     * 权限：
     * - 任务拥有者允许；
     * - 所有执行者允许（任一子任务执行者均可调整整个集合顺序）
     *
     * 行为：
     * - 校验登录与父任务存在；
     * - 检查请求体为有序的 `childTaskId` 列表，集合需与当前任务的子任务集合一致（保持相同集、无重复）；
     * - 依次更新 `child_task_index` 为列表顺序（从 1 开始）；
     * - 返回按最新索引排序的子任务列表（索引升序 → 创建时间降序）。
     *
     * 返回：
     * - 成功：`R<List<ChildTask>>`（最新顺序的子任务列表）
     *
     * 异常/失败：
     * - 401：未认证
     * - 400：缺少或非法的 `childTaskId` 列表、集合不一致或存在重复
     * - 403：无权限（非拥有者且不是任一子任务执行者）
     * - 404：父任务或子任务不存在
     * - 500：更新失败
     *
     * @param request 当前请求（解析登录用户）
     * @param taskId 父任务ID（路径参数）
     * @param orderedChildIds 有序的子任务ID列表（保持相同集）
     * @return R<List<ChildTask>>
     */
    @TaskAuditLog(action = TaskAuditActionEnum.UPDATE, description = "调整子任务排序")
    @PutMapping("/{taskId}/children/order")
    @Transactional(rollbackFor = Exception.class)
    public R<List<ChildTask>> reorderChildTasks(HttpServletRequest request,
                                                @PathVariable("taskId") String taskId,
                                                @RequestBody List<String> orderedChildIds) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(taskId);
        if (tid == null || tid.isEmpty()) {
            return R.fail("缺少任务ID");
        }
        Task parent = taskService.getById(tid);
        if (parent == null) return R.notFound("任务不存在");

        // 读取当前任务的子任务集合
        List<ChildTask> existingChildren = childTaskService.list(new LambdaQueryWrapper<ChildTask>().eq(ChildTask::getTaskId, tid));
        if (existingChildren == null) existingChildren = Collections.emptyList();
        Set<String> existingIds = existingChildren.stream()
                .map(ChildTask::getChildTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 校验请求体
        if (orderedChildIds == null) {
            return R.fail("需提供有序的子任务ID列表");
        }
        List<String> cleaned = orderedChildIds.stream()
                .map(CommonUtil::trim)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toList());
        if (cleaned.size() != orderedChildIds.size()) {
            return R.fail("子任务ID列表存在空值");
        }
        Set<String> providedSet = new LinkedHashSet<>(cleaned);
        if (providedSet.size() != cleaned.size()) {
            return R.fail("子任务ID列表存在重复项");
        }
        if (providedSet.size() != existingIds.size() || !existingIds.equals(providedSet)) {
            return R.fail("子任务集合不一致：需与当前任务的子任务集合完全一致");
        }

        // 权限校验：拥有者或对所有子任务均为执行人
        boolean isOwner = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>()
                .eq(TaskUserRelation::getTaskId, tid)
                .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                .eq(TaskUserRelation::getIfOwner, IfOwnerEnum.IS_OWNER.getCode())) > 0;
        boolean isExecutor = existingChildren.stream().anyMatch(ct -> {
            String assigneeId = CommonUtil.trim(ct.getChildTaskAssigneeId());
            return assigneeId != null && assigneeId.equals(currentUser.getUserId());
        });
        if (!isOwner && !isExecutor) {
            return R.fail("仅任务拥有者或任一子任务执行者可调整排序");
        }

        // 依次更新索引（从 1 开始）
        int index = 1;
        for (String cid : cleaned) {
            ChildTask ct = childTaskService.getById(cid);
            if (ct == null) return R.notFound("子任务不存在: " + cid);
            ct.setChildTaskIndex(index++);
            ct.setUpdateTime(LocalDateTime.now());
            boolean ok = childTaskService.updateById(ct);
            if (!ok) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("子任务排序更新失败");
            }
        }

        // 返回最新顺序
        List<ChildTask> children = childTaskService.list(new LambdaQueryWrapper<ChildTask>().eq(ChildTask::getTaskId, tid));
        if (children == null) children = Collections.emptyList();
        children.sort(Comparator
                .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return R.success(children, "排序更新成功");
    }



    /**
     * 列出指定任务族的任务列表（非分页）
     *
     * 接口：GET `/api/tasks/taskgroups/{taskGroupId}/tasks`
     * 权限：需为该任务族的正常成员（owner/manager/member）
     *
     * 请求参数：
     * - taskName：任务名称（模糊匹配）
     * - taskStatus：任务状态筛选（整数枚举列表）
     * - taskPriority：任务优先级筛选（整数枚举列表）
     * - startDueTime：截止时间起（含）
     * - EndDueTime：截止时间止（含）
     * - sortByMode：排序模式（0=按截止时间；1=按优先级；默认创建时间倒序）
     * - relatedToMe：是否只返回与我有关的任务（我为拥有者或协助者）
     *
     * 返回：`R<List<TaskSummaryResponse>>`
     * - 扁平结构的任务摘要列表，包含任务、任务族、团队、是否拥有者、子任务列表等必要字段
     */
    @GetMapping("/taskgroups/{taskGroupId}/tasks")
    public R<List<TaskSummaryResponse>> getTaskListByTaskGroup(HttpServletRequest request,
                                                               @PathVariable("taskGroupId") String taskGroupId,
                                                               @RequestParam(value = "taskName", required = false) String taskName,
                                                               @RequestParam(value = "taskStatus", required = false) List<Integer> taskStatusList,
                                                               @RequestParam(value = "taskPriority", required = false) List<Integer> taskPriorityList,
                                                               @RequestParam(value = "startDueTime", required = false) LocalDate startDueTime,
                                                               @RequestParam(value = "EndDueTime", required = false) LocalDate endDueTime,
                                                               @RequestParam(value = "sortByMode", required = false) Integer sortByMode, // 0-按照DDL时间排序，1-按照优先级排序
                                                               @RequestParam(value = "relatedToMe", required = false) Boolean relatedToMe) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        taskGroupId = CommonUtil.trim(taskGroupId);
        if (taskGroupId == null || taskGroupId.isEmpty()) {
            return R.fail("缺少任务族ID");
        }
        TaskGroup group = taskGroupService.getById(taskGroupId);
        if (group == null) {
            return R.notFound("任务族不存在");
        }
        TaskGroupUserRelation viewerRel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                        .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                        .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (viewerRel == null) {
            return R.fail("无权查看该任务族的任务列表");
        }

        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<Task>()
                .eq(Task::getTaskGroupId, taskGroupId);

        String nameLike = CommonUtil.trim(taskName);
        if (nameLike != null && !nameLike.isEmpty()) {
            qw.like(Task::getTaskName, nameLike);
        }
        if (taskStatusList != null && !taskStatusList.isEmpty()) {
            qw.in(Task::getTaskStatus, taskStatusList);
        }
        if (taskPriorityList != null && !taskPriorityList.isEmpty()) {
            qw.in(Task::getTaskPriority, taskPriorityList);
        }
        if (startDueTime != null) {
            qw.ge(Task::getDueTime, startDueTime);
        }
        if (endDueTime != null) {
            qw.le(Task::getDueTime, endDueTime);
        }

        List<Task> tasks = taskService.list(qw);
        if (tasks == null) tasks = Collections.emptyList();

        Set<String> taskIds = tasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, TaskUserRelation> myRelMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<TaskUserRelation> myRels = taskUserRelationService.list(
                    new LambdaQueryWrapper<TaskUserRelation>()
                            .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                            .in(TaskUserRelation::getTaskId, taskIds)
            );
            for (TaskUserRelation r : myRels) {
                if (r == null || r.getTaskId() == null) continue;
                myRelMap.put(r.getTaskId(), r);
            }
        }

        if (Boolean.TRUE.equals(relatedToMe)) {
            tasks = tasks.stream().filter(t -> myRelMap.containsKey(t.getTaskId())).collect(Collectors.toList());
        }

        Integer sortMode = sortByMode;
        if (sortMode == null) sortMode = -1;
        if (sortMode == 0) {
            tasks.sort(
                    Comparator.comparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority()).reversed())
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else if (sortMode == 1) {
            tasks.sort(
                    Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority()).reversed()
                            .thenComparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else {
            tasks.sort(Comparator.comparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        Map<String, List<ChildTask>> childrenMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<ChildTask> allChildren = childTaskService.list(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, taskIds));
            if (allChildren == null) allChildren = Collections.emptyList();
            for (ChildTask ct : allChildren) {
                if (ct == null || ct.getTaskId() == null) continue;
                List<ChildTask> list = childrenMap.computeIfAbsent(ct.getTaskId(), k -> new ArrayList<>());
                list.add(ct);
            }
            for (Map.Entry<String, List<ChildTask>> e : childrenMap.entrySet()) {
                List<ChildTask> list = e.getValue();
                if (list == null) continue;
                list.sort(Comparator
                        .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        }

        Team team = null;
        if (group.getTeamId() != null && !group.getTeamId().trim().isEmpty()) {
            team = teamService.getById(group.getTeamId());
        }

        List<TaskSummaryResponse> out = new ArrayList<>();
        for (Task t : tasks) {
            TaskSummaryResponse item = new TaskSummaryResponse();
            item.setTaskId(t.getTaskId());
            item.setTaskGroupId(t.getTaskGroupId());
            item.setTaskGroupName(group.getGroupName());
            item.setTeamId(group.getTeamId());
            item.setTeamName(team == null ? null : team.getTeamName());
            item.setTaskName(t.getTaskName());
            item.setTaskDescription(t.getTaskDescription());
            item.setTaskStatus(t.getTaskStatus());
            item.setTaskPriority(t.getTaskPriority());
            item.setStartTime(t.getStartTime());
            item.setDueTime(t.getDueTime());
            item.setFinishTime(t.getFinishTime());
            TaskUserRelation rel = myRelMap.get(t.getTaskId());
            item.setIsOwner(rel == null ? IfOwnerEnum.IS_NOT_OWNER.getCode() : rel.getIfOwner());
            item.setChildTaskList(childrenMap.getOrDefault(t.getTaskId(), Collections.emptyList()));
            out.add(item);
        }

        return R.success(out, "查询成功");
    }

    /**
     * 分页列出指定任务族的任务列表（筛选与排序与非分页一致）
     *
     * 接口：GET `/api/tasks/taskgroups/{taskGroupId}/tasks/page`
     * 权限：需为该任务族的正常成员（owner/manager/member）
     *
     * 请求参数：
     * - page：页码，默认 1
     * - size：每页大小，默认 10，最大 100
     * - 其余参数与非分页列表一致：taskName、taskStatus、taskPriority、startDueTime、EndDueTime、sortByMode、relatedToMe
     *
     * 返回：`R<Page<TaskSummaryResponse>>`
     * - 分页的扁平任务摘要记录
     */
    @GetMapping("/taskgroups/{taskGroupId}/tasks/page")
    public R<Page<TaskSummaryResponse>> getTaskPageByTaskGroup(HttpServletRequest request,
                                                               @PathVariable("taskGroupId") String taskGroupId,
                                                               @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                               @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                               @RequestParam(value = "taskName", required = false) String taskName,
                                                               @RequestParam(value = "taskStatus", required = false) List<Integer> taskStatusList,
                                                               @RequestParam(value = "taskPriority", required = false) List<Integer> taskPriorityList,
                                                               @RequestParam(value = "startDueTime", required = false) LocalDate startDueTime,
                                                               @RequestParam(value = "EndDueTime", required = false) LocalDate endDueTime,
                                                               @RequestParam(value = "sortByMode", required = false) Integer sortByMode,
                                                               @RequestParam(value = "relatedToMe", required = false) Boolean relatedToMe) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        taskGroupId = CommonUtil.trim(taskGroupId);
        if (taskGroupId == null || taskGroupId.isEmpty()) {
            return R.fail("缺少任务族ID");
        }
        TaskGroup group = taskGroupService.getById(taskGroupId);
        if (group == null) {
            return R.notFound("任务族不存在");
        }
        TaskGroupUserRelation viewerRel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                        .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                        .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (viewerRel == null) {
            return R.fail("无权查看该任务族的任务列表");
        }

        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<Task>()
                .eq(Task::getTaskGroupId, taskGroupId);
        String nameLike = CommonUtil.trim(taskName);
        if (nameLike != null && !nameLike.isEmpty()) {
            qw.like(Task::getTaskName, nameLike);
        }
        if (taskStatusList != null && !taskStatusList.isEmpty()) {
            qw.in(Task::getTaskStatus, taskStatusList);
        }
        if (taskPriorityList != null && !taskPriorityList.isEmpty()) {
            qw.in(Task::getTaskPriority, taskPriorityList);
        }
        if (startDueTime != null) {
            qw.ge(Task::getDueTime, startDueTime);
        }
        if (endDueTime != null) {
            qw.le(Task::getDueTime, endDueTime);
        }

        List<Task> tasks = taskService.list(qw);
        if (tasks == null) tasks = Collections.emptyList();

        Set<String> taskIds = tasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, TaskUserRelation> myRelMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<TaskUserRelation> myRels = taskUserRelationService.list(
                    new LambdaQueryWrapper<TaskUserRelation>()
                            .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                            .in(TaskUserRelation::getTaskId, taskIds)
            );
            for (TaskUserRelation r : myRels) {
                if (r == null || r.getTaskId() == null) continue;
                myRelMap.put(r.getTaskId(), r);
            }
        }

        if (Boolean.TRUE.equals(relatedToMe)) {
            tasks = tasks.stream().filter(t -> myRelMap.containsKey(t.getTaskId())).collect(Collectors.toList());
        }

        Integer sortMode = sortByMode;
        if (sortMode == null) sortMode = -1;
        if (sortMode == 0) {
            tasks.sort(
                    Comparator.comparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority()).reversed())
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else if (sortMode == 1) {
            tasks.sort(
                    Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority()).reversed()
                            .thenComparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else {
            tasks.sort(Comparator.comparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 10 : Math.min(size, 100);
        int fromIndex = (safePage - 1) * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, tasks.size());
        List<Task> pageTasks = (fromIndex >= tasks.size()) ? Collections.emptyList() : tasks.subList(fromIndex, toIndex);

        Map<String, List<ChildTask>> childrenMap = new HashMap<>();
        Set<String> pageTaskIds = pageTasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!pageTaskIds.isEmpty()) {
            List<ChildTask> allChildren = childTaskService.list(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, pageTaskIds));
            if (allChildren == null) allChildren = Collections.emptyList();
            for (ChildTask ct : allChildren) {
                if (ct == null || ct.getTaskId() == null) continue;
                List<ChildTask> list = childrenMap.computeIfAbsent(ct.getTaskId(), k -> new ArrayList<>());
                list.add(ct);
            }
            for (Map.Entry<String, List<ChildTask>> e : childrenMap.entrySet()) {
                List<ChildTask> list = e.getValue();
                if (list == null) continue;
                list.sort(Comparator
                        .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        }

        Team team = null;
        if (group.getTeamId() != null && !group.getTeamId().trim().isEmpty()) {
            team = teamService.getById(group.getTeamId());
        }

        List<TaskSummaryResponse> records = new ArrayList<>();
        for (Task t : pageTasks) {
            TaskSummaryResponse item = new TaskSummaryResponse();
            item.setTaskId(t.getTaskId());
            item.setTaskGroupId(t.getTaskGroupId());
            item.setTaskGroupName(group.getGroupName());
            item.setTeamId(group.getTeamId());
            item.setTeamName(team == null ? null : team.getTeamName());
            item.setTaskName(t.getTaskName());
            item.setTaskDescription(t.getTaskDescription());
            item.setTaskStatus(t.getTaskStatus());
            item.setTaskPriority(t.getTaskPriority());
            item.setStartTime(t.getStartTime());
            item.setDueTime(t.getDueTime());
            item.setFinishTime(t.getFinishTime());
            TaskUserRelation rel = myRelMap.get(t.getTaskId());
            item.setIsOwner(rel == null ? IfOwnerEnum.IS_NOT_OWNER.getCode() : rel.getIfOwner());
            item.setChildTaskList(childrenMap.getOrDefault(t.getTaskId(), Collections.emptyList()));
            records.add(item);
        }

        Page<TaskSummaryResponse> outPage = new Page<>(safePage, safeSize);
        outPage.setTotal(tasks.size());
        long pagesNum = (tasks.size() + safeSize - 1L) / safeSize;
        outPage.setPages(pagesNum);
        outPage.setRecords(records);
        return R.success(outPage, "查询成功");
    }


    /**
     * 列出当前用户相关的任务列表（非分页）
     *
     * 接口：GET `/api/tasks/me`
     * 权限：需 JWT 鉴权（仅返回与当前用户相关的任务）
     *
     * 请求参数：
     * - taskName：任务名称（模糊匹配）
     * - taskStatus：任务状态筛选（整数枚举列表）
     * - taskPriority：任务优先级筛选（整数枚举列表）
     * - startDueTime：截止时间起（含）
     * - EndDueTime：截止时间止（含）
     * - sortByMode：排序模式（0=按截止时间；1=按优先级；默认创建时间倒序）
     * - taskGroupId：任务族ID列表（可选，用于限定范围）
     *
     * 返回：`R<List<TaskSummaryResponse>>`
     * - 扁平结构的任务摘要列表，包含任务、任务族、团队、与我关系、子任务摘要
     */
    @GetMapping("/me")
    public R<List<TaskSummaryResponse>> getTaskListByUserId(HttpServletRequest request,
                                                            @RequestParam(value = "taskName", required = false) String taskName,
                                                            @RequestParam(value = "taskStatus", required = false) List<Integer> taskStatusList,
                                                            @RequestParam(value = "taskPriority", required = false) List<Integer> taskPriorityList,
                                                            @RequestParam(value = "startDueTime", required = false) LocalDate startDueTime,
                                                            @RequestParam(value = "EndDueTime", required = false) LocalDate endDueTime,
                                                            @RequestParam(value = "sortByMode", required = false) Integer sortByMode,
                                                            @RequestParam(value = "taskGroupId", required = false) List<String> taskGroupIds) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        // 基础筛选：taskGroupId（可选列表）、taskName（模糊）、taskStatus（列表）
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        if (taskGroupIds != null && !taskGroupIds.isEmpty()) {
            qw.in(Task::getTaskGroupId, taskGroupIds);
        }
        String nameLike = CommonUtil.trim(taskName);
        if (nameLike != null && !nameLike.isEmpty()) {
            qw.like(Task::getTaskName, nameLike);
        }
        if (taskStatusList != null && !taskStatusList.isEmpty()) {
            qw.in(Task::getTaskStatus, taskStatusList);
        }
        if (taskPriorityList != null && !taskPriorityList.isEmpty()) {
            qw.in(Task::getTaskPriority, taskPriorityList);
        }
        if (startDueTime != null) {
            qw.ge(Task::getDueTime, startDueTime);
        }
        if (endDueTime != null) {
            qw.le(Task::getDueTime, endDueTime);
        }

        List<Task> tasks = taskService.list(qw);
        if (tasks == null) tasks = Collections.emptyList();

        // 预取当前用户与这些任务的关系（固定必要条件：仅保留与我相关）
        Set<String> taskIds = tasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, TaskUserRelation> myRelMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<TaskUserRelation> myRels = taskUserRelationService.list(
                    new LambdaQueryWrapper<TaskUserRelation>()
                            .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                            .in(TaskUserRelation::getTaskId, taskIds)
            );
            for (TaskUserRelation r : myRels) {
                if (r == null || r.getTaskId() == null) continue;
                TaskUserRelation prev = myRelMap.get(r.getTaskId());
                if (prev == null || (prev.getCreateTime() != null && r.getCreateTime() != null && r.getCreateTime().isAfter(prev.getCreateTime()))) {
                    myRelMap.put(r.getTaskId(), r);
                } else if (prev == null) {
                    myRelMap.put(r.getTaskId(), r);
                }
            }
        }

        // 与我相关：当前用户在该任务上存在协助/拥有者关系记录
        List<Task> relatedTasks = tasks.stream()
                .filter(t -> myRelMap.containsKey(t.getTaskId()))
                .collect(Collectors.toList());

        // 排序模式：与 getTaskListByTaskGroup 一致（0=按截止时间；1=按优先级；默认创建时间倒序）
        Integer sortMode = (sortByMode == null) ? -1 : sortByMode;
        if (sortMode == 0) {
            relatedTasks.sort(
                    Comparator.comparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority()).reversed())
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else if (sortMode == 1) {
            relatedTasks.sort(
                    Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority())
                            .reversed()
                            .thenComparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else {
            relatedTasks.sort(Comparator.comparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        if (relatedTasks.isEmpty()) {
            return R.success(Collections.emptyList(), "查询成功");
        }

        // 批量查询子任务
        Map<String, List<ChildTask>> childrenMap = new HashMap<>();
        Set<String> relatedTaskIds = relatedTasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!relatedTaskIds.isEmpty()) {
            List<ChildTask> allChildren = childTaskService.list(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, relatedTaskIds));
            if (allChildren == null) allChildren = Collections.emptyList();
            for (ChildTask ct : allChildren) {
                if (ct == null || ct.getTaskId() == null) continue;
                List<ChildTask> list = childrenMap.computeIfAbsent(ct.getTaskId(), k -> new ArrayList<>());
                list.add(ct);
            }
            for (Map.Entry<String, List<ChildTask>> e : childrenMap.entrySet()) {
                List<ChildTask> list = e.getValue();
                if (list == null) continue;
                list.sort(Comparator
                        .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        }

        // 预取任务族名称映射
        Set<String> groupIds = relatedTasks.stream().map(Task::getTaskGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, TaskGroup> groupMap = groupIds.isEmpty() ? Collections.emptyMap()
                : taskGroupService.listByIds(groupIds).stream().collect(Collectors.toMap(TaskGroup::getTaskGroupId, g -> g));

        // 组装列表记录（简化版）
        List<TaskSummaryResponse> out = new ArrayList<>();
        for (Task t : relatedTasks) {
            TaskGroup g = t.getTaskGroupId() == null ? null : groupMap.get(t.getTaskGroupId());
            TaskUserRelation myRel = myRelMap.get(t.getTaskId());

            TaskSummaryResponse item = new TaskSummaryResponse();
            item.setTaskId(t.getTaskId());
            item.setTaskGroupId(t.getTaskGroupId());
            item.setTaskGroupName(g == null ? null : g.getGroupName());
            item.setTeamId(t.getTeamId());
            item.setTeamName(null);
            item.setTaskName(t.getTaskName());
            item.setTaskDescription(t.getTaskDescription());
            item.setTaskStatus(t.getTaskStatus());
            item.setTaskPriority(t.getTaskPriority());
            item.setStartTime(t.getStartTime());
            item.setDueTime(t.getDueTime());
            item.setFinishTime(t.getFinishTime());
            Integer ownerFlag = 0;
            if (myRel != null && myRel.getIfOwner() != null) {
                ownerFlag = myRel.getIfOwner();
            }
            item.setIsOwner(ownerFlag);
            item.setChildTaskList(childrenMap.getOrDefault(t.getTaskId(), Collections.emptyList()));

            out.add(item);
        }

        return R.success(out, "查询成功");
    }


    /**
     * 分页列出当前用户相关的任务列表（筛选与非分页一致）
     *
     * 接口：GET `/api/tasks/me/page`
     * 权限：需 JWT 鉴权（仅返回与当前用户相关的任务）
     *
     * 请求参数：
     * - page：页码，默认 1
     * - size：每页大小，默认 10，最大 100
     * - 其余参数与非分页列表一致：taskName、taskStatus、taskPriority、startDueTime、EndDueTime、sortByMode、taskGroupId
     *
     * 返回：`R<Page<TaskSummaryResponse>>`
     * - 分页的扁平任务摘要记录
     */
    @GetMapping("/me/page")
    public R<Page<TaskSummaryResponse>> getTaskPageByUserId(HttpServletRequest request,
                                                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                            @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                            @RequestParam(value = "taskName", required = false) String taskName,
                                                            @RequestParam(value = "taskStatus", required = false) List<Integer> taskStatusList,
                                                            @RequestParam(value = "taskPriority", required = false) List<Integer> taskPriorityList,
                                                            @RequestParam(value = "startDueTime", required = false) java.time.LocalDate startDueTime,
                                                            @RequestParam(value = "EndDueTime", required = false) java.time.LocalDate endDueTime,
                                                            @RequestParam(value = "sortByMode", required = false) Integer sortByMode,
                                                            @RequestParam(value = "taskGroupId", required = false) List<String> taskGroupIds) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        // 基础筛选：taskGroupId（可选列表）、taskName（模糊）、taskStatus（列表）
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<>();
        if (taskGroupIds != null && !taskGroupIds.isEmpty()) {
            qw.in(Task::getTaskGroupId, taskGroupIds);
        }
        String nameLike = CommonUtil.trim(taskName);
        if (nameLike != null && !nameLike.isEmpty()) {
            qw.like(Task::getTaskName, nameLike);
        }
        if (taskStatusList != null && !taskStatusList.isEmpty()) {
            qw.in(Task::getTaskStatus, taskStatusList);
        }
        if (taskPriorityList != null && !taskPriorityList.isEmpty()) {
            qw.in(Task::getTaskPriority, taskPriorityList);
        }
        if (startDueTime != null) {
            qw.ge(Task::getDueTime, startDueTime);
        }
        if (endDueTime != null) {
            qw.le(Task::getDueTime, endDueTime);
        }

        List<Task> tasks = taskService.list(qw);
        if (tasks == null) tasks = Collections.emptyList();
        if (tasks.isEmpty()) {
            Page<TaskSummaryResponse> empty = new Page<>(page == null ? 1 : page, size == null ? 10 : size, 0);
            empty.setRecords(Collections.emptyList());
            return R.success(empty, "查询成功");
        }

        // 预取当前用户与这些任务的关系（固定必要条件：仅保留与我相关）
        Set<String> taskIds = tasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, TaskUserRelation> myRelMap = new HashMap<>();
        if (!taskIds.isEmpty()) {
            List<TaskUserRelation> myRels = taskUserRelationService.list(
                    new LambdaQueryWrapper<TaskUserRelation>()
                            .eq(TaskUserRelation::getUserId, currentUser.getUserId())
                            .in(TaskUserRelation::getTaskId, taskIds)
            );
            for (TaskUserRelation r : myRels) {
                if (r == null || r.getTaskId() == null) continue;
                TaskUserRelation prev = myRelMap.get(r.getTaskId());
                if (prev == null || (prev.getCreateTime() != null && r.getCreateTime() != null && r.getCreateTime().isAfter(prev.getCreateTime()))) {
                    myRelMap.put(r.getTaskId(), r);
                } else if (prev == null) {
                    myRelMap.put(r.getTaskId(), r);
                }
            }
        }

        // 与我相关：当前用户在该任务上存在协助/拥有者关系记录
        List<Task> relatedTasks = tasks.stream()
                .filter(t -> myRelMap.containsKey(t.getTaskId()))
                .collect(Collectors.toList());

        // 排序模式：与 getTaskListByTaskGroup 一致（0=按截止时间；1=按优先级；默认创建时间倒序）
        Integer sortMode = (sortByMode == null) ? -1 : sortByMode;
        if (sortMode == 0) {
            relatedTasks.sort(
                    Comparator.comparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority()).reversed())
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else if (sortMode == 1) {
            relatedTasks.sort(
                    Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority())
                            .reversed()
                            .thenComparing(Task::getDueTime, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else {
            relatedTasks.sort(Comparator.comparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        // 内存分页切片
        int safePage = (page == null || page < 1) ? 1 : page;
        int safeSize = (size == null || size < 1) ? 10 : Math.min(size, 100);
        int fromIndex = (safePage - 1) * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, relatedTasks.size());
        List<Task> pageTasks = (fromIndex >= relatedTasks.size()) ? Collections.emptyList() : relatedTasks.subList(fromIndex, toIndex);

        // 批量查询当前页子任务
        Map<String, List<ChildTask>> childrenMap = new HashMap<>();
        Set<String> pageTaskIds = pageTasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!pageTaskIds.isEmpty()) {
            List<ChildTask> allChildren = childTaskService.list(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, pageTaskIds));
            if (allChildren == null) allChildren = Collections.emptyList();
            for (ChildTask ct : allChildren) {
                if (ct == null || ct.getTaskId() == null) continue;
                List<ChildTask> list = childrenMap.computeIfAbsent(ct.getTaskId(), k -> new ArrayList<>());
                list.add(ct);
            }
            for (Map.Entry<String, List<ChildTask>> e : childrenMap.entrySet()) {
                List<ChildTask> list = e.getValue();
                if (list == null) continue;
                list.sort(Comparator
                        .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        }

        // 任务族名称映射不依赖协助者信息

        // 构建任务族名称映射（跨多个任务族）
        Set<String> pageGroupIds = pageTasks.stream().map(Task::getTaskGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, TaskGroup> groupMap = pageGroupIds.isEmpty() ? Collections.emptyMap()
                : taskGroupService.listByIds(pageGroupIds).stream().collect(Collectors.toMap(TaskGroup::getTaskGroupId, g -> g));

        // 组装分页记录（简化版）
        List<TaskSummaryResponse> records = new ArrayList<>();
        for (Task t : pageTasks) {
            TaskGroup g = t.getTaskGroupId() == null ? null : groupMap.get(t.getTaskGroupId());
            TaskUserRelation myRel = myRelMap.get(t.getTaskId());

            TaskSummaryResponse item = new TaskSummaryResponse();
            item.setTaskId(t.getTaskId());
            item.setTaskGroupId(t.getTaskGroupId());
            item.setTaskGroupName(g == null ? null : g.getGroupName());
            item.setTeamId(t.getTeamId());
            item.setTeamName(null);
            item.setTaskName(t.getTaskName());
            item.setTaskDescription(t.getTaskDescription());
            item.setTaskStatus(t.getTaskStatus());
            item.setTaskPriority(t.getTaskPriority());
            item.setStartTime(t.getStartTime());
            item.setDueTime(t.getDueTime());
            item.setFinishTime(t.getFinishTime());
            Integer ownerFlag = 0;
            if (myRel != null && myRel.getIfOwner() != null) {
                ownerFlag = myRel.getIfOwner();
            }
            item.setIsOwner(ownerFlag);
            item.setChildTaskList(childrenMap.getOrDefault(t.getTaskId(), Collections.emptyList()));

            records.add(item);
        }

        Page<TaskSummaryResponse> outPage = new Page<>(safePage, safeSize);
        outPage.setTotal(relatedTasks.size());
        long pagesNum = (relatedTasks.size() + safeSize - 1L) / safeSize;
        outPage.setPages(pagesNum);
        outPage.setRecords(records);
        return R.success(outPage, "查询成功");
    }


    /**
     * 我的任务日程视图（按年/月）
     *
     * 接口：GET `/api/tasks/me/schedule`
     * 权限：需 JWT 鉴权（仅返回与当前用户有关的任务）
     *
     * 请求参数：
     * - year：年份（必填）
     * - month：月份（1~12，必填）
     * - taskGroupId：任务族ID列表（可选，用于限定范围）
     *
     * 行为：
     * - 取当前用户在任务上的关系（拥有者或协助者），得到相关任务ID集合
     * - 在这些任务中筛选当月截止时间范围（dueTime ∈ [当月1日, 当月末]）
     * - 支持限定任务族ID列表
     * - 排序：startTime 升序 → 优先级降序 → 创建时间降序
     *
     * 返回：`R<List<TaskSummaryResponse>>`
     * - 扁平结构的任务摘要列表，包含任务基本信息、任务族名称、是否拥有者、子任务列表（简要）
     */
    @GetMapping("/me/schedule")
    public R<List<TaskSummaryResponse>> getScheduleTaskListByUserId(HttpServletRequest request,
                                                                    @RequestParam("year") Integer year,
                                                                    @RequestParam("month") Integer month,
                                                                    @RequestParam(value = "taskGroupId", required = false) List<String> taskGroupIds) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        // 参数校验
        if (year == null || month == null) return R.fail("需提供年份与月份");
        if (month < 1 || month > 12) return R.fail("月份需在 1~12 范围内");

        // 计算当月时间范围（包含整月）
        YearMonth ym = YearMonth.of(year, month);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        // 当前用户的任务关系（拥有者或协助者）
        List<TaskUserRelation> myRels = taskUserRelationService.list(
                new LambdaQueryWrapper<TaskUserRelation>()
                        .eq(TaskUserRelation::getUserId, currentUser.getUserId())
        );
        if (myRels == null) myRels = Collections.emptyList();
        Set<String> taskIds = myRels.stream()
                .map(TaskUserRelation::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (taskIds.isEmpty()) {
            return R.success(Collections.emptyList(), "查询成功");
        }

        // 查询当月有协助关系的任务（按 dueTime 截止时间过滤，支持按任务族ID筛选）
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task> tw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Task>()
                        .in(Task::getTaskId, taskIds)
                        .ge(Task::getDueTime, monthStart)
                        .le(Task::getDueTime, monthEnd);
        if (taskGroupIds != null && !taskGroupIds.isEmpty()) {
            tw.in(Task::getTaskGroupId, taskGroupIds);
        }
        List<Task> tasks = taskService.list(tw);
        if (tasks == null) tasks = Collections.emptyList();
        if (tasks.isEmpty()) {
            return R.success(Collections.emptyList(), "查询成功");
        }

        // 构建关系映射（同任务取创建时间较新的关系）
        Map<String, TaskUserRelation> myRelMap = new HashMap<>();
        for (TaskUserRelation r : myRels) {
            if (r == null || r.getTaskId() == null) continue;
            TaskUserRelation prev = myRelMap.get(r.getTaskId());
            if (prev == null || (prev.getCreateTime() != null && r.getCreateTime() != null && r.getCreateTime().isAfter(prev.getCreateTime()))) {
                myRelMap.put(r.getTaskId(), r);
            } else if (prev == null) {
                myRelMap.put(r.getTaskId(), r);
            }
        }

        // 批量子任务查询与排序（与其它列表保持一致）
        Map<String, List<ChildTask>> childrenMap = new HashMap<>();
        Set<String> ids = tasks.stream().map(Task::getTaskId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (!ids.isEmpty()) {
            List<ChildTask> allChildren = childTaskService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, ids));
            if (allChildren == null) allChildren = Collections.emptyList();
            for (ChildTask ct : allChildren) {
                if (ct == null || ct.getTaskId() == null) continue;
                List<ChildTask> list = childrenMap.computeIfAbsent(ct.getTaskId(), k -> new ArrayList<>());
                list.add(ct);
            }
            for (Map.Entry<String, List<ChildTask>> e : childrenMap.entrySet()) {
                List<ChildTask> list = e.getValue();
                if (list == null) continue;
                list.sort(Comparator
                        .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        }

        // 预取任务族名称映射（跨多个任务族）
        Set<String> groupIds = tasks.stream().map(Task::getTaskGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, TaskGroup> groupMap = groupIds.isEmpty() ? Collections.emptyMap()
                : taskGroupService.listByIds(groupIds).stream().collect(Collectors.toMap(TaskGroup::getTaskGroupId, g -> g));

        // 排序：按 startTime 升序，再按优先级与创建时间
        tasks.sort(Comparator
                .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Comparator.comparingInt((Task t) -> t.getTaskPriority() == null ? -1 : t.getTaskPriority()).reversed())
                .thenComparing(Task::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));

        // 组装列表记录
        List<TaskSummaryResponse> out = new ArrayList<>();
        for (Task t : tasks) {
            TaskGroup g = t.getTaskGroupId() == null ? null : groupMap.get(t.getTaskGroupId());
            TaskUserRelation myRel = myRelMap.get(t.getTaskId());

            TaskSummaryResponse item = new TaskSummaryResponse();
            item.setTaskId(t.getTaskId());
            item.setTaskGroupId(t.getTaskGroupId());
            item.setTaskGroupName(g == null ? null : g.getGroupName());
            item.setTeamId(t.getTeamId());
            item.setTeamName(null);
            item.setTaskName(t.getTaskName());
            item.setTaskDescription(t.getTaskDescription());
            item.setTaskStatus(t.getTaskStatus());
            item.setTaskPriority(t.getTaskPriority());
            item.setStartTime(t.getStartTime());
            item.setDueTime(t.getDueTime());
            item.setFinishTime(t.getFinishTime());
            Integer ownerFlag = (myRel != null && myRel.getIfOwner() != null) ? myRel.getIfOwner() : 0;
            item.setIsOwner(ownerFlag);
            item.setChildTaskList(childrenMap.getOrDefault(t.getTaskId(), Collections.emptyList()));

            out.add(item);
        }

        return R.success(out, "查询成功");
    }


    /**
     * 查询任务详情（VO）
     *
     * 接口：GET `/api/tasks/{taskId}`
     * 权限：需为该任务关联任务族的正常成员（userStatus=Normal）
     *
     * 请求参数：
     * - taskId：任务ID（路径参数）
     *
     * 行为：
     * - 解析并校验当前用户与任务族关系
     * - 查询任务基础信息、任务族与团队信息
     * - 查询子任务（索引升序，其次创建时间降序）
     * - 查询附件（仅状态正常的附件）
     * - 融合协助者列表（来自 TaskUserRelation 与 User），并判定当前用户是否拥有者
     *
     * 返回：`R<TaskDetailResponse>`
     * - 包含任务基本字段、任务族/团队名称、协助者列表、子任务列表、附件列表、`isOwner` 标识
     */
    @GetMapping("/{taskId}")
    @Transactional(readOnly = true)
    public R<TaskDetailResponse> getTaskDetail(HttpServletRequest request, @PathVariable("taskId") String taskId) {
        User cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(taskId);
        if (tid == null || tid.isEmpty()) return R.fail("缺少任务ID");
        Task task = taskService.getById(tid);
        if (task == null) return R.notFound("任务不存在");
        String groupId = CommonUtil.trim(task.getTaskGroupId());
        if (groupId == null || groupId.isEmpty()) return R.fail("任务未关联任务族，无法校验权限");
        TaskGroupUserRelation rel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, groupId)
                        .eq(TaskGroupUserRelation::getUserId, cu.getUserId())
                        .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.fail("无权查看该任务的子任务");
        TaskGroup group = taskGroupService.getById(groupId);
        Team team = (task.getTeamId() != null && !task.getTeamId().trim().isEmpty()) ? teamService.getById(task.getTeamId()) : null;
        List<ChildTask> childTasks = childTaskService.list(new LambdaQueryWrapper<ChildTask>().eq(ChildTask::getTaskId, tid));
        if (childTasks == null) childTasks = Collections.emptyList();
        childTasks.sort(Comparator
                .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        List<TaskFile> attachments = taskFileService.list(new LambdaQueryWrapper<TaskFile>()
                .eq(TaskFile::getTaskId, tid)
                .eq(TaskFile::getTaskFileStatus, 1));
        if (attachments == null) attachments = Collections.emptyList();
        List<TaskUserRelation> rels = taskUserRelationService.list(new LambdaQueryWrapper<TaskUserRelation>().eq(TaskUserRelation::getTaskId, tid));
        if (rels == null) rels = Collections.emptyList();
        Set<String> userIds = new HashSet<>();
        for (TaskUserRelation r : rels) {
            if (r != null && r.getUserId() != null) userIds.add(r.getUserId());
        }
        Map<String, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userService.listByIds(userIds).stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(User::getUserId, u -> u));
        List<TaskDetailResponse.TaskHelper> helpers = new ArrayList<>();
        for (TaskUserRelation r : rels) {
            if (r == null) continue;
            User u = userMap.get(r.getUserId());
            TaskDetailResponse.TaskHelper h = new TaskDetailResponse.TaskHelper(
                    r.getTaskUserRelationId(),
                    r.getUserId(),
                    r.getIfOwner() != null && r.getIfOwner().equals(IfOwnerEnum.IS_OWNER.getCode()),
                    u == null ? null : u.getUserName(),
                    u == null ? null : u.getUserEmail(),
                    u == null ? null : u.getUserPhone(),
                    u == null ? null : u.getUserSex(),
                    u == null ? null : u.getUserAvatar(),
                    u == null ? null : u.getUserRemark()
            );
            helpers.add(h);
        }
        Integer isOwner = 0;
        for (TaskUserRelation r : rels) {
            if (r != null && cu.getUserId().equals(r.getUserId()) && r.getIfOwner() != null && r.getIfOwner().equals(IfOwnerEnum.IS_OWNER.getCode())) {
                isOwner = 1;
                break;
            }
        }
        TaskDetailResponse out = new TaskDetailResponse();
        out.setTaskId(task.getTaskId());
        out.setTaskGroupId(task.getTaskGroupId());
        out.setTaskGroupName(group == null ? null : group.getGroupName());
        out.setTeamId(task.getTeamId());
        out.setTeamName(team == null ? null : team.getTeamName());
        out.setTaskName(task.getTaskName());
        out.setTaskDescription(task.getTaskDescription());
        out.setTaskStatus(task.getTaskStatus());
        out.setTaskPriority(task.getTaskPriority());
        out.setStartTime(task.getStartTime());
        out.setDueTime(task.getDueTime());
        out.setFinishTime(task.getFinishTime());
        out.setIsOwner(isOwner);
        out.setTaskHelperList(helpers);
        out.setChildTaskList(childTasks);
        out.setAttachments(attachments);
        return R.success(out, "查询成功");
    }


    /**
     * 列出指定任务的子任务列表
     *
     * 接口：GET `/api/tasks/{taskId}/children`
     * 权限：需为该任务关联任务族的正常成员（userStatus=Normal）
     *
     * 请求参数：
     * - taskId：任务ID（路径参数）
     *
     * 行为：
     * - 校验当前用户是否属于该任务关联的任务族
     * - 查询并返回子任务列表，排序：索引升序 → 创建时间降序
     *
     * 返回：`R<List<ChildTask>>`
     */
    @GetMapping("/{taskId}/children")
    public R<List<ChildTask>> getChildTaskList(HttpServletRequest request, @PathVariable("taskId") String taskId) {
        User cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");

        String tid = CommonUtil.trim(taskId);
        if (tid == null || tid.isEmpty()) {
            return R.fail("缺少任务ID");
        }

        Task task = taskService.getById(tid);
        if (task == null) return R.notFound("任务不存在");

        // 权限校验：当前用户需属于该任务关联的任务族（且状态为正常）
        String groupId = CommonUtil.trim(task.getTaskGroupId());
        if (groupId == null || groupId.isEmpty()) {
            return R.fail("任务未关联任务族，无法校验权限");
        }
        TaskGroupUserRelation rel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, groupId)
                        .eq(TaskGroupUserRelation::getUserId, cu.getUserId())
                        .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) {
            return R.fail("无权查看该任务的子任务");
        }

        List<ChildTask> children = childTaskService.list(new LambdaQueryWrapper<ChildTask>().eq(ChildTask::getTaskId, tid));
        if (children == null) children = Collections.emptyList();
        children.sort(Comparator
                .comparing(ChildTask::getChildTaskIndex, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(ChildTask::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));

        return R.success(children, "查询成功");
    }


    /**
     * 列出指定任务的附件列表
     *
     * 接口：GET `/api/tasks/{taskId}/files`
     * 权限：需为该任务关联任务族的正常成员（userStatus=Normal）
     *
     * 请求参数：
     * - taskId：任务ID（路径参数）
     *
     * 行为：
     * - 校验当前用户是否属于该任务关联的任务族（且状态为正常）
     * - 查询并返回附件列表（仅状态为 1 的有效附件）
     * - 排序：uploadTime 降序 → createTime 降序
     *
     * 返回：`R<List<TaskFile>>`
     */
    @GetMapping("/{taskId}/files")
    public R<List<TaskFile>> getFileTaskList(HttpServletRequest request, @PathVariable("taskId") String taskId) {
        User cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");

        String tid = CommonUtil.trim(taskId);
        if (tid == null || tid.isEmpty()) {
            return R.fail("缺少任务ID");
        }

        Task task = taskService.getById(tid);
        if (task == null) return R.notFound("任务不存在");

        // 权限校验：当前用户需属于该任务关联的任务族（且状态为正常）
        String groupId = CommonUtil.trim(task.getTaskGroupId());
        if (groupId == null || groupId.isEmpty()) {
            return R.fail("任务未关联任务族，无法校验权限");
        }
        TaskGroupUserRelation rel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, groupId)
                        .eq(TaskGroupUserRelation::getUserId, cu.getUserId())
                        .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) {
            return R.fail("无权查看该任务的附件");
        }

        // 仅返回状态为 1 的有效附件，按时间降序
        List<TaskFile> files = taskFileService.list(
                new LambdaQueryWrapper<TaskFile>()
                        .eq(TaskFile::getTaskId, tid)
                        .eq(TaskFile::getTaskFileStatus, 1)
        );
        if (files == null) files = Collections.emptyList();
        files.sort(Comparator
                .comparing(TaskFile::getUploadTime, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(TaskFile::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return R.success(files, "查询成功");
    }

    /**
     * 获取任务审计日志
     *
     * 接口：GET `/api/tasks/{taskId}/audits`
     * 权限：需为该任务关联任务族的正常成员（userStatus=Normal）
     *
     * 请求参数：
     * - taskId：任务ID（路径参数）
     *
     * 行为：
     * - 校验当前用户是否属于该任务关联的任务族（且状态为正常）
     * - 查询并返回审计日志列表
     * - 排序：createTime 降序
     *
     * 返回：`R<List<TaskAudit>>`
     */
    @GetMapping("/{taskId}/audits")
    public R<List<TaskAudit>> getTaskAuditList(HttpServletRequest request, @PathVariable("taskId") String taskId) {
        User cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");
        String tid = CommonUtil.trim(taskId);
        if (tid == null || tid.isEmpty()) {
            return R.fail("缺少任务ID");
        }
        Task task = taskService.getById(tid);
        if (task == null) return R.notFound("任务不存在");
        String groupId = CommonUtil.trim(task.getTaskGroupId());
        if (groupId == null || groupId.isEmpty()) {
            return R.fail("任务未关联任务族，无法校验权限");
        }
        TaskGroupUserRelation rel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, groupId)
                        .eq(TaskGroupUserRelation::getUserId, cu.getUserId())
                        .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) {
            return R.fail("无权查看该任务的审计日志");
        }
        List<TaskAudit> audits = taskAuditService.list(
                new LambdaQueryWrapper<TaskAudit>()
                        .eq(TaskAudit::getTaskId, tid)
        );
        if (audits == null) audits = Collections.emptyList();
        audits.sort(Comparator
                .comparing(TaskAudit::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return R.success(audits, "查询成功");
    }


    // ======================== 内置方法 =====================================
    private String extractObjectNameFromUrl(String url) {
        try {
            if (url == null || url.trim().isEmpty()) return null;
            java.net.URI u = new java.net.URI(url);
            String path = u.getPath(); // 形如 '/bucket/object/path'
            if (path == null) return null;
            if (path.startsWith("/")) path = path.substring(1);
            int idx = path.indexOf('/');
            if (idx < 0) return null; // 无 bucket，异常
            return path.substring(idx + 1);
        } catch (Exception e) {
            return null;
        }
    }
}
