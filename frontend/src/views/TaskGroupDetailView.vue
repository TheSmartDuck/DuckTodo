<template>
  <!-- 任务族详情视图根容器 -->
  <div class="task-group-detail-body">
    <!-- 左侧面板：任务列表 -->
    <task-list
      ref="taskList"
      :task-group-id="taskGroupId"
      :task-group-name="currentGroupName"
      :task-group="currentGroup"
      :user-list="userList"
      :selected-task-id="selectedTaskId"
      @select="onSelectTask"
      @create="startCreateTask"
      @deleted="onTaskGroupDeleted"
    />

    <!-- 右侧面板：详情或创建 -->
    <div class="task-group-detail-pane" v-loading="loading">
      <!-- 创建新任务 -->
      <task-create
        v-if="creating"
        :group-id="taskGroupId"
        :task-group="currentGroup"
        :current-user-id="currentUserId"
        @created="onTaskCreated"
      />

      <!-- 任务详情 -->
      <task-detail
        v-else-if="selectedTask"
        :task-id="selectedTaskId"
        :task-group-id="String(taskGroupId)"
        :current-user-id="currentUserId"
        @refresh="onTaskUpdated"
        @delete="onTaskDeleted"
      />

      <!-- 空状态：未选择任务时显示 -->
      <div class="task-group-detail-block task-group-detail-empty-block" v-else>
        <!-- 浮动粒子装饰效果 -->
        <div class="task-group-detail-particle task-group-detail-particle-1"></div>
        <div class="task-group-detail-particle task-group-detail-particle-2"></div>
        <div class="task-group-detail-particle task-group-detail-particle-3"></div>
        
        <!-- 空状态内容区域 -->
        <div class="task-group-detail-empty-content">
           <!-- 浮动菱形 Logo 效果 -->
           <div class="task-group-detail-rhine-empty-logo">
              <!-- Logo 内部方块 -->
              <div class="task-group-detail-rhine-logo-inner"></div>
              <!-- 轨道环 -->
              <div class="task-group-detail-rhine-orbit-ring"></div>
              <!-- 轨道点 -->
              <div class="task-group-detail-rhine-orbit-dot"></div>
           </div>
           <!-- 空状态标题 -->
           <div class="task-group-detail-empty-title">NO DATA SELECTED</div>
           <!-- 空状态副标题 -->
           <div class="task-group-detail-empty-subtitle">请选择左侧任务查看详情 // PENDING SELECTION</div>
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import TaskList from '@/components/task-group-detail/TaskList.vue'
import TaskDetail from '@/components/task-group-detail/TaskDetail.vue'
import TaskCreate from '@/components/task-group-detail/TaskCreate.vue'

import { getMyTaskGroupList } from '@/api/taskgroup-api'
import { listUsers, getMe } from '@/api/user-api'

/**
 * 任务族详情视图组件
 * 
 * 功能：
 * 1. 显示任务族中的任务列表（左侧）
 * 2. 显示选中任务的详情或创建新任务（右侧）
 * 3. 管理任务的选择、创建、更新、删除状态
 * 4. 加载并管理任务族数据、用户列表、当前用户信息
 * 
 * 组件结构：
 * - 左侧：TaskList 组件（任务列表）
 * - 右侧：TaskCreate（创建任务）或 TaskDetail（任务详情）或空状态
 * 
 * Props：
 * - taskGroupId: 任务族ID（可选）
 * 
 * Events：
 * - 无（通过子组件事件处理）
 */
