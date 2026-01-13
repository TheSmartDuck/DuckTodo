<template>
  <!-- 团队成员管理面板根容器 -->
  <div class="team-members-panel">
    <!-- 主卡片容器 -->
    <div class="team-members-panel-card">
      <!-- 面板头部：标题和操作按钮 -->
      <div class="team-members-panel-header">
        <span class="team-members-panel-title">MEMBER LIST / 成员列表</span>
        <div class="team-members-panel-header-actions">
          <!-- 成员搜索输入框 -->
          <el-input 
            v-model="memberQuery" 
            placeholder="SEARCH MEMBER" 
            size="small" 
            class="team-members-panel-search-input" 
            clearable 
            @clear="$emit('refresh')" 
            @keyup.enter.native="$emit('refresh', memberQuery)"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <!-- 刷新按钮 -->
          <button class="team-members-panel-action-btn team-members-panel-refresh-btn" @click="$emit('refresh')">
            <i class="el-icon-refresh"></i> REFRESH
          </button>
          <!-- 添加成员按钮（仅 OWNER 或 MANAGER 可见） -->
          <button 
            v-if="canAddMember"
            class="team-members-panel-action-btn team-members-panel-add-btn" 
            @click="openAddDrawer"
          >
            <i class="el-icon-plus"></i> ADD MEMBER
          </button>
        </div>
      </div>
      
      <!-- 表格包装器 -->
      <div class="team-members-panel-table-wrapper">
        <el-table :data="members" size="small" style="width: 100%" height="100%" class="team-members-panel-mission-table">
          <!-- 头像列 -->
          <el-table-column label="AVATAR" width="70" align="center">
            <template slot-scope="{ row }">
              <el-avatar :size="28" :src="row.userAvatar" icon="el-icon-user-solid" shape="square" class="team-members-panel-user-avatar"></el-avatar>
            </template>
          </el-table-column>
          <!-- 成员名称列 -->
          <el-table-column prop="userName" label="NAME / 成员" min-width="140">
            <template slot-scope="{ row }">
              <span style="font-weight: 700;">{{ row.userName }}</span>
            </template>
          </el-table-column>
          <!-- 角色列 -->
          <el-table-column prop="memberRole" label="ROLE / 角色" width="100">
            <template slot-scope="{ row }">
              <span v-if="String(row.memberRole) === '0'" class="team-members-panel-role-tag team-members-panel-role-tag-owner">OWNER</span>
              <span v-else-if="String(row.memberRole) === '1'" class="team-members-panel-role-tag team-members-panel-role-tag-manager">MANAGER</span>
              <span v-else class="team-members-panel-role-tag team-members-panel-role-tag-member">MEMBER</span>
            </template>
          </el-table-column>
          <!-- 状态列 -->
          <el-table-column prop="memberStatus" label="STATUS / 状态" width="120">
            <template slot-scope="{ row }">
              <!-- 状态值：0-禁用，1-正常（已加入），2-邀请中，3-已拒绝 -->
              <span v-if="Number(row.memberStatus) === 0" class="team-members-panel-status-text team-members-panel-status-text-info team-members-panel-status-text-bold">DISABLED</span>
              <span v-else-if="Number(row.memberStatus) === 1" class="team-members-panel-status-text team-members-panel-status-text-success team-members-panel-status-text-bold">JOINED</span>
              <span v-else-if="Number(row.memberStatus) === 2" class="team-members-panel-status-text team-members-panel-status-text-warning team-members-panel-status-text-bold">INVITING</span>
              <span v-else-if="Number(row.memberStatus) === 3" class="team-members-panel-status-text team-members-panel-status-text-danger team-members-panel-status-text-bold">REJECTED</span>
              <span v-else class="team-members-panel-status-text team-members-panel-status-text-info team-members-panel-status-text-bold">UNKNOWN</span>
            </template>
          </el-table-column>
          <!-- 邮箱列 -->
          <el-table-column prop="userEmail" label="EMAIL / 邮箱" min-width="180" show-overflow-tooltip>
            <template slot-scope="{ row }">
              <span v-if="row.userEmail" class="team-members-panel-contact-info">{{ row.userEmail }}</span>
              <span v-else class="team-members-panel-contact-info-empty">-</span>
            </template>
          </el-table-column>
          <!-- 联系电话列 -->
          <el-table-column prop="userPhone" label="PHONE / 联系电话" min-width="140" show-overflow-tooltip>
            <template slot-scope="{ row }">
              <span v-if="row.userPhone" class="team-members-panel-contact-info">{{ row.userPhone }}</span>
              <span v-else class="team-members-panel-contact-info-empty">-</span>
            </template>
          </el-table-column>
          <!-- 操作列 -->
          <el-table-column label="ACTIONS / 操作" width="320" fixed="right">
            <template slot-scope="{ row }">
              <div class="team-members-panel-actions-group">
                <!-- 修改角色按钮（仅当有权限时显示） -->
                <button 
                  v-if="canEditRole(row)" 
                  class="team-members-panel-btn-edit-split" 
                  @click="openEditRoleDialog(row)"
                >
                  <span class="team-members-panel-btn-edit-split-left">EDIT ROLE</span>
                  <span class="team-members-panel-btn-edit-split-sep">|</span>
                  <span class="team-members-panel-btn-edit-split-right">修改角色</span>
                </button>
                <!-- 重新邀请按钮（仅当状态为拒绝时显示） -->
                <button 
                  v-if="Number(row.memberStatus) === 3 && canReinvite(row)" 
                  class="team-members-panel-btn-text" 
                  @click="$emit('reinvite', row)"
                >
                  [RE-INVITE]
                </button>
                <!-- 删除按钮（邀请中时禁用，根据权限显示） -->
                <button 
                  v-if="canRemove(row)"
                  class="team-members-panel-btn-danger-split" 
                  :disabled="Number(row.memberStatus) === 2" 
                  @click="$emit('remove', row)"
                >
                  <span class="team-members-panel-btn-danger-split-left">DELETE</span>
                  <span class="team-members-panel-btn-danger-split-sep">|</span>
                  <span class="team-members-panel-btn-danger-split-right">删除</span>
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 添加成员抽屉 -->
    <el-drawer 
      title="ADD MEMBER / 添加成员"
      :visible.sync="drawerVisible" 
      size="35%" 
      direction="rtl" 
      custom-class="team-members-panel-mission-drawer"
      :append-to-body="true"
    >
      <div class="team-members-panel-drawer-body">
        <!-- 抽屉工具栏：用户搜索 -->
        <div class="team-members-panel-drawer-toolbar">
          <el-input 
            v-model="allUserQuery" 
            placeholder="SEARCH USER BY NAME" 
            size="small" 
            class="team-members-panel-search-input" 
            clearable 
            @clear="loadAllUsers" 
            @keyup.enter.native="loadAllUsers"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </div>
        <!-- 抽屉表格容器 -->
        <div class="team-members-panel-drawer-table-container">
          <el-table :data="allUsers" size="small" height="100%" class="team-members-panel-mission-table">
            <!-- 头像列 -->
            <el-table-column label="AVATAR" width="80" align="center">
              <template slot-scope="{ row }">
                <el-avatar :size="24" :src="row.userAvatar" icon="el-icon-user-solid" shape="square" class="team-members-panel-user-avatar"></el-avatar>
              </template>
            </el-table-column>
            <!-- 用户名列 -->
            <el-table-column prop="userName" label="NAME" min-width="100" />
            <!-- 邮箱列 -->
            <el-table-column prop="userEmail" label="EMAIL" min-width="100" show-overflow-tooltip />
            <!-- 状态列 -->
            <el-table-column label="STATUS" width="100">
              <template slot-scope="{ row }">
                <span v-if="isAlreadyJoined(row.userId)" class="team-members-panel-status-text team-members-panel-status-text-success">JOINED</span>
                <span v-else-if="isInviting(row.userId)" class="team-members-panel-status-text team-members-panel-status-text-warning">WAITING</span>
                <span v-else class="team-members-panel-status-text team-members-panel-status-text-info">READY</span>
              </template>
            </el-table-column>
            <!-- 操作列 -->
            <el-table-column label="ACT / 操作" width="120" fixed="right" align="center">
              <template slot-scope="{ row }">
                <button 
                  class="team-members-panel-btn-sm" 
                  :class="{
                    'team-members-panel-btn-sm-added': isAlreadyJoined(row.userId),
                    'team-members-panel-btn-sm-waiting': isInviting(row.userId)
                  }"
                  :disabled="!canInvite(row.userId)" 
                  @click="invite(row)"
                  style="margin-left: 20px;"
                >
                  <span v-if="isAlreadyJoined(row.userId)">JOINED</span>
                  <span v-else-if="isInviting(row.userId)">WAITING</span>
                  <span v-else>ADD</span>
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>

    <!-- 修改角色对话框 -->
    <el-dialog
      title="EDIT ROLE / 修改角色"
      :visible.sync="editRoleDialogVisible"
      width="400px"
      custom-class="team-members-panel-edit-role-dialog"
    >
      <div class="team-members-panel-edit-role-content">
        <div class="team-members-panel-edit-role-info">
          <span class="team-members-panel-edit-role-label">MEMBER / 成员：</span>
          <span class="team-members-panel-edit-role-name">{{ editingMember.userName || editingMember.userId }}</span>
        </div>
        <div class="team-members-panel-edit-role-select">
          <span class="team-members-panel-edit-role-label">ROLE / 角色：</span>
          <el-select v-model="editingRole" placeholder="SELECT ROLE" class="team-members-panel-role-select">
            <el-option label="MANAGER / 管理者" :value="1"></el-option>
            <el-option label="MEMBER / 普通成员" :value="2"></el-option>
          </el-select>
        </div>
      </div>
      <div slot="footer" class="team-members-panel-edit-role-footer">
        <button class="team-members-panel-btn-text" @click="editRoleDialogVisible = false">CANCEL</button>
        <button class="team-members-panel-action-btn" @click="confirmEditRole">CONFIRM</button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
