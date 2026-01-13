package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.smartduck.ducktodo.common.enums.ResultCode;
import top.smartduck.ducktodo.common.exception.BusinessException;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.common.constant.SystemConstant;
import top.smartduck.ducktodo.common.enums.TaskGroupStatusEnum;
import top.smartduck.ducktodo.common.enums.UserRoleEnum;
import top.smartduck.ducktodo.common.enums.UserStatusEnum;
import top.smartduck.ducktodo.model.entity.TaskGroup;
import top.smartduck.ducktodo.model.entity.Team;
import top.smartduck.ducktodo.model.entity.TaskGroupUserRelation;
import top.smartduck.ducktodo.model.entity.Task;
import top.smartduck.ducktodo.model.entity.TaskUserRelation;
import top.smartduck.ducktodo.model.entity.TaskFile;
import top.smartduck.ducktodo.model.entity.ChildTask;
import top.smartduck.ducktodo.model.entity.User;
import top.smartduck.ducktodo.model.entity.TaskNode;
import top.smartduck.ducktodo.model.entity.TaskEdge;
import top.smartduck.ducktodo.modelService.*;
import top.smartduck.ducktodo.util.CommonUtil;
import top.smartduck.ducktodo.model.request.CreateTaskGroupRequest;
import top.smartduck.ducktodo.model.request.SwapMyTaskGroupOrderRequest;
import top.smartduck.ducktodo.model.response.MyTaskGroupResponse;
import top.smartduck.ducktodo.model.response.TaskGroupMemberResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 任务族接口（需 JWT 鉴权）
 *
 * 接口概览：
 * - POST /api/taskgroups：创建私有任务族（仅当前登录用户）
 * - PUT /api/taskgroups：更新私有任务族（仅创建者）
 * - DELETE /api/taskgroups/{taskGroupId}：删除私有任务族（仅创建者）
 * - PUT /api/taskgroups/order：交换个人两个任务族的排序（仅关系成员）
 * - PUT /api/taskgroups/{taskGroupId}/members/me/color：修改我的任务族颜色（仅关系成员）
 * - PUT /api/taskgroups/{taskGroupId}/members/me/alias：修改我的任务族别名（仅关系成员）
 * - GET /api/taskgroups/me：列出当前用户相关任务族（按关系排序）
 *
 * 说明：
 * - 私有任务族的 `teamId` 为空字符串
 * - 颜色格式需为 `#xxxxxx`（6 位十六进制）
 * - 角色与状态遵循 `UserRoleEnum` / `UserStatusEnum`
 * - 返回对象采用融合模型 `MyTaskGroupResponse`，便于前端直接渲染
 */
@RestController
@RequestMapping("/api/taskgroups")
public class TaskGroupController {

    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private TaskGroupUserRelationService taskGroupUserRelationService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskUserRelationService taskUserRelationService;

    @Autowired
    private TaskFileService taskFileService;

    @Autowired
    private ChildTaskService childTaskService;

    @Autowired
    private TaskNodeService taskNodeService;

    @Autowired
    private TaskEdgeService taskEdgeService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private UserService userService;

    /**
     * 创建私有任务族
     *
     * <p>接口：`POST /api/taskgroups`</p>
     * <p>权限：匿名 禁止 | 登录用户 允许</p>
     * <p>返回：创建成功的 `TaskGroup`（不含关系字段）</p>
     * <p>请求体：`TaskGroup`（读取 `groupName`、`groupDescription`、`groupStatus`）</p>
     *
     * <p>请求体支持字段：</p>
     * <ul>
     *   <li>`groupName`：必填，长度 ≥ 2</li>
     *   <li>`groupDescription`：选填</li>
     *   <li>`groupStatus`：选填，默认 `NORMAL(1)`</li>
     * </ul>
     *
     * <p>默认行为：</p>
     * <ul>
     *   <li>`teamId` 置为空字符串（表示私有任务族）</li>
     *   <li>初始化与当前用户的成员关系（`owner`、`NORMAL`）</li>
     *   <li>关系属性默认：`groupColor=SystemConstant.DEFAULT_GROUP_COLOR`、`groupAlias=groupName`、`groupIndex=countByUserId+1`</li>
     * </ul>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>读取当前用户，校验登录；</li>
     *   <li>规范化并校验输入字段；</li>
     *   <li>创建 `TaskGroup` 记录（私有任务族，`teamId=""`）；</li>
     *   <li>创建 `TaskGroupUserRelation` 关系记录（`owner`、`NORMAL`）；</li>
     *   <li>返回创建的 `TaskGroup`。</li>
     * </ol>
     *
     * <p>备注：私有任务族无需绑定团队；团队任务族由 `TeamController` 在创建团队时初始化。</p>
     */
    @PostMapping
    public R<TaskGroup> createPrivateTaskGroup(HttpServletRequest request, @RequestBody CreateTaskGroupRequest createTaskGroupRequest) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        // 入参为 TaskGroup：读取必要字段并进行默认值处理
        String groupName = CommonUtil.trim(createTaskGroupRequest.getGroupName());
        String groupDescription = CommonUtil.trim(createTaskGroupRequest.getGroupDescription());
        Integer groupStatus = createTaskGroupRequest.getGroupStatus(); // 默认 1 正常
        String groupColor = createTaskGroupRequest.getGroupColor();

