<template>
  <!-- 任务列表组件根容器 -->
  <div class="task-list-pane">
    <!-- 列表头部：标题和工具栏 -->
    <div class="task-list-header" ref="listHeader">
      <!-- 标题区域 -->
      <div class="task-list-header-title-row">
        <div class="task-list-title-text-group">
          <div class="task-list-title-en">TASKS</div>
          <div class="task-list-title-cn">任务列表<span v-if="taskGroupName" style="color: var(--oxidized-sage);">  >> {{ taskGroupName }}</span></div>
        </div>
        <div class="task-list-header-decor-line">
          <span class="task-list-decor-text">PIONEER PROJECT // </span>
          <span class="task-list-decor-highlight">EXECUTE</span>
          <div class="task-list-decor-shapes">
            <span class="task-list-shape-circle"></span>
            <span class="task-list-shape-rect"></span>
          </div>
        </div>
      </div>

      <!-- 工具栏：排序、筛选、搜索、创建 -->
      <div class="task-list-toolbar task-list-retro-toolbar">
        <span style="color: var(--brand-primary);font-weight: 800;font-size: 12px;">&nbsp;>>>&nbsp;</span>
        <!-- 排序：时间 / 优先级 -->
        <el-radio-group v-model="sortBy" size="mini" @change="reloadTasks" style="margin-right: 8px;" class="task-list-retro-radio-group">
          <el-radio-button label="dueTime">时间排序</el-radio-button>
          <el-radio-button label="priority" style="margin-left: -5px;">优先级排序</el-radio-button>
        </el-radio-group>
        
        <!-- 状态筛选（重建） -->
        <el-popover
          placement="bottom"
          trigger="click"
          width="220"
          popper-class="task-list-retro-filter-popover"
          v-model="statusPopoverVisible"
        >
          <div class="task-list-retro-filter-content">
             <div class="task-list-filter-header-line">SELECT STATUS //</div>
             <div 
               class="task-list-filter-option" 
               v-for="(label, val) in statusDict" 
               :key="val" 
               @click="toggleStatus(val)"
               :class="{ 'task-list-is-selected': filterStatusList.includes(String(val)) }"
             >
                <div class="task-list-option-checkbox">
                  <div class="task-list-checkbox-inner"></div>
                </div>
                <i 
                  :class="['task-list-option-icon', getStatusIcon(val)]" 
                  :style="{ color: getStatusColor(val) }"
                ></i>
                <span class="task-list-option-label">{{ label }}</span>
             </div>
             <div class="task-list-filter-actions-line" v-if="filterStatusList.length > 0">
               <span class="task-list-action-text" @click="clearStatusFilter">>> CLEAR SELECTION</span>
             </div>
          </div>
          <div slot="reference" class="task-list-retro-status-trigger" :class="{ 'task-list-has-value': filterStatusList.length > 0 }">
             <span class="task-list-trigger-label">STATUS</span>
             <div class="task-list-trigger-value-box">
                <span v-if="filterStatusList.length === 0">ALL</span>
                <div v-else class="task-list-status-icon-group">
                  <i 
                    v-for="st in filterStatusList" 
                    :key="st" 
                    :class="getStatusIcon(st)" 
                    :style="{ color: getStatusColor(st) }"
                    class="task-list-status-icon-item"
                  ></i>
                </div>
             </div>
          </div>
        </el-popover>

        <!-- 与我相关切换按钮 -->
        <div 
          class="task-list-retro-filter-btn"
          :class="{ 'task-list-active': relatedToMe }"
          @click="toggleRelatedToMe"
        >
          <span class="task-list-btn-text">与我相关 {{ relatedToMe ? '[ON]' : '[OFF]' }}</span>
        </div>

        <!-- 搜索框 -->
        <div class="task-list-retro-search-box">
          <i class="el-icon-search task-list-search-icon"></i>
          <input 
            v-model="keyword" 
            class="task-list-retro-search-input" 
            placeholder="INPUT_TASK_NAME..." 
            @keyup.enter="onSearch"
          />
          <div class="task-list-search-corner"></div>
        </div>

        <!-- 创建任务按钮 -->
        <el-button size="mini" type="primary" style="margin-left: auto;" @click="$emit('create')" class="task-list-retro-btn task-list-create-btn">
          <div class="task-list-btn-content">
            <i class="el-icon-plus task-list-btn-icon"></i>
            <div class="task-list-btn-text-group">
              <span class="task-list-btn-en">C NEW</span>
              <span class="task-list-btn-cn">新建</span>
            </div>
          </div>
        </el-button>
      </div>
    </div>

    <!-- 任务列表容器 -->
    <div class="task-list-wrap task-list-retro-list-container" ref="tableWrap" v-loading="loading" element-loading-background="rgba(255, 255, 255, 0.5)" element-loading-text="LOADING..." element-loading-spinner="el-icon-loading">
      <div class="task-list" :style="{ height: listTableHeight }" ref="taskList" @scroll="onListScroll">
        <div class="task-list-task-row-wrapper" v-for="row in tasks" :key="String(row.taskId || row.taskID || row.id || Math.random())">
          
          <!-- Rhine Lab 风格的任务条 -->
          <div 
            class="task-list-rhine-task-strip task-list-main-task-strip" 
            :class="{ 'task-list-is-selected': isRowSelected(row), 'task-list-is-timeout': isTaskOverdue(row) }" 
            @click="onRowClick(row)"
          >
            <!-- 左侧三色条 -->
            <div class="task-list-rhine-strip-left-tri">
              <div class="task-list-tri-bar task-list-bar-mustard"></div>
              <div class="task-list-tri-bar task-list-bar-sage"></div>
              <div class="task-list-tri-bar task-list-bar-burgundy"></div>
            </div>

            <!-- 内容主体 -->
            <div class="task-list-rhine-strip-body task-list-main-body">
              <!-- 第一行：元信息 -->
              <div class="task-list-strip-row-meta">
                 <div class="task-list-meta-left">
                    <div class="task-list-rhine-task-date" :class="{ 'task-list-is-overdue': isTaskOverdue(row) }">
                       <span class="task-list-date-text">{{ row.dueTime ? formatTime(row.dueTime) : 'PENDING' }}</span>
                       <div class="task-list-date-icon-dots">
                          <span></span><span></span><span></span>
                       </div>
                    </div>
                    <!-- 状态标签 -->
                    <div class="task-list-rhine-status-pill" :class="getTaskStatusClass(row)">
                       {{ getTaskStatusText(row) }}
                    </div>

                 </div>
                 <div class="task-list-meta-right">
                    <div class="task-list-rhine-priority-logo task-list-mini">
                        <div class="task-list-logo-half task-list-left"><div class="task-list-logo-circle">P</div></div>
                        <div class="task-list-logo-half task-list-right" :style="{ backgroundColor: getPriorityColor(row.taskPriority) }">
                           <div class="task-list-logo-circle">{{ getPriorityNumber(row.taskPriority) }}</div>
                        </div>
                    </div>
                 </div>
              </div>

              <!-- 第二行：标题和子任务 -->
              <div class="task-list-strip-row-main">
                 <div class="task-list-task-name-text" :class="taskTitleStatusClass(row.taskStatus, row.dueTime)">
                    {{ row.taskName || 'UNTITLED_TASK' }}
                 </div>
                 <div class="task-list-subtask-indicator" @click.stop="toggleSubtaskExpand(row)">
                    SUBTASKS [{{ getChildren(row).length }}]
                    <i class="el-icon-arrow-right" :class="{ 'task-list-is-expanded': activeCollapseNames.includes(resolveTaskId(row)) }"></i>
                 </div>
              </div>
            </div>
          </div>

          <!-- 可折叠的子任务列表 -->
          <el-collapse-transition>
            <div v-show="activeCollapseNames.includes(resolveTaskId(row))" class="task-list-subtask-collapse-panel">
               <div v-if="getChildren(row).length" class="task-list-subtask-list-mini">
                  <div 
                    v-for="(sub, sIdx) in getChildren(row)" 
                    :key="sub.id || sIdx" 
                    class="task-list-subtask-timeline-row"
                  >
                     <!-- 时间线列 -->
                     <div class="task-list-timeline-col">
                        <div class="task-list-timeline-line"></div>
                        <div class="task-list-timeline-node" :class="getChildNodeClass(sub)">
                           <div class="task-list-ripple-ring" v-if="isChildActive(sub)"></div>
                        </div>
                     </div>
                     <div 
                       class="task-list-rhine-task-strip task-list-sub-strip"
                       :class="{ 'task-list-is-parent-timeout': isTaskOverdue(row), 'task-list-is-active-sub': isChildActive(sub) }"
                     >
                        <div class="task-list-rhine-strip-body task-list-sub-body">
                           <!-- 单行布局 -->
                           <div class="task-list-sub-status-box">
                              <div class="task-list-rhine-status-pill task-list-mini" :class="getChildTaskStatusClass(sub)">
                                 {{ getChildTaskStatusText(sub) }}
                              </div>
                           </div>
                           <div class="task-list-sub-name-text" :class="getChildTaskStatusClass(sub)">{{ sub.name }}</div>
                           <div class="task-list-sub-date" v-if="sub.dueDate">{{ formatTime(sub.dueDate) }}</div>
                        </div>
                     </div>
                  </div>
               </div>
               <div v-else class="task-list-no-subtasks-mini">NO SUBTASKS RECORDED</div>
            </div>
          </el-collapse-transition>

        </div>
        
        <!-- 加载更多 / 列表结束 -->
        <div class="task-list-loading-more" v-if="listLoadingMore">
          <i class="el-icon-loading" style="margin-right:6px"></i>
          LOADING MORE...
        </div>
        <div class="task-list-end" v-if="!listHasMore && tasks.length">
          <!-- 列表结束标记 -->
        </div>
      </div>
    </div>

    <!-- 列表底部：筛选摘要和设置 -->
    <div class="task-list-footer" ref="listFooter" style="background-color: black;">
      <div class="task-list-footer-left">
        <span>>> FILTER:</span> {{ filterSummary }} // <span>TOTAL:</span> {{ tasks.length }}
      </div>
      <div class="task-list-footer-right">
        <!-- 设置下拉菜单 -->
        <el-dropdown trigger="click" @command="handleMenuCommand" class="task-list-footer-menu">
          <el-button size="mini" icon="el-icon-setting" style="margin-left: 6px;" class="task-list-retro-btn">
            设置<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown" class="task-list-footer-dropdown-menu">
            <el-dropdown-item command="delete" :disabled="!canDeleteTaskGroup">
              <i class="el-icon-delete"></i> 删除任务族
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
import { getTaskPageByTaskGroup } from '@/api/task-api'
import { deletePrivateTaskGroup } from '@/api/taskgroup-api'
import defaultAvatar from '@/assets/imgs/default-user-avatar.png'
import { compareDateStrings } from '@/utils/validation'