/**
 * 团队成员管理面板组件
 * 
 * 功能：
 * 1. 显示团队成员列表（头像、名称、角色、状态）
 * 2. 支持成员搜索功能
 * 3. 支持添加新成员（通过抽屉选择用户）
 * 4. 支持重新邀请被拒绝的成员
 * 5. 支持删除团队成员
 * 
 * Props：
 * - members: 团队成员列表数组
 *   - userId: 用户ID
 *   - userName: 用户名称
 *   - userAvatar: 用户头像URL
 *   - memberRole: 成员角色（0: OWNER, 1: MANAGER, 2: MEMBER）
 *   - memberStatus: 成员状态（0: DISABLED, 1: JOINED, 2: INVITING, 3: REJECTED）
 * - currentUserId: 当前用户ID
 * - currentUserRole: 当前用户在团队中的角色（0: OWNER, 1: MANAGER, 2: MEMBER）
 * - teamId: 团队ID
 * 
 * Events：
 * - refresh: 刷新成员列表（可选参数：搜索关键词）
 * - invite: 邀请用户加入团队（参数：用户对象）
 * - reinvite: 重新邀请被拒绝的成员（参数：成员对象）
 * - remove: 移除团队成员（参数：成员对象）
 * - update-role: 更新成员角色（参数：{ userId, userRole }）
 */
