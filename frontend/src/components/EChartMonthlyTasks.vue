<template>
  <div class="chart-card">
    <div ref="chart" class="echart-canvas"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'EChartMonthlyTasks',
  props: {
    title: {
      type: String,
      default: '本月每日任务数'
    },
    labels: {
      type: Array,
      default: () => []
    },
    values: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return { chart: null }
  },
  mounted () {
    this.initChart()
    window.addEventListener('resize', this.onResize)
  },
  beforeDestroy () {
    window.removeEventListener('resize', this.onResize)
    try { if (this.chart) this.chart.dispose() } catch (e) {}
    this.chart = null
  },
  watch: {
    labels () { this.render() },
    values () { this.render() }
  },
  methods: {
    onResize () {
      try { if (this.chart) this.chart.resize() } catch (e) {}
    },
    initChart () {
      try {
        if (!this.chart) this.chart = echarts.init(this.$refs.chart)
        this.render()
      } catch (e) {
        // ignore
      }
    },
    render () {
      try {
        if (!this.chart) return
        const option = {
          title: { text: this.title, left: 'center', textStyle: { fontSize: 13 } },
          tooltip: { trigger: 'axis' },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: { type: 'category', data: this.labels, axisTick: { alignWithLabel: true } },
          yAxis: { type: 'value', minInterval: 1 },
          series: [
            {
              name: '任务数',
              type: 'bar',
              data: this.values,
              itemStyle: { color: '#409EFF' }
            }
          ]
        }
        this.chart.setOption(option)
      } catch (e) {
        // ignore
      }
    }
  }
}
</script>

<style scoped>
.chart-card { padding: 6px 0; }
.echart-canvas { width: 100%; height: 220px; }
</style>