<template>
  <!-- 左侧最近待办列表容器 -->
  <div class="left-recent-list-pane">
    <!-- 列表头部：包含标题和搜索栏 -->
    <div class="left-recent-list-header" ref="listHeader">
      <!-- 标题区域 -->
      <div class="left-recent-list-header-title-row">
        <div class="left-recent-list-title-text-group">
          <div class="left-recent-list-title-en">RECENT</div>
          <div class="left-recent-list-title-cn">最近待办</div>
        </div>
        <div class="left-recent-list-header-decor-line">
          <span class="left-recent-list-decor-text">PIONEER PROJECT // </span>
          <span class="left-recent-list-decor-highlight">EXECUTE</span>
          <div class="left-recent-list-decor-shapes">
            <span class="left-recent-list-shape-circle"></span>
            <span class="left-recent-list-shape-rect"></span>
          </div>
        </div>
      </div>

      <!-- 工具栏：搜索框 -->
      <div class="left-recent-list-toolbar">
        <span style="color: var(--brand-primary);font-weight: 800;font-size: 12px;">&nbsp;>>>&nbsp;</span>
        
        <!-- 搜索框 -->
        <div class="left-recent-list-search-box">
          <i class="el-icon-search left-recent-list-search-icon"></i>
          <input 
            v-model="keyword" 
            class="left-recent-list-search-input" 
            placeholder="INPUT_TASK_NAME..." 
            @keyup.enter="onSearch"
          />
          <div class="left-recent-list-search-corner"></div>
        </div>
      </div>
    </div>

    <!-- 任务列表容器 -->
    <div class="left-recent-list-task-list-wrap" ref="tableWrap" v-loading="loading" element-loading-background="rgba(255, 255, 255, 0.5)" element-loading-text="LOADING..." element-loading-spinner="el-icon-loading">
      <div class="left-recent-list-task-list" :style="{ height: listTableHeight }" ref="taskList" @scroll="onListScroll">
        <!-- 任务行包装器：遍历显示每个任务 -->
        <div class="left-recent-list-task-row-wrapper" v-for="row in tasks" :key="String(row.taskId || row.taskID || row.id || Math.random())">
          
          <!-- 任务条：Rhine Lab风格的任务卡片 -->
          <div 
            class="left-recent-list-task-strip left-recent-list-main-task-strip" 
            :class="{ 'left-recent-list-is-selected': isRowSelected(row), 'left-recent-list-is-timeout': isTaskOverdue(row) }" 
            @click="onRowClick(row)"
          >
            <!-- 左侧三色条：装饰性彩色条 -->
            <div class="left-recent-list-strip-left-tri">
              <div class="left-recent-list-tri-bar left-recent-list-bar-mustard"></div>
              <div class="left-recent-list-tri-bar left-recent-list-bar-sage"></div>
              <div class="left-recent-list-tri-bar left-recent-list-bar-burgundy"></div>
            </div>

            <!-- 任务条主体内容 -->
            <div class="left-recent-list-strip-body left-recent-list-main-body">
              <!-- 第一行：元信息（日期、状态、ID） -->
              <div class="left-recent-list-strip-row-meta">
                 <div class="left-recent-list-meta-left">
                    <!-- 任务日期显示 -->
                    <div class="left-recent-list-task-date" :class="{ 'left-recent-list-is-overdue': isTaskOverdue(row) }">
                       <span class="left-recent-list-date-text">{{ row.dueTime ? formatTime(row.dueTime) : 'PENDING' }}</span>
                       <div class="left-recent-list-date-icon-dots">
                          <span></span><span></span><span></span>
                       </div>
                    </div>
                    <!-- 状态标签 -->
                    <div class="left-recent-list-status-pill" :class="getTaskStatusClass(row)">
                       {{ getTaskStatusText(row) }}
                    </div>

                 </div>
                 <!-- 右侧：优先级标识 -->
                 <div class="left-recent-list-meta-right">
                    <div class="left-recent-list-priority-logo left-recent-list-mini">
                        <div class="left-recent-list-logo-half left-recent-list-left"><div class="left-recent-list-logo-circle">P</div></div>
                        <div class="left-recent-list-logo-half left-recent-list-right" :style="{ backgroundColor: getPriorityColor(row.taskPriority) }">
                           <div class="left-recent-list-logo-circle">{{ getPriorityNumber(row.taskPriority) }}</div>
                        </div>
                    </div>
                 </div>
              </div>

              <!-- 第二行：任务标题和子任务指示器 -->
              <div class="left-recent-list-strip-row-main">
                 <!-- 任务名称 -->
                 <div class="left-recent-list-task-name-text" :class="taskTitleStatusClass(row.taskStatus, row.dueTime)">
                    {{ row.taskName || 'UNTITLED_TASK' }}
                 </div>
                 <!-- 子任务展开指示器 -->
                 <div class="left-recent-list-subtask-indicator" @click.stop="toggleSubtaskExpand(row)">
                    SUBTASKS [{{ getChildren(row).length }}]
                    <i class="el-icon-arrow-right" :class="{ 'left-recent-list-is-expanded': activeCollapseNames.includes(resolveTaskId(row)) }"></i>
                 </div>
              </div>
            </div>
          </div>

          <!-- 可折叠的子任务面板 -->
          <el-collapse-transition>
            <div v-show="activeCollapseNames.includes(resolveTaskId(row))" class="left-recent-list-subtask-collapse-panel">
               <!-- 子任务列表 -->
               <div v-if="getChildren(row).length" class="left-recent-list-subtask-list-mini">
                  <div 
                    v-for="(sub, sIdx) in getChildren(row)" 
                    :key="sub.id || sIdx" 
                    class="left-recent-list-subtask-timeline-row"
                  >
                     <!-- 时间线列：左侧时间线装饰 -->
                     <div class="left-recent-list-timeline-col">
                        <div class="left-recent-list-timeline-line"></div>
                        <div class="left-recent-list-timeline-node" :class="getChildNodeClass(sub)">
                           <div class="left-recent-list-ripple-ring" v-if="isChildActive(sub)"></div>
                        </div>
                     </div>
                     <!-- 子任务条 -->
                     <div 
                       class="left-recent-list-task-strip left-recent-list-sub-strip"
                       :class="{ 'left-recent-list-is-parent-timeout': isTaskOverdue(row), 'left-recent-list-is-active-sub': isChildActive(sub) }"
                     >
                       <div class="left-recent-list-strip-body left-recent-list-sub-body">
                          <!-- 单行布局：状态、名称、日期 -->
                          <div class="left-recent-list-sub-status-box">
                             <div class="left-recent-list-status-pill left-recent-list-mini" :class="getChildTaskStatusClass(sub)">
                                {{ getChildTaskStatusText(sub) }}
                             </div>
                          </div>
                          <div class="left-recent-list-sub-name-text" :class="getChildTaskStatusClass(sub)">{{ sub.name }}</div>
                          <div class="left-recent-list-sub-date" v-if="sub.dueDate">{{ formatTime(sub.dueDate) }}</div>
                       </div>
                     </div>
                  </div>
               </div>
               <!-- 无子任务提示 -->
               <div v-else class="left-recent-list-no-subtasks-mini">NO SUBTASKS RECORDED</div>
            </div>
          </el-collapse-transition>

        </div>
        
        <!-- 加载更多指示器 -->
        <div class="left-recent-list-loading-more" v-if="listLoadingMore">
          <i class="el-icon-loading" style="margin-right:6px"></i>
          LOADING MORE...
        </div>
        <!-- 列表结束标记 -->
        <div class="left-recent-list-end" v-if="!listHasMore && tasks.length">
          <!-- End of list marker -->
        </div>
      </div>
    </div>

    <!-- 列表底部：显示总数 -->
    <div class="left-recent-list-footer" ref="listFooter" style="background-color: var(--color-black);">
      <div class="left-recent-list-footer-left">
        <span>TOTAL:</span> {{ tasks.length }}
      </div>
      <div class="left-recent-list-footer-right">
        <!-- 设置按钮已移除 -->
      </div>
    </div>
  </div>