/**
 * 任务状态字典
 * 映射状态码到中文描述
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
 * 优先级选项配置
 * 包含标签、值和颜色
 */
const priorityOptions = [
  { label: 'High', value: 'high', color: '#F56C6C' },
  { label: 'Medium', value: 'medium', color: '#E6A23C' },
  { label: 'Low', value: 'low', color: '#409EFF' },
  { label: 'None', value: 'none', color: '#909399' }
]

/**
 * 格式化任务状态
 * @param {string|number} val - 状态值
 * @returns {string} 状态中文描述
 */
function formatStatus (val) {
  const v = String(val)
  return statusDict[v] || '未知'
}

/**
 * 格式化时间
 * 如果是完整的 ISO 字符串，提取日期部分
 * @param {string} val - 时间字符串
 * @returns {string} 格式化后的日期字符串
 */
function formatTime (val) {
  if (!val) return ''
  // 如果是完整的 ISO 字符串，提取日期部分
  if (val.includes('T')) return val.split('T')[0]
  return val
}

/**
 * 任务列表组件
 * 
 * 功能：
 * 1. 显示指定任务族中的任务列表
 * 2. 支持按时间或优先级排序
 * 3. 支持按状态筛选任务
 * 4. 支持搜索任务名称
 * 5. 支持"与我相关"筛选
 * 6. 支持展开/折叠子任务
 * 7. 支持分页加载（滚动加载更多）
 * 8. 显示任务状态、优先级、到期时间等信息
 * 
 * Props：
 * - taskGroupId: 任务族ID（必填）
 * - taskGroupName: 任务族名称（可选）
 * - userList: 用户列表（用于显示用户名）
 * - selectedTaskId: 当前选中的任务ID（可选）
 * 
 * Events：
 * - select: 选择任务时触发，传递任务对象
 * - create: 点击创建按钮时触发
 * - deleted: 任务族被删除时触发
 */
