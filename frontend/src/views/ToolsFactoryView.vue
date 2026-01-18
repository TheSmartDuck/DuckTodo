<template>
  <div class="tools-factory-page">
    <!-- 左侧工具列表 -->
    <div class="tools-factory-left-panel">
      <!-- 搜索和分类选择区域 -->
      <div class="tools-factory-search-section">
        <div class="tools-factory-search-row">
          <!-- 搜索框 -->
          <div class="tools-factory-search-box">
            <i class="el-icon-search tools-factory-search-icon"></i>
            <input 
              v-model="searchKeyword" 
              class="tools-factory-search-input" 
              placeholder="SEARCH_TOOL_NAME..." 
              @input="handleSearch"
            />
            <div class="tools-factory-search-corner"></div>
          </div>
          
          <!-- 分类选择器 -->
          <el-select
            v-model="selectedCategory"
            placeholder="全部类型"
            class="tools-factory-category-select"
            popper-class="tools-factory-category-select-dropdown"
            clearable
          >
            <el-option
              v-for="category in availableCategories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </div>
      </div>

      <!-- 工具列表 -->
      <div class="tools-factory-tool-list">
        <div 
          v-for="tool in filteredTools" 
          :key="tool.id"
          class="tools-factory-tool-card"
          :class="{ 'tools-factory-tool-card-active': selectedToolId === tool.id }"
          @click="selectTool(tool.id)"
        >
          <div class="tools-factory-tool-card-content">
            <div class="tools-factory-tool-card-header">
              <div 
                class="tools-factory-tool-card-title-wrapper"
                :ref="`title-wrapper-${tool.id}`"
              >
                <div 
                  class="tools-factory-tool-card-title-track"
                  :class="{ 'tools-factory-title-animate': titleOverflowMap[tool.id] }"
                >
                  <span 
                    class="tools-factory-tool-card-title"
                    :ref="`title-${tool.id}`"
                  >{{ tool.name }}</span>
                  <span 
                    v-if="titleOverflowMap[tool.id]"
                    class="tools-factory-tool-card-title-duplicate"
                  >{{ tool.name }}</span>
                </div>
              </div>
              <span class="tools-factory-tool-card-badge" v-if="tool.badge">{{ tool.badge }}</span>
            </div>
            <div class="tools-factory-tool-card-description">{{ tool.description }}</div>
            <div class="tools-factory-tool-card-footer">
              <span 
                v-for="(cat, index) in tool.category" 
                :key="index"
                class="tools-factory-tool-card-tag"
              >
                {{ cat }}
              </span>
            </div>
          </div>
          <div class="tools-factory-tool-card-indicator">
            <span class="tools-factory-tool-card-indicator-text">>></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧工具界面 -->
    <div class="tools-factory-right-panel">
      <div v-if="selectedTool" class="tools-factory-tool-view">
        <!-- 工具视图头部 -->
        <div class="tools-factory-tool-view-header">
          <div class="tools-factory-tool-view-title-group">
            <span class="tools-factory-tool-view-square">■</span>
            <span class="tools-factory-tool-view-title">{{ selectedTool.name }}</span>
            <span class="tools-factory-tool-view-line-deco"></span>
          </div>
        </div>

        <!-- 工具视图内容 -->
        <div class="tools-factory-tool-view-content">
          <!-- 这里可以动态加载不同的工具组件 -->
          <component 
            v-if="selectedTool.component" 
            :is="selectedTool.component"
            :tool="selectedTool"
          />
          <div v-else class="tools-factory-tool-view-placeholder">
            <div class="tools-factory-placeholder-text">{{ selectedTool.description }}</div>
            <div class="tools-factory-placeholder-hint">工具界面开发中...</div>
          </div>
        </div>
      </div>
      <div v-else class="tools-factory-empty-state">
        <div class="tools-factory-empty-icon">>></div>
        <div class="tools-factory-empty-text">请从左侧选择一个工具</div>
      </div>
    </div>
  </div>
