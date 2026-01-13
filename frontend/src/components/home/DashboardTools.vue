<template>
  <div class="mission-tools">
     <div class="tools-header">
        <span class="header-text">QUICK TOOLS / 快捷工具</span>
        <div class="header-status">
           <span class="status-dot"></span>
           <span class="status-text">READY</span>
        </div>
     </div>
     
     <div class="tools-grid">
        <!-- Quick Create -->
        <div class="tool-card" @click="handleToolClick('create')">
           <div class="tool-icon"><i class="el-icon-plus"></i></div>
           <div class="tool-info">
              <div class="tool-name">NEW MISSION</div>
              <div class="tool-desc">Create new task</div>
           </div>
           <div class="tool-arrow">-></div>
        </div>

        <!-- Team Manage -->
        <div class="tool-card" @click="handleToolClick('team')">
           <div class="tool-icon"><i class="el-icon-user"></i></div>
           <div class="tool-info">
              <div class="tool-name">TEAM UNIT</div>
              <div class="tool-desc">Manage members</div>
           </div>
           <div class="tool-arrow">-></div>
        </div>

        <!-- Statistics -->
        <div class="tool-card" @click="handleToolClick('stats')">
           <div class="tool-icon"><i class="el-icon-data-line"></i></div>
           <div class="tool-info">
              <div class="tool-name">DATA LOGS</div>
              <div class="tool-desc">View statistics</div>
           </div>
           <div class="tool-arrow">-></div>
        </div>

        <!-- System Settings -->
        <div class="tool-card" @click="handleToolClick('settings')">
           <div class="tool-icon"><i class="el-icon-setting"></i></div>
           <div class="tool-info">
              <div class="tool-name">SYSTEM CFG</div>
              <div class="tool-desc">Global settings</div>
           </div>
           <div class="tool-arrow">-></div>
        </div>
     </div>

     <div class="tools-footer">
        <div class="footer-line">
           <span class="label">SYSTEM VERSION:</span>
           <span class="value">v2.4.0-ALPHA</span>
        </div>
        <div class="footer-line">
           <span class="label">MEMORY USAGE:</span>
           <div class="mem-bar">
              <div class="mem-fill" style="width: 42%"></div>
           </div>
        </div>
     </div>
  </div>
</template>

<script>
export default {
  name: 'DashboardTools',
  methods: {
    handleToolClick (action) {
      this.$message({
        message: `EXECUTING PROTOCOL: ${action.toUpperCase()}`,
        type: 'success',
        customClass: 'retro-message'
      })
      
      // Navigate or perform action
      if (action === 'create') {
         // emit or route
      }
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/home/dashboard-tools.css';
</style>
