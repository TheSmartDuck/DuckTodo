<template>
  <!-- 数据面板视图根容器 -->
  <div class="data-panel-view">
    <!-- 面板头部：显示标题和上下文信息 -->
    <div class="data-panel-header">
      <div class="data-panel-header-left">
        <!-- 主标题：英文标题 -->
        <span class="data-panel-header-main-title">DATA PANEL</span>
        <!-- 副标题：中文标题 -->
        <span class="data-panel-header-subtitle">数据统计</span>
        <!-- 分隔符 -->
        <span class="data-panel-header-separator">>></span>
        <!-- 上下文信息 -->
        <span class="data-panel-header-context">全局数据概览</span>
      </div>
      <!-- 头部右侧装饰文字 -->
      <div class="data-panel-header-right">MISSION CONTROL // STATISTICS</div>
    </div>
    
    <!-- 面板内容区域 -->
    <div class="data-panel-content">
      <!-- 左侧边栏：数据概览 -->
      <div class="data-panel-content-left">
        <DataOverviewSidebar />
      </div>

      <!-- 右侧内容：图表区域 -->
      <div class="data-panel-content-right">
        <!-- 顶部行：负载趋势图表 -->
        <div class="data-panel-chart-row data-panel-top-row">
           <LoadTrendChart />
        </div>
        
        <!-- 底部行：优先级分布和过期预测图表 -->
        <div class="data-panel-chart-row data-panel-bottom-row">
           <!-- 优先级分布图表列 -->
           <div class="data-panel-chart-col">
              <PriorityDistChart />
           </div>
           <!-- 过期预测图表列 -->
           <div class="data-panel-chart-col">
              <ExpirationForecastChart />
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import DataOverviewSidebar from '@/components/data-panel/DataOverviewSidebar.vue'
import LoadTrendChart from '@/components/data-panel/LoadTrendChart.vue'
import PriorityDistChart from '@/components/data-panel/PriorityDistChart.vue'
import ExpirationForecastChart from '@/components/data-panel/ExpirationForecastChart.vue'

/**
 * 数据面板视图组件
 * 
 * 功能：
 * 1. 显示全局数据统计概览
 * 2. 展示负载趋势图表
 * 3. 展示优先级分布图表
 * 4. 展示过期预测图表
 * 
 * 布局结构：
 * - 头部：标题和上下文信息
 * - 左侧边栏：数据概览组件
 * - 右侧内容：多个图表组件（负载趋势、优先级分布、过期预测）
 */
export default {
  name: 'DataPanelView',
  components: {
    // 数据概览侧边栏组件
    DataOverviewSidebar,
    // 负载趋势图表组件
    LoadTrendChart,
    // 优先级分布图表组件
    PriorityDistChart,
    // 过期预测图表组件
    ExpirationForecastChart
  },
  data () {
    return {
      // 当前日期（用于数据统计，当前未使用）
      currentDate: '2026-01-01'
    }
  },
  /**
   * 组件挂载后的生命周期钩子
   * 初始化当前日期
   */
  mounted () {
    // 设置当前日期为今天的日期（ISO格式：YYYY-MM-DD）
    this.currentDate = new Date().toISOString().split('T')[0]
  }
}
</script>

<style scoped>
@import '@/assets/css/data-panel/data-panel-view.css';
</style>
