<template>
  <div class="sidebar-team-list-wrapper">
    <!-- 子模块：项目团队列表（拖拽排序、右键菜单） -->
    <div class="sidebar-team-list-section-title" @click="toggle">
      <i :class="expanded ? 'el-icon-caret-bottom' : 'el-icon-caret-right'" class="sidebar-team-list-toggle-icon"></i>
      <span>项目团队</span>
    </div>
    <el-menu
      class="sidebar-team-list-side-menu"
      :default-active="active"
      @select="onSelect"
      @dragover.native.prevent="onListDragOver($event)"
      @drop.native.prevent="onListDrop($event)"
      v-show="expanded"
    >
      <el-menu-item
        v-for="(t, idx) in teams"
        :key="t.teamId"
        :index="'team-' + t.teamId"
        @contextmenu.native.prevent="onTeamContextMenu(t, $event)"
        :class="{ 
          'sidebar-team-list-dragging': draggingTeamId === t.teamId, 
          'sidebar-team-list-drag-over': dragOverTeamId === t.teamId,
          'sidebar-team-list-shift-down': isDragging && dragInsertIndex !== -1 && idx >= dragInsertIndex && draggingTeamId !== t.teamId
        }"
        :draggable="true"
        ref="teamItem"
        @dragstart.native="onDragStart(t, $event)"
        @dragenter.native="onDragEnter(t, $event)"
        @dragover.native.prevent="onDragOver(t, idx, $event)"
        @drop.native="onDrop(t, $event)"
        @dragend.native="onDragEnd($event)"
      >
        <span class="sidebar-team-list-dot" :style="{ background: (t.teamColor || '#c0c4cc') }"></span>
        <span class="sidebar-team-list-text" :title="t.teamName">{{ t.teamName || '未命名项目' }}</span>
      </el-menu-item>
      <el-menu-item index="team-add">
        <i class="el-icon el-icon-plus sidebar-team-list-nav-icon sidebar-team-list-plus"></i>
        <span class="sidebar-team-list-text">&nbsp;新增</span>
      </el-menu-item>
    </el-menu>

    <!-- 模块：项目团队右键菜单（显示完整名、修改颜色） -->
    <div v-if="contextMenu.visible" class="sidebar-team-list-context-menu-mask" @click="closeContextMenu"></div>
    <div
      v-if="contextMenu.visible"
      class="sidebar-team-list-context-menu"
      :style="{ top: contextMenu.y + 'px', left: contextMenu.x + 'px' }"
      ref="contextMenu"
      @click.stop
      @contextmenu.stop.prevent
    >
      <!-- 顶部黑色栏 -->
      <div class="sidebar-team-list-context-menu-header">
        <span class="sidebar-team-list-header-id">{{ contextMenu.teamId }}</span>
      </div>

      <div class="sidebar-team-list-context-menu-body">
        <div class="sidebar-team-list-menu-title-row">
          <div class="sidebar-team-list-menu-main-title" :title="contextMenu.teamName">
            {{ contextMenu.teamName || '未命名项目' }}
          </div>
        </div>

        <div class="sidebar-team-list-color-section">
          <div class="sidebar-team-list-section-label">标签颜色 / LABEL COLOR</div>
          <div class="sidebar-team-list-current-color-row">
             <span class="sidebar-team-list-preview-dot" :style="{ background: (contextMenu.color || '#409EFF') }"></span>
             <span class="sidebar-team-list-current-color-text">{{ contextMenu.color || '#409EFF' }}</span>
          </div>

          <div class="sidebar-team-list-palette-container">
            <div class="sidebar-team-list-palette-group" v-for="p in sortedColorPalettes" :key="p.key">
              <div class="sidebar-team-list-palette-group-header" @click="togglePalette(p.key)">
                <i :class="p.expanded ? 'el-icon-caret-bottom' : 'el-icon-caret-right'" class="sidebar-team-list-toggle-icon"></i>
                <span class="sidebar-team-list-palette-group-title">{{ p.name }}</span>
              </div>
              <div class="sidebar-team-list-palette" v-show="p.expanded">
                <button
                  v-for="c in p.colorsSorted"
                  :key="c"
                  class="sidebar-team-list-color-swatch"
                  :style="{ background: c, '--swatch-color': c }"
                  :class="{ 'sidebar-team-list-color-swatch--active': isActiveColor(c) }"
                  @click="selectPresetColor(c)"
                  :title="c"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 底部按钮 -->
      <div class="sidebar-team-list-menu-footer">
        <div class="sidebar-team-list-footer-btn sidebar-team-list-footer-btn--cancel" @click="closeContextMenu">CLOSE</div>
      </div>
    </div>
  </div>
