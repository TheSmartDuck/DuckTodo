<template>
  <!-- 创建团队左侧面板根容器 -->
  <div class="team-create-left-panel-container">
    <!-- 面板内容区域 -->
    <div class="team-create-left-panel-content" style="display: block !important;">
      <!-- 品牌标识 -->
      <div class="team-create-left-panel-brand-large">DUCK // TODO</div>
      
      <!-- 装饰性标题区域 -->
      <div class="team-create-left-panel-decor-title" style="height: 80px;">
        <!-- 主标题（打字机效果） -->
        <h1 class="team-create-left-panel-typewriter-text">
          {{ displayedTitle }}<span class="team-create-left-panel-cursor">_</span>
        </h1>
        <!-- 副标题（打字机效果） -->
        <h2 class="team-create-left-panel-typewriter-text">{{ displayedSubtitle }}</h2>
      </div>

      <!-- 动画柱状图容器 -->
      <div class="team-create-left-panel-chart-container" style="">
        <div class="team-create-left-panel-chart-label">SYS.LOAD // 算力负载监控</div>
        <div class="team-create-left-panel-bar-chart">
          <div 
            class="team-create-left-panel-bar" 
            v-for="n in 12" 
            :key="n" 
            :style="{ animationDelay: `-${n * 0.2}s` }"
          ></div>
        </div>
      </div>

      <!-- 动画正弦波容器（SVG） -->
      <div class="team-create-left-panel-wave-container">
        <div class="team-create-left-panel-chart-label">NET.SYNC // 神经链路同步</div>
        <svg class="team-create-left-panel-sine-wave" viewBox="0 0 300 60" preserveAspectRatio="none">
          <!-- 第一层波形路径 -->
          <path d="M0 30 Q 75 60, 150 30 T 300 30" class="team-create-left-panel-wave-path team-create-left-panel-layer1"></path>
          <!-- 第二层波形路径 -->
          <path d="M0 30 Q 75 0, 150 30 T 300 30" class="team-create-left-panel-wave-path team-create-left-panel-layer2"></path>
        </svg>
      </div>

      <!-- 系统状态信息 -->
      <div class="team-create-left-panel-system-status">
        <!-- 操作员信息 -->
        <div class="team-create-left-panel-status-row">
          <span>OPERATOR // 操作员</span>
          <span class="team-create-left-panel-ok">{{ userInfo.userName || 'GUEST' }}</span>
        </div>
        <!-- 已加入项目数量 -->
        <div class="team-create-left-panel-status-row">
          <span>JOINED // 已加入项目</span>
          <span class="team-create-left-panel-ok">{{ stats.joinedCount }}</span>
        </div>
        <!-- 待处理邀请数量 -->
        <div class="team-create-left-panel-status-row">
          <span>PENDING // 待处理邀请</span>
          <span class="team-create-left-panel-warning">{{ stats.inviteCount }}</span>
        </div>
      </div>
      
      <!-- 导航操作按钮 -->
      <div class="team-create-left-panel-nav-actions" v-if="showBack">
        <button class="team-create-left-panel-retro-back-btn" @click="$emit('back')">
          <i class="el-icon-back"></i> RETURN // 返回上级
        </button>
      </div>

      <!-- 面板底部 -->
      <div class="team-create-left-panel-footer">
        <div class="team-create-left-panel-code-block">SESSION: {{ sessionID }} // 会话标识</div>
      </div>
    </div>

    <!-- 粒子画布 -->
    <canvas ref="particleCanvas" class="team-create-left-panel-particle-canvas"></canvas>
    
    <!-- 背景装饰 -->
    <div class="team-create-left-panel-bg-decor"></div>
    <!-- 扫描线效果 -->
    <div class="team-create-left-panel-scanline"></div>
  </div>
</template>

<script>
/**
 * 创建团队左侧面板组件
 * 
 * 功能：
 * 1. 显示品牌标识和标题（打字机效果）
 * 2. 显示动画柱状图和正弦波图表
 * 3. 显示系统状态信息（操作员、已加入项目、待处理邀请）
 * 4. 提供返回按钮（可选）
 * 5. 渲染粒子背景效果
 * 
 * Props：
 * - title: 页面标题（字符串）
 * - subtitle: 页面副标题（字符串）
 * - userInfo: 用户信息对象（包含 userName、userId 等）
 * - stats: 团队统计信息对象（包含 joinedCount、inviteCount）
 * - showBack: 是否显示返回按钮（布尔值）
 * 
 * Events：
 * - back: 点击返回按钮时触发
 */
