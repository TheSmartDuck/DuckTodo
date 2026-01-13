<template>
  <!-- 团队设置面板根容器 -->
  <div class="team-settings-panel">
    <!-- 主卡片容器 -->
    <div class="team-settings-panel-card">
      <!-- 面板头部：标题 -->
      <div class="team-settings-panel-header">
        <span class="team-settings-panel-title">TEAM SETTINGS / 团队设置</span>
      </div>
      
      <!-- 设置内容区域 -->
      <div class="team-settings-panel-content">
        <!-- 危险操作区域 -->
        <div class="team-settings-panel-danger-section">
          <div class="team-settings-panel-section-header">
            <span class="team-settings-panel-section-title">DANGER ZONE / 危险操作</span>
            <span class="team-settings-panel-section-subtitle">不可逆操作，请谨慎使用</span>
          </div>
          
          <div class="team-settings-panel-danger-content">
            <div class="team-settings-panel-danger-item">
              <div class="team-settings-panel-danger-info">
                <span class="team-settings-panel-danger-label">DELETE TEAM / 删除团队</span>
                <span class="team-settings-panel-danger-desc">
                  删除团队将永久移除所有相关数据，包括任务、成员关系等。此操作不可恢复。
                </span>
              </div>
              <button 
                class="team-settings-panel-btn-danger-split" 
                @click="handleDeleteTeam"
              >
                <span class="team-settings-panel-btn-danger-split-left">DELETE</span>
                <span class="team-settings-panel-btn-danger-split-sep">|</span>
                <span class="team-settings-panel-btn-danger-split-right">删除团队</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 团队设置面板组件
 * 
 * 功能：
 * 1. 显示团队设置选项
 * 2. 提供删除团队功能（仅 OWNER 可见）
 * 
 * Props：
 * - teamId: 团队ID（必填）
 * - teamName: 团队名称（用于确认对话框）
 * 
 * Events：
 * - deleted: 团队删除成功后触发
 */
export default {
  name: 'TeamSettingsPanel',
  props: {
    /**
     * 团队ID
     * @type {String|Number}
     */
    teamId: {
      type: [String, Number],
      required: true
    },
    
    /**
     * 团队名称
     * @type {String}
     */
    teamName: {
      type: String,
      default: ''
    }
  },
  methods: {
    /**
     * 处理删除团队操作
     * 显示确认对话框，确认后调用删除 API
     */
    async handleDeleteTeam () {
      try {
        // 显示确认对话框
        await this.$confirm(
          `确认删除团队「${this.teamName || this.teamId}」吗？此操作不可恢复！`,
          '删除团队',
          {
            confirmButtonText: '确认删除',
            cancelButtonText: '取消',
            type: 'error',
            confirmButtonClass: 'team-settings-panel-confirm-btn-danger'
          }
        )
        
        // 调用删除 API
        const { deleteTeam } = await import('@/api/team-api')
        const teamId = String(this.teamId || '').trim()
        await deleteTeam(teamId)
        
        // 显示成功消息
        this.$message.success('团队已删除')
        
        // 触发删除事件，由父组件处理跳转
        this.$emit('deleted', teamId)
      } catch (error) {
        // 用户取消操作时不显示错误
        if (error !== 'cancel') {
          this.$message.error(error.message || '删除团队失败')
        }
      }
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/team-detail/team-settings-panel.css';
</style>
