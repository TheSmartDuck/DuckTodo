<template>
  <div class="daily-report-tool">
    <!-- 左右两栏布局 -->
    <div class="daily-report-tool-layout">
      <!-- 左侧面板 -->
      <div class="daily-report-tool-left-panel">
        <!-- 日期选择区域 -->
        <div class="daily-report-tool-header">
          <div class="daily-report-tool-date-selector">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              class="daily-report-tool-date-picker"
              @change="handleDateChange"
            />
            <el-button
              type="primary"
              size="small"
              class="daily-report-tool-refresh-btn"
              :loading="loading"
              @click="loadData"
            >
              REFRESH / 刷新
            </el-button>
          </div>
          <div class="daily-report-tool-deco-line">
            <div class="daily-report-tool-line-seg daily-report-tool-line-mustard"></div>
            <div class="daily-report-tool-line-seg daily-report-tool-line-sage"></div>
            <div class="daily-report-tool-line-seg daily-report-tool-line-burgundy"></div>
          </div>
        </div>

        <!-- 任务列表区域 -->
        <div class="daily-report-tool-task-section" v-loading="loading">
          <!-- 空状态 -->
          <div v-if="!loading && (!taskList || taskList.length === 0) && !localTasks.length" class="daily-report-tool-empty">
            <div class="daily-report-tool-empty-icon">>></div>
            <div class="daily-report-tool-empty-text">NO_COMPLETED_TASKS / 暂无已完成任务</div>
            <div class="daily-report-tool-empty-hint">SELECT_DATE_TO_VIEW / 请选择日期查看</div>
          </div>

          <!-- 任务树形列表 -->
          <div v-else class="daily-report-tool-task-tree">
        <!-- 从接口获取的任务 -->
        <div
          v-for="(item, index) in taskList"
          :key="`api-${index}`"
          class="daily-report-tool-tree-node"
        >
          <!-- 主任务 -->
          <div class="daily-report-tool-tree-main-task">
            <span class="daily-report-tool-tree-icon">■</span>
            <span class="daily-report-tool-tree-status-tag" :class="getTaskStatusClass(item.task)">
              {{ getTaskStatusText(item.task) }}
            </span>
            <span class="daily-report-tool-tree-name">
              {{ item.task.task_name || item.task.taskName || 'UNNAMED_TASK' }}
              <span v-if="item.task.task_group_name" class="daily-report-tool-tree-group-name">
                [{{ item.task.task_group_name }}]
              </span>
            </span>
            <el-button
              type="text"
              size="mini"
              class="daily-report-tool-tree-delete-btn"
              @click="removeApiTask(index)"
            >
              ×
            </el-button>
          </div>
          <!-- 子任务 -->
          <div
            v-for="(childTask, childIndex) in item.child_tasks"
            :key="`api-child-${index}-${childIndex}`"
            class="daily-report-tool-tree-child-task"
          >
            <span class="daily-report-tool-tree-line"></span>
            <span class="daily-report-tool-tree-icon">▸</span>
            <span class="daily-report-tool-tree-status-tag" :class="getChildTaskStatusClass(childTask)">
              {{ getChildTaskStatusText(childTask) }}
            </span>
            <span class="daily-report-tool-tree-name">{{ childTask.child_task_name || childTask.taskName || 'UNNAMED_CHILD_TASK' }}</span>
            <el-button
              type="text"
              size="mini"
              class="daily-report-tool-tree-delete-btn"
              @click="removeApiChildTask(index, childIndex)"
            >
              ×
            </el-button>
          </div>
        </div>

        <!-- 本地添加的任务 -->
        <div
          v-for="(localTask, index) in localTasks"
          :key="`local-${index}`"
          class="daily-report-tool-tree-node"
        >
          <!-- 主任务 -->
          <div class="daily-report-tool-tree-main-task">
            <span class="daily-report-tool-tree-icon">■</span>
            <span class="daily-report-tool-tree-status-tag" :class="getLocalTaskStatusClass(localTask)">
              {{ getLocalTaskStatusText(localTask) }}
            </span>
            <span class="daily-report-tool-tree-name">
              {{ localTask.taskName || 'UNNAMED_TASK' }}
              <span v-if="localTask.task_group_name" class="daily-report-tool-tree-group-name">
                [{{ localTask.task_group_name }}]
              </span>
            </span>
            <el-button
              type="text"
              size="mini"
              class="daily-report-tool-tree-delete-btn"
              @click="removeLocalTask(index)"
            >
              ×
            </el-button>
          </div>
          <!-- 子任务 -->
          <div
            v-for="(childTask, childIndex) in localTask.child_tasks"
            :key="`local-child-${index}-${childIndex}`"
            class="daily-report-tool-tree-child-task"
          >
            <span class="daily-report-tool-tree-line"></span>
            <span class="daily-report-tool-tree-icon">▸</span>
            <span class="daily-report-tool-tree-status-tag" :class="getLocalChildTaskStatusClass(childTask)">
              {{ getLocalChildTaskStatusText(childTask) }}
            </span>
            <span class="daily-report-tool-tree-name">{{ childTask.taskName || 'UNNAMED_CHILD_TASK' }}</span>
            <el-button
              type="text"
              size="mini"
              class="daily-report-tool-tree-delete-btn"
              @click="removeLocalChildTask(index, childIndex)"
            >
              ×
            </el-button>
          </div>
        </div>
          </div>
        </div>

        <!-- 添加任务区域 -->
        <div class="daily-report-tool-add-section">
        <div class="daily-report-tool-add-deco-line">
          <div class="daily-report-tool-line-seg daily-report-tool-line-mustard"></div>
          <div class="daily-report-tool-line-seg daily-report-tool-line-sage"></div>
          <div class="daily-report-tool-line-seg daily-report-tool-line-burgundy"></div>
        </div>
        
        <!-- 添加主任务 -->
        <div class="daily-report-tool-add-main-task">
          <div class="daily-report-tool-add-label">ADD_MAIN_TASK / 添加主任务</div>
          <div class="daily-report-tool-add-input-group">
            <el-input
              v-model="newMainTaskName"
              placeholder="输入主任务名称"
              class="daily-report-tool-add-input"
              @keyup.enter.native="addMainTask"
            />
            <el-button
              type="primary"
              size="small"
              class="daily-report-tool-add-btn"
              @click="addMainTask"
            >
              ADD / 添加
            </el-button>
          </div>
        </div>

        <!-- 添加子任务 -->
        <div v-if="selectedMainTaskIndex !== null" class="daily-report-tool-add-child-task">
          <div class="daily-report-tool-add-label">
            ADD_CHILD_TASK / 添加子任务
            <span class="daily-report-tool-add-label-hint">({{ getSelectedMainTaskName() }})</span>
            <span class="daily-report-tool-back-link" @click="clearMainTaskSelection">← 返回</span>
          </div>
          <div class="daily-report-tool-add-input-group">
            <el-input
              v-model="newChildTaskName"
              placeholder="输入子任务名称"
              class="daily-report-tool-add-input"
              @keyup.enter.native="addChildTask"
            />
            <el-button
              type="primary"
              size="small"
              class="daily-report-tool-add-btn"
              @click="addChildTask"
            >
              ADD / 添加
            </el-button>
          </div>
        </div>

        <!-- 选择主任务以添加子任务 -->
        <div v-else class="daily-report-tool-select-main-task">
          <div class="daily-report-tool-add-label">
            SELECT_MAIN_TASK / 选择主任务以添加子任务
          </div>
          <div class="daily-report-tool-select-group">
            <el-select
              v-model="selectedMainTaskIndex"
              placeholder="选择主任务"
              class="daily-report-tool-select"
            >
              <el-option
                v-for="(task, index) in allMainTasks"
                :key="index"
                :label="task.taskName || 'UNNAMED_TASK'"
                :value="index"
              />
            </el-select>
          </div>
        </div>
        
        <!-- 生成日报按钮 -->
        <div class="daily-report-tool-generate-section">
          <el-button
            type="primary"
            class="daily-report-tool-generate-btn"
            :loading="generatingReport"
            :disabled="!currentModelInfo || generatingReport"
            @click="generateReport"
          >
            {{ generatingReport ? 'GENERATING / 生成中...' : 'GENERATE_REPORT / 生成日报' }}
          </el-button>
        </div>
      </div>
      </div>

      <!-- 右侧面板 -->
      <div class="daily-report-tool-right-panel">
        <!-- 右上角配置按钮 -->
        <div class="daily-report-tool-config-header">
          <!-- 当前使用的模型信息 -->
          <div v-if="currentModelInfo" class="daily-report-tool-current-model">
            <span class="daily-report-tool-current-model-label">当前模型:</span>
            <span class="daily-report-tool-current-model-value">
              {{ currentModelInfo.provider }} - {{ currentModelInfo.modelName }}
            </span>
          </div>
          <div v-else class="daily-report-tool-current-model">
            <span class="daily-report-tool-current-model-label">CURRENT_MODEL / 当前模型:</span>
            <span class="daily-report-tool-current-model-value daily-report-tool-current-model-empty">
              未配置
            </span>
          </div>
          <el-button
            type="text"
            size="small"
            class="daily-report-tool-config-btn"
            @click="showConfigDialog = true"
          >
            ⚙ CONFIG / 配置
          </el-button>
        </div>
        
        <div class="daily-report-tool-right-content" :class="{ 'daily-report-tool-content-loading': generatingReport }">
          <!-- 菱形风格 Loading 动画 -->
          <div v-if="generatingReport" class="daily-report-tool-diamond-loading">
            <!-- 背景装饰菱形 -->
            <div class="diamond-bg-decoration diamond-bg-1"></div>
            <div class="diamond-bg-decoration diamond-bg-2"></div>
            <div class="diamond-bg-decoration diamond-bg-3"></div>
            
            <!-- 中心菱形结构 -->
            <div class="diamond-loading-container">
              <!-- 外层虚线菱形（旋转） -->
              <div class="diamond-outer-dashed"></div>
              
              <!-- 中层轮廓菱形 -->
              <div class="diamond-middle-outline">
                <!-- 小菱形（右上角） -->
                <div class="diamond-small"></div>
              </div>
              
              <!-- 内层实心菱形 -->
              <div class="diamond-inner-solid"></div>
            </div>
            
            <!-- 加载文字 -->
            <div class="diamond-loading-text">
              <div class="diamond-loading-text-main">GENERATING_REPORT / 生成日报中...</div>
            </div>
          </div>
          <!-- 日报内容显示区域 -->
          <div v-if="reportContent.todayFinishTasksReport || reportContent.tomorrowTodoTasksReport || reportContent.thinkReport" class="daily-report-tool-report-content">
            <!-- 今日完成 -->
            <div class="daily-report-tool-report-section">
              <div class="daily-report-tool-report-section-title daily-report-tool-title-mustard">
                <span class="daily-report-tool-color-block daily-report-tool-color-mustard"></span>
                <span class="daily-report-tool-title-text">TODAY_FINISH / 今日完成</span>
              </div>
              <el-input
                v-model="reportContent.todayFinishTasksReport"
                type="textarea"
                :rows="6"
                class="daily-report-tool-report-textarea daily-report-tool-textarea-today"
                placeholder="今日完成报告内容..."
              />
            </div>
            
            <!-- 明日待办 -->
            <div class="daily-report-tool-report-section">
              <div class="daily-report-tool-report-section-title daily-report-tool-title-sage">
                <span class="daily-report-tool-color-block daily-report-tool-color-sage"></span>
                <span class="daily-report-tool-title-text">TOMORROW_TODO / 明日待办</span>
              </div>
              <el-input
                v-model="reportContent.tomorrowTodoTasksReport"
                type="textarea"
                :rows="6"
                class="daily-report-tool-report-textarea daily-report-tool-textarea-tomorrow"
                placeholder="明日待办报告内容..."
              />
            </div>
            
            <!-- 思考 -->
            <div class="daily-report-tool-report-section">
              <div class="daily-report-tool-report-section-title daily-report-tool-title-burgundy">
                <span class="daily-report-tool-color-block daily-report-tool-color-burgundy"></span>
                <span class="daily-report-tool-title-text">THINKING / 思考</span>
              </div>
              <el-input
                v-model="reportContent.thinkReport"
                type="textarea"
                :rows="6"
                class="daily-report-tool-report-textarea daily-report-tool-textarea-thinking"
                placeholder="思考报告内容..."
              />
            </div>
          </div>
          
          <!-- 空状态占位符 -->
          <div v-else class="daily-report-tool-right-placeholder">
            <div class="daily-report-tool-right-placeholder-icon">>></div>
            <div class="daily-report-tool-right-placeholder-text">REPORT_PREVIEW / 日报预览</div>
            <div class="daily-report-tool-right-placeholder-hint">点击左侧"生成日报"按钮开始生成</div>
          </div>
        </div>
        
        <!-- 推送钉钉日报按钮区域 -->
        <div v-if="(reportContent.todayFinishTasksReport || reportContent.tomorrowTodoTasksReport || reportContent.thinkReport) && !generatingReport" class="daily-report-tool-push-section">
          <el-button
            type="primary"
            class="daily-report-tool-push-btn"
            @click="pushToDingTalk"
            :loading="pushingToDingTalk"
            :disabled="pushingToDingTalk"
          >
            {{ pushingToDingTalk ? '推送中...' : 'PUSH_TO_DINGTALK / 推送至钉钉' }}
          </el-button>
          <el-tooltip
            content='提示：请确保已安装"钉钉日报自动提交组件"'
            placement="top"
            effect="dark"
            style="width: 10%;"
          >
            <span class="daily-report-tool-push-hint-wrapper">
              <i class="el-icon-question daily-report-tool-push-hint-icon"></i>
            </span>
          </el-tooltip>
        </div>
      </div>
      
      <!-- 配置对话框 -->
      <el-dialog
        :visible.sync="showConfigDialog"
        width="500px"
        class="daily-report-tool-config-dialog"
        :close-on-click-modal="false"
        @open="handleConfigDialogOpen"
      >
        <div slot="title" class="daily-report-tool-config-dialog-title">
          CONFIG / 日报生成工具配置
        </div>
        <div class="daily-report-tool-config-content">
          <div class="daily-report-tool-config-label">
            SELECT_CHAT_LLM / 选择 Chat LLM 模型
          </div>
          <el-select
            v-model="selectedLlmConfigId"
            placeholder="请选择 Chat LLM 模型"
            class="daily-report-tool-config-select"
            filterable
            :loading="loadingLlmConfigs"
          >
            <el-option
              v-for="config in chatLlmConfigs"
              :key="config.userLlmConfigId"
              :label="`${config.llmProvider} - ${config.llmModelName}`"
              :value="config.userLlmConfigId"
            />
          </el-select>
          <div v-if="chatLlmConfigs.length === 0 && !loadingLlmConfigs" class="daily-report-tool-config-empty">
            暂无 Chat LLM 配置，请先在用户信息页面添加
          </div>
        </div>
        <div slot="footer" class="daily-report-tool-config-footer">
          <el-button @click="showConfigDialog = false">CANCEL / 取消</el-button>
          <el-button
            type="primary"
            @click="saveConfig"
            :loading="savingConfig"
            :disabled="!selectedLlmConfigId"
          >
            SAVE / 保存
          </el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { getTodayCompletedTasksWithChildren, getDailyReportToolConfigByUserId, createDailyReportToolConfig, updateDailyReportToolConfig, generateDailyReport } from '@/api/daily_report_api'