export default {
  name: 'TeamCreateLeftPanel',
  props: {
    // 页面标题
    title: { type: String, default: '' },
    // 页面副标题
    subtitle: { type: String, default: '' },
    // 用户信息对象
    userInfo: { type: Object, default: () => ({}) },
    // 团队统计信息对象
    stats: { type: Object, default: () => ({ joinedCount: 0, inviteCount: 0 }) },
    // 是否显示返回按钮
    showBack: { type: Boolean, default: false }
  },
  data () {
    return {
      // 会话ID（随机生成）
      sessionID: 'INIT-' + Math.floor(Math.random() * 10000).toString().padStart(4, '0'),
      // 当前显示的主标题（打字机效果）
      displayedTitle: '',
      // 当前显示的副标题（打字机效果）
      displayedSubtitle: '',
      // 主标题打字机效果的定时器ID
      titleTimeout: null,
      // 副标题打字机效果的定时器ID
      subtitleTimeout: null,
      // Canvas 2D 渲染上下文
      ctx: null,
      // 粒子数组
      particles: [],
      // 动画帧ID（用于取消动画）
      animationFrame: null
    }
  },
  watch: {
    /**
     * 监听标题变化
     * 当标题改变时，使用打字机效果重新显示
     */
    title: {
      immediate: true,
      handler (newVal) {
        this.typeText(newVal || '添加或创建项目', 'displayedTitle')
      }
    },
    /**
     * 监听副标题变化
     * 当副标题改变时，使用打字机效果重新显示（延迟200ms）
     */
    subtitle: {
      immediate: true,
      handler (newVal) {
        this.typeText(newVal || 'INIT_OR_SYNC_PROJECT', 'displayedSubtitle', 200)
      }
    }
  },
  /**
   * 组件挂载后钩子
   * 初始化粒子效果并监听窗口大小变化
   */
  mounted () {
    this.initParticles()
    window.addEventListener('resize', this.handleResize)
  },
  /**
   * 组件销毁前钩子
   * 清理事件监听器、定时器和动画帧
   */
  beforeDestroy () {
    window.removeEventListener('resize', this.handleResize)
    // 清理定时器
    if (this.titleTimeout) clearTimeout(this.titleTimeout)
    if (this.subtitleTimeout) clearTimeout(this.subtitleTimeout)
    // 取消动画帧
    if (this.animationFrame) cancelAnimationFrame(this.animationFrame)
  },
  methods: {
    /**
     * ========== 打字机效果相关方法 ==========
     */
    
    /**
     * 打字机效果
     * 逐字符显示文本，模拟打字机效果
     * @param {string} text - 要显示的文本
     * @param {string} target - 目标数据属性名（'displayedTitle' 或 'displayedSubtitle'）
     * @param {number} delay - 延迟时间（毫秒），默认为 0
     */
    typeText (text, target, delay = 0) {
      // 清除之前的定时器
      if (this[`${target}Timeout`]) clearTimeout(this[`${target}Timeout`])
      
      // 重置显示文本
      this[target] = ''
      let i = 0
      
      /**
       * 逐字符添加函数
       * 每次添加一个字符，并设置随机延迟以模拟真实的打字效果
       */
      const typeChar = () => {
        if (i < text.length) {
          // 添加当前字符
          this[target] += text.charAt(i)
          i++
          // 设置随机延迟（50-100ms）以模拟真实的打字速度
          this[`${target}Timeout`] = setTimeout(typeChar, 50 + Math.random() * 50)
        } else {
          // 打字完成，清除定时器ID
          this[`${target}Timeout`] = null
        }
      }
      
      // 如果设置了延迟，则延迟执行；否则立即执行
      if (delay > 0) {
        this[`${target}Timeout`] = setTimeout(typeChar, delay)
      } else {
        typeChar()
      }
    },
    
    /**
     * ========== 粒子效果相关方法 ==========
     */
    
    /**
     * 初始化粒子效果
     * 创建 Canvas 上下文，调整画布大小，生成粒子数组并开始动画
     */
    initParticles () {
      const canvas = this.$refs.particleCanvas
      if (!canvas) return
      
      // 获取 2D 渲染上下文
      this.ctx = canvas.getContext('2d')
      // 调整画布大小
      this.resizeCanvas()
      
      // 初始化粒子数组
      this.particles = []
      const particleCount = 40  // 粒子数量
      
      // 生成粒子
      for (let i = 0; i < particleCount; i++) {
        this.particles.push(this.createParticle())
      }
      
      // 开始动画循环
      this.animateParticles()
    },
    
    /**
     * 调整画布大小
     * 根据父容器大小设置画布尺寸
     */
    resizeCanvas () {
      const canvas = this.$refs.particleCanvas
      if (canvas) {
        canvas.width = canvas.parentElement.offsetWidth
        canvas.height = canvas.parentElement.offsetHeight
      }
    },
    
    /**
     * 处理窗口大小变化
     * 当窗口大小改变时重新调整画布大小
     */
    handleResize () {
      this.resizeCanvas()
    },
    
    /**
     * 创建单个粒子对象
     * @returns {Object} 粒子对象，包含位置、速度、大小、透明度等属性
     */
    createParticle () {
      const canvas = this.$refs.particleCanvas
      return {
        x: Math.random() * canvas.width,                    // X 坐标（随机）
        y: Math.random() * canvas.height,                   // Y 坐标（随机）
        size: Math.random() * 2 + 0.5,                      // 粒子大小（0.5-2.5）
        speedX: (Math.random() - 0.5) * 0.5,               // X 方向速度（-0.25 到 0.25）
        speedY: (Math.random() - 0.5) * 0.5,               // Y 方向速度（-0.25 到 0.25）
        opacity: Math.random() * 0.5 + 0.1                 // 透明度（0.1-0.6）
      }
    },
    
    /**
     * 动画粒子效果
     * 更新粒子位置，绘制粒子
     */
    animateParticles () {
      if (!this.ctx) return
      const canvas = this.$refs.particleCanvas
      const ctx = this.ctx
      
      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      
      // 使用主题月球奶油色（带透明度）
      ctx.fillStyle = 'rgba(245, 237, 220, 0.6)'  // Lunar Cream with opacity
      
      // 更新和绘制每个粒子
      this.particles.forEach(p => {
        // 移动粒子
        p.x += p.speedX
        p.y += p.speedY
        
        // 边界处理：当粒子超出画布边界时，从另一侧重新进入
        if (p.x < 0) p.x = canvas.width
        if (p.x > canvas.width) p.x = 0
        if (p.y < 0) p.y = canvas.height
        if (p.y > canvas.height) p.y = 0
        
        // 绘制粒子
        ctx.globalAlpha = p.opacity
        ctx.beginPath()
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
        ctx.fill()
      })
      
      // 请求下一帧动画
      this.animationFrame = requestAnimationFrame(this.animateParticles)
    }
  }
}
</script>

<style>
@import '@/assets/css/team-create/team-create-left-panel.css';
</style>