</template>

<script>
import { getTaskPageByUserId } from '@/api/task-api'
import defaultAvatar from '@/assets/imgs/default-user-avatar.png'
import { compareDateStrings } from '@/utils/validation'

/**
 * 任务状态字典
 * 映射任务状态码到中文描述
 */
const statusDict = {
  '0': '已取消',
  '1': '未开始',
  '2': '进行中',
  '3': '已完成',
  '4': '已延迟',
  '5': '暂停中'
}

/**
 * 优先级选项（未使用，保留用于未来扩展）
 */
const priorityOptions = [
  { label: 'High', value: 'high', color: '#F56C6C' },
  { label: 'Medium', value: 'medium', color: '#E6A23C' },
  { label: 'Low', value: 'low', color: '#409EFF' },
  { label: 'None', value: 'none', color: '#909399' }
]

/**
 * 格式化任务状态
 * 将状态码转换为中文描述
 * @param {string|number} val - 任务状态码
 * @returns {string} 状态中文描述
 */
function formatStatus (val) {
  const v = String(val)
  return statusDict[v] || '未知'
}

/**
 * 格式化时间
 * 将ISO时间字符串格式化为日期格式（YYYY-MM-DD）
 * @param {string} val - 时间字符串
 * @returns {string} 格式化后的日期字符串
 */
