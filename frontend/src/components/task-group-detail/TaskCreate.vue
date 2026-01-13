<template>
  <!-- 任务创建/详情组件根容器 -->
  <div class="task-detail-main-card task-detail-create-card" v-loading="loading">
    <!-- 终端样式头部区域：包含任务名称输入和装饰性线条 -->
    <div class="task-detail-terminal-header">
      <div class="task-detail-header-top">
        <div class="task-detail-terminal-title-container">
          <textarea
            ref="taskNameInput"
            v-model="newTask.taskName"
            class="task-detail-terminal-title-input"
            :class="taskNameStatusClass"
            placeholder="ADD_NEW_TASK_NAME"
            rows="1"
            @input="adjustInputSize"
            @focus="adjustInputSize"
            @keydown.enter.prevent
          ></textarea>
        </div>
        <div class="task-detail-header-lines">
          <div class="task-detail-line-mustard"></div>
          <div class="task-detail-line-sage"></div>
          <div class="task-detail-line-burgundy"></div>
        </div>
      </div>
      <div class="task-detail-header-sub">
        <!-- Priority Logo (RP Style) -->
        <div class="task-detail-priority-logo">
          <div class="task-detail-logo-half task-detail-left">
            <div class="task-detail-logo-circle">P</div>
          </div>
          <div class="task-detail-logo-half task-detail-right" :style="{ backgroundColor: currentPriorityColor }">
            <div class="task-detail-logo-circle">{{ currentPriorityNumber }}</div>
          </div>
        </div>

        <!-- Status Button (Capsule Style) -->
        <div class="task-detail-header-status-btn" 
             :class="[headerStatusState, { 'task-detail-is-clickable': isStatusClickable }]"
             @click="toggleStatus">
          {{ headerStatusText }}
        </div>

        <div class="task-detail-header-desc">
          Rhine Lab Pioneer Project
        </div>
      </div>
    </div>

    <div class="task-detail-body task-detail-theme-body">
      <!-- 背景装饰文字 -->
      <div class="task-detail-bg-text">ARKNIGHTS</div>

      <!-- 第一部分：基础信息 (包含任务分配、时间节点、状态监测) -->
      <div class="task-detail-section-header">
         <div class="task-detail-title-block">
            <h1 class="task-detail-main-title">Instruction <span class="task-detail-thin">| 基础信息</span></h1>
            <div class="task-detail-subtitle">{{ dueDateStatusText }}</div>
         </div>
         
         <div class="task-detail-info-grid">
            <!-- 任务分配卡片：展示所属分组和团队信息 -->
            <div class="task-detail-info-card">
               <div class="task-detail-pill-label">ASSIGNMENT / 任务分配</div>
               <div class="task-detail-card-content">
                  <div class="task-detail-input-group">
                     <span class="task-detail-input-label">
                        <div>GROUP</div>
                        <div class="task-detail-sub-label">归属</div>
                     </span>
                     <div class="task-detail-scroll-wrapper" ref="groupWrapper" :class="{ 'task-detail-is-scrolling': isGroupOverflow }">
                        <div class="task-detail-marquee-track" :class="{ 'task-detail-animate-marquee': isGroupOverflow }" :style="isGroupOverflow ? { animationName: 'task-detail-marquee-left' } : {}">
                           <span ref="groupText">{{ currentGroupName }}</span>
                           <span v-if="isGroupOverflow" class="task-detail-marquee-duplicate">{{ currentGroupName }}</span>
                        </div>
                     </div>
                  </div>
                  <div class="task-detail-input-group">
                     <span class="task-detail-input-label">
                        <div>TEAM</div>
                        <div class="task-detail-sub-label">团队</div>
                     </span>
                     <div class="task-detail-scroll-wrapper" ref="teamWrapper" :class="{ 'task-detail-is-scrolling': isTeamOverflow }">
                        <div class="task-detail-marquee-track" :class="{ 'task-detail-animate-marquee': isTeamOverflow }" :style="isTeamOverflow ? { animationName: 'task-detail-marquee-left' } : {}">
                           <span ref="teamText">{{ currentTeamName }}</span>
                           <span v-if="isTeamOverflow" class="task-detail-marquee-duplicate">{{ currentTeamName }}</span>
                        </div>
                     </div>
                  </div>
               </div>
            </div>

            <!-- 时间节点卡片：设置开始和截止日期 -->
            <div class="task-detail-info-card">
               <div class="task-detail-pill-label">TIMEFRAME / 时间节点</div>
               <div class="task-detail-card-content">
                  <div class="task-detail-input-group">
                     <span class="task-detail-input-label">
                        <div>START</div>
                        <div class="task-detail-sub-label">开始</div>
                     </span>
                     <el-date-picker 
                       v-model="newTask.startTime" 
                       type="date" 
                       value-format="yyyy-MM-dd" 
                       placeholder="PENDING" 
                       size="mini" 
                       :picker-options="newStartPickerOptions" 
                       class="task-detail-clean-picker" 
                       popper-class="task-detail-date-popper" 
                     />
                  </div>
                  <div class="task-detail-input-group">
                     <span class="task-detail-input-label">
                        <div>DUE</div>
                        <div class="task-detail-sub-label">截止</div>
                     </span>
                     <el-date-picker 
                       v-model="newTask.dueTime" 
                       type="date" 
                       value-format="yyyy-MM-dd" 
                       placeholder="PENDING" 
                       size="mini" 
                       :picker-options="newDuePickerOptions" 
                       class="task-detail-clean-picker" 
                       popper-class="task-detail-date-popper" 
                     />
                  </div>
               </div>
            </div>

            <!-- 状态监测卡片：设置优先级和当前状态 -->
            <div class="task-detail-info-card">
               <div class="task-detail-pill-label">CONDITION / 状态监测</div>
               <div class="task-detail-card-content">
                  <div class="task-detail-input-group">
                     <span class="task-detail-input-label">
                        <div>PRIORITY</div>
                        <div class="task-detail-sub-label">优先级</div>
                     </span>
                     <el-select 
                       v-model="newTask.taskPriority" 
                       placeholder="PRIORITY" 
                       size="mini" 
                       class="task-detail-clean-select" 
                       popper-class="task-detail-dropdown"
                     >
                        <el-option v-for="p in priorityOptions" :key="p.value" :label="p.label" :value="p.value">
                          <span class="task-detail-option-with-dot"><span class="task-detail-color-dot" :style="{ background: p.color }"></span>{{ p.label }}</span>
                        </el-option>
                     </el-select>
                  </div>
                  <div class="task-detail-input-group">
                     <span class="task-detail-input-label">
                        <div>STATUS</div>
                        <div class="task-detail-sub-label">状态</div>
                     </span>
                     <el-select 
                       v-model="newTask.taskStatus" 
                       placeholder="STATUS" 
                       size="mini" 
                       class="task-detail-clean-select" 
                       popper-class="task-detail-dropdown"
                     >
                        <el-option 
                          v-for="(label, val) in taskStatusByCode" 
                          :key="val" 
                          :label="label" 
                          :value="Number(val)" 
                          :disabled="isStatusOptionDisabled(Number(val))"
                        />
                     </el-select>
                  </div>
               </div>
            </div>
         </div>
         
         <!-- 协助人员卡片：全宽显示，支持添加多个协助人 -->
         <div class="task-detail-info-card task-detail-full-width">
            <div class="task-detail-pill-label">COLLABORATOR / 协助人员</div>
            <div class="task-detail-card-content">
               <div class="task-detail-collaborator-row">
                  <div class="task-detail-collaborator-list">
                     <div v-for="u in selectedAssignees" :key="u.userId" class="task-detail-collab-chip" :title="u.userName">
                        <div class="task-detail-collab-avatar">
                           <img :src="u.userAvatar || defaultAvatar" class="task-detail-avatar-img" />
                        </div>
                        <div class="task-detail-collab-name">{{ u.userName }}</div>
                     </div>
                     <div v-if="selectedAssignees.length === 0" class="task-detail-collab-placeholder">
                        NO COLLABORATORS
                     </div>
                  </div>
                  <div class="task-detail-mini-add-btn" @click="assignDrawerVisible = true" title="Add Collaborator">
                     <span class="task-detail-plus-sign">+</span>
                  </div>
               </div>
            </div>
         </div>
      </div>

      <!-- 第二部分：子任务模块 (支持拖拽排序) -->
      <div class="task-detail-list-section">
         <div class="task-detail-list-header">
         <div class="task-detail-header-title-container">
            <div class="task-detail-header-square"></div>
            <span class="task-detail-list-title">SUB_TASKS_MODULE / 子任务模块</span>
            <div class="task-detail-header-dashed-line"></div>
         </div>
         <div class="task-detail-add-btn" @click="addChildTask">
            <span>+ ADD NEW</span>
         </div>
      </div>
         
         <div class="task-detail-task-list">
            <!-- 无数据状态展示 -->
            <div v-if="newTask.childTasks.length === 0" class="task-detail-no-data">
               <div class="task-detail-nd-header">
                  <span class="task-detail-nd-label">System.</span>
                  <div class="task-detail-nd-line-container">
                     <div class="task-detail-nd-line-segment task-detail-seg-1"></div>
                     <div class="task-detail-nd-line-segment task-detail-seg-2"></div>
                     <div class="task-detail-nd-line-segment task-detail-seg-3"></div>
                  </div>
               </div>
               <div class="task-detail-nd-title">NO DATA AVAILABLE</div>
            </div>
            <!-- 子任务列表项 -->
            <div 
               v-for="(ct, idx) in newTask.childTasks" 
               :key="ct._lid || idx"
               class="task-detail-task-strip-detail"
               draggable="true"
               :class="{ 'task-detail-dragging': dragSubtaskIndex === idx, 'task-detail-drag-over': dragOverSubtaskIndex === idx }"
               @dragstart="onSubtaskDragStart(idx, $event)"
               @dragover.prevent="onSubtaskDragOver(idx, $event)"
               @drop="onSubtaskDrop(idx, $event)"
               @dragend="onSubtaskDragEnd($event)"
            >
               <!-- 左侧三色装饰条 -->
               <div class="task-detail-strip-left-tri">
                  <div class="task-detail-tri-bar task-detail-bar-mustard"></div>
                  <div class="task-detail-tri-bar task-detail-bar-sage"></div>
                  <div class="task-detail-tri-bar task-detail-bar-burgundy"></div>
               </div>
               
               <!-- 子任务主体内容 -->
               <div class="task-detail-strip-body">
                  <!-- 第一行：状态胶囊、索引编号、字数统计 -->
                  <div class="task-detail-strip-row-1">
                     <div class="task-detail-status-pill" :class="getChildTaskStatusClass(ct)" @click="toggleChildStatus(ct)">
                        {{ getChildTaskStatusText(ct) }}
                     </div>
                     <span class="task-detail-idx">CW-{{ idx + 1 }}</span>
                     <span class="task-detail-char-count">{{ (ct.name || '').length }}/80</span>
                  </div>
                  
                  <!-- 第二行：子任务名称输入 -->
                  <div class="task-detail-strip-row-2">
                     <span class="task-detail-input-prefix">>>&nbsp;&nbsp;</span>
                     <el-input 
                        type="textarea"
                        v-model="ct.name" 
                        class="task-detail-strip-name-input" 
                        :class="getChildTaskStatusClass(ct)"
                        placeholder="ENTER OBJECTIVE..." 
                        :autosize="{ minRows: 1, maxRows: 4 }"
                        resize="none"
                        maxlength="80"
                     />
                  </div>
                  
                  <!-- 第三行：控制栏 (日期、执行人、删除) -->
                  <div class="task-detail-strip-row-3">
                     <!-- 截止日期选择器 -->
                     <el-date-picker
                       v-model="ct.dueDate"
                       type="date"
                       placeholder="DDL DATE"
                       class="task-detail-mini-date"
                       value-format="yyyy-MM-dd"
                       :popper-class="'task-detail-date-picker-popper'"
                       :picker-options="getChildTaskDatePickerOptions(ct)"
                       prefix-icon="el-icon-date"
                       size="mini"
                       @focus="onChildDateFocus(ct)"
                       @change="onChildDateChange(ct)"
                     />
                     
                     <!-- 执行人选择器 (胶囊样式) -->
                     <div class="task-detail-capsule-selector">
                        <el-select 
                           v-model="ct.assigneeId" 
                           class="task-detail-capsule-select" 
                           popper-class="task-detail-dropdown" 
                           placeholder="ASSIGN"
                           size="mini"
                        >
                           <!-- 自定义前缀：显示当前选中用户的头像 -->
                           <template slot="prefix">
                              <div class="task-detail-capsule-avatar-container">
                                 <img 
                                   :src="getAssigneeAvatar(ct.assigneeId)" 
                                   class="task-detail-capsule-avatar"
                                 />
                              </div>
                           </template>

                           <el-option 
                               v-for="u in subtaskCandidateUsers" 
                               :key="u.userId" 
                               :label="u.userName" 
                               :value="u.userId"
                           >
                              <div style="display: flex; align-items: center; justify-content: space-between;">
                                 <div style="display:flex; align-items:center;">
                                    <img :src="u.userAvatar || defaultAvatar" style="width: 20px; height: 20px; border-radius: 50%; margin-right:8px;">
                                    <span>{{ u.userName }}</span>
                                 </div>
                              </div>
                           </el-option>
                        </el-select>
                     </div>
                     
                     <!-- 删除按钮 -->
                     <div class="task-detail-delete-btn" @click="removeChildTask(idx)">×</div>
                  </div>
               </div>
            </div>
         </div>
      </div>

      <!-- 第三部分：任务描述模块 (录音机风格) -->
      <div class="task-detail-recorder-section">
         <div class="task-detail-list-header">
            <div class="task-detail-header-title-container">
               <div class="task-detail-header-square" style="background-color: var(--oxidized-sage)"></div>
               <span class="task-detail-list-title">DESCRIPTION_MODULE / 任务描述模块</span>
               <div class="task-detail-header-dashed-line"></div>
            </div>
         </div>
         <div class="task-detail-recorder-box">
            <div class="task-detail-recorder-header">
               <div class="task-detail-recorder-icon-chip">
                  <div class="task-detail-chip-inner"></div>
               </div>
               <div class="task-detail-recorder-meta">
                  <span>DESCRIPTION_INTRODUCTION</span>
                  <div class="task-detail-nd-line-container" style="margin-top: -4px;padding: 0 10px 0 10px;">
                     <div class="task-detail-nd-line-segment task-detail-seg-1" ></div>
                     <div class="task-detail-nd-line-segment task-detail-seg-2"></div>
                     <div class="task-detail-nd-line-segment task-detail-seg-3"></div>
                  </div>
                  <span class="task-detail-recorder-code">{{ taskDescriptionLength }}</span>
               </div>
            </div>
            <div class="task-detail-recorder-body">
               <el-input 
                 type="textarea" 
                 v-model="newTask.taskDescription" 
                 :autosize="{ minRows: 3 }" 
                 placeholder="INPUT FLIGHT LOG DETAILS..." 
                 class="task-detail-recorder-input" 
               />
            </div>
            <div class="task-detail-recorder-footer">
               <span>► GET</span>
               <span class="task-detail-recorder-timer">00:00:00 HR MIN SEC</span>
               <span class="task-detail-recorder-rh2">RH2</span>
            </div>
         </div>
      </div>

      <!-- 第四部分：附件模块 (占位符) -->
      <div class="task-detail-attachment-section" style="padding: 0 24px 24px;">
         <div class="task-detail-list-header">
            <div class="task-detail-header-title-container">
               <div class="task-detail-header-square"></div>
               <span class="task-detail-list-title">ATTACHMENT_MODULE / 附件模块</span>
               <div class="task-detail-header-dashed-line"></div>
            </div>
         </div>
         
         <div class="task-detail-info-card task-detail-full-width" style="margin-bottom: 0; min-height: 80px; display: flex; align-items: center; justify-content: center;">
            <div class="task-detail-pill-label">NOTICE / 提示</div>
            <div class="task-detail-card-content" style="justify-content: center; width: 100%;">
                <div style="text-align: center;">
                    <div style="font-size: 16px; font-weight: bold; color: #2B2B2B; opacity: 0.4;">
                        ATTACHMENTS UNAVAILABLE
                    </div>
                    <div style="font-size: 12px; color: #E65D25; margin-top: 4px; font-weight: bold;">
                        仅在创建成功后才能添加附件
                    </div>
                </div>
            </div>
         </div>
      </div>
    </div>

    <!-- 底部操作栏：提交按钮 -->
    <div class="task-detail-create-footer">
       <div class="task-detail-submit-btn" @click="submitCreateTask">
          <div class="task-detail-submit-count">Action</div>
          <div class="task-detail-submit-text">Create Task</div>
       </div>
    </div>

    <assign-drawer
      :task-group-id="newTask.taskGroupId || groupId"
      :current-user-id="currentUserId"
      :visible.sync="assignDrawerVisible"
      :initial-selected-ids="selectedAssignees.map(u => String(u.userId))"
      @confirm="onAssignConfirm"
    />
  </div>
