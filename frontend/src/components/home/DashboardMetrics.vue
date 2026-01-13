<template>
  <!-- 指标容器：包含头部和指标卡片列表 -->
  <div class="dashboard-metrics-container" style="padding: 16px;">
     <!-- 头部栏：包含标题和配置按钮 -->
     <div class="dashboard-metrics-header-bar">
        <div class="dashboard-metrics-title-group">
            <!-- 装饰线：三个彩色装饰块和横线 -->
            <div class="dashboard-metrics-deco-line">
                <div class="dashboard-metrics-deco-block dashboard-metrics-block-mustard"></div>
                <div class="dashboard-metrics-deco-block dashboard-metrics-block-sage"></div>
                <div class="dashboard-metrics-deco-block dashboard-metrics-block-burgundy"></div>
                <div class="dashboard-metrics-title">KEY METRICS / 关键指标</div>
                <div class="dashboard-metrics-deco-strip"></div>
            </div>
        </div>
        <!-- 配置触发器：点击打开配置对话框 -->
        <div class="dashboard-metrics-config-trigger" @click="openConfig">
           <i class="el-icon-setting"></i> CONFIGURE
        </div>
     </div>
     
     <!-- 指标卡片行：使用 Element UI 的栅格系统 -->
     <el-row :gutter="16">
        <!-- 遍历显示每个指标卡片 -->
        <el-col :span="6" v-for="(mKey, idx) in metricKeys" :key="idx">
           <div class="dashboard-metrics-mission-card">
              <!-- 卡片左侧：标题、标签和数值 -->
              <div class="dashboard-metrics-card-left">
                  <!-- 英文标题 -->
                  <div class="dashboard-metrics-title-eng">{{ getDef(mKey).title }}</div>
                  
                  <!-- 信息组：中文标签和装饰点 -->
                  <div class="dashboard-metrics-info-group">
                      <span class="dashboard-metrics-label-cn" style="font-family: var(--font-family) !important;">{{ getDef(mKey).tag }}</span>
                      <!-- 装饰点：三个小圆点 -->
                      <div class="dashboard-metrics-deco-dots">
                          <span></span><span></span><span></span>
                      </div>
                  </div>

                  <!-- 英文标题反射效果 -->
                  <!-- <div class="dashboard-metrics-title-eng-reflect">{{ getDef(mKey).title }}</div> -->
                  
                  <!-- 数值显示：主要数值和单位 -->
                  <div class="dashboard-metrics-value-display">
                      <span class="dashboard-metrics-val-num">{{ displayValue(mKey) }}</span>
                      <span class="dashboard-metrics-val-unit">{{ getDef(mKey).unit }}</span>
                  </div>
              </div>
              
              <!-- 卡片右侧：装饰图案 -->
              <div class="dashboard-metrics-card-right">
                  <!-- 图案 0：同心波纹 -->
                  <div v-if="idx % 4 === 0" class="dashboard-metrics-pattern-ripples">
                      <div class="dashboard-metrics-ripple dashboard-metrics-r1"></div>
                      <div class="dashboard-metrics-ripple dashboard-metrics-r2"></div>
                      <div class="dashboard-metrics-ripple dashboard-metrics-r3"></div>
                      <div class="dashboard-metrics-ripple-sector dashboard-metrics-s1"></div>
                      <div class="dashboard-metrics-ripple-sector dashboard-metrics-s2"></div>
                  </div>
                  <!-- 图案 1：六边形网格 -->
                  <div v-if="idx % 4 === 1" class="dashboard-metrics-pattern-hex">
                      <div class="dashboard-metrics-hex-row" v-for="r in 3" :key="r">
                          <div class="dashboard-metrics-hex" v-for="c in 3" :key="c"></div>
                      </div>
                  </div>
                  <!-- 图案 2：雷达扫描 -->
                  <div v-if="idx % 4 === 2" class="dashboard-metrics-pattern-radar">
                      <div class="dashboard-metrics-radar-circle"></div>
                      <div class="dashboard-metrics-radar-sweep"></div>
                  </div>
                  <!-- 图案 3：均衡器 -->
                  <div v-if="idx % 4 === 3" class="dashboard-metrics-pattern-eq">
                      <div class="dashboard-metrics-eq-bar" v-for="b in 5" :key="b"></div>
                  </div>
              </div>

              <!-- 底部装饰条：三色装饰条 -->
              <div class="dashboard-metrics-bottom-bar">
                  <div class="dashboard-metrics-bar-seg dashboard-metrics-seg-mustard"></div>
                  <div class="dashboard-metrics-bar-seg dashboard-metrics-seg-sage"></div>
                  <div class="dashboard-metrics-bar-seg dashboard-metrics-seg-burgundy"></div>
              </div>
           </div>
        </el-col>
     </el-row>

     <!-- 配置对话框：用于配置指标类型 -->
     <el-dialog 
       :visible.sync="configVisible" 
       width="600px" 
       custom-class="home-retro-dialog"
       :append-to-body="true"
     >
        <span slot="title" class="dialog-title">METRICS CONFIGURATION // 指标配置</span>
        <div class="home-config-container">
           <div class="home-config-grid">
              <!-- 遍历4个指标插槽 -->
              <div v-for="i in 4" :key="'m'+i" class="home-config-item">
                 <span class="home-label">SLOT {{ i }}</span>
                 <!-- 指标类型选择器 -->
                 <el-select v-model="tempKeys[i-1]" size="small" class="home-retro-select" popper-class="retro-dropdown">
                    <el-option v-for="opt in metricOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                 </el-select>
                 
                 <!-- 预览示例：缩小的指标卡片预览 -->
                 <div class="home-demo-box" style="height: 130px;">
                    <div class="home-demo-label">PREVIEW</div>
                    <!-- 缩小的指标卡片预览 -->
                    <div class="dashboard-metrics-mission-card" style="height: 140px; transform: scale(0.65); transform-origin: top left; width: 154%;">
                       <div class="dashboard-metrics-card-left">
                           <div class="dashboard-metrics-title-eng">{{ getDef(tempKeys[i-1]).title }}</div>
                           
                           <div class="dashboard-metrics-info-group">
                               <span class="dashboard-metrics-label-cn">{{ getDef(tempKeys[i-1]).tag }}</span>
                               <div class="dashboard-metrics-deco-dots">
                                   <span></span><span></span><span></span>
                               </div>
                           </div>
                           
                           <div class="dashboard-metrics-title-eng-reflect">{{ getDef(tempKeys[i-1]).title }}</div>
                           
                           <div class="dashboard-metrics-value-display">
                               <span class="dashboard-metrics-val-num">88</span>
                               <span class="dashboard-metrics-val-unit">{{ getDef(tempKeys[i-1]).unit }}</span>
                           </div>
                       </div>
                       
                       <div class="dashboard-metrics-card-right">
                           <!-- 预览图案 0：同心波纹 -->
                           <div v-if="(i-1) % 4 === 0" class="dashboard-metrics-pattern-ripples">
                               <div class="dashboard-metrics-ripple dashboard-metrics-r1"></div>
                               <div class="dashboard-metrics-ripple dashboard-metrics-r2"></div>
                               <div class="dashboard-metrics-ripple dashboard-metrics-r3"></div>
                               <div class="dashboard-metrics-ripple-sector dashboard-metrics-s1"></div>
                               <div class="dashboard-metrics-ripple-sector dashboard-metrics-s2"></div>
                           </div>
                           <!-- 预览图案 1：六边形网格 -->
                           <div v-if="(i-1) % 4 === 1" class="dashboard-metrics-pattern-hex">
                               <div class="dashboard-metrics-hex-row" v-for="r in 3" :key="r">
                                   <div class="dashboard-metrics-hex" v-for="c in 3" :key="c"></div>
                               </div>
                           </div>
                           <!-- 预览图案 2：雷达扫描 -->
                           <div v-if="(i-1) % 4 === 2" class="dashboard-metrics-pattern-radar">
                               <div class="dashboard-metrics-radar-circle"></div>
                               <div class="dashboard-metrics-radar-sweep"></div>
                           </div>
                           <!-- 预览图案 3：均衡器 -->
                           <div v-if="(i-1) % 4 === 3" class="dashboard-metrics-pattern-eq">
                               <div class="dashboard-metrics-eq-bar" v-for="b in 5" :key="b"></div>
                           </div>
                       </div>

                       <div class="dashboard-metrics-bottom-bar">
                           <div class="dashboard-metrics-bar-seg dashboard-metrics-seg-mustard"></div>
                           <div class="dashboard-metrics-bar-seg dashboard-metrics-seg-sage"></div>
                           <div class="dashboard-metrics-bar-seg dashboard-metrics-seg-burgundy"></div>
                       </div>
                    </div>
                 </div>
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
/**
 * 指标定义常量
 * 包含所有可用指标的类型、标题、标签、描述、颜色和单位等信息
 */
