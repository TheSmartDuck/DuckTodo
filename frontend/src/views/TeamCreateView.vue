<template>
  <!-- 创建团队页面根容器 -->
  <div class="team-create-page team-create-split-layout">
    <!-- 左侧装饰面板 -->
    <div class="team-create-left-panel">
      <team-create-left-panel 
        :title="pageTitle"
        :subtitle="subtitleText"
        :user-info="userInfo"
        :stats="teamStats"
        :show-back="!!mode"
        @back="resetMode"
      />
    </div>

    <!-- 右侧内容面板 -->
    <div class="team-create-right-panel">
      <!-- 动态背景层 -->
      <div class="team-create-dynamic-bg">
        <!-- 背景柱状图区域 -->
        <div class="team-create-bg-chart-area">
          <div 
            class="team-create-bg-bar" 
            v-for="n in 20" 
            :key="n" 
            :style="{ '--delay': n * -0.3 + 's', '--duration': 2 + (n % 3) + 's' }"
          ></div>
        </div>
        <!-- 背景曲线区域 -->
        <div class="team-create-bg-curve-area">
           <svg viewBox="0 0 1440 320" preserveAspectRatio="none">
              <path fill="rgba(186, 133, 48, 0.05)" d="M0,224L48,213.3C96,203,192,181,288,181.3C384,181,480,203,576,224C672,245,768,267,864,261.3C960,256,1056,224,1152,197.3C1248,171,1344,149,1392,138.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
              <path fill="rgba(178, 101, 59, 0.03)" d="M0,96L48,122.7C96,149,192,203,288,208C384,213,480,171,576,138.7C672,107,768,85,864,96C960,107,1056,149,1152,160C1248,171,1344,149,1392,138.7L1440,128L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
           </svg>
        </div>
        
        <!-- 顶部粒子画布层 -->
        <canvas ref="particleCanvas" class="team-create-top-particle-canvas"></canvas>
      </div>

      <!-- 装饰层（右侧面板） -->
      <div class="team-create-decorative-layer">
        <!-- 右上角装饰括号 -->
        <div class="team-create-corner-bracket team-create-top-right"></div>
        <!-- 右下角装饰括号 -->
        <div class="team-create-corner-bracket team-create-bottom-right"></div>
        <!-- 右上角十字准星 -->
        <div class="team-create-crosshair team-create-top-right"></div>
        
        <!-- 浮动版本文字 -->
        <div class="team-create-floating-text team-create-version">VER: 3.0.1 BETA</div>
      </div>

      <!-- 内容层 -->
      <div class="team-create-content-layer">
        <!-- 主内容区域 -->
        <div class="team-create-main-content-area">
          <!-- 模式选择器过渡动画 -->
          <transition name="team-create-fade" mode="out-in" @after-leave="onChoicesAfterLeave">
            <team-create-mode-selector v-if="showChoices" @select="selectMode" />
          </transition>

          <!-- 表单/列表过渡动画 -->
          <transition name="team-create-rise-fade" mode="out-in" @after-leave="onCardAfterLeave">
            <!-- 创建团队表单 -->
            <team-create-form v-if="mode === 'create'" key="create" @success="resetMode" @back="resetMode" />
            <!-- 加入团队列表 -->
            <team-join-list v-if="mode === 'join'" key="join" @back="resetMode" />
          </transition>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import TeamCreateModeSelector from '@/components/team-create/TeamCreateModeSelector.vue'
import TeamCreateForm from '@/components/team-create/TeamCreateForm.vue'
import TeamJoinList from '@/components/team-create/TeamJoinList.vue'
import TeamCreateLeftPanel from '@/components/team-create/TeamCreateLeftPanel.vue'
import { getMe } from '@/api/user-api'
import { getMyRelatedTeamList, getMyInviteTeamPage } from '@/api/team-api'

/**
 * 创建团队视图组件
 * 
 * 功能：
 * 1. 提供创建新团队或加入现有团队的入口
 * 2. 显示用户信息和团队统计信息
 * 3. 管理页面状态和模式切换（创建/加入）
 * 4. 渲染动态背景和粒子效果
 * 
 * 数据来源：
 * - 通过 user-api 的 getMe API 获取用户信息
 * - 通过 team-api 的 getMyRelatedTeamList 和 getMyInviteTeamPage API 获取团队统计
 * 
 * 组件结构：
 * - 左侧：装饰面板（显示用户信息和统计）
 * - 右侧：内容面板（模式选择器、创建表单、加入列表）
 * - 背景：动态柱状图、曲线、粒子效果
 */
