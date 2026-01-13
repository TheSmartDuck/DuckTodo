<template>
  <div class="sidebar-task-group-list-wrapper">
    <!-- 
      SECTION HEADER (分节标题)
      点击可切换任务族列表的显示/隐藏。
      样式：复古标题栏，带有铁锈红（Rust）指示条。
    -->
    <div class="sidebar-task-group-list-section-title" @click="toggle">
      <i :class="expanded ? 'el-icon-caret-bottom' : 'el-icon-caret-right'" class="sidebar-task-group-list-toggle-icon"></i>
      <span>任务族</span>
    </div>

    <!-- 
      TASK GROUP LIST (任务族列表)
      可拖拽的任务族列表。
      - 支持拖拽排序 (Drag & Drop)。
      - 支持右键菜单 (Context Menu)。
    -->
    <el-menu
      class="sidebar-task-group-list-side-menu"
      :default-active="active"
      @select="onSelect"
      @dragover.native.prevent="onListDragOver($event)"
      @drop.native.prevent="handleDrop($event)"
      v-show="expanded"
    >
      <el-menu-item
        v-for="(g, idx) in taskGroups"
        :key="g.taskGroupId"
        :index="'group-' + g.taskGroupId"
        @contextmenu.native.prevent="onContextMenu(g, $event)"
        :class="{
          'sidebar-task-group-list-dragging': draggingGroupId === g.taskGroupId,
          'sidebar-task-group-list-drag-over': dragOverGroupId === g.taskGroupId,
          'sidebar-task-group-list-shift-down': isDraggingGroup && dragInsertIndexGroup !== -1 && idx >= dragInsertIndexGroup && draggingGroupId !== g.taskGroupId
        }"
        :draggable="true"
        ref="groupItem"
        @dragstart.native="onGroupDragStart(g, $event)"
        @dragenter.native="onGroupDragEnter(g, $event)"
        @dragover.native.prevent="onGroupDragOver(g, idx, $event)"
        @drop.native.stop.prevent="handleDrop($event)"
        @dragend.native="onGroupDragEnd($event)"
      >
        <!-- Color Dot Indicator (颜色指示点) -->
        <span class="sidebar-task-group-list-dot" :style="{ background: (g.groupColor || '#B2653B') }"></span>
        <!-- Task Group Name (任务族名称，过长截断) -->
        <span class="sidebar-task-group-list-text" :title="g.groupAlias || g.taskGroupName || g.groupName">
          {{ g.groupAlias || g.taskGroupName || g.groupName || '未命名任务族' }}
        </span>
      </el-menu-item>
      
      <!-- 'Add New' Button (新增按钮) -->
      <el-menu-item index="family-add">
        <i class="el-icon el-icon-plus sidebar-task-group-list-nav-icon sidebar-task-group-list-plus"></i>
        <span class="sidebar-task-group-list-text">&nbsp;新增</span>
      </el-menu-item>
    </el-menu>

    <!-- 
      CONTEXT MENU (右键菜单)
      允许用户：
      1. 查看分组信息（ID、名称、隐私状态）
      2. 修改标签颜色（从预设色板中选择）
      3. 重命名别名
    -->
    <div v-if="groupContextMenu.visible" class="sidebar-task-group-list-context-menu-mask" @click="closeGroupContextMenu"></div>
    <div
      v-if="groupContextMenu.visible"
      class="sidebar-task-group-list-context-menu"
      :style="{ top: groupContextMenu.y + 'px', left: groupContextMenu.x + 'px' }"
      ref="groupContextMenu"
      @click.stop
      @contextmenu.stop.prevent
    >
      <!-- Header: ID & Privacy Tag (头部：ID与隐私标签) -->
      <div class="sidebar-task-group-list-context-menu-header">
        <div class="sidebar-task-group-list-privacy-tag" :class="groupContextMenu.isPrivate ? 'sidebar-task-group-list-privacy-tag--private' : 'sidebar-task-group-list-privacy-tag--public'">
          {{ groupContextMenu.isPrivate ? 'PRIVATE' : 'PUBLIC' }}
        </div>
        <span class="sidebar-task-group-list-header-id">{{ groupContextMenu.taskGroupId }}</span>
      </div>
      
      <!-- Body: Title & Color Picker (主体：标题与颜色选择器) -->
      <div class="sidebar-task-group-list-context-menu-body">
        <div class="sidebar-task-group-list-menu-title-row">
          <div class="sidebar-task-group-list-menu-main-title" :title="groupContextMenu.groupAlias || groupContextMenu.groupName">
            {{ groupContextMenu.groupAlias || groupContextMenu.groupName || '未命名任务族' }}
          </div>
          <div class="sidebar-task-group-list-menu-sub-info">
            原始名称：{{ groupContextMenu.groupName || '私人任务族' }}
          </div>
        </div>

        <!-- Color Selection Area (颜色选择区域) -->
        <div class="sidebar-task-group-list-color-section">
          <div class="sidebar-task-group-list-section-label">标签颜色 / LABEL COLOR</div>
          <div class="sidebar-task-group-list-current-color-row">
             <span class="sidebar-task-group-list-preview-dot" :style="{ background: (groupContextMenu.color || '#B2653B') }"></span>
             <span class="sidebar-task-group-list-current-color-text">{{ groupContextMenu.color || '#B2653B' }}</span>
          </div>
          
          <div class="sidebar-task-group-list-palette-container">
            <!-- Color Palettes (Common, Cassette, Brutalism, History) (色板系列：通用、磁带、野兽派、历史) -->
            <div class="sidebar-task-group-list-palette-group" v-for="p in sortedColorPalettes" :key="p.key">
              <div class="sidebar-task-group-list-palette-group-header" @click="togglePalette(p.key)">
                <i :class="p.expanded ? 'el-icon-caret-bottom' : 'el-icon-caret-right'" class="sidebar-task-group-list-toggle-icon"></i>
                <span class="sidebar-task-group-list-palette-group-title">{{ p.name }}</span>
              </div>
              <div class="sidebar-task-group-list-palette" v-show="p.expanded">
                <button
                  v-for="c in p.colorsSorted"
                  :key="c"
                  class="sidebar-task-group-list-color-swatch"
                  :style="{ background: c, '--swatch-color': c }"
                  :class="{ 'sidebar-task-group-list-color-swatch--active': isActiveGroupColor(c) }"
                  @click="selectGroupPresetColor(c)"
                  :title="c"
                />
              </div>
            </div>
          </div>
        </div>
        
        <!-- Alias Input Area (别名输入区域) -->
        <div class="sidebar-task-group-list-alias-section">
          <div class="sidebar-task-group-list-section-label">任务族别名 / ALIAS</div>
          <el-input
            size="mini"
            v-model="groupContextMenu.alias"
            maxlength="64"
            placeholder="自定义别名..."
            class="sidebar-task-group-list-retro-input"
          />
        </div>
      </div>

      <!-- Footer: Actions (底部：操作按钮) -->
      <div class="sidebar-task-group-list-menu-footer">
        <div class="sidebar-task-group-list-footer-btn sidebar-task-group-list-footer-btn--cancel" @click="closeGroupContextMenu">CANCEL</div>
        <div class="sidebar-task-group-list-footer-btn sidebar-task-group-list-footer-btn--save" @click="submitGroupAliasChange">SAVE ALIAS</div>
      </div>
    </div>
  </div>
