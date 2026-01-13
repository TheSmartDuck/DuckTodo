<template>
  <!-- 数据概览侧边栏根容器 -->
  <div class="data-overview-sidebar">
    <!-- 侧边栏头部：标题和设置图标 -->
    <div class="data-overview-sidebar-header">
      <span class="data-overview-sidebar-title">OVERVIEW / 数据概览</span>
      <i class="el-icon-setting" style="color: #ccc; cursor: pointer;"></i>
    </div>
    
    <!-- 侧边栏内容区域 -->
    <div class="data-overview-sidebar-content">
      <!-- 1. 加入统计区域（磁带未来主义配色：铁锈红+鼠尾草绿） -->
      <div class="data-overview-sidebar-section">
        <div class="data-overview-sidebar-section-header">
          <i class="el-icon-s-home"></i> 加入统计 / JOINED
        </div>
        <div class="data-overview-sidebar-stat-grid">
          <!-- 团队数量统计框（使用铁锈红 mission-rust） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-rust">
            <div class="data-overview-sidebar-stat-val">{{ joined.teamCount }}</div>
            <div class="data-overview-sidebar-stat-label">TEAMS / 团队</div>
          </div>
          <!-- 任务组数量统计框（使用鼠尾草绿 oxidized-sage） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-sage">
            <div class="data-overview-sidebar-stat-val">{{ joined.taskGroupCount }}</div>
            <div class="data-overview-sidebar-stat-label">GROUPS / 任务组</div>
          </div>
        </div>
      </div>

      <!-- 2. 进行中统计区域（磁带未来主义配色：铁锈红+芥末黄） -->
      <div class="data-overview-sidebar-section">
        <div class="data-overview-sidebar-section-header">
          <i class="el-icon-time"></i> 进行中 / IN_PROGRESS
        </div>
        <div class="data-overview-sidebar-stat-grid">
          <!-- 任务数量统计框（使用铁锈红 mission-rust） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-rust">
            <div class="data-overview-sidebar-stat-val">{{ inProgress.tasks }}</div>
            <div class="data-overview-sidebar-stat-label">TASKS / 任务</div>
          </div>
          <!-- 子任务数量统计框（使用芥末黄 avionics-mustard） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-mustard">
            <div class="data-overview-sidebar-stat-val">{{ inProgress.childTasks }}</div>
            <div class="data-overview-sidebar-stat-label">SUBS / 子任务</div>
          </div>
        </div>
      </div>

      <!-- 3. 已完成统计区域（磁带未来主义配色：鼠尾草绿） -->
      <div class="data-overview-sidebar-section">
        <div class="data-overview-sidebar-section-header">
          <i class="el-icon-circle-check"></i> 已完成 / COMPLETED
        </div>
        <!-- 三列网格布局 -->
        <div class="data-overview-sidebar-stat-grid data-overview-sidebar-stat-grid-three-col">
          <!-- 本周完成数量统计框（使用鼠尾草绿 oxidized-sage） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-sage">
            <div class="data-overview-sidebar-stat-val">{{ completed.week }}</div>
            <div class="data-overview-sidebar-stat-label">
              <div class="data-overview-sidebar-stat-label-en">/* week */</div>
              <div class="data-overview-sidebar-stat-label-cn">本周完成</div>
            </div>
          </div>
          <!-- 本月完成数量统计框（使用鼠尾草绿 oxidized-sage） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-sage">
            <div class="data-overview-sidebar-stat-val">{{ completed.month }}</div>
            <div class="data-overview-sidebar-stat-label">
              <div class="data-overview-sidebar-stat-label-en">/* month */</div>
              <div class="data-overview-sidebar-stat-label-cn">本月完成</div>
            </div>
          </div>
          <!-- 总计完成数量统计框（使用鼠尾草绿 oxidized-sage） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-sage">
            <div class="data-overview-sidebar-stat-val">{{ completed.total }}</div>
            <div class="data-overview-sidebar-stat-label">
              <div class="data-overview-sidebar-stat-label-en">/* total */</div>
              <div class="data-overview-sidebar-stat-label-cn">总计完成</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 4. 逾期警告统计区域（磁带未来主义配色：酒红） -->
      <div class="data-overview-sidebar-section">
        <!-- 黑色警告头部 -->
        <div class="data-overview-sidebar-section-header">
          <i class="el-icon-warning"></i> 逾期警告 / OVERDUE
        </div>
        <!-- 三列网格布局 -->
        <div class="data-overview-sidebar-stat-grid data-overview-sidebar-stat-grid-three-col">
          <!-- 总计逾期数量统计框（使用酒红 alarm-burgundy） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-burgundy">
            <div class="data-overview-sidebar-stat-val">{{ overdue.total }}</div>
            <div class="data-overview-sidebar-stat-label">
              <div class="data-overview-sidebar-stat-label-en">/* total */</div>
              <div class="data-overview-sidebar-stat-label-cn">总计逾期</div>
            </div>
          </div>
          <!-- 中度逾期数量统计框（使用酒红 alarm-burgundy） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-burgundy">
            <div class="data-overview-sidebar-stat-val">{{ overdue.moderate }}</div>
            <div class="data-overview-sidebar-stat-label">
              <div class="data-overview-sidebar-stat-label-en">/* mo */</div>
              <div class="data-overview-sidebar-stat-label-cn">中度逾期</div>
            </div>
          </div>
          <!-- 严重逾期数量统计框（使用酒红 alarm-burgundy） -->
          <div class="data-overview-sidebar-stat-box data-overview-sidebar-stat-box-burgundy">
            <div class="data-overview-sidebar-stat-val">{{ overdue.severe }}</div>
            <div class="data-overview-sidebar-stat-label">
              <div class="data-overview-sidebar-stat-label-en">/* se */</div>
              <div class="data-overview-sidebar-stat-label-cn">严重逾期</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 状态栏：显示在线状态 -->
      <div class="data-overview-sidebar-status-bar">
          STATUS: ONLINE
      </div>
    </div>
  </div>
