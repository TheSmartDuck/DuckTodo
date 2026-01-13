<template>
  <!-- 左侧日程日历容器 -->
  <div class="left-schedule-calendar-pane">
    <!-- 列表头部：包含标题和工具栏 -->
    <div class="left-schedule-calendar-header" ref="listHeader">
      <!-- 标题区域 -->
      <div class="left-schedule-calendar-header-title-row">
        <div class="left-schedule-calendar-title-text-group">
          <div class="left-schedule-calendar-title-en">SCHEDULE</div>
          <div class="left-schedule-calendar-title-cn">日程概览</div>
        </div>
        <div class="left-schedule-calendar-header-decor-line">
          <span class="left-schedule-calendar-decor-text">PIONEER PROJECT // </span>
          <span class="left-schedule-calendar-decor-highlight">PLANNING</span>
          <div class="left-schedule-calendar-decor-shapes">
            <span class="left-schedule-calendar-shape-circle"></span>
            <span class="left-schedule-calendar-shape-rect"></span>
          </div>
        </div>
      </div>

      <!-- 工具栏：年份和月份选择器 -->
      <div class="left-schedule-calendar-toolbar">
        <!-- 年份选择器 -->
        <div class="left-schedule-calendar-year-wrap">
          <span class="left-schedule-calendar-year-unit">YEAR</span>
          <el-input-number 
            v-model="calendarYear" 
            :step="1" 
            controls-position="right" 
            size="mini" 
            class="left-schedule-calendar-retro-input-number" 
            @change="onCalendarYearMonthChange"
          />
        </div>

        <!-- 月份选择器 -->
        <div class="left-schedule-calendar-month-wrap" style="margin-left:8px;">
          <span class="left-schedule-calendar-month-unit">MONTH</span>
          <el-input-number 
            v-model="calendarMonth" 
            :step="1" 
            controls-position="right" 
            size="mini" 
            class="left-schedule-calendar-retro-input-number" 
            @change="onCalendarYearMonthChange" 
          />
        </div>
      </div>
    </div>

    <!-- 日历容器 -->
    <div class="left-schedule-calendar-wrap" ref="tableWrap">
      <div class="left-schedule-calendar-container" v-loading="loading" element-loading-background="rgba(255, 255, 255, 0.5)" element-loading-text="LOADING..." element-loading-spinner="el-icon-loading">
        <!-- 星期标题行 -->
        <div class="left-schedule-calendar-week-header">
          <div class="left-schedule-calendar-weekday" v-for="w in weekHeaders" :key="w">{{ w }}</div>
        </div>
        <!-- 日历主体：6行7列的网格 -->
        <div class="left-schedule-calendar-body">
          <!-- 日历格子：每个格子代表一天 -->
          <div
            class="left-schedule-calendar-cell"
            v-for="cell in calendarCells"
            :key="cell.key"
            :class="{ 'left-schedule-calendar-is-other-month': cell.isOtherMonth, 'left-schedule-calendar-is-today': cell.isToday }"
          >
            <!-- 格子头部：显示日期 -->
            <div class="left-schedule-calendar-cell-header">
              <span class="left-schedule-calendar-gregorian">{{ cell.day }}</span>
              <!-- 今天的标签 -->
              <div v-if="cell.isToday" class="left-schedule-calendar-today-tag">TODAY</div>
            </div>
            <!-- 格子任务列表：显示该日期的所有任务 -->
            <div class="left-schedule-calendar-cell-tasks">
              <div
                class="left-schedule-calendar-task-item"
                v-for="t in cell.tasks"
                :key="String(t.taskId || t.taskID || t.id || Math.random())"
                :title="t.taskName"
                :class="[getTaskPriorityClass(t), getTaskStatusClass(t), { 'left-schedule-calendar-is-selected': isTaskSelected(t) }]"
                @click.stop="onRowClick(t)"
              >
                <span class="left-schedule-calendar-task-name">{{ t.taskName || 'UNTITLED' }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表底部：显示统计信息 -->
    <div class="left-schedule-calendar-footer" ref="listFooter">
      <div class="left-schedule-calendar-footer-left">
        <span>FILTER:</span> {{ filteredTasks.length }} TASKS IN VIEW
      </div>
      <div class="left-schedule-calendar-footer-right">
        <!-- 可选控件 -->
      </div>
    </div>
  </div>
</template>

<script>
import { getScheduleTaskListByUserId } from '@/api/task-api'
import { getMyTaskGroupList } from '@/api/taskgroup-api'
import { compareDateStrings } from '@/utils/validation'

/**
 * 左侧日程日历组件
 * 
 * 功能：
 * 1. 显示月视图日历，每个格子显示当天的任务
 * 2. 支持年份和月份选择
 * 3. 任务按日期分组显示
 * 4. 支持任务选择事件
 * 5. 任务按状态和优先级排序
 */
export default {
  name: 'LeftScheduleCalendar',
  props: {
    // 当前选中的任务ID
    selectedTaskId: { type: String, default: null }
  },
  data () {
    return {
      // 加载状态
      loading: false,
      // 任务列表数据
      tasks: [],
      // 我的任务组列表（用于过滤，当前未使用）
      myTaskGroups: [],
      
      // 当前选中的年份
      calendarYear: new Date().getFullYear(),
      // 当前选中的月份（1-12）
      calendarMonth: new Date().getMonth() + 1,
      // 选中的任务组ID列表（用于过滤，当前未使用）
      selectedGroupIds: [],
      
      // 星期标题数组
      weekHeaders: ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']
    }
  },
  computed: {
    /**
     * 过滤后的任务列表
     * 返回当前月份的所有任务（扁平列表）
     * @returns {Array} 任务数组
     */
    filteredTasks () {
      // 任务已经按月份获取，直接返回
      return this.tasks
    },
    
    /**
     * 按日期分组的任务映射
     * 将任务列表按截止日期分组，只包含当前月份的任务
     * 任务按状态、优先级、名称排序
     * @returns {Object} 日期到任务数组的映射对象，key为日期字符串（YYYY-MM-DD）
     */
    tasksByDate () {
      const map = {}
      const y = Number(this.calendarYear)
      const m = Number(this.calendarMonth)
      const list = Array.isArray(this.tasks) ? this.tasks : []
      
      // 遍历任务，按日期分组
      list.forEach(t => {
        // 按选中的任务组过滤（如果已选择）
        const gids = Array.isArray(this.selectedGroupIds) ? this.selectedGroupIds.map(x => String(x)) : []
        const tgId = String(t && (t.taskGroupId || t.taskGroupID || t.task_group_id || ''))
        if (gids.length && (!tgId || !gids.includes(tgId))) return
        
        // 提取任务的截止时间
        const ts = t && t.dueTime
        if (!ts) return
        
        try {
          // 解析日期，只处理当前月份的任务
          const d = new Date(ts)
          const yy = d.getFullYear()
          const mm = d.getMonth() + 1
          if (yy !== y || mm !== m) return
          
          // 格式化日期作为key
          const key = this.formatTime(ts)
          if (!key) return
          if (!map[key]) map[key] = []
          map[key].push(t)
        } catch (e) {
          // 日期解析失败，跳过该任务
        }
      })
      
      // 对每个日期的任务进行排序：状态 > 优先级 > 名称
      const prRank = { high: 3, medium: 2, low: 1, none: 0 }
      const statusRank = (s) => {
        const v = String(s || '')
        if (v === '2') return 0 // 进行中 - 最高优先级
        if (v === '1') return 1 // 未开始
        if (v === '3') return 2 // 已完成
        return 3 // 其他状态
      }
      
      // 对每个日期的任务数组进行排序
      Object.keys(map).forEach(k => {
        const arr = Array.isArray(map[k]) ? map[k] : []
        arr.sort((a, b) => {
          // 首先按状态排序
          const sa = statusRank(a && (a.taskStatus ?? ''))
          const sb = statusRank(b && (b.taskStatus ?? ''))
          if (sa !== sb) return sa - sb
          
          // 然后按优先级排序（优先级高的在前）
          const paKey = this.serverValueToPriorityKey(a && (a.taskPriority ?? 0))
          const pbKey = this.serverValueToPriorityKey(b && (b.taskPriority ?? 0))
          const pa = prRank[paKey] ?? 0
          const pb = prRank[pbKey] ?? 0
          if (pa !== pb) return pb - pa
          
          // 最后按名称排序（字母顺序）
          const na = String(a && a.taskName || '')
          const nb = String(b && b.taskName || '')
          return na.localeCompare(nb)
        })
        map[k] = arr
      })
      return map
    },
    
    /**
     * 日历格子数组
     * 生成42个格子（6行×7列），包含当前月份及前后月份的日期
     * @returns {Array} 日历格子对象数组，每个对象包含日期信息和任务列表
     */
    calendarCells () {
      const y = Number(this.calendarYear)
      const m = Number(this.calendarMonth)
      // 计算当前月份第一天
      const first = new Date(y, m - 1, 1)
      // 计算第一天是星期几（0=周日，转换为0=周一）
      const weekdayMonStart = (first.getDay() + 6) % 7 // 0-6, Mon=0
      // 计算日历开始日期（包含上个月的日期）
      const startDate = new Date(y, m - 1, 1 - weekdayMonStart)
      const todayStr = this.formatTime(new Date())
      
      const cells = []
      // 生成42个格子（6周×7天）
      for (let i = 0; i < 42; i++) {
        const d = new Date(startDate)
        d.setDate(startDate.getDate() + i)
        const ds = this.formatTime(d)
        cells.push({
          key: ds,
          dateStr: ds,
          day: d.getDate(),
          // 是否为其他月份的日期
          isOtherMonth: (d.getMonth() + 1) !== m,
          // 是否为今天
          isToday: ds === todayStr,
          // 该日期的任务列表
          tasks: Array.isArray(this.tasksByDate[ds]) ? this.tasksByDate[ds] : []
        })
      }
      return cells
    }
  },
  /**
   * 组件创建时加载任务组和任务列表
   */
  created () {
    this.loadGroup()
    this.loadTasks()
  },
  methods: {
    /**
     * ========== 格式化方法 ==========
     */
    
    /**
     * 格式化时间
     * 将日期对象或日期字符串格式化为 YYYY-MM-DD 格式
     * @param {Date|string} val - 日期对象或日期字符串
     * @returns {string} 格式化后的日期字符串（YYYY-MM-DD），失败时返回空字符串
     */
    formatTime (val) {
      if (!val) return ''
      try {
        const d = (val instanceof Date) ? val : new Date(val)
        const y = d.getFullYear()
        const m = String(d.getMonth() + 1).padStart(2, '0')
        const dd = String(d.getDate()).padStart(2, '0')
        return `${y}-${m}-${dd}`
      } catch (e) { 
        return '' 
      }
    },
    
    /**
     * ========== 优先级相关方法 ==========
     */
    
    /**
     * 将服务器优先级值转换为优先级键
     * 将数字优先级（0-4）转换为字符串键（high/medium/low/none）
     * @param {number} val - 服务器优先级值（0-4）
     * @returns {string} 优先级键（high/medium/low/none）
     */
    serverValueToPriorityKey (val) {
      const v = Number(val)
      if (v === 0) return 'high' // P0 - 高优先级
      if (v === 1) return 'high' // P1 - 高优先级
      if (v === 2) return 'medium' // P2 - 中优先级
      if (v === 3) return 'low' // P3 - 低优先级
      return 'none' // P4/默认 - 无优先级
    },
    
    /**
     * 获取任务优先级类名
     * 根据任务优先级值返回对应的CSS类名
     * @param {Object} t - 任务对象
     * @returns {string} 优先级类名（p-high/p-medium/p-low/p-none）
     */
    getTaskPriorityClass (t) {
      const p = Number(t.taskPriority)
      if (p === 0 || p === 1) return 'p-high'
      if (p === 2) return 'p-medium'
      if (p === 3) return 'p-low'
      return 'p-none'
    },
    
    /**
     * ========== 状态相关方法 ==========
     */
    
    /**
     * 获取任务状态类名
     * 根据任务状态和截止时间判断状态类名
     * @param {Object} t - 任务对象
     * @returns {string} 状态类名（s-completed/s-timeout/空字符串）
     */
    getTaskStatusClass (t) {
      const s = String(t.taskStatus)
      // 已完成状态
      if (s === '3') return 's-completed'
      
      // 检查是否超期（未开始或进行中且已过截止日期）
      if ((s === '1' || s === '2') && t.dueTime) {
        const now = this.formatTime(new Date())
        if (compareDateStrings(t.dueTime, now) < 0) return 's-timeout'
      }
      return ''
    },
    
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载任务组列表
     * 从API获取当前用户的任务组列表（用于过滤，当前未使用）
     */
    async loadGroup () {
      try {
        const res = await getMyTaskGroupList()
        let list = []
        // 规范化API响应数据
        if (Array.isArray(res)) list = res
        else if (res && Array.isArray(res.list)) list = res.list
        else if (res && Array.isArray(res.data)) list = res.data
        this.myTaskGroups = list
      } catch (e) {
        this.myTaskGroups = []
      }
    },
    
    /**
     * 加载任务列表
     * 根据当前选中的年份和月份从API获取任务列表
     */
    async loadTasks () {
      this.loading = true
      this.tasks = []
      try {
        // 构建请求参数：年份和月份
        const params = {
          year: this.calendarYear,
          month: this.calendarMonth
        }
        
        // 调用API获取指定月份的任务列表
        const res = await getScheduleTaskListByUserId(params)
        let list = []
        
        // 规范化API响应数据
        if (Array.isArray(res)) list = res
        else if (res && Array.isArray(res.data)) list = res.data
        else if (res && Array.isArray(res.list)) list = res.list
        
        this.tasks = list
      } catch (e) {
        console.error('Load tasks failed', e)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * ========== 事件处理方法 ==========
     */
    
    /**
     * 处理年份/月份变化事件
     * 当年份或月份改变时，自动调整边界值并重新加载任务
     */
    onCalendarYearMonthChange () {
      // 处理月份溢出：超过12月则进入下一年1月
      if (this.calendarMonth > 12) {
        this.calendarMonth = 1
        this.calendarYear++
      } 
      // 处理月份下溢：小于1月则进入上一年12月
      else if (this.calendarMonth < 1) {
        this.calendarMonth = 12
        this.calendarYear--
      }
      // 重新加载任务列表
      this.loadTasks()
    },
    
    /**
     * 处理任务组过滤变化事件（当前未使用）
     * 当任务组过滤条件改变时，重新计算任务分组
     */
    onGroupFilterChange () {
      // 仅重新计算tasksByDate，不需要重新加载数据
    },
    
    /**
     * 处理任务行点击事件
     * 当用户点击日历格子中的任务时触发select事件
     * @param {Object} task - 被点击的任务对象
     */
    onRowClick (task) {
      this.$emit('select', task)
    },
    
    /**
     * ========== 判断方法 ==========
     */
    
    /**
     * 判断任务是否被选中
     * 比较任务ID与当前选中的任务ID
     * @param {Object} t - 任务对象
     * @returns {boolean} 是否被选中
     */
    isTaskSelected (t) {
      if (!t || !this.selectedTaskId) return false
      return String(t.taskId) === String(this.selectedTaskId)
    }
  }
}
</script>

<style scoped>
  @import "@/assets/css/schedule-overview/left-schedule-calendar.css";
</style>