export default {
  name: 'TeamCreateView',
  components: {
    TeamCreateModeSelector,
    TeamCreateForm,
    TeamJoinList,
    TeamCreateLeftPanel
  },
  data () {
    return {
      // 当前模式：'create'（创建）或 'join'（加入），null 表示显示选择器
      mode: null,
      // 是否显示模式选择器
      showChoices: true,
      // 待切换的模式（用于过渡动画）
      pendingMode: null,
      // 用户信息
      userInfo: {
        userName: 'GUEST',    // 用户名
        userId: 'UNKNOWN'     // 用户ID
      },
      // 团队统计信息
      teamStats: {
        joinedCount: 0,      // 已加入的团队数量
        inviteCount: 0        // 待处理的邀请数量
      },
      // Canvas 2D 渲染上下文
      ctx: null,
      // 粒子数组
      particles: [],
      // 动画帧ID（用于取消动画）
      animationFrame: null
    }
  },
  computed: {
    /**
     * 页面标题
     * 根据当前模式返回对应的标题文本
     */
    pageTitle () {
      if (this.mode === 'create') return '新建项目'
      if (this.mode === 'join') return '加入项目'
      return '加入或新建项目'
    },
    /**
     * 副标题文本
     * 根据当前模式返回对应的副标题文本
     */
    subtitleText () {
      if (this.mode === 'create') return 'INIT_NEW_PROJECT'
      if (this.mode === 'join') return 'SYNC_INVITATION'
      return 'INIT_OR_SYNC_PROJECT'
    }
  },
  /**
   * 组件创建时钩子
   * 获取初始数据（用户信息、团队统计）
   */
  created () {
    this.fetchData()
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
   * 清理事件监听器和动画帧
   */
  beforeDestroy () {
    window.removeEventListener('resize', this.handleResize)
    if (this.animationFrame) cancelAnimationFrame(this.animationFrame)
  },
  methods: {
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
      const particleCount = 150  // 粒子数量
      // 使用主题色作为粒子颜色
      const colors = ['#B2653B', '#BA8530', '#5C7F71', '#802520']  // mission-rust, avionics-mustard, oxidized-sage, alarm-burgundy
      
      // 生成粒子
      for (let i = 0; i < particleCount; i++) {
        this.particles.push(this.createParticle(colors))
      }
      
      // 开始动画循环
      this.animateParticles()
    },
    
    /**
     * 调整画布大小
     * 根据窗口大小设置画布尺寸，覆盖右侧面板的上半部分
     */
    resizeCanvas () {
      const canvas = this.$refs.particleCanvas
      if (canvas) {
        // 设置画布为固定视口大小，覆盖右侧面板的上半部分
        const width = window.innerWidth * 0.65  // 约等于右侧面板宽度（65%）
        const height = window.innerHeight * 0.6  // 屏幕高度的 60%
        
        canvas.width = width
        canvas.height = height
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
     * @param {Array<string>} colors - 粒子颜色数组
     * @returns {Object} 粒子对象，包含位置、速度、大小、颜色、透明度、生命周期等属性
     */
    createParticle (colors) {
      const canvas = this.$refs.particleCanvas
      return {
        x: Math.random() * canvas.width,                    // X 坐标（随机）
        y: Math.random() * canvas.height,                   // Y 坐标（随机）
        size: Math.random() * 3 + 1,                        // 粒子大小（1-4）
        speedX: (Math.random() - 0.5) * 0.5,               // X 方向速度（-0.25 到 0.25）
        speedY: (Math.random() - 0.5) * 0.5,               // Y 方向速度（-0.25 到 0.25）
        color: colors[Math.floor(Math.random() * colors.length)],  // 随机选择颜色
        opacity: 0,                                         // 初始透明度为 0（淡入）
        fadeIn: true,                                      // 是否正在淡入
        life: Math.random() * 200 + 100                    // 生命周期（100-300）
      }
    },
    
    /**
     * 动画粒子效果
     * 更新粒子位置、透明度、生命周期，绘制粒子和连接线
     */
    animateParticles () {
      if (!this.ctx) return
      const canvas = this.$refs.particleCanvas
      const ctx = this.ctx
      
      // 清空画布
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      
      // 更新和绘制每个粒子
      this.particles.forEach((p, index) => {
        // 移动粒子
        p.x += p.speedX
        p.y += p.speedY
        p.life--
        
        // 边界处理：当粒子超出画布边界时，从另一侧重新进入
        if (p.x < 0) p.x = canvas.width
        if (p.x > canvas.width) p.x = 0
        if (p.y < 0) p.y = canvas.height
        if (p.y > canvas.height) p.y = 0
        
        // 透明度动画（淡入/淡出）
        if (p.fadeIn) {
          // 淡入阶段：逐渐增加透明度
          p.opacity += 0.01
          if (p.opacity >= 0.6) p.fadeIn = false  // 达到最大透明度后停止淡入
        } else {
          // 淡出阶段：当生命周期接近结束时逐渐降低透明度
          if (p.life < 50) p.opacity -= 0.01
        }
        
        // 如果粒子死亡（生命周期结束或透明度为 0），重新生成
        if (p.life <= 0 || p.opacity <= 0) {
          const colors = ['#B2653B', '#BA8530', '#5C7F71', '#802520']
          this.particles[index] = this.createParticle(colors)
        }
        
        // 绘制粒子
        ctx.globalAlpha = Math.max(0, p.opacity)
        ctx.fillStyle = p.color
        ctx.beginPath()
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
        ctx.fill()
        
        // 绘制连接线：当两个粒子距离小于 150 时，绘制连接线
        for (let j = index + 1; j < this.particles.length; j++) {
          const p2 = this.particles[j]
          const dx = p.x - p2.x
          const dy = p.y - p2.y
          const dist = Math.sqrt(dx * dx + dy * dy)
          
          if (dist < 150) {
            ctx.beginPath()
            ctx.strokeStyle = p.color
            // 连接线透明度根据距离和粒子透明度计算
            ctx.globalAlpha = Math.max(0, (1 - dist / 150) * 0.5 * Math.min(p.opacity, p2.opacity))
            ctx.lineWidth = 1.5
            ctx.moveTo(p.x, p.y)
            ctx.lineTo(p2.x, p2.y)
            ctx.stroke()
          }
        }
      })
      
      // 请求下一帧动画
      this.animationFrame = requestAnimationFrame(this.animateParticles)
    },
    
    /**
     * ========== 数据获取相关方法 ==========
     */
    
    /**
     * 获取初始数据
     * 异步获取用户信息、已加入团队数量、待处理邀请数量
     */
    async fetchData () {
      try {
        // 获取用户信息
        const userRes = await getMe()
        if (userRes) {
          this.userInfo = userRes
        }

        // 获取已加入的团队列表并统计数量
        const teams = await getMyRelatedTeamList()
        this.teamStats.joinedCount = Array.isArray(teams) ? teams.length : 0

        // 获取待处理的邀请数量
        const invitesPage = await getMyInviteTeamPage({ page: 1, size: 1, memberStatus: '2' })
        this.teamStats.inviteCount = invitesPage?.total || 0
      } catch (e) {
        console.error('Failed to fetch initial data', e)
      }
    },
    
    /**
     * ========== 模式切换相关方法 ==========
     */
    
    /**
     * 选择模式
     * 当用户选择创建或加入模式时调用，设置待切换模式并隐藏选择器
     * @param {string} m - 模式：'create' 或 'join'
     */
    selectMode (m) {
      this.pendingMode = m
      this.showChoices = false
    },
    
    /**
     * 重置模式
     * 返回到模式选择界面，并刷新统计数据
     */
    resetMode () {
      this.mode = null
      this.fetchData()  // 返回时刷新统计数据
    },
    
    /**
     * 卡片离开后回调
     * 当表单或列表组件离开后，如果模式已重置，则重新显示选择器
     */
    onCardAfterLeave () {
      // 当卡片（表单/列表）离开后，如果模式为 null，则重新显示选择器
      if (!this.mode) {
        this.showChoices = true
      }
    },
    
    /**
     * 选择器离开后回调
     * 当模式选择器离开后，切换到待切换的模式
     */
    onChoicesAfterLeave () {
      // 当选择器离开后，切换到待切换的模式
      if (this.pendingMode) {
        this.mode = this.pendingMode
        this.pendingMode = null
      }
    }
  }
}
</script>

<style scoped src="@/assets/css/team-create/team-create-view.css"></style>
