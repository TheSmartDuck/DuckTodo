<template>
  <div class="radar-particles-container" ref="container">
    <!-- Contour Lines SVG -->
    <div class="contour-layer">
      <svg width="100%" height="100%" viewBox="0 0 200 200" preserveAspectRatio="xMidYMid slice">
        <defs>
          <filter id="contour-disp">
            <feTurbulence type="fractalNoise" baseFrequency="0.01" numOctaves="3" result="noise" />
            <feDisplacementMap in="SourceGraphic" in2="noise" scale="40" xChannelSelector="R" yChannelSelector="G" />
          </filter>
        </defs>
        <g filter="url(#contour-disp)" style="transform-origin: 100px 100px; animation: contour-spin 60s linear infinite;">
          <circle v-for="i in 10" :key="i" cx="100" cy="100" :r="i * 12" 
                  fill="none" stroke="rgba(92, 127, 113, 0.4)" stroke-width="0.8" />
        </g>
      </svg>
    </div>

    <!-- Particles Canvas -->
    <canvas ref="canvas"></canvas>
  </div>
</template>

<script>
export default {
  name: 'RadarParticles',
  data() {
    return {
      ctx: null,
      width: 0,
      height: 0,
      particles: [],
      animationId: null,
      particleCount: 20,
      connectionDistance: 60
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
      this.width = container.clientWidth
      this.height = container.clientHeight
      canvas.width = this.width
      canvas.height = this.height

      this.createParticles()
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
    },
    createParticles() {
      this.particles = []
      for (let i = 0; i < this.particleCount; i++) {
        this.particles.push({
          x: Math.random() * this.width,
          y: Math.random() * this.height,
          vx: (Math.random() - 0.5) * 0.5,
          vy: (Math.random() - 0.5) * 0.5,
          radius: Math.random() * 1.5 + 0.5,
          alpha: Math.random() * 0.5 + 0.5
        })
      }
    },
    animate() {
      // Trail effect: fade out previous frame instead of clearing
      this.ctx.globalCompositeOperation = 'destination-out'
      this.ctx.fillStyle = 'rgba(0, 0, 0, 0.1)' // Adjust alpha for trail length (0.1 = long, 0.3 = short)
      this.ctx.fillRect(0, 0, this.width, this.height)
      
      // Reset composite operation to default
      this.ctx.globalCompositeOperation = 'source-over'
      
      // Update and draw particles
      this.particles.forEach(p => {
        p.x += p.vx
        p.y += p.vy

        // Bounce
        if (p.x < 0 || p.x > this.width) p.vx *= -1
        if (p.y < 0 || p.y > this.height) p.vy *= -1

        // Draw Dot
        this.ctx.beginPath()
        this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
        this.ctx.fillStyle = `rgba(255, 255, 255, ${p.alpha})`
        this.ctx.fill()
      })

      // Draw connections - DISABLED as per user request (trail effect only)
      // this.drawConnections()

      this.animationId = requestAnimationFrame(this.animate)
    },
    // drawConnections() { ... } removed to clean up code
  }
}
</script>

<style scoped>
.radar-particles-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1; /* Lowest level inside radar chart (but above bg color) */
}

.contour-layer {
  position: absolute;
  top: -20%;
  left: -20%;
  width: 140%;
  height: 140%;
  opacity: 0.8;
  z-index: 1;
}

canvas {
  display: block;
  position: relative;
  z-index: 2; /* Particles above contour */
}

@keyframes contour-spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style>
