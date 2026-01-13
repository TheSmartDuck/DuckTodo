<template>
  <!-- 团队详情视图根容器 -->
  <div class="team-detail-view" v-if="team">
    <!-- 头部组件：显示团队名称和ID -->
    <TeamDetailHeader 
      :teamName="team.teamName" 
      :teamId="team.teamId" 
      :teamIndex="team.teamIndex" 
      :teamLogo="teamLogo" 
    />
    
    <!-- 页面内容区域 -->
    <div class="team-detail-page-content" v-loading="loading"
         element-loading-text="LOADING..."
         element-loading-spinner="duck-spinner"
         element-loading-custom-class="duck-loading-overlay"
         element-loading-background="rgba(251, 248, 241, 0.9)">
      
      <!-- 顶部导航标签页 -->
      <div class="team-detail-top-nav">
        <el-tabs v-model="activeTab" class="team-detail-panel-tabs">
          <el-tab-pane label="PROJECT REPORT / 项目报表" name="report"></el-tab-pane>
          <el-tab-pane label="MEMBERS / 项目成员" name="members"></el-tab-pane>
          <el-tab-pane label="MILESTONES / 里程碑" name="milestones"></el-tab-pane>
          <el-tab-pane label="KNOWLEDGE / 知识库" name="knowledge"></el-tab-pane>
          <!-- 设置标签页：仅 OWNER 可见 -->
          <el-tab-pane 
            v-if="currentUserRole === 0" 
            label="SETTINGS / 设置" 
            name="settings"
          ></el-tab-pane>
        </el-tabs>
      </div>

      <!-- 标签页内容区域 -->
      <div class="team-detail-tab-content">
        <!-- 项目报表面板 -->
        <TeamReportPanel 
          v-if="activeTab === 'report'" 
          :summary="summary" 
        />

        <!-- 成员管理面板 -->
        <TeamMembersPanel 
          v-else-if="activeTab === 'members'" 
          :members="members"
          :current-user-id="currentUserId"
          :current-user-role="currentUserRole"
          :team-id="team.teamId"
          @refresh="loadMembers"
          @invite="inviteFromDrawer"
          @reinvite="onReinvite"
          @remove="onRemove"
          @update-role="onUpdateRole"
        />

        <!-- 团队设置面板 -->
        <TeamSettingsPanel 
          v-else-if="activeTab === 'settings'" 
          :team-id="team.teamId"
          :team-name="team.teamName"
          @deleted="onTeamDeleted"
        />

        <!-- 其他标签页占位符 -->
        <div v-else class="team-detail-placeholder-layout">
          <div class="team-detail-placeholder-box">
            <i class="el-icon-loading"></i>
            <span>MODULE UNDER CONSTRUCTION...</span>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- 加载状态：团队数据未加载时显示 -->
  <div class="team-detail-view" v-else>
    <div class="team-detail-loading-holder">SYSTEM LOADING...</div>
  </div>
</template>

<script>
import { getMyRelatedTeamList, getTeamMemberPage, inviteTeamMember, deleteTeamMember } from '@/api/team-api'
import { getTeamOverview, getTeamWorkload, getTeamBurndown } from '@/api/statistics-api'
import { getMe } from '@/api/user-api'
import TeamDetailHeader from '@/components/team-detail/TeamDetailHeader.vue'
import TeamReportPanel from '@/components/team-detail/TeamReportPanel.vue'
import TeamMembersPanel from '@/components/team-detail/TeamMembersPanel.vue'
import TeamSettingsPanel from '@/components/team-detail/TeamSettingsPanel.vue'

/**
 * 团队详情视图组件
 * 
 * 功能：
 * 1. 显示团队详情信息（头部）
 * 2. 提供多个标签页：项目报表、成员管理、里程碑、知识库、设置
 * 3. 加载并显示团队统计数据
 * 4. 管理团队成员（邀请、移除、重新邀请）
 * 
 * Props：
 * - teamId: 团队ID（可选，也可从路由参数获取）
 */
