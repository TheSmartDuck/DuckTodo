<template>
  <!-- 创建团队表单根容器 -->
  <div class="team-create-form-main-card">
    <!-- 卡片头部 -->
    <div class="team-create-form-card-header-retro">
      <!-- 头部标题文字 -->
      <span class="team-create-form-header-title-text">CREATE_TEAM // 创建项目团队</span>
      <!-- 头部右侧操作组 -->
      <div class="team-create-form-header-right-group" style="display:flex; align-items:center;">
        <!-- 创建项目按钮 -->
        <el-button 
          size="mini" 
          type="primary" 
          :loading="saving" 
          class="team-create-form-retro-btn-primary" 
          @click="submit" 
          style="margin-right:12px;"
        >
          CREATE PROJECT / 创建
        </el-button>
        <!-- 返回按钮 -->
        <el-button 
          size="mini" 
          class="team-create-form-retro-btn" 
          icon="el-icon-back" 
          @click="$emit('back')" 
          style="margin-right:12px;"
        >
          BACK / 返回
        </el-button>
        <!-- 头部装饰元素 -->
        <div class="team-create-form-header-deco">
          <span></span><span></span><span></span>
        </div>
      </div>
    </div>
    
    <!-- 装饰线条（三色渐变） -->
    <div class="team-create-form-retro-deco-line">
      <div class="team-create-form-line-seg" style="background: var(--avionics-mustard);"></div>
      <div class="team-create-form-line-seg" style="background: var(--oxidized-sage);"></div>
      <div class="team-create-form-line-seg" style="background: var(--alarm-burgundy);"></div>
    </div>
    
    <!-- 表单主体内容区域 -->
    <div class="team-create-form-body">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small" class="team-create-form-retro-form">
        
        <!-- 第一部分：基本信息（叙事日志风格） -->
        <div class="team-create-form-section team-create-form-narrative-log">
          <!-- 部分头部 -->
          <div class="team-create-form-section-header">
            <!-- 方形标记点（黄色） -->
            <div class="team-create-form-square-bullet team-create-form-yellow"></div>
            <!-- 部分标题 -->
            <span class="team-create-form-section-title">BASIC_INFO // 基本信息</span>
          </div>
          <!-- 部分内容 -->
          <div class="team-create-form-section-content">
            <!-- 团队名称输入 -->
            <el-form-item label="团队名称" prop="teamName">
              <el-input v-model="form.teamName" placeholder="请输入团队名称" />
            </el-form-item>
            <!-- 团队描述输入 -->
            <el-form-item label="团队描述" prop="teamDescription">
              <el-input type="textarea" :rows="3" v-model="form.teamDescription" placeholder="请输入团队描述（可选）" />
            </el-form-item>
            <!-- 团队头像输入 -->
            <el-form-item label="团队头像" prop="teamAvatar">
              <div class="team-create-form-avatar-row">
                <el-input v-model="form.teamAvatar" placeholder="头像直链 URL（可选）" />
                <el-upload
                  :action="''"
                  :show-file-list="false"
                  :http-request="doUpload"
                  accept="image/*"
                  style="margin-left:8px"
                >
                  <el-button size="mini" class="team-create-form-retro-btn">上传图片</el-button>
                </el-upload>
              </div>
            </el-form-item>
            <!-- 团队状态选择 -->
            <el-form-item label="团队状态" prop="teamStatus">
              <el-select 
                v-model="form.teamStatus" 
                placeholder="请选择团队状态" 
                style="width:100%" 
                popper-class="team-create-form-retro-select-dropdown"
              >
                <el-option
                  v-for="(label, value) in teamStatusOptions"
                  :key="value"
                  :label="label"
                  :value="Number(value)"
                />
              </el-select>
            </el-form-item>
          </div>
          <!-- 虚线分隔符 -->
          <div class="team-create-form-dashed-line-separator"></div>
        </div>

        <!-- 第二部分：邀请成员（社区传输风格） -->
        <div class="team-create-form-section team-create-form-community-transmission">
          <!-- 部分头部 -->
          <div class="team-create-form-section-header">
            <!-- 方形标记点（橙色） -->
            <div class="team-create-form-square-bullet team-create-form-orange"></div>
            <!-- 部分标题 -->
            <span class="team-create-form-section-title">MEMBER_INVITE // 邀请成员</span>
          </div>
          <!-- 部分内容（灰色背景框） -->
          <div class="team-create-form-section-content team-create-form-grey-box">
            <el-form-item label-width="0">
              <!-- 用户选择框容器 -->
              <div class="team-create-form-user-select-box">
                <!-- 用户选择工具栏 -->
                <div class="team-create-form-user-select-toolbar">
                  <el-input
                    v-model="userQuery"
                    clearable
                    placeholder="按用户名搜索"
                    size="small"
                    style="width: 240px;"
                    @keyup.enter.native="fetchUserList"
                  />
                  <el-button 
                    size="mini" 
                    style="margin-left:8px" 
                    :loading="userListLoading" 
                    class="team-create-form-retro-btn" 
                    @click="fetchUserList"
                  >
                    SEARCH
                  </el-button>
                </div>
                <!-- 邀请标签列表 -->
                <div class="team-create-form-invite-tags" v-if="inviteList.length">
                  <el-tag
                    v-for="(u, idx) in inviteList"
                    :key="(u.userId || u) + '_' + idx"
                    size="small"
                    :closable="!u.locked"
                    class="team-create-form-invite-tag team-create-form-retro-tag"
                    @close="removeInvite(idx)"
                    style="margin-right:6px; margin-top:6px;"
                  >
                    <span class="team-create-form-avatar" v-if="u.userAvatar">
                      <img :src="u.userAvatar" alt="" />
                    </span>
                    <span class="team-create-form-name">{{ u.userName || u.userId || u }}</span>
                    <span class="team-create-form-role" style="margin-left:4px;" v-if="u.role">
                      [{{ getRoleLabel(u.role) }}]
                    </span>
                  </el-tag>
                </div>
                <!-- 用户列表表格 -->
                <el-table
                  ref="userTableRef"
                  :data="userList"
                  v-loading="userListLoading"
                  height="260"
                  class="team-create-form-retro-table"
                  style="width: 100%; margin-top: 8px;"
                  :row-key="row => row.userId"
                >
                  <!-- 头像列 -->
                  <el-table-column label="AVATAR" width="72">
                    <template slot-scope="scope">
                      <img 
                        :src="scope.row.userAvatar" 
                        alt="" 
                        style="width:32px;height:32px;border: 1px solid #1a1a1a; border-radius:0; object-fit:cover;" 
                      />
                    </template>
                  </el-table-column>
                  <!-- 用户名列 -->
                  <el-table-column prop="userName" label="USERNAME/用户名" min-width="160" />
                  <!-- 联系方式列 -->
                  <el-table-column prop="userPhone" label="PHONE/联系方式" min-width="140" />
                  <!-- 电子邮箱列（带滚动效果） -->
                  <el-table-column label="EMAIL/电子邮箱" min-width="200">
                    <template slot-scope="scope">
                      <div class="team-create-form-email-scroll-container" ref="emailContainer">
                        <span class="team-create-form-email-content">{{ scope.row.userEmail }}</span>
                      </div>
                    </template>
                  </el-table-column>
                  <!-- 操作列（固定右侧） -->
                  <el-table-column label="ACTION/角色" width="240" fixed="right">
                    <template slot-scope="scope">
                      <el-select 
                        v-model="scope.row.selectedRole" 
                        size="mini" 
                        style="width: 120px; margin-right:8px;" 
                        :disabled="isInvited(scope.row.userId)" 
                        popper-class="team-create-form-retro-select-dropdown"
                      >
                        <el-option
                          v-for="(label, value) in inviteRoleOptions"
                          :key="value"
                          :label="label"
                          :value="Number(value)"
                        />
                      </el-select>
                      <el-button 
                        v-if="!isInvited(scope.row.userId)" 
                        type="primary" 
                        size="mini" 
                        class="team-create-form-retro-btn-small" 
                        @click="addInviteUser(scope.row)"
                      >
                        ADD
                      </el-button>
                      <el-button 
                        v-else 
                        size="mini" 
                        disabled 
                        class="team-create-form-retro-btn-disabled"
                      >
                        ADDED
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <!-- 用户选择分页 -->
                <div class="team-create-form-user-select-pagination">
                  <el-pagination
                    layout="prev, pager, next"
                    :current-page="userPageNum"
                    :page-size="userPageSize"
                    :total="userTotal"
                    @current-change="onUserPageChange"
                  />
                </div>
              </div>
            </el-form-item>
          </div>
        </div>

      </el-form>
    </div>
  </div>
