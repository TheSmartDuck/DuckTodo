<template>
  <!-- 负载趋势图表模块根容器 -->
  <div class="load-trend-chart-module">
    <!-- 图表头部：标题和设置图标 -->
    <div class="load-trend-chart-header">
      <span class="load-trend-chart-title">LOAD TREND / 负载趋势</span>
      <i class="el-icon-setting" style="color: #ccc; cursor: pointer;"></i>
    </div>
    <!-- 图表主体：ECharts 容器 -->
    <div class="load-trend-chart-body" ref="chart"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import * as statsApi from '@/api/statistics-api'

/**
 * 负载趋势图表组件
 * 
 * 功能：
 * 1. 显示最近14天的任务和子任务负载趋势
 * 2. 使用 ECharts 绘制折线图，展示任务和子任务的数量变化
 * 3. 支持响应式布局，窗口大小变化时自动调整图表尺寸
 * 
 * 数据来源：
 * - 通过 statistics-api 的 getMyLoadTrend API 获取负载趋势数据
 * - 默认获取最近14天的数据
 * 
 * 图表配置：
 * - X轴：日期（MM-DD格式）
 * - Y轴：任务/子任务数量
 * - 系列：两条折线（Tasks 和 SubTasks）
 */
export default {
  name: 'LoadTrendChart',
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
     * 创建图表实例并设置基础配置
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
          top: 40,    // 顶部边距（为图例留出空间）
          right: 20,  // 右侧边距
          bottom: 30, // 底部边距（为X轴标签留出空间）
          left: 40    // 左侧边距（为Y轴标签留出空间）
        },
        // 提示框配置：鼠标悬停时显示数据
        tooltip: { 
          trigger: 'axis' // 触发方式：坐标轴触发
        },
        // 图例配置：显示系列名称
        legend: { 
          top: 10,           // 距离顶部距离
          right: 10,         // 距离右侧距离
          itemWidth: 10,    // 图例标记宽度
          itemHeight: 10,   // 图例标记高度
          textStyle: { 
            fontSize: 10    // 图例文字大小
          } 
        },
        // X轴配置：分类轴（日期）
        xAxis: { 
          type: 'category',           // 类型：类目轴
          data: [],                   // 数据（日期数组，初始为空）
          axisTick: { 
            alignWithLabel: true      // 刻度线与标签对齐
          } 
        },
        // Y轴配置：数值轴（任务数量）
        yAxis: { 
          type: 'value',               // 类型：数值轴
          splitLine: { 
            lineStyle: { 
              type: 'dashed'          // 分割线样式：虚线
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
     * 加载负载趋势数据
     * 从API获取最近14天的任务和子任务负载数据，并更新图表
     */
    async loadData () {
      try {
        // 调用API获取最近14天的负载趋势数据
        const res = await statsApi.getMyLoadTrend(14)
        // 提取数据项数组
        const items = res.items || []
        
        // 处理日期数据：提取 MM-DD 格式的日期字符串
        const dates = items.map(i => i.date.slice(5)) // 从 "YYYY-MM-DD" 提取 "MM-DD"
        // 提取任务数量数组
        const tasks = items.map(i => i.tasks)
        // 提取子任务数量数组
        const subs = items.map(i => i.childTasks)

        // 获取主题颜色（从CSS变量）
        const sageColor = this.getThemeColor('--oxidized-sage') || '#5C7F71'
        const mustardColor = this.getThemeColor('--avionics-mustard') || '#BA8530'

        // 更新图表配置和数据
        this.chartInstance.setOption({
          // 更新X轴数据（日期）
          xAxis: { 
            data: dates 
          },
          // 更新系列数据
          series: [
            {
              name: 'Tasks/任务负载',      // 系列名称：任务负载
              type: 'line',               // 图表类型：折线图
              data: tasks,                 // 数据：任务数量数组
              smooth: true,                // 平滑曲线
              symbol: 'circle',           // 数据点标记：圆形
              itemStyle: { 
                color: sageColor          // 数据点颜色：使用主题色（氧化鼠尾草绿）
              },
              lineStyle: {
                color: sageColor          // 线条颜色：使用主题色（氧化鼠尾草绿）
              },
              areaStyle: { 
                opacity: 0.1              // 区域填充透明度
              }
            },
            {
              name: 'SubTasks/子任务负载', // 系列名称：子任务负载
              type: 'line',               // 图表类型：折线图
              data: subs,                  // 数据：子任务数量数组
              smooth: true,                // 平滑曲线
              symbol: 'circle',           // 数据点标记：圆形
              itemStyle: { 
                color: mustardColor       // 数据点颜色：使用主题色（航空芥末黄）
              },
              lineStyle: {
                color: mustardColor       // 线条颜色：使用主题色（航空芥末黄）
              },
              areaStyle: { 
                opacity: 0.1              // 区域填充透明度
              }
            }
          ]
        })
      } catch (e) {
        console.error('LoadTrend chart failed', e)
      }
    },
    
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 获取主题颜色
     * 从CSS变量中获取颜色值
     * @param {string} varName - CSS变量名称（如 '--mission-rust'）
     * @returns {string} 颜色值（如 '#B2653B'）
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
@import '@/assets/css/data-panel/load-trend-chart.css';
</style>