</template>

<script>
import * as statsApi from '@/api/statistics-api'

/**
 * 数据概览侧边栏组件
 * 
 * 功能：
 * 1. 显示用户加入的团队和任务组数量
 * 2. 显示进行中的任务和子任务数量
 * 3. 显示已完成的任务统计（本周、本月、总计）
 * 4. 显示逾期任务统计（总计、中度逾期、严重逾期）
 * 5. 显示系统在线状态
 * 
 * 数据来源：
 * - 通过 statistics-api 获取各项统计数据
 * - 组件挂载时自动加载数据
 */
export default {
  name: 'DataOverviewSidebar',
  data () {
    return {
      // 加入统计：团队和任务组数量
      joined: { 
        teamCount: 0,      // 团队数量
        taskGroupCount: 0  // 任务组数量
      },
      // 进行中统计：任务和子任务数量
      inProgress: { 
        tasks: 0,        // 任务数量
        childTasks: 0    // 子任务数量
      },
      // 已完成统计：按时间周期分类
      completed: { 
        week: 0,   // 本周完成数量
        month: 0,  // 本月完成数量
        total: 0   // 总计完成数量
      },
      // 逾期统计：按严重程度分类
      overdue: { 
        total: 0,      // 总计逾期数量
        moderate: 0,   // 中度逾期数量
        severe: 0      // 严重逾期数量
      }
    }
  },
  /**
   * 组件挂载后的生命周期钩子
   * 自动加载统计数据
   */
  mounted () {
    this.loadData()
  },
  methods: {
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载统计数据
     * 并行请求所有统计API，更新组件数据
     * 
     * API调用：
     * - getMyJoinedOverview(): 获取加入统计
     * - getMyInProgressOverview(): 获取进行中统计
     * - getMyCompletedWeekOverview(): 获取本周完成统计
     * - getMyCompletedMonthOverview(): 获取本月完成统计
     * - getMyCompletedTotalOverview(): 获取总计完成统计
     * - getMyOverdueOverview(): 获取逾期统计
     */
    async loadData () {
      try {
        // 并行请求所有统计数据API
        const [j, ip, cw, cm, ct, od] = await Promise.all([
          statsApi.getMyJoinedOverview(),           // 加入统计
          statsApi.getMyInProgressOverview(),       // 进行中统计
          statsApi.getMyCompletedWeekOverview(),     // 本周完成统计
          statsApi.getMyCompletedMonthOverview(),   // 本月完成统计
          statsApi.getMyCompletedTotalOverview(),   // 总计完成统计
          statsApi.getMyOverdueOverview()          // 逾期统计
        ])
        
        // 更新组件数据
        this.joined = j
        this.inProgress = ip
        // 合并完成统计数据
        this.completed = { 
          week: cw.count, 
          month: cm.count, 
          total: ct.count 
        }
        this.overdue = od
      } catch (e) {
        console.error('Overview load failed', e)
      }
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/data-panel/data-overview-sidebar.css';
</style>