</template>

<script>
import { Message } from 'element-ui'
import { createTeam } from '@/api/team-api'
import baseApi from '@/api/base-api'
import { listUsers, getMe, getUserById } from '@/api/user-api'
import { teamStatusByCode, userRoleByCode } from '@/utils/enums'

/**
 * 创建团队表单组件
 * 
 * 功能：
 * 1. 创建新的团队项目
 * 2. 设置团队基本信息（名称、描述、头像、状态）
 * 3. 邀请团队成员并设置角色
 * 4. 搜索和选择用户
 * 5. 上传团队头像
 * 
 * 数据来源：
 * - 通过 team-api 的 createTeam API 创建团队
 * - 通过 user-api 的 listUsers、getMe API 获取用户信息
 * - 通过 base-api 的 uploadAvatar API 上传头像
 * 
 * Events：
 * - success: 团队创建成功时触发
 * - back: 点击返回按钮时触发
 * 
 * 组件结构：
 * - 卡片头部：标题和操作按钮
 * - 装饰线条：三色渐变装饰条
 * - 表单主体：基本信息部分和邀请成员部分
 */
export default {
  name: 'TeamCreateForm',
  data () {
    return {
      // 表单数据
      form: {
        teamName: '',           // 团队名称
        teamDescription: '',    // 团队描述
        teamAvatar: '',         // 团队头像 URL
        teamStatus: 1           // 团队状态（默认：运行中）
      },
      // 提交状态
      saving: false,
      // 邀请输入（已废弃，保留以防引用）
      inviteInput: '',
      // 邀请列表
      inviteList: [],
      // 当前用户ID
      currentUserId: '',
      // 用户搜索查询
      userQuery: '',
      // 用户列表
      userList: [],
      // 用户列表加载状态
      userListLoading: false,
      // 用户列表分页：当前页码
      userPageNum: 1,
      // 用户列表分页：每页数量
      userPageSize: 10,
      // 用户列表总数
      userTotal: 0,
      // 表单验证规则
      rules: {
        teamName: [
          { required: true, message: '请输入团队名称', trigger: 'blur' },
          { min: 2, message: '团队名称至少 2 位', trigger: 'blur' }
        ],
        teamStatus: [
          { required: true, message: '请选择团队状态', trigger: 'change' }
        ]
      },
      // 团队状态选项（从枚举工具类获取）
      teamStatusOptions: teamStatusByCode,
      // 邀请角色选项（仅显示管理者和普通成员，跳过所有者）
      inviteRoleOptions: {
        1: userRoleByCode[1], // 管理者
        2: userRoleByCode[2]  // 普通成员
      }
    }
  },
  /**
   * 组件创建时钩子
   * 加载当前用户信息并获取用户列表
   */
  created () {
    this.loadCurrentUser()
    this.fetchUserList()
  },
  watch: {
    /**
     * 监听用户列表变化
     * 当用户列表更新时，检查邮箱滚动效果
     */
    userList: {
      handler () {
        this.$nextTick(() => {
          this.checkMarquee()
        })
      },
      deep: true
    }
  },
  /**
   * 组件更新后钩子
   * 检查邮箱滚动效果
   */
  updated () {
    this.checkMarquee()
  },
  methods: {
    /**
     * ========== UI 效果相关方法 ==========
     */
    
    /**
     * 检查邮箱滚动效果
     * 当邮箱内容超出容器宽度时，添加滚动动画类
     */
    checkMarquee () {
      if (!this.$refs.emailContainer) return
      const containers = Array.isArray(this.$refs.emailContainer) ? this.$refs.emailContainer : [this.$refs.emailContainer]
      containers.forEach(container => {
        if (!container) return
        const content = container.querySelector('.team-create-form-email-content')
        if (!content) return
        content.classList.remove('team-create-form-scrolling')
        if (content.scrollWidth > container.clientWidth) {
           content.classList.add('team-create-form-scrolling')
        }
      })
    },
    
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 获取角色标签
     * 根据角色值返回对应的中文标签
     * @param {number} roleVal - 角色值
     * @returns {string} 角色标签
     */
    getRoleLabel(roleVal) {
      return userRoleByCode[roleVal] || roleVal
    },
    
    /**
     * ========== 用户相关方法 ==========
     */
    
    /**
     * 加载当前用户信息
     * 获取当前登录用户信息，并自动添加到邀请列表（作为所有者，锁定状态）
     */
    async loadCurrentUser () {
      try {
        const me = await getMe()
        const uid = (me && me.userId) ? me.userId.toString() : ''
        this.currentUserId = uid
        if (uid) {
          const exists = this.inviteList.some(u => (u.userId || u) === uid)
          if (!exists) {
            // 所有者角色为 0，锁定状态（不可删除）
            this.inviteList.push({ 
              userId: uid, 
              userName: me?.userName || '我', 
              userAvatar: me?.userAvatar || '', 
              locked: true, 
              role: 0 
            })
          }
        }
      } catch (e) {
        // 静默失败
      }
    },
    
    /**
     * 获取用户列表
     * 根据搜索条件分页获取用户列表，排除当前用户
     */
    async fetchUserList () {
      this.userListLoading = true
      try {
        const res = await listUsers({ 
          page: this.userPageNum, 
          size: this.userPageSize, 
          userName: this.userQuery 
        })
        const records = Array.isArray(res) ? res : (res?.records || res?.list || res?.rows || [])
        const total = Array.isArray(res) ? (res.length || 0) : (res?.total || 0)
        const myId = (this.currentUserId || '').toString()
        // 过滤掉当前用户，并标记已邀请的用户
        this.userList = (records || [])
          .filter(u => (u.userId || '').toString() !== myId)
          .map(u => {
            const invited = this.inviteList.find(x => (x.userId || x) === u.userId)
            return {
              userId: u.userId,
              userName: u.userName,
              userAvatar: u.userAvatar,
              userPhone: u.userPhone,
              userEmail: u.userEmail,
              // 默认角色为普通成员（2），如果已邀请则使用邀请时的角色
              selectedRole: invited ? (invited.role || 2) : 2
            }
          })
        this.userTotal = total
      } catch (e) {
        this.userList = []
        this.userTotal = 0
      } finally {
        this.userListLoading = false
      }
    },
    
    /**
     * 用户列表分页变化处理
     * @param {number} p - 新的页码
     */
    onUserPageChange (p) {
      this.userPageNum = p
      this.fetchUserList()
    },
    
    /**
     * 检查用户是否已邀请
     * @param {string|number} uid - 用户ID
     * @returns {boolean} 是否已邀请
     */
    isInvited (uid) {
      const id = (uid || '').toString()
      return this.inviteList.some(u => (u.userId || u) === id)
    },
    
    /**
     * 添加邀请用户
     * 将用户添加到邀请列表
     * @param {Object} row - 用户行数据对象
     */
    addInviteUser (row) {
      const uid = row && row.userId
      if (!uid) return
      const exists = this.inviteList.some(u => (u.userId || u) === uid)
      if (!exists) {
        this.inviteList.push({ 
          userId: row.userId, 
          userName: row.userName, 
          userAvatar: row.userAvatar, 
          role: (row.selectedRole || 2) 
        })
      }
    },
    
    /**
     * 移除邀请用户
     * 从邀请列表中移除指定索引的用户
     * @param {number} index - 用户在邀请列表中的索引
     */
    removeInvite (index) {
      this.inviteList.splice(index, 1)
    },
    
    /**
     * ========== 文件上传相关方法 ==========
     */
    
    /**
     * 执行文件上传
     * 上传团队头像图片
     * @param {Object} options - Element UI Upload 组件的上传选项
     */
    async doUpload (options) {
      try {
        const file = options.file
        const urlObj = await baseApi.uploadAvatar(file)
        const url = typeof urlObj === 'string' ? urlObj : (urlObj?.url || urlObj)
        if (url) {
          this.form.teamAvatar = url
          Message.success('上传成功')
        } else {
          Message.error('上传失败')
        }
        options.onSuccess && options.onSuccess(urlObj)
      } catch (e) {
        options.onError && options.onError(e)
      }
    },
    
    /**
     * ========== 表单提交相关方法 ==========
     */
    
    /**
     * 提交表单
     * 验证表单并创建团队
     */
    submit () {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        try {
          this.saving = true
          // 构建邀请成员列表（排除锁定的用户，即当前用户）
          const invitedMemberList = this.inviteList
            .filter(u => !u.locked)
            .map(u => ({
              userId: (u.userId || u),
              userRole: u.role
            }))
          // 构建提交参数
          const params = { ...this.form, invitedMemberList }
          // 调用 API 创建团队
          const res = await createTeam(params)
          if (res && res.teamId) {
            Message.success('团队创建成功')
          } else {
            Message.success('创建成功')
          }
          // 触发全局事件，刷新团队和任务组列表
          if (this.$root && this.$root.$emit) {
            this.$root.$emit('refresh-my-teams')
            this.$root.$emit('refresh-my-taskgroups')
          }
          // 触发成功事件
          this.$emit('success')
        } catch (e) {
          // 错误由拦截器处理
        } finally {
          this.saving = false
        }
      })
    }
  }
}
</script>

<style>
@import '@/assets/css/team-create/team-create-form.css';
</style>