import { getMyLlmConfigList } from '@/api/llm-config-api'

export default {
  name: 'DailyReportTool',
  props: {
    tool: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      loading: false,
      selectedDate: null, // 默认为今天，格式：YYYY-MM-DD
      taskList: [], // 从接口获取的任务列表，格式：[{ task: Task, child_tasks: [ChildTask, ...] }]
      localTasks: [], // 本地添加的任务列表，格式：[{ taskName: string, child_tasks: [{ taskName: string }] }]
      newMainTaskName: '', // 新主任务名称
      newChildTaskName: '', // 新子任务名称
      selectedMainTaskIndex: null, // 选中的主任务索引（用于添加子任务）
      // 配置相关
      showConfigDialog: false, // 是否显示配置对话框
      selectedLlmConfigId: null, // 选中的 LLM 配置ID
      chatLlmConfigs: [], // Chat LLM 配置列表
      loadingLlmConfigs: false, // 加载 LLM 配置列表的状态
      savingConfig: false, // 保存配置的状态
      currentToolConfig: null, // 当前工具配置
      // 日报生成相关
      generatingReport: false, // 是否正在生成日报
      reportContent: {
        todayFinishTasksReport: '', // 今日完成报告
        tomorrowTodoTasksReport: '', // 明日待办报告
        thinkReport: '' // 思考报告
      },
      // 推送钉钉相关
      pushingToDingTalk: false // 是否正在推送至钉钉
    }
  },
  computed: {
    /**
     * 所有主任务（接口获取的 + 本地添加的）
     */
    allMainTasks() {
      const apiTasks = this.taskList.map(item => ({
        taskName: item.task.task_name || item.task.taskName || 'UNNAMED_TASK',
        source: 'api'
      }))
      const localTasks = this.localTasks.map(task => ({
        taskName: task.taskName || 'UNNAMED_TASK',
        source: 'local'
      }))
      return [...apiTasks, ...localTasks]
    },
    /**
     * 当前使用的模型信息
     */
    currentModelInfo() {
      if (!this.currentToolConfig || !this.currentToolConfig.config_json) {
        return null
      }
      
      // 解析 config_json
      let configJson = this.currentToolConfig.config_json
      if (typeof configJson === 'string') {
        try {
          configJson = JSON.parse(configJson)
        } catch (e) {
          console.error('Failed to parse config_json:', e)
          return null
        }
      }
      
      const llmConfigId = configJson.llm_config_id
      if (!llmConfigId) {
        return null
      }
      
      // 从 chatLlmConfigs 中找到对应的配置
      const config = this.chatLlmConfigs.find(c => c.userLlmConfigId === llmConfigId)
      if (config) {
        return {
          provider: config.llmProvider || 'Unknown',
          modelName: config.llmModelName || 'Unknown'
        }
      }
      
      // 如果找不到，尝试从 config_json 中获取（备用方案）
      if (configJson.llm_provider && configJson.llm_model_name) {
        return {
          provider: configJson.llm_provider,
          modelName: configJson.llm_model_name
        }
      }
      
      return null
    }
  },
  mounted() {
    // 组件挂载时，默认加载今天的数据
    this.selectedDate = this.getTodayDate()
    this.loadData()
    // 加载 LLM 配置列表和当前工具配置
    this.loadLlmConfigs()
    this.loadToolConfig()
  },
  methods: {
    /**
     * 获取今天的日期（YYYY-MM-DD格式）
     */
    getTodayDate() {
      const today = new Date()
      const year = today.getFullYear()
      const month = String(today.getMonth() + 1).padStart(2, '0')
      const day = String(today.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    /**
     * 处理日期变化
     */
    handleDateChange() {
      if (this.selectedDate) {
        this.loadData()
      }
    },
    /**
     * 加载数据
     */
    async loadData() {
      if (!this.selectedDate) {
        this.$message.warning('请先选择日期')
        return
      }

      this.loading = true
      try {
        const params = {
          targetDate: this.selectedDate
        }
        const data = await getTodayCompletedTasksWithChildren(params)
        this.taskList = data || []
      } catch (error) {
        console.error('Failed to load daily report data:', error)
        this.$message.error('加载数据失败：' + (error.message || '未知错误'))
        this.taskList = []
      } finally {
        this.loading = false
      }
    },
    /**
     * 添加主任务
     */
    addMainTask() {
      if (!this.newMainTaskName || !this.newMainTaskName.trim()) {
        this.$message.warning('请输入主任务名称')
        return
      }
      
      this.localTasks.push({
        taskName: this.newMainTaskName.trim(),
        child_tasks: []
      })
      this.newMainTaskName = ''
      this.$message.success('主任务添加成功')
    },
    /**
     * 添加子任务
     */
    addChildTask() {
      if (this.selectedMainTaskIndex === null) {
        this.$message.warning('请先选择主任务')
        return
      }
      
      if (!this.newChildTaskName || !this.newChildTaskName.trim()) {
        this.$message.warning('请输入子任务名称')
        return
      }

      // 判断是接口任务还是本地任务
      const apiTaskCount = this.taskList.length
      if (this.selectedMainTaskIndex < apiTaskCount) {
        // 接口任务，创建一个本地主任务副本并添加子任务
        const apiTask = this.taskList[this.selectedMainTaskIndex]
        const newLocalTask = {
          taskName: apiTask.task.task_name || apiTask.task.taskName || 'UNNAMED_TASK',
          child_tasks: [{
            taskName: this.newChildTaskName.trim()
          }]
        }
        this.localTasks.push(newLocalTask)
        // 更新选中索引为新的本地任务
        this.selectedMainTaskIndex = apiTaskCount + this.localTasks.length - 1
        this.$message.success('已创建本地主任务并添加子任务')
      } else {
        // 本地任务
        const localIndex = this.selectedMainTaskIndex - apiTaskCount
        if (!this.localTasks[localIndex]) {
          this.$message.error('主任务不存在')
          return
        }
        if (!this.localTasks[localIndex].child_tasks) {
          this.localTasks[localIndex].child_tasks = []
        }
        this.localTasks[localIndex].child_tasks.push({
          taskName: this.newChildTaskName.trim()
        })
        this.$message.success('子任务添加成功')
      }
      
      this.newChildTaskName = ''
    },
    /**
     * 删除接口获取的主任务
     */
    removeApiTask(index) {
      if (this.taskList[index]) {
        this.taskList.splice(index, 1)
        // 如果删除的是选中的主任务，清空选中状态
        if (this.selectedMainTaskIndex === index) {
          this.selectedMainTaskIndex = null
        } else if (this.selectedMainTaskIndex !== null && this.selectedMainTaskIndex > index) {
          // 如果删除的任务在选中任务之前，需要调整索引
          this.selectedMainTaskIndex--
        }
        this.$message.success('主任务已删除')
      }
    },
    /**
     * 删除接口获取的子任务
     */
    removeApiChildTask(mainTaskIndex, childTaskIndex) {
      if (this.taskList[mainTaskIndex] && this.taskList[mainTaskIndex].child_tasks) {
        this.taskList[mainTaskIndex].child_tasks.splice(childTaskIndex, 1)
        this.$message.success('子任务已删除')
      }
    },
    /**
     * 删除本地主任务
     */
    removeLocalTask(index) {
      this.localTasks.splice(index, 1)
      // 如果删除的是选中的主任务，清空选中状态
      const apiTaskCount = this.taskList.length
      if (this.selectedMainTaskIndex === index + apiTaskCount) {
        this.selectedMainTaskIndex = null
      } else if (this.selectedMainTaskIndex > index + apiTaskCount) {
        // 如果删除的任务在选中任务之前，需要调整索引
        this.selectedMainTaskIndex--
      }
      this.$message.success('主任务已删除')
    },
    /**
     * 删除本地子任务
     */
    removeLocalChildTask(mainTaskIndex, childTaskIndex) {
      const apiTaskCount = this.taskList.length
      const localIndex = mainTaskIndex - apiTaskCount
      if (this.localTasks[localIndex] && this.localTasks[localIndex].child_tasks) {
        this.localTasks[localIndex].child_tasks.splice(childTaskIndex, 1)
        this.$message.success('子任务已删除')
      }
    },
    /**
     * 获取选中的主任务名称
     */
    getSelectedMainTaskName() {
      if (this.selectedMainTaskIndex === null) {
        return ''
      }
      const apiTaskCount = this.taskList.length
      if (this.selectedMainTaskIndex < apiTaskCount) {
        const task = this.taskList[this.selectedMainTaskIndex].task
        return task.task_name || task.taskName || 'UNNAMED_TASK'
      } else {
        const localIndex = this.selectedMainTaskIndex - apiTaskCount
        return this.localTasks[localIndex]?.taskName || 'UNNAMED_TASK'
      }
    },
    /**
     * 判断任务是否已完成（接口获取的任务）
     */
    isTaskCompleted(task) {
      // 接口返回的都是已完成的任务，但为了健壮性，还是检查一下状态
      return task.task_status === 3 || (task.finish_time && task.finish_time !== null)
    },
    /**
     * 判断子任务是否已完成（接口获取的子任务）
     */
    isChildTaskCompleted(childTask) {
      // 接口返回的都是已完成的子任务，但为了健壮性，还是检查一下状态
      return childTask.child_task_status === 3 || (childTask.finish_time && childTask.finish_time !== null)
    },
    /**
     * 获取任务状态文本（接口获取的任务）
     */
    getTaskStatusText(task) {
      return this.isTaskCompleted(task) ? '已完成' : '未完成'
    },
    /**
     * 获取任务状态样式类（接口获取的任务）
     */
    getTaskStatusClass(task) {
      return this.isTaskCompleted(task) ? 'daily-report-tool-status-completed' : 'daily-report-tool-status-pending'
    },
    /**
     * 获取子任务状态文本（接口获取的子任务）
     */
    getChildTaskStatusText(childTask) {
      return this.isChildTaskCompleted(childTask) ? '已完成' : '未完成'
    },
    /**
     * 获取子任务状态样式类（接口获取的子任务）
     */
    getChildTaskStatusClass(childTask) {
      return this.isChildTaskCompleted(childTask) ? 'daily-report-tool-status-completed' : 'daily-report-tool-status-pending'
    },
    /**
     * 获取本地任务状态文本
     */
    getLocalTaskStatusText(localTask) {
      // 本地添加的任务标签显示为"自添加"
      return '自添加'
    },
    /**
     * 获取本地任务状态样式类
     */
    getLocalTaskStatusClass(localTask) {
      // 自添加标签使用特殊样式
      return 'daily-report-tool-status-custom'
    },
    /**
     * 获取本地子任务状态文本
     */
    getLocalChildTaskStatusText(childTask) {
      // 本地添加的子任务标签显示为"自添加"
      return '自添加'
    },
    /**
     * 获取本地子任务状态样式类
     */
    getLocalChildTaskStatusClass(childTask) {
      // 自添加标签使用特殊样式
      return 'daily-report-tool-status-custom'
    },
    /**
     * 判断主任务是否被选中（用于添加子任务）
     * @param {number} index - 任务在完整列表中的索引（接口任务从0开始，本地任务从taskList.length开始）
     */
    isMainTaskSelected(index) {
      return this.selectedMainTaskIndex === index
    },
    /**
     * 清除主任务选择（返回）
     */
    clearMainTaskSelection() {
      this.selectedMainTaskIndex = null
    },
    /**
     * 加载 Chat LLM 配置列表
     */
    async loadLlmConfigs() {
      this.loadingLlmConfigs = true
      try {
        const configs = await getMyLlmConfigList()
        // 过滤出 Chat 类型的配置（llmModelType === 1）
        this.chatLlmConfigs = (configs || []).filter(config => config.llmModelType === 1)
      } catch (error) {
        console.error('Failed to load LLM configs:', error)
        this.$message.error('加载 LLM 配置失败：' + (error.message || '未知错误'))
        this.chatLlmConfigs = []
      } finally {
        this.loadingLlmConfigs = false
      }
    },
    /**
     * 加载当前工具配置
     */
    async loadToolConfig() {
      try {
        const config = await getDailyReportToolConfigByUserId()
        if (config && config.config_json) {
          this.currentToolConfig = config
          // 从 config_json 中获取 llm_config_id
          const configJson = typeof config.config_json === 'string' 
            ? JSON.parse(config.config_json) 
            : config.config_json
          if (configJson && configJson.llm_config_id) {
            this.selectedLlmConfigId = configJson.llm_config_id
          }
        }
      } catch (error) {
        console.error('Failed to load tool config:', error)
        // 加载失败不显示错误提示，因为可能是首次使用
      }
    },
    /**
     * 处理配置对话框打开事件
     */
    handleConfigDialogOpen() {
      // 打开对话框时刷新 LLM 配置列表
      this.loadLlmConfigs()
    },
    /**
     * 保存配置
     */
    async saveConfig() {
      if (!this.selectedLlmConfigId) {
        this.$message.warning('请选择 Chat LLM 模型')
        return
      }
      
      this.savingConfig = true
      try {
        const params = {
          llmConfigId: this.selectedLlmConfigId
        }
        
        // 如果已有配置，则更新；否则创建
        if (this.currentToolConfig && this.currentToolConfig.id) {
          await updateDailyReportToolConfig(params)
          this.$message.success('配置更新成功')
        } else {
          await createDailyReportToolConfig(params)
          this.$message.success('配置保存成功')
        }
        
        // 重新加载配置
        await this.loadToolConfig()
        // 关闭对话框
        this.showConfigDialog = false
      } catch (error) {
        console.error('Failed to save config:', error)
        this.$message.error('保存配置失败：' + (error.message || '未知错误'))
      } finally {
        this.savingConfig = false
      }
    },
    /**
     * 生成日报
     */
    async generateReport() {
      // 检查是否已配置模型
      if (!this.currentModelInfo) {
        this.$message.warning('请先配置 Chat LLM 模型')
        this.showConfigDialog = true
        return
      }
      
      // 检查是否有选中的 LLM 配置ID
      if (!this.selectedLlmConfigId && this.currentToolConfig && this.currentToolConfig.config_json) {
        let configJson = this.currentToolConfig.config_json
        if (typeof configJson === 'string') {
          try {
            configJson = JSON.parse(configJson)
          } catch (e) {
            console.error('Failed to parse config_json:', e)
          }
        }
        if (configJson && configJson.llm_config_id) {
          this.selectedLlmConfigId = configJson.llm_config_id
        }
      }
      
      if (!this.selectedLlmConfigId) {
        this.$message.warning('请先配置 Chat LLM 模型')
        this.showConfigDialog = true
        return
      }
      
      this.generatingReport = true
      try {
        // 构建今日完成任务列表
        const todayFinishTaskList = []
        
        // 添加接口获取的任务
        this.taskList.forEach(item => {
          const task = item.task
          const childTaskList = (item.child_tasks || []).map(ct => ({
            child_task_name: ct.child_task_name || ct.childTaskName || '',
            child_task_status: ct.child_task_status || ct.childTaskStatus || ''
          }))
          
          todayFinishTaskList.push({
            task_name: task.task_name || task.taskName || '',
            task_status: task.task_status || task.taskStatus || '',
            task_group_name: task.task_group_name || '',
            child_task_list: childTaskList
          })
        })
        
        // 添加本地添加的任务
        this.localTasks.forEach(localTask => {
          const childTaskList = (localTask.child_tasks || []).map(ct => ({
            child_task_name: ct.taskName || '',
            child_task_status: 'completed' // 本地添加的任务默认为已完成
          }))
          
          todayFinishTaskList.push({
            task_name: localTask.taskName || '',
            task_status: 'completed',
            task_group_name: localTask.task_group_name || '',
            child_task_list: childTaskList
          })
        })
        
        // 调用生成日报接口
        const params = {
          llmConfigId: this.selectedLlmConfigId,
          todayFinishTaskList: todayFinishTaskList.length > 0 ? todayFinishTaskList : null
        }
        
        const result = await generateDailyReport(params)
        
        // 更新报告内容
        this.reportContent = {
          todayFinishTasksReport: result.today_finish_tasks_report || '',
          tomorrowTodoTasksReport: result.tomorrow_todo_tasks_report || '',
          thinkReport: result.think_report || ''
        }
        
        this.$message.success('日报生成成功')
      } catch (error) {
        console.error('Failed to generate report:', error)
        this.$message.error('生成日报失败：' + (error.message || '未知错误'))
      } finally {
        this.generatingReport = false
      }
    },
    /**
     * 推送至钉钉
     */
    async pushToDingTalk() {
      // 检查是否有报告内容
      if (!this.reportContent.todayFinishTasksReport && 
          !this.reportContent.tomorrowTodoTasksReport && 
          !this.reportContent.thinkReport) {
        this.$message.warning('请先生成日报内容')
        return
      }
      
      this.pushingToDingTalk = true
      
      try {
        // 构建JSON对象
        const reportJson = {
          today_finish_tasks_report: this.reportContent.todayFinishTasksReport || '暂无',
          tomorrow_todo_tasks_report: this.reportContent.tomorrowTodoTasksReport || '暂无',
          think_report: this.reportContent.thinkReport || '暂无'
        }
        
        // 格式化为紧凑的JSON字符串
        const compactJson = JSON.stringify(reportJson)
        
        // 复制到剪贴板
        if (navigator.clipboard && navigator.clipboard.writeText) {
          await navigator.clipboard.writeText(compactJson)
        } else {
          // 降级方案：使用传统方法
          const textArea = document.createElement('textarea')
          textArea.value = compactJson
          textArea.style.position = 'fixed'
          textArea.style.left = '-999999px'
          document.body.appendChild(textArea)
          textArea.select()
          try {
            document.execCommand('copy')
            document.body.removeChild(textArea)
          } catch (e) {
            document.body.removeChild(textArea)
            throw new Error('复制到剪贴板失败：' + e.message)
          }
        }
        
        // 延迟一下，确保剪贴板操作完成
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 调用协议链接启动外部程序
        try {
          window.location.href = 'dingtalk-daily-report://'
          this.$message.success('JSON已复制到剪贴板，正在启动钉钉日报推送程序...')
        } catch (e) {
          this.$message.warning('JSON已复制到剪贴板，但启动程序失败。请检查：1) 协议是否已注册 2) 程序是否已安装')
        }
      } catch (error) {
        console.error('Failed to push to DingTalk:', error)
        this.$message.error('推送失败：' + (error.message || '未知错误'))
      } finally {
        this.pushingToDingTalk = false
      }
    }
  }
}
</script>

<style>
@import '@/assets/css/tools-factory/daily-report-tool.css';
</style>