</template>

<script>
import { updateMyTeamColor, swapMyTeamOrder, getMyRelatedTeamList } from '@/api/team-api'
import { getMe } from '@/api/user-api'
import colorPalettesData from '@/assets/common/color-palettes.json'

/**
 * 侧边栏项目团队列表组件
 * 功能：
 * 1. 显示和管理项目团队列表
 * 2. 支持拖拽排序
 * 3. 支持右键菜单进行颜色设置
 * 4. 支持展开/折叠列表
 */
export default {
  name: 'SidebarTeamList',
  data() {
    return {
      // 项目团队列表数据
      teams: [],
      // 当前选中的菜单项索引
      active: '',
      // 列表展开/折叠状态
      expanded: true,
      
      // ========== 拖拽排序相关状态 ==========
      // 当前正在拖拽的团队ID
      draggingTeamId: '',
      // 当前鼠标悬停的目标团队ID
      dragOverTeamId: '',
      // 全局拖拽激活标志
      isDragging: false,
      // 计算出的插入位置索引
      dragInsertIndex: -1,
      // 拖拽开始前的列表顺序备份（用于回滚）
      dragStartOrder: [],
      
      // ========== 右键菜单相关状态 ==========
      // 是否正在更新团队颜色（防止重复请求）
      isUpdatingColor: false,
      // 当前用户ID
      currentUserId: '',
      // 右键菜单数据
      contextMenu: {
        visible: false,        // 菜单是否可见
        x: 0,                 // 菜单X坐标
        y: 0,                 // 菜单Y坐标
        teamId: '',           // 团队ID
        teamName: '',         // 团队名称
        color: '#409EFF'      // 团队颜色
      },
      
      // ========== 颜色配置 ==========
      // 预设颜色列表
      presetColors: colorPalettesData.presetColors,
      // 颜色色板集合（包含多个色板系列）
      colorPalettes: colorPalettesData.colorPalettes
    }
  },
  watch: {
    /**
     * 监听路由变化，同步菜单选中状态
     * 根据当前路由自动高亮对应的团队菜单项
     */
    $route: {
      handler(to) {
        if (!to) return
        // 创建团队页：高亮新增按钮
        if (to.name === 'team-create') {
          this.active = 'team-add'
        } 
        // 团队详情页：高亮对应团队
        else if (to.name === 'team-detail' && to.params && to.params.teamId) {
          this.active = 'team-' + String(to.params.teamId)
        } 
        // 其他页面：取消高亮
        else {
          this.active = ''
        }
      },
      immediate: true
    }
  },
  computed: {
    /**
     * 按色相排序的预设颜色列表
     * 用于在颜色选择器中更好地展示颜色
     */
    sortedPresetColors () {
      return this.sortColorsByHue(this.presetColors)
    },
    /**
     * 按色相排序的所有色板集合
     * 每个色板内的颜色都按色相排序
     */
    sortedColorPalettes() {
      return this.colorPalettes.map(p => ({
        ...p,
        colorsSorted: this.sortColorsByHue(p.colors)
      }))
    }
  },
  /**
   * 组件挂载时初始化
   */
  mounted() {
    // 捕获阶段监听全局点击，点击菜单外部自动关闭
    document.addEventListener('click', this.handleGlobalClick, true)
    this.loadMe()
    this.fetchMyTeams()
    // 监听全局刷新事件
    if (this.$root && this.$root.$on) {
      this.$root.$on('refresh-my-teams', this.fetchMyTeams)
    }
  },
  /**
   * 组件销毁前清理
   */
  beforeDestroy() {
    // 移除全局点击监听
    document.removeEventListener('click', this.handleGlobalClick, true)
    // 移除全局事件监听
    if (this.$root && this.$root.$off) {
      this.$root.$off('refresh-my-teams', this.fetchMyTeams)
    }
  },
  methods: {
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 按色相（Hue）对颜色数组进行排序
     * @param {Array<string>} colors - 颜色数组（HEX格式）
     * @returns {Array<string>} 排序后的颜色数组
     */
    sortColorsByHue(colors) {
      if (!Array.isArray(colors)) return []
      
      const toRgb = (hex) => {
        const v = this.normalizeHex(hex)
        if (!v) return null
        const r = parseInt(v.substring(0, 2), 16)
        const g = parseInt(v.substring(2, 4), 16)
        const b = parseInt(v.substring(4, 6), 16)
        return { r, g, b }
      }
      
      const rgbToHsl = (r, g, b) => {
        r /= 255; g /= 255; b /= 255
        const max = Math.max(r, g, b), min = Math.min(r, g, b)
        let h, s, l = (max + min) / 2
        if (max === min) {
          h = s = 0
        } else {
          const d = max - min
          s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
          switch (max) {
            case r: h = (g - b) / d + (g < b ? 6 : 0); break
            case g: h = (b - r) / d + 2; break
            case b: h = (r - g) / d + 4; break
          }
          h /= 6
        }
        return { h: h * 360, s, l }
      }
      
      return colors.slice().sort((a, b) => {
        const c1 = toRgb(a)
        const c2 = toRgb(b)
        if (!c1 || !c2) return 0
        const h1 = rgbToHsl(c1.r, c1.g, c1.b)
        const h2 = rgbToHsl(c2.r, c2.g, c2.b)
        return h1.h - h2.h
      })
    },
    
    /**
     * 标准化HEX颜色代码为6位格式
     * @param {string} hex - HEX颜色代码（如 #FFF 或 #FFFFFF）
     * @returns {string|null} 标准化后的6位HEX颜色代码（不含#号），无效时返回null
     */
    normalizeHex(hex) {
      if (!hex) return null
      const v = hex.replace(/^#/, '')
      // 3位转6位：F -> FF
      if (v.length === 3) return v.split('').map(x => x + x).join('')
      return v.length === 6 ? v : null
    },
    
    /**
     * ========== 列表控制方法 ==========
     */
    
    /**
     * 切换列表展开/折叠状态
     */
    toggle() {
      this.expanded = !this.expanded
    },
    
    /**
     * 从API加载我的项目团队列表
     */
    async fetchMyTeams () {
      try {
        const res = await getMyRelatedTeamList()
        this.teams = Array.isArray(res) ? res : (res?.data || [])
      } catch (e) {
        // 加载失败时清空列表
        this.teams = []
      }
    },
    
    /**
     * ========== 导航相关方法 ==========
     */
    
    /**
     * 菜单项选择处理
     * @param {string} index - 菜单项索引
     */
    onSelect(index) {
      if (!index) return
      
      if (index === 'team-add') {
        // 点击新增按钮
        if (this.$route.name !== 'team-create') {
          this.$router.push({ name: 'team-create' }).catch(() => {})
        }
        return
      }
      
      if (index.startsWith('team-')) {
        // 点击团队项
        const id = index.replace('team-', '')
        // 如果已经在目标页面，则不重复跳转
        if (this.$route.name !== 'team-detail' || String(this.$route.params.teamId) !== String(id)) {
          this.$router.push({ name: 'team-detail', params: { teamId: id } }).catch(() => {})
        }
      }
    },
    
    /**
     * 加载当前用户信息
     */
    async loadMe() {
      try {
        const me = await getMe()
        this.currentUserId = me && me.userId ? me.userId : this.currentUserId
      } catch (e) {
        // 加载失败时保持原有值
      }
    },
    
    /**
     * ========== 颜色选择器相关方法 ==========
     */
    
    /**
     * 切换色板展开/折叠状态
     * @param {string} key - 色板的唯一标识
     */
    togglePalette (key) {
      const idx = this.colorPalettes.findIndex(p => p.key === key)
      if (idx !== -1) {
        this.$set(this.colorPalettes[idx], 'expanded', !this.colorPalettes[idx].expanded)
      }
    },
    
    /**
     * 判断指定颜色是否为当前团队的激活颜色
     * @param {string} color - 颜色值（HEX格式）
     * @returns {boolean} 是否为激活颜色
     */
    isActiveColor (color) {
      const normalized = this.normalizeHex(color)
      return normalized && normalized === this.normalizeHex(this.contextMenu.color)
    },
    /**
     * ========== 右键菜单相关方法 ==========
     */
    
    /**
     * 显示右键菜单
     * @param {Object} team - 团队对象
     * @param {Event} evt - 鼠标事件对象
     */
    onTeamContextMenu (team, evt) {
      // 阻止默认行为
      evt.preventDefault()
      // 关闭可能存在的拖拽状态，避免冲突
      this.onDragEnd()

      // 获取触发元素的边界矩形，用于计算菜单位置
      let rect = null
      if (evt.currentTarget && evt.currentTarget.getBoundingClientRect) {
        rect = evt.currentTarget.getBoundingClientRect()
      } else if (evt.target && evt.target.getBoundingClientRect) {
        rect = evt.target.getBoundingClientRect()
      }
      
      // 填充上下文菜单数据
      this.contextMenu.teamId = team.teamId
      this.contextMenu.teamName = team.teamName
      this.contextMenu.color = team.teamColor || '#409EFF'
      
      this.contextMenu.visible = true
      
      // 计算菜单位置，确保在视口内
      this.$nextTick(() => {
        const menu = this.$refs.contextMenu
        if (!menu) return
        
        const h = menu.offsetHeight
        const wh = window.innerHeight
        
        // 默认显示在触发元素右侧
        let x = rect ? rect.right + 4 : evt.clientX + 12
        let y = rect ? rect.top : evt.clientY
        
        // 如果菜单超出视口底部，则向上调整
        if (y + h > wh) y = Math.max(0, wh - h - 12)
        
        this.contextMenu.x = x
        this.contextMenu.y = y
      })
    },
    
    /**
     * 关闭右键菜单
     */
    closeContextMenu () {
      this.contextMenu.visible = false
    },
    
    /**
     * 全局点击处理：若点击不在菜单内则关闭菜单
     * @param {Event} e - 点击事件对象
     */
    handleGlobalClick (e) {
      if (this.contextMenu.visible) {
        const menuEl = this.$refs.contextMenu
        if (!(menuEl && menuEl.contains(e.target))) {
          this.closeContextMenu()
        }
      }
    },
    
    /**
     * 选择并更新团队颜色
     * @param {string} color - 颜色值（HEX格式）
     */
    async selectPresetColor (color) {
      const normalized = this.normalizeHex(color)
      if (!normalized) return
      // 防止重复请求
      if (this.isUpdatingColor) return
      
      const fullColor = '#' + normalized
      // 立即更新菜单中的颜色显示（乐观更新）
      this.contextMenu.color = fullColor
      
      this.isUpdatingColor = true
      try {
        // 调用API更新颜色
        await updateMyTeamColor({
          teamId: this.contextMenu.teamId,
          teamColor: fullColor
        })
        // 乐观更新本地列表
        const idx = this.teams.findIndex(t => t.teamId === this.contextMenu.teamId)
        if (idx !== -1) {
          const newTeam = { ...this.teams[idx], teamColor: fullColor }
          this.teams.splice(idx, 1, newTeam)
        }
        this.$message && this.$message.success('颜色已更新')
      } catch (e) {
        // 更新失败时，错误信息由API层处理
      } finally {
        this.isUpdatingColor = false
      }
    },

    /**
     * ========== 拖拽排序相关方法 ==========
     */
    
    /**
     * 开始拖拽团队
     * 存储初始状态并设置拖拽数据
     * @param {Object} team - 被拖拽的团队对象
     * @param {Event} evt - 拖拽事件对象
     */
    onDragStart (team, evt) {
      this.draggingTeamId = team.teamId
      this.isDragging = true
      this.dragInsertIndex = -1
      // 保存拖拽前的列表顺序，用于回滚
      this.dragStartOrder = [...this.teams]
      // 关闭右键菜单
      this.closeContextMenu()
      try {
        if (evt.dataTransfer) {
          evt.dataTransfer.setData('text/plain', String(team.teamId))
          evt.dataTransfer.dropEffect = 'move'
        }
      } catch (e) {}
    },
    
    /**
     * 拖拽进入团队项
     * 高亮潜在放置目标
     * @param {Object} team - 目标团队对象
     * @param {Event} evt - 拖拽事件对象
     */
    onDragEnter (team, evt) {
      if (!this.draggingTeamId || this.draggingTeamId === team.teamId) return
      this.dragOverTeamId = team.teamId
    },
    
    /**
     * 拖拽悬停在团队项上
     * 计算插入位置
     * @param {Object} team - 目标团队对象
     * @param {number} idx - 当前项索引
     * @param {Event} evt - 拖拽事件对象
     */
    onDragOver (team, idx, evt) {
      if (!this.draggingTeamId) return
      if (this.draggingTeamId === team.teamId) return
      evt && evt.preventDefault && evt.preventDefault()
      const el = evt.currentTarget || evt.target
      try {
        const rect = el.getBoundingClientRect ? el.getBoundingClientRect() : null
        let insertIndex = idx
        if (rect && typeof evt.clientY === 'number') {
          // 判断是放置在项目中心线的上方还是下方
          const mid = rect.top + rect.height / 2
          insertIndex = evt.clientY > mid ? (idx + 1) : idx
        }
        this.dragInsertIndex = insertIndex
      } catch (e) {
        this.dragInsertIndex = idx
      }
      this.dragOverTeamId = team.teamId
    },
    
    /**
     * 根据Y坐标计算插入索引
     * @param {number} clientY - 鼠标Y坐标
     * @returns {number} 插入位置索引，无效时返回-1
     */
    computeInsertIndexFromPoint (clientY) {
      const refs = this.$refs.teamItem || []
      const els = Array.isArray(refs) ? refs.map(r => (r && r.$el) ? r.$el : r) : []
      if (!els.length || typeof clientY !== 'number') return -1
      for (let i = 0; i < els.length; i++) {
        const rect = els[i].getBoundingClientRect()
        const mid = rect.top + rect.height / 2
        if (clientY < mid) return i
      }
      return els.length
    },
    
    /**
     * 处理在列表空白处的拖拽
     * @param {Event} evt - 拖拽事件对象
     */
    onListDragOver (evt) {
      if (this.draggingTeamId) {
        const idx = this.computeInsertIndexFromPoint(evt.clientY)
        if (idx !== -1) this.dragInsertIndex = idx
        return
      }
    },
    
    /**
     * 处理列表放置操作
     * @param {Event} evt - 拖拽事件对象
     */
    async onListDrop (evt) {
       await this.handleDrop(evt)
    },
    
    /**
     * 处理团队项放置操作
     * @param {Object} team - 目标团队对象
     * @param {Event} evt - 拖拽事件对象
     */
    async onDrop (team, evt) {
       await this.handleDrop(evt)
    },
    
    /**
     * 统一处理放置操作
     * @param {Event} evt - 拖拽事件对象
     */
    async handleDrop (evt) {
      if (this.draggingTeamId) {
        const idx = this.computeInsertIndexFromPoint(evt.clientY)
        if (idx === -1) { 
          this.onDragEnd()
          return 
        }
        await this.applyMoveToIndex(idx)
      }
    },
    
    /**
     * 应用团队移动操作
     * 更新本地状态并调用API持久化顺序
     * @param {number} insertIndex - 插入位置索引
     */
    async applyMoveToIndex (insertIndex) {
      const fromId = this.draggingTeamId
      this.dragOverTeamId = ''
      // 参数验证
      if (!fromId || typeof insertIndex !== 'number' || insertIndex < 0) {
        this.onDragEnd()
        return
      }
      const fromIndex = this.teams.findIndex(t => t.teamId === fromId)
      if (fromIndex === -1) { 
        this.onDragEnd()
        return 
      }
      // 计算目标索引（如果向后移动，需要减1）
      let toIndex = insertIndex
      if (fromIndex < insertIndex) toIndex = insertIndex - 1
      // 如果位置没有变化，直接返回
      if (fromIndex === toIndex) { 
        this.onDragEnd()
        return 
      }
      
      // 乐观更新：立即更新本地列表
      const before = [...this.teams]
      const [moved] = before.splice(fromIndex, 1)
      before.splice(toIndex, 0, moved)
      
      this.teams = before

      try {
        const oldList = [...this.teams]
        const oldIds = oldList.map(t => t.teamId)
        
        // 顺序发送交换请求以持久化顺序
        const idsToSwap = [...oldIds]
        const swaps = []
        
        // 向后移动：从原位置到目标位置，依次交换相邻元素
        if (fromIndex < toIndex) {
            for (let i = fromIndex; i < toIndex; i++) {
                swaps.push([idsToSwap[i], idsToSwap[i+1]])
                // 本地交换以追踪状态
                const tmp = idsToSwap[i]
                idsToSwap[i] = idsToSwap[i+1]
                idsToSwap[i+1] = tmp
            }
        } 
        // 向前移动：从原位置到目标位置，依次交换相邻元素
        else {
            for (let i = fromIndex; i > toIndex; i--) {
                swaps.push([idsToSwap[i], idsToSwap[i-1]])
                const tmp = idsToSwap[i-1]
                idsToSwap[i-1] = idsToSwap[i]
                idsToSwap[i] = tmp
            }
        }
        
        // 执行所有交换请求
        for (const [teamIdA, teamIdB] of swaps) {
            await swapMyTeamOrder({ teamIdA, teamIdB })
        }
        
        // 重新加载列表以确保数据同步
        this.fetchMyTeams()
      } catch (e) {
        // 出错时回滚到拖拽前的状态
        this.teams = this.dragStartOrder
      } finally {
        this.onDragEnd()
      }
    },
    
    /**
     * 重置拖拽状态
     * 在拖拽结束或取消时调用
     */
    onDragEnd () {
      this.isDragging = false
      this.dragInsertIndex = -1
      this.draggingTeamId = ''
      this.dragOverTeamId = ''
    }
  }
}
</script>

<style src="@/assets/css/index/sidebar-team-list.css" scoped></style>