export default {
  name: 'TeamDetailView',
  components: {
    TeamDetailHeader,
    TeamReportPanel,
    TeamMembersPanel,
    TeamSettingsPanel
  },
  props: {
    /**
     * 团队ID
     * @type {String|Number}
     */
    teamId: {
      type: [String, Number],
      required: false
    }
  },
  data () {
    return {
      team: null, // 当前团队对象
      loading: false, // 数据加载状态
      activeTab: 'report', // 当前激活的标签页
      summary: {
        teamInProgressTasksCount: 0, // 进行中任务数
        teamCompletedLast7DaysCount: 0, // 近7天完成任务数
        teamCreatedLast7DaysCount: 0, // 近7天新增任务数
        teamOverdueTasksCount: 0, // 逾期任务数
        burndown: [], // 燃尽图数据
        workload: [] // 工作负载数据
      },
      members: [], // 团队成员列表
      memberQuery: '', // 成员搜索关键词
      currentUserId: '', // 当前用户ID
      currentUserRole: null // 当前用户在团队中的角色（0: OWNER, 1: MANAGER, 2: MEMBER）
    }
  },
  computed: {
    /**
     * 团队Logo
     * @returns {string} 团队头像URL
     */
    teamLogo () {
      const v = this.team && this.team.teamAvatar
      return v || ''
    }
  },
  /**
   * 组件创建时加载团队数据和当前用户信息
   */
  created () {
    this.loadCurrentUser()
    this.loadTeam()
  },
  watch: {
    /**
     * 监听路由参数变化，重新加载团队数据
     */
    '$route.params.teamId' () {
      this.loadTeam()
    },
    
    /**
     * 监听当前用户角色变化
     * 如果用户不是 OWNER 且当前在设置页面，则切换到报表页面
     */
    currentUserRole (newRole) {
      if (newRole !== 0 && this.activeTab === 'settings') {
        this.activeTab = 'report'
      }
    }
  },
  methods: {
    /**
     * 获取日期字符串（相对于今天的偏移天数）
     * @param {Number} offsetDays - 偏移天数（负数表示过去，正数表示未来）
     * @returns {string} 格式化的日期字符串 yyyy-MM-dd
     */
    getDateStr (offsetDays) {
      const d = new Date()
      d.setDate(d.getDate() + offsetDays)
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const dd = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${dd}`
    },
    
    /**
     * 加载团队基本信息
     * 从路由参数或props获取团队ID，然后加载团队数据、统计数据和成员列表
     */
    async loadTeam () {
      const id = (this.$route.params.teamId || this.teamId || '').toString()
      if (!id) { this.team = null; return }
      try {
        const list = await getMyRelatedTeamList()
        const t = Array.isArray(list) ? list.find(x => (x.teamId || '').toString() === id) : null
        this.team = t || { teamId: id, teamName: '未命名项目' }
        await this.loadStats()
        await this.loadMembers()
      } catch (e) {
        this.team = { teamId: id, teamName: '未命名项目' }
        await this.loadStats()
        await this.loadMembers()
      }
    },
    
    /**
     * 加载团队成员列表
     * @param {String} query - 搜索关键词（可选）
     */
    async loadMembers (query) {
      if (typeof query === 'string') this.memberQuery = query
      try {
        const id = String(this.team && this.team.teamId || this.$route.params.teamId || this.teamId || '')
        if (!id) { this.members = []; return }
        const page = await getTeamMemberPage(id, { page: 1, size: 100, userName: this.memberQuery })
        const rec = Array.isArray(page && page.records) ? page.records : []
        this.members = rec
        // 更新当前用户在团队中的角色
        this.updateCurrentUserRole()
      } catch (e) {
        this.members = []
        this.currentUserRole = null
      }
    },
    
    /**
     * 从抽屉中邀请成员
     * @param {Object} row - 用户对象
     */
    async inviteFromDrawer (row) {
      const teamId = String(this.team && this.team.teamId || '')
      await inviteTeamMember({ teamId, userId: row.userId, userRole: 2 })
      this.$message.success('已发送邀请')
      await this.loadMembers()
    },
    
    /**
     * 移除团队成员
     * @param {Object} row - 成员对象
     */
    async onRemove (row) {
      const ok = await this.$confirm(`确认移除成员「${row.userName || row.userId}」吗？`, '提示', { type: 'warning' }).catch(() => false)
      if (!ok) return
      const teamId = String(this.team && this.team.teamId || '')
      await deleteTeamMember({ teamId, userId: row.userId })
      this.$message.success('已移除')
      await this.loadMembers()
    },
    
    /**
     * 重新邀请成员
     * @param {Object} row - 成员对象
     */
    async onReinvite (row) {
      const teamId = String(this.team && this.team.teamId || '')
      await inviteTeamMember({ teamId, userId: row.userId, userRole: ((row.memberRole === 'manager' || row.memberRole === 1 || row.memberRole === '1') ? 1 : 2) })
      this.$message.success('已重新邀请')
      await this.loadMembers()
    },
    
    /**
     * 更新成员角色
     * @param {Object} params - 参数对象
     * @param {String} params.userId - 成员用户ID
     * @param {Number} params.userRole - 新角色（1: MANAGER, 2: MEMBER）
     */
    async onUpdateRole (params) {
      try {
        const teamId = String(this.team && this.team.teamId || '')
        const { updateMemberRole } = await import('@/api/team-api')
        // 后端期望的枚举值：1-MANAGER, 2-MEMBER
        await updateMemberRole({ 
          teamId, 
          userId: params.userId, 
          userRole: params.userRole 
        })
        this.$message.success('角色更新成功')
        await this.loadMembers()
      } catch (e) {
        this.$message.error(e.message || '角色更新失败')
      }
    },
    
    /**
     * 加载当前用户信息
     * 获取当前登录用户的ID，并从成员列表中查找当前用户在团队中的角色
     */
    async loadCurrentUser () {
      try {
        const me = await getMe()
        // 尝试多种可能的用户ID字段名
        const uid = (me && (me.userId || me.userID || me.user_id)) 
          ? String(me.userId || me.userID || me.user_id) 
          : ''
        this.currentUserId = uid
        
        // 如果直接获取失败，尝试从包装数据中提取
        if (!uid && me && me.data) {
          this.currentUserId = String(me.data.userId || me.data.id || '')
        }
      } catch (e) {
        console.error('Failed to load current user', e)
        this.currentUserId = ''
      }
    },
    
    /**
     * 更新当前用户在团队中的角色
     * 从成员列表中查找当前用户的角色信息
     */
    updateCurrentUserRole () {
      if (!this.currentUserId || !this.members || this.members.length === 0) {
        this.currentUserRole = null
        return
      }
      
      const currentMember = this.members.find(m => 
        String(m.userId || '') === String(this.currentUserId)
      )
      
      if (currentMember) {
        this.currentUserRole = Number(currentMember.memberRole)
      } else {
        this.currentUserRole = null
      }
    },
    
    /**
     * 处理团队删除事件
     * 团队删除成功后，跳转到首页
     * @param {String} teamId - 已删除的团队ID
     */
    onTeamDeleted (teamId) {
      // 团队已删除，延迟跳转到首页（给用户看到成功消息的时间）
      setTimeout(() => {
        this.$router.push({ name: 'home' }).catch(err => {
          // 如果路由跳转失败，尝试使用路径跳转
          console.error('路由跳转失败:', err)
          this.$router.push({ path: '/home' }).catch(() => {
            // 如果路径跳转也失败，使用 replace 强制跳转
            window.location.href = '/'
          })
        })
      }, 1000)
    },
    
    /**
     * 加载团队统计数据
     * 包括概览数据、工作负载数据和燃尽图数据
     */
    async loadStats () {
      try {
        this.loading = true
        const id = String(this.team && this.team.teamId || this.$route.params.teamId || this.teamId || '')
        if (!id) { this.summary = { ...this.summary }; return }

        // 并行请求三个统计接口
        const [overviewRes, workloadRes, burndownRes] = await Promise.all([
          getTeamOverview(id).catch(() => ({})),
          getTeamWorkload(id, { scope: 'all' }).catch(() => ({ items: [] })),
          getTeamBurndown(id, { from: this.getDateStr(-14), to: this.getDateStr(0) }).catch(() => ({ items: [] }))
        ])

        const overview = overviewRes || {}
        const workload = workloadRes && workloadRes.items ? workloadRes.items : []
        const burndown = burndownRes && burndownRes.items ? burndownRes.items : []

        const inProgress = overview.inProgress || 0
        const overdue = overview.overdue || 0

        // 从燃尽图数据计算近7天完成的任务数
        const last7 = burndown.slice(-7)
        const completed7d = last7.reduce((acc, cur) => acc + (cur.completed || 0), 0)

        // 从燃尽图数据计算近7天新增的任务数
        // 公式：Created = Remaining(i) - Remaining(i-1) + Completed(i)
        let created7d = 0
        if (burndown.length > 0) {
           for (let i = Math.max(1, burndown.length - 7); i < burndown.length; i++) {
              const curr = burndown[i]
              const prev = burndown[i-1]
              const created = (curr.remaining || 0) - (prev.remaining || 0) + (curr.completed || 0)
              if (created > 0) created7d += created
           }
        }

        this.summary = {
          teamInProgressTasksCount: inProgress,
          teamOverdueTasksCount: overdue,
          teamCompletedLast7DaysCount: completed7d,
          teamCreatedLast7DaysCount: created7d,
          burndown,
          workload
        }
      } catch (e) {
        console.error(e)
        // 失败时保持原有数据
        this.summary = { ...this.summary }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/team-detail/team-detail-view.css';
</style>