</template>

<script>
import { createTask } from '@/api/task-api'
import { getMe } from '@/api/user-api'
import { getTaskGroupMembers } from '@/api/taskgroup-api'
import AssignDrawer from './AssignDrawer.vue'
import defaultAvatar from '@/assets/imgs/default-user-avatar.png'
import { taskStatusByCode, taskPriorityByCode, priorityColors } from '@/utils/enums'
import { inMapKeys, compareDateStrings } from '@/utils/validation'

const priorityOptions = Object.keys(taskPriorityByCode).map(k => ({
  label: taskPriorityByCode[k],
  value: Number(k),
  color: priorityColors[k] || '#909399'
}))

function mapToUserStruct(u) {
  if (!u) return null
  return {
    userId: u.userId || u.id,
    userName: u.userName || u.name || u.username || ('User' + (u.userId || u.id)),
    userAvatar: u.userAvatar || u.avatar || ''
  }
}

export default {
  name: 'TaskCreate',
  components: { AssignDrawer },
  props: {
    groupId: { type: [String, Number], default: '' },
    taskGroup: { type: Object, default: () => ({}) },
    currentUserId: { type: [String, Number], default: '' }
  },
  data () {
    return {
      defaultAvatar,
      assignees: [], // Task Group Members (Objects)
      selectedAssignees: [], // Selected Collaborators (Objects)
      assignDrawerVisible: false,
      loading: false,
      taskStatusByCode,
      priorityOptions,
      newTask: {
        taskGroupId: '',
        taskName: '',
        taskStatus: 2,
        startTime: null,
        dueTime: null,
        finishTime: null,
        taskPriority: 2,
        taskDescription: '',
        childTasks: []
      },
      dragSubtaskIndex: null,
      dragOverSubtaskIndex: null,
      isGroupOverflow: false,
      isTeamOverflow: false,
      tempChildDate: null
    }
  },
  computed: {
    /**
     * 开始时间日期选择器的配置选项
     * 包含 cellClassName 方法用于自定义日历单元格样式
     * 在开始日期选择器中，需要标注开始日期、结束日期和中间日期
     */
    newStartPickerOptions () {
      const self = this
      return {
        cellClassName (date) {
          return self.getDateCellClassNameForStartPicker(date)
        }
      }
    },
    
    /**
     * 截止时间日期选择器的配置选项
     * 包含 cellClassName 方法用于自定义日历单元格样式
     * 在结束日期选择器中，需要标注开始日期、结束日期和中间日期
     */
    newDuePickerOptions () {
      const self = this
      return {
        cellClassName (date) {
          return self.getDateCellClassNameForDuePicker(date)
        }
      }
    },
    currentTeamId () {
      const group = this.taskGroup
      return group ? group.teamId : ''
    },
    selectedAssigneeNames () {
      if (!this.selectedAssignees.length) return ''
      return this.selectedAssignees.map(u => u.userName).join(', ')
    },
    currentGroupName () {
      const group = this.taskGroup
      if (!group || !group.taskGroupId) return 'PENDING'
      return group.groupAlias || group.taskGroupName || group.groupName || 'UNNAMED'
    },
    currentTeamName () {
      const group = this.taskGroup
      if (!group || !group.taskGroupId) return 'PENDING'
      return group.teamName || 'PRIVATE PROJECT'
    },
    
    /**
     * 计算任务描述的长度
     * @returns {number} 任务描述文本的长度
     */
    taskDescriptionLength () {
      return (this.newTask.taskDescription || '').length
    },
    /**
     * 计算距离截止日期的天数并生成状态文本
     * @returns {string} 状态文本：距离截止还有 N 天 或 已超时 N 天
     */
    dueDateStatusText () {
      const dueTime = this.newTask.dueTime
      if (!dueTime) {
        return 'CREATE NEW TASK AND SET THE OPTIONS. THEN GO TO WORK.'
      }
      
      // 获取今天的日期字符串
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const today = `${y}-${m}-${d}`
      
      // 计算日期差
      const dueDate = new Date(dueTime)
      const todayDate = new Date(today)
      
      // 设置时间为 0，只比较日期
      dueDate.setHours(0, 0, 0, 0)
      todayDate.setHours(0, 0, 0, 0)
      
      // 计算天数差（毫秒转天数）
      const diffTime = dueDate.getTime() - todayDate.getTime()
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      if (diffDays < 0) {
        // 已超时
        return `已超时 ${Math.abs(diffDays)} 天`
      } else if (diffDays === 0) {
        // 今天截止
        return '距离截止还有 0 天'
      } else {
        // 未超时
        return `距离截止还有 ${diffDays} 天`
      }
    },
    
    subtaskCandidateUsers () {
      // Users available for subtask assignment: Selected Helpers + Current User
      const candidates = [...this.selectedAssignees]
      // Add current user if not already in list
      if (this.currentUserId) {
        const alreadyIn = candidates.find(u => String(u.userId) === String(this.currentUserId))
        if (!alreadyIn) {
             candidates.push({
                userId: this.currentUserId,
                userName: 'Me', // Fallback
                userAvatar: '' // Fallback
             })
        }
      }
      return candidates
    },
    currentPriorityLabel () {
      const p = this.newTask.taskPriority
      // 0-P0, 1-P1, 2-P2, 3-P3, 4-P4
      if (p === 0) return 'P0'
      if (p === 1) return 'P1'
      if (p === 2) return 'P2'
      if (p === 3) return 'P3'
      if (p === 4) return 'P4'
      return '##'
    },
    currentPriorityColor () {
      const p = this.newTask.taskPriority
      // Theme Colors:
      // P0: Alarm Burgundy (Red-ish)
      // P1: Mission Rust (Orange-ish)
      // P2: Avionics Mustard (Yellow-ish)
      // P3: Oxidized Sage (Green-ish)
      // P4: Space Black (Dark)
      if (p === 0) return '#802520'
      if (p === 1) return '#B2653B'
      if (p === 2) return '#BA8530'
      if (p === 3) return '#5C7F71'
      if (p === 4) return '#181818'
      return '#181818'
    },
    currentPriorityNumber () {
      const p = this.newTask.taskPriority
      if (p >= 0 && p <= 4) return String(p)
      return '#'
    },
    /**
     * 获取头部状态按钮的状态样式类
     * @returns {string} 状态类名：task-detail-disabled、task-detail-completed、task-detail-timeout、task-detail-process
     */
    headerStatusState () {
      const status = Number(this.newTask.taskStatus)
      const due = this.newTask.dueTime
      
      // Disabled: 0 (Disabled), 4 (Cancelled)
      if (status === 0 || status === 4) return 'task-detail-disabled'
      
      // Completed: 3 (Completed)
      if (status === 3) return 'task-detail-completed'
      
      // Timeout: 1 (Not Started), 2 (In Progress) AND due < today
      if ((status === 1 || status === 2) && due) {
        const now = new Date()
        const y = now.getFullYear()
        const m = String(now.getMonth() + 1).padStart(2, '0')
        const d = String(now.getDate()).padStart(2, '0')
        const today = `${y}-${m}-${d}`
        
        if (compareDateStrings(due, today) < 0) {
          return 'task-detail-timeout'
        }
      }
      
      // Process: 1 (Not Started), 2 (In Progress)
      return 'task-detail-process'
    },
    /**
     * 获取头部状态按钮的文本
     * @returns {string} 状态文本：Disabled、Completed、Timeout、Process
     */
    headerStatusText () {
      const state = this.headerStatusState
      if (state === 'task-detail-disabled') return 'Disabled'
      if (state === 'task-detail-completed') return 'Completed'
      if (state === 'task-detail-timeout') return 'Timeout'
      return 'Process'
    },
    /**
     * 获取任务名称输入框的状态样式类
     * @returns {string} 状态类名：task-detail-completed、task-detail-timeout 或空字符串
     */
    /**
     * 获取任务名称输入框的状态样式类
     * @returns {string} 状态类名：task-detail-completed、task-detail-timeout 或空字符串
     */
    taskNameStatusClass () {
      const state = this.headerStatusState
      if (state === 'task-detail-completed') return 'task-detail-completed'
      if (state === 'task-detail-timeout') return 'task-detail-timeout'
      return '' // Default (Process/Disabled) - No special style
    },
    isStatusClickable () {
      const status = Number(this.newTask.taskStatus)
      // Allow toggle between Process (1, 2) and Completed (3)
      // Disable for Disabled (0) and Cancelled (4)
      return status === 1 || status === 2 || status === 3
    },
  },
  watch: {
    currentUser: {
      immediate: true,
      handler (val) {
        if (val && val.id) {
          const user = mapToUserStruct(val)
          const uid = String(user.userId)
          // Initialize with current user if not present in selected
          if (!this.selectedAssignees.some(u => String(u.userId) === uid)) {
            this.selectedAssignees.push(user)
          }
        }
      }
    },
    'newTask.taskGroupId': {
      handler (val) {
        if (val) {
          this.fetchGroupMembers()
        }
      },
      immediate: true
    },
    groupId: {
      handler (val) {
        if (val) this.newTask.taskGroupId = String(val)
      },
      immediate: true
    },
    'newTask.taskName': {
      handler() {
        this.$nextTick(() => {
          this.adjustInputSize();
        });
      }
    },
    'newTask.startTime': {
      handler (val, oldVal) {
        if (val === oldVal) return

        // 校验子任务
        if (val && this.newTask.childTasks.length > 0) {
          for (const ct of this.newTask.childTasks) {
            if (ct.dueDate) {
              // 1. 子任务截止不能早于主任务开始
              if (compareDateStrings(ct.dueDate, val) < 0) {
                this.$message.warning(`操作拒绝：子任务截止日期 (${ct.dueDate}) 早于新的开始日期`)
                this.$nextTick(() => {
                  this.newTask.startTime = oldVal
                })
                return
              }
              // 2. 如果新的开始日期会导致截止日期自动调整（Start > Due -> Due = Start），
              //    则需要确保子任务截止日期不晚于新的截止日期（即 Start）
              const currentDue = this.newTask.dueTime
              if (currentDue && compareDateStrings(val, currentDue) > 0) {
                 if (compareDateStrings(ct.dueDate, val) > 0) {
                    this.$message.warning(`操作拒绝：自动调整后的截止日期 (${val}) 将早于子任务截止日期 (${ct.dueDate})`)
                    this.$nextTick(() => {
                      this.newTask.startTime = oldVal
                    })
                    return
                 }
              }
            }
          }
        }

        this.checkDateConsistency()
        this.updateTaskStatusAuto()
      }
    },
    'newTask.dueTime': {
      handler (val, oldVal) {
        if (val === oldVal) return

        if (val && this.newTask.childTasks.length > 0) {
           // 计算有效截止日期（考虑 checkDateConsistency 会自动修正 Start > Due 的情况）
           let effectiveDue = val
           if (this.newTask.startTime && compareDateStrings(this.newTask.startTime, val) > 0) {
              effectiveDue = this.newTask.startTime
           }

           for (const ct of this.newTask.childTasks) {
             if (ct.dueDate) {
                if (compareDateStrings(ct.dueDate, effectiveDue) > 0) {
                   this.$message.warning(`操作拒绝：子任务截止日期 (${ct.dueDate}) 晚于新的截止日期`)
                   this.$nextTick(() => {
                      this.newTask.dueTime = oldVal
                   })
                   return
                }
             }
           }
        }

        this.checkDateConsistency()
        this.updateTaskStatusAuto()
      }
    }
  },
  created () {
    this.adjustInputSize()
    this.setDefaultDates()
  },
  mounted() {
    this.initCurrentUser()
    this.$nextTick(() => {
      this.adjustInputSize()
      this.checkMarquee()
    })
  },
  methods: {
    /**
     * 格式化日期为 yyyy-MM-dd
     * @param {Date} d - 日期对象
     * @returns {string} 格式化后的日期字符串
     */
    formatDateToString (d) {
      const y = d.getFullYear()
      const m = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      return `${y}-${m}-${day}`
    },
    
    /**
     * 获取开始日期选择器单元格的自定义类名
     * 在开始日期选择器中，需要标注开始日期、结束日期和中间日期
     * @param {Date} date - 日期对象
     * @returns {string} 类名字符串
     */
    getDateCellClassNameForStartPicker (date) {
      if (!date) return ''
      
      const startTime = this.newTask.startTime
      const dueTime = this.newTask.dueTime
      
      const cellDateStr = this.formatDateToString(date)
      const classes = []
      
      // 检查是否是开始时间（绿色背景）
      if (startTime && cellDateStr === startTime) {
        classes.push('task-detail-date-start')
      }
      
      // 检查是否是结束时间（红色背景）
      if (dueTime && cellDateStr === dueTime) {
        classes.push('task-detail-date-end')
      }
      
      // 检查是否在开始时间和结束时间之间（中间时间，橙色背景）
      if (startTime && dueTime) {
        const start = new Date(startTime)
        const end = new Date(dueTime)
        const cell = new Date(cellDateStr)
        
        // 确保日期比较时不包含时间部分
        start.setHours(0, 0, 0, 0)
        end.setHours(0, 0, 0, 0)
        cell.setHours(0, 0, 0, 0)
        
        // 如果日期在开始和结束之间（不包括开始和结束本身）
        if (cell > start && cell < end) {
          classes.push('task-detail-date-range')
        }
      }
      
      return classes.join(' ')
    },
    
    /**
     * 获取子任务日期选择器的配置选项
     * 用于标注主任务的开始日期、结束日期和中间日期
     * @param {Object} ct - 子任务对象
     * @returns {Object} picker-options 配置对象
     */
    getChildTaskDatePickerOptions (ct) {
      const self = this
      return {
        cellClassName (date) {
          return self.getChildTaskDateCellClassName(date)
        }
      }
    },
    
    /**
     * 获取子任务日期选择器单元格的自定义类名
     * 标注主任务的开始日期、结束日期和中间日期
     * @param {Date} date - 日期对象
     * @returns {string} 类名字符串
     */
    getChildTaskDateCellClassName (date) {
      if (!date) return ''
      
      const startTime = this.newTask.startTime
      const dueTime = this.newTask.dueTime
      
      const cellDateStr = this.formatDateToString(date)
      const classes = []
      
      // 检查是否是主任务的开始时间（绿色背景）
      if (startTime && cellDateStr === startTime) {
        classes.push('task-detail-date-start')
      }
      
      // 检查是否是主任务的结束时间（红色背景）
      if (dueTime && cellDateStr === dueTime) {
        classes.push('task-detail-date-end')
      }
      
      // 检查是否在主任务开始时间和结束时间之间（中间时间，橙色背景，30%不透明度）
      if (startTime && dueTime) {
        const start = new Date(startTime)
        const end = new Date(dueTime)
        const cell = new Date(cellDateStr)
        
        // 确保日期比较时不包含时间部分
        start.setHours(0, 0, 0, 0)
        end.setHours(0, 0, 0, 0)
        cell.setHours(0, 0, 0, 0)
        
        // 如果日期在开始和结束之间（不包括开始和结束本身）
        if (cell > start && cell < end) {
          classes.push('task-detail-date-range')
        }
      }
      
      return classes.join(' ')
    },
    
    /**
     * 获取结束日期选择器单元格的自定义类名
     * 在结束日期选择器中，需要标注开始日期、结束日期和中间日期
     * @param {Date} date - 日期对象
     * @returns {string} 类名字符串
     */
    getDateCellClassNameForDuePicker (date) {
      if (!date) return ''
      
      const startTime = this.newTask.startTime
      const dueTime = this.newTask.dueTime
      
      const cellDateStr = this.formatDateToString(date)
      const classes = []
      
      // 检查是否是开始时间（绿色背景）
      if (startTime && cellDateStr === startTime) {
        classes.push('task-detail-date-start')
      }
      
      // 检查是否是结束时间（红色背景）
      if (dueTime && cellDateStr === dueTime) {
        classes.push('task-detail-date-end')
      }
      
      // 检查是否在开始时间和结束时间之间（中间时间，橙色背景）
      if (startTime && dueTime) {
        const start = new Date(startTime)
        const end = new Date(dueTime)
        const cell = new Date(cellDateStr)
        
        // 确保日期比较时不包含时间部分
        start.setHours(0, 0, 0, 0)
        end.setHours(0, 0, 0, 0)
        cell.setHours(0, 0, 0, 0)
        
        // 如果日期在开始和结束之间（不包括开始和结束本身）
        if (cell > start && cell < end) {
          classes.push('task-detail-date-range')
        }
      }
      
      return classes.join(' ')
    },
    
    /**
     * 获取子任务状态样式类
     * @param {Object} ct - 子任务对象
     * @returns {string} 状态类名：task-detail-completed、task-detail-timeout 或空字符串
     */
    getChildTaskStatusClass (ct) {
      const status = String(ct.status)
      // Completed (3)
      if (status === '3') return 'task-detail-completed'
      
      // Timeout: Process (1, 2) AND due < today
      if ((status === '1' || status === '2') && ct.dueDate) {
         const now = new Date()
         const y = now.getFullYear()
         const m = String(now.getMonth() + 1).padStart(2, '0')
         const d = String(now.getDate()).padStart(2, '0')
         const today = `${y}-${m}-${d}`
         
         if (compareDateStrings(ct.dueDate, today) < 0) {
            return 'task-detail-timeout'
         }
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
      if (cls === 'task-detail-completed') return 'Completed'
      if (cls === 'task-detail-timeout') return 'Timeout'
      return 'Process'
    },

    toggleStatus () {
      if (!this.isStatusClickable) return
      
      const current = Number(this.newTask.taskStatus)
      
      // If Process (1, 2) -> Completed (3)
      if (current === 1 || current === 2) {
        this.newTask.taskStatus = 3
        // If needed, set finishTime
        if (!this.newTask.finishTime) {
          this.newTask.finishTime = new Date().toISOString()
        }
      } 
      // If Completed (3) -> Process (1 or 2)
      else if (current === 3) {
        let nextStatus = 2 // Default: In Progress
        
        const start = this.newTask.startTime
        if (start) {
          const now = new Date()
          const y = now.getFullYear()
          const m = String(now.getMonth() + 1).padStart(2, '0')
          const d = String(now.getDate()).padStart(2, '0')
          const today = `${y}-${m}-${d}`
          
          // If start > today, then Not Started (1)
          if (compareDateStrings(start, today) > 0) {
            nextStatus = 1
          }
        }

        this.newTask.taskStatus = nextStatus
        // Clear finishTime
        this.newTask.finishTime = null
      }
    },
    // ==========================================
    // 初始化与生命周期相关函数
    // ==========================================

    /**
     * 检查文本是否溢出以启用跑马灯效果
     * 在数据更新后调用，计算容器与内容的宽度差
     */
    checkMarquee () {
      this.$nextTick(() => {
        this.checkOverflow('groupWrapper', 'groupText', 'isGroupOverflow')
        this.checkOverflow('teamWrapper', 'teamText', 'isTeamOverflow')
      })
    },

    /**
     * 检查特定元素的宽度溢出情况
     * @param {String} wrapperRef - 容器的 ref 名称
     * @param {String} textRef - 文本内容的 ref 名称
     * @param {String} dataKey - 用于存储溢出状态的 data 属性名
     */
    checkOverflow (wrapperRef, textRef, dataKey) {
      const wrapper = this.$refs[wrapperRef]
      const text = this.$refs[textRef]
      if (wrapper && text) {
        this[dataKey] = text.offsetWidth > wrapper.clientWidth
      }
    },

    /**
     * 获取任务组的所有成员列表
     * 用于在分配任务时提供候选人
     */
    async fetchGroupMembers () {
      const gid = this.newTask.taskGroupId || this.groupId
      if (!gid) return
      try {
        const res = await getTaskGroupMembers(gid)
        let list = []
        if (Array.isArray(res)) {
           list = res
        } else if (res && Array.isArray(res.data)) {
           list = res.data
        } else if (res && (res.list || res.records)) {
           list = res.list || res.records
        }
        this.assignees = list.map(mapToUserStruct)
      } catch (e) {
        console.error('Failed to fetch group members', e)
      }
    },

    /**
     * 初始化当前用户信息
     * 确保当前用户在协助人列表中，以便默认分配给自己
     */
    async initCurrentUser () {
      if (!this.currentUserId) return
      
      try {
        const res = await getMe()
        if (res) {
          const user = mapToUserStruct(res)
          if (!this.selectedAssignees.some(u => String(u.userId) === String(user.userId))) {
             this.selectedAssignees.push(user)
          }
        }
      } catch (e) {
        console.error('Failed to fetch current user info', e)
        // 降级处理：如果获取失败，创建一个临时的当前用户对象
        if (!this.selectedAssignees.some(u => String(u.userId) === String(this.currentUserId))) {
           this.selectedAssignees.push({ userId: this.currentUserId, userName: 'Me', userAvatar: '' })
        }
      }
    },

    /**
     * 设置默认日期（预留函数）
     * 可用于初始化开始时间和截止时间
     */
    setDefaultDates () {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      this.newTask.startTime = `${y}-${m}-${d}`
    },

    /**
     * 校验日期一致性
     * 当截止日期小于开始日期时，自动修正并提示
     */
    checkDateConsistency () {
      if (this.newTask.startTime && this.newTask.dueTime) {
        if (compareDateStrings(this.newTask.startTime, this.newTask.dueTime) > 0) {
          this.newTask.dueTime = this.newTask.startTime
          this.$message.warning('主任务截止日期必须大于开始日期')
        }
      }
    },

    /**
     * 判断状态选项是否应被禁用
     * @param {Number} statusVal 状态值
     */
    isStatusOptionDisabled (statusVal) {
      const startDate = this.newTask.startTime
      if (!startDate) return false
      
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const todayStr = `${y}-${m}-${d}`
      const cmp = compareDateStrings(todayStr, startDate)

      // 1: 未开始
      // 如果 当前日期 >= 开始日期，则禁用 "未开始"
      if (statusVal === 1) {
        return cmp >= 0
      }

      // 2: 进行中
      // 如果 当前日期 < 开始日期，则禁用 "进行中"
      if (statusVal === 2) {
        return cmp < 0
      }

      return false
    },

    /**
     * 根据日期自动更新任务状态
     * 规则：
     * 1. 如果状态是禁用/完成/取消，则不变更
     * 2. 当前日期 < 开始日期 -> 未开始
     * 3. 当前日期 >= 开始日期 -> 进行中
     */
    updateTaskStatusAuto () {
      const currentStatus = Number(this.newTask.taskStatus)
      // 0:已禁用, 3:已完成, 4:已取消
      if ([0, 3, 4].includes(currentStatus)) return

      const startDate = this.newTask.startTime
      if (!startDate) return

      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const todayStr = `${y}-${m}-${d}`

      const cmp = compareDateStrings(todayStr, startDate)
      
      if (cmp < 0) {
        if (currentStatus !== 1) this.newTask.taskStatus = 1
      } else {
        if (currentStatus !== 2) this.newTask.taskStatus = 2
      }
    },

    // ==========================================
    // UI 交互与显示逻辑
    // ==========================================

    /**
     * 动态调整任务名称输入框的大小
     * 模拟 textarea 的 auto-size 行为，支持 Canvas 文本测量
     */
    adjustInputSize() {
      const el = this.$refs.taskNameInput;
      if (!el) return;

      // 创建临时 Canvas 上下文用于测量文本宽度
      if (!this.canvasCtx) {
        const canvas = document.createElement('canvas');
        this.canvasCtx = canvas.getContext('2d');
      }
      
      // 获取计算样式以匹配字体设置
      const computedStyle = window.getComputedStyle(el);
      this.canvasCtx.font = computedStyle.font;
      const letterSpacing = parseFloat(computedStyle.letterSpacing) || 0;
      
      const text = this.newTask.taskName || 'ADD_NEW_TASK_NAME';
      
      // 按行分割以处理多行宽度的计算
      const lines = text.split('\n');
      let maxWidth = 0;
      
      lines.forEach(line => {
        const metrics = this.canvasCtx.measureText(line);
        // 手动补偿 letter-spacing，因为 canvas measureText 通常不包含它
        const spacingAdjustment = (line.length) * letterSpacing;
        const lineWidth = metrics.width + spacingAdjustment;
        
        if (lineWidth > maxWidth) {
          maxWidth = lineWidth;
        }
      });
      
      // 添加缓冲宽度防止换行抖动
      maxWidth += 30;
      
      // 限制最小和最大宽度
      const minWidth = 250;
      const finalWidth = Math.max(minWidth, maxWidth);
      
      el.style.width = finalWidth + 'px';
      
      // 自动调整高度
      el.style.height = 'auto';
      el.style.height = el.scrollHeight + 'px';
    },


    /**
     * 根据用户 ID 获取用户名称
     */
    getUserName (uid) {
      const u = this.assignees.find(m => String(m.userId) === String(uid)) || this.selectedAssignees.find(m => String(m.userId) === String(uid))
      return u ? u.userName : ('User' + uid)
    },

    /**
     * 根据用户 ID 获取用户头像
     */
    getUserAvatar (uid) {
      const u = this.assignees.find(m => String(m.userId) === String(uid)) || this.selectedAssignees.find(m => String(m.userId) === String(uid))
      return u ? u.userAvatar : ''
    },

    /**
     * 获取子任务执行人的头像
     * 优先从候选人列表中查找
     */
    getAssigneeAvatar (userId) {
       const user = this.subtaskCandidateUsers.find(u => String(u.userId) === String(userId))
       return (user && user.userAvatar) ? user.userAvatar : defaultAvatar
    },

    /**
     * 获取主任务标题的状态样式类
     */
    taskTitleStatusClass (status, dueTime) {
      if (status === '3') return 'is-completed'
      return ''
    },

    /**
     * 获取不同优先级的圆点颜色
     */
    dotColor (status) {
      const s = String(status || '1')
      if (s === '3') return '#67C23A' // 完成 - 绿色
      if (s === '2') return '#409EFF' // 进行中 - 蓝色
      return '#909399' // 其他 - 灰色
    },

    /**
     * 获取子任务标题的样式类
     */
    childTitleClass (ct) {
      if (String(ct.status) === '3') return 'is-completed'
      return ''
    },

    // ==========================================
    // 数据操作与协助人管理
    // ==========================================

    /**
     * 从选中列表中移除协助人
     */
    removeAssignee (uid) {
      const idx = this.selectedAssignees.findIndex(u => String(u.userId) === String(uid))
      if (idx > -1) {
        this.selectedAssignees.splice(idx, 1)
      }
    },

    /**
     * 协助人选择抽屉确认回调
     */
    onAssignConfirm (users) {
      this.selectedAssignees = users.map(mapToUserStruct)
    },

    // ==========================================
    // 子任务管理逻辑
    // ==========================================

    /**
     * 添加一个新的空白子任务
     */
    addChildTask () {
      this.newTask.childTasks.push({
        _lid: Date.now(),
        name: '',
        assigneeId: this.currentUserId || '',
        status: '1',
        dueDate: this.newTask.dueTime || '',
        finishTime: ''
      })
    },

    /**
     * 移除指定索引的子任务
     */
    removeChildTask (idx) {
      this.newTask.childTasks.splice(idx, 1)
    },

    /**
     * 切换子任务的完成状态 (1 <-> 3)
     */
    toggleChildStatus (ct) {
       ct.status = String(ct.status) === '3' ? '1' : '3'
    },

    /**
     * 子任务日期聚焦时记录旧值
     */
    onChildDateFocus (ct) {
      this.tempChildDate = ct.dueDate
    },

    /**
     * 子任务日期变更校验
     */
    onChildDateChange (ct) {
      const childDue = ct.dueDate
      // 允许清空
      if (!childDue) return

      const mainStart = this.newTask.startTime
      const mainDue = this.newTask.dueTime

      // 1. 不能早于主任务开始
      if (mainStart && compareDateStrings(childDue, mainStart) < 0) {
        this.$message.warning('子任务截止日期不能早于主任务开始日期')
        this.$nextTick(() => {
           ct.dueDate = this.tempChildDate
        })
        return
      }

      // 2. 不能晚于主任务截止
      if (mainDue && compareDateStrings(childDue, mainDue) > 0) {
        this.$message.warning('子任务截止日期不能晚于主任务截止日期')
        this.$nextTick(() => {
           ct.dueDate = this.tempChildDate
        })
        return
      }
    },

    // ==========================================
    // 拖拽排序逻辑 (Drag & Drop)
    // ==========================================

    /**
     * 开始拖拽子任务
     */
    onSubtaskDragStart (index, evt) {
      this.dragSubtaskIndex = index
      if (evt.dataTransfer) evt.dataTransfer.effectAllowed = 'move'
    },

    /**
     * 拖拽经过其他子任务时，交换位置
     */
    onSubtaskDragOver (index, evt) {
      this.dragOverSubtaskIndex = index
      const from = this.dragSubtaskIndex
      const to = index
      if (from !== to && from !== null) {
         const list = this.newTask.childTasks
         // 移动元素：删除原位置，插入新位置
         const moved = list.splice(from, 1)[0]
         list.splice(to, 0, moved)
         this.dragSubtaskIndex = to
      }
    },

    /**
     * 放置子任务（结束拖拽交互）
     */
    onSubtaskDrop () {
      this.dragOverSubtaskIndex = null
      this.dragSubtaskIndex = null
    },

    /**
     * 拖拽结束清理状态
     */
    onSubtaskDragEnd () {
      this.dragOverSubtaskIndex = null
      this.dragSubtaskIndex = null
    },

    // ==========================================
    // 提交逻辑
    // ==========================================

    /**
     * 提交表单创建任务
     * 包含校验逻辑和数据格式化
     */
    async submitCreateTask () {
      // 1. 基础校验
      // 任务族ID必填
      if (!this.newTask.taskGroupId && !this.groupId) {
         this.$message.warning('未指定任务族')
         return
      }

      // 任务名称
      const name = (this.newTask.taskName || '').trim()
      if (name.length < 2) {
        this.$message.warning('任务名称至少 2 个字符')
        return
      }

      // 任务状态枚举校验
      if (this.newTask.taskStatus && !inMapKeys(taskStatusByCode, this.newTask.taskStatus)) {
         this.$message.warning('非法任务状态')
         return
      }

      // 任务优先级枚举校验
      if (this.newTask.taskPriority && !inMapKeys(taskPriorityByCode, this.newTask.taskPriority)) {
         this.$message.warning('非法任务优先级')
         return
      }

      // 日期校验
      if (!this.newTask.dueTime) {
        this.$message.warning('截止日期不能为空')
        return
      }
      // 开始日期不得晚于截止日期
      if (this.newTask.startTime && this.newTask.dueTime) {
         if (compareDateStrings(this.newTask.startTime, this.newTask.dueTime) > 0) {
            this.$message.warning('开始日期必须不晚于截止日期')
            return
         }
      }
      
      // 子任务日期校验
      for (let i = 0; i < this.newTask.childTasks.length; i++) {
        const ct = this.newTask.childTasks[i]
        if (ct.dueDate) {
          // 必须 >= 主任务开始日期
          if (this.newTask.startTime && compareDateStrings(ct.dueDate, this.newTask.startTime) < 0) {
            this.$message.warning(`子任务 #${i + 1} 截止日期不能早于主任务开始日期`)
            return
          }
          // 必须 <= 主任务截止日期
          if (this.newTask.dueTime && compareDateStrings(ct.dueDate, this.newTask.dueTime) > 0) {
            this.$message.warning(`子任务 #${i + 1} 截止日期不能晚于主任务截止日期`)
            return
          }
        }
      }
      
      this.loading = true
      try {
        // 2. 构造提交载荷
        const payload = {
          taskGroupId: this.newTask.taskGroupId,
          taskName: name,
          taskDescription: this.newTask.taskDescription,
          taskStatus: this.newTask.taskStatus,
          // 转换为 ISO 格式日期字符串 (后端可能需要 T00:00:00)
          startTime: this.newTask.startTime,
          dueTime: this.newTask.dueTime,
          taskPriority: this.newTask.taskPriority,
          helperUserIdList: this.selectedAssignees.map(u => u.userId),
          // 映射子任务列表
          childTaskList: this.newTask.childTasks.map(ct => ({
            childTaskName: ct.name,
            childTaskStatus: ct.status,
            assigneeUserId: ct.assigneeId,
            dueTime: ct.dueDate
          }))
        }
        
        // 3. 发送请求
        await createTask(payload)
        this.$message.success('创建成功')
        this.$emit('created')
      } catch (e) {
        this.$message.error(e.message || '创建失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/task-group-detail/task-create.css';
</style>