function formatTime (val) {
  if (!val) return ''
  // 如果是完整的ISO字符串，只取日期部分
  if (val.includes('T')) return val.split('T')[0]
  return val
}

/**
 * 左侧最近待办列表组件
 * 
 * 功能：
 * 1. 显示当前用户相关的任务列表
 * 2. 支持任务搜索和分页加载
 * 3. 显示任务详情（状态、优先级、日期等）
 * 4. 支持子任务的展开/折叠
 * 5. 处理任务选择事件
 */
export default {
  name: 'LeftRecentList',
  props: {
    // 当前选中的任务ID
    selectedTaskId: { type: String, default: null }
  },
  data () {
    return {
      defaultAvatar,
      // 任务列表数据
      tasks: [],
      // 加载状态
      loading: false,
      // 加载更多状态
      listLoadingMore: false,
      // 是否还有更多数据
      listHasMore: true,
      // 当前页码
      listPage: 1,
      // 每页大小
      listPageSize: 20,
      // 总记录数
      listTotal: 0,
      
      // 搜索关键词
      keyword: '',
      
      // 展开的子任务任务ID列表
      activeCollapseNames: [],
      // 子任务缓存
      childCache: {},
      
      // 状态字典和优先级选项
      statusDict,
      priorityOptions
    }
  },
  /**
   * 组件挂载时加载任务列表
   */
  mounted () {
    this.loadTasks()
  },
  computed: {
    /**
     * 列表表格高度
     * @returns {string} 高度值
     */
    listTableHeight () {
      return '100%'
    },
    /**
     * 优先级颜色列表
     * 将优先级选项转换为颜色映射对象
     * @returns {Object} 优先级颜色映射
     */
    priorityColorList () {
      return this.priorityOptions.reduce((acc, cur) => {
        acc[cur.value] = cur.color
        return acc
      }, {})
    }
  },
  methods: {
    /**
     * ========== 优先级相关方法 ==========
     */
    
    /**
     * 获取优先级颜色
     * 根据优先级值返回对应的主题色
     * @param {number|string} p - 优先级值（0-4）
     * @returns {string} 颜色值（十六进制）
     */
    getPriorityColor (p) {
      const val = Number(p)
      if (val === 0) return '#802520' // P0 警报酒红
      if (val === 1) return '#B2653B' // P1 任务铁锈红
      if (val === 2) return '#BA8530' // P2 航空芥末黄
      if (val === 3) return '#5C7F71' // P3 氧化鼠尾草绿
      return '#181818' // P4/默认 深空黑
    },
    
    /**
     * 获取优先级数字
     * 返回优先级的数字表示（0-4）
     * @param {number|string} p - 优先级值
     * @returns {string} 优先级数字字符串
     */
    getPriorityNumber (p) {
      const val = Number(p)
      return (val >= 0 && val <= 4) ? String(val) : '#'
    },

    /**
     * ========== 状态相关方法 ==========
     */
    
    /**
     * 获取任务状态类名
     * 根据任务状态和截止时间判断状态类名
     * @param {Object} row - 任务对象
     * @returns {string} 状态类名（disabled/completed/timeout/process）
     */
    getTaskStatusClass (row) {
      const status = Number(row.taskStatus)
      // 已取消或已延迟
      if (status === 0 || status === 4) return 'disabled'
      // 已完成
      if (status === 3) return 'completed'
      
      // 检查是否超期（未开始或进行中且已过截止日期）
      const due = row.dueTime
      if ((status === 1 || status === 2) && due) {
        const now = new Date()
        const y = now.getFullYear()
        const m = String(now.getMonth() + 1).padStart(2, '0')
        const d = String(now.getDate()).padStart(2, '0')
        const today = `${y}-${m}-${d}`
        if (compareDateStrings(due, today) < 0) return 'timeout'
      }
      return 'process'
    },
    
    /**
     * 获取任务状态文本
     * 根据状态类名返回对应的英文文本
     * @param {Object} row - 任务对象
     * @returns {string} 状态文本（Disabled/Completed/Timeout/Process）
     */
    getTaskStatusText (row) {
      const cls = this.getTaskStatusClass(row)
      if (cls === 'disabled') return 'Disabled'
      if (cls === 'completed') return 'Completed'
      if (cls === 'timeout') return 'Timeout'
      return 'Process'
    },

    /**
     * ========== 子任务状态相关方法 ==========
     */
    
    /**
     * 获取子任务状态类名
     * 根据子任务状态和截止日期判断状态类名
     * @param {Object} ct - 子任务对象
     * @returns {string} 状态类名（completed/timeout/空字符串）
     */
    getChildTaskStatusClass (ct) {
      const status = String(ct.status)
      // 已完成
      if (status === '3') return 'completed'
      
      // 检查是否超期
      if ((status === '1' || status === '2') && ct.dueDate) {
         const now = new Date()
         const y = now.getFullYear()
         const m = String(now.getMonth() + 1).padStart(2, '0')
         const d = String(now.getDate()).padStart(2, '0')
         const today = `${y}-${m}-${d}`
         if (compareDateStrings(ct.dueDate, today) < 0) return 'timeout'
      }
      return ''
    },
    
    /**
     * 获取子任务状态文本
     * 根据状态类名返回对应的英文文本
     * @param {Object} ct - 子任务对象
     * @returns {string} 状态文本（Completed/Timeout/Process）
     */
    getChildTaskStatusText (ct) {
      const cls = this.getChildTaskStatusClass(ct)
      if (cls === 'completed') return 'Completed'
      if (cls === 'timeout') return 'Timeout'
      return 'Process'
    },
    
    /**
     * 判断子任务是否处于活跃状态（进行中）
     * @param {Object} sub - 子任务对象
     * @returns {boolean} 是否处于进行中状态
     */
    isChildActive (sub) {
      return String(sub.status) === '2'
    },
    
    /**
     * 获取子任务时间线节点类名
     * 根据子任务状态返回对应的节点样式类名
     * @param {Object} sub - 子任务对象
     * @returns {string} 节点类名（active/completed/timeout/pending）
     */
    getChildNodeClass (sub) {
      const status = String(sub.status)
      if (status === '2') return 'active'
      if (status === '3') return 'completed'
      if (this.isChildOverdue(sub)) return 'timeout'
      return 'pending'
    },

    /**
     * ========== 格式化方法 ==========
     */
    
    formatStatus,
    formatTime,
    
    /**
     * 格式化优先级
     * 将优先级值转换为大写字符串
     * @param {string|number} val - 优先级值
     * @returns {string} 格式化后的优先级字符串
     */
    formatPriority (val) {
      return (val || 'NONE').toUpperCase()
    },
    
    /**
     * 获取状态标签类名（未使用，保留用于未来扩展）
     * @param {number|string} val - 状态值
     * @returns {string} 标签类名
     */
    statusTagClass (val) {
      const v = Number(val)
      return v === 2 ? 'tag-processing' : ''
    },
    
    /**
     * 获取任务标题状态类名
     * 根据任务状态和截止时间判断标题样式类名
     * @param {number|string} status - 任务状态
     * @param {string} dueTime - 截止时间
     * @returns {string} 状态类名（is-completed/is-overdue/空字符串）
     */
    taskTitleStatusClass (status, dueTime) {
      if (String(status) === '3') return 'is-completed'
      if (this.isTaskOverdue({ taskStatus: status, dueTime })) return 'is-overdue'
      return ''
    },
    
    /**
     * ========== 判断方法 ==========
     */
    
    /**
     * 判断任务是否超期
     * 检查任务的截止时间是否早于今天
     * @param {Object} task - 任务对象
     * @returns {boolean} 是否超期
     */
    isTaskOverdue (task) {
      if (!task || !task.dueTime) return false
      // 已完成的任务不算超期
      if (String(task.taskStatus) === '3') return false
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const dd = String(now.getDate()).padStart(2, '0')
      const today = `${y}-${m}-${dd}`
      return compareDateStrings(task.dueTime, today) < 0
    },
    
    /**
     * 判断子任务是否超期
     * 检查子任务的截止日期是否早于今天
     * @param {Object} sub - 子任务对象
     * @returns {boolean} 是否超期
     */
    isChildOverdue (sub) {
      if (!sub || !sub.dueDate) return false
      // 已完成的子任务不算超期
      if (String(sub.status) === '3') return false
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const today = `${y}-${m}-${d}`
      return compareDateStrings(sub.dueDate, today) < 0
    },
    
    /**
     * 判断行是否被选中
     * 比较任务ID与当前选中的任务ID
     * @param {Object} row - 任务行对象
     * @returns {boolean} 是否被选中
     */
    isRowSelected (row) {
      return this.resolveTaskId(row) === this.selectedTaskId
    },
    
    /**
     * 解析任务ID
     * 从任务对象中提取任务ID，支持多种字段名
     * @param {Object} t - 任务对象
     * @returns {string} 任务ID字符串
     */
    resolveTaskId (t) {
      return String((t && (t.taskId || t.taskID || t.id)) || '')
    },
    
    /**
     * ========== 子任务相关方法 ==========
     */
    
    /**
     * 获取任务的子任务列表
     * 从任务对象中提取并规范化子任务列表
     * @param {Object} row - 任务对象
     * @returns {Array} 规范化后的子任务数组
     */
    getChildren (row) {
      const raw = row.childTaskList || []
      return this.normalizeChildTasks(raw)
    },
    
    /**
     * 规范化子任务列表
     * 将原始子任务数据转换为统一的格式
     * @param {Array} list - 原始子任务数组
     * @returns {Array} 规范化后的子任务数组
     */
    normalizeChildTasks (list) {
      if (!Array.isArray(list)) return []
      return list.map(c => ({
        id: c.childTaskId || c.id,
        name: c.childTaskName || c.name,
        status: String(c.childTaskStatus || c.status || '1'),
        dueDate: c.dueTime || c.dueDate,
        assigneeId: c.assigneeUserId || c.assigneeId
      }))
    },
    
    /**
     * ========== 操作相关方法 ==========
     */
    
    /**
     * 重新加载任务列表
     * 重置并重新加载第一页任务
     */
    reloadTasks () {
      this.loadTasks(false)
    },
    
    /**
     * 处理搜索事件
     * 当用户在搜索框按回车时触发
     */
    onSearch () {
      this.reloadTasks()
    },
    
    /**
     * 清除搜索
     * 清空搜索关键词并重新加载任务列表
     */
    onSearchClear () {
      this.keyword = ''
      this.reloadTasks()
    },
    
    /**
     * 加载任务列表
     * 从API获取任务列表数据，支持分页和追加加载
     * @param {boolean} append - 是否为追加加载（true=追加到现有列表，false=重置列表）
     */
    async loadTasks (append = false) {
      // 追加模式：检查是否正在加载或已无更多数据
      if (append) {
        if (this.listLoadingMore || !this.listHasMore) return
        this.listLoadingMore = true
      } else {
        // 重置模式：初始化状态
        this.loading = true
        this.listPage = 1
        this.listTotal = 0
        this.listHasMore = true
        this.tasks = []
        this.childCache = {}
        // 滚动到顶部
        if (this.$refs.taskList) this.$refs.taskList.scrollTop = 0
      }
      
      try {
        // 构建请求参数
        const params = {}
        if (this.keyword && this.keyword.trim()) params.taskName = this.keyword.trim()
        
        // 设置分页参数
        const page = append ? (this.listPage + 1) : 1
        params.page = page
        params.size = this.listPageSize
        params.sortByMode = 0 // 默认排序模式
        params.taskStatus = [1, 2]
        
        // 调用API获取任务列表
        const res = await getTaskPageByUserId(params)
        console.log('Task List Loaded:', res)

        let items = []
        let total = 0
        
        // 规范化API响应数据
        if (Array.isArray(res)) {
          items = res
        } else if (res && typeof res === 'object') {
          const list = res.list || res.data || res.records || res.content || []
          total = Number(res.total || res.totalCount || res.totalElements || 0)
          items = Array.isArray(list) ? list : []
        }
        
        console.log('Processed Items:', items.length, 'Total:', total)

        // 处理子任务缓存
        const newChildCache = {}
        items.forEach(it => {
          const taskObj = (it && it.task) ? it.task : it
          const tid = this.resolveTaskId(taskObj)
          if (!tid) return
          // 提取子任务数据
          const rawChildren = Array.isArray(it && it.childTasks) ? it.childTasks 
            : (Array.isArray(taskObj && taskObj.childTasks) ? taskObj.childTasks : [])
          newChildCache[tid] = this.normalizeChildTasks(rawChildren)
        })
        
        // 更新子任务缓存
        this.childCache = append ? { ...this.childCache, ...newChildCache } : newChildCache
        
        // 扁平化任务数据（处理嵌套结构）
        const flat = Array.isArray(items) ? items.map(it => (it && it.task) ? it.task : it) : []
        
        // 更新任务列表
        if (append) {
          // 追加模式：合并到现有列表
          this.tasks = this.tasks.concat(flat)
          this.listPage = page
        } else {
          // 重置模式：替换列表
          this.tasks = flat
          this.listPage = 1
          // 如果列表不为空且没有选中任务，自动选中第一个任务
          if (this.tasks.length && !this.selectedTaskId) {
             this.onRowClick(this.tasks[0])
          }
        }
        
        // 更新分页信息
        if (total && !isNaN(total)) {
          this.listTotal = total
          this.listHasMore = (this.tasks.length < total)
        } else {
          // 如果没有总数信息，根据返回数据量判断是否还有更多
          this.listHasMore = flat.length >= this.listPageSize
        }
        
      } catch (e) {
        console.error('Load tasks failed', e)
        // 重置模式下，加载失败时清空列表
        if (!append) this.tasks = []
      } finally {
        // 重置加载状态
        if (append) this.listLoadingMore = false
        else this.loading = false
      }
    },
    
    /**
     * 处理列表滚动事件
     * 当滚动到底部附近时自动加载更多数据
     * @param {Event} e - 滚动事件对象
     */
    onListScroll (e) {
      const target = e.target
      // 距离底部50px时触发加载更多
      if (target.scrollTop + target.clientHeight >= target.scrollHeight - 50) {
        this.loadTasks(true)
      }
    },
    
    /**
     * 处理任务行点击事件
     * 当用户点击任务行时触发select事件
     * @param {Object} row - 被点击的任务行对象
     */
    onRowClick (row) {
      this.$emit('select', row)
    },
    
    /**
     * 切换子任务展开/折叠状态
     * 控制子任务面板的显示/隐藏
     * @param {Object} row - 任务行对象
     */
    toggleSubtaskExpand (row) {
      const tid = this.resolveTaskId(row)
      const idx = this.activeCollapseNames.indexOf(tid)
      if (idx > -1) {
        // 如果已展开，则折叠
        this.activeCollapseNames.splice(idx, 1)
      } else {
        // 如果已折叠，则展开
        this.activeCollapseNames.push(tid)
      }
    }
  }
}
</script>

<style scoped>
  @import "@/assets/css/recently-to-do/left-recent-list.css";
</style>
