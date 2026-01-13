<template>
  <!-- 首页视图根容器 -->
  <div class="home-page">
    <!-- 背景粒子效果组件 -->
    <dashboard-particles />
    
    <!-- 页面头部区域：包含用户欢迎信息和打字效果 -->
    <div class="home-page-header-wrapper">
      <dashboard-header 
        :user-name="userName"
        :typed-text="typedText"
      />
    </div>

    <!-- 页面内容区域：包含指标卡片和组件布局 -->
    <div class="home-page-content">
      <!-- 顶部指标卡片行 -->
      <dashboard-metrics 
         :metric-keys="dashboardConfig.metrics" 
         :metric-values="metricValues"
         @update:keys="updateMetricsConfig"
      />
      
      <!-- 内容分割区域：左右分栏布局 -->
      <el-row :gutter="16" class="home-content-split" style="padding: 0px 16px 16px 16px;">
        <!-- 左侧面板：可配置的组件区域 -->
        <el-col :span="12" class="home-left-pane">
           <!-- 顶部组件插槽 -->
           <div class="home-widget-slot home-top-slot">
              <dashboard-widget 
                 :widget-key="dashboardConfig.leftWidgets[0]" 
                 :data="widgetData[dashboardConfig.leftWidgets[0]]"
                 @item-click="handleItemClick"
                 @update:widget-key="(val) => updateWidgetConfig(0, val)"
              />
           </div>

           <!-- 底部组件插槽 -->
           <div class="home-widget-slot home-bottom-slot" style="margin-top: 16px;">
              <dashboard-widget 
                 :widget-key="dashboardConfig.leftWidgets[1]" 
                 :data="widgetData[dashboardConfig.leftWidgets[1]]"
                 @item-click="handleItemClick"
                 @update:widget-key="(val) => updateWidgetConfig(1, val)"
              />
           </div>
        </el-col>
        
        <!-- 右侧面板：工具区域（预留） -->
        <el-col :span="12" class="home-right-pane">
           <dashboard-tools />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getMe } from '@/api/user-api'
import { getMyTaskGroupList } from '@/api/taskgroup-api'
import { getTaskListByUserId } from '@/api/task-api'
import * as statsApi from '@/api/statistics-api'

import DashboardMetrics from '@/components/home/DashboardMetrics.vue'
import DashboardWidget from '@/components/home/DashboardWidget.vue'
import DashboardTools from '@/components/home/DashboardTools.vue'
import DashboardHeader from '@/components/home/DashboardHeader.vue'
import DashboardParticles from '@/components/home/DashboardParticles.vue'

/**
 * 首页视图组件
 * 
 * 功能：
 * 1. 显示用户欢迎信息和打字效果
 * 2. 展示仪表板指标卡片（任务组数、进行中任务、逾期任务、已完成任务等）
 * 3. 提供可配置的组件插槽（最近任务、逾期任务等）
 * 4. 支持用户自定义仪表板配置（指标类型、组件类型）
 * 5. 配置持久化到 localStorage
 */
