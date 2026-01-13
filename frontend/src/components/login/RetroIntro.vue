<template>
  <div class="retro-intro">
    <canvas ref="canvas" class="particle-canvas"></canvas>
    <div class="intro-text" :class="{ 'fade-in': showText, 'exit': isExiting }">
      <div class="line en">{{ displayTextEn }}<span class="cursor">_</span></div>
      <div class="line cn">{{ displayTextCn }}</div>
      <div class="loading-bar">
        <div class="progress" :style="{ width: progress + '%' }"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RetroIntro',
  data () {
    return {
      showText: false,
      progress: 0,
      animationId: null,
      fullTextEn: 'ACCESS PERMISSION REQUIRED',
      fullTextCn: '需 要 访 问 许 可',
      displayTextEn: '',
      displayTextCn: '',
      isExiting: false,
      explosionProgress: 0
    }
  },
  mounted () {
    this.initCanvas()
    setTimeout(() => {
      this.showText = true
      this.startProgress()
    }, 500)
  },
  beforeDestroy () {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
    }
    window.removeEventListener('resize', this.onResize)
  },
  methods: {
    initCanvas () {
      const canvas = this.$refs.canvas
      const ctx = canvas.getContext('2d')
      let width = window.innerWidth
      let height = window.innerHeight
      canvas.width = width
      canvas.height = height

      // 粒子参数
      const particleCount = 2000 // 增加粒子数量
      const maxRadius = Math.min(width, height) * 0.35
      const particles = []

      for (let i = 0; i < particleCount; i++) {
        // 球体表面分布 - 使用单位球坐标
        const theta = Math.random() * 2 * Math.PI
        const phi = Math.acos((Math.random() * 2) - 1)
        
        particles.push({
          x: Math.sin(phi) * Math.cos(theta),
          y: Math.sin(phi) * Math.sin(theta),
          z: Math.cos(phi),
          size: Math.random() * 1.5 + 0.5
        })
      }

      let angleX = 0
      let angleY = 0

      const draw = () => {
        ctx.fillStyle = '#f2f0e6' // 背景色清空
        ctx.fillRect(0, 0, width, height)

        // 绘制网格背景
        this.drawGrid(ctx, width, height)

        ctx.save()
        ctx.translate(width / 2, height / 2)
        
        // 旋转
        angleY += 0.003
        angleX += 0.001

        // 处理爆炸效果
        if (this.isExiting) {
          // 指数级加速爆炸
          this.explosionProgress += 0.015
        }

        // 根据进度计算当前球体半径和显示的粒子数量
        // 使用反指数曲线（EaseOutExpo）: 1 - 2^(-10t)
        // 进度从0到100
        const progressRatio = Math.max(0, Math.min(1, this.progress / 100))
        
        // EaseOutExpo: 增长极快，后期平缓
        const easeVal = progressRatio === 1 ? 1 : 1 - Math.pow(2, -10 * progressRatio)
        
        // 如果正在爆炸，半径急剧增大
        let currentRadius = maxRadius * (0.2 + 0.8 * easeVal)
        if (this.isExiting) {
          // 爆炸半径系数，从1快速变大
          const explosionScale = 1 + Math.pow(this.explosionProgress * 5, 2)
          currentRadius *= explosionScale
        }
        
        // 粒子数量也跟随曲线增加，但保留最小数量
        const visibleCount = Math.floor(particleCount * Math.max(0.05, easeVal))

        for (let i = 0; i < visibleCount; i++) {
          const p = particles[i]
          
          // 绕Y轴旋转
          let x1 = p.x * Math.cos(angleY) - p.z * Math.sin(angleY)
          let z1 = p.z * Math.cos(angleY) + p.x * Math.sin(angleY)
          
          // 绕X轴旋转
          let y2 = p.y * Math.cos(angleX) - z1 * Math.sin(angleX)
          let z2 = z1 * Math.cos(angleX) + p.y * Math.sin(angleX)

          // 应用当前半径
          let xFinal = x1 * currentRadius
          let yFinal = y2 * currentRadius
          let zFinal = z2 * currentRadius

          // 投影前检查：如果粒子在摄像机后面（z < -400），则不绘制
          // 防止 scale 为负导致 radius 为负的错误
          if (zFinal <= -399) continue

          // 投影
          const scale = 400 / (400 + zFinal) // 简单的透视投影
          const x2d = xFinal * scale
          const y2d = yFinal * scale

          // 绘制点
          let alpha = (zFinal + currentRadius) / (2 * currentRadius) // 根据深度调整透明度
          
          // 爆炸时透明度逐渐降低
          if (this.isExiting) {
            alpha *= Math.max(0, 1 - this.explosionProgress * 1.5)
          }

          // 确保 alpha 在合理范围
          const safeAlpha = Math.max(0, Math.min(1, 0.1 + alpha * 0.6))
          
          if (safeAlpha > 0.01) {
            ctx.fillStyle = `rgba(0, 0, 0, ${safeAlpha})`
            ctx.beginPath()
            ctx.arc(x2d, y2d, p.size * scale, 0, Math.PI * 2)
            ctx.fill()
          }
        }

        ctx.restore()
        this.animationId = requestAnimationFrame(draw)
      }

      this.onResize = () => {
        width = window.innerWidth
        height = window.innerHeight
        canvas.width = width
        canvas.height = height
      }
      window.addEventListener('resize', this.onResize)

      draw()
    },
    drawGrid(ctx, w, h) {
      ctx.lineWidth = 1
      ctx.strokeStyle = 'rgba(0, 0, 0, 0.03)'
      const step = 20
      
      ctx.beginPath()
      for (let x = 0; x <= w; x += step) {
        ctx.moveTo(x, 0)
        ctx.lineTo(x, h)
      }
      for (let y = 0; y <= h; y += step) {
        ctx.moveTo(0, y)
        ctx.lineTo(w, y)
      }
      ctx.stroke()
    },
    startProgress () {
      const duration = 2200 // 增加时长以展示等待效果
      const start = Date.now()
      
      const tick = () => {
        const now = Date.now()
        const p = Math.min(100, ((now - start) / duration) * 100)
        this.progress = p
        
        // 打字机效果 - 在 75% 进度时完成
        const textThreshold = 75
        const textRatio = Math.min(1, p / textThreshold)
        
        const enLen = Math.ceil(this.fullTextEn.length * textRatio)
        const cnLen = Math.ceil(this.fullTextCn.length * textRatio)
        
        this.displayTextEn = this.fullTextEn.substring(0, enLen)
        this.displayTextCn = this.fullTextCn.substring(0, cnLen)

        if (p < 100) {
          requestAnimationFrame(tick)
        } else {
          // 加载完成，开始退场动画
          this.isExiting = true
          // 等待爆炸动画播放一段时间后再通知父组件
          setTimeout(() => {
            this.$emit('finish')
          }, 300)
        }
      }
      tick()
    }
  }
}
</script>

<style src="@/assets/css/login/retro-intro.css" scoped></style>