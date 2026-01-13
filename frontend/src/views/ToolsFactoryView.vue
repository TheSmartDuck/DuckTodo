<template>
  <div class="page">
    <el-card class="empty-card">
      <div class="placeholder">工具工厂预留处</div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ToolsFactoryView'
}
</script>

<style scoped>
.page { width: 100%; height: 100%; padding: 12px; box-sizing: border-box; }
.empty-card { height: 100%; text-align: center; background-color: transparent; border-color: #EBEEF5; }
.empty-card ::v-deep(.el-card__body) { height: 100%; display: flex; align-items: center; justify-content: center; }
.placeholder { font-size: 16px; color: #909399; }
</style>
