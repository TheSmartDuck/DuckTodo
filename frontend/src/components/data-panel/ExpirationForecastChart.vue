<template>
  <!-- 到期预测图表模块根容器 -->
  <div class="expiration-forecast-chart-module">
    <!-- 图表头部：标题和设置图标 -->
    <div class="expiration-forecast-chart-header">
      <span class="expiration-forecast-chart-title">FORECAST / 到期预测</span>
      <i class="el-icon-setting" style="color: #ccc; cursor: pointer;"></i>
    </div>
    <!-- 图表主体：ECharts 容器 -->
    <div class="expiration-forecast-chart-body" ref="chart"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import * as statsApi from '@/api/statistics-api'

/**
 * 到期预测图表组件
 * 
 * 功能：
 * 1. 显示任务到期预测情况
 * 2. 使用 ECharts 绘制柱状图，展示不同时间段内即将到期的任务数量
 * 3. 支持响应式布局，窗口大小变化时自动调整图表尺寸
 * 
 * 数据来源：
 * - 通过 statistics-api 的 getMyExpirationForecast API 获取到期预测数据
 * - 默认获取任务（tasks）的到期预测
 * - 时间桶（buckets）：today（今天）、3d（3天内）、7d（7天内）、14d（14天内）、30d（30天内）
 * 
 * 图表配置：
 * - 图表类型：柱状图（bar chart）
 * - X轴：时间桶中文标签（今日到期、3天内到期、7天内到期、14天内到期、30天内到期）
 * - Y轴：任务数量
 * - 柱状图颜色（磁带未来主义配色，纯色，无渐变）：
 *   - 今日到期：深空黑（--space-black）
 *   - 3天内到期：警报酒红（--alarm-burgundy）
 *   - 7天内到期：任务铁锈红（--mission-rust）
 *   - 14天内到期：航空芥末黄（--avionics-mustard）
 *   - 30天内到期：氧化鼠尾草绿（--oxidized-sage）
 */
