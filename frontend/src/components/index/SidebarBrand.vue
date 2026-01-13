<template>
  <div class="sidebar-brand-wrapper">
    <!-- 顶部装饰条：模拟磁带/控制台的条纹 -->
    <div class="sidebar-brand-header-strip">
      <div class="sidebar-brand-strip-block" style="background-color: var(--avionics-mustard);"></div>
      <div class="sidebar-brand-strip-block" style="background-color: var(--oxidized-sage);"></div>
      <div class="sidebar-brand-strip-block" style="background-color: var(--alarm-burgundy);"></div>
      <div class="sidebar-brand-strip-line"></div>
    </div>

    <!-- 品牌身份区 -->
    <div class="sidebar-brand-panel">
      <div class="sidebar-brand-info">
        <div class="sidebar-brand-info-label">OPERATOR //</div>
        <div class="sidebar-brand-info-name-container">
          <div class="sidebar-brand-name-scroll-wrapper" ref="scrollWrapper">
            <div 
              class="sidebar-brand-name-scroll-content" 
              ref="scrollContent"
              :class="{ 'sidebar-brand-is-scrolling': isOverflowing }"
            >
              <span class="sidebar-brand-info-name" :title="userName">{{ userName || 'UNKNOWN' }}</span>
              <span v-if="isOverflowing" class="sidebar-brand-info-name-duplicate">{{ userName || 'UNKNOWN' }}</span>
            </div>
          </div>
          <span class="sidebar-brand-status-dot" :style="statusStyle"></span>
        </div>
      </div>
      
      <div class="sidebar-brand-controls">
        <el-dropdown @command="onCommand" trigger="click" placement="bottom-start">
          <div class="sidebar-brand-control-btn" title="SYSTEM MENU">
            <i class="el-icon-s-operation"></i>
          </div>
          <el-dropdown-menu slot="dropdown" class="sidebar-brand-dropdown-menu">
            <el-dropdown-item command="profile">
              <span class="sidebar-brand-menu-code">>>[01]</span>
              <span class="sidebar-brand-menu-text">个人设置</span>
              <span class="sidebar-brand-menu-text" style="font-size: 10px;">&nbsp;PROFILE</span>
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <span class="sidebar-brand-menu-code">>>[02]</span>
              <span class="sidebar-brand-menu-text">登出</span>
              <span class="sidebar-brand-menu-text" style="font-size: 10px;">&nbsp;LOGOUT</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!-- 搜索区 -->
    <div class="sidebar-brand-search-panel">
      <div class="sidebar-brand-search-label">>> echo "SEARCH_TASK_DB"</div>
      <div class="sidebar-brand-search-input-wrapper">
        <el-input 
          size="small" 
          clearable 
          prefix-icon="el-icon-search" 
          placeholder="INPUT_QUERY..." 
          class="sidebar-brand-retro-search-input"
        />
        <div class="sidebar-brand-input-corner"></div>
      </div>
    </div>
  </div>
</template>

<script>
import { health, logout } from '@/api/base-api'
import { getMe } from '@/api/user-api'

export default {
  name: 'SidebarBrand',
  data() {
    return {
      // 标记用户名是否超出容器宽度，用于控制滚动动画
      isOverflowing: false,
      // 系统健康状态，true表示正常，false表示异常
      isHealthy: true,
      // 健康检查定时器引用
      healthTimer: null,
      // 当前登录用户的用户名
      userName: ''
    }
  },
  computed: {
    /**
     * 计算状态指示灯的样式
     * 根据 isHealthy 状态返回不同的颜色和阴影样式
     * 正常状态显示氧化鼠尾草色(greenish)，异常状态显示警报勃艮第红(reddish)
     */
    statusStyle() {
      const color = this.isHealthy ? 'var(--oxidized-sage)' : 'var(--alarm-burgundy)'
      return {
        background: color,
        boxShadow: `0 0 4px ${color}`
      }
    }
  },
  watch: {
    /**
     * 监听用户名变化
     * 当用户名发生改变时，重新检查是否需要滚动显示
     * 使用 immediate: true 确保组件初始化时也会执行
     */
    userName: {
      handler() {
        this.$nextTick(() => {
          this.checkOverflow()
        })
      },
      immediate: true
    }
  },
  created() {
    // 组件创建时加载当前用户信息
    this.loadMe()
  },
  mounted() {
    // 组件挂载后进行一次溢出检查
    this.checkOverflow()
    // 监听窗口大小变化，以便重新计算溢出状态
    window.addEventListener('resize', this.checkOverflow)
    
    // 初始化健康检查，并设置每30分钟检查一次
    this.checkSystemHealth()
    this.healthTimer = setInterval(this.checkSystemHealth, 30 * 60 * 1000)
  },
  beforeDestroy() {
    // 组件销毁前移除 resize 事件监听
    window.removeEventListener('resize', this.checkOverflow)
    // 清除健康检查定时器
    if (this.healthTimer) {
      clearInterval(this.healthTimer)
      this.healthTimer = null
    }
  },
  methods: {
    /**
     * 加载当前用户信息
     * 调用 getMe API 获取用户信息并更新 userName
     * 如果失败则保持原值或默认为空
     */
    async loadMe() {
      try {
        const me = await getMe()
        this.userName = me && me.userName ? me.userName : this.userName
      } catch (e) {
        // 静默失败，不打断用户流程
      }
    },
    /**
     * 检查系统健康状态
     * 调用 health API，成功则设置 isHealthy 为 true，否则为 false
     */
    async checkSystemHealth() {
      try {
        await health()
        this.isHealthy = true
      } catch (e) {
        this.isHealthy = false
      }
    },
    /**
     * 处理下拉菜单指令
     * @param {string} cmd - 菜单项的 command 属性值
     */
    onCommand(cmd) {
      if (cmd === 'profile') this.goProfile()
      if (cmd === 'logout') this.logout()
    },
    /**
     * 跳转到个人设置页面
     * 如果当前不在个人设置页，则进行路由跳转
     */
    goProfile() {
      if (this.$route.name !== 'userinformation') {
        this.$router.push({ name: 'userinformation' }).catch(() => {})
      }
    },
    /**
     * 执行登出操作
     * 调用 logout API，无论成功失败都清除本地 token
     * 并跳转回登录页面
     */
    async logout() {
      try {
        await logout()
      } catch (e) {
        // 忽略登出 API 的错误，确保用户能退出前端状态
      } finally {
        try { localStorage.removeItem('token') } catch (err) {}
        this.$message && this.$message.success('已登出')
        this.$router.replace('/login')
      }
    },
    /**
     * 检查用户名是否超出显示区域
     * 比较内容宽度和容器宽度，设置 isOverflowing 状态
     * 用于控制跑马灯效果的启停
     */
    checkOverflow() {
      const wrapper = this.$refs.scrollWrapper
      const content = this.$refs.scrollContent
      if (wrapper && content) {
        // 临时移除滚动类以便准确测量自然宽度
        const wasScrolling = this.isOverflowing
        this.isOverflowing = false
        
        this.$nextTick(() => {
          // 获取单个名字 span 的宽度
          const nameSpan = content.querySelector('.sidebar-brand-info-name')
          if (nameSpan) {
            // 如果名字宽度大于容器宽度，则标记为溢出
            this.isOverflowing = nameSpan.offsetWidth > wrapper.offsetWidth
          }
        })
      }
    }
  }
}
</script>

<style src="@/assets/css/index/sidebar-brand.css" scoped></style>