export default {
  name: 'TaskGroupDetailView',
  components: {
    TaskList,
    TaskDetail,
    TaskCreate
  },
  props: {
    /**
     * 任务族ID
     * @type {String|Number}
     */
    taskGroupId: {
      type: [String, Number],
      required: false
    }
  },
  data () {
    return {
      // 数据加载状态
      loading: false,
      // 是否正在创建任务
      creating: false,
      // 当前选中的任务对象
      selectedTask: null,
      // 我的任务族列表
      myTaskGroups: [],
      // 用户列表（用于显示用户名）
      userList: [],
      // 当前用户ID
      currentUserId: '',
      // 当前用户对象
      currentUser: null
    }
  },
  computed: {
    /**
     * 当前选中任务的ID
     * 支持多种字段名：taskId、taskID、id
     * @returns {string|null} 任务ID字符串，如果未选择则返回 null
     */
    selectedTaskId () {
      return this.selectedTask ? String(this.selectedTask.taskId || this.selectedTask.taskID || this.selectedTask.id) : null
    },
    
    /**
     * 当前任务族对象
     * 根据 taskGroupId 从 myTaskGroups 中查找匹配的任务族
     * @returns {Object} 任务族对象，如果未找到则返回空对象
     */
    currentGroup () {
      return this.myTaskGroups.find(x => String(x.taskGroupId) === String(this.taskGroupId)) || {}
    },
    
    /**
     * 当前任务族名称
     * @returns {string} 任务族名称，如果未找到则返回空字符串
     */
    currentGroupName () {
      return this.currentGroup.groupName || ''
    }
  },
  /**
   * 组件创建时钩子
   * 加载公共数据（任务族列表、用户列表、当前用户信息）
   */
  async created () {
    await this.loadCommonData()
  },
  watch: {
    /**
     * 监听任务族ID变化
     * 当任务族ID改变时，清空当前选中的任务和创建状态
     * 注意：任务列表组件会自行处理重新加载
     */
    taskGroupId () {
      this.selectedTask = null
      this.creating = false
    }
  },
  methods: {
    /**
     * ========== 数据加载相关方法 ==========
     */
    
    /**
     * 加载公共数据
     * 并行加载任务族列表、用户列表和当前用户信息
     * 处理不同的响应结构（直接数组或包含 data 字段的对象）
     */
    async loadCommonData () {
      this.loading = true
      try {
        // 并行请求三个接口
        const [groupsRes, usersRes, meRes] = await Promise.all([
          getMyTaskGroupList(),
          listUsers(),
          getMe()
        ])
        
        // 处理响应结构（http 工具可能直接返回数据或包含 data 字段）
        this.myTaskGroups = Array.isArray(groupsRes) ? groupsRes : (groupsRes.data || [])
        this.userList = Array.isArray(usersRes) ? usersRes : (usersRes.data || [])
        
        // 处理当前用户信息（支持多种字段名：id、userId）
        const me = (meRes && (meRes.id || meRes.userId)) ? meRes : (meRes.data || meRes)
        this.currentUser = me
        this.currentUserId = me ? (me.id || me.userId) : ''
        
      } catch (e) {
        console.error('无法加载数据，请重试', e)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * ========== 任务选择相关方法 ==========
     */
    
    /**
     * 选择任务
     * 当用户从任务列表中选择一个任务时调用
     * @param {Object} task - 选中的任务对象
     */
    onSelectTask (task) {
      this.creating = false
      this.selectedTask = task
    },
    
    /**
     * 开始创建任务
     * 当用户点击创建任务按钮时调用
     * 清空当前选中的任务，设置创建状态为 true
     */
    startCreateTask () {
      this.selectedTask = null
      this.creating = true
    },
    
    /**
     * ========== 任务操作回调方法 ==========
     */
    
    /**
     * 任务创建完成回调
     * 当任务创建成功后调用，刷新任务列表
     */
    onTaskCreated () {
      this.creating = false
      // 刷新任务列表
      if (this.$refs.taskList) {
        this.$refs.taskList.reloadTasks()
      }
    },
    
    /**
     * 任务更新回调
     * 当任务详情更新（如状态改变）时调用，刷新任务列表
     * 注意：selectedTask 对象引用仍然是旧的，但 TaskDetail 组件会根据 ID 重新加载数据
     * 理想情况下应该更新 selectedTask 为新数据，但列表刷新可能会获取新的对象
     * TaskDetail 组件主要依赖 'task' prop 中的 ID
     */
    onTaskUpdated () {
      // 刷新任务列表
      if (this.$refs.taskList) {
        this.$refs.taskList.reloadTasks()
      }
    },
    
    /**
     * 任务删除回调
     * 当任务被删除时调用，清空当前选中的任务并刷新列表
     */
    onTaskDeleted () {
      this.selectedTask = null
      // 刷新任务列表
      if (this.$refs.taskList) {
        this.$refs.taskList.reloadTasks()
      }
    },
    
    /**
     * 任务族删除回调
     * 当任务族被删除时调用，跳转到首页
     */
    onTaskGroupDeleted () {
      // 任务族已删除，跳转到首页
      this.$router.push({ name: 'home' })
    }
  }
}
</script>

<style scoped>
@import "@/assets/css/task-group-detail/task-group-detail.css";
</style>