export default {
  name: 'ExpirationForecastChart',
  data () {
    return {
      // ECharts 图表实例
      chartInstance: null
    }
  },
  /**
   * 组件挂载后的生命周期钩子
   * 初始化图表并加载数据，监听窗口大小变化
   */
  mounted () {
    this.$nextTick(() => {
      // 初始化图表
      this.initChart()
      // 加载数据
      this.loadData()
      // 监听窗口大小变化，自动调整图表尺寸
      window.addEventListener('resize', this.handleResize)
    })
  },
  /**
   * 组件销毁前的生命周期钩子
   * 清理事件监听器和图表实例
   */
  beforeDestroy () {
    // 移除窗口大小变化监听器
    window.removeEventListener('resize', this.handleResize)
    // 销毁图表实例，释放资源
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  methods: {
    /**
     * ========== 图表初始化方法 ==========
     */
    
    /**
     * 初始化 ECharts 图表
     * 创建图表实例并设置基础配置（柱状图）
     */
    initChart () {
      // 检查图表容器是否存在
      if (!this.$refs.chart) return
      
      // 初始化 ECharts 实例
      this.chartInstance = echarts.init(this.$refs.chart)
      
      // 设置图表基础配置
      this.chartInstance.setOption({
        // 图表网格配置：设置图表与容器的边距
        grid: { 
          top: 30,              // 顶部边距
          right: 20,            // 右侧边距
          bottom: 20,           // 底部边距
          left: 20,             // 左侧边距
          containLabel: true    // 包含坐标轴标签在内
        },
        // 提示框配置：鼠标悬停时显示数据
        tooltip: { 
          trigger: 'axis',      // 触发方式：坐标轴触发
          axisPointer: { 
            type: 'shadow'      // 指示器类型：阴影指示器
          } 
        },
        // X轴配置：分类轴（时间桶）
        xAxis: { 
          type: 'category',     // 类型：类目轴
          data: [],             // 数据（时间桶标签数组，初始为空）
          axisLabel: { 
            fontSize: 10,       // 标签字体大小
            interval: 0         // 标签显示间隔：0表示强制显示所有标签
          } 
        },
        // Y轴配置：数值轴（任务数量）
        yAxis: { 
          type: 'value',        // 类型：数值轴
          splitLine: { 
            lineStyle: { 
              type: 'dashed'   // 分割线样式：虚线
            } 
          } 
        },
        // 系列配置：数据系列数组（初始为空，等待数据加载）
        series: []
      })
    },
    
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载到期预测数据
     * 从API获取不同时间段内即将到期的任务数量，并更新图表
     */
    async loadData () {
      try {
        // 调用API获取到期预测数据
        // 时间桶：today（今天）、3d（3天内）、7d（7天内）、14d（14天内）、30d（30天内）
        const res = await statsApi.getMyExpirationForecast({ 
          buckets: 'today,3d,7d,14d,30d', 
          scope: 'tasks' 
        })
        // 提取数据项数组
        const items = res.items || []
        
        // 将时间桶映射为中文标签
        const labels = items.map(i => this.getBucketLabel(i.bucket))
        // 将数据映射为带颜色的数据项
        const data = items.map(i => ({
          value: i.count,
          itemStyle: {
            color: this.getBucketColor(i.bucket)
          }
        }))

        // 更新图表配置和数据
        this.chartInstance.setOption({
          // 更新X轴数据（时间桶中文标签）
          xAxis: { 
            data: labels 
          },
          // 更新系列数据
          series: [{
            name: 'Expiring',           // 系列名称：即将到期
            type: 'bar',                // 图表类型：柱状图
            data: data,                  // 数据：带颜色的任务数量数组
            barWidth: '40%'             // 柱状图宽度：40%
          }]
        })
      } catch (e) {
        console.error('Expiration chart failed', e)
      }
    },
    
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 获取时间桶的中文标签
     * 将API返回的时间桶标识映射为中文标签
     * @param {string} bucket - 时间桶标识（today, 3d, 7d, 14d, 30d）
     * @returns {string} 中文标签
     */
    getBucketLabel (bucket) {
      const labelMap = {
        'today': '今日到期',
        '3d': '3天内到期',
        '7d': '7天内到期',
        '14d': '14天内到期',
        '30d': '30天内到期'
      }
      return labelMap[bucket] || bucket
    },
    
    /**
     * 根据时间桶获取对应的颜色
     * 使用磁带未来主义配色方案
     * @param {string} bucket - 时间桶标识（today, 3d, 7d, 14d, 30d）
     * @returns {string} 颜色值（十六进制）
     */
    getBucketColor (bucket) {
      // 今日到期：深空黑（最紧急）
      if (bucket === 'today') {
        return this.getThemeColor('--space-black') || '#181818'
      }
      // 3天内到期：警报酒红（紧急）
      if (bucket === '3d') {
        return this.getThemeColor('--alarm-burgundy') || '#802520'
      }
      // 7天内到期：任务铁锈红（较紧急）
      if (bucket === '7d') {
        return this.getThemeColor('--mission-rust') || '#B2653B'
      }
      // 14天内到期：航空芥末黄（中等）
      if (bucket === '14d') {
        return this.getThemeColor('--avionics-mustard') || '#BA8530'
      }
      // 30天内到期：氧化鼠尾草绿（较宽松）
      if (bucket === '30d') {
        return this.getThemeColor('--oxidized-sage') || '#5C7F71'
      }
      // 默认：深空黑
      return this.getThemeColor('--space-black') || '#181818'
    },
    
    /**
     * 获取主题颜色
     * 从CSS变量中获取颜色值
     * @param {string} varName - CSS变量名称（如 '--avionics-mustard'）
     * @returns {string} 颜色值（如 '#BA8530'）
     */
    getThemeColor (varName) {
      return getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
    },
    
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 获取主题颜色
     * 从CSS变量中获取颜色值
     * @param {string} varName - CSS变量名称（如 '--avionics-mustard'）
     * @returns {string} 颜色值（如 '#BA8530'）
     */
    getThemeColor (varName) {
      return getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
    },
    
    /**
     * ========== 事件处理方法 ==========
     */
    
    /**
     * 处理窗口大小变化事件
     * 当窗口大小改变时，自动调整图表尺寸以适应容器
     */
    handleResize () {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/data-panel/expiration-forecast-chart.css';
</style>
