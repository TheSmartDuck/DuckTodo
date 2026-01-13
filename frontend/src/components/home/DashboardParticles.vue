<template>
  <div class="particles-container" ref="container">
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script>
const COLORS = ['#BA8530', '#5C7F71', '#802520'] // avionics-mustard, oxidized-sage, alarm-burgundy

class Particle {
  constructor(w, h) {
    this.w = w
    this.h = h
    this.reset(true)
  }

  reset(initial = false) {
    this.x = Math.random() * this.w
    this.y = Math.random() * this.h
    this.vx = (Math.random() - 0.5) * 0.5
    this.vy = (Math.random() - 0.5) * 0.5
    this.color = COLORS[Math.floor(Math.random() * COLORS.length)]
    this.radius = Math.random() * 2 + 1
    
    // Lifecycle
    this.life = 0
    this.maxLife = Math.random() * 200 + 100 // frames
    this.state = 'in' // in, stable, out
    this.opacity = 0
    
    if (initial) {
        this.life = Math.random() * this.maxLife
        this.state = 'stable'
        this.opacity = 1
    }
  }

  update() {
    this.x += this.vx
    this.y += this.vy

    // Bounce off walls or wrap around? Let's bounce gently or just wrap
    if (this.x < 0 || this.x > this.w) this.vx *= -1
    if (this.y < 0 || this.y > this.h) this.vy *= -1

    this.life++
    
    // State management for opacity
    // Fade in
    if (this.state === 'in') {
        this.opacity += 0.02
        if (this.opacity >= 1) {
            this.opacity = 1
            this.state = 'stable'
        }
    }
    // Fade out
    else if (this.life > this.maxLife) {
        this.state = 'out'
        this.opacity -= 0.02
        if (this.opacity <= 0) {
            this.reset()
        }
    }
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2)
    ctx.fillStyle = this.hexToRgba(this.color, this.opacity)
    ctx.fill()
  }

  hexToRgba(hex, alpha) {
    const r = parseInt(hex.slice(1, 3), 16)
    const g = parseInt(hex.slice(3, 5), 16)
    const b = parseInt(hex.slice(5, 7), 16)
    return `rgba(${r}, ${g}, ${b}, ${alpha})`
  }
}

export default {
  name: 'DashboardParticles',
  data() {
    return {
      particles: [],
      animationId: null,
      ctx: null,
      width: 0,
      height: 0,
      particleCount: 60,
      connectionDistance: 100
    }
  },
  mounted() {
    this.initCanvas()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    cancelAnimationFrame(this.animationId)
  },
  methods: {
    initCanvas() {
      const container = this.$refs.container
      const canvas = this.$refs.canvas
      if (!container || !canvas) return

      this.ctx = canvas.getContext('2d')
      
      // Use offsetWidth/Height for accurate pixel size
      this.width = container.offsetWidth
      this.height = container.offsetHeight
      canvas.width = this.width
      canvas.height = this.height

      this.particles = []
      for (let i = 0; i < this.particleCount; i++) {
        this.particles.push(new Particle(this.width, this.height))
      }

      this.animate()
    },
    handleResize() {
      const container = this.$refs.container
      const canvas = this.$refs.canvas
      if (!container || !canvas) return
      
      this.width = container.clientWidth
      this.height = container.clientHeight
      canvas.width = this.width
      canvas.height = this.height
      
      // Update boundaries for existing particles
      this.particles.forEach(p => {
          p.w = this.width
          p.h = this.height
      })
    },
    animate() {
      this.ctx.clearRect(0, 0, this.width, this.height)
      
      // Update and draw particles
      this.particles.forEach(p => {
        p.update()
        p.draw(this.ctx)
      })

      // Draw connections
      this.drawConnections()

      this.animationId = requestAnimationFrame(this.animate)
    },
    drawConnections() {
      for (let i = 0; i < this.particles.length; i++) {
        for (let j = i + 1; j < this.particles.length; j++) {
          const p1 = this.particles[i]
          const p2 = this.particles[j]
          
          const dx = p1.x - p2.x
          const dy = p1.y - p2.y
          const dist = Math.sqrt(dx * dx + dy * dy)

          if (dist < this.connectionDistance) {
            const opacity = (1 - dist / this.connectionDistance) * Math.min(p1.opacity, p2.opacity) * 0.5
            if (opacity > 0) {
                this.ctx.beginPath()
                this.ctx.strokeStyle = `rgba(100, 100, 100, ${opacity})` // Neutral connection color
                this.ctx.lineWidth = 1
                this.ctx.moveTo(p1.x, p1.y)
                this.ctx.lineTo(p2.x, p2.y)
                this.ctx.stroke()
            }
          }
        }
      }
    }
  }
}
</script>

<style scoped>
.particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none; /* Let clicks pass through */
  z-index: 0; /* Behind content but above background */
  overflow: hidden;
}
canvas {
  display: block;
}
</style>
