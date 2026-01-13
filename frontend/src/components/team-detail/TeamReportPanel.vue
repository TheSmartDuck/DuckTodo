<template>
  <!-- 团队报表面板根容器 -->
  <div class="team-report-panel">
    <!-- 顶部指标卡片网格 -->
    <div class="team-report-panel-metrics-grid">
      <!-- 进行中任务数卡片 -->
      <div class="team-report-panel-stat-box team-report-panel-stat-box-blue">
        <span class="team-report-panel-stat-val team-report-panel-stat-val-inprogress">{{ summary.teamInProgressTasksCount }}</span>
        <span class="team-report-panel-stat-key"><i class="el-icon-time"></i> IN_PROGRESS / 进行中</span>
      </div>
      <!-- 近7天完成任务数卡片 -->
      <div class="team-report-panel-stat-box team-report-panel-stat-box-green">
        <span class="team-report-panel-stat-val team-report-panel-stat-val-completed">{{ summary.teamCompletedLast7DaysCount }}</span>
        <span class="team-report-panel-stat-key"><i class="el-icon-check"></i> DONE_7D / 近7天完成</span>
      </div>
      <!-- 近7天新增任务数卡片 -->
      <div class="team-report-panel-stat-box team-report-panel-stat-box-mustard">
        <span class="team-report-panel-stat-val team-report-panel-stat-val-created">{{ summary.teamCreatedLast7DaysCount }}</span>
        <span class="team-report-panel-stat-key"><i class="el-icon-plus"></i> NEW_7D / 近7天新增</span>
      </div>
      <!-- 逾期任务数卡片 -->
      <div class="team-report-panel-stat-box team-report-panel-stat-box-red">
        <span class="team-report-panel-stat-val team-report-panel-stat-val-overdue">{{ summary.teamOverdueTasksCount }}</span>
        <span class="team-report-panel-stat-key"><i class="el-icon-warning"></i> OVERDUE / 逾期任务</span>
      </div>
    </div>

    <!-- 图表区域容器 -->
    <div class="team-report-panel-charts-container">
      <!-- 左侧图表区域：趋势分析 -->
      <div class="team-report-panel-chart-left">
        <div class="team-report-panel-chart-card">
          <div class="team-report-panel-chart-header">
            <span class="team-report-panel-chart-title">PROGRESS TREND / 趋势分析 (14D)</span>
            <i class="el-icon-setting team-report-panel-chart-setting-icon"></i>
          </div>
          <div class="team-report-panel-chart-wrapper">
            <div ref="progressLine" class="team-report-panel-full-chart"></div>
          </div>
        </div>
      </div>
      <!-- 右侧图表区域：分布图表 -->
      <div class="team-report-panel-chart-right">
        <!-- 进行中任务分布饼图 -->
        <div class="team-report-panel-chart-card team-report-panel-chart-card-half-h">
          <div class="team-report-panel-chart-header">
            <span class="team-report-panel-chart-title">DISTRIBUTION (ING) / 进行中分布</span>
          </div>
          <div class="team-report-panel-chart-wrapper">
            <div ref="pieInProgress" class="team-report-panel-full-chart"></div>
          </div>
        </div>
        <!-- 总任务分布饼图 -->
        <div class="team-report-panel-chart-card team-report-panel-chart-card-half-h">
          <div class="team-report-panel-chart-header">
            <span class="team-report-panel-chart-title">DISTRIBUTION (TOTAL) / 总任务分布</span>
          </div>
          <div class="team-report-panel-chart-wrapper">
            <div ref="pieCompleted" class="team-report-panel-full-chart"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

/**
 * 团队报表面板组件
 * 
 * 功能：
 * 1. 显示团队关键指标（进行中、完成、新增、逾期任务数）
 * 2. 渲染趋势分析折线图（14天燃尽图）
 * 3. 渲染进行中任务分布饼图
 * 4. 渲染总任务分布饼图
 * 
 * Props：
 * - summary: 团队统计数据对象
 *   - teamInProgressTasksCount: 进行中任务数
 *   - teamCompletedLast7DaysCount: 近7天完成任务数
 *   - teamCreatedLast7DaysCount: 近7天新增任务数
 *   - teamOverdueTasksCount: 逾期任务数
 *   - burndown: 燃尽图数据数组 [{ date, remaining, completed }]
 *   - workload: 工作负载数据数组 [{ userName, userId, inProgress, total }]
 */
