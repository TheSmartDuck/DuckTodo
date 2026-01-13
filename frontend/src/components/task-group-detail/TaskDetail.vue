<template>
  <!-- 任务详情组件根容器 -->
  <div class="task-detail-main-card task-detail-create-card" v-loading="loading" element-loading-background="rgba(255, 255, 255, 0.5)" element-loading-text="LOADING..." element-loading-spinner="el-icon-loading">
    <!-- 终端样式头部区域 -->
    <div class="task-detail-terminal-header">
      <div class="task-detail-header-top">
        <div class="task-detail-terminal-title-container">
          <div ref="taskNameMirror" class="task-detail-terminal-title-mirror"></div>
          <textarea
            ref="taskNameInput"
            v-model="newTask.taskName"
            class="task-detail-terminal-title-input"
            :class="taskNameStatusClass"
            placeholder="ENTER_TASK_NAME"
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
          ID: {{ resolveTaskId(task).padStart(3, '0') }} | Rhine Lab Project
        </div>
      </div>
    </div>

    <div class="task-detail-body task-detail-theme-body">
      <!-- 背景装饰文字 -->
      <div class="task-detail-bg-text">ARKNIGHTS</div>

      <!-- 第一部分：基础信息 -->
      <div class="task-detail-section-header">
         <div class="task-detail-title-block">
            <h1 class="task-detail-main-title">Instruction <span class="task-detail-thin">| 基础信息</span></h1>
            <div class="task-detail-subtitle">{{ dueDateStatusText }}</div>
         </div>
         
         <div class="task-detail-info-grid">
            <!-- 任务分配卡片 -->
            <div class="task-detail-info-card">
               <div class="task-detail-pill-label">ASSIGNMENT / 任务分配</div>
               <div class="task-detail-card-content">
                  <div class="task-detail-input-group">
                     <span class="task-detail-input-label">
                        <div>GROUP</div>
                        <div class="task-detail-sub-label">归属</div>
                     </span>
                     <div class="task-detail-scroll-wrapper" ref="groupWrapper" :class="{ 'is-scrolling': isGroupOverflow }">
                        <div class="task-detail-marquee-track" :class="{ 'animate-marquee': isGroupOverflow }" :style="isGroupOverflow ? { animationName: 'marquee-left' } : {}">
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
                     <div class="task-detail-scroll-wrapper" ref="teamWrapper" :class="{ 'is-scrolling': isTeamOverflow }">
                        <div class="task-detail-marquee-track" :class="{ 'animate-marquee': isTeamOverflow }" :style="isTeamOverflow ? { animationName: 'marquee-left' } : {}">
                           <span ref="teamText">{{ currentTeamName }}</span>
                           <span v-if="isTeamOverflow" class="task-detail-marquee-duplicate">{{ currentTeamName }}</span>
                        </div>
                     </div>
                  </div>
               </div>
            </div>

            <!-- 时间节点卡片 -->
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
                       class="task-detail-clean-picker" 
                       popper-class="task-detail-date-popper" 
                       :picker-options="startPickerOptions"
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
                       class="task-detail-clean-picker" 
                       popper-class="task-detail-date-popper" 
                       :picker-options="duePickerOptions"
                     />
                  </div>
               </div>
            </div>

            <!-- 状态监测卡片 -->
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
                       @change="submitUpdateTask"
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
         
         <!-- 协助人员卡片 -->
         <div class="task-detail-info-card full-width">
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

      <!-- 第二部分：子任务模块 -->
      <div class="task-detail-list-section">
         <div class="task-detail-list-header">
         <div class="task-detail-header-title-container">
            <div class="task-detail-header-square"></div>
            <span class="task-detail-list-title">SUB_TASKS_MODULE / 子任务模块</span>
            <div class="task-detail-header-dashed-line"></div>
         </div>
         <div class="task-detail-add-btn" @click="addDetailChildTask">
            <span>+ ADD NEW</span>
         </div>
      </div>
         
         <div class="task-detail-task-list">
            <!-- 无数据状态展示 -->
            <div v-if="newTask.childTasks.length === 0" class="task-detail-no-data">
               <div class="task-detail-nd-header">
                  <span class="task-detail-nd-label">System.</span>
                  <div class="task-detail-nd-line-container">
                     <div class="task-detail-nd-line-segment seg-1"></div>
                     <div class="task-detail-nd-line-segment seg-2"></div>
                     <div class="task-detail-nd-line-segment seg-3"></div>
                  </div>
               </div>
               <div class="task-detail-nd-title">NO DATA AVAILABLE</div>
            </div>
            <!-- 子任务列表项 -->
            <div 
               v-for="(ct, idx) in newTask.childTasks" 
               :key="ct._lid || ct.childTaskId || idx"
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
                  <div class="task-detail-tri-bar bar-mustard"></div>
                  <div class="task-detail-tri-bar bar-sage"></div>
                  <div class="task-detail-tri-bar bar-burgundy"></div>
               </div>
               
               <!-- 子任务主体内容 -->
               <div class="task-detail-strip-body">
                  <!-- 第一行：状态胶囊、索引编号、字数统计 -->
                  <div class="task-detail-strip-row-1">
                     <div class="task-detail-status-pill" :class="getChildTaskStatusClass(ct)" @click="toggleChildStatus(ct)">
                        {{ getChildTaskStatusText(ct) }}
                     </div>
                     <span class="task-detail-idx">CW-{{ idx + 1 }}</span>
                     <span class="task-detail-char-count">{{ (ct.childTaskName || '').length }}/80</span>
                  </div>
                  
                  <!-- 第二行：子任务名称输入 -->
                  <div class="task-detail-strip-row-2">
                     <span class="task-detail-input-prefix">>>&nbsp;&nbsp;</span>
                     <el-input 
                        type="textarea"
                        v-model="ct.childTaskName" 
                        class="task-detail-strip-name-input" 
                        :class="getChildTaskStatusClass(ct)"
                        placeholder="ENTER OBJECTIVE..." 
                        :autosize="{ minRows: 1, maxRows: 4 }"
                        resize="none"
                        maxlength="80"
                        @blur="onDetailChildNameBlur(ct)"
                     />
                  </div>
                  
                  <!-- 第三行：控制栏 -->
                  <div class="task-detail-strip-row-3">
                     <!-- 截止日期 -->
                     <el-date-picker
                       v-model="ct.dueTime"
                       type="date"
                       placeholder="DDL DATE"
                       class="task-detail-mini-date"
                       value-format="yyyy-MM-dd"
                       :popper-class="'task-detail-date-picker-popper'"
                       :picker-options="getChildTaskDatePickerOptions(ct)"
                       prefix-icon="el-icon-date"
                       size="mini"
                       @focus="onChildDateFocus(ct)"
                       @change="validateChildTaskDueTime(ct, $event)"
                     />
                     
                     <!-- 执行人 -->
                     <div class="task-detail-capsule-selector">
                        <el-select 
                           v-model="ct.assigneeUserId" 
                           class="task-detail-capsule-select" 
                           popper-class="task-detail-dropdown" 
                           placeholder="ASSIGN"
                           size="mini"
                           @change="onDetailChildAssigneeChange(ct)"
                        >
                           <template slot="prefix">
                              <div class="task-detail-capsule-avatar-container">
                                 <img :src="getAssigneeAvatar(ct.assigneeUserId)" class="task-detail-capsule-avatar"/>
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
                     <div class="task-detail-delete-btn" @click="removeDetailChildTask(ct, idx)">×</div>
                  </div>
               </div>
            </div>
         </div>
      </div>

      <!-- 第三部分：任务描述模块 -->
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

      <!-- 第四部分：附件模块 -->
      <div class="task-detail-attachment-section" style="padding: 0 24px 24px;">
         <div class="task-detail-list-header">
            <div class="task-detail-header-title-container">
               <div class="task-detail-header-square" style="background-color: #802520;"></div>
               <span class="task-detail-list-title">ATTACHMENT_MODULE / 附件模块</span>
               <div class="task-detail-header-dashed-line"></div>
            </div>
            <el-upload
              action="#"
              :show-file-list="false"
              :http-request="handleAttachmentUpload"
              accept="*"
              multiple
            >
               <div class="task-detail-add-btn" style="margin-left: 12px;">
                  <span>+ UPLOAD</span>
               </div>
            </el-upload>
         </div>
         
         <div class="task-detail-info-card task-detail-full-width" style="margin-bottom: 0;">
            <div class="task-detail-card-content" style="display: block;">
               <div v-if="attachmentsLoading" style="text-align:center; color:#999; font-family:'Courier Prime'; padding: 20px;">LOADING...</div>
               <div v-else-if="newTask.attachments && newTask.attachments.length" class="task-detail-att-grid">
                   <div 
                      v-for="file in newTask.attachments" 
                      :key="file.taskFileId" 
                      class="task-detail-att-card" 
                      @click="openAttachment(file)"
                   >
                      <!-- Content -->
                      <div class="task-detail-att-content">
                          <div class="task-detail-att-info">
                              <div class="task-detail-att-name" :title="file.taskFileName">{{ file.taskFileName }}</div>
                              <div class="task-detail-att-meta">{{ formatFileSize(file.taskFileSize) }}</div>
                          </div>
                          <div class="task-detail-att-icon-box">
                              <img :src="getFileIcon(file.taskFileName)" />
                          </div>
                      </div>
                      
                      <!-- Deco Bar -->
                      <div class="task-detail-att-bar">
                          <div class="task-detail-att-bar-l"></div>
                          <div class="task-detail-att-bar-r"></div>
                      </div>

                      <!-- Delete Action -->
                      <div class="task-detail-att-del-btn" @click.stop="removeAttachment(file)">
                          <i class="el-icon-close"></i>
                      </div>
                   </div>
               </div>
               <div v-else style="text-align: center;">
                    <div style="font-size: 16px; font-weight: bold; color: #2B2B2B; opacity: 0.4;">
                        NO ATTACHMENTS
                    </div>
               </div>
            </div>
         </div>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="task-detail-create-footer">
       <div class="task-detail-submit-btn" @click="submitUpdateTask">
          <div class="task-detail-submit-count">Action</div>
          <div class="task-detail-submit-text">Update Task</div>
       </div>
       <div class="task-detail-submit-btn" @click="confirmDeleteTask" style="margin-left: 12px; background: #802520; box-shadow: 4px 4px 0 rgba(128, 37, 32, 0.2);">
          <div class="task-detail-submit-count">Danger</div>
          <div class="task-detail-submit-text">Delete</div>
       </div>
    </div>

    <assign-drawer
      :task-group-id="newTask.taskGroupId || taskGroupId"
      :current-user-id="currentUserId"
      :visible.sync="assignDrawerVisible"
      :initial-selected-ids="selectedAssignees.map(u => String(u.userId))"
      @confirm="onAssignConfirm"
    />

    <!-- Upload Progress Overlay -->
    <div v-if="uploadVisible" class="task-detail-upload-overlay">
      <div class="task-detail-upload-box">
        <div class="task-detail-upload-title">UPLOADING...</div>
        <div class="task-detail-upload-progress-track">
          <div class="task-detail-upload-progress-bar" :style="{ width: uploadProgress + '%' }"></div>
        </div>
        <div class="task-detail-upload-percent">{{ uploadProgress }}%</div>
      </div>
    </div>
  </div>
