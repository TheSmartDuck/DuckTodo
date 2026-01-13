<template>
  <!-- 优先级分布图表模块根容器 -->
  <div class="priority-dist-chart-module">
    <!-- 图表头部：标题和设置图标 -->
    <div class="priority-dist-chart-header">
      <span class="priority-dist-chart-title">PRIORITY / 优先级分布</span>
      <i class="el-icon-setting" style="color: #ccc; cursor: pointer;"></i>
    </div>
    <!-- 图表主体：ECharts 容器 -->
    <div class="priority-dist-chart-body" ref="chart"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import * as statsApi from '@/api/statistics-api'

/**
 * 优先级分布图表组件
 * 
 * 功能：
 * 1. 显示任务优先级分布情况
 * 2. 使用 ECharts 绘制环形饼图，展示不同优先级的任务数量占比
 * 3. 支持响应式布局，窗口大小变化时自动调整图表尺寸
 * 
 * 数据来源：
 * - 通过 statistics-api 的 getMyPriorityDistribution API 获取优先级分布数据
 * - 默认获取任务（tasks）的优先级分布
 * 
 * 图表配置：
 * - 图表类型：环形饼图（donut chart）
 * - 图例位置：右侧垂直排列
 * - 优先级颜色映射（磁带未来主义配色）：
 *   - P0: Alarm Burgundy (Red-ish) - 警报酒红，最高优先级
 *   - P1: Mission Rust (Orange-ish) - 任务铁锈红，高优先级
 *   - P2: Avionics Mustard (Yellow-ish) - 航空芥末黄，中优先级
 *   - P3: Oxidized Sage (Green-ish) - 氧化鼠尾草绿，低优先级
 *   - P4: Space Black (Dark) - 深空黑，最低优先级/无优先级
 */
export default {
  name: 'PriorityDistChart',
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
     * 创建图表实例并设置基础配置（环形饼图）
     */
    initChart () {
      // 检查图表容器是否存在
      if (!this.$refs.chart) return
      
      // 初始化 ECharts 实例
      this.chartInstance = echarts.init(this.$refs.chart)
      
      // 设置图表基础配置
      this.chartInstance.setOption({
        // 提示框配置：鼠标悬停时显示数据
        tooltip: { 
          trigger: 'item' // 触发方式：数据项触发
        },
        // 图例配置：显示系列名称
        legend: { 
          orient: 'vertical',        // 图例排列方向：垂直
          right: 10,                 // 距离右侧距离
          top: 'center',             // 垂直居中
          itemWidth: 10,             // 图例标记宽度
          itemHeight: 10,            // 图例标记高度
          textStyle: { 
            fontSize: 10              // 图例文字大小
          } 
        },
        // 系列配置：饼图系列
        series: [
          {
            name: 'Priority',                    // 系列名称：优先级
            type: 'pie',                         // 图表类型：饼图
            radius: ['40%', '70%'],             // 内外半径：环形饼图
            center: ['35%', '50%'],             // 圆心位置：左侧35%，垂直居中
            avoidLabelOverlap: false,           // 是否防止标签重叠
            itemStyle: {
              borderRadius: 4,                  // 扇形圆角
              borderColor: '#fff',              // 边框颜色：白色
              borderWidth: 2                    // 边框宽度
            },
            label: { 
              show: false                        // 默认不显示标签
            },
            emphasis: {
              // 高亮状态配置
              label: { 
                show: true,                      // 高亮时显示标签
                fontSize: '12',                  // 标签字体大小
                fontWeight: 'bold'               // 标签字体粗细
              }
            },
            data: []                            // 数据（初始为空，等待数据加载）
          }
        ]
      })
    },
    
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载优先级分布数据
     * 从API获取任务优先级分布数据，并更新图表
     */
    async loadData () {
      try {
        // 调用API获取任务优先级分布数据
        const res = await statsApi.getMyPriorityDistribution({ scope: 'tasks' })
        // 提取数据项数组
        const items = res.items || []
        
        // 将API数据映射为ECharts数据格式
        // 优先级映射：P0-P4（根据实际API/后端枚举调整）
        // P0: Alarm Burgundy, P1: Mission Rust, P2: Avionics Mustard, P3: Oxidized Sage, P4: Space Black
        // 如果API直接返回名称，则使用名称
        const data = items.map(i => ({
          value: i.count,                       // 数值：该优先级的任务数量
          name: i.name || `P${i.priority}`,   // 名称：优先级名称或P+优先级代码
          itemStyle: { 
            color: this.getColor(i.priority)   // 颜色：根据优先级获取对应颜色
          }
        }))

        // 更新图表数据
        this.chartInstance.setOption({
          series: [{ 
            data 
          }]
        })
      } catch (e) {
        console.error('PriorityDist chart failed', e)
      }
    },
    
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 根据优先级获取颜色
     * 将优先级代码或名称映射为对应的主题颜色值
     * 使用磁带未来主义配色方案
     * 
     * 优先级配色体系：
     * - P0: Alarm Burgundy (Red-ish) - 警报酒红，最高优先级
     * - P1: Mission Rust (Orange-ish) - 任务铁锈红，高优先级
     * - P2: Avionics Mustard (Yellow-ish) - 航空芥末黄，中优先级
     * - P3: Oxidized Sage (Green-ish) - 氧化鼠尾草绿，低优先级
     * - P4: Space Black (Dark) - 深空黑，最低优先级/无优先级
     * 
     * @param {number|string} p - 优先级代码（0/1/2/3/4）或名称（High/Medium/Low等）
     * @returns {string} 颜色值（十六进制）
     */
    getColor (p) {
      // P0：警报酒红（alarm-burgundy）- 最高优先级，红色系
      if (p === 0 || p === 'P0' || p === 'Critical' || p === 'Critical') {
        return this.getThemeColor('--alarm-burgundy') || '#802520'
      }
      // P1：任务铁锈红（mission-rust）- 高优先级，橙色系
      if (p === 1 || p === 'P1' || p === 'High') {
        return this.getThemeColor('--mission-rust') || '#B2653B'
      }
      // P2：航空芥末黄（avionics-mustard）- 中优先级，黄色系
      if (p === 2 || p === 'P2' || p === 'Medium') {
        return this.getThemeColor('--avionics-mustard') || '#BA8530'
      }
      // P3：氧化鼠尾草绿（oxidized-sage）- 低优先级，绿色系
      if (p === 3 || p === 'P3' || p === 'Low') {
        return this.getThemeColor('--oxidized-sage') || '#5C7F71'
      }
      // P4：深空黑（space-black）- 最低优先级/无优先级，深色
      if (p === 4 || p === 'P4' || p === 'None' || p === 'None') {
        return this.getThemeColor('--space-black') || '#181818'
      }
      // 默认：深空黑（space-black）
      return this.getThemeColor('--space-black') || '#181818'
    },
    
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
@import '@/assets/css/data-panel/priority-dist-chart.css';
</style>