export default {
  name: 'TeamReportPanel',
  props: {
    /**
     * 团队统计数据
     * @type {Object}
     */
    summary: {
      type: Object,
      required: true
    }
  },
  data () {
    return {
      /**
       * ECharts 图表实例集合
       * @type {Object}
       * @property {Object} line - 趋势分析折线图实例
       * @property {Object} pieInProgress - 进行中任务分布饼图实例
       * @property {Object} pieCompleted - 总任务分布饼图实例
       */
      charts: { 
        line: null, 
        pieInProgress: null, 
        pieCompleted: null 
      }
    }
  },
  /**
   * 组件挂载后初始化图表并监听窗口大小变化
   */
  mounted () {
    this.$nextTick(() => {
      this.renderCharts()
      window.addEventListener('resize', this.handleResize)
    })
  },
  /**
   * 组件销毁前清理事件监听和图表实例
   */
  beforeDestroy () {
    window.removeEventListener('resize', this.handleResize)
    Object.values(this.charts).forEach(c => c && c.dispose())
  },
  watch: {
    /**
     * 监听 summary 数据变化，重新渲染图表
     */
    summary: {
      deep: true,
      handler () {
        this.renderCharts()
      }
    }
  },
  methods: {
    /**
     * 获取主题色值
     * 从 CSS 变量中读取主题色值
     * @param {String} varName - CSS 变量名（如 '--mission-rust'）
     * @returns {String} 颜色值（如 '#B2653B'）
     */
    getThemeColor (varName) {
      return getComputedStyle(document.documentElement).getPropertyValue(varName).trim()
    },
    
    /**
     * 处理窗口大小变化事件
     * 调整所有图表尺寸以适应新窗口大小
     */
    handleResize () {
      Object.values(this.charts).forEach(c => c && c.resize())
    },
    
    /**
     * 渲染所有图表
     * 依次渲染折线图、进行中分布饼图、总任务分布饼图
     */
    renderCharts () {
      this.renderLine()
      this.renderPieInProgress()
      this.renderPieCompleted()
    },
    
    /**
     * 渲染趋势分析折线图
     * 显示14天内待办剩余任务数的变化趋势
     * 使用主题色并显示刻度标签
     */
    renderLine () {
      const el = this.$refs.progressLine
      if (!el) return
      
      // 初始化图表实例（如果尚未初始化）
      if (!this.charts.line) {
        this.charts.line = echarts.init(el)
      }
      
      // 获取主题色
      const rustColor = this.getThemeColor('--mission-rust') || '#B2653B'
      const textColor = this.getThemeColor('--space-black') || '#181818'
      const lineColor = this.getThemeColor('--space-black') || '#181818'
      
      // 获取燃尽图数据
      const burndown = Array.isArray(this.summary.burndown) ? this.summary.burndown : []
      
      // 提取日期标签和剩余任务数数据
      // Burndown 数据结构: { date, remaining, completed }
      const labels = burndown.map(x => x.date)
      const seriesData = burndown.map(x => x.remaining)
      
      // 配置折线图选项
      const option = {
        tooltip: { 
          trigger: 'axis',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: rustColor,
          borderWidth: 1,
          textStyle: {
            color: '#fff',
            fontFamily: 'var(--font-family)'
          }
        },
        grid: { 
          left: 50, 
          right: 30, 
          top: 40, 
          bottom: 50 
        },
        xAxis: { 
          type: 'category', 
          data: labels,
          // X轴刻度标签配置
          axisLabel: { 
            color: textColor, 
            fontSize: 11,
            fontFamily: 'var(--font-family)',
            rotate: 45, // 旋转45度避免重叠
            show: true // 显示刻度标签
          },
          axisLine: { 
            lineStyle: { 
              color: lineColor,
              width: 2
            } 
          },
          axisTick: {
            show: true,
            lineStyle: {
              color: lineColor
            }
          }
        },
        yAxis: { 
          type: 'value',
          // Y轴刻度标签配置
          axisLabel: { 
            color: textColor, 
            fontSize: 11,
            fontFamily: 'var(--font-family)',
            show: true // 显示刻度标签
          },
          axisLine: {
            show: true,
            lineStyle: {
              color: lineColor,
              width: 2
            }
          },
          axisTick: {
            show: true,
            lineStyle: {
              color: lineColor
            }
          },
          splitLine: { 
            lineStyle: { 
              color: 'rgba(0, 0, 0, 0.1)', 
              type: 'dashed',
              width: 1
            } 
          }
        },
        series: [{ 
          name: 'Backlog / 待办剩余', 
          type: 'line', 
          smooth: true, 
          data: seriesData,
          // 使用主题橙色
          color: rustColor,
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 2,
            color: rustColor
          },
          itemStyle: {
            color: rustColor,
            borderColor: '#fff',
            borderWidth: 2
          },
          areaStyle: { 
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: rustColor + '40' }, // 40% 透明度
                { offset: 1, color: rustColor + '10' }  // 10% 透明度
              ]
            }
          },
          // 显示数据标签
          label: {
            show: true,
            position: 'top',
            color: textColor,
            fontSize: 10,
            fontFamily: 'var(--font-family)'
          }
        }]
      }
      
      this.charts.line.setOption(option)
    },
    
    /**
     * 渲染进行中任务分布饼图
     * 显示各成员进行中任务数的分布情况
     * 使用主题色并显示刻度标签（百分比和名称）
     */
    renderPieInProgress () {
      const el = this.$refs.pieInProgress
      if (!el) return
      
      // 初始化图表实例（如果尚未初始化）
      if (!this.charts.pieInProgress) {
        this.charts.pieInProgress = echarts.init(el)
      }
      
      // 获取主题色
      const rustColor = this.getThemeColor('--mission-rust') || '#B2653B'
      const sageColor = this.getThemeColor('--oxidized-sage') || '#5C7F71'
      const mustardColor = this.getThemeColor('--avionics-mustard') || '#BA8530'
      const burgundyColor = this.getThemeColor('--alarm-burgundy') || '#802520'
      const textColor = this.getThemeColor('--space-black') || '#181818'
      const whiteColor = this.getThemeColor('--color-white') || '#FBF8F1'
      
      // 主题色数组，用于饼图配色
      const themeColors = [rustColor, sageColor, mustardColor, burgundyColor]
      
      // 获取工作负载数据并转换为饼图数据格式
      const list = Array.isArray(this.summary.workload) ? this.summary.workload : []
      const data = list.map((x, index) => ({ 
        name: x.userName || x.userId, 
        value: x.inProgress,
        itemStyle: {
          color: themeColors[index % themeColors.length]
        }
      }))
      
      // 配置饼图选项
      const option = {
        tooltip: { 
          trigger: 'item',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: rustColor,
          borderWidth: 1,
          textStyle: {
            color: '#fff',
            fontFamily: 'var(--font-family)'
          },
          formatter: '{b}: {c} ({d}%)'
        },
        series: [{ 
          type: 'pie', 
          radius: ['40%', '70%'], // 环形饼图
          center: ['50%', '50%'],
          data,
          // 显示标签：名称和百分比
          label: { 
            show: true,
            position: 'outside',
            formatter: '{b}\n{d}%',
            color: textColor,
            fontSize: 11,
            fontFamily: 'var(--font-family)',
            fontWeight: 'bold'
          },
          // 标签线配置
          labelLine: {
            show: true,
            length: 15,
            length2: 10,
            lineStyle: {
              color: textColor,
              width: 1
            }
          },
          itemStyle: { 
            borderRadius: 4, 
            borderColor: whiteColor, 
            borderWidth: 2 
          },
          // 强调效果：鼠标悬停时放大
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            },
            label: {
              fontSize: 12,
              fontWeight: 'bold'
            }
          }
        }]
      }
      
      this.charts.pieInProgress.setOption(option)
    },
    
    /**
     * 渲染总任务分布饼图
     * 显示各成员总任务数的分布情况
     * 使用主题色并显示刻度标签（百分比和名称）
     */
    renderPieCompleted () {
      const el = this.$refs.pieCompleted
      if (!el) return
      
      // 初始化图表实例（如果尚未初始化）
      if (!this.charts.pieCompleted) {
        this.charts.pieCompleted = echarts.init(el)
      }
      
      // 获取主题色
      const rustColor = this.getThemeColor('--mission-rust') || '#B2653B'
      const sageColor = this.getThemeColor('--oxidized-sage') || '#5C7F71'
      const mustardColor = this.getThemeColor('--avionics-mustard') || '#BA8530'
      const burgundyColor = this.getThemeColor('--alarm-burgundy') || '#802520'
      const textColor = this.getThemeColor('--space-black') || '#181818'
      const whiteColor = this.getThemeColor('--color-white') || '#FBF8F1'
      
      // 主题色数组，用于饼图配色
      const themeColors = [sageColor, rustColor, mustardColor, burgundyColor]
      
      // 获取工作负载数据并转换为饼图数据格式
      const list = Array.isArray(this.summary.workload) ? this.summary.workload : []
      const data = list.map((x, index) => ({ 
        name: x.userName || x.userId, 
        value: x.total,
        itemStyle: {
          color: themeColors[index % themeColors.length]
        }
      }))
      
      // 配置饼图选项
      const option = {
        tooltip: { 
          trigger: 'item',
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          borderColor: sageColor,
          borderWidth: 1,
          textStyle: {
            color: '#fff',
            fontFamily: 'var(--font-family)'
          },
          formatter: '{b}: {c} ({d}%)'
        },
        series: [{ 
          type: 'pie', 
          radius: ['40%', '70%'], // 环形饼图
          center: ['50%', '50%'],
          data,
          // 显示标签：名称和百分比
          label: { 
            show: true,
            position: 'outside',
            formatter: '{b}\n{d}%',
            color: textColor,
            fontSize: 11,
            fontFamily: 'var(--font-family)',
            fontWeight: 'bold'
          },
          // 标签线配置
          labelLine: {
            show: true,
            length: 15,
            length2: 10,
            lineStyle: {
              color: textColor,
              width: 1
            }
          },
          itemStyle: { 
            borderRadius: 4, 
            borderColor: whiteColor, 
            borderWidth: 2 
          },
          // 强调效果：鼠标悬停时放大
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            },
            label: {
              fontSize: 12,
              fontWeight: 'bold'
            }
          }
        }]
      }
      
      this.charts.pieCompleted.setOption(option)
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/team-detail/team-report-panel.css';
</style>
