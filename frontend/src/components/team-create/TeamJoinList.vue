<template>
  <!-- 加入团队列表根容器 -->
  <div class="team-join-list-main-card">
    <!-- 卡片头部 -->
    <div class="team-join-list-card-header-retro">
      <!-- 头部标题文字 -->
      <span class="team-join-list-header-title-text">INVITATION_LIST // 邀请列表</span>
      <!-- 头部右侧操作组 -->
      <div class="team-join-list-header-right-group" style="display:flex; align-items:center;">
        <!-- 返回按钮 -->
        <el-button 
          size="mini" 
          class="team-join-list-retro-btn" 
          icon="el-icon-back" 
          @click="$emit('back')" 
          style="margin-right:12px;"
        >
          BACK / 返回
        </el-button>
        <!-- 头部装饰元素 -->
        <div class="team-join-list-header-deco">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>
    
    <!-- 装饰线条（三色渐变） -->
    <div class="team-join-list-retro-deco-line">
      <div class="team-join-list-line-seg" style="background: var(--avionics-mustard);"></div>
      <div class="team-join-list-line-seg" style="background: var(--oxidized-sage);"></div>
      <div class="team-join-list-line-seg" style="background: var(--alarm-burgundy);"></div>
    </div>
    
    <!-- 列表主体内容区域 -->
    <div class="team-join-list-body">
      <!-- 表单部分（叙事日志风格） -->
      <div class="team-join-list-form-section team-join-list-narrative-log">
        <!-- 部分头部 -->
        <div class="team-join-list-section-header">
          <!-- 方形标记点（黄色） -->
          <div class="team-join-list-square-bullet team-join-list-yellow"></div>
          <!-- 部分标题 -->
          <span class="team-join-list-section-title">PENDING INVITES // 待处理邀请</span>
          <!-- 状态筛选和刷新按钮组 -->
          <div style="margin-left:auto; display:flex; align-items:center; gap:8px;">
            <!-- 邀请状态选择器 -->
            <el-select 
              v-model="invitesStatus" 
              size="mini" 
              style="width:120px" 
              @change="onInvitesStatusChange" 
              popper-class="team-join-list-retro-select-dropdown"
            >
              <el-option label="邀请中" :value="2" />
              <el-option label="已接受" :value="1" />
              <el-option label="已拒绝" :value="3" />
            </el-select>
            <!-- 刷新按钮 -->
            <el-button 
              size="mini" 
              class="team-join-list-retro-btn" 
              :loading="invitesLoading" 
              @click="fetchInvites"
            >
              REFRESH
            </el-button>
          </div>
        </div>
        <!-- 部分内容 -->
        <div class="team-join-list-section-content">
          <!-- 邀请列表表格 -->
          <el-table 
            :data="inviteDisplayList" 
            size="mini" 
            class="team-join-list-retro-table" 
            v-loading="invitesLoading" 
            empty-text="NO DATA / 暂无数据"
          >
            <!-- 项目名称列（带滚动效果） -->
            <el-table-column label="PROJECT NAME/项目名称" min-width="200">
              <template slot-scope="scope">
                <div class="team-join-list-project-name-scroll-container" ref="projectNameContainer">
                  <span class="team-join-list-project-name-content">{{ scope.row.teamName }}</span>
                </div>
              </template>
            </el-table-column>
            <!-- 角色列 -->
            <el-table-column label="ROLE/角色" min-width="150">
              <template slot-scope="scope">
                <span>
                  {{ scope.row.memberRole === 1 ? 'MANAGER/管理员' : (scope.row.memberRole === 0 ? 'OWNER/管理员' : 'MEMBER/项目成员') }}
                </span>
              </template>
            </el-table-column>
            <!-- 邀请状态列 -->
            <el-table-column label="STATE/邀请状态" width="150">
              <template slot-scope="scope">
                <!-- 等待确认状态 -->
                <el-tag 
                  v-if="scope.row.memberStatus === 2" 
                  type="warning" 
                  size="mini" 
                  class="team-join-list-retro-tag-warning"
                >
                  WAITING/等待确认
                </el-tag>
                <!-- 已接受状态 -->
                <el-tag 
                  v-else-if="scope.row.memberStatus === 1" 
                  type="success" 
                  size="mini" 
                  class="team-join-list-retro-tag-success"
                >
                  ACCEPTED/已接受
                </el-tag>
                <!-- 已拒绝状态 -->
                <el-tag 
                  v-else-if="scope.row.memberStatus === 3" 
                  type="info" 
                  size="mini" 
                  class="team-join-list-retro-tag-info"
                >
                  REJECTED/已拒绝
                </el-tag>
                <!-- 未知状态 -->
                <el-tag v-else size="mini">UNKNOWN</el-tag>
              </template>
            </el-table-column>
            <!-- 邀请时间列 -->
            <el-table-column label="INVITED AT" min-width="180">
              <template slot-scope="scope">
                <span>{{ formatDate(scope.row.updateTime) }}</span>
              </template>
            </el-table-column>
            <!-- 操作列（固定右侧） -->
            <el-table-column label="ACTION" width="230" fixed="right">
              <template slot-scope="scope">
                <!-- 等待确认状态：显示接受和拒绝按钮 -->
                <el-button 
                  v-if="scope.row.memberStatus === 2" 
                  type="primary" 
                  size="mini" 
                  :loading="saving" 
                  class="team-join-list-retro-btn-primary" 
                  @click="acceptItemInvite(scope.row)"
                >
                  ACCEPT
                </el-button>
                <el-button 
                  v-if="scope.row.memberStatus === 2" 
                  type="danger" 
                  size="mini" 
                  :loading="saving" 
                  class="team-join-list-retro-btn-small-danger" 
                  @click="rejectItemInvite(scope.row)" 
                  style="margin-left:8px;"
                >
                  REJECT
                </el-button>
                <!-- 已接受状态：显示已加入标签 -->
                <el-tag 
                  v-else-if="scope.row.memberStatus === 1" 
                  type="success" 
                  size="mini" 
                  class="team-join-list-retro-tag-success"
                >
                  JOINED
                </el-tag>
                <!-- 已拒绝状态：显示已拒绝标签 -->
                <el-tag 
                  v-else-if="scope.row.memberStatus === 3" 
                  type="info" 
                  size="mini" 
                  class="team-join-list-retro-tag-info"
                >
                  REJECTED
                </el-tag>
                <!-- 其他状态：显示横线 -->
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <!-- 邀请列表分页 -->
          <div class="team-join-list-invite-pagination">
            <el-pagination
              layout="prev, pager, next"
              :current-page="invitesPageNum"
              :page-size="invitesPageSize"
              :total="invitesTotal"
              @current-change="onInvitesPageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Message } from 'element-ui'