</template>

<script>
// 动态导入工具组件（按需加载）
import DailyReportTool from '@/components/tools-factory/DailyReportTool.vue'

export default {
  name: 'ToolsFactoryView',
  components: {
    // 在这里注册工具组件
    DailyReportTool
  },
  data() {
    return {
      searchKeyword: '',
      selectedToolId: null,
      selectedCategory: null, // 选中的分类，null 表示"全部类型"
      titleOverflowMap: {}, // 存储每个工具标题是否溢出
      // 工具列表（示例数据，后续可以从 API 获取）
      tools: [
        {
          id: 'daily-report',
          name: '日报生成',
          description: '生成每日工作日报，包含今日完成、明日待办和思考总结',
          category: ['REPORT', 'ANALYSIS'], // 支持多个分类
          badge: 'NEW',
          component: 'DailyReportTool' // 关联 DailyReportTool 组件
        }
      ]
    }
  },
  computed: {
    // 获取所有可用的分类（从数组 category 中提取）
    availableCategories() {
      const categoriesSet = new Set()
      this.tools.forEach(tool => {
        if (Array.isArray(tool.category)) {
          tool.category.forEach(cat => categoriesSet.add(cat))
        } else {
          // 兼容旧数据格式（字符串类型）
          categoriesSet.add(tool.category)
        }
      })
      return Array.from(categoriesSet).sort()
    },
    filteredTools() {
      let result = this.tools

      // 按分类筛选
      if (this.selectedCategory) {
        result = result.filter(tool => {
          if (Array.isArray(tool.category)) {
            return tool.category.includes(this.selectedCategory)
          } else {
            // 兼容旧数据格式
            return tool.category === this.selectedCategory
          }
        })
      }

      // 按关键词筛选
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(tool => {
          const nameMatch = tool.name.toLowerCase().includes(keyword)
          const descMatch = tool.description.toLowerCase().includes(keyword)
          const categoryMatch = Array.isArray(tool.category)
            ? tool.category.some(cat => cat.toLowerCase().includes(keyword))
            : tool.category.toLowerCase().includes(keyword)
          return nameMatch || descMatch || categoryMatch
        })
      }

      return result
    },
    selectedTool() {
      return this.tools.find(tool => tool.id === this.selectedToolId)
    }
  },
  watch: {
    filteredTools: {
      handler() {
        // 当筛选结果变化时，重新检查溢出
        this.checkTitleOverflow()
      },
      immediate: false
    }
  },
  mounted() {
    // 组件挂载后检查标题溢出
    setTimeout(() => {
      this.checkTitleOverflow()
    }, 100)
    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 清理事件监听
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    handleSearch() {
      // 搜索逻辑已在 computed 中处理
    },
    selectTool(toolId) {
      this.selectedToolId = toolId
    },
    /**
     * 处理窗口大小变化
     */
    handleResize() {
      this.checkTitleOverflow()
    },
    /**
     * 检查标题是否溢出
     */
    checkTitleOverflow() {
      this.$nextTick(() => {
        this.filteredTools.forEach(tool => {
          const titleRef = this.$refs[`title-${tool.id}`]
          const wrapperRef = this.$refs[`title-wrapper-${tool.id}`]
          
          if (titleRef && wrapperRef) {
            // 处理数组情况（v-for 可能返回数组）
            const titleElement = Array.isArray(titleRef) ? titleRef[0] : titleRef
            const wrapperElement = Array.isArray(wrapperRef) ? wrapperRef[0] : wrapperRef
            
            if (titleElement && wrapperElement) {
              // 确保元素已渲染
              if (titleElement.offsetWidth > 0 && wrapperElement.offsetWidth > 0) {
                const isOverflow = titleElement.scrollWidth > wrapperElement.clientWidth
                this.$set(this.titleOverflowMap, tool.id, isOverflow)
              }
            }
          }
        })
      })
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/tools-factory/tools-factory-view.css';
</style>
