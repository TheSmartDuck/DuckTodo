<template>
  <!-- 仪表板组件根容器 -->
  <div class="dashboard-widget-mission-widget">
     <!-- 卡片头部：包含标题和配置按钮 -->
     <div class="dashboard-widget-card-header" :class="{ 'dashboard-widget-alert-header': isAlert }">
        <span class="dashboard-widget-header-text">{{ title }}</span>
        <div style="display:flex; align-items:center;">
            <!-- 配置按钮：点击打开配置对话框 -->
            <i class="el-icon-setting dashboard-widget-widget-config-btn" @click="openConfig"></i>
        </div>
     </div>
     
     <!-- 组件内容区域：包含列表或图表 -->
     <div class="dashboard-widget-widget-content">
        <!-- 列表类型组件：显示任务列表 -->
        <div v-if="type === 'list'" class="dashboard-widget-list-container">
           <!-- 空状态：无数据时显示 -->
           <div v-if="(!data || data.length === 0) && !demo" class="dashboard-widget-retro-empty">
              <div class="dashboard-widget-empty-icon"><i class="el-icon-cpu"></i></div>
              <div class="dashboard-widget-empty-text">NO DATA</div>
           </div>
           <!-- 任务列表：有数据时显示 -->
           <div v-else>
              <!-- 遍历显示每个任务项 -->
              <div v-for="item in displayData" 
                   :key="item.id" 
                   class="dashboard-widget-mission-list-row" 
                   :class="{ 
                     'dashboard-widget-is-timeout': isTimeout(item.time) 
                   }" 
                   @click="handleItemClick(item)">
                 
                 <!-- 装饰条：左侧彩色装饰条 -->
                 <div class="dashboard-widget-row-decor-bar"></div>

                 <!-- 主内容区域：包含日期、分隔符和任务名称 -->
                 <div class="dashboard-widget-row-main-content">
                    <!-- 到期日期 -->
                    <span class="dashboard-widget-row-date">{{ formatDate(item.time) }}</span>
                    <!-- 分隔符 -->
                    <span class="dashboard-widget-row-dots">::</span>
                    <!-- 任务名称 -->
                    <span class="dashboard-widget-row-name" :title="item.name">{{ item.name || 'UNKNOWN MISSION' }}</span>
                 </div>

                 <!-- 右侧状态区域：包含状态标签和任务组信息 -->
                 <div class="dashboard-widget-row-right-status">
                    <!-- 状态标签 -->
                    <div class="dashboard-widget-status-pill" :class="isTimeout(item.time) ? 'dashboard-widget-timeout' : (item.status || 'PROCESS').toLowerCase()">
                       {{ isTimeout(item.time) ? 'TIMEOUT' : (item.status || 'PROCESS') }}
                    </div>
                    <!-- 任务组信息 -->
                    <div class="dashboard-widget-group-info" :title="item.groupName">
                       <span class="dashboard-widget-group-icon">G</span>
                       <span class="dashboard-widget-group-name">{{ item.groupName || 'NULL' }}</span>
                    </div>
                 </div>

              </div>
           </div>
        </div>
        
        <!-- 图表类型组件：显示 ECharts 图表 -->
        <div v-else-if="type === 'chart'" class="dashboard-widget-chart-container">
           <div ref="chart" class="dashboard-widget-echart-box"></div>
        </div>
     </div>

     <!-- 底部装饰条：三色装饰条 -->
     <div class="dashboard-widget-bottom-bar">
        <div class="dashboard-widget-bar-seg dashboard-widget-seg-mustard"></div>
        <div class="dashboard-widget-bar-seg dashboard-widget-seg-sage"></div>
        <div class="dashboard-widget-bar-seg dashboard-widget-seg-burgundy"></div>
     </div>

     <!-- 配置对话框：用于配置组件类型 -->
     <el-dialog 
       :title="`MODULE CONFIG // 模块配置`" 
       :visible.sync="configVisible" 
       width="800px" 
       custom-class="home-retro-dialog"
       :append-to-body="true"
     >
        <div class="home-config-container">
           <div class="home-config-item">
               <span class="home-label">SELECT MODULE TYPE</span>
               <!-- 组件类型选择器 -->
               <el-select v-model="tempWidgetKey" size="small" class="home-retro-select" popper-class="retro-dropdown">
                  <el-option v-for="opt in widgetOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
               </el-select>
           </div>
           
           <!-- 预览示例：实时预览选中的组件 -->
           <div class="home-demo-box">
              <div class="home-demo-label">LIVE PREVIEW</div>
              <div style="height: 400px; border: 1px solid #eee; overflow: hidden; position: relative;">
                  <!-- 渲染选中组件的缩小版本 -->
                  <dashboard-widget 
                      :widget-key="tempWidgetKey" 
                      :demo="true" 
                      style="box-shadow: none; border: none; padding: 4px;"
                  />
              </div>
           </div>
        </div>
        <span slot="footer" class="dialog-footer">
           <el-button @click="configVisible = false" size="small" class="home-retro-btn-plain">CANCEL</el-button>
           <el-button type="primary" @click="saveConfig" size="small" class="home-retro-btn-primary">APPLY</el-button>
        </span>
     </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'