        if (groupName == null || groupName.length() < 2) {
            return R.error("未找到具体任务族名称或任务族名称必须大于两个字符");
        }
        if (groupStatus == null || !CommonUtil.inEnumCodes(TaskGroupStatusEnum.values(), groupStatus)) {
            groupStatus = TaskGroupStatusEnum.NORMAL.getCode();
        }
        if (groupColor == null || groupColor.isEmpty()){
            groupColor = SystemConstant.DEFAULT_GROUP_COLOR;
        }

        LocalDateTime now = LocalDateTime.now();

        // 创建任务族
        TaskGroup taskGroup = new TaskGroup();
        taskGroup.setTeamId("");
        taskGroup.setGroupName(groupName);
        taskGroup.setGroupDescription(groupDescription);
        taskGroup.setGroupStatus(groupStatus);
        taskGroup.setCreateTime(now);
        taskGroup.setUpdateTime(now);

        if(!taskGroupService.save(taskGroup)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建私人任务族失败");
        }

        // 创建当前用户与任务族关系
        TaskGroupUserRelation ownerGroupRelation = new TaskGroupUserRelation();
        ownerGroupRelation.setTaskGroupId(taskGroup.getTaskGroupId());
        ownerGroupRelation.setUserId(currentUser.getUserId());
        ownerGroupRelation.setUserRole(UserRoleEnum.OWNER.getCode());
        ownerGroupRelation.setUserStatus(UserStatusEnum.NORMAL.getCode());
        ownerGroupRelation.setGroupIndex(taskGroupUserRelationService.countByUserId(currentUser.getUserId()) + 1);
        ownerGroupRelation.setGroupAlias(groupName);
        ownerGroupRelation.setGroupColor(groupColor);
        ownerGroupRelation.setJoinTime(LocalDateTime.now());
        ownerGroupRelation.setUpdateTime(LocalDateTime.now());

        if(!taskGroupUserRelationService.save(ownerGroupRelation)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建团队所属任务族与所有者关系失败");
        }

