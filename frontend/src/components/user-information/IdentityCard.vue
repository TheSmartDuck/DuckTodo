<template>
  <!-- 身份卡片根容器：终端风格 -->
  <div class="identity-card-terminal">
    <!-- 顶部栏：标题和项目信息 -->
    <div class="identity-card-top-bar">
      <div class="identity-card-top-left">
        <i class="el-icon-monitor"></i>
        <span class="identity-card-top-title">TERMINAL</span>
        <span class="identity-card-top-subtitle">// 终端</span>
      </div>
      <div class="identity-card-top-right">
        <span class="identity-card-project-name">DuckTodo Pioneer Project</span>
        <div class="identity-card-status-dots">
          <span class="identity-card-dot identity-card-dot-active"></span>
          <span class="identity-card-dot"></span>
          <span class="identity-card-dot"></span>
        </div>
      </div>
    </div>

    <!-- 主体内容：仪表盘、信息、头像 -->
    <div class="identity-card-main-content">
      <!-- 左侧：仪表盘（头像） -->
      <div class="identity-card-gauge-section">
        <div class="identity-card-gauge-ring">
          <div class="identity-card-gauge-inner">
             <img :src="avatarUrl" class="identity-card-gauge-avatar" />
          </div>
        </div>
      </div>

      <!-- 中间：用户信息 -->
      <div class="identity-card-info-section">
        <!-- 用户角色标签 -->
        <div class="identity-card-user-role-tag">CURRENT USER</div>
        <!-- 用户名（打字机效果） -->
        <h1 class="identity-card-user-name-large">
          {{ typedUserName }}<span class="identity-card-cursor">_</span>
        </h1>
        <!-- 用户状态条：三色装饰条 -->
        <div class="identity-card-user-status-bar">
          <div class="identity-card-bar-segment identity-card-bar-segment-mustard"></div>
          <div class="identity-card-bar-segment identity-card-bar-segment-sage"></div>
          <div class="identity-card-bar-segment identity-card-bar-segment-burgundy"></div>
        </div>
        <!-- 用户引用：用户ID和备注 -->
        <div class="identity-card-user-quote">
          <div class="identity-card-user-id">UID: {{ user.userId || '00' }}</div>
          "{{ user.userRemark || 'System operational. Ready for input.' }}"
        </div>
        
      </div>

      <!-- 右侧：详细信息 -->
      <div class="identity-card-details-section">
         <!-- 邮箱信息组 -->
         <div class="identity-card-detail-group">
            <span class="identity-card-detail-label">EMAIL</span>
            <span class="identity-card-detail-value">{{ user.userEmail || 'N/A' }}</span>
         </div>
         <!-- 电话信息组 -->
         <div class="identity-card-detail-group">
            <span class="identity-card-detail-label">PHONE</span>
            <span class="identity-card-detail-value">{{ user.userPhone || 'N/A' }}</span>
         </div>
         <!-- 性别信息组 -->
         <div class="identity-card-detail-group">
             <span class="identity-card-detail-label">GENDER</span>
             <span class="identity-card-detail-value">{{ genderText }}</span>
         </div>
         <!-- 状态信息组 -->
         <div class="identity-card-detail-group">
             <span class="identity-card-detail-label">STATUS</span>
             <span class="identity-card-detail-value identity-card-detail-value-active">ACTIVE</span>
         </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 身份卡片组件
 * 
 * 功能：
 * 1. 显示用户的身份信息（终端风格）
 * 2. 显示用户头像（圆形仪表盘样式）
 * 3. 显示用户名（打字机动画效果）
 * 4. 显示用户状态条（三色装饰条）
 * 5. 显示用户详细信息（邮箱、电话、性别、状态）
 * 
 * 数据来源：
 * - 通过 props 接收用户信息对象
 * - 通过 props 接收是否有访问密钥的标志
 * 
 * 视觉效果：
 * - 终端风格界面
 * - 打字机动画效果（用户名逐字显示）
 * - 三色状态条（芥末黄、鼠尾草绿、酒红）
 * - 圆形头像仪表盘
 */
export default {
  name: 'IdentityCard',
  props: {
    // 用户信息对象
    user: {
      type: Object,
      default: () => ({})
    },
    // 是否有访问密钥
    hasAk: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      // 打字机效果显示的用户名
      typedUserName: '',
      // 打字机定时器
      typingTimer: null
    }
  },
  /**
   * 监听用户名变化
   * 当用户名改变时，重新启动打字机动画
   */
  watch: {
    'user.userName': {
      immediate: true,
      handler(val) {
        this.startTypingUserName(val || 'UNKNOWN')
      }
    }
  },
  computed: {
    /**
     * 头像URL
     * 优先使用用户头像，否则使用默认头像
     * @returns {string} 头像URL
     */
    avatarUrl() {
      return this.user.userAvatar || this.user.avatar || require('@/assets/imgs/default-user-avatar.png')
    },
    
    /**
     * 性别文本
     * 将性别代码转换为可读的文本
     * @returns {string} 性别文本（MALE/FEMALE/UNKNOWN）
     */
    genderText() {
      const sex = (this.user.userSex || '').toString().trim().toLowerCase()
      if (sex === '1' || sex === 'm' || sex === 'male') return 'MALE'
      if (sex === '0' || sex === 'f' || sex === 'female') return 'FEMALE'
      return 'UNKNOWN'
    }
  },
  methods: {
    /**
     * ========== 动画相关方法 ==========
     */
    
    /**
     * 启动用户名打字机动画
     * 逐字显示用户名，模拟打字机效果
     * @param {string} text - 要显示的用户名文本
     */
    startTypingUserName(text) {
      // 清除之前的定时器
      if (this.typingTimer) clearInterval(this.typingTimer)
      const target = String(text)
      this.typedUserName = ''
      let i = 0
      // 每100毫秒显示一个字符
      this.typingTimer = setInterval(() => {
        if (i >= target.length) {
          // 动画完成，清除定时器
          clearInterval(this.typingTimer)
          this.typingTimer = null
          return
        }
        // 追加下一个字符
        this.typedUserName += target.charAt(i)
        i++
      }, 100)
    }
  },
  /**
   * 组件销毁前的生命周期钩子
   * 清理打字机定时器，防止内存泄漏
   */
  beforeDestroy() {
    if (this.typingTimer) {
      clearInterval(this.typingTimer)
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/user-information/identity-card.css';
</style>