const METRIC_DEFS = {
  // 团队数量指标
  teamCount: {
    title: 'TEAMS',
    tag: '团队',
    desc: '加入的团队总数',
    label: 'COUNT:',
    color: '#6C5CE7',
    unit: 'TEAMS'
  },
  // 任务组数量指标
  groupCount: {
    title: 'TASK GROUPS',
    tag: '任务族',
    desc: '接入的业务单元总数',
    label: 'COUNT:',
    color: '#6C5CE7',
    unit: 'GROUPS'
  },
  // 进行中任务数量指标
  inProgressCount: {
    title: 'IN PROGRESS',
    tag: '进行中',
    desc: '正在执行的任务序列',
    label: 'ACTIVE:',
    color: '#1976D2',
    unit: 'TASKS'
  },
  // 逾期任务数量指标
  overdueCount: {
    title: 'OVERDUE',
    tag: '逾期',
    desc: '警告：时间窗口溢出',
    label: 'ALERT:',
    color: '#D32F2F',
    titleColor: '#D32F2F',
    tagClass: 'tag-danger',
    unit: 'TASKS'
  },
  // 本周完成任务数量指标
  completedWeek: {
    title: 'DONE (WEEK)',
    tag: '本周完成',
    desc: '本周归档任务数',
    label: 'DONE:',
    color: '#67C23A',
    tagClass: 'tag-success',
    unit: 'TASKS'
  },
  // 本月完成任务数量指标
  completedMonth: {
    title: 'DONE (MONTH)',
    tag: '本月完成',
    desc: '本月归档任务数',
    label: 'DONE:',
    color: '#67C23A',
    tagClass: 'tag-success',
    unit: 'TASKS'
  },
  // 累计完成任务数量指标
  completedTotal: {
    title: 'DONE (TOTAL)',
    tag: '累计完成',
    desc: '历史累计归档任务',
    label: 'TOTAL:',
    color: '#67C23A',
    tagClass: 'tag-success',
    unit: 'TASKS'
  }
}

