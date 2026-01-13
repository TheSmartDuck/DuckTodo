<template>
  <canvas ref="canvas" class="stardust-canvas"></canvas>
</template>

<script>
export default {
  name: 'DriftingStardust',
  data() {
    return {
      ctx: null,
      width: 0,
      height: 0,
      particles: [],
      animationId: null,
      // avionics-mustard, oxidized-sage, alarm-burgundy
      colors: ['#BA8530', '#5C7F71', '#802520'],
      scanOffset: 0,
      scanSpeed: 0.2,
      scanGap: 200
    }
  },
  mounted() {
    this.init()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
    }
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    init() {
      const canvas = this.$refs.canvas
      this.ctx = canvas.getContext('2d')
      this.handleResize()
      this.createParticles()
      this.animate()
    },
    handleResize() {
      const canvas = this.$refs.canvas
      if (!canvas) return
      this.width = window.innerWidth
      this.height = window.innerHeight
      canvas.width = this.width
      canvas.height = this.height
      // Re-create particles on resize to maintain density? Or just let them be.
      // Let's just let them be, maybe add if density drops too low, but simple is fine.
    },
    createParticles() {
      // Density: 1 particle per 8000 pixels (Increased density)
      const count = Math.floor((window.innerWidth * window.innerHeight) / 8000)
      this.particles = []
      for (let i = 0; i < count; i++) {
        this.particles.push(this.createParticle())
      }
    },
    createParticle() {
      return {
        x: Math.random() * this.width,
        y: Math.random() * this.height,
        // Initial velocity (slow)
        vx: (Math.random() - 0.5) * 0.3, 
        vy: (Math.random() - 0.5) * 0.3,
        size: Math.random() * 2 + 0.5,
        color: this.colors[Math.floor(Math.random() * this.colors.length)],
        // Opacity 10% - 30%
        alpha: Math.random() * 0.2 + 0.1,
        // Trail properties
        trail: [],
        isTrailing: Math.random() < 0.05, // 5% chance start with trail
        trailMax: Math.floor(Math.random() * 40 + 20) // Longer trail
      }
    },
    animate() {
      if (!this.ctx) return
      this.ctx.clearRect(0, 0, this.width, this.height)

      // Draw Scan Lines
      this.scanOffset += this.scanSpeed
      if (this.scanOffset >= this.scanGap) {
        this.scanOffset = 0
      }

      this.ctx.beginPath()
      this.ctx.strokeStyle = 'rgba(92, 127, 113, 0.15)' // oxidized-sage low opacity
      this.ctx.lineWidth = 1

      // Vertical lines (moving right)
      // Start loop from offset - gap to cover the entering line from left
      for (let x = this.scanOffset - this.scanGap; x < this.width; x += this.scanGap) {
        this.ctx.moveTo(x, 0)
        this.ctx.lineTo(x, this.height)
      }

      // Horizontal lines (moving down)
      // Start loop from offset - gap to cover the entering line from top
      for (let y = this.scanOffset - this.scanGap; y < this.height; y += this.scanGap) {
        this.ctx.moveTo(0, y)
        this.ctx.lineTo(this.width, y)
      }
      this.ctx.stroke()
      
      this.particles.forEach(p => {
        // Apply slight random acceleration (Brownian motion-ish)
        p.vx += (Math.random() - 0.5) * 0.02
        p.vy += (Math.random() - 0.5) * 0.02
        
        // Speed cap
        const maxSpeed = 0.8
        const speed = Math.sqrt(p.vx * p.vx + p.vy * p.vy)
        if (speed > maxSpeed) {
            p.vx = (p.vx / speed) * maxSpeed
            p.vy = (p.vy / speed) * maxSpeed
        }

        // Update position
        p.x += p.vx
        p.y += p.vy
        
        // Wrap around screen
        if (p.x < -50) p.x = this.width + 50
        if (p.x > this.width + 50) p.x = -50
        if (p.y < -50) p.y = this.height + 50
        if (p.y > this.height + 50) p.y = -50
        
        // Randomly toggle trailing
        // 0.2% chance per frame to toggle trail state
        if (Math.random() < 0.002) {
            p.isTrailing = !p.isTrailing
            if (!p.isTrailing) p.trail = []
        }

        // Draw Particle
        this.ctx.beginPath()
        this.ctx.fillStyle = p.color
        this.ctx.globalAlpha = p.alpha
        this.ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
        this.ctx.fill()
        
        // Draw Trail (Long Exposure Effect)
        if (p.isTrailing) {
            p.trail.push({x: p.x, y: p.y})
            if (p.trail.length > p.trailMax) {
                p.trail.shift()
            }
            
            if (p.trail.length > 1) {
                this.ctx.beginPath()
                this.ctx.strokeStyle = p.color
                this.ctx.globalAlpha = p.alpha * 0.6 // Fainter trail
                this.ctx.lineWidth = p.size
                this.ctx.moveTo(p.trail[0].x, p.trail[0].y)
                // Smooth curve or straight lines? Straight is fine for "long exposure" look
                for (let i = 1; i < p.trail.length; i++) {
                    this.ctx.lineTo(p.trail[i].x, p.trail[i].y)
                }
                this.ctx.stroke()
            }
        }
      })
      
      this.animationId = requestAnimationFrame(this.animate)
    }
  }
}
</script>

<style scoped>
.stardust-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0; 
}
</style>