        return R.success(taskGroup, "创建成功");
    }



    /**
     * 更新私有任务族（仅创建者）
     *
     * <p>接口：`PUT /api/taskgroups`</p>
     * <p>权限：仅任务族创建者允许；匿名 禁止</p>
     * <p>请求体：`TaskGroup`（读取 `taskGroupId`、`groupName`、`groupDescription`、`groupStatus`）</p>
     * <p>返回：更新后的 `TaskGroup`</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户登录；校验 `taskGroupId` 存在；</li>
     *   <li>仅允许更新私有任务族（`teamId` 为空）；</li>
     *   <li>校验当前用户为任务族创建者且为正常状态成员；</li>
     *   <li>校验与规范输入字段后进行更新；</li>
     *   <li>返回更新结果。</li>
     * </ol>
     */
    @PutMapping
    public R<TaskGroup> updatePrivateTaskGroup(HttpServletRequest request, @RequestBody TaskGroup taskGroup) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String taskGroupId = CommonUtil.trim(taskGroup.getTaskGroupId());
        if (taskGroupId == null || taskGroupId.isEmpty()) {
            return R.fail("需提供 taskGroupId");
        }

        TaskGroup group = taskGroupService.getById(taskGroupId);
        if (group == null) return R.notFound("任务族不存在");

        // 仅允许更新私有任务族（无 teamId）
        String tgTeamId = CommonUtil.trim(group.getTeamId());
        if (tgTeamId != null && !tgTeamId.isEmpty()) {
            return R.fail("仅允许更新私有任务族（无 teamId）");
        }

        TaskGroupUserRelation myRel = taskGroupUserRelationService.getOne(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (myRel == null || !UserRoleEnum.OWNER.getCode().equals(myRel.getUserRole())) return R.fail("仅任务族创建者可更新任务族");

        String groupName = CommonUtil.trim(taskGroup.getGroupName());
        String groupDescription = CommonUtil.trim(taskGroup.getGroupDescription());
        Integer groupStatus = taskGroup.getGroupStatus();

        if (groupName == null || groupName.length() < 2) {
            return R.error("任务族名称必须大于等于两个字符");
        }
        if (groupStatus == null) {
            groupStatus = TaskGroupStatusEnum.NORMAL.getCode();
        }

        group.setGroupName(groupName);
        group.setGroupDescription(groupDescription);
        group.setGroupStatus(groupStatus);
        group.setUpdateTime(LocalDateTime.now());

        if(!taskGroupService.updateById(group)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新任务族失败");
        }

        TaskGroup updated = taskGroupService.getById(taskGroupId);
        return R.success(updated, "更新成功");
    }

    // ============ 删除任务族（仅创建者）===========
    @DeleteMapping("/{taskGroupId}")
    @Transactional(rollbackFor = Exception.class)
    public R<?> deletePrivateTaskGroup(HttpServletRequest request, @PathVariable("taskGroupId") String taskGroupId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        taskGroupId = CommonUtil.trim(taskGroupId);

        TaskGroup group = taskGroupService.getById(taskGroupId);
        if (group == null) return R.notFound("任务族不存在");
        // 仅允许删除私有任务族（无 teamId）
        String tgTeamId = CommonUtil.trim(group.getTeamId());
        if (tgTeamId != null && !tgTeamId.isEmpty()) {
            return R.fail("仅允许删除私有任务族（无 teamId）");
        }
        // 禁止删除默认任务族
        String groupName = CommonUtil.trim(group.getGroupName());
        if ("默认任务族".equals(groupName)) {
            return R.fail("默认任务族不可删除");
        }
        TaskGroupUserRelation myRel = taskGroupUserRelationService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                .eq(TaskGroupUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (myRel == null || !UserRoleEnum.OWNER.getCode().equals(myRel.getUserRole())) return R.fail("仅任务族创建者可删除任务族");

        // 1) 查询任务族下的所有任务
        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>().eq(Task::getTaskGroupId, taskGroupId));
        List<String> taskIds = new ArrayList<>();
        for (Task t : tasks) taskIds.add(t.getTaskId());

        // 2) 删除与该任务族相关的子任务、节点与边
        List<ChildTask> childTasks = taskIds.isEmpty()
                ? Collections.emptyList()
                : childTaskService.list(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, taskIds));
        List<String> childTaskIds = new ArrayList<>();
        for (ChildTask ct : childTasks) childTaskIds.add(ct.getChildTaskId());

        LambdaQueryWrapper<TaskNode> nodeQw = new LambdaQueryWrapper<TaskNode>()
                .eq(TaskNode::getTaskGroupId, taskGroupId);
        if (!taskIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getTaskId, taskIds));
        if (!childTaskIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getChildTaskId, childTaskIds));
        List<TaskNode> nodes = taskNodeService.list(nodeQw);
        Set<String> nodeIds = new HashSet<>();
        for (TaskNode n : nodes) nodeIds.add(n.getTaskNodeId());
        if (!nodeIds.isEmpty()) {
            boolean okEdges = taskEdgeService.remove(new LambdaQueryWrapper<TaskEdge>()
                    .in(TaskEdge::getSourceNodeId, nodeIds).or().in(TaskEdge::getTargetNodeId, nodeIds));
            if (!okEdges) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除任务链失败");
            }
            boolean okNodes = taskNodeService.remove(nodeQw);
            if (!okNodes) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除任务节点失败");
            }
        }

        // 3) 删除任务协助者关系与任务附件
        if (!taskIds.isEmpty()) {
            long helperCount = taskUserRelationService.count(new LambdaQueryWrapper<TaskUserRelation>().in(TaskUserRelation::getTaskId, taskIds));
            boolean helperRemoved = (helperCount == 0) || taskUserRelationService.remove(new LambdaQueryWrapper<TaskUserRelation>().in(TaskUserRelation::getTaskId, taskIds));
            if (helperCount > 0 && !helperRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除任务与用户关系失败");
            }

            long fileCount = taskFileService.count(new LambdaQueryWrapper<TaskFile>().in(TaskFile::getTaskId, taskIds));
            boolean fileRemoved = (fileCount == 0) || taskFileService.remove(new LambdaQueryWrapper<TaskFile>().in(TaskFile::getTaskId, taskIds));
            if (fileCount > 0 && !fileRemoved) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return R.error("删除任务文件失败");
            }
        }

        // 4) 删除子任务
        List<String> childIds = childTaskIds;

        long childCount = childIds.size();
        boolean childRemoved = (childCount == 0) || childTaskService.remove(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getChildTaskId, childIds));
        if (childCount > 0 && !childRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除子任务失败");
        }

        // 5) 删除任务
        long taskCount = tasks.size();
        boolean taskRemoved = (taskCount == 0) || taskService.remove(new LambdaQueryWrapper<Task>().eq(Task::getTaskGroupId, taskGroupId));
        if (taskCount > 0 && !taskRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除任务失败");
        }

        // 6) 删除任务族成员关系
        long relCount = taskGroupUserRelationService.count(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId));
        boolean relRemoved = (relCount == 0) || taskGroupUserRelationService.remove(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId));
        if (relCount > 0 && !relRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除任务族用户关系失败");
        }

        // 7) 删除任务族
        boolean groupRemoved = taskGroupService.removeById(taskGroupId);
        if (!groupRemoved) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return R.error("删除任务族失败");
        }

        return R.success(null, "删除成功");
    }

    /**
     * 交换个人两个任务族的排序（当前用户维度）
     *
     * <p>接口：`PUT /api/taskgroups/order`</p>
     * <p>权限：需为两个目标任务族的正常成员（owner/manager/member 均可）</p>
     * <p>请求体：`SwapMyTaskGroupOrderRequest`（读取 `taskGroupIdA`、`taskGroupIdB`）</p>
     * <p>返回：按最新关系顺序的融合列表 `List<MyTaskGroupResponse>`</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户，验证两个任务族ID有效且不重复；</li>
     *   <li>校验当前用户在两个任务族的关系均存在；</li>
     *   <li>交换关系的 `groupIndex` 并更新；</li>
     *   <li>按最新关系顺序返回融合后的 `MyTaskGroupResponse` 列表。</li>
     * </ol>
     */
    @PutMapping("/order")
    public R<List<MyTaskGroupResponse>> swapMyTaskGroupOrder(HttpServletRequest request, @RequestBody SwapMyTaskGroupOrderRequest swapMyTaskGroupOrderRequest) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String groupIdA = CommonUtil.trim(swapMyTaskGroupOrderRequest.getTaskGroupIdA());
        String groupIdB = CommonUtil.trim(swapMyTaskGroupOrderRequest.getTaskGroupIdB());
        System.out.println("groupIdA: " + groupIdA);
        System.out.println("groupIdB: " + groupIdB);
        if (groupIdA == null || groupIdB == null || groupIdA.equals(groupIdB)) {
            return R.fail("需提供两个不同的任务族ID");
        }

        // 查询两个关系记录
        TaskGroupUserRelation relA = taskGroupUserRelationService.getOne(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, groupIdA)
                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId()), false);
        TaskGroupUserRelation relB = taskGroupUserRelationService.getOne(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, groupIdB)
                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId()), false);

        if (relA == null || relB == null) {
            return R.notFound("排序交换失败：至少一个任务族不存在或未与当前用户建立关系");
        }

        // 处理空索引：使用当前用户最大索引+1作为填充
        Integer idxA = relA.getGroupIndex();
        Integer idxB = relB.getGroupIndex();
        Integer max = taskGroupUserRelationService.list(new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId()))
                .stream()
                .map(TaskGroupUserRelation::getGroupIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        if (idxA == null && idxB == null) {
            idxA = max + 1;
            idxB = max + 2;
        } else if (idxA == null) {
            idxA = max + 1;
        } else if (idxB == null) {
            idxB = max + 1;
        }

        // 交换索引并保存
        relA.setGroupIndex(idxB);
        relB.setGroupIndex(idxA);
        relA.setUpdateTime(LocalDateTime.now());
        relB.setUpdateTime(LocalDateTime.now());

        boolean ok = taskGroupUserRelationService.updateById(relA) && taskGroupUserRelationService.updateById(relB);
        if (!ok) return R.error("交换排序失败");

        // 交换完成后，按最新关系顺序返回融合的任务族列表
        List<TaskGroupUserRelation> rels = taskGroupUserRelationService.list(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                .orderByAsc(TaskGroupUserRelation::getGroupIndex)
                .orderByAsc(TaskGroupUserRelation::getJoinTime));
        Set<String> groupIds = new HashSet<>();
        for (TaskGroupUserRelation r : rels) {
            if (r.getTaskGroupId() != null) groupIds.add(r.getTaskGroupId());
        }
        List<TaskGroup> allGroups = groupIds.isEmpty() ? Collections.emptyList() : taskGroupService.listByIds(groupIds);
        Map<String, TaskGroup> id2Group = new HashMap<>();
        for (TaskGroup g : allGroups) id2Group.put(g.getTaskGroupId(), g);

        // 关联团队映射（仅团队绑定的任务族）
        Set<String> teamIds = new HashSet<>();
        for (TaskGroup g : allGroups) {
            String tid = g.getTeamId();
            if (tid != null && !tid.trim().isEmpty()) teamIds.add(tid);
        }
        Map<String, Team> teamMap = teamIds.isEmpty()
                ? Collections.emptyMap()
                : teamService.listByIds(teamIds).stream().collect(HashMap::new, (m, t) -> m.put(t.getTeamId(), t), HashMap::putAll);

        // 构造融合输出
        List<MyTaskGroupResponse> out = new ArrayList<>();
        for (TaskGroupUserRelation r : rels) {
            TaskGroup g = id2Group.get(r.getTaskGroupId());
            if (g == null) continue;
            boolean isPrivate = (g.getTeamId() == null || g.getTeamId().trim().isEmpty());
            Team t = isPrivate ? null : teamMap.get(g.getTeamId());

            MyTaskGroupResponse item = new MyTaskGroupResponse();
            item.setTaskGroupId(g.getTaskGroupId());
            item.setTeamId(g.getTeamId());
            item.setGroupName(g.getGroupName());
            item.setGroupDescription(g.getGroupDescription());
            item.setGroupStatus(g.getGroupStatus());
            item.setUserRole(r.getUserRole());
            item.setGroupAlias(r.getGroupAlias());
            item.setGroupColor(r.getGroupColor());
            item.setGroupIndex(r.getGroupIndex());
            item.setJoinTime(r.getJoinTime());
            item.setUpdateTime(r.getUpdateTime());
            item.setPrivate(isPrivate);
            if (t != null) {
                item.setTeamName(t.getTeamName());
                item.setTeamDescription(t.getTeamDescription());
                item.setTeamAvatar(t.getTeamAvatar());
                item.setTeamStatus(t.getTeamStatus());
            }
            out.add(item);
        }
        return R.success(out, "交换成功");
    }

    /**
     * 修改我的任务族颜色（关系维度，仅当前用户）
     *
     * <p>接口：`PUT /api/taskgroups/{taskGroupId}/members/me/color`</p>
     * <p>权限：需为该任务族的关系成员（owner/manager/member 均可）</p>
     * <p>约束：颜色需为 `#xxxxxx`（6 位十六进制）</p>
     * <p>请求体：`TaskGroupUserRelation`（仅读取 `groupColor`）</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户与任务族ID；</li>
     *   <li>校验颜色格式；</li>
     *   <li>查询关系并更新颜色（幂等处理：相同颜色直接返回）；</li>
     *   <li>返回融合后的 `MyTaskGroupResponse`。</li>
     * </ol>
     */
    @PutMapping("/{taskGroupId}/members/me/color")
    public R<MyTaskGroupResponse> updateMyTaskGroupColor(HttpServletRequest request,
                                                         @PathVariable("taskGroupId") String taskGroupId,
                                                         @RequestBody TaskGroupUserRelation taskGroupUserRelation) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        taskGroupId = CommonUtil.trim(taskGroupId);
        // 仅读取请求体中的 groupColor
        String color = taskGroupUserRelation == null ? null : CommonUtil.trim(taskGroupUserRelation.getGroupColor());

        // 颜色校验：必须为 #xxxxxx（6位16进制）
        if (color == null || !SystemConstant.HEX_COLOR_6.matcher(color).matches()) {
            return R.fail("颜色格式无效，需为 #xxxxxx（6位十六进制）");
        }
        if (taskGroupId == null || taskGroupId.isEmpty()) {
            return R.fail("需提供 taskGroupId");
        }

        // 获取当前用户在该任务族下的关系
        TaskGroupUserRelation rel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                        .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId()),
                false);
        if (rel == null) return R.notFound("未找到该任务族的用户关系");

        // 幂等：若颜色一致，直接返回融合对象
        if (Objects.equals(color, rel.getGroupColor())) {
            TaskGroup g = taskGroupService.getById(taskGroupId);
            boolean isPrivate = (g.getTeamId() == null || g.getTeamId().trim().isEmpty());
            Team t = isPrivate ? null : teamService.getById(g.getTeamId());
            MyTaskGroupResponse item = new MyTaskGroupResponse();
            item.setTaskGroupId(g.getTaskGroupId());
            item.setTeamId(g.getTeamId());
            item.setGroupName(g.getGroupName());
            item.setGroupDescription(g.getGroupDescription());
            item.setGroupStatus(g.getGroupStatus());
            item.setUserRole(rel.getUserRole());
            item.setGroupAlias(rel.getGroupAlias());
            item.setGroupColor(rel.getGroupColor());
            item.setGroupIndex(rel.getGroupIndex());
            item.setJoinTime(rel.getJoinTime());
            item.setUpdateTime(rel.getUpdateTime());
            item.setPrivate(isPrivate);
            if (t != null) {
                item.setTeamName(t.getTeamName());
                item.setTeamDescription(t.getTeamDescription());
                item.setTeamAvatar(t.getTeamAvatar());
                item.setTeamStatus(t.getTeamStatus());
            }
            return R.success(item, "颜色已更新");
        }

        // 更新颜色并返回融合对象
        rel.setGroupColor(color);
        rel.setUpdateTime(LocalDateTime.now());
        boolean ok = taskGroupUserRelationService.updateById(rel);
        if (!ok) return R.error("颜色更新失败");
        TaskGroup g = taskGroupService.getById(taskGroupId);
        boolean isPrivate = (g.getTeamId() == null || g.getTeamId().trim().isEmpty());
        Team t = isPrivate ? null : teamService.getById(g.getTeamId());
        MyTaskGroupResponse item = new MyTaskGroupResponse();
        item.setTaskGroupId(g.getTaskGroupId());
        item.setTeamId(g.getTeamId());
        item.setGroupName(g.getGroupName());
        item.setGroupDescription(g.getGroupDescription());
        item.setGroupStatus(g.getGroupStatus());
        item.setUserRole(rel.getUserRole());
        item.setGroupAlias(rel.getGroupAlias());
        item.setGroupColor(rel.getGroupColor());
        item.setGroupIndex(rel.getGroupIndex());
        item.setJoinTime(rel.getJoinTime());
        item.setUpdateTime(rel.getUpdateTime());
        item.setPrivate(isPrivate);
        if (t != null) {
            item.setTeamName(t.getTeamName());
            item.setTeamDescription(t.getTeamDescription());
            item.setTeamAvatar(t.getTeamAvatar());
            item.setTeamStatus(t.getTeamStatus());
        }
        return R.success(item, "颜色已更新");
    }

    /**
     * 修改我的任务族别名（关系维度，仅当前用户）
     *
     * <p>接口：`PUT /api/taskgroups/{taskGroupId}/members/me/alias`</p>
     * <p>权限：需为该任务族的关系成员（owner/manager/member 均可）</p>
     * <p>请求体：`TaskGroupUserRelation`（仅读取 `groupAlias`）</p>
     * <p>返回：融合对象 `MyTaskGroupResponse`</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户与任务族ID；</li>
     *   <li>校验别名非空与长度上限；</li>
     *   <li>更新关系的别名并返回融合对象；</li>
     * </ol>
     */
    @PutMapping("/{taskGroupId}/members/me/alias")
    public R<MyTaskGroupResponse> updateMyGroupAlias(HttpServletRequest request,
                                                     @PathVariable("taskGroupId") String taskGroupId,
                                                     @RequestBody TaskGroupUserRelation taskGroupUserRelation) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        taskGroupId = CommonUtil.trim(taskGroupId);
        // 仅读取请求体中的 groupAlias
        String alias = taskGroupUserRelation == null ? null : CommonUtil.trim(taskGroupUserRelation.getGroupAlias());

        if (taskGroupId == null || taskGroupId.isEmpty()) {
            return R.fail("需提供 taskGroupId");
        }
        // 基本校验：别名非空且长度限制
        if (alias == null || alias.isEmpty()) {
            return R.fail("别名不能为空");
        }
        if (alias.length() > 64) {
            return R.fail("别名长度过长（最多64字符）");
        }

        // 获取当前用户在该任务族下的关系
        TaskGroupUserRelation rel = taskGroupUserRelationService.getOne(
                new LambdaQueryWrapper<TaskGroupUserRelation>()
                        .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                        .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId()),
                false);
        if (rel == null) return R.notFound("未找到该任务族的用户关系");

        rel.setGroupAlias(alias);
        rel.setUpdateTime(LocalDateTime.now());
        boolean ok = taskGroupUserRelationService.updateById(rel);
        if (!ok) return R.error("别名更新失败");

        TaskGroup g = taskGroupService.getById(taskGroupId);
        boolean isPrivate = (g.getTeamId() == null || g.getTeamId().trim().isEmpty());
        Team t = isPrivate ? null : teamService.getById(g.getTeamId());
        MyTaskGroupResponse item = new MyTaskGroupResponse();
        item.setTaskGroupId(g.getTaskGroupId());
        item.setTeamId(g.getTeamId());
        item.setGroupName(g.getGroupName());
        item.setGroupDescription(g.getGroupDescription());
        item.setGroupStatus(g.getGroupStatus());
        item.setUserRole(rel.getUserRole());
        item.setGroupAlias(rel.getGroupAlias());
        item.setGroupColor(rel.getGroupColor());
        item.setGroupIndex(rel.getGroupIndex());
        item.setJoinTime(rel.getJoinTime());
        item.setUpdateTime(rel.getUpdateTime());
        item.setPrivate(isPrivate);
        if (t != null) {
            item.setTeamName(t.getTeamName());
            item.setTeamDescription(t.getTeamDescription());
            item.setTeamAvatar(t.getTeamAvatar());
            item.setTeamStatus(t.getTeamStatus());
        }
        return R.success(item, "别名已更新");
    }

    /**
     * 列出当前用户相关的所有任务族
     *
     * <p>接口：`GET /api/taskgroups/me`</p>
     * <p>权限：登录用户允许（仅限本人）</p>
     * <p>返回：融合列表 `List<MyTaskGroupResponse>`</p>
     *
     * <p>说明：</p>
     * - 基于 `TaskGroupUserRelation` 关系表，按 `groupIndex` 升序，其次按 `joinTime` 升序
     * - 私有任务族的 `teamId` 为空字符串，相关团队信息为空
     */
    @GetMapping("/me")
    public R<List<MyTaskGroupResponse>> getMyTaskGroupList(HttpServletRequest request) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        List<TaskGroupUserRelation> rels = taskGroupUserRelationService.list(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                .orderByAsc(TaskGroupUserRelation::getGroupIndex)
                .orderByAsc(TaskGroupUserRelation::getJoinTime));

        // 批量查询任务族详情
        Set<String> groupIds = new HashSet<>();
        for (TaskGroupUserRelation r : rels) {
            if (r.getTaskGroupId() != null) groupIds.add(r.getTaskGroupId());
        }
        List<TaskGroup> groups = groupIds.isEmpty() ? Collections.emptyList() : taskGroupService.listByIds(groupIds);
        Map<String, TaskGroup> groupMap = new HashMap<>();
        for (TaskGroup g : groups) {
            groupMap.put(g.getTaskGroupId(), g);
        }

        // 批量查询关联团队信息（排除私有任务族：teamId 为空或空串）
        Set<String> teamIds = new HashSet<>();
        for (TaskGroup g : groups) {
            String tid = g.getTeamId();
            if (tid != null && !tid.isEmpty()) {
                teamIds.add(tid);
            }
        }
        List<Team> teams = teamIds.isEmpty() ? Collections.emptyList() : teamService.listByIds(teamIds);
        Map<String, Team> teamMap = new HashMap<>();
        for (Team t : teams) {
            teamMap.put(t.getTeamId(), t);
        }

        // 组装输出（按关系排序返回）
        List<MyTaskGroupResponse> out = new ArrayList<>();
        for (TaskGroupUserRelation r : rels) {
            TaskGroup g = groupMap.get(r.getTaskGroupId());
            if (g == null) continue; // 若任务族不存在，跳过
            boolean isPrivate = (g.getTeamId() == null || g.getTeamId().isEmpty());
            Team t = isPrivate ? null : teamMap.get(g.getTeamId());

            MyTaskGroupResponse item = new MyTaskGroupResponse();
            // 任务族信息
            item.setTaskGroupId(g.getTaskGroupId());
            item.setTeamId(g.getTeamId());
            item.setGroupName(g.getGroupName());
            item.setGroupDescription(g.getGroupDescription());
            item.setGroupStatus(g.getGroupStatus());

            // 关系信息
            item.setUserRole(r.getUserRole());
            item.setGroupAlias(r.getGroupAlias());
            item.setGroupColor(r.getGroupColor());
            item.setGroupIndex(r.getGroupIndex());
            item.setJoinTime(r.getJoinTime());
            item.setUpdateTime(r.getUpdateTime());

            // 派生信息
            item.setPrivate(isPrivate);

            // 团队融合信息
            if (t != null) {
                item.setTeamName(t.getTeamName());
                item.setTeamDescription(t.getTeamDescription());
                item.setTeamAvatar(t.getTeamAvatar());
                item.setTeamStatus(t.getTeamStatus());
            }
            out.add(item);
        }

        return R.success(out);
    }

    /**
     * 查询单个任务族下的所有成员
     *
     * <p>接口：`GET /api/taskgroups/{taskGroupId}/members`</p>
     * <p>权限：需为该任务族的正常成员（owner/manager/member 均可）</p>
     * <p>返回：融合列表 `List<TaskGroupMemberResponse>`（按用户名升序，仅返回正常成员）</p>
     */
    @GetMapping("/{taskGroupId}/members")
    public R<List<TaskGroupMemberResponse>> getTaskGroupMembers(HttpServletRequest request, @PathVariable("taskGroupId") String taskGroupId) {
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
        TaskGroupUserRelation viewerRel = taskGroupUserRelationService.getOne(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                .eq(TaskGroupUserRelation::getUserStatus, top.smartduck.ducktodo.common.enums.UserStatusEnum.NORMAL.getCode()), false);
        if (viewerRel == null) {
            return R.fail("无权限查看该任务族成员");
        }
        List<TaskGroupUserRelation> rels = taskGroupUserRelationService.list(new LambdaQueryWrapper<TaskGroupUserRelation>()
                .eq(TaskGroupUserRelation::getTaskGroupId, taskGroupId)
                .eq(TaskGroupUserRelation::getUserStatus, top.smartduck.ducktodo.common.enums.UserStatusEnum.NORMAL.getCode()));
        if (rels == null || rels.isEmpty()) {
            return R.success(java.util.Collections.emptyList(), "查询成功");
        }
        java.util.Set<String> userIds = new java.util.HashSet<>();
        for (TaskGroupUserRelation r : rels) {
            if (r.getUserId() != null) userIds.add(r.getUserId());
        }
        java.util.List<User> users = userService.list(new LambdaQueryWrapper<User>().in(User::getUserId, userIds));
        if (users == null) users = java.util.Collections.emptyList();
        users.sort(java.util.Comparator.comparing(User::getUserName, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        java.util.Map<String, TaskGroupUserRelation> relMap = new java.util.HashMap<>();
        for (TaskGroupUserRelation r : rels) {
            if (r.getUserId() != null) relMap.put(r.getUserId(), r);
        }
        java.util.List<TaskGroupMemberResponse> out = new java.util.ArrayList<>();
        for (User u : users) {
            TaskGroupUserRelation r = relMap.get(u.getUserId());
            if (r == null) continue;
            out.add(new TaskGroupMemberResponse(
                    r.getTaskGroupUserRelationId(),
                    r.getTaskGroupId(),
                    r.getUserRole(),
                    r.getUserStatus(),
                    r.getGroupIndex(),
                    r.getGroupColor(),
                    r.getGroupAlias(),
                    r.getJoinTime(),
                    r.getUpdateTime(),
                    u.getUserId(),
                    u.getUserName(),
                    u.getUserEmail(),
                    u.getUserPhone(),
                    u.getUserSex(),
                    u.getUserAvatar(),
                    u.getUserRemark(),
                    u.getLastLoginTime()
            ));
        }
        return R.success(out, "查询成功");
    }

}
