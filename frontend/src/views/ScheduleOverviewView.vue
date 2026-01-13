<template>
  <!-- 日程概览视图根容器 -->
  <div class="schedule-overview-body">
    <!-- 左侧模块：日程日历 -->
    <div class="schedule-overview-left-module">
       <LeftScheduleCalendar 
         ref="leftCalendar"
         :selected-task-id="currentTaskId" 
         @select="onTaskSelect" 
       />
    </div>
    <!-- 右侧模块：任务详情 -->
    <div class="schedule-overview-right-module">
       <!-- 空状态：未选择任务时显示 -->
       <div v-if="!currentTaskId" class="schedule-overview-empty-state schedule-overview-empty-block">
          <!-- 浮动粒子效果 -->
          <div class="schedule-overview-particle schedule-overview-p1"></div>
          <div class="schedule-overview-particle schedule-overview-p2"></div>
          <div class="schedule-overview-particle schedule-overview-p3"></div>
          
          <div class="schedule-overview-empty-content">
             <!-- 浮动菱形效果 -->
             <div class="schedule-overview-rhine-empty-logo">
                <div class="schedule-overview-rhine-logo-inner"></div>
                <div class="schedule-overview-rhine-orbit-ring"></div>
                <div class="schedule-overview-rhine-orbit-dot"></div>
             </div>
             <div class="schedule-overview-empty-title">NO DATA SELECTED</div>
             <div class="schedule-overview-empty-subtitle">请选择左侧任务查看详情 // PENDING SELECTION</div>
          </div>
       </div>
       <!-- 任务详情组件：选择任务后显示 -->
       <TaskDetail 
         v-else 
         :task-id="currentTaskId" 
         :task-group-id="currentTaskGroupId"
         :current-user-id="currentUserId"
         @refresh="onTaskRefresh"
         @delete="onTaskDelete"
       />
    </div>
  </div>
</template>

<script>
import LeftScheduleCalendar from '@/components/schedule-overview/LeftScheduleCalendar.vue'
import TaskDetail from '@/components/task-group-detail/TaskDetail.vue'
import { getMe } from '@/api/user-api'

/**
 * 日程概览视图组件
 * 
 * 功能：
 * 1. 显示左侧日程日历和右侧任务详情
 * 2. 管理当前选中的任务ID和任务组ID
 * 3. 处理任务选择、刷新、删除等事件
 * 4. 加载当前用户信息
 */
export default {
  name: 'ScheduleOverviewView',
  components: {
    LeftScheduleCalendar,
    TaskDetail
  },
  data() {
    return {
      // 当前用户ID
      currentUserId: '',
      // 当前选中的任务ID
      currentTaskId: '',
      // 当前任务所属的任务组ID
      currentTaskGroupId: ''
    }
  },
  /**
   * 组件创建时加载当前用户信息
   */
  created() {
    this.loadCurrentUser()
  },
  methods: {
    /**
     * ========== 用户相关方法 ==========
     */
    
    /**
     * 加载当前用户信息
     * 从API获取当前登录用户的信息，并提取用户ID
     * 支持多种可能的用户ID字段名（userId, userID, user_id）
     * 如果API返回的是包装数据，会从data字段中提取
     */
    async loadCurrentUser() {
      try {
        const me = await getMe()
        // 尝试多种可能的用户ID字段名
        const uid = (me && (me.userId || me.userID || me.user_id)) 
          ? String(me.userId || me.userID || me.user_id) 
          : ''
        this.currentUserId = uid
        
        // 如果直接获取失败，尝试从包装数据中提取
        if (!uid && me && me.data) {
           this.currentUserId = String(me.data.userId || me.data.id || '')
        }
      } catch (e) {
        console.error('Failed to load current user', e)
      }
    },
    
    /**
     * ========== 任务相关方法 ==========
     */
    
    /**
     * 处理任务选择事件
     * 当用户在左侧日历中选择任务时触发
     * @param {Object} row - 选中的任务行数据对象
     */
    onTaskSelect(row) {
      if (!row) return
      // 提取任务ID，支持多种可能的字段名
      this.currentTaskId = String(row.taskId || row.taskID || row.id || '')
      // 提取任务组ID
      this.currentTaskGroupId = String(row.taskGroupId || '')
    },
    
    /**
     * 处理任务刷新事件
     * 当任务详情发生变化需要刷新日历时调用
     */
    onTaskRefresh() {
      if (this.$refs.leftCalendar) {
        this.$refs.leftCalendar.loadTasks()
      }
    },
    
    /**
     * 处理任务删除事件
     * 当任务被删除后，清空当前选中的任务并刷新日历
     */
    onTaskDelete() {
      this.currentTaskId = ''
      this.currentTaskGroupId = ''
      this.onTaskRefresh()
    }
  }
}
</script>

<style scoped>
  @import "@/assets/css/schedule-overview/schedule-overview-view.css";
</style>