export default {
  name: 'HomeView',
  components: {
    DashboardMetrics,
    DashboardWidget,
    DashboardTools,
    DashboardHeader,
    DashboardParticles
  },
  data () {
    return {
      // 用户名称（大写显示）
      userName: '',
      // 当前日期（ISO 格式）
      currentDate: '',
      // 打字效果显示的文本
      typedText: '',
      // 打字效果定时器
      typingTimer: null,
      // 打字速度（毫秒/字符）
      typingSpeed: 70,
      // 欢迎语模板集合（随机选择）
      greetingTemplates: [
        '欢迎回来，{name}。系统已初始化完毕，等待指令输入。',
        '检测到操作员 {name} 登录。正在同步神经元数据链接...',
        '保持专注，{name}。今日的任务队列尚未清空，请尽快处理。',
        '早上好，{name}。大气层外通讯正常，开始新的一天。',
        '警告：检测到 {name} 的创造力指数正在上升。建议立即投入工作。',
        '{name}，即使是在末日废土，也要保持代码的整洁与优雅。',
        '{name}，工作的时候一定要保持全神贯注......嗯，全神贯注。',
        '{name}，您还有许多事情需要处理。现在还不能休息哦。'
      ],
      
      // ========== 仪表板配置 ==========
      dashboardConfig: {
        // 指标键数组（显示在顶部的指标卡片）
        metrics: ['groupCount', 'inProgressCount', 'overdueCount', 'completedCount'],
        // 左侧组件键数组（显示在左侧面板的组件）
        leftWidgets: ['recentTasks', 'overdueTasks']
      },
      
      // ========== 数据存储 ==========
      // 指标值映射（key -> value）
      metricValues: {},
      // 组件数据映射（key -> data array）
      widgetData: {},
      // 任务组名称映射（taskGroupId -> groupName）
      groupNameMap: {}
    }
  },
  /**
   * 组件挂载时初始化
   */
  mounted () {
    this.currentDate = new Date().toISOString().split('T')[0]
    this.loadUserName()
    this.loadConfig()
    this.initDashboard()
  },
  methods: {
    /**
     * ========== 配置管理方法 ==========
     */
    
    /**
     * 从 localStorage 加载仪表板配置
     * 如果配置不存在或格式错误，使用默认配置
     */
    loadConfig () {
      try {
        const saved = localStorage.getItem('DUCK_DASHBOARD_CONFIG')
        if (saved) {
          const parsed = JSON.parse(saved)
          // 验证并应用指标配置（必须为4个）
          if (parsed.metrics && parsed.metrics.length === 4) {
            this.dashboardConfig.metrics = parsed.metrics
          }
          // 验证并应用组件配置（必须为2个）
          if (parsed.leftWidgets && parsed.leftWidgets.length === 2) {
            this.dashboardConfig.leftWidgets = parsed.leftWidgets
          }
        }
      } catch (e) {
        console.error('加载仪表板配置失败', e)
      }
    },
    
    /**
     * 保存仪表板配置到 localStorage
     * 保存后重新初始化仪表板以加载新配置的数据
     */
    saveDashboardConfig () {
      localStorage.setItem('DUCK_DASHBOARD_CONFIG', JSON.stringify(this.dashboardConfig))
      // 重新初始化仪表板以加载新配置的数据
      this.initDashboard()
    },
    
    /**
     * 更新指标配置
     * @param {Array<string>} newKeys - 新的指标键数组
     */
    updateMetricsConfig (newKeys) {
      this.dashboardConfig.metrics = newKeys
      this.saveDashboardConfig()
    },
    
    /**
     * 更新组件配置
     * @param {number} idx - 组件索引（0 或 1）
     * @param {string} newKey - 新的组件键
     */
    updateWidgetConfig (idx, newKey) {
      this.$set(this.dashboardConfig.leftWidgets, idx, newKey)
      this.saveDashboardConfig()
    },
    
    /**
     * ========== 初始化方法 ==========
     */
    
    /**
     * 初始化仪表板
     * 1. 加载任务组信息（用于任务项显示）
     * 2. 加载所有配置的指标数据
     * 3. 加载所有配置的组件数据
     * 4. 启动欢迎语打字效果
     */
    async initDashboard () {
      // 加载任务组信息（用于任务项显示任务组名称）
      await this.loadGroupInfo()
      // 加载所有配置的指标数据
      this.dashboardConfig.metrics.forEach(mKey => this.loadMetricData(mKey))
      // 加载所有配置的组件数据
      this.dashboardConfig.leftWidgets.forEach(wKey => this.loadWidgetData(wKey))
      // 随机选择欢迎语模板并启动打字效果
      const randomGreeting = this.greetingTemplates[
        Math.floor(Math.random() * this.greetingTemplates.length)
      ].replace('{name}', this.userName || 'User')
      this.startTyping(randomGreeting)
    },
    
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载任务组信息
     * 构建任务组ID到名称的映射，用于任务项显示
     */
    async loadGroupInfo () {
      try {
        const res = await getMyTaskGroupList()
        const list = Array.isArray(res) ? res : (res.list || [])
        const map = {}
        // 构建映射：优先使用别名，其次使用任务组名称
        list.forEach(g => {
          map[g.taskGroupId] = g.groupAlias || g.taskGroupName
        })
        this.groupNameMap = map
      } catch (e) {
        // 加载失败时静默处理，不影响其他功能
      }
    },
    
    /**
     * 加载指标数据
     * @param {string} key - 指标键（如 'groupCount', 'inProgressCount' 等）
     */
    async loadMetricData (key) {
      // 如果指标值已存在且不为占位符，则跳过加载
      if (this.metricValues[key] !== undefined && this.metricValues[key] !== '-') {
        return
      }
      
      try {
        let val = 0
        
        // 根据指标键调用不同的API
        if (key === 'teamCount' || key === 'groupCount') {
          // 团队数或任务组数
          const res = await statsApi.getMyJoinedOverview()
          val = (key === 'teamCount' ? res.teamCount : res.taskGroupCount) || 0
        } else if (key === 'inProgressCount') {
          // 进行中任务数
          const res = await statsApi.getMyInProgressOverview()
          val = res.tasks || 0
        } else if (key === 'overdueCount') {
          // 逾期任务数
          const res = await statsApi.getMyOverdueOverview()
          val = res.total || 0
        } else if (key === 'completedWeek') {
          // 本周完成任务数
          const res = await statsApi.getMyCompletedWeekOverview()
          val = res.count || 0
        } else if (key === 'completedMonth') {
          // 本月完成任务数
          const res = await statsApi.getMyCompletedMonthOverview()
          val = res.count || 0
        } else if (key === 'completedTotal') {
          // 总完成任务数
          const res = await statsApi.getMyCompletedTotalOverview()
          val = res.count || 0
        }
        
        // 使用 $set 确保响应式更新
        this.$set(this.metricValues, key, val)
      } catch (e) {
        // 加载失败时设置为占位符
        this.$set(this.metricValues, key, '-')
      }
    },
    
    /**
     * 加载组件数据
     * @param {string} key - 组件键（如 'recentTasks', 'overdueTasks' 等）
     */
    async loadWidgetData (key) {
      // 如果组件数据已存在，则跳过加载
      if (this.widgetData[key]) {
        return
      }
      
      // 先设置占位符数组，避免组件显示为空
      this.$set(this.widgetData, key, [])
      
      try {
        if (key === 'recentTasks') {
          // 最近任务：获取所有任务
          const res = await getTaskListByUserId({ sortByMode: 0, taskStatus: [1,2] })
          const list = (Array.isArray(res) ? res : (res.list || [])).map(i => i.task || i)
          // 过滤出有到期时间的任务
          const tasksWithDueTime = list.filter(t => t.dueTime)
          // 按到期时间排序，取前10条
          tasksWithDueTime.sort((a, b) => new Date(a.dueTime) - new Date(b.dueTime))
          this.$set(this.widgetData, key, tasksWithDueTime.slice(0, 10).map(this.mapTaskItem))
        } else if (key === 'overdueTasks') {
          // 逾期任务：获取已过期的任务
          // 获取当前日期（YYYY-MM-DD格式）
          const today = new Date()
          const todayStr = today.toISOString().split('T')[0]
          const res = await getTaskListByUserId({ 
            sortByMode: 0, 
            taskStatus: [1, 2],
            EndDueTime: todayStr  // 截止日期止为当前日期
          })
          const list = (Array.isArray(res) ? res : (res.list || [])).map(i => i.task || i)
          const todayStart = new Date(new Date().setHours(0, 0, 0, 0))
          const overdue = list.filter(t => t.dueTime && new Date(t.dueTime) < todayStart)
          // 按到期时间排序
          overdue.sort((a, b) => new Date(a.dueTime) - new Date(b.dueTime))
          this.$set(this.widgetData, key, overdue.map(this.mapTaskItem))
        } else if (key === 'loadTrend') {
          // 负载趋势：获取14天的负载趋势数据
          const res = await statsApi.getMyLoadTrend(14)
          const data = res.items || []
          this.$set(this.widgetData, key, data)
        } else if (key === 'taskTrend') {
          // 任务趋势：获取一周的任务趋势数据
          const res = await statsApi.getMyTaskTrend({ range: 'week' })
          const data = res.items || []
          this.$set(this.widgetData, key, data)
        }
      } catch (e) {
        console.error('组件数据加载失败', key, e)
      }
    },
    
    /**
     * ========== 辅助方法 ==========
     */
    
    /**
     * 映射任务项数据
     * 将API返回的任务对象转换为组件需要的格式
     * @param {Object} t - 任务对象
     * @returns {Object} 映射后的任务项对象
     */
    mapTaskItem (t) {
      return {
        id: t.taskId,
        name: t.taskName,
        groupName: this.groupNameMap[t.taskGroupId], // 从映射中获取任务组名称
        time: t.dueTime,
        groupId: t.taskGroupId
      }
    },
    
    /**
     * 处理组件项点击事件
     * 跳转到任务详情页面
     * @param {Object} item - 被点击的任务项对象
     */
    handleItemClick (item) {
      if (item.id) {
        this.$router.push({ name: 'recent', query: { taskId: item.id } })
      }
    },
    
    /**
     * ========== 通用方法 ==========
     */
    
    /**
     * 加载用户名称
     * 优先从API获取，失败时从localStorage获取
     */
    async loadUserName () {
      try {
        const me = await getMe()
        this.userName = (me.userName || me.name || '').toUpperCase()
      } catch (e) {
        // API失败时从localStorage获取
        this.userName = (localStorage.getItem('userName') || '').toUpperCase()
      }
    },
    
    /**
     * 启动打字效果
     * 逐字符显示文本，模拟打字机效果
     * @param {string} text - 要显示的文本
     */
    startTyping (text) {
      // 清除之前的定时器
      clearInterval(this.typingTimer)
      let i = 0
      this.typedText = ''
      
      // 设置定时器，逐字符添加文本
      this.typingTimer = setInterval(() => {
        if (i < text.length) {
          this.typedText += text.charAt(i)
          i++
        } else {
          // 文本显示完成，清除定时器
          clearInterval(this.typingTimer)
        }
      }, this.typingSpeed)
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/home/home-view.css';
</style>

<style>
/* ==========================================================================
   全局样式：复古对话框样式
   注意：这些样式必须是全局的，因为 Element UI 的对话框会 append 到 body
   ========================================================================== */

/* 复古对话框头部 */
.home-retro-dialog .el-dialog__header {
  /* 使用全局边框宽度变量 */
  border-bottom: var(--border-width-bold) solid var(--color-black);
  padding: 16px 20px;
  /* 使用全局背景色变量 */
  background: var(--color-white);
}

/* 复古对话框标题 */
.home-retro-dialog .el-dialog__title {
  /* 使用全局字体族变量 */
  font-family: var(--font-family);
  font-weight: 900;
  text-transform: uppercase;
  /* 使用全局颜色变量 */
  color: var(--color-black) !important;
}

/* 复古对话框主体 */
.home-retro-dialog .el-dialog__body {
  padding: 20px;
  background: #f4f4f4;
}

/* 复古对话框底部 */
.home-retro-dialog .el-dialog__footer {
  /* 使用全局边框宽度变量 */
  border-top: var(--border-width-bold) solid var(--color-black);
  padding: 16px 20px;
  /* 使用全局背景色变量 */
  background: var(--color-white);
}

/* 复古选择器输入框 */
.home-retro-select .el-input__inner {
  /* 使用全局边框宽度和颜色变量 */
  border: var(--border-width-bold) solid var(--color-black) !important;
  /* 使用全局圆角变量 */
  border-radius: var(--border-radius) !important;
  /* 使用全局字体族变量 */
  font-family: var(--font-family);
}

/* 复古按钮：主要样式 */
.home-retro-btn-primary {
  /* 使用全局颜色变量 */
  background: var(--color-black) !important;
  border-color: var(--color-black) !important;
  color: var(--color-white) !important;
  /* 使用全局圆角变量 */
  border-radius: var(--border-radius) !important;
  font-weight: bold;
}

/* 复古按钮：普通样式 */
.home-retro-btn-plain {
  /* 使用全局颜色变量 */
  background: var(--color-white) !important;
  border-color: var(--color-black) !important;
  color: var(--color-black) !important;
  /* 使用全局圆角变量 */
  border-radius: var(--border-radius) !important;
}
</style>
