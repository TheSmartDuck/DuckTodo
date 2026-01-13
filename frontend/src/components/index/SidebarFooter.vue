<template>
  <!-- 侧边栏底部组件：包含 CRT 效果开关、版本信息和 GitHub 链接 -->
  <div class="sidebar-footer">
    <div class="sidebar-footer-box">
      <!-- CRT 效果开关行 -->
      <div class="sidebar-footer-row">
        <span class="sidebar-footer-label">CRT EFFECT</span>
        <label class="sidebar-footer-crt-switch">
          <input type="checkbox" v-model="isCrtActive" @change="toggleCrt">
          <span class="sidebar-footer-slider"></span>
        </label>
      </div>
      <!-- 版本信息和 GitHub 链接行 -->
      <div class="sidebar-footer-row">
        <span class="sidebar-footer-version">v0.1.0-alpha</span>
        <a href="https://github.com/DuckTodo" target="_blank" class="sidebar-footer-github-link" title="GitHub">
          <svg class="sidebar-footer-github-icon" viewBox="0 0 24 24">
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
        </a>
      </div>
    </div>
  </div>
</template>

<script>
// 导入 CRT 效果样式
import '@/assets/css/crt-effect.css'

export default {
  name: 'SidebarFooter',
  data() {
    return {
      // CRT 效果开关状态
      isCrtActive: false
    }
  },
  mounted() {
    // 从本地存储读取 CRT 效果状态
    const saved = localStorage.getItem('ducktodo_crt_enabled')
    this.isCrtActive = saved === 'true'
    this.applyCrtEffect(this.isCrtActive)
  },
  methods: {
    /**
     * 切换 CRT 效果
     */
    toggleCrt() {
      this.applyCrtEffect(this.isCrtActive)
      // 保存状态到本地存储
      localStorage.setItem('ducktodo_crt_enabled', this.isCrtActive)
    },
    /**
     * 应用或移除 CRT 效果
     * @param {boolean} active - 是否激活 CRT 效果
     */
    applyCrtEffect(active) {
      if (active) {
        // 激活 CRT 效果：创建覆盖层并添加样式类
        if (!document.getElementById('crt-overlay-container')) {
          const overlay = document.createElement('div')
          overlay.id = 'crt-overlay-container'
          overlay.className = 'crt-overlay'
          overlay.innerHTML = `
            <div class="crt-scanlines"></div>
            <div class="crt-grain"></div>
            <div class="crt-vignette"></div>
          `
          document.body.appendChild(overlay)
          document.body.classList.add('crt-aberration')
        } else {
          document.getElementById('crt-overlay-container').style.display = 'block'
          document.body.classList.add('crt-aberration')
        }
      } else {
        // 移除 CRT 效果：隐藏覆盖层并移除样式类
        const overlay = document.getElementById('crt-overlay-container')
        if (overlay) {
          overlay.style.display = 'none'
        }
        document.body.classList.remove('crt-aberration')
      }
    }
  }
}
</script>

<style src="@/assets/css/index/sidebar-footer.css" scoped></style>