export default {
  name: 'TaskList',
  props: {
    /**
     * 任务族ID
     * @type {String}
     */
    taskGroupId: { type: String, required: true },
    /**
     * 任务族名称
     * @type {String}
     */
    taskGroupName: { type: String, default: '' },
    /**
     * 任务族对象（包含 teamId、isPrivate 等信息）
     * @type {Object}
     */
    taskGroup: { type: Object, default: () => ({}) },
    /**
     * 用户列表
     * @type {Array}
     */
    userList: { type: Array, default: () => [] },
    /**
     * 当前选中的任务ID
     * @type {String}
     */
    selectedTaskId: { type: String, default: null }
  },
  data () {
    return {
      // 默认头像
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
      // 每页数量
      listPageSize: 20,
      // 总记录数
      listTotal: 0,
      
      // 搜索关键词
      keyword: '',
      // 状态筛选列表（默认：未开始和进行中）
      filterStatusList: ['1', '2'],
      // 状态筛选弹窗可见性
      statusPopoverVisible: false,
      // 排序方式：dueTime（时间）或 priority（优先级）
      sortBy: 'dueTime',
      // 是否只显示与我相关的任务（默认开启）
      relatedToMe: true,
      
      // 当前展开的子任务列表（存储任务ID）
      activeCollapseNames: [],
      // 子任务缓存
      childCache: {},
      
      // 状态字典
      statusDict,
      // 优先级选项
      priorityOptions,
      // 修复：添加 showBack 到 data 以防止引用错误（如果模板或 mixin 中存在）
      showBack: false
    }
  },
  computed: {
    /**
     * 判断是否为私人任务族
     * 根据 taskGroup 对象的 teamId 或 isPrivate 字段判断
     * @returns {Boolean} 是否为私人任务族
     */
    isPrivateTaskGroup () {
      const group = this.taskGroup || {}
      // 优先使用 isPrivate 字段
      if (typeof group.isPrivate === 'boolean') {
        return group.isPrivate
      }
      // 如果没有 isPrivate，则判断 teamId 是否为空
      const teamId = group.teamId
      return !teamId || teamId.trim() === ''
    },
    
    /**
     * 判断是否可以删除任务族
     * 只有私人任务族才能删除
     * @returns {Boolean} 是否可以删除
     */
    canDeleteTaskGroup () {
      return this.isPrivateTaskGroup
    },
    
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
     * @returns {Object} 优先级值到颜色的映射
     */
    priorityColorList () {
      return this.priorityOptions.reduce((acc, cur) => {
        acc[cur.value] = cur.color
        return acc
      }, {})
    },
    /**
     * 筛选摘要
     * 生成当前筛选条件的文本摘要
     * @returns {string} 筛选摘要字符串
     */
    filterSummary () {
      const statusLabels = this.filterStatusList.map(v => this.statusDict[v] || v).join(',')
      return `STATUS[${statusLabels || 'ALL'}] SORT[${this.sortBy}]`
    }
  },
  methods: {
    /**
     * ========== 菜单命令处理方法 ==========
     */
    
    /**
     * 处理下拉菜单命令
     * @param {String} command - 命令名称
     */
    handleMenuCommand (command) {
      if (command === 'delete') {
        this.handleDeleteTaskGroup()
      }
    },
    
    /**
     * 处理删除任务族操作
     * 根据任务族类型显示不同的处理逻辑
     */
    async handleDeleteTaskGroup () {
      // 判断是否为私人任务族
      if (this.isPrivateTaskGroup) {
        // 私人任务族：显示二次确认对话框
        try {
          // 显示确认对话框，等待用户确认
          await this.$confirm(
            `确认删除任务族「${this.taskGroupName || this.taskGroupId}」吗？此操作不可恢复！`,
            '删除任务族',
            {
              confirmButtonText: '确认删除',
              cancelButtonText: '取消',
              type: 'error'
            }
          )
          
          // 用户确认后，调用删除 API
          const taskGroupId = String(this.taskGroupId || '').trim()
          if (!taskGroupId) {
            this.$message.error('任务族ID无效')
            return
          }
          
          // 调用删除 API
          const result = await deletePrivateTaskGroup(taskGroupId)
          
          // 检查删除结果
          console.log('删除任务族结果:', result)
          
          // 显示成功消息
          this.$message.success('任务族已删除')
          
          // 触发删除事件，通知父组件
          this.$emit('deleted')
          
          // 延迟跳转到首页
          setTimeout(() => {
            this.$router.push({ name: 'home' }).catch(err => {
              // 如果路由跳转失败，尝试使用路径跳转
              console.error('路由跳转失败:', err)
              this.$router.push({ path: '/home' }).catch(() => {
                window.location.href = '/'
              })
            })
          }, 1000)
        } catch (error) {
          // 用户取消操作时不显示错误
          if (error === 'cancel' || error === 'close' || error === 'clickoutside') {
            return
          }
          
          // 处理 API 错误
          let errorMessage = '删除任务族失败'
          if (error && error.message) {
            errorMessage = error.message
          } else if (error && error.response && error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message
          } else if (typeof error === 'string') {
            errorMessage = error
          }
          
          console.error('删除任务族失败:', error)
          this.$message.error(errorMessage)
        }
      } else {
        // 团队任务族：提示需要前往项目团队删除
        this.$message.warning('团队任务族需要在项目团队中进行删除，请前往项目团队设置页面操作')
      }
    },
    
    /**
     * ========== 优先级相关辅助方法 ==========
     */
    
    /**
     * 获取优先级颜色
     * 根据优先级值返回对应的主题颜色
     * @param {string|number} p - 优先级值
     * @returns {string} 颜色值（十六进制）
     */
    getPriorityColor (p) {
      const val = Number(p)
      if (val === 0) return '#802520' // P0 Burgundy
      if (val === 1) return '#B2653B' // P1 Rust
      if (val === 2) return '#BA8530' // P2 Mustard
      if (val === 3) return '#5C7F71' // P3 Sage
      return '#181818' // P4/Default Black
    },
    
    /**
     * 获取优先级数字
     * @param {string|number} p - 优先级值
     * @returns {string} 优先级数字字符串（0-4）或 '#'
     */
    getPriorityNumber (p) {
      const val = Number(p)
      return (val >= 0 && val <= 4) ? String(val) : '#'
    },

    /**
     * ========== 任务状态相关辅助方法 ==========
     */
    
    /**
     * 获取任务状态类名
     * 根据任务状态和到期时间计算状态类名
     * @param {Object} row - 任务对象
     * @returns {string} 状态类名：disabled、completed、timeout、process
     */
    getTaskStatusClass (row) {
      const status = Number(row.taskStatus)
      if (status === 0 || status === 4) return 'disabled'
      if (status === 3) return 'completed'
      
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
     * @param {Object} row - 任务对象
     * @returns {string} 状态文本：Disabled、Completed、Timeout、Process
     */
    getTaskStatusText (row) {
      const cls = this.getTaskStatusClass(row)
      if (cls === 'disabled') return 'Disabled'
      if (cls === 'completed') return 'Completed'
      if (cls === 'timeout') return 'Timeout'
      return 'Process'
    },

    /**
     * ========== 子任务状态相关辅助方法 ==========
     */
    
    /**
     * 获取子任务状态类名
     * @param {Object} ct - 子任务对象
     * @returns {string} 状态类名：completed、timeout 或空字符串
     */
    getChildTaskStatusClass (ct) {
      const status = String(ct.status)
      if (status === '3') return 'completed'
      
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
     * @param {Object} ct - 子任务对象
     * @returns {string} 状态文本：Completed、Timeout、Process
     */
    getChildTaskStatusText (ct) {
      const cls = this.getChildTaskStatusClass(ct)
      if (cls === 'completed') return 'Completed'
      if (cls === 'timeout') return 'Timeout'
      return 'Process'
    },
    
    /**
     * 判断子任务是否处于活动状态（进行中）
     * @param {Object} sub - 子任务对象
     * @returns {boolean} 是否为活动状态
     */
    isChildActive (sub) {
      return String(sub.status) === '2'
    },
    
    /**
     * 获取子任务节点类名
     * 用于时间线节点的样式
     * @param {Object} sub - 子任务对象
     * @returns {string} 节点类名：active、completed、timeout、pending
     */
    getChildNodeClass (sub) {
      const status = String(sub.status)
      if (status === '2') return 'active'
      if (status === '3') return 'completed'
      if (this.isChildOverdue(sub)) return 'timeout'
      return 'pending'
    },

    /**
     * ========== 用户相关辅助方法 ==========
     */
    
    /**
     * 获取用户头像
     * @param {string} uid - 用户ID
     * @returns {string} 头像URL
     */
    getUserAvatar (uid) {
      if (!uid) return ''
      const u = this.userList.find(x => String(x.userId) === String(uid))
      return u ? (u.userAvatar || u.avatar) : ''
    },

    /**
     * ========== 筛选相关方法 ==========
     */
    
    /**
     * 切换状态筛选
     * 如果状态已在筛选列表中则移除，否则添加
     * @param {string|number} val - 状态值
     */
    toggleStatus(val) {
      const v = String(val)
      const index = this.filterStatusList.indexOf(v)
      if (index > -1) {
        this.filterStatusList.splice(index, 1)
      } else {
        this.filterStatusList.push(v)
      }
      this.reloadTasks()
    },
    
    /**
     * 清空状态筛选
     */
    clearStatusFilter() {
      this.filterStatusList = []
      this.reloadTasks()
    },
    
    /**
     * 获取状态图标类名
     * @param {string|number} status - 状态值
     * @returns {string} Element UI 图标类名
     */
    getStatusIcon(status) {
      const icons = {
        '0': 'el-icon-circle-close',
        '1': 'el-icon-remove-outline',
        '2': 'el-icon-video-play',
        '3': 'el-icon-circle-check',
        '4': 'el-icon-warning-outline',
        '5': 'el-icon-video-pause'
      }
      return icons[String(status)] || 'el-icon-question'
    },
    
    /**
     * 获取状态颜色
     * @param {string|number} status - 状态值
     * @returns {string} 颜色值（十六进制）
     */
    getStatusColor(status) {
      const colors = {
        '0': '#909399', // Cancelled - Grey
        '1': '#606266', // Not Started - Dark Grey
        '2': '#409EFF', // In Progress - Blue
        '3': '#67C23A', // Completed - Green
        '4': '#E6A23C', // Delayed - Orange
        '5': '#F56C6C'  // Paused - Red
      }
      return colors[String(status)] || '#606266'
    },

    /**
     * ========== 格式化方法 ==========
     */
    
    formatStatus,
    formatTime,
    
    /**
     * 格式化优先级
     * @param {string} val - 优先级值
     * @returns {string} 大写的优先级字符串
     */
    formatPriority (val) {
      return (val || 'NONE').toUpperCase()
    },
    
    /**
     * 状态标签类名
     * @param {string|number} val - 状态值
     * @returns {string} 标签类名
     */
    statusTagClass (val) {
      const v = Number(val)
      return v === 2 ? 'tag-processing' : ''
    },
    
    /**
     * 任务标题状态类名
     * @param {string|number} status - 任务状态
     * @param {string} dueTime - 到期时间
     * @returns {string} 状态类名：task-list-is-completed、task-list-is-overdue 或空字符串
     */
    taskTitleStatusClass (status, dueTime) {
      if (String(status) === '3') return 'task-list-is-completed'
      if (this.isTaskOverdue({ taskStatus: status, dueTime })) return 'task-list-is-overdue'
      return ''
    },

    /**
     * ========== 任务判断方法 ==========
     */
    
    /**
     * 判断任务是否逾期
     * @param {Object} task - 任务对象
     * @returns {boolean} 是否逾期
     */
    isTaskOverdue (task) {
      if (!task || !task.dueTime) return false
      if (String(task.taskStatus) === '3') return false
      const now = new Date()
      const d = new Date(task.dueTime)
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const dd = String(now.getDate()).padStart(2, '0')
      const today = `${y}-${m}-${dd}`
      // 使用 compareDateStrings 逻辑进行日期比较
      return compareDateStrings(task.dueTime, today) < 0
    },
    
    /**
     * 判断子任务是否逾期
     * @param {Object} sub - 子任务对象
     * @returns {boolean} 是否逾期
     */
    isChildOverdue (sub) {
      if (!sub || !sub.dueDate) return false
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
     * @param {Object} row - 任务对象
     * @returns {boolean} 是否选中
     */
    isRowSelected (row) {
      return this.resolveTaskId(row) === this.selectedTaskId
    },
    
    /**
     * 解析任务ID
     * 支持多种字段名：taskId、taskID、id
     * @param {Object} t - 任务对象
     * @returns {string} 任务ID字符串
     */
    resolveTaskId (t) {
      return String((t && (t.taskId || t.taskID || t.id)) || '')
    },
    
    /**
     * 根据ID显示用户名
     * @param {string} uid - 用户ID
     * @returns {string} 用户名或用户ID
     */
    displayUserNameById (uid) {
      if (!uid) return ''
      const u = this.userList.find(x => String(x.userId) === String(uid))
      return u ? (u.userName || u.name) : uid
    },

    /**
     * ========== 子任务相关方法 ==========
     */
    
    /**
     * 获取任务的子任务列表
     * @param {Object} row - 任务对象
     * @returns {Array} 规范化后的子任务列表
     */
    getChildren (row) {
      const raw = row.childTaskList || []
      return this.normalizeChildTasks(raw)
    },
    
    /**
     * 规范化子任务列表
     * 统一子任务对象的字段名和格式
     * @param {Array} list - 原始子任务列表
     * @returns {Array} 规范化后的子任务列表
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
     * 重置分页并重新加载第一页数据
     */
    reloadTasks () {
      this.loadTasks(false)
    },
    
    /**
     * 切换"与我相关"筛选
     */
    toggleRelatedToMe () {
      this.relatedToMe = !this.relatedToMe
      this.reloadTasks()
    },
    
    /**
     * 移除状态筛选
     * @param {string} val - 要移除的状态值
     */
    removeFilterStatus (val) {
      this.filterStatusList = this.filterStatusList.filter(v => v !== val)
      this.reloadTasks()
    },
    
    /**
     * 搜索处理
     * 按回车键时触发
     */
    onSearch () {
      this.reloadTasks()
    },
    
    /**
     * 清空搜索
     */
    onSearchClear () {
      this.keyword = ''
      this.reloadTasks()
    },
    
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载任务列表
     * 支持追加模式和重置模式
     * @param {boolean} append - 是否为追加模式（true：追加到现有列表，false：重置列表）
     */
    async loadTasks (append = false) {
      const id = this.taskGroupId
      if (!id) { this.tasks = []; return }
      
      if (append) {
        // 追加模式：检查是否正在加载或没有更多数据
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
        // 重置滚动位置
        if (this.$refs.taskList) this.$refs.taskList.scrollTop = 0
      }
      
      try {
        // 构建请求参数
        const params = {}
        if (this.keyword && this.keyword.trim()) params.taskName = this.keyword.trim()
        
        // taskStatus: List<Integer> (后端期望的参数名是 taskStatus，不是 taskStatusList)
        if (Array.isArray(this.filterStatusList) && this.filterStatusList.length) {
          params.taskStatus = this.filterStatusList.map(s => Number(s))
        }

        // sortByMode: 0 (Time) / 1 (Priority)
        if (this.sortBy) {
          params.sortByMode = this.sortBy === 'priority' ? 1 : 0
        }
        
        if (this.relatedToMe) params.relatedToMe = true
        
        const page = append ? (this.listPage + 1) : 1
        params.page = page
        params.size = this.listPageSize
        
        // 调用API获取任务列表
        const res = await getTaskPageByTaskGroup(id, params)
        console.log('Task List Loaded:', res) // Debug log

        let items = []
        let total = 0
        
        // 规范化响应结构
        if (Array.isArray(res)) {
          items = res
        } else if (res && typeof res === 'object') {
          // 假设响应结构为 { list, total } 或类似格式
          const list = res.list || res.data || res.records || res.content || []
          total = Number(res.total || res.totalCount || res.totalElements || 0)
          items = Array.isArray(list) ? list : []
        }
        
        console.log('Processed Items:', items.length, 'Total:', total) // Debug log

        // 处理子任务缓存
        const newChildCache = {}
        items.forEach(it => {
          const taskObj = (it && it.task) ? it.task : it
          const tid = this.resolveTaskId(taskObj)
          if (!tid) return
          const rawChildren = Array.isArray(it && it.childTasks) ? it.childTasks 
            : (Array.isArray(taskObj && taskObj.childTasks) ? taskObj.childTasks : [])
          newChildCache[tid] = this.normalizeChildTasks(rawChildren)
        })
        
        this.childCache = append ? { ...this.childCache, ...newChildCache } : newChildCache
        
        // 扁平化任务列表（提取 task 对象）
        const flat = Array.isArray(items) ? items.map(it => (it && it.task) ? it.task : it) : []
        
        if (append) {
          // 追加模式：合并到现有列表
          this.tasks = this.tasks.concat(flat)
          this.listPage = page
        } else {
          // 重置模式：替换列表
          this.tasks = flat
          this.listPage = 1
          // 如果存在任务且没有选中任务，自动选择第一个
          // 父组件通过事件处理选择，但我们可以触发 'loaded' 或 'select' 事件
          if (this.tasks.length && !this.selectedTaskId) {
             this.onRowClick(this.tasks[0])
          }
        }
        
        // 更新分页信息
        if (total && !isNaN(total)) {
          this.listTotal = total
          this.listHasMore = (this.tasks.length < total)
        } else {
          this.listHasMore = flat.length >= this.listPageSize
        }
        
      } catch (e) {
        console.error('Load tasks failed', e)
        if (!append) this.tasks = []
      } finally {
        if (append) this.listLoadingMore = false
        else this.loading = false
      }
    },
    
    /**
     * ========== 事件处理方法 ==========
     */
    
    /**
     * 列表滚动处理
     * 当滚动到底部附近时自动加载更多
     * @param {Event} e - 滚动事件对象
     */
    onListScroll (e) {
      const target = e.target
      if (target.scrollTop + target.clientHeight >= target.scrollHeight - 50) {
        this.loadTasks(true)
      }
    },
    
    /**
     * 行点击处理
     * 触发 select 事件，传递任务对象
     * @param {Object} row - 任务对象
     */
    onRowClick (row) {
      this.$emit('select', row)
    },
    
    /**
     * 切换子任务展开/折叠
     * @param {Object} row - 任务对象
     */
    toggleSubtaskExpand (row) {
      const tid = this.resolveTaskId(row)
      const idx = this.activeCollapseNames.indexOf(tid)
      if (idx > -1) {
        this.activeCollapseNames.splice(idx, 1)
      } else {
        this.activeCollapseNames.push(tid)
      }
    }
  },
  watch: {
    /**
     * 监听任务族ID变化
     * 当任务族ID改变时，立即重新加载任务列表
     */
    taskGroupId: {
      immediate: true,
      handler () { this.loadTasks() }
    }
  }
}
</script>

<style scoped>
  @import "@/assets/css/task-group-detail/task-list.css";
</style>