import { acceptInviteTeam, rejectInviteTeam, getMyInviteTeamPage } from '@/api/team-api'
import { userRoleByCode, userStatusByCode } from '@/utils/enums'

/**
 * 加入团队列表组件
 * 
 * 功能：
 * 1. 显示待处理的团队邀请列表
 * 2. 支持按邀请状态筛选（邀请中、已接受、已拒绝）
 * 3. 接受或拒绝团队邀请
 * 4. 分页显示邀请列表
 * 5. 项目名称超长时自动滚动显示
 * 
 * 数据来源：
 * - 通过 team-api 的 getMyInviteTeamPage API 获取邀请列表
 * - 通过 team-api 的 acceptInviteTeam、rejectInviteTeam API 处理邀请
 * 
 * Events：
 * - back: 点击返回按钮时触发
 * 
 * 组件结构：
 * - 卡片头部：标题和返回按钮
 * - 装饰线条：三色渐变装饰条
 * - 列表主体：邀请列表表格和分页
 */
export default {
  name: 'TeamJoinList',
  data () {
    return {
      // 邀请列表加载状态
      invitesLoading: false,
      // 邀请状态筛选（2: 邀请中, 1: 已接受, 3: 已拒绝）
      invitesStatus: 2,
      // 邀请列表分页：当前页码
      invitesPageNum: 1,
      // 邀请列表分页：每页数量
      invitesPageSize: 10,
      // 邀请列表总数
      invitesTotal: 0,
      // 邀请列表记录
      invitesRecords: [],
      // 保存操作状态（接受/拒绝邀请）
      saving: false
    }
  },
  computed: {
    /**
     * 邀请显示列表
     * 确保返回数组格式的邀请列表
     */
    inviteDisplayList () {
      return Array.isArray(this.invitesRecords) ? this.invitesRecords : []
    }
  },
  watch: {
    /**
     * 监听邀请记录变化
     * 当邀请记录更新时，检查项目名称滚动效果
     */
    invitesRecords: {
      handler () {
        this.$nextTick(() => {
          this.checkMarquee()
        })
      },
      deep: true
    }
  },
  /**
   * 组件创建时钩子
   * 获取邀请列表
   */
  created () {
    this.fetchInvites()
  },
  /**
   * 组件更新后钩子
   * 检查项目名称滚动效果
   */
  updated () {
    this.checkMarquee()
  },
  methods: {
    /**
     * ========== UI 效果相关方法 ==========
     */
    
    /**
     * 检查项目名称滚动效果
     * 当项目名称内容超出容器宽度时，添加滚动动画类
     */
    checkMarquee () {
      if (!this.$refs.projectNameContainer) return
      const containers = Array.isArray(this.$refs.projectNameContainer) ? this.$refs.projectNameContainer : [this.$refs.projectNameContainer]
      containers.forEach(container => {
        if (!container) return
        const content = container.querySelector('.team-join-list-project-name-content')
        if (!content) return
        // 重置以检查自然宽度
        content.classList.remove('team-join-list-scrolling')
        
        if (content.scrollWidth > container.clientWidth) {
           content.classList.add('team-join-list-scrolling')
        }
      })
    },
    
    /**
     * ========== 数据获取相关方法 ==========
     */
    
    /**
     * 获取邀请列表
     * 根据当前分页和状态筛选条件获取邀请列表
     */
    async fetchInvites () {
      try {
        this.invitesLoading = true
        const page = await getMyInviteTeamPage({ 
          page: this.invitesPageNum, 
          size: this.invitesPageSize, 
          memberStatus: this.invitesStatus 
        })
        const records = Array.isArray(page?.records) ? page.records : (Array.isArray(page) ? page : [])
        const total = typeof page?.total === 'number' ? page.total : (Array.isArray(records) ? records.length : 0)
        this.invitesRecords = records
        this.invitesTotal = total
      } catch (e) {
        this.invitesRecords = []
        this.invitesTotal = 0
      } finally {
        this.invitesLoading = false
      }
    },
    
    /**
     * ========== 分页和筛选相关方法 ==========
     */
    
    /**
     * 邀请列表分页变化处理
     * @param {number} p - 新的页码
     */
    onInvitesPageChange (p) {
      this.invitesPageNum = p
      this.fetchInvites()
    },
    
    /**
     * 邀请状态筛选变化处理
     * 当状态筛选改变时，重置到第一页并重新获取数据
     */
    onInvitesStatusChange () {
      this.invitesPageNum = 1
      this.fetchInvites()
    },
    
    /**
     * ========== 邀请操作相关方法 ==========
     */
    
    /**
     * 接受邀请
     * 接受指定团队的邀请
     * @param {Object} row - 邀请记录行数据对象
     */
    async acceptItemInvite (row) {
      if (!row?.teamId) return
      try {
        this.saving = true
        const res = await acceptInviteTeam({ teamId: row.teamId })
        Message.success(res?.msg || '加入成功')
        // 刷新邀请列表
        await this.fetchInvites()
        // 触发全局事件，刷新团队列表和任务族列表
        if (this.$root && this.$root.$emit) {
          this.$root.$emit('refresh-my-teams')
          this.$root.$emit('refresh-my-taskgroups')
        }
      } catch (e) {
        // 错误由拦截器处理
      } finally {
        this.saving = false
      }
    },
    
    /**
     * 拒绝邀请
     * 拒绝指定团队的邀请
     * @param {Object} row - 邀请记录行数据对象
     */
    async rejectItemInvite (row) {
      if (!row?.teamId) return
      try {
        this.saving = true
        const res = await rejectInviteTeam({ teamId: row.teamId })
        Message.success(res?.msg || '已拒绝邀请')
        // 刷新邀请列表
        await this.fetchInvites()
      } catch (e) {
        // 错误由拦截器处理
      } finally {
        this.saving = false
      }
    },
    
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 格式化日期
     * 将时间字符串格式化为中文日期时间格式
     * @param {string} timeStr - 时间字符串
     * @returns {string} 格式化后的日期时间字符串
     */
    formatDate (timeStr) {
      if (!timeStr) return '-'
      const date = new Date(timeStr)
      if (isNaN(date.getTime())) return timeStr
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      const hh = String(date.getHours()).padStart(2, '0')
      const mm = String(date.getMinutes()).padStart(2, '0')
      const ss = String(date.getSeconds()).padStart(2, '0')
      return `${y}年${m}月${d}日 ${hh}:${mm}:${ss}`
    }
  }
}
</script>

<style>
@import '@/assets/css/team-create/team-join-list.css';
</style>
