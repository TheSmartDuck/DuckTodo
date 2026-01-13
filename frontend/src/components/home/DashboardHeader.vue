<template>
  <!-- 仪表板头部根容器 -->
  <div class="dashboard-header-retro">
    <!-- 左侧区域：包含雷达图和标题信息 -->
    <div class="dashboard-header-left-group">
        <div class="dashboard-header-title-row">
            <!-- 动态雷达图：显示当前时间和雷达扫描效果 -->
            <div class="dashboard-header-radar-chart">
                <!-- 粒子效果和轮廓（由 RadarParticles 组件处理） -->
                <radar-particles />
                
                <!-- 仪表弧线：彩色环形指示器 -->
                <div class="dashboard-header-radar-gauge-arc"></div>
                
                <!-- 旋转扫描线：雷达扫描动画效果 -->
                <div class="dashboard-header-radar-scan-line"></div>
                
                <!-- 静态装饰：内环和刻度标记 -->
                <div class="dashboard-header-radar-static-ring"></div>
                <div class="dashboard-header-radar-tick-marks"></div>

                <!-- 指示器：三角形和加号标记 -->
                <div class="dashboard-header-radar-indicator-tri">▲</div>
                <div class="dashboard-header-radar-indicator-plus">+</div>
                
                <!-- 数字显示：大号时间（时:分）和小号秒数 -->
                <div class="dashboard-header-radar-number-big">{{ timeStr }}</div>
                <div class="dashboard-header-radar-number-small">{{ seconds }}</div>
            </div>

            <!-- 标题组：包含打字效果文本和装饰线 -->
            <div class="dashboard-header-title-group">
                <!-- 标题栏：打字效果文本容器 -->
                <div class="dashboard-header-title-bar">
                    <div class="dashboard-header-typed-container-inline">
                        <!-- 动态文本：从父组件传入的打字效果文本 -->
                        <span class="dashboard-header-dynamic-text">{{ typedText }}</span>
                        <!-- 光标：闪烁的光标动画 -->
                        <span class="dashboard-header-cursor">_</span>
                    </div>
                </div>
                
                <!-- 装饰线：三条彩色装饰线（酒红、芥末黄、鼠尾草绿） -->
                <div class="dashboard-header-deco-lines">
                    <div class="dashboard-header-deco-line dashboard-header-line-burgundy"></div>
                    <div class="dashboard-header-deco-line dashboard-header-line-mustard"></div>
                    <div class="dashboard-header-deco-line dashboard-header-line-sage"></div>
                </div>
                
                <!-- 信息行：用户标签和日期信息 -->
                <div class="dashboard-header-info-row">
                    <!-- 用户标签：显示登录用户名 -->
                    <div class="dashboard-header-user-tag">
                        <span>LOGGED BY: {{ userName || 'OBSERVER' }}</span>
                    </div>
                    <!-- 日期块：显示月/日 -->
                    <div class="dashboard-header-date-block-small">
                        <span style="color: var(--mission-rust);">{{ month }}/{{ day }}</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 右侧区域：系统状态信息 -->
    <div class="dashboard-header-right-group-static">
        <!-- 静态块：系统状态显示 -->
        <div class="dashboard-header-static-block">
            <div class="dashboard-header-static-label">SYSTEM</div>
            <div class="dashboard-header-static-val">ONLINE</div>
        </div>
        <!-- 装饰框：右侧装饰性条纹框 -->
        <div class="dashboard-header-static-deco-box"></div>
    </div>
  </div>
</template>

<script>
import RadarParticles from './RadarParticles.vue'

/**
 * 仪表板头部组件
 * 
 * 功能：
 * 1. 显示动态雷达图，展示当前时间（时:分:秒）
 * 2. 显示打字效果文本（从父组件传入）
 * 3. 显示登录用户名和当前日期
 * 4. 显示系统状态（ONLINE）
 * 5. 提供复古风格的视觉装饰效果
 */
export default {
  name: 'DashboardHeader',
  components: { 
    RadarParticles 
  },
  props: {
    // 用户名称（大写显示）
    userName: { 
      type: String, 
      default: '' 
    },
    // 打字效果显示的文本
    typedText: { 
      type: String, 
      default: '' 
    }
  },
  data () {
    return {
      // 当前月份（两位数格式，如 "01"）
      month: '01',
      // 当前日期（两位数格式，如 "01"）
      day: '01',
      // 时间字符串（时:分格式，如 "12:34"）
      timeStr: '00:00',
      // 秒数（两位数格式，如 "00"）
      seconds: '00',
      // 定时器引用（用于更新时间）
      timer: null
    }
  },
  /**
   * 组件挂载时启动时间更新定时器
   */
  mounted () {
    // 立即更新一次时间
    this.updateTime()
    // 每秒更新一次时间
    this.timer = setInterval(this.updateTime, 1000)
  },
  /**
   * 组件销毁前清理定时器
   */
  beforeDestroy () {
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods: {
    /**
     * 更新时间显示
     * 从当前日期对象中提取月、日、时、分、秒，并格式化为两位数
     */
    updateTime () {
      const now = new Date()
      
      // 更新月份（1-12，补零到两位数）
      this.month = String(now.getMonth() + 1).padStart(2, '0')
      
      // 更新日期（1-31，补零到两位数）
      this.day = String(now.getDate()).padStart(2, '0')
      
      // 更新时间字符串（时:分格式，补零到两位数）
      this.timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
      
      // 更新秒数（补零到两位数）
      this.seconds = String(now.getSeconds()).padStart(2, '0')
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/home/dashboard-header.css';
</style>
