<template>
  <div class="page-header">
    <div class="header-left">
      <span class="header-main-title">TEAM DETAIL</span>
      <span class="header-subtitle">项目详情</span>
      <span class="header-separator">>></span>
      <span class="header-context">{{ teamName || 'UNNAMED PROJECT' }}</span>
    </div>
    <div class="header-right">MISSION CONTROL // ID: {{ teamId }}</div>
  </div>
</template>

<script>
export default {
  name: 'TeamDetailHeader',
  props: {
    teamName: String,
    teamId: [String, Number],
    teamIndex: [String, Number],
    teamLogo: String
  }
}
</script>

<style scoped>
@import '@/assets/css/team-detail/team-detail-header.css';
</style>
