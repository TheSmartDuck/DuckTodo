package top.smartduck.ducktodo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import top.smartduck.ducktodo.common.enums.*;
import top.smartduck.ducktodo.common.exception.BusinessException;
import top.smartduck.ducktodo.common.result.R;
import top.smartduck.ducktodo.model.entity.*;
import top.smartduck.ducktodo.modelService.*;
import top.smartduck.ducktodo.util.CommonUtil;
import top.smartduck.ducktodo.common.constant.SystemConstant;
import top.smartduck.ducktodo.model.request.CreateTeamRequest;
import top.smartduck.ducktodo.model.request.SwapMyTeamOrderRequest;
import top.smartduck.ducktodo.model.response.MyTeamResponse;
 
import top.smartduck.ducktodo.model.response.TeamMemberResponse;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 团队相关接口（需 JWT 鉴权）。
 *
 * <p>接口列表：</p>
 * <ol>
 *   <li><b>POST /api/teams</b>：创建团队，初始化任务族与成员关系，并可邀请成员。</li>
 *   <li><b>POST /api/teams/{teamId}/members</b>：邀请成员加入团队（支持 manager/member 角色）。</li>
 *   <li><b>DELETE /api/teams/{teamId}</b>：删除团队及其关联数据（任务族、任务、成员、附件、节点、链）。</li>
 *   <li><b>DELETE /api/teams/{teamId}/members/me</b>：当前用户退出团队（所有者不可退出）。</li>
 *   <li><b>DELETE /api/teams/{teamId}/members/{userId}</b>：删除指定成员（不可删除团队所有者）。</li>
 *   <li><b>PUT /api/teams</b>：修改团队信息（名称、描述、状态）。</li>
 *   <li><b>PUT /api/teams/{teamId}/invites/me/accept</b>：接受加入邀请（从邀请中转为正常成员）。</li>
 *   <li><b>PUT /api/teams/{teamId}/invites/me/reject</b>：拒绝加入邀请（标记为已拒绝）。</li>
 *   <li><b>PUT /api/teams/{teamId}/members/{userId}/role</b>：修改成员角色（仅 manager/member）。</li>
 *   <li><b>PUT /api/teams/order</b>：交换个人两个团队的排序。</li>
 *   <li><b>PUT /api/teams/{teamId}/members/me/color</b>：修改个人在该团队的显示颜色。</li>
 *   <li><b>GET /api/teams/me/invites</b>：本人被邀请团队分页（支持邀请状态与 teamName 模糊）。</li>
 *   <li><b>GET /api/teams/me</b>：查询本人相关团队（正常成员、团队进行中，按 teamIndex 升序）。</li>
 *   <li><b>GET /api/teams/{teamId}</b>：查询团队基础信息（需为该团队正常成员）。</li>
 *   <li><b>GET /api/teams/{teamId}/dashboard</b>：团队数据大屏（骨架占位）。</li>
 *   <li><b>GET /api/teams/{teamId}/members</b>：团队成员分页（支持用户名模糊、角色过滤、邀请状态）。</li>
 * </ol>
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private static final Logger log = LoggerFactory.getLogger(TeamController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private TeamUserRelationService teamUserRelationService;

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


    /**
     * 创建团队
     *
     * <p>执行流程：</p>
     * <ol>
     *     <li><b>基础非空校验</b>：检查必填参数是否存在。</li>
     *     <li><b>格式校验</b>：校验团队名称、团队状态。</li>
     *     <li><b>唯一性检查</b>：检查团队名称是否已被注册。</li>
     *     <li><b>创建团队基础信息</b>：生成 UUID，初始化 Team 对象并保存。</li>
     *     <li><b>创建团队所属任务族基础信息</b>：生成 UUID，初始化 TaskGroup 对象并保存。</li>
     *     <li><b>创建团队与所有者关系</b>：创建一个 TeamUserRelationship 对象，将用户设为所有者，状态为正常。</li>
     *     <li><b>创建团队所属任务族与所有者关系</b>：创建一个 TaskGroupUserRelationship 对象，将用户设为所有者，状态为正常。</li>
     *     <li><b>创建团队与被邀请成员的邀请关系</b>：基于被邀请成员Id创建TeamUserRelationship，将用户设为指定角色，状态为邀请中。</li>
     *     <li><b>返回结果</b>：返回完成创建的Team响应对象。</li>
     * </ol>
     * <p>注意：整个过程在事务中执行，任一步骤失败将触发自动回滚。</p>
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R<Team> createTeam(HttpServletRequest request, @RequestBody CreateTeamRequest createTeamRequest) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String teamName = CommonUtil.trim(createTeamRequest.getTeamName());
        String teamDescription = CommonUtil.trim(createTeamRequest.getTeamDescription());
        String teamAvatar = CommonUtil.trim(createTeamRequest.getTeamAvatar());
        Integer teamStatus = createTeamRequest.getTeamStatus(); // 团队状态，0-已禁用，1-进行中，2-已结束

        // 1. 基础非空校验
        if (teamName == null || teamName.trim().isEmpty()) {
            return R.fail("团队名不能为空");
        }

        // 2. 格式校验
        if (teamName == null || teamName.length() < 2) {
            return R.fail("团队名称至少 2 位");
        }
        if (teamStatus == null) {
            teamStatus = TeamStatusEnum.IN_PROGRESS.getCode();
        } else if (!CommonUtil.inEnumCodes(TeamStatusEnum.values(), teamStatus)) {
            return R.fail("非法团队状态");
        }

        // 3. 唯一性检测
        if(teamService.count(new LambdaQueryWrapper<Team>().eq(Team::getTeamName, teamName)) > 0){
            return R.fail("团队名称已经存在");
        }

        // 4. 创建团队基础信息
        Team team = new Team();
        team.setTeamName(teamName);
        team.setTeamDescription(teamDescription.isEmpty()? "" : teamDescription);
        team.setTeamAvatar(teamAvatar.isEmpty()? "" : teamAvatar);
        team.setTeamStatus(teamStatus);
        team.setCreateTime(LocalDateTime.now());
        team.setUpdateTime(LocalDateTime.now());

        if(!teamService.save(team)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建团队失败");
        }

        // 5. 创建团队所属任务族基础信息
        String taskGroupName = teamName + "的任务族";
        TaskGroup taskGroup  = new TaskGroup();
        taskGroup.setTeamId(team.getTeamId());
        taskGroup.setGroupName(taskGroupName);
        taskGroup.setGroupDescription("基于团队项目：" + teamName + "构建的任务族");
        taskGroup.setGroupStatus(TaskGroupStatusEnum.NORMAL.getCode());
        taskGroup.setCreateTime(LocalDateTime.now());
        taskGroup.setUpdateTime(LocalDateTime.now());

        if(!taskGroupService.save(taskGroup)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建团队所属任务族失败");
        }

        // 6. 创建团队与所有者关系
        TeamUserRelation ownerTeamRelation = new TeamUserRelation();
        ownerTeamRelation.setTeamId(team.getTeamId());
        ownerTeamRelation.setUserId(currentUser.getUserId());
        ownerTeamRelation.setUserRole(UserRoleEnum.OWNER.getCode());
        ownerTeamRelation.setUserStatus(UserStatusEnum.NORMAL.getCode());
        ownerTeamRelation.setTeamIndex(teamUserRelationService.countByUserId(currentUser.getUserId()) + 1);
        ownerTeamRelation.setTeamColor(SystemConstant.DEFAULT_TEAM_COLOR);
        ownerTeamRelation.setJoinTime(LocalDateTime.now());
        ownerTeamRelation.setUpdateTime(LocalDateTime.now());

        if(!teamUserRelationService.save(ownerTeamRelation)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建团队与所有者关系失败");
        }

        // 7. 创建团队所属任务族与所有者关系
        TaskGroupUserRelation ownerGroupRelation = new TaskGroupUserRelation();
        ownerGroupRelation.setTaskGroupId(taskGroup.getTaskGroupId());
        ownerGroupRelation.setUserId(currentUser.getUserId());
        ownerGroupRelation.setUserRole(UserRoleEnum.OWNER.getCode());
        ownerGroupRelation.setUserStatus(UserStatusEnum.NORMAL.getCode());
        ownerGroupRelation.setGroupIndex(taskGroupUserRelationService.countByUserId(currentUser.getUserId()) + 1);
        ownerGroupRelation.setGroupAlias(taskGroupName);
        ownerGroupRelation.setGroupColor(SystemConstant.DEFAULT_GROUP_COLOR);
        ownerGroupRelation.setJoinTime(LocalDateTime.now());
        ownerGroupRelation.setUpdateTime(LocalDateTime.now());

        if(!taskGroupUserRelationService.save(ownerGroupRelation)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建团队所属任务族与所有者关系失败");
        }


        // 8. 创建团队与被邀请成员的邀请关系
        for (CreateTeamRequest.InvitedMember invitedMember : createTeamRequest.getInvitedMemberList()){
            // 读取被邀请成员
            String invitedUserId = invitedMember.getUserId();
            Integer invitedUserRole = invitedMember.getMemberRole();

            // 跳过自己
            if(invitedUserId.equals(currentUser.getUserId())){
                continue;
            }

            // 验证该角色是否存在
            if(userService.getById(invitedUserId) == null){
                log.info("在邀请成员时，成员：" + invitedUserId + "不存在");
                continue;
            }

            // 验证赋予角色是否合理
            if(invitedUserRole != UserRoleEnum.MANAGER.getCode() && invitedUserRole != UserRoleEnum.MEMBER.getCode()){
                log.info("在邀请成员时，成员：" + invitedUserId + "赋予的角色：" + invitedUserRole + "不合法");
                continue;
            }

            // 构建添加成员邀请
            TeamUserRelation invitedTeamRelation = new TeamUserRelation();
            invitedTeamRelation.setTeamId(team.getTeamId());
            invitedTeamRelation.setUserId(invitedUserId);
            invitedTeamRelation.setUserRole(invitedUserRole);
            invitedTeamRelation.setUserStatus(UserStatusEnum.INVITING.getCode());
            invitedTeamRelation.setTeamIndex(0);
            invitedTeamRelation.setTeamColor(SystemConstant.DEFAULT_TEAM_COLOR);
            invitedTeamRelation.setJoinTime(LocalDateTime.now());
            invitedTeamRelation.setUpdateTime(LocalDateTime.now());

            if(!teamUserRelationService.save(invitedTeamRelation)){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建团队与被邀请成员的邀请关系失败");
            }
        }

        return R.success(team, "创建成功");
    }


    /**
     * 邀请成员
     *
     * <p>作用简介：向指定团队邀请一个用户加入，记录邀请关系。</p>
     * <p>执行逻辑：
     * 1. 校验当前用户与团队存在性并确认邀请权限（owner/manager）；
     * 2. 校验被邀请用户存在性与角色参数（仅 manager 或 member）；
     * 3. 查询现有关联：
     *    - 若已是成员（NORMAL），返回失败；
     *    - 若已拒绝（REJECTED），置为邀请中并更新角色；
     *    - 若邀请中（INVITING），更新角色；
     * 4. 无关联时创建邀请关系，设置状态为邀请中与默认显示属性；
     * 5. 返回结果。
     * </p>
     *
     * @param request HTTP 请求对象
     * @param teamId 团队ID路径参数
     * @param teamUserRelation 包含被邀请用户及角色的请求体（仅读取 userId、userRole）
     * @return 创建或更新的成员关系
     */
    @PostMapping("/{teamId}/members")
    public R<TeamUserRelation> inviteTeamMember(HttpServletRequest request, @PathVariable("teamId") String teamId, @RequestBody TeamUserRelation teamUserRelation) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        // 1. 校验当前用户与团队存在性并确认邀请权限（owner/manager）；
        teamId = CommonUtil.trim(teamId);
        String targetUserId = teamUserRelation == null ? null : CommonUtil.trim(teamUserRelation.getUserId());
        Integer memberRoleCode = teamUserRelation == null ? null : teamUserRelation.getUserRole();

        if (teamId == null || teamId.isEmpty() || targetUserId == null || targetUserId.isEmpty()) return R.fail("缺少 teamId 或 userId");
        if (targetUserId.equals(currentUser.getUserId())) return R.fail("不可邀请自己");

        if (userService.getById(targetUserId) == null) {
            return R.notFound("用户不存在");
        }
        if (teamService.getById(teamId) == null){
            return R.notFound("团队不存在");
        }

        // 2. 校验被邀请用户存在性与角色参数（仅 manager 或 member）；
        TeamUserRelation inviterRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId()), false);
        if (inviterRel == null) return R.fail("非团队成员无法邀请");
        Integer inviterRole = inviterRel.getUserRole();
        if (!(UserRoleEnum.OWNER.getCode().equals(inviterRole) || UserRoleEnum.MANAGER.getCode().equals(inviterRole))) {
            return R.fail("没有邀请权限");
        }

        // 3. 查询现有关联与角色校验（仅允许 MANAGER/MEMBER）
        if (memberRoleCode == null ||
                !(UserRoleEnum.MANAGER.getCode().equals(memberRoleCode) || UserRoleEnum.MEMBER.getCode().equals(memberRoleCode))) {
            return R.fail("角色仅支持 manager 或 member");
        }

        TeamUserRelation existing = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, targetUserId), false);

        if (existing != null) {
            Integer status = existing.getUserStatus();
            if (UserStatusEnum.NORMAL.getCode().equals(status)) {
                return R.fail("该用户已是团队成员");
            }
            if (UserStatusEnum.REJECTED.getCode().equals(status)) {
                existing.setUserStatus(UserStatusEnum.INVITING.getCode());
                existing.setUserRole(memberRoleCode);
                existing.setUpdateTime(LocalDateTime.now());

                if(!teamUserRelationService.updateById(existing)){
                    throw new BusinessException(ResultCode.INTERNAL_ERROR, "重新邀请失败");
                }

                return R.success(existing, "已重新邀请");
            }else {
                existing.setUserRole(memberRoleCode);
                existing.setUpdateTime(LocalDateTime.now());

                if(!teamUserRelationService.updateById(existing)){
                    throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新邀请失败");
                }

                return R.success(existing, "已更新邀请");
            }
        }

        TeamUserRelation rel = new TeamUserRelation();
        rel.setTeamId(teamId);
        rel.setUserId(targetUserId);
        rel.setUserRole(memberRoleCode);
        rel.setUserStatus(UserStatusEnum.INVITING.getCode());
        rel.setTeamIndex(0);
        rel.setTeamColor(SystemConstant.DEFAULT_TEAM_COLOR);
        rel.setJoinTime(LocalDateTime.now());
        rel.setUpdateTime(LocalDateTime.now());

        if(!teamUserRelationService.save(rel)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "邀请失败");
        }

        return R.success(rel, "邀请成功");
    }


    /**
     * 删除团队
     *
     * <p>作用简介：级联逻辑删除团队及其下属数据（任务族、任务、成员与附件等）。</p>
     * <p>执行逻辑：
     * 1. 校验当前用户为团队所有者，校验团队存在性；
     * 2. 查询团队下所有任务族与任务，收集 `groupIds` 与 `taskIds`；
     * 3. 逻辑删除任务族成员关系、任务协助者关系、任务附件、子任务；
     * 4. 逻辑删除任务与任务族记录；
     * 5. 逻辑删除团队成员关系；
     * 6. 逻辑删除团队记录；
     * 7. 返回删除成功。
     * </p>
     *
     * @param request HTTP 请求对象
     * @param teamId 团队ID路径参数
     * @return 删除结果
     */
    @DeleteMapping("/{teamId}")
    @Transactional(rollbackFor = Exception.class)
    public R<?> deleteTeam(HttpServletRequest request, @PathVariable("teamId") String teamId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        teamId = CommonUtil.trim(teamId);

        Team team = teamService.getById(teamId);
        if (team == null) return R.notFound("团队不存在");

        TeamUserRelation myRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (myRel == null || !UserRoleEnum.OWNER.getCode().equals(myRel.getUserRole())) {
            return R.fail("仅团队所有者可删除团队");
        }

        List<TaskGroup> groups = taskGroupService.list(new LambdaQueryWrapper<TaskGroup>()
                .eq(TaskGroup::getTeamId, teamId));
        List<String> groupIds = new ArrayList<>();
        for (TaskGroup g : groups) groupIds.add(g.getTaskGroupId());

        List<Task> tasks = taskService.list(new LambdaQueryWrapper<Task>()
                .eq(Task::getTeamId, teamId));
        List<String> taskIds = new ArrayList<>();
        for (Task t : tasks) taskIds.add(t.getTaskId());

        // 3. 逻辑删除任务族成员关系
        if (!groupIds.isEmpty()) {
            if(!taskGroupUserRelationService.remove(new LambdaQueryWrapper<TaskGroupUserRelation>().in(TaskGroupUserRelation::getTaskGroupId, groupIds))){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除任务族成员关系失败");
            }
        }

        // 3. 逻辑删除任务节点与任务链
        // 3.1 收集子任务ID以便节点筛选
        List<ChildTask> childTasks = taskIds.isEmpty()
                ? Collections.emptyList()
                : childTaskService.list(new LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, taskIds));
        List<String> childTaskIds = new ArrayList<>();
        for (ChildTask ct : childTasks) childTaskIds.add(ct.getChildTaskId());
        // 3.2 列出待删除的任务节点（按 teamId / groupIds / taskIds / childTaskIds 组合筛选）
        // 只有当存在任务族、任务或子任务时，才需要查询节点
        List<TaskNode> nodes = Collections.emptyList();
        Set<String> nodeIds = new HashSet<>();
        if (!groupIds.isEmpty() || !taskIds.isEmpty() || !childTaskIds.isEmpty()) {
            LambdaQueryWrapper<TaskNode> nodeQw = new LambdaQueryWrapper<TaskNode>().eq(TaskNode::getTeamId, teamId);
            if (!groupIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getTaskGroupId, groupIds));
            if (!taskIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getTaskId, taskIds));
            if (!childTaskIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getChildTaskId, childTaskIds));
            nodes = taskNodeService.list(nodeQw);
            for (TaskNode n : nodes) nodeIds.add(n.getTaskNodeId());
        }
        // 3.3 逻辑删除任务链（边），按源/目标节点ID
        if (!nodeIds.isEmpty()) {
            if(!taskEdgeService.remove(new LambdaQueryWrapper<TaskEdge>().in(TaskEdge::getSourceNodeId, nodeIds).or().in(TaskEdge::getTargetNodeId, nodeIds))){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除任务链失败");
            }
        }
        // 3.4 逻辑删除任务节点
        if (!nodes.isEmpty()) {
            LambdaQueryWrapper<TaskNode> nodeQw = new LambdaQueryWrapper<TaskNode>().eq(TaskNode::getTeamId, teamId);
            if (!groupIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getTaskGroupId, groupIds));
            if (!taskIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getTaskId, taskIds));
            if (!childTaskIds.isEmpty()) nodeQw.or(w -> w.in(TaskNode::getChildTaskId, childTaskIds));
            if(!taskNodeService.remove(nodeQw)){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除任务节点失败");
            }
        }

        // 4. 逻辑删除任务关联数据：协助者关系、附件与子任务
        if (!taskIds.isEmpty()) {
            // 4.1 协助者关系（如果存在）
            LambdaQueryWrapper<TaskUserRelation> taskUserRelQw = new LambdaQueryWrapper<TaskUserRelation>().in(TaskUserRelation::getTaskId, taskIds);
            long taskUserRelCount = taskUserRelationService.count(taskUserRelQw);
            if (taskUserRelCount > 0) {
                if(!taskUserRelationService.remove(taskUserRelQw)){
                    throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除任务协助者关系失败");
                }
            }

            // 4.2 任务附件（如果存在）
            LambdaQueryWrapper<TaskFile> taskFileQw = new LambdaQueryWrapper<TaskFile>().in(TaskFile::getTaskId, taskIds);
            long taskFileCount = taskFileService.count(taskFileQw);
            if (taskFileCount > 0) {
                if(!taskFileService.remove(taskFileQw)){
                    throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除任务附件失败");
                }
            }

            // 4.3 子任务（如果存在）
            LambdaQueryWrapper<ChildTask> childTaskQw = new LambdaQueryWrapper<ChildTask>().in(ChildTask::getTaskId, taskIds);
            long childTaskCount = childTaskService.count(childTaskQw);
            if (childTaskCount > 0) {
                if(!childTaskService.remove(childTaskQw)){
                    throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除子任务失败");
                }
            }
        }

        // 5. 逻辑删除任务（如果存在）
        long taskCount = taskService.count(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, teamId));
        if (taskCount > 0) {
            if(!taskService.remove(new LambdaQueryWrapper<Task>().eq(Task::getTeamId, teamId))){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除任务失败");
            }
        }

        // 6. 逻辑删除任务族（如果存在）
        long taskGroupCount = taskGroupService.count(new LambdaQueryWrapper<TaskGroup>().eq(TaskGroup::getTeamId, teamId));
        if (taskGroupCount > 0) {
            if(!taskGroupService.remove(new LambdaQueryWrapper<TaskGroup>().eq(TaskGroup::getTeamId, teamId))){
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除任务族失败");
            }
        }

        // 7. 逻辑删除团队成员关系
        if(!teamUserRelationService.remove(new LambdaQueryWrapper<TeamUserRelation>().eq(TeamUserRelation::getTeamId, teamId))){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除团队成员关系失败");
        }

        // 8. 逻辑删除团队记录
        if(!teamService.removeById(teamId)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "删除团队失败");
        }

        return R.success(null, "删除成功");
    }


    /**
     * 退出团队（当前用户）
     *
     * <p>接口：`DELETE /api/teams/{teamId}/members/me`</p>
     * <p>权限：匿名 禁止 | 成员 允许 | 管理员 允许</p>
     * <p>备注：所有者不可退出团队；成员退出将清除相关任务族关系。</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>校验当前用户在目标团队的正常成员关系，若为所有者则拒绝退出；</li>
     *     <li>逻辑删除该用户与团队的成员关系；</li>
     *     <li>逻辑删除该用户在该团队下所有任务族的成员关系；</li>
     *     <li>返回退出成功的结果。</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @param teamId 团队ID路径参数
     * @return 包含 `teamId` 与当前用户 `userId` 的结果
     */
    @DeleteMapping("/{teamId}/members/me")
    @Transactional(rollbackFor = Exception.class)
    public R<String> leaveTeam(HttpServletRequest request, @PathVariable("teamId") String teamId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.error("参数缺失: teamId");

        TeamUserRelation rel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, tid)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.notFound("未找到该团队的成员关系或未激活");
        if (UserRoleEnum.OWNER.getCode().equals(rel.getUserRole())) {
            return R.fail("团队所有者不可退出团队");
        }

        boolean removedTeamRel = teamUserRelationService.remove(
                new LambdaQueryWrapper<TeamUserRelation>()
                        .eq(TeamUserRelation::getTeamId, tid)
                        .eq(TeamUserRelation::getUserId, currentUser.getUserId())
        );
        if (!removedTeamRel) return R.error("删除团队成员关系失败");

        List<TaskGroup> groups = taskGroupService.list(new LambdaQueryWrapper<TaskGroup>().eq(TaskGroup::getTeamId, tid));
        for (TaskGroup g : groups) {
            TaskGroupUserRelation exist = taskGroupUserRelationService.getOne(
                    new LambdaQueryWrapper<TaskGroupUserRelation>()
                            .eq(TaskGroupUserRelation::getTaskGroupId, g.getTaskGroupId())
                            .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId()), false);
            if (exist != null) {
                boolean removed = taskGroupUserRelationService.remove(
                        new LambdaQueryWrapper<TaskGroupUserRelation>()
                                .eq(TaskGroupUserRelation::getTaskGroupId, g.getTaskGroupId())
                                .eq(TaskGroupUserRelation::getUserId, currentUser.getUserId())
                );
                if (!removed) return R.error("删除任务族成员关系失败");
            }
        }

        return R.success("退出成功");
    }


    /**
     * 删除团队成员
     *
     * <p>接口：`DELETE /api/teams/{teamId}/members/{userId}`</p>
     *
     * <p>权限矩阵：</p>
     * <ul>
     *   <li>删除他人：owner 允许 | manager 允许 | member 禁止 | 不可删除团队所有者</li>
     *   <li>删除自己：owner 禁止 | manager 允许 | member 允许 | 所有者不可删除自身；其他成员可自行退出</li>
     * </ul>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验操作者在目标团队的正常成员关系；</li>
     *   <li>若为删除自身且操作者为所有者，则拒绝；</li>
     *   <li>若为删除他人，需操作者为所有者或管理员；若目标为团队所有者，则拒绝；</li>
     *   <li>逻辑删除目标用户与团队的成员关系；</li>
     *   <li>逻辑删除该用户在该团队所属任务族的成员关系；</li>
     *   <li>返回删除成功；</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @param teamId 团队ID路径参数
     * @param targetUserId 目标用户ID路径参数
     * @return “删除成功”
     */
    @DeleteMapping("/{teamId}/members/{userId}")
    @Transactional(rollbackFor = Exception.class)
    public R<String> deleteTeamMember(HttpServletRequest request, @PathVariable("teamId") String teamId, @PathVariable("userId") String targetUserId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        teamId = CommonUtil.trim(teamId);
        targetUserId = CommonUtil.trim(targetUserId);
        if (teamId == null || teamId.isEmpty() || targetUserId == null || targetUserId.isEmpty()) {
            return R.fail("缺少必要参数: teamId 或 userId");
        }

        String operatorId = currentUser.getUserId();
        TeamUserRelation operatorRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, operatorId)
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (operatorRel == null) return R.fail("当前用户不属于该团队或未激活");

        TeamUserRelation targetRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, targetUserId), false);

        if (operatorId.equals(targetUserId)) {
            if (UserRoleEnum.OWNER.getCode().equals(operatorRel.getUserRole())) {
                return R.fail("团队所有者不可删除自身");
            }
        } else {
            if (!(UserRoleEnum.OWNER.getCode().equals(operatorRel.getUserRole()) || UserRoleEnum.MANAGER.getCode().equals(operatorRel.getUserRole()))) {
                return R.fail("仅所有者或管理员可删除他人");
            }
            if (targetRel != null && UserRoleEnum.OWNER.getCode().equals(targetRel.getUserRole())) {
                return R.fail("不可删除团队所有者");
            }
        }

        boolean removedTeamRel = teamUserRelationService.remove(
                new LambdaQueryWrapper<TeamUserRelation>()
                        .eq(TeamUserRelation::getTeamId, teamId)
                        .eq(TeamUserRelation::getUserId, targetUserId)
        );

        TaskGroup group = taskGroupService.getOne(new LambdaQueryWrapper<TaskGroup>().eq(TaskGroup::getTeamId, teamId), false);
        boolean removedTaskGroupRel = true;
        if (group != null) {
            removedTaskGroupRel = taskGroupUserRelationService.remove(
                    new LambdaQueryWrapper<TaskGroupUserRelation>()
                            .eq(TaskGroupUserRelation::getTaskGroupId, group.getTaskGroupId())
                            .eq(TaskGroupUserRelation::getUserId, targetUserId)
            );
        }

        if (!removedTeamRel) return R.error("删除团队成员关系失败");
        if (!removedTaskGroupRel) return R.error("删除任务族成员关系失败");

        return R.success("删除成功");
    }


    /**
     * 修改团队信息
     *
     * <p>接口：`PUT /api/teams`</p>
     * <p>权限：仅团队所有者允许</p>
     * <p>说明：`teamId` 从请求体 `Team` 中读取</p>
     *
     * <p>支持字段：</p>
     * - `teamId`：团队ID（必填）
     * - `teamName`：团队名称，长度需 ≥ 2，且唯一
     * - `teamDescription`：团队描述
     * - `teamStatus`：团队状态（枚举数值），必须为合法枚举
     *
     * <p>执行逻辑：</p>
     * <ol>
     *     <li>从请求体读取 teamId，校验操作者为该团队所有者且为正常状态成员；</li>
     *     <li>读取待更新字段，进行格式与唯一性校验；</li>
     *     <li>仅当字段发生变化时才更新对应值；</li>
     *     <li>若无任何变更，直接返回“无变更”；否则更新并返回“更新成功”。</li>
     * </ol>
     */
    @PutMapping
    public R<Team> updateTeam(HttpServletRequest request, @RequestBody Team teamToUpdate) {
        User currentUser = CommonUtil.getCurrentUser(request);
        
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        if (teamToUpdate == null) return R.fail("缺少请求体");
        String teamId = CommonUtil.trim(teamToUpdate.getTeamId());
        if (teamId == null || teamId.isEmpty()) return R.fail("缺少团队ID");
        TeamUserRelation myRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (myRel == null || !UserRoleEnum.OWNER.getCode().equals(myRel.getUserRole())) return R.fail("仅团队所有者可修改");

        Team team = teamService.getById(teamId);
        if (team == null) return R.notFound("团队不存在");

        // 读取待更新字段并规范化
        String newName = CommonUtil.trim(teamToUpdate.getTeamName());
        String newDesc = CommonUtil.trim(teamToUpdate.getTeamDescription());
        Integer newStatus = teamToUpdate.getTeamStatus();

        boolean changed = false;

        // 团队名称校验与更新
        if (newName != null) {
            if (newName.isEmpty() || newName.length() < 2) {
                return R.fail("团队名称至少 2 位");
            }
            if (!Objects.equals(newName, team.getTeamName())) {
                boolean nameTaken = teamService.count(
                        new LambdaQueryWrapper<Team>()
                                .eq(Team::getTeamName, newName)
                                .ne(Team::getTeamId, teamId)
                ) > 0;
                if (nameTaken) return R.fail("团队名称已经存在");
                team.setTeamName(newName);
                changed = true;
            }
        }

        // 团队描述更新
        if (newDesc != null && !Objects.equals(newDesc, team.getTeamDescription())) {
            team.setTeamDescription(newDesc);
            changed = true;
        }

        // 团队状态校验与更新（仅合法枚举）
        if (newStatus != null) {
            if (!CommonUtil.inEnumCodes(TeamStatusEnum.values(), newStatus)) {
                return R.fail("非法团队状态");
            }
            if (!Objects.equals(newStatus, team.getTeamStatus())) {
                team.setTeamStatus(newStatus);
                changed = true;
            }
        }

        // 若无任何变更，直接返回
        if (!changed) {
            return R.success(team, "无变更");
        }

        team.setUpdateTime(LocalDateTime.now());

        if(!teamService.updateById(team)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新失败");
        }

        return R.success(team, "更新成功");
    }


    /**
     * 接受加入团队
     *
     * <p>接口：`PUT /api/teams/{teamId}/invites/me/accept`</p>
     * <p>权限：仅受邀用户（当前用户）允许；匿名禁止；非受邀状态禁止</p>
     * <p>返回：调用成功后仅返回字符串“已接受”</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户与 teamId 参数，校验团队存在；</li>
     *   <li>查询当前用户在该团队的成员关系；</li>
     *   <li>若状态为 NORMAL，视为幂等接受，直接返回“已接受”；</li>
     *   <li>若状态非 INVITING（例如 REJECTED/其他），拒绝操作；</li>
     *   <li>将状态从 INVITING 更新为 NORMAL，设置加入时间与个人排序；</li>
     *   <li>若团队所属任务族下不存在该用户关系，则补充创建默认关系；</li>
     *   <li>返回“已接受”。</li>
     * </ol>
     */
    @PutMapping("/{teamId}/invites/me/accept")
    @Transactional(rollbackFor = Exception.class)
    public R<String> acceptInviteTeam(HttpServletRequest request, @PathVariable("teamId") String teamId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.error("参数缺失: teamId");
        // 校验团队存在性
        Team team = teamService.getById(tid);
        if (team == null) return R.notFound("团队不存在");

        String acceptUserId = currentUser.getUserId();

        // 查询当前用户在该团队的成员关系（不限定状态，便于幂等与状态判断）
        TeamUserRelation rel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, tid)
                .eq(TeamUserRelation::getUserId, acceptUserId), false);
        if (rel == null) return R.notFound("未找到邀请记录");

        // 幂等：若已加入，直接返回成功
        if (UserStatusEnum.NORMAL.getCode().equals(rel.getUserStatus())) {
            return R.success("已接受");
        }
        // 仅允许从 INVITING 接受
        if (!UserStatusEnum.INVITING.getCode().equals(rel.getUserStatus())) {
            return R.fail("当前状态不可接受邀请");
        }

        // 状态更新为正常，设置加入时间与排序
        rel.setUserStatus(UserStatusEnum.NORMAL.getCode());
        rel.setJoinTime(LocalDateTime.now());
        rel.setTeamIndex(teamUserRelationService.countByUserId(acceptUserId) + 1);
        rel.setUpdateTime(LocalDateTime.now());

        if(!teamUserRelationService.updateById(rel)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "接受失败");
        }

        // 若团队所属任务族不存在该用户关系，则补充创建
        TaskGroup taskGroup = taskGroupService.getOne(new LambdaQueryWrapper<TaskGroup>().eq(TaskGroup::getTeamId, tid), false);
        if (taskGroup != null) {
            TaskGroupUserRelation existingRel = taskGroupUserRelationService.getOne(
                    new LambdaQueryWrapper<TaskGroupUserRelation>()
                            .eq(TaskGroupUserRelation::getTaskGroupId, taskGroup.getTaskGroupId())
                            .eq(TaskGroupUserRelation::getUserId, acceptUserId), false);
            if (existingRel == null) {
                TaskGroupUserRelation tgur = new TaskGroupUserRelation();
                tgur.setTaskGroupId(taskGroup.getTaskGroupId());
                tgur.setUserId(acceptUserId);
                tgur.setUserRole(rel.getUserRole());
                tgur.setUserStatus(UserStatusEnum.NORMAL.getCode());
                tgur.setGroupColor(SystemConstant.DEFAULT_GROUP_COLOR);
                tgur.setGroupAlias(taskGroup.getGroupName());
                tgur.setGroupIndex(taskGroupUserRelationService.countByUserId(acceptUserId) + 1);
                tgur.setJoinTime(LocalDateTime.now());
                tgur.setUpdateTime(LocalDateTime.now());
                taskGroupUserRelationService.save(tgur);
            }
        }

        return R.success("已接受");
    }


    /**
     * 拒绝加入团队
     *
     * <p>接口：`PUT /api/teams/{teamId}/invites/me/reject`</p>
     * <p>权限：仅受邀用户（当前用户）允许；匿名禁止；非受邀状态禁止</p>
     * <p>返回：调用成功后仅返回字符串“已拒绝”</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户与 teamId 参数，校验团队存在；</li>
     *   <li>查询当前用户在该团队的成员关系；</li>
     *   <li>若状态为 REJECTED，视为幂等拒绝，直接返回“已拒绝”；</li>
     *   <li>若状态非 INVITING（例如 NORMAL/其他），拒绝操作；</li>
     *   <li>将状态从 INVITING 更新为 REJECTED；</li>
     *   <li>返回“已拒绝”。</li>
     * </ol>
     */
    @PutMapping("/{teamId}/invites/me/reject")
    @Transactional(rollbackFor = Exception.class)
    public R<String> rejectInviteTeam(HttpServletRequest request, @PathVariable("teamId") String teamId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String tid = CommonUtil.trim(teamId);
        if (tid == null || tid.isEmpty()) return R.error("参数缺失: teamId");
        // 校验团队存在性
        Team team = teamService.getById(tid);
        if (team == null) return R.notFound("团队不存在");

        String rejectUserId = currentUser.getUserId();

        // 查询成员关系（不限定状态，便于幂等与状态判断）
        TeamUserRelation rel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, tid)
                .eq(TeamUserRelation::getUserId, rejectUserId), false);
        if (rel == null) return R.notFound("未找到邀请记录");

        // 幂等：若已拒绝，直接返回成功
        if (UserStatusEnum.REJECTED.getCode().equals(rel.getUserStatus())) {
            return R.success("已拒绝");
        }
        // 仅允许从 INVITING 拒绝
        if (!UserStatusEnum.INVITING.getCode().equals(rel.getUserStatus())) {
            return R.fail("当前状态不可拒绝邀请");
        }

        // 状态更新为拒绝
        rel.setUserStatus(UserStatusEnum.REJECTED.getCode());
        rel.setUpdateTime(LocalDateTime.now());

        if(!teamUserRelationService.updateById(rel)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "拒绝失败");
        }

        return R.success("已拒绝");
    }


    /**
     * 修改成员角色
     *
     * <p>接口：`PUT /api/teams/{teamId}/members/{userId}/role`</p>
     * <p>权限：owner 允许 | manager 允许 | member 禁止</p>
     * <p>约束：仅可设为 `manager` 或 `member`；禁止设为 `owner`；不可修改自身角色</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户已加入该团队且为正常状态；</li>
     *   <li>校验操作者为所有者或管理员，否则禁止；</li>
     *   <li>若目标为自身，禁止修改；</li>
     *   <li>从请求体读取目标角色（使用枚举数值），仅允许 manager/member，禁止 owner；</li>
     *   <li>校验目标成员存在且为正常状态；</li>
     *   <li>更新成员角色并返回“修改成功”；</li>
     * </ol>
     *
     * @param request HTTP 请求对象
     * @param teamId 团队ID路径参数
     * @param userId 目标用户ID路径参数
     * @param teamUserRelation 包含目标角色的请求体（仅读取 `userRole` 字段）
     * @return 修改后的成员关系
     */
    @PutMapping("/{teamId}/members/{userId}/role")
    public R<TeamUserRelation> updateMemberRole(HttpServletRequest request,
                                                @PathVariable("teamId") String teamId,
                                                @PathVariable("userId") String userId,
                                                @RequestBody TeamUserRelation teamUserRelation) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        teamId = CommonUtil.trim(teamId);
        userId = CommonUtil.trim(userId);
        Integer targetRoleCode = teamUserRelation == null ? null : teamUserRelation.getUserRole();

        TeamUserRelation operatorRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (operatorRel == null || !(UserRoleEnum.OWNER.getCode().equals(operatorRel.getUserRole()) || UserRoleEnum.MANAGER.getCode().equals(operatorRel.getUserRole()))) {
            return R.fail("没有修改权限");
        }

        if (Objects.equals(currentUser.getUserId(), userId)) return R.fail("不可修改自身角色");

        if (targetRoleCode == null) return R.fail("角色参数缺失");

        TeamUserRelation rel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, userId)
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.notFound("成员不存在或未激活");

        if (UserRoleEnum.OWNER.getCode().equals(targetRoleCode)) {
            return R.fail("禁止设为 owner");
        }
        if (!(UserRoleEnum.MANAGER.getCode().equals(targetRoleCode) || UserRoleEnum.MEMBER.getCode().equals(targetRoleCode))) {
            return R.fail("角色仅支持 manager 或 member");
        }
        rel.setUserRole(targetRoleCode);
        rel.setUpdateTime(LocalDateTime.now());

        if(!teamUserRelationService.updateById(rel)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新失败");
        }

        return R.success(rel, "修改成功");
    }


    /**
     * 交换个人两个团队的排序
     *
     * <p>接口：`PUT /api/teams/order`</p>
     * <p>权限：需为两个目标团队的正常成员（owner/manager/member 均可）</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户、请求体与两个团队ID的有效性且不重复；</li>
     *   <li>校验当前用户在两个团队的成员关系均为正常状态；</li>
     *   <li>校验两个排序值必须不一致，否则拒绝；</li>
     *   <li>执行个人排序交换（原子性保障）；</li>
     *   <li>返回两个团队的基本信息与“交换成功”。</li>
     * </ol>
     */
    @PutMapping("/order")
    @Transactional(rollbackFor = Exception.class)
    public R<List<MyTeamResponse>> swapMyTeamOrder(HttpServletRequest request, @RequestBody SwapMyTeamOrderRequest swapMyTeamOrderRequest) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        String teamIdA = CommonUtil.trim(swapMyTeamOrderRequest.getTeamIdA());
        String teamIdB = CommonUtil.trim(swapMyTeamOrderRequest.getTeamIdB());
        if (teamIdA == null || teamIdB == null || teamIdA.equals(teamIdB)) {
            return R.fail("参数错误");
        }

        TeamUserRelation relA = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamIdA)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        TeamUserRelation relB = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamIdB)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);

        if (relA == null || relB == null) {
            return R.notFound("未找到成员关系");
        }

        // 若两个排序值一致，拒绝操作
        Integer idxA = relA.getTeamIndex();
        Integer idxB = relB.getTeamIndex();
        if (Objects.equals(idxA, idxB)) {
            return R.fail("排序值禁止一致");
        }

        if(!teamUserRelationService.swapTeamOrder(currentUser.getUserId(), relA.getTeamUserRelationId(), relB.getTeamUserRelationId())){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新失败");
        }

        TeamUserRelation newRelA = teamUserRelationService.getById(relA.getTeamUserRelationId());
        TeamUserRelation newRelB = teamUserRelationService.getById(relB.getTeamUserRelationId());
        Team teamA = teamService.getById(teamIdA);
        Team teamB = teamService.getById(teamIdB);
        List<MyTeamResponse> result = new ArrayList<>(2);
        if (teamA != null && newRelA != null) {
            MyTeamResponse itemA = new MyTeamResponse();
            itemA.setTeamId(teamA.getTeamId());
            itemA.setTeamName(teamA.getTeamName());
            itemA.setTeamAvatar(teamA.getTeamAvatar());
            itemA.setTeamDescription(teamA.getTeamDescription());
            itemA.setTeamStatus(teamA.getTeamStatus());
            itemA.setMemberRole(newRelA.getUserRole());
            itemA.setMemberStatus(newRelA.getUserStatus());
            itemA.setTeamIndex(newRelA.getTeamIndex());
            itemA.setTeamColor(newRelA.getTeamColor());
            itemA.setJoinTime(newRelA.getJoinTime());
            itemA.setUpdateTime(newRelA.getUpdateTime());
            result.add(itemA);
        }
        if (teamB != null && newRelB != null) {
            MyTeamResponse itemB = new MyTeamResponse();
            itemB.setTeamId(teamB.getTeamId());
            itemB.setTeamName(teamB.getTeamName());
            itemB.setTeamAvatar(teamB.getTeamAvatar());
            itemB.setTeamDescription(teamB.getTeamDescription());
            itemB.setTeamStatus(teamB.getTeamStatus());
            itemB.setMemberRole(newRelB.getUserRole());
            itemB.setMemberStatus(newRelB.getUserStatus());
            itemB.setTeamIndex(newRelB.getTeamIndex());
            itemB.setTeamColor(newRelB.getTeamColor());
            itemB.setJoinTime(newRelB.getJoinTime());
            itemB.setUpdateTime(newRelB.getUpdateTime());
            result.add(itemB);
        }
        return R.success(result, "交换成功");
    }


    /**
     * 修改个人团队颜色
     *
     * <p>接口：`PUT /api/teams/{teamId}/members/me/color`</p>
     * <p>权限：需为该团队的正常成员（owner/manager/member 均可）</p>
     * <p>约束：颜色需为 `#xxxxxx`（6 位十六进制）</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户与团队ID的有效性；</li>
     *   <li>校验颜色格式；</li>
     *   <li>校验成员关系为正常状态；</li>
     *   <li>若颜色无变化，直接返回“颜色已更新”；否则更新并返回。</li>
     * </ol>
     * <p>请求体：`TeamUserRelation`（仅读取 `teamColor` 字段）</p>
     */
    @PutMapping("/{teamId}/members/me/color")
    public R<MyTeamResponse> updateMyTeamColor(HttpServletRequest request, @PathVariable("teamId") String teamId, @RequestBody TeamUserRelation teamUserRelation) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        teamId = CommonUtil.trim(teamId);
        String color = teamUserRelation == null ? null : CommonUtil.trim(teamUserRelation.getTeamColor());
        String operatorId = currentUser.getUserId();

        if (teamId == null || teamId.isEmpty()) {
            return R.fail("缺少团队ID");
        }

        // 颜色校验：必须为 #xxxxxx（6位16进制）
        if (color == null || !SystemConstant.HEX_COLOR_6.matcher(color).matches()) {
            return R.fail("颜色格式无效，需为 #xxxxxx（6位十六进制）");
        }

        TeamUserRelation rel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, operatorId)
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (rel == null) return R.notFound("成员不存在或未激活");

        // 无变化直接返回
        if (Objects.equals(color, rel.getTeamColor())) {
            Team team = teamService.getById(teamId);
            MyTeamResponse item = new MyTeamResponse();
            if (team != null) {
                item.setTeamId(team.getTeamId());
                item.setTeamName(team.getTeamName());
                item.setTeamAvatar(team.getTeamAvatar());
                item.setTeamDescription(team.getTeamDescription());
                item.setTeamStatus(team.getTeamStatus());
            } else {
                item.setTeamId(teamId);
            }
            item.setMemberRole(rel.getUserRole());
            item.setMemberStatus(rel.getUserStatus());
            item.setTeamIndex(rel.getTeamIndex());
            item.setTeamColor(rel.getTeamColor());
            item.setJoinTime(rel.getJoinTime());
            item.setUpdateTime(rel.getUpdateTime());
            return R.success(item, "颜色已更新");
        }

        rel.setTeamColor(color);
        rel.setUpdateTime(LocalDateTime.now());

        if(!teamUserRelationService.updateById(rel)){
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "颜色更新失败");
        }

        Team team = teamService.getById(teamId);
        MyTeamResponse item = new MyTeamResponse();
        if (team != null) {
            item.setTeamId(team.getTeamId());
            item.setTeamName(team.getTeamName());
            item.setTeamAvatar(team.getTeamAvatar());
            item.setTeamDescription(team.getTeamDescription());
            item.setTeamStatus(team.getTeamStatus());
        } else {
            item.setTeamId(teamId);
        }
        item.setMemberRole(rel.getUserRole());
        item.setMemberStatus(rel.getUserStatus());
        item.setTeamIndex(rel.getTeamIndex());
        item.setTeamColor(rel.getTeamColor());
        item.setJoinTime(rel.getJoinTime());
        item.setUpdateTime(rel.getUpdateTime());
        return R.success(item, "颜色已更新");
    }


    /**
     * 本人的被邀请团队分页
     *
     * <p>接口：`GET /api/teams/me/invites`</p>
     * <p>权限：owner 允许 | manager 允许 | member 允许（仅限本人）</p>
     * <p>说明：可选邀请状态过滤（默认 memberStatus=2，排除 owner），支持 `teamName` 模糊查询。</p>
     *
     * <p>请求参数：</p>
     * <ul>
     *   <li>page：页码，默认 1</li>
     *   <li>size：每页数量，默认 10</li>
     *   <li>memberStatus：成员状态，非必填，取值 1/2/3；默认 2</li>
     *   <li>teamName：团队名称关键字，非必填，用于模糊匹配</li>
     * </ul>
     *
     * <p>返回：分页的 `MyTeamResponse` 列表</p>
     */
    @GetMapping("/me/invites")
    public R<Page<MyTeamResponse>> getMyInviteTeamPage(HttpServletRequest request,
                                                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                       @RequestParam(value = "memberStatus", required = false) String memberStatus,
                                                       @RequestParam(value = "teamName", required = false) String teamName) {
        User cu = CommonUtil.getCurrentUser(request);
        if (cu == null) return R.unauthorized("未能识别当前用户");

        String status = StringUtils.isBlank(memberStatus) ? "2" : memberStatus.trim();
        if (!Arrays.asList("1", "2", "3").contains(status)) {
            status = "2";
        }

        Integer statusCode = Integer.parseInt(status);
        LambdaQueryWrapper<TeamUserRelation> wrapper = new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getUserId, cu.getUserId())
                .eq(TeamUserRelation::getUserStatus, statusCode)
                .ne(TeamUserRelation::getUserRole, UserRoleEnum.OWNER.getCode());
        String teamKw = CommonUtil.trim(teamName);
        if (teamKw != null && !teamKw.isEmpty()) {
            List<Team> matchedTeams = teamService.list(new LambdaQueryWrapper<Team>()
                    .like(Team::getTeamName, teamKw));
            if (matchedTeams == null || matchedTeams.isEmpty()) {
                Page<MyTeamResponse> emptyOut = new Page<>(page, size);
                emptyOut.setTotal(0);
                emptyOut.setRecords(Collections.emptyList());
                emptyOut.setPages(0);
                return R.success(emptyOut, "查询成功");
            }
            Set<String> matchedIds = new HashSet<>();
            for (Team mt : matchedTeams) {
                if (mt.getTeamId() != null) matchedIds.add(mt.getTeamId());
            }
            if (matchedIds.isEmpty()) {
                Page<MyTeamResponse> emptyOut = new Page<>(page, size);
                emptyOut.setTotal(0);
                emptyOut.setRecords(Collections.emptyList());
                emptyOut.setPages(0);
                return R.success(emptyOut, "查询成功");
            }
            wrapper.in(TeamUserRelation::getTeamId, matchedIds);
        }
        if ("1".equals(status)) {
            wrapper.orderByAsc(TeamUserRelation::getTeamIndex);
        } else {
            wrapper.orderByDesc(TeamUserRelation::getUpdateTime);
        }

        Page<TeamUserRelation> relPage = teamUserRelationService.page(new Page<>(page, size), wrapper);

        List<TeamUserRelation> rels = relPage.getRecords();
        Set<String> teamIds = new HashSet<>();
        for (TeamUserRelation r : rels) { if (r.getTeamId() != null) teamIds.add(r.getTeamId()); }
        List<Team> teams = teamIds.isEmpty() ? Collections.emptyList() : teamService.listByIds(teamIds);
        Map<String, Team> teamMap = new HashMap<>();
        for (Team t : teams) { teamMap.put(t.getTeamId(), t); }

        List<MyTeamResponse> records = new ArrayList<>();
        for (TeamUserRelation r : rels) {
            Team t = teamMap.get(r.getTeamId());
            MyTeamResponse item = new MyTeamResponse();
            item.setTeamId(r.getTeamId());
            item.setMemberRole(r.getUserRole());
            item.setMemberStatus(r.getUserStatus());
            item.setTeamIndex(r.getTeamIndex());
            item.setTeamColor(r.getTeamColor());
            item.setJoinTime(r.getJoinTime());
            item.setUpdateTime(r.getUpdateTime());
            if (t != null) {
                item.setTeamName(t.getTeamName());
                item.setTeamAvatar(t.getTeamAvatar());
                item.setTeamDescription(t.getTeamDescription());
                item.setTeamStatus(t.getTeamStatus());
            }
            records.add(item);
        }

        Page<MyTeamResponse> out = new Page<>(page, size);
        out.setTotal(relPage.getTotal());
        out.setRecords(records);
        out.setPages(relPage.getPages());

        return R.success(out, "查询成功");
    }


    /**
     * 查询本人相关的团队（无需分页）
     *
     * <p>接口：`GET /api/teams/me/related`</p>
     * <p>权限：owner 允许 | manager 允许 | member 允许（仅限本人）</p>
     * <p>说明：返回与本人有关的团队（已加入）</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>查询当前用户的团队成员关系（状态：NORMAL/INVITING/REJECTED）；</li>
     *   <li>汇总团队基础信息；</li>
     *   <li>返回融合后的记录列表。</li>
     * </ol>
     */
    @GetMapping("/me")
    public R<List<MyTeamResponse>> getMyRelatedTeamList(HttpServletRequest request) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        List<TeamUserRelation> rels = teamUserRelationService.list(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode())
                .orderByAsc(TeamUserRelation::getTeamIndex));
        Set<String> teamIds = new HashSet<>();
        for (TeamUserRelation r : rels) {
            if (r.getTeamId() != null) teamIds.add(r.getTeamId());
        }
        List<Team> teams = teamIds.isEmpty() ? Collections.emptyList() : teamService.list(new LambdaQueryWrapper<Team>()
                .in(Team::getTeamId, teamIds)
                .eq(Team::getTeamStatus, TeamStatusEnum.IN_PROGRESS.getCode()));
        Map<String, Team> teamMap = new HashMap<>();
        for (Team t : teams) teamMap.put(t.getTeamId(), t);
        List<MyTeamResponse> out = new ArrayList<>();
        for (TeamUserRelation r : rels) {
            Team t = teamMap.get(r.getTeamId());
            if (t == null) continue;
            MyTeamResponse item = new MyTeamResponse();
            item.setTeamId(r.getTeamId());
            item.setMemberRole(r.getUserRole());
            item.setMemberStatus(r.getUserStatus());
            item.setTeamIndex(r.getTeamIndex());
            item.setTeamColor(r.getTeamColor());
            item.setJoinTime(r.getJoinTime());
            item.setUpdateTime(r.getUpdateTime());
            if (t != null) {
                item.setTeamName(t.getTeamName());
                item.setTeamAvatar(t.getTeamAvatar());
                item.setTeamDescription(t.getTeamDescription());
                item.setTeamStatus(t.getTeamStatus());
            }
            out.add(item);
        }
        return R.success(out, "查询成功");
    }


    /**
     * 查询团队基础信息
     *
     * <p>接口：`GET /api/teams/{teamId}`</p>
     * <p>权限：owner 允许 | manager 允许 | member 允许（需为该团队的正常成员）</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户与团队ID；</li>
     *   <li>校验成员关系为正常状态；</li>
     *   <li>返回团队基础信息。</li>
     * </ol>
     */
    @GetMapping("/{teamId}")
    public R<Team> getTeamBaseInfo(HttpServletRequest request, @PathVariable("teamId") String teamId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        teamId = CommonUtil.trim(teamId);
        if (teamId == null || teamId.isEmpty()) return R.fail("缺少团队ID");
        TeamUserRelation viewerRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (viewerRel == null) return R.fail("仅团队成员可查看");
        Team team = teamService.getById(teamId);
        if (team == null) return R.notFound("团队不存在");
        return R.success(team, "查询成功");
    }


    /**
     * 团队数据大屏（骨架）
     *
     * <p>接口：`GET /api/teams/{teamId}/dashboard`</p>
     * <p>权限：owner 允许 | manager 允许 | member 允许（需为该团队的正常成员）</p>
     * <p>说明：仅创建接口，内部内容 TODO</p>
     *
     * <p>执行逻辑：</p>
     * <ol>
     *   <li>校验当前用户与团队ID；</li>
     *   <li>校验成员关系为正常状态；</li>
     *   <li>返回空数据与 TODO 提示。</li>
     * </ol>
     */
    @GetMapping("/{teamId}/dashboard")
    public R<String> getTeamDashboard(HttpServletRequest request, @PathVariable("teamId") String teamId) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");
        teamId = CommonUtil.trim(teamId);
        if (teamId == null || teamId.isEmpty()) return R.fail("缺少团队ID");
        TeamUserRelation viewerRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (viewerRel == null) return R.fail("仅团队成员可查看");
        Map<String, Object> data = new HashMap<>();
        return R.success("查询成功，当前功能暂未开发", "TODO");
    }


    /**
     * 团队成员分页
     *
     * <p>接口：`GET /api/teams/{teamId}/members`</p>
     * <p>权限：owner 允许 | manager 允许 | member 允许（需为该团队的正常成员）</p>
     * <p>说明：支持用户名模糊查询、成员角色过滤与邀请状态筛选。</p>
     *
     * <p>请求参数：</p>
     * <ul>
     *   <li>page：页码，默认 1</li>
     *   <li>size：每页数量，默认 10，最大 100</li>
     *   <li>userName：用户名关键字，非必填，用于模糊匹配</li>
     *   <li>memberRole：成员角色过滤，支持 0/1/2 或 owner/manager/member</li>
     *   <li>invited：是否筛选邀请中成员（true=邀请中，false/未传=已加入）</li>
     * </ul>
     *
     * <p>返回：分页的 `TeamMemberResponse` 列表</p>
     */
    @GetMapping("/{teamId}/members")
    public R<Page<TeamMemberResponse>> getTeamMemberPage(HttpServletRequest request,
                                                         @PathVariable("teamId") String teamId,
                                                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                         @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                         @RequestParam(value = "userName", required = false) String userName,
                                                         @RequestParam(value = "memberRole", required = false) String memberRole,
                                                         @RequestParam(value = "invited", required = false) Boolean invited) {
        User currentUser = CommonUtil.getCurrentUser(request);
        if (currentUser == null) return R.unauthorized("未能识别当前用户");

        teamId = CommonUtil.trim(teamId);
        if (teamId == null || teamId.isEmpty()) {
            return R.fail("缺少团队ID");
        }

        // 权限：需为该团队的成员（owner/manager/member，状态为1）
        TeamUserRelation viewerRel = teamUserRelationService.getOne(new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserId, currentUser.getUserId())
                .eq(TeamUserRelation::getUserStatus, UserStatusEnum.NORMAL.getCode()), false);
        if (viewerRel == null) {
            return R.fail("无权限查看该团队成员");
        }

        // 规范化分页参数
        page = (page == null || page < 1) ? 1 : page;
        if (size == null || size < 1) size = 10;
        if (size > 100) size = 100;

        // 根据 invited 参数筛选成员关系（true: 被邀请但未加入=2；默认：已加入=1），可选角色过滤
        Integer statusFilter = (invited != null && invited) ? UserStatusEnum.INVITING.getCode() : UserStatusEnum.NORMAL.getCode();
        LambdaQueryWrapper<TeamUserRelation> relQw = new LambdaQueryWrapper<TeamUserRelation>()
                .eq(TeamUserRelation::getTeamId, teamId)
                .eq(TeamUserRelation::getUserStatus, statusFilter);

        if (StringUtils.isNotBlank(memberRole)) {
            String role = memberRole.trim().toLowerCase();
            if (!Arrays.asList("owner", "manager", "member").contains(role)) {
                return R.fail("成员角色参数不合法");
            }
            Integer roleCode = null;
            try {
                roleCode = Integer.parseInt(role);
            } catch (Exception ignore) {
                if ("owner".equals(role)) roleCode = UserRoleEnum.OWNER.getCode();
                else if ("manager".equals(role)) roleCode = UserRoleEnum.MANAGER.getCode();
                else if ("member".equals(role)) roleCode = UserRoleEnum.MEMBER.getCode();
            }
            if (roleCode == null) {
                return R.fail("成员角色参数不合法");
            }
            relQw.eq(TeamUserRelation::getUserRole, roleCode);
        }

        List<TeamUserRelation> rels = teamUserRelationService.list(relQw);

        if (rels == null || rels.isEmpty()) {
            Page<TeamMemberResponse> emptyPage = new Page<>(page, size);
            emptyPage.setTotal(0);
            emptyPage.setPages(0);
            emptyPage.setRecords(Collections.emptyList());
            return R.success(emptyPage, "查询成功");
        }

        // 收集用户ID集合
        Set<String> userIds = new HashSet<>();
        for (TeamUserRelation r : rels) {
            if (r.getUserId() != null) userIds.add(r.getUserId());
        }

        // 构建用户查询（IN + LIKE），按用户名升序
        LambdaQueryWrapper<User> uw = new LambdaQueryWrapper<>();
        uw.in(User::getUserId, userIds);
        String kw = CommonUtil.trim(userName);
        if (kw != null && !kw.isEmpty()) {
            uw.like(User::getUserName, kw);
        }
        uw.orderByAsc(User::getUserName);

        Page<User> p = new Page<>(page, size);
        Page<User> userPage = userService.page(p, uw);

        Map<String, TeamUserRelation> relMap = new HashMap<>();
        for (TeamUserRelation r : rels) {
            if (r.getUserId() != null) relMap.put(r.getUserId(), r);
        }

        // 组装融合的分页记录
        List<TeamMemberResponse> records = new ArrayList<>();
        for (User u : userPage.getRecords()) {
            TeamUserRelation r = relMap.get(u.getUserId());
            if (r == null) continue; // 理论上不会发生
            TeamMemberResponse item = new TeamMemberResponse(
                    r.getTeamUserRelationId(),
                    r.getTeamId(),
                    r.getUserRole(),
                    r.getUserStatus(),
                    r.getTeamIndex(),
                    r.getTeamColor(),
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
            );
            records.add(item);
        }

        Page<TeamMemberResponse> outPage = new Page<>(page, size);
        outPage.setTotal(userPage.getTotal());
        outPage.setPages(userPage.getPages());
        outPage.setRecords(records);

        return R.success(outPage, "查询成功");
    }


}