</template>

<script>
import { getMyTaskGroupList, swapMyTaskGroupOrder, updateMyTaskGroupColor, updateMyGroupAlias } from '@/api/taskgroup-api'
import colorPalettesData from '@/assets/common/color-palettes.json'

/**
 * 侧边栏任务族列表组件
 * 功能：
 * 1. 显示和管理任务族列表
 * 2. 支持拖拽排序
 * 3. 支持右键菜单进行颜色和别名设置
 * 4. 支持展开/折叠列表
 */
export default {
  name: 'SidebarTaskGroupList',
  data() {
    return {
      // 任务族列表数据
      taskGroups: [],
      // 列表展开/折叠状态
      expanded: true,
      // 当前选中的菜单项索引
      active: '',
      
      // ========== 拖拽排序相关状态 ==========
      // 当前正在拖拽的任务族ID
      draggingGroupId: '',
      // 当前鼠标悬停的目标任务族ID
      dragOverGroupId: '',
      // 全局拖拽激活标志
      isDraggingGroup: false,
      // 计算出的插入位置索引
      dragInsertIndexGroup: -1,
      // 拖拽开始前的列表顺序备份（用于回滚）
      dragStartOrderGroup: [],

      // ========== 右键菜单相关状态 ==========
      // 是否正在更新任务族颜色（防止重复请求）
      isUpdatingGroupColor: false,
      // 右键菜单数据
      groupContextMenu: {
        visible: false,        // 菜单是否可见
        x: 0,                 // 菜单X坐标
        y: 0,                 // 菜单Y坐标
        taskGroupId: '',      // 任务族ID
        groupName: '',         // 任务族原始名称
        groupAlias: '',        // 任务族别名
        alias: '',             // 编辑中的别名（临时值）
        isPrivate: false,      // 是否为私有任务族
        color: '#B2653B'       // 任务族颜色
      },
      
      // ========== 颜色配置 ==========
      // 预设颜色列表
      presetColors: colorPalettesData.presetColors,
      // 颜色色板集合（包含多个色板系列）
      colorPalettes: colorPalettesData.colorPalettes
    }
  },
  /**
   * 组件创建时初始化
   */
  created() {
    this.loadMyTaskGroups()
    // 监听全局刷新事件
    if (this.$root && this.$root.$on) {
      this.$root.$on('refresh-my-taskgroups', this.loadMyTaskGroups)
    }
  },
  /**
   * 组件销毁前清理
   */
  beforeDestroy() {
    // 移除全局事件监听
    if (this.$root && this.$root.$off) {
      this.$root.$off('refresh-my-taskgroups', this.loadMyTaskGroups)
    }
  },
  watch: {
    /**
     * 监听路由变化，同步菜单选中状态
     * 根据当前路由自动高亮对应的任务族菜单项
     */
    $route: {
      handler(to) {
        if (!to) return
        // 任务族详情页：高亮对应任务族
        if (to.name === 'taskgroup-detail' && to.params && to.params.taskGroupId) {
          this.active = 'group-' + String(to.params.taskGroupId)
        } 
        // 创建私有任务族页：高亮新增按钮
        else if (to.name === 'taskgroup-create-private') {
          this.active = 'family-add'
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
     * 从API加载我的任务族列表
     */
    async loadMyTaskGroups() {
      try {
        const list = await getMyTaskGroupList()
        this.taskGroups = Array.isArray(list) ? list : []
      } catch (e) {
        // 加载失败时清空列表
        this.taskGroups = []
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
      if (index === 'family-add') {
        // 点击新增按钮
        this.goTaskGroupCreatePrivate()
      } else if (index && index.startsWith('group-')) {
        // 点击任务族项
        const gid = index.slice(6)
        this.goTaskGroupDetail(gid)
      }
    },
    
    /**
     * 跳转到创建私有任务族页面
     */
    goTaskGroupCreatePrivate () {
      if (this.$route.name !== 'taskgroup-create-private') {
        this.$router.push({ name: 'taskgroup-create-private' }).catch(() => {})
      }
    },
    
    /**
     * 跳转到任务族详情页面
     * @param {string|number} taskGroupId - 任务族ID
     */
    goTaskGroupDetail (taskGroupId) {
      const to = { name: 'taskgroup-detail', params: { taskGroupId: String(taskGroupId) } }
      // 如果已经在目标页面，则不重复跳转
      if (this.$route.name === 'taskgroup-detail' && String(this.$route.params.taskGroupId) === String(taskGroupId)) {
        return
      }
      this.$router.push(to).catch(() => {})
    },
    
    /**
     * ========== 颜色选择器相关方法 ==========
     */
    
    /**
     * 切换色板展开/折叠状态
     * @param {string} key - 色板的唯一标识
     */
    togglePalette(key) {
      const p = this.colorPalettes.find(x => x.key === key)
      if (p) p.expanded = !p.expanded
    },
    
    /**
     * ========== 右键菜单相关方法 ==========
     */
    
    /**
     * 显示右键菜单
     * @param {Object} group - 任务族对象
     * @param {Event} evt - 鼠标事件对象
     */
    onContextMenu(group, evt) {
      evt.preventDefault()
      // 关闭可能存在的拖拽状态，避免冲突
      this.onGroupDragEnd()

      // 获取触发元素的边界矩形，用于计算菜单位置
      let rect = null
      if (evt.currentTarget && evt.currentTarget.getBoundingClientRect) {
        rect = evt.currentTarget.getBoundingClientRect()
      } else if (evt.target && evt.target.getBoundingClientRect) {
        rect = evt.target.getBoundingClientRect()
      }

      // 填充上下文菜单数据
      this.groupContextMenu.taskGroupId = group.taskGroupId
      this.groupContextMenu.groupName = group.taskGroupName || group.groupName
      this.groupContextMenu.groupAlias = group.groupAlias || ''
      this.groupContextMenu.alias = group.groupAlias || ''
      this.groupContextMenu.color = group.groupColor || '#B2653B'
      // 判断是否为私有任务族（没有teamId或teamId为空）
      this.groupContextMenu.isPrivate = !(group.teamId && String(group.teamId).trim())

      this.groupContextMenu.visible = true
      
      // 计算菜单位置，确保在视口内
      this.$nextTick(() => {
        const menu = this.$refs.groupContextMenu
        if (!menu) return
        
        const h = menu.offsetHeight
        const wh = window.innerHeight
        
        // 默认显示在触发元素右侧
        let x = rect ? rect.right + 4 : evt.clientX + 12
        let y = rect ? rect.top : evt.clientY
        
        // 如果菜单超出视口底部，则向上调整
        if (y + h > wh) y = Math.max(0, wh - h - 12)
        
        this.groupContextMenu.x = x
        this.groupContextMenu.y = y
      })
    },
    
    /**
     * 关闭右键菜单
     */
    closeGroupContextMenu () {
      this.groupContextMenu.visible = false
    },
    
    /**
     * 判断指定颜色是否为当前任务族的激活颜色
     * @param {string} color - 颜色值（HEX格式）
     * @returns {boolean} 是否为激活颜色
     */
    isActiveGroupColor (color) {
      const normalized = this.normalizeHex(color)
      return normalized && normalized === this.normalizeHex(this.groupContextMenu.color)
    },
    
    /**
     * 选择并更新任务族颜色
     * @param {string} color - 颜色值（HEX格式）
     */
    async selectGroupPresetColor (color) {
      const normalized = this.normalizeHex(color)
      if (!normalized) return
      // 防止重复请求
      if (this.isUpdatingGroupColor) return
      
      const fullColor = '#' + normalized
      // 立即更新菜单中的颜色显示（乐观更新）
      this.groupContextMenu.color = fullColor
      
      this.isUpdatingGroupColor = true
      try {
        // 调用API更新颜色
        await updateMyTaskGroupColor({
          taskGroupId: this.groupContextMenu.taskGroupId,
          groupColor: fullColor
        })
        // 乐观更新本地列表
        const idx = this.taskGroups.findIndex(g => g.taskGroupId === this.groupContextMenu.taskGroupId)
        if (idx !== -1) {
          this.taskGroups[idx] = { ...this.taskGroups[idx], groupColor: fullColor }
        }
        this.$message && this.$message.success('颜色已更新')
        // 重新加载列表以确保数据同步
        this.loadMyTaskGroups()
      } catch (e) {
        // 更新失败时，错误信息由API层处理
      } finally {
        this.isUpdatingGroupColor = false
      }
    },
    
    /**
     * 提交任务族别名更改
     */
    async submitGroupAliasChange () {
      const alias = (this.groupContextMenu.alias || '').trim()
      // 验证别名长度
      if (alias.length > 64) {
        this.$message && this.$message.error('别名长度不能超过64字符')
        return
      }
      try {
        // 调用API更新别名
        await updateMyGroupAlias({
          taskGroupId: this.groupContextMenu.taskGroupId,
          groupAlias: alias
        })
        // 乐观更新本地列表
        const idx = this.taskGroups.findIndex(g => g.taskGroupId === this.groupContextMenu.taskGroupId)
        if (idx !== -1) {
          this.taskGroups[idx] = { ...this.taskGroups[idx], groupAlias: alias }
        }
        this.$message && this.$message.success('别名已更新')
        this.closeGroupContextMenu()
        // 重新加载列表以确保数据同步
        this.loadMyTaskGroups()
      } catch (e) {
        // 更新失败时，错误信息由API层处理
      }
    },

    /**
     * ========== 拖拽排序相关方法 ==========
     */
    
    /**
     * 开始拖拽任务族
     * 存储初始状态并设置拖拽数据
     * @param {Object} group - 被拖拽的任务族对象
     * @param {Event} evt - 拖拽事件对象
     */
    onGroupDragStart (group, evt) {
      this.draggingGroupId = group.taskGroupId
      this.isDraggingGroup = true
      this.dragInsertIndexGroup = -1
      // 保存拖拽前的列表顺序，用于回滚
      this.dragStartOrderGroup = [...this.taskGroups]
      // 关闭右键菜单
      this.closeGroupContextMenu()
      try {
        if (evt.dataTransfer) {
          evt.dataTransfer.setData('text/plain', String(group.taskGroupId))
          evt.dataTransfer.dropEffect = 'move'
        }
      } catch (e) {}
    },
    
    /**
     * 拖拽进入任务族项
     * 高亮潜在放置目标
     * @param {Object} group - 目标任务族对象
     * @param {Event} evt - 拖拽事件对象
     */
    onGroupDragEnter (group, evt) {
      if (!this.draggingGroupId || this.draggingGroupId === group.taskGroupId) return
      this.dragOverGroupId = group.taskGroupId
    },
    
    /**
     * 拖拽悬停在任务族项上
     * 计算插入位置
     * @param {Object} group - 目标任务族对象
     * @param {number} idx - 当前项索引
     * @param {Event} evt - 拖拽事件对象
     */
    onGroupDragOver (group, idx, evt) {
      if (!this.draggingGroupId) return
      if (this.draggingGroupId === group.taskGroupId) return
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
        this.dragInsertIndexGroup = insertIndex
      } catch (e) {
        this.dragInsertIndexGroup = idx
      }
      this.dragOverGroupId = group.taskGroupId
    },
    
    /**
     * 根据Y坐标计算插入索引
     * @param {number} clientY - 鼠标Y坐标
     * @returns {number} 插入位置索引，无效时返回-1
     */
    computeGroupInsertIndexFromPoint (clientY) {
      const refs = this.$refs.groupItem || []
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
      if (this.draggingGroupId) {
        const idx = this.computeGroupInsertIndexFromPoint(evt.clientY)
        if (idx !== -1) this.dragInsertIndexGroup = idx
      }
    },
    
    /**
     * 处理放置操作
     * @param {Event} evt - 拖拽事件对象
     */
    async handleDrop (evt) {
      if (this.draggingGroupId) {
        const idx = this.computeGroupInsertIndexFromPoint(evt.clientY)
        if (idx === -1) { 
          this.onGroupDragEnd()
          return 
        }
        await this.applyGroupMoveToIndex(idx)
      }
    },
    
    /**
     * 应用任务族移动操作
     * 更新本地状态并调用API持久化顺序
     * @param {number} insertIndex - 插入位置索引
     */
    async applyGroupMoveToIndex (insertIndex) {
      const fromId = this.draggingGroupId
      this.dragOverGroupId = ''
      // 参数验证
      if (!fromId || typeof insertIndex !== 'number' || insertIndex < 0) {
        this.onGroupDragEnd()
        return
      }
      const fromIndex = this.taskGroups.findIndex(g => g.taskGroupId === fromId)
      if (fromIndex === -1) { 
        this.onGroupDragEnd()
        return 
      }
      // 计算目标索引（如果向后移动，需要减1）
      let toIndex = insertIndex
      if (fromIndex < insertIndex) toIndex = insertIndex - 1
      // 如果位置没有变化，直接返回
      if (fromIndex === toIndex) { 
        this.onGroupDragEnd()
        return 
      }
      
      // 乐观更新：立即更新本地列表
      const before = [...this.taskGroups]
      const beforeIds = before.map(g => g.taskGroupId)
      const [moved] = before.splice(fromIndex, 1)
      before.splice(toIndex, 0, moved)
      
      // 直接赋值以立即更新前端显示
      this.taskGroups = before

      try {
        // 顺序发送交换请求以持久化顺序
        const idsToSwap = [...beforeIds]
        const swaps = []
        
        // 向后移动：从原位置到目标位置，依次交换相邻元素
        if (fromIndex < toIndex) {
          for (let i = fromIndex; i < toIndex; i++) {
            swaps.push([idsToSwap[i], idsToSwap[i + 1]])
            // 本地交换以追踪状态
            const tmp = idsToSwap[i]
            idsToSwap[i] = idsToSwap[i + 1]
            idsToSwap[i + 1] = tmp
          }
        } 
        // 向前移动：从原位置到目标位置，依次交换相邻元素
        else {
          for (let i = fromIndex; i > toIndex; i--) {
            swaps.push([idsToSwap[i - 1], idsToSwap[i]])
            const tmp = idsToSwap[i - 1]
            idsToSwap[i - 1] = idsToSwap[i]
            idsToSwap[i] = tmp
          }
        }
        
        // 执行所有交换请求
        for (const [taskGroupId1, taskGroupId2] of swaps) {
          await swapMyTaskGroupOrder({ taskGroupId1, taskGroupId2 })
        }
        
        // 重新加载列表以确保数据同步
        this.loadMyTaskGroups()
      } catch (e) {
        // 出错时回滚到拖拽前的状态
        this.taskGroups = this.dragStartOrderGroup
        // 显示错误提示
        if (this.$message) {
          this.$message.error(e.message || '交换任务族顺序失败')
        }
      } finally {
        this.onGroupDragEnd()
      }
    },
    
    /**
     * 重置拖拽状态
     * 在拖拽结束或取消时调用
     */
    onGroupDragEnd () {
      this.isDraggingGroup = false
      this.dragInsertIndexGroup = -1
      this.draggingGroupId = ''
      this.dragOverGroupId = ''
    }
  }
}
</script>

<style src="@/assets/css/index/sidebar-task-group-list.css" scoped></style>