</template>

<script>
import { updateTask, updateChildTask, addChildTask, deleteChildTask, addTaskHelper, deleteTaskHelper, getTaskDetail, uploadTaskFile, deleteTaskFile, deleteTask } from '@/api/task-api'
import { getTaskGroupMembers } from '@/api/taskgroup-api'
import AssignDrawer from './AssignDrawer.vue'
import defaultAvatar from '@/assets/imgs/default-user-avatar.png'
import { taskStatusByCode, taskPriorityByCode, priorityColors } from '@/utils/enums'
import { compareDateStrings } from '@/utils/validation'

const priorityOptions = Object.keys(taskPriorityByCode).map(k => ({
  label: taskPriorityByCode[k],
  value: Number(k),
  color: priorityColors[k] || '#909399'
}))

function mapToUserStruct(u) {
  if (!u) return null
  return {
    userId: u.userId || u.id || u.userID,
    userName: u.userName || u.name || u.username || ('User' + (u.userId || u.id)),
    userAvatar: u.userAvatar || u.avatar || ''
  }
}

export default {
  name: 'TaskDetail',
  components: { AssignDrawer },
  props: {
    taskId: { type: String, default: '' },
    taskGroupId: { type: String, default: '' },
    currentUserId: { type: [String, Number], default: '' }
  },
  data () {
    return {
      task: null,
      canEditCurrentTask: true,
      defaultAvatar,
      assignees: [], 
      selectedAssignees: [], 
      assignDrawerVisible: false,
      loading: false,
      uploadVisible: false,
      uploadProgress: 0,
      attachmentsLoading: false,
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
        childTasks: [],
        attachments: []
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
     * 在开始日期选择器中，需要标注结束日期和中间日期
     */
    startPickerOptions () {
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
     * 在结束日期选择器中，需要标注开始日期和中间日期
     */
    duePickerOptions () {
      const self = this
      return {
        cellClassName (date) {
          return self.getDateCellClassNameForDuePicker(date)
        }
      }
    },
    resolvedTaskGroup () {
       // Since we don't have myTaskGroups prop anymore, we rely on fetched data or just IDs
       // If we need group name, we might need to fetch group details separately or rely on task detail returning it
       return { 
         taskGroupId: this.newTask.taskGroupId || this.taskGroupId,
         // These might be missing if not fetched, but task detail might provide them
         groupName: 'LOADING...',
         teamName: 'LOADING...'
       }
    },
    
    /**
     * 计算距离截止日期的天数并生成状态文本
     * @returns {string} 状态文本：距离截止还有 N 天 或 已超时 N 天
     */
    dueDateStatusText () {
      const dueTime = this.newTask.dueTime
      if (!dueTime) {
        return 'UPDATE TASK DETAILS AND TRACK PROGRESS.'
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
    currentTeamId () {
      return '' // We might not need this unless strictly required
    },
    currentGroupName () {
      // Try to get from task detail if available
      if (this.task && this.task.taskGroupName) return this.task.taskGroupName
      if (this.task && this.task.groupName) return this.task.groupName
      return 'LOADING...'
    },
    currentTeamName () {
      if (this.task && this.task.teamName) return this.task.teamName
      return 'PRIVATE PROJECT'
    },
    
    /**
     * 计算任务描述的长度
     * @returns {number} 任务描述文本的长度
     */
    taskDescriptionLength () {
      return (this.newTask.taskDescription || '').length
    },
    subtaskCandidateUsers () {
      const candidates = [...this.selectedAssignees]
      if (this.currentUserId) {
        const alreadyIn = candidates.find(u => String(u.userId) === String(this.currentUserId))
        if (!alreadyIn) {
             candidates.push({
                userId: this.currentUserId,
                userName: 'Me',
                userAvatar: ''
             })
        }
      }
      return candidates
    },
    currentPriorityLabel () {
      const p = this.newTask.taskPriority
      if (p === 0) return 'P0'
      if (p === 1) return 'P1'
      if (p === 2) return 'P2'
      if (p === 3) return 'P3'
      if (p === 4) return 'P4'
      return '##'
    },
    currentPriorityColor () {
      const p = this.newTask.taskPriority
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
      
      if (status === 0 || status === 4) return 'task-detail-disabled'
      if (status === 3) return 'task-detail-completed'
      if ((status === 1 || status === 2) && due) {
        const now = new Date()
        const y = now.getFullYear()
        const m = String(now.getMonth() + 1).padStart(2, '0')
        const d = String(now.getDate()).padStart(2, '0')
        const today = `${y}-${m}-${d}`
        if (compareDateStrings(due, today) < 0) return 'task-detail-timeout'
      }
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
    taskNameStatusClass () {
      const state = this.headerStatusState
      if (state === 'task-detail-completed') return 'task-detail-completed'
      if (state === 'task-detail-timeout') return 'task-detail-timeout'
      return ''
    },
    isStatusClickable () {
      const status = Number(this.newTask.taskStatus)
      return status === 1 || status === 2 || status === 3
    },
  },
  watch: {
    taskId: {
      handler (val) {
        if (val) {
          this.loadDetail()
        }
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
        this.validateStartTime(val, oldVal)
      }
    },
    'newTask.dueTime': {
      handler (val, oldVal) {
        if (val === oldVal) return
        this.validateDueTime(val, oldVal)
      }
    }
  },
  methods: {
    /**
     * 解析任务ID
     * @param {Object} t - 任务对象（未使用）
     * @returns {string} 任务ID
     */
    resolveTaskId (t) {
      return this.taskId
    },
    
    /**
     * 加载任务详情
     * 从后端获取任务完整信息并填充到表单
     */
    async loadDetail () {
      const tid = this.taskId
      if (!tid) return
      this.loading = true
      try {
        const res = await getTaskDetail(tid)
        // http.js interceptor returns res.data directly if success=true.
        // TaskDetailResponse structure:
        // {
        //   taskId, taskGroupId, taskGroupName, teamId, teamName,
        //   taskName, taskDescription, taskStatus, taskPriority,
        //   startTime, dueTime, finishTime, isOwner,
        //   taskHelperList: [...],
        //   childTaskList: [...],
        //   attachments: [...]
        // }
        const t = (res && res.data) ? res.data : res
        
        if (t) {
          this.task = t // Save full task object
          
          // Populate newTask
          this.newTask.taskGroupId = String(t.taskGroupId || this.taskGroupId || '')
          this.newTask.taskName = t.taskName || ''
          this.newTask.taskStatus = Number(t.taskStatus)
          this.newTask.startTime = t.startTime ? t.startTime.slice(0, 10) : null
          this.newTask.dueTime = t.dueTime ? t.dueTime.slice(0, 10) : null
          this.newTask.finishTime = t.finishTime ? t.finishTime.slice(0, 10) : null
          this.newTask.taskPriority = Number(t.taskPriority)
          this.newTask.taskDescription = t.taskDescription || ''
          
          // Child Tasks: Map from TaskDetailResponse.childTaskList (List<ChildTask>)
          // ChildTask fields: childTaskId, childTaskName, childTaskStatus, childTaskAssigneeId, dueTime...
          const childrenRaw = Array.isArray(t.childTaskList) ? t.childTaskList : []

          this.newTask.childTasks = childrenRaw.map(ct => ({
            ...ct,
            // Standardize fields for frontend usage
            childTaskName: ct.childTaskName || '',
            childTaskStatus: Number(ct.childTaskStatus || 1),
            // Map backend 'childTaskAssigneeId' to frontend 'assigneeUserId'
            assigneeUserId: ct.childTaskAssigneeId || ct.assigneeUserId || null,
            dueTime: ct.dueTime ? ct.dueTime.slice(0, 10) : null
          }))
          
          // Attachments: Map from TaskDetailResponse.attachments (List<TaskFile>)
          this.newTask.attachments = Array.isArray(t.attachments) ? t.attachments : []
          
          // Helpers: Map from TaskDetailResponse.taskHelperList (List<TaskHelper>)
          // TaskHelper fields: userId, userName, userAvatar...
          const helpersRaw = Array.isArray(t.taskHelperList) ? t.taskHelperList : []

          this.selectedAssignees = helpersRaw.map(h => ({
             userId: h.userId,
             userName: h.userName || ('User' + h.userId),
             userAvatar: h.userAvatar || ''
          })).filter(u => u.userId)
          
          this.fetchGroupMembers()
          this.$nextTick(() => {
            this.adjustInputSize()
            this.checkMarquee()
          })
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * UI 辅助方法
     */
    
    /**
     * 检查文本是否溢出以启用跑马灯效果
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
     * 动态调整任务名称输入框的大小
     * 使用镜像元素测量文本宽度
     */
    adjustInputSize() {
      const el = this.$refs.taskNameInput;
      const mirror = this.$refs.taskNameMirror;
      if (!el || !mirror) return;

      // 更新镜像内容
      mirror.textContent = this.newTask.taskName || el.placeholder || '';
      
      // 调整宽度
      const newWidth = mirror.offsetWidth + 20; // 添加缓冲
      el.style.width = newWidth + 'px';

      // 调整高度
      el.style.height = 'auto';
      el.style.height = el.scrollHeight + 'px';
    },
    
    /**
     * 数据获取方法
     */
    
    /**
     * 获取任务组的所有成员列表
     */
    async fetchGroupMembers () {
      const gid = this.resolvedTaskGroup.taskGroupId
      if (!gid) {
        this.assignees = []
        return
      }
      try {
        const res = await getTaskGroupMembers(gid)
        let list = []
        if (Array.isArray(res)) list = res
        else if (res && Array.isArray(res.data)) list = res.data
        else if (res && (res.list || res.records)) list = res.list || res.records
        this.assignees = list.map(mapToUserStruct)
        
        // Check edit permission if user is in group
        // If current user is not in members, they might be owner or it's a private group
        // This is a basic check; adjust if needed based on API response for permissions
        if (this.currentUserId) {
           // Default to true, if we need strict check we can verify against assignees list
           // But assignees list might be empty for new groups or private groups
           this.canEditCurrentTask = true
        }
      } catch (e) {
        console.error(e)
      }
    },
    
    /**
     * 获取执行人头像
     * @param {String|Number} uid - 用户ID
     * @returns {string} 头像URL
     */
    getAssigneeAvatar (uid) {
      const u = this.assignees.find(m => String(m.userId) === String(uid)) || this.selectedAssignees.find(m => String(m.userId) === String(uid))
      return u ? u.userAvatar : ''
    },

    /**
     * 主要更新逻辑
     */
    
    /**
     * 提交任务更新
     */
    async submitUpdateTask () {
      const tid = this.resolveTaskId(this.task)
      if (!tid) return
      try {
        const payload = {
          taskName: this.newTask.taskName,
          taskDescription: this.newTask.taskDescription,
          taskStatus: this.newTask.taskStatus,
          taskPriority: this.newTask.taskPriority,
          startTime: this.newTask.startTime,
          dueTime: this.newTask.dueTime
        }
        await updateTask(tid, payload)
        this.$message.success('更新成功')
        this.$emit('refresh')
      } catch (e) {
        this.$message.error('更新失败: ' + e.message)
      }
    },
    
    /**
     * 确认删除任务
     */
    async confirmDeleteTask () {
      try {
        await this.$confirm('确认删除该任务？', '提示', {
          type: 'warning',
          customClass: 'task-detail-confirm-dialog',
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          confirmButtonClass: 'task-detail-confirm-btn',
          cancelButtonClass: 'task-detail-cancel-btn'
        })
        
        // 调用删除 API
        const tid = this.resolveTaskId(this.task)
        if (!tid) {
          this.$message.error('无法获取任务ID')
          return
        }
        
        this.loading = true
        await deleteTask(tid)
        this.$message.success('任务删除成功')
        
        // 触发删除事件，通知父组件
        this.$emit('delete')
      } catch (e) {
        // 用户取消或删除失败
        if (e !== 'cancel' && e !== 'close') {
          this.$message.error('删除失败: ' + (e.message || '未知错误'))
        }
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 状态逻辑
     */
    
    /**
     * 判断状态选项是否应被禁用
     * @param {Number} statusVal - 状态值
     * @returns {boolean} 是否禁用
     */
    isStatusOptionDisabled (statusVal) {
      // Logic copied from TaskCreate
      const startDate = this.newTask.startTime
      if (!startDate) return false
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      const todayStr = `${y}-${m}-${d}`
      const cmp = compareDateStrings(todayStr, startDate)
      if (statusVal === 1) return cmp >= 0
      if (statusVal === 2) return cmp < 0
      return false
    },
    
    /**
     * 切换任务状态
     * 在 Process (1, 2) 和 Completed (3) 之间切换
     */
    toggleStatus () {
      if (!this.isStatusClickable) return
      const current = Number(this.newTask.taskStatus)
      if (current === 1 || current === 2) {
        this.newTask.taskStatus = 3
        this.submitUpdateTask()
      } else if (current === 3) {
        let nextStatus = 2
        const start = this.newTask.startTime
        if (start) {
          const now = new Date()
          const y = now.getFullYear()
          const m = String(now.getMonth() + 1).padStart(2, '0')
          const d = String(now.getDate()).padStart(2, '0')
          const today = `${y}-${m}-${d}`
          if (compareDateStrings(start, today) > 0) nextStatus = 1
        }
        this.newTask.taskStatus = nextStatus
        this.submitUpdateTask()
      }
    },
    
    /**
     * 协助人管理逻辑
     */
    
    /**
     * 协助人选择确认回调
     * @param {Array} users - 选中的用户列表
     */
    async onAssignConfirm (users) {
      const newIds = users.map(u => String(u.userId || u.id))
      const oldIds = this.selectedAssignees.map(u => String(u.userId))
      
      const toAdd = newIds.filter(id => !oldIds.includes(id))
      const toRemove = oldIds.filter(id => !newIds.includes(id))
      
      const tid = this.resolveTaskId(this.task)
      if (!tid) return
      
      this.loading = true
      try {
        for (const uid of toRemove) await deleteTaskHelper(tid, uid)
        for (const uid of toAdd) await addTaskHelper(tid, uid)
        this.$message.success('协助者更新成功')
        this.loadDetail() // Refresh
      } catch (e) {
        this.$message.error('协助者更新部分失败: ' + e.message)
      } finally {
        this.loading = false
      }
    },
    /**
     * 日期校验逻辑
     */
    
    /**
     * 校验开始时间
     * @param {String} newVal - 新的开始时间
     * @param {String} oldVal - 旧的开始时间
     */
    validateStartTime(newVal, oldVal) {
      if (!newVal) return
      
      // Check 1: startTime <= dueTime
      if (this.newTask.dueTime && compareDateStrings(newVal, this.newTask.dueTime) > 0) {
        this.$message.warning('开始时间不能晚于截止时间')
        this.$nextTick(() => { this.newTask.startTime = oldVal }) 
        return
      }

      // Check 2: startTime <= all subtask dueTimes
      const invalidSubtask = this.newTask.childTasks.find(ct => ct.dueTime && compareDateStrings(newVal, ct.dueTime) > 0)
      if (invalidSubtask) {
        this.$message.warning(`开始时间不能晚于子任务 "${invalidSubtask.childTaskName}" 的截止时间 (${invalidSubtask.dueTime})`)
         this.$nextTick(() => { this.newTask.startTime = oldVal }) 
         return
      }
    },

    /**
     * 校验截止时间
     * @param {String} newVal - 新的截止时间
     * @param {String} oldVal - 旧的截止时间
     */
    validateDueTime(newVal, oldVal) {
       if (!newVal) return

       // Check 1: dueTime >= startTime
       if (this.newTask.startTime && compareDateStrings(this.newTask.startTime, newVal) > 0) {
          this.$message.warning('截止时间不能早于开始时间')
          this.$nextTick(() => { this.newTask.dueTime = oldVal })
          return
       }

       // Check 2: dueTime >= all subtask dueTimes
       const invalidSubtask = this.newTask.childTasks.find(ct => ct.dueTime && compareDateStrings(ct.dueTime, newVal) > 0)
       if (invalidSubtask) {
          this.$message.warning(`截止时间不能早于子任务 "${invalidSubtask.childTaskName}" 的截止时间 (${invalidSubtask.dueTime})`)
          this.$nextTick(() => { this.newTask.dueTime = oldVal })
          return
       }
    },

    /**
     * 子任务日期聚焦时记录旧值
     * @param {Object} ct - 子任务对象
     */
    onChildDateFocus(ct) {
        this.tempChildDate = ct.dueTime
    },

    /**
     * 校验子任务截止时间
     * @param {Object} ct - 子任务对象
     * @param {String} newVal - 新的截止时间
     */
    validateChildTaskDueTime(ct, newVal) {
        if (!newVal) {
            // If cleared, just save
            this.onDetailChildDueDateChange(ct)
            return
        }

        // Check range: startTime <= childDue <= dueTime
        if (this.newTask.startTime && compareDateStrings(this.newTask.startTime, newVal) > 0) {
            this.$message.warning('子任务截止时间不能早于主任务开始时间')
            this.$nextTick(() => { ct.dueTime = this.tempChildDate })
            return
        }
        
        if (this.newTask.dueTime && compareDateStrings(newVal, this.newTask.dueTime) > 0) {
            this.$message.warning('子任务截止时间不能晚于主任务截止时间')
            this.$nextTick(() => { ct.dueTime = this.tempChildDate })
            return
        }

        // If valid, proceed to save
        this.onDetailChildDueDateChange(ct)
    },

    /**
     * 获取子任务状态样式类
     * @param {Object} ct - 子任务对象
     * @returns {string} 状态类名：task-detail-completed、task-detail-timeout 或空字符串
     */
    getChildTaskStatusClass (ct) {
      const status = String(ct.childTaskStatus)
      if (status === '3') return 'task-detail-completed'
      if ((status === '1' || status === '2') && ct.dueTime) {
         const now = new Date()
         const y = now.getFullYear()
         const m = String(now.getMonth() + 1).padStart(2, '0')
         const d = String(now.getDate()).padStart(2, '0')
         const today = `${y}-${m}-${d}`
         if (compareDateStrings(ct.dueTime, today) < 0) return 'task-detail-timeout'
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
      
      // 检查是否在开始时间和结束时间之间（中间时间，橙色背景，大小缩小）
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
      
      // 检查是否在开始时间和结束时间之间（中间时间，橙色背景，大小缩小）
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
     * 添加新的子任务
     * 自动生成名称并调用 API 创建
     */
    async addDetailChildTask () {
      // 根据数量自动生成名称（例如 "CW-1"）
      const count = this.newTask.childTasks.length + 1
      const autoName = `CW-${count}`
      
      const newSub = {
        _lid: Date.now(),
        childTaskName: autoName,
        childTaskStatus: 1,
        dueTime: this.newTask.dueTime || null,
        assigneeUserId: this.currentUserId || null
      }
      
      // Push to list immediately for UI feedback
      this.newTask.childTasks.push(newSub)

      // Automatically call API to create
      const tid = this.resolveTaskId(this.task)
      if (!tid) return

      try {
         const res = await addChildTask({
           taskId: tid,
           childTaskName: newSub.childTaskName,
           childTaskStatus: newSub.childTaskStatus,
           childTaskIndex: count,
           childTaskAssigneeId: newSub.assigneeUserId,
           dueTime: newSub.dueTime,
           finishTime: null
         })
         const data = (res && res.data) ? res.data : res
         if (data) {
           Object.assign(newSub, {
             ...data,
             childTaskName: data.childTaskName || data.name,
             childTaskStatus: data.childTaskStatus || data.status,
             dueTime: data.dueTime || data.dueDate
           })
           this.$message.success('子任务创建成功')
           this.$emit('refresh')
         }
      } catch (e) {
         this.$message.error('子任务自动创建失败')
         // Optionally remove from list if failed? 
         // For now keep it to let user retry or edit.
      }
    },
    
    /**
     * 子任务名称失焦处理
     * 如果名称已更改，调用 API 更新或创建
     * @param {Object} ct - 子任务对象
     */
    async onDetailChildNameBlur (ct) {
      if (!ct.childTaskName || !ct.childTaskName.trim()) return
      const tid = this.resolveTaskId(this.task)
      try {
        if (ct.childTaskId) {
          await updateChildTask(this.taskId, ct.childTaskId, { childTaskName: ct.childTaskName })
          this.$message.success('子任务更新成功')
          this.$emit('refresh')
        } else {
           const res = await addChildTask({
             taskId: tid,
             childTaskName: ct.childTaskName,
             childTaskStatus: ct.childTaskStatus || 1
           })
           const data = (res && res.data) ? res.data : res
           if (data) {
             Object.assign(ct, {
               ...data,
               childTaskName: data.childTaskName || data.name,
               childTaskStatus: data.childTaskStatus || data.status,
               dueTime: data.dueTime || data.dueDate
             })
             this.$message.success('子任务创建成功')
             this.$emit('refresh')
           }
        }
      } catch (e) {
        this.$message.error('子任务操作失败')
      }
    },
    
    /**
     * 切换子任务状态
     * @param {Object} ct - 子任务对象
     */
    async toggleChildStatus (ct) {
      if (!ct.childTaskId) return
      // Toggle logic: 1/2 -> 3, 3 -> 1
      const current = Number(ct.childTaskStatus)
      let next = 1
      if (current === 1 || current === 2) next = 3
      else if (current === 3) next = 1
      
      try {
        await updateChildTask(this.taskId, ct.childTaskId, { childTaskStatus: next })
        ct.childTaskStatus = next
        this.$message.success('子任务状态更新成功')
        this.$emit('refresh')
      } catch (e) {
        this.$message.error('状态更新失败')
      }
    },
    
    /**
     * 子任务截止日期变更处理
     * @param {Object} ct - 子任务对象
     */
    async onDetailChildDueDateChange (ct) {
       if (!ct.childTaskId) return
       try {
         await updateChildTask(this.taskId, ct.childTaskId, { dueTime: ct.dueTime })
         this.$message.success('子任务日期更新成功')
         this.$emit('refresh')
       } catch (e) {
         this.$message.error('日期更新失败')
       }
    },
    
    /**
     * 子任务执行人变更处理
     * @param {Object} ct - 子任务对象
     */
    async onDetailChildAssigneeChange (ct) {
       if (!ct.childTaskId) return
       try {
         await updateChildTask(this.taskId, ct.childTaskId, { assigneeUserId: ct.assigneeUserId })
         this.$message.success('子任务执行人更新成功')
         this.$emit('refresh')
       } catch (e) {
         this.$message.error('执行人更新失败')
       }
    },
    
    /**
     * 删除子任务
     * @param {Object} ct - 子任务对象
     * @param {Number} idx - 子任务索引
     */
    async removeDetailChildTask (ct, idx) {
      if (ct.childTaskId) {
        try {
          await deleteChildTask(this.taskId, ct.childTaskId)
          this.$message.success('子任务删除成功')
          this.$emit('refresh')
        } catch (e) {
          this.$message.error('删除失败')
          return
        }
      }
      this.newTask.childTasks.splice(idx, 1)
    },
    
    /**
     * 拖拽排序逻辑
     */
    
    /**
     * 开始拖拽子任务
     * @param {Number} index - 子任务索引
     * @param {Event} evt - 拖拽事件
     */
    onSubtaskDragStart (index, evt) {
      this.dragSubtaskIndex = index
      if (evt.dataTransfer) evt.dataTransfer.effectAllowed = 'move'
    },
    /**
     * 拖拽经过其他子任务时，交换位置
     * @param {Number} index - 目标索引
     * @param {Event} evt - 拖拽事件
     */
    onSubtaskDragOver (index, evt) {
      this.dragOverSubtaskIndex = index
      const from = this.dragSubtaskIndex
      const to = index
      if (from !== to && from !== null) {
         const list = this.newTask.childTasks
         const moved = list.splice(from, 1)[0]
         list.splice(to, 0, moved)
         this.dragSubtaskIndex = to
         // TODO: Implement Reorder API if backend supports it
      }
    },
    /**
     * 放置子任务（结束拖拽交互）
     * @param {Number} index - 目标索引
     * @param {Event} evt - 拖拽事件
     */
    onSubtaskDrop (index, evt) {
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

    /**
     * 附件管理逻辑
     */
    
    /**
     * 处理附件上传
     * @param {Object} options - 上传选项，包含 file 对象
     */
    async handleAttachmentUpload (options) {
      const { file } = options
      const tid = this.resolveTaskId(this.task)
      if (!tid) return
      try {
        this.uploadVisible = true
        this.uploadProgress = 0
        
        await uploadTaskFile(tid, file, '', (progressEvent) => {
          if (progressEvent.lengthComputable) {
             const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
             this.uploadProgress = percent
          }
        })
        
        this.$message.success('上传成功')
        this.loadDetail()
      } catch (e) {
        this.$message.error('上传失败')
      } finally {
        this.uploadVisible = false
        this.uploadProgress = 0
      }
    },
    /**
     * 删除附件
     * @param {Object} file - 附件对象
     */
    async removeAttachment (file) {
      const tid = this.resolveTaskId(this.task)
      if (!tid) return
      try {
        await deleteTaskFile(tid, file.taskFileId)
        this.loadDetail()
      } catch (e) {
        this.$message.error('删除附件失败')
      }
    },
    /**
     * 打开附件
     * @param {Object} file - 附件对象
     */
    openAttachment(file) {
      if(file.taskFilePath) window.open(file.taskFilePath, '_blank')
    },
    
    /**
     * 根据文件名获取文件图标
     * @param {String} name - 文件名
     * @returns {string} 图标路径
     */
    getFileIcon (name) {
      if (!name) return require('@/assets/imgs/plug_file_icon/other.png')
      const n = String(name).toLowerCase()
      
      if (n.endsWith('.pdf')) return require('@/assets/imgs/plug_file_icon/pdf.png')
      if (n.endsWith('.doc') || n.endsWith('.docx')) return require('@/assets/imgs/plug_file_icon/word.png')
      if (n.endsWith('.xls') || n.endsWith('.xlsx')) return require('@/assets/imgs/plug_file_icon/excel.png')
      if (n.endsWith('.ppt') || n.endsWith('.pptx')) return require('@/assets/imgs/plug_file_icon/ppt.png')
      if (n.endsWith('.txt')) return require('@/assets/imgs/plug_file_icon/txt.png')
      if (n.endsWith('.csv')) return require('@/assets/imgs/plug_file_icon/csv.png')
      if (n.endsWith('.jpg') || n.endsWith('.jpeg')) return require('@/assets/imgs/plug_file_icon/jpg.png')
      if (n.endsWith('.png')) return require('@/assets/imgs/plug_file_icon/png.png')
      if (n.endsWith('.gif')) return require('@/assets/imgs/plug_file_icon/gif.png')
      if (n.endsWith('.zip')) return require('@/assets/imgs/plug_file_icon/zip.png')
      if (n.endsWith('.rar')) return require('@/assets/imgs/plug_file_icon/rar.png')
      if (n.endsWith('.mp3')) return require('@/assets/imgs/plug_file_icon/mp3.png')
      if (n.endsWith('.mp4')) return require('@/assets/imgs/plug_file_icon/mp4.png')
      if (n.endsWith('.xml')) return require('@/assets/imgs/plug_file_icon/xml.png')
      if (n.endsWith('.yml') || n.endsWith('.yaml')) return require('@/assets/imgs/plug_file_icon/yml.png')
      if (n.endsWith('.json')) return require('@/assets/imgs/plug_file_icon/json.png')
      if (n.endsWith('.exe')) return require('@/assets/imgs/plug_file_icon/exe.png')
      if (n.endsWith('.log')) return require('@/assets/imgs/plug_file_icon/log.png')

      return require('@/assets/imgs/plug_file_icon/other.png')
    },
    /**
     * 格式化文件大小
     * @param {Number} size - 文件大小（字节）
     * @returns {string} 格式化后的大小字符串
     */
    formatFileSize(size) {
      if (!size) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(size) / Math.log(k))
      return (size / Math.pow(k, i)).toFixed(1) + ' ' + sizes[i]
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/task-group-detail/task-create.css';

/**
 * 任务详情卡片样式覆盖
 * 确保在模态框或视图中正确显示
 */
.task-detail-create-card {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/**
 * 附件卡片样式
 */

/* 附件网格容器 */
.task-detail-att-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

/* 附件卡片 */
.task-detail-att-card {
    text-align: left;
    width: calc(50% - 6px); /* 每行2个，减去gap的一半 */
    height: 64px;
    background: var(--space-black);
    position: relative;
    cursor: pointer;
    border: 1px solid rgba(51, 51, 51, 1); /* #333 */
    box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.2);
    transition: all 0.2s;
}

.task-detail-att-card:hover {
    transform: translateY(-2px);
    box-shadow: 6px 6px 0 rgba(0, 0, 0, 0.3);
    border-color: rgba(85, 85, 85, 1); /* #555 */
}

/* 附件内容 */
.task-detail-att-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    height: 100%;
    box-sizing: border-box;
}

/* 附件信息 */
.task-detail-att-info {
    flex: 1;
    overflow: hidden;
    margin-right: 12px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

/* 附件名称 */
.task-detail-att-name {
    color: var(--color-white);
    font-family: 'Impact', sans-serif;
    font-size: 16px;
    letter-spacing: 0.5px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    margin-bottom: 2px;
    line-height: 1.2;
    text-shadow: 2px 2px 0px rgba(0, 0, 0, 0.5);
}

/* 附件元信息 */
.task-detail-att-meta {
    color: var(--mission-rust);
    font-size: 12px;
    font-weight: bold;
    font-family: var(--font-family);
}

/* 附件图标容器 */
.task-detail-att-icon-box {
    width: 42px;
    height: 42px;
    border-radius: 50%;
    background: var(--mission-rust);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    border: 2px solid var(--space-black);
    box-shadow: 0 0 0 1px var(--mission-rust);
    overflow: hidden;
}

.task-detail-att-icon-box img {
    width: 28px;
    height: 28px;
    object-fit: contain;
    mix-blend-mode: multiply;
}

/* 附件装饰条 */
.task-detail-att-bar {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 4px;
    display: flex;
}

.task-detail-att-bar-l {
    width: 60%;
    background: var(--mission-rust);
}

.task-detail-att-bar-r {
    flex: 1;
    background: var(--alarm-burgundy);
}

/* 附件删除按钮 */
.task-detail-att-del-btn {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 20px;
    height: 20px;
    background: var(--alarm-burgundy);
    color: var(--color-white);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 12px;
    opacity: 0;
    transform: scale(0.5);
    transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    z-index: 2;
}

.task-detail-att-card:hover .task-detail-att-del-btn {
    opacity: 1;
    top: -6px;
    right: -6px;
    transform: scale(1);
}

/**
 * 上传覆盖层
 */
.task-detail-upload-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.7);
    z-index: 100;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(2px);
}

/* 上传框 */
.task-detail-upload-box {
    width: 300px;
    background: var(--space-black);
    border: var(--border-width) solid rgba(51, 51, 51, 1); /* #333 */
    padding: 24px;
    box-shadow: 8px 8px 0 rgba(0, 0, 0, 0.5);
    text-align: center;
}

/* 上传标题 */
.task-detail-upload-title {
    color: var(--color-white);
    font-family: 'Impact', sans-serif;
    font-size: 20px;
    letter-spacing: 1px;
    margin-bottom: 16px;
    text-transform: uppercase;
}

/* 上传进度轨道 */
.task-detail-upload-progress-track {
    width: 100%;
    height: 12px;
    background: rgba(51, 51, 51, 1); /* #333 */
    border: 1px solid rgba(85, 85, 85, 1); /* #555 */
    margin-bottom: 12px;
    position: relative;
    overflow: hidden;
}

/* 上传进度条 */
.task-detail-upload-progress-bar {
    height: 100%;
    background: repeating-linear-gradient(
        45deg,
        var(--mission-rust),
        var(--mission-rust) 10px,
        rgba(156, 88, 51, 1) 10px, /* #9c5833，更深的铁锈红 */
        rgba(156, 88, 51, 1) 20px
    );
    transition: width 0.2s linear;
    box-shadow: 0 0 10px rgba(178, 101, 59, 0.5);
}

/* 上传百分比 */
.task-detail-upload-percent {
    color: var(--mission-rust);
    font-family: var(--font-family);
    font-weight: bold;
    font-size: 14px;
}

/**
 * 终端标题镜像元素
 * 用于测量文本宽度以自动调整输入框大小
 */
.task-detail-terminal-title-mirror {
  position: absolute;
  visibility: hidden;
  height: 0;
  overflow: hidden;
  white-space: pre;
  font-family: 'Impact', sans-serif;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 1px;
  text-transform: uppercase;
  padding: 0; 
}
</style>