export default {
  name: 'TeamMembersPanel',
  props: {
    /**
     * 团队成员列表
     * @type {Array}
     */
    members: {
      type: Array,
      default: () => []
    },
    
    /**
     * 当前用户ID
     * @type {String}
     */
    currentUserId: {
      type: String,
      default: ''
    },
    
    /**
     * 当前用户在团队中的角色
     * @type {Number}
     * @description 0: OWNER, 1: MANAGER, 2: MEMBER
     */
    currentUserRole: {
      type: Number,
      default: null
    },
    
    /**
     * 团队ID
     * @type {String}
     */
    teamId: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      /**
       * 成员搜索关键词
       * @type {String}
       */
      memberQuery: '',
      
      /**
       * 添加成员抽屉显示状态
       * @type {Boolean}
       */
      drawerVisible: false,
      
      /**
       * 所有用户列表（用于添加成员抽屉）
       * @type {Array}
       */
      allUsers: [],
      
      /**
       * 用户搜索关键词（用于添加成员抽屉）
       * @type {String}
       */
      allUserQuery: '',
      
      /**
       * 修改角色对话框显示状态
       * @type {Boolean}
       */
      editRoleDialogVisible: false,
      
      /**
       * 正在编辑的成员对象
       * @type {Object}
       */
      editingMember: {},
      
      /**
       * 正在编辑的角色值
       * @type {Number}
       */
      editingRole: null
    }
  },
  computed: {
    /**
     * 判断当前用户是否可以添加成员
     * 只有 OWNER (0) 或 MANAGER (1) 可以添加成员
     * @returns {Boolean} 是否可以添加成员
     */
    canAddMember () {
      const role = this.currentUserRole
      // OWNER (0) 或 MANAGER (1) 可以添加成员
      return role === 0 || role === 1
    }
  },
  methods: {
    /**
     * 打开添加成员抽屉
     * 显示抽屉并加载所有用户列表
     */
    openAddDrawer () {
      this.drawerVisible = true
      this.loadAllUsers()
    },
    
    /**
     * 加载所有用户列表
     * 从 API 获取用户列表，支持按用户名搜索
     */
    async loadAllUsers () {
      try {
        const { listUsers } = require('@/api/user-api')
        const res = await listUsers({ page: 1, size: 200, userName: this.allUserQuery })
        // 兼容不同的响应格式
        const records = Array.isArray(res) ? res : (res && (res.records || res.list || res.rows))
        this.allUsers = Array.isArray(records) ? records : []
      } catch (e) {
        console.error('Failed to load users:', e)
        this.allUsers = []
      }
    },
    
    /**
     * 判断用户是否已加入团队
     * @param {String|Number} userId - 用户ID
     * @returns {Boolean} 是否已加入（状态为 1-正常）
     */
    isAlreadyJoined (userId) {
      const uid = String(userId || '')
      return (this.members || []).some(m => 
        String(m.userId || '') === uid && Number(m.memberStatus) === 1
      )
    },
    
    /**
     * 判断用户是否正在邀请中
     * @param {String|Number} userId - 用户ID
     * @returns {Boolean} 是否正在邀请中（状态为 2-邀请中）
     */
    isInviting (userId) {
      const uid = String(userId || '')
      return (this.members || []).some(m => 
        String(m.userId || '') === uid && Number(m.memberStatus) === 2
      )
    },
    
    /**
     * 判断是否可以邀请用户
     * @param {String|Number} userId - 用户ID
     * @returns {Boolean} 是否可以邀请（未加入且未在邀请中）
     */
    canInvite (userId) {
      const uid = String(userId || '')
      if (!uid) return false
      return !this.isAlreadyJoined(uid) && !this.isInviting(uid)
    },
    
    /**
     * 邀请用户加入团队
     * @param {Object} row - 用户对象
     */
    invite (row) {
      this.$emit('invite', row)
    },
    
    /**
     * 判断是否可以编辑角色
     * 只有 OWNER 和 MANAGER 可以编辑角色，且不能编辑自己和 OWNER
     * @param {Object} row - 成员对象
     * @returns {Boolean} 是否可以编辑角色
     */
    canEditRole (row) {
      // 当前用户必须是 OWNER 或 MANAGER
      if (this.currentUserRole !== 0 && this.currentUserRole !== 1) {
        return false
      }
      
      // 不能编辑自己
      if (String(row.userId || '') === String(this.currentUserId || '')) {
        return false
      }
      
      // 不能编辑 OWNER
      if (Number(row.memberRole) === 0) {
        return false
      }
      
      // 只能编辑已加入的成员（状态为 1）
      if (Number(row.memberStatus) !== 1) {
        return false
      }
      
      return true
    },
    
    /**
     * 判断是否可以重新邀请
     * 只有 OWNER 和 MANAGER 可以重新邀请
     * @param {Object} row - 成员对象
     * @returns {Boolean} 是否可以重新邀请
     */
    canReinvite (row) {
      return this.currentUserRole === 0 || this.currentUserRole === 1
    },
    
    /**
     * 判断是否可以删除成员
     * 只有 OWNER 和 MANAGER 可以删除成员，且不能删除 OWNER
     * @param {Object} row - 成员对象
     * @returns {Boolean} 是否可以删除成员
     */
    canRemove (row) {
      // 当前用户必须是 OWNER 或 MANAGER
      if (this.currentUserRole !== 0 && this.currentUserRole !== 1) {
        return false
      }
      
      // 不能删除 OWNER
      if (Number(row.memberRole) === 0) {
        return false
      }
      
      return true
    },
    
    /**
     * 打开修改角色对话框
     * @param {Object} row - 成员对象
     */
    openEditRoleDialog (row) {
      this.editingMember = row
      this.editingRole = Number(row.memberRole)
      this.editRoleDialogVisible = true
    },
    
    /**
     * 确认修改角色
     */
    confirmEditRole () {
      if (!this.editingMember.userId || this.editingRole === null) {
        this.$message.warning('请选择角色')
        return
      }
      
      // 如果角色没有变化，直接关闭对话框
      if (Number(this.editingMember.memberRole) === Number(this.editingRole)) {
        this.editRoleDialogVisible = false
        return
      }
      
      // 触发更新角色事件
      this.$emit('update-role', {
        userId: this.editingMember.userId,
        userRole: this.editingRole
      })
      
      this.editRoleDialogVisible = false
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/team-detail/team-members-panel.css';
</style>