/**
 * 组件定义常量
 * 包含所有可用组件的类型、标题、标签等信息
 */
const WIDGET_DEFS = {
  // 最近任务组件
  recentTasks: {
    title: 'RECENT TASKS / 最近待办',
    type: 'list',
    label: '最近待办'
  },
  // 超期任务组件（警告样式）
  overdueTasks: {
    title: 'OVERDUE ALERTS / 超期任务',
    type: 'list',
    isAlert: true,
    label: '超期任务'
  },
  // 负载趋势图表组件（14天）
  loadTrend: {
    title: 'LOAD TREND (14D) / 负载趋势',
    type: 'chart',
    label: '负载趋势 (14天)'
  },
  // 任务活动趋势图表组件
  taskTrend: {
    title: 'TASK ACTIVITY / 任务活动',
    type: 'chart',
    label: '任务活动趋势'
  }
}

/**
 * 仪表板组件
 * 
 * 功能：
 * 1. 支持两种类型的组件：列表类型（显示任务列表）和图表类型（显示 ECharts 图表）
 * 2. 列表类型支持任务项的点击跳转
 * 3. 支持超时任务的特殊样式显示
 * 4. 支持通过配置对话框自定义组件类型
 * 5. 支持演示模式（显示模拟数据）
 */
export default {
  name: 'DashboardWidget',
  props: {
    // 组件键（决定显示哪种类型的组件）
    widgetKey: {
      type: String,
      default: 'recentTasks'
    },
    // 组件数据（列表类型为数组，图表类型为对象或数组）
    data: {
      type: [Array, Object],
      default: () => []
    },
    // 是否为演示模式（显示模拟数据）
    demo: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      // ECharts 图表实例
      chartInstance: null,
      // 配置对话框是否可见
      configVisible: false,
      // 临时存储的组件键（用于配置对话框）
      tempWidgetKey: ''
    }
  },
  computed: {
    /**
     * 获取组件定义
     * 根据组件键获取对应的定义对象，如果不存在则返回默认值
     * @returns {Object} 组件定义对象
     */
    def () {
      return WIDGET_DEFS[this.widgetKey] || {
        title: 'UNKNOWN',
        type: 'list'
      }
    },
    
    /**
     * 组件标题
     * @returns {string} 组件标题
     */
    title () {
      return this.def.title
    },
    
    /**
     * 组件类型
     * @returns {string} 组件类型（'list' 或 'chart'）
     */
    type () {
      return this.def.type
    },
    
    /**
     * 是否为警告样式
     * @returns {boolean} 是否为警告样式
     */
    isAlert () {
      return this.def.isAlert
    },
    
    /**
     * 显示的数据
     * 如果是演示模式且为列表类型，返回模拟数据；否则返回实际数据
     * @returns {Array} 显示的数据数组
     */
    displayData () {
      // 演示模式：返回模拟数据
      if (this.demo && this.type === 'list') {
        return [
          {
            id: '7FDB2319B2AC9A76D844183867C1B0BF',
            groupName: 'DEV_CORE',
            name: 'Refactor Widget Components for UI Consistency',
            time: '2026-01-31',
            status: 'PROCESS'
          },
          {
            id: '97E26847CE11FA66847CE5C729F50FCF',
            groupName: 'UI_REVAMP',
            name: 'Update Dashboard CSS to Dark Theme',
            time: '2025-12-24',
            status: 'TIMEOUT'
          },
          {
            id: '3',
            groupName: 'TESTING',
            name: 'Unit Tests for New Modules',
            time: '2026-02-15',
            status: 'PENDING'
          }
        ]
      }
      // 正常模式：返回实际数据
      return Array.isArray(this.data) ? this.data : []
    },
    
    /**
     * 组件选项列表
     * 用于配置对话框的下拉选择器
     * @returns {Array<{label: string, value: string}>} 组件选项数组
     */
    widgetOptions () {
      return Object.keys(WIDGET_DEFS).map(k => ({
        label: WIDGET_DEFS[k].label,
        value: k
      }))
    }
  },
  watch: {
    /**
     * 监听数据变化，重新渲染图表
     */
    data: {
      handler () {
        this.$nextTick(this.renderChart)
      },
      deep: true
    },
    
    /**
     * 监听组件键变化，重新渲染图表
     */
    widgetKey () {
      // 如果切换为非图表类型，清理图表实例
      if (this.type !== 'chart' && this.chartInstance) {
        this.chartInstance.dispose()
        this.chartInstance = null
      }
      // 重新渲染图表
      this.$nextTick(this.renderChart)
    }
  },
  /**
   * 组件挂载时初始化图表
   */
  mounted () {
    this.$nextTick(this.renderChart)
    // 监听窗口大小变化，自动调整图表大小
    window.addEventListener('resize', this.handleResize)
  },
  /**
   * 组件销毁前清理资源
   */
  beforeDestroy () {
    // 移除窗口大小变化监听
    window.removeEventListener('resize', this.handleResize)
    // 销毁图表实例
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  methods: {
    /**
     * ========== 格式化方法 ==========
     */
    
    /**
     * 获取CSS变量值
     * 从全局主题样式中获取CSS变量值
     * @param {string} varName - CSS变量名（如 '--mission-rust'）
     * @returns {string} CSS变量值
     */
    getThemeColor (varName) {
      return getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
    },
    
    /**
     * 格式化日期
     * 将日期字符串格式化为 YYYY-MM-DD 格式
     * @param {string} str - 日期字符串
     * @returns {string} 格式化后的日期字符串（YYYY-MM-DD），无效时返回 '-'
     */
    formatDate (str) {
      if (!str) return '-'
      return str.slice(0, 10)
    },
    
    /**
     * 缩短ID
     * 将ID字符串缩短为8位大写字母
     * @param {string|number} id - ID值
     * @returns {string} 缩短后的ID字符串
     */
    shortId (id) {
      if (!id) return '000'
      return id.toString().slice(0, 8).toUpperCase()
    },
    
    /**
     * ========== 判断方法 ==========
     */
    
    /**
     * 判断任务是否超时
     * 比较任务的到期日期是否早于今天
     * @param {string} timeStr - 任务的到期日期字符串
     * @returns {boolean} 是否超时
     */
    isTimeout (timeStr) {
      if (!timeStr) return false
      
      const today = new Date()
      const date = new Date(timeStr)
      
      // 检查日期是否有效
      if (isNaN(date.getTime())) return false
      
      // 重置时间为0点，只比较日期
      today.setHours(0, 0, 0, 0)
      date.setHours(0, 0, 0, 0)
      
      // 如果任务的到期日期早于今天，则为超时
      return date < today
    },
    
    /**
     * ========== 事件处理方法 ==========
     */
    
    /**
     * 处理任务项点击事件
     * 触发 item-click 事件，将任务项数据传递给父组件
     * @param {Object} item - 被点击的任务项对象
     */
    handleItemClick (item) {
      // 演示模式下不触发事件
      if (this.demo) return
      // 触发事件，将任务项数据传递给父组件
      this.$emit('item-click', item)
    },
    
    /**
     * ========== 图表渲染方法 ==========
     */
    
    /**
     * 渲染图表
     * 根据组件类型和组件键渲染对应的 ECharts 图表
     */
    renderChart () {
      // 如果不是图表类型，不执行渲染
      if (this.type !== 'chart') return
      
      const el = this.$refs.chart
      if (!el) return
      
      // 如果已存在图表实例，先销毁
      if (this.chartInstance) {
        this.chartInstance.dispose()
      }
      
      // 初始化 ECharts 实例
      this.chartInstance = echarts.init(el)
      
      // 通用图表配置
      const commonOption = {
        grid: { top: 30, right: 10, bottom: 20, left: 30 },
        tooltip: { trigger: 'axis' },
        textStyle: {
          /* 使用全局字体族变量 */
          fontFamily: 'Courier Prime'
        },
        animation: !this.demo // 演示模式下禁用动画
      }
      
      // 获取图表数据
      let chartData = this.data
      
      // 演示模式：生成模拟数据
      if (this.demo) {
        chartData = []
        for (let i = 0; i < 7; i++) {
          chartData.push({
            date: `2024-01-0${i + 1}`,
            value1: Math.floor(Math.random() * 10),
            value2: Math.floor(Math.random() * 10)
          })
        }
      }

      // 根据组件键渲染不同的图表
      if (this.widgetKey === 'loadTrend') {
        // 负载趋势图表：双折线图
        const items = Array.isArray(chartData) ? chartData : []
        // 获取主题色
        const rustColor = this.getThemeColor('--mission-rust') || '#B2653B' // 任务铁锈红
        const sageColor = this.getThemeColor('--oxidized-sage') || '#5C7F71' // 氧化鼠尾草绿
        // 确保0值也被正确渲染，使用 != null 来检查（会同时检查 null 和 undefined）
        // 这样当值为0时不会被替换为fallback值
        this.chartInstance.setOption({
          ...commonOption,
          xAxis: {
            type: 'category',
            data: items.map(i => (i.date || '').slice(5)) // 只显示月-日
          },
          yAxis: {
            type: 'value',
            splitLine: { lineStyle: { type: 'dashed' } }
          },
          series: [
            {
              name: '[Tasks]',
              type: 'line',
              // 使用 != null 检查，确保0值不会被替换为fallback值
              // 如果 tasks 字段不存在，则使用 value1（演示模式）或 0
              data: items.map(i => {
                if (i.tasks != null) return i.tasks
                if (i.value1 != null) return i.value1
                return 0
              }),
              smooth: true,
              itemStyle: { color: rustColor },
              lineStyle: { color: rustColor },
              // 确保连接所有点，包括0值（connectNulls: false 表示不连接null值，但我们使用的是0而不是null）
              connectNulls: false
            },
            {
              name: '[SubTasks]',
              type: 'line',
              // 使用 != null 检查，确保0值不会被替换为fallback值
              // 如果 childTasks 字段不存在，则使用 value2（演示模式）或 0
              data: items.map(i => {
                if (i.childTasks != null) return i.childTasks
                if (i.value2 != null) return i.value2
                return 0
              }),
              smooth: true,
              itemStyle: { color: sageColor },
              lineStyle: { color: sageColor },
              // 确保连接所有点，包括0值（connectNulls: false 表示不连接null值，但我们使用的是0而不是null）
              connectNulls: false
            }
          ]
        })
      } else if (this.widgetKey === 'taskTrend') {
        // 任务活动趋势图表：双柱状图
        const items = Array.isArray(chartData) ? chartData : []
        // 获取主题色
        const mustardColor = this.getThemeColor('--avionics-mustard') || '#BA8530' // 航空芥末黄
        const sageColor = this.getThemeColor('--oxidized-sage') || '#5C7F71' // 氧化鼠尾草绿
        this.chartInstance.setOption({
          ...commonOption,
          xAxis: {
            type: 'category',
            data: items.map(i => (i.date || '').slice(5)) // 只显示月-日
          },
          yAxis: {
            type: 'value',
            splitLine: { lineStyle: { type: 'dashed' } }
          },
          series: [
            {
              name: 'Created',
              type: 'bar',
              data: items.map(i => i.created || i.value1),
              itemStyle: { color: mustardColor }
            },
            {
              name: 'Completed',
              type: 'bar',
              data: items.map(i => i.completed || i.value2),
              itemStyle: { color: sageColor }
            }
          ]
        })
      }
    },
    
    /**
     * 处理窗口大小变化
     * 当窗口大小变化时，自动调整图表大小
     */
    handleResize () {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },
    
    /**
     * ========== 配置方法 ==========
     */
    
    /**
     * 打开配置对话框
     * 初始化临时组件键并显示配置对话框
     */
    openConfig () {
      // 将当前组件键复制到临时变量
      this.tempWidgetKey = this.widgetKey
      // 显示配置对话框
      this.configVisible = true
    },
    
    /**
     * 保存配置
     * 将临时组件键发送给父组件并关闭配置对话框
     */
    saveConfig () {
      // 触发事件，将新的组件键发送给父组件
      this.$emit('update:widget-key', this.tempWidgetKey)
      // 关闭配置对话框
      this.configVisible = false
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/home/dashboard-widget.css';
</style>