/**
 * 仪表板指标组件
 * 
 * 功能：
 * 1. 显示4个可配置的指标卡片
 * 2. 每个卡片显示不同的装饰图案（4种图案循环）
 * 3. 支持通过配置对话框自定义指标类型
 * 4. 提供指标卡片的预览功能
 */
export default {
  name: 'DashboardMetrics',
  props: {
    // 指标键数组（要显示的指标类型）
    metricKeys: {
      type: Array,
      default: () => []
    },
    // 指标值映射对象（key -> value）
    metricValues: {
      type: Object,
      default: () => ({})
    },
    // 是否为演示模式（显示随机值）
    demo: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      // 配置对话框是否可见
      configVisible: false,
      // 临时存储的指标键数组（用于配置对话框）
      tempKeys: []
    }
  },
  computed: {
    /**
     * 指标选项列表
     * 用于配置对话框的下拉选择器
     * @returns {Array<{label: string, value: string}>} 指标选项数组
     */
    metricOptions () {
      return Object.keys(METRIC_DEFS).map(k => ({
        label: METRIC_DEFS[k].tag,
        value: k
      }))
    }
  },
  methods: {
    /**
     * 获取指标定义
     * 根据指标键获取对应的定义对象，如果不存在则返回默认值
     * @param {string} key - 指标键
     * @returns {Object} 指标定义对象
     */
    getDef (key) {
      return METRIC_DEFS[key] || {
        title: 'UNKNOWN',
        label: '??',
        color: '#999'
      }
    },
    
    /**
     * 显示指标值
     * 如果是演示模式则返回随机值，否则返回实际的指标值
     * @param {string} key - 指标键
     * @returns {number|string} 指标值
     */
    displayValue (key) {
      // 演示模式：返回随机值（0-99）
      if (this.demo) {
        return Math.floor(Math.random() * 100)
      }
      // 正常模式：返回实际值，如果不存在则返回0
      return this.metricValues[key] || 0
    },
    
    /**
     * 打开配置对话框
     * 初始化临时指标键数组并显示配置对话框
     */
    openConfig () {
      // 复制当前的指标键数组
      this.tempKeys = [...this.metricKeys]
      // 确保有4个插槽（如果不足则用第一个指标类型填充）
      while (this.tempKeys.length < 4) {
        this.tempKeys.push(Object.keys(METRIC_DEFS)[0])
      }
      // 显示配置对话框
      this.configVisible = true
    },
    
    /**
     * 保存配置
     * 将临时指标键数组发送给父组件并关闭配置对话框
     */
    saveConfig () {
      // 触发事件，将新的指标键数组发送给父组件
      this.$emit('update:keys', this.tempKeys)
      // 关闭配置对话框
      this.configVisible = false
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/home/dashboard-metrics.css';
</style>
