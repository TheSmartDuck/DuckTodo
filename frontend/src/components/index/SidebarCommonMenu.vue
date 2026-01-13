<template>
  <div class="sidebar-common-menu-container">
    <div class="sidebar-common-menu-header-label">DIRECTORY</div>
    <el-menu
      class="sidebar-common-menu-list"
      :default-active="active"
      @select="onSelect"
    >
      <el-menu-item index="home">
        <div class="sidebar-common-menu-item-card">
          <div class="sidebar-common-menu-text-group">
            <span class="sidebar-common-menu-en-title">HOME</span>
            <span class="sidebar-common-menu-cn-subtitle">主页</span>
          </div>
          <div class="sidebar-common-menu-icon-area">
            <i class="el-icon el-icon-s-home"></i>
          </div>
          <div class="sidebar-common-menu-bottom-strip"></div>
        </div>
      </el-menu-item>
      <el-menu-item index="recent">
        <div class="sidebar-common-menu-item-card">
          <div class="sidebar-common-menu-text-group">
            <span class="sidebar-common-menu-en-title">RECENT</span>
            <span class="sidebar-common-menu-cn-subtitle">最近待办</span>
          </div>
          <div class="sidebar-common-menu-icon-area">
            <i class="el-icon el-icon-notebook-2"></i>
          </div>
          <div class="sidebar-common-menu-bottom-strip"></div>
        </div>
      </el-menu-item>
      <el-menu-item index="schedule">
        <div class="sidebar-common-menu-item-card">
          <div class="sidebar-common-menu-text-group">
            <span class="sidebar-common-menu-en-title">SCHEDULE</span>
            <span class="sidebar-common-menu-cn-subtitle">日程概览</span>
          </div>
          <div class="sidebar-common-menu-icon-area">
            <i class="el-icon el-icon-date"></i>
          </div>
          <div class="sidebar-common-menu-bottom-strip"></div>
        </div>
      </el-menu-item>
      <el-menu-item index="datapanel">
        <div class="sidebar-common-menu-item-card">
          <div class="sidebar-common-menu-text-group">
            <span class="sidebar-common-menu-en-title">DATA</span>
            <span class="sidebar-common-menu-cn-subtitle">数据面板</span>
          </div>
          <div class="sidebar-common-menu-icon-area">
            <i class="el-icon el-icon-s-data"></i>
          </div>
          <div class="sidebar-common-menu-bottom-strip"></div>
        </div>
      </el-menu-item>
      <el-menu-item index="tools">
        <div class="sidebar-common-menu-item-card">
          <div class="sidebar-common-menu-text-group">
            <span class="sidebar-common-menu-en-title">TOOLS</span>
            <span class="sidebar-common-menu-cn-subtitle">工具工厂</span>
          </div>
          <div class="sidebar-common-menu-icon-area">
            <i class="el-icon el-icon-s-tools"></i>
          </div>
          <div class="sidebar-common-menu-bottom-strip"></div>
        </div>
      </el-menu-item>
      <el-menu-item index="profile">
        <div class="sidebar-common-menu-item-card">
          <div class="sidebar-common-menu-text-group">
            <span class="sidebar-common-menu-en-title">PROFILE</span>
            <span class="sidebar-common-menu-cn-subtitle">个人中心</span>
          </div>
          <div class="sidebar-common-menu-icon-area">
            <i class="el-icon el-icon-user"></i>
          </div>
          <div class="sidebar-common-menu-bottom-strip"></div>
        </div>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
/**
 * SidebarCommonMenu 组件
 * 
 * 侧边栏通用菜单组件，包含主页、最近待办、日程、数据面板、工具工厂、个人中心等固定导航项。
 * 负责处理路由跳转和当前激活项的高亮显示。
 */
export default {
  name: 'SidebarCommonMenu',
  
  data() {
    return {
      /**
       * 当前激活的菜单项索引 (index)
       * 用于控制 el-menu 的 default-active 属性，实现高亮显示
       * @type {string}
       */
      active: 'home'
    }
  },
  
  watch: {
    /**
     * 监听路由 ($route) 变化
     * 
     * 当路由发生变化时，根据当前路由的 name 更新 active 属性，
     * 从而保持侧边栏菜单的高亮状态与当前页面同步。
     * 
     * @property {object} $route - Vue Router 的路由对象
     */
    $route: {
      /**
       * 路由变化的处理函数
       * @param {Object} to - 目标路由对象
       */
      handler(to) {
        if (!to) return
        const name = to.name
        // 定义侧边栏包含的路由名称列表
        const sidebarRoutes = ['home', 'recent', 'schedule', 'datapanel', 'tools', 'userinformation']
        
        if (sidebarRoutes.includes(name)) {
           // 特殊处理：userinformation 路由对应 profile 菜单项
           if (name === 'userinformation') {
             this.active = 'profile'
           } else {
             this.active = name
           }
        } else {
          // 如果不在列表中，取消选中状态
          this.active = ''
        }
      },
      immediate: true // 组件创建时立即执行一次，确保初始状态正确
    }
  },
  
  methods: {
    /**
     * 处理菜单项选中事件 (select)
     * 
     * 当用户点击 el-menu-item 时触发，根据 index 跳转到对应的路由。
     * 
     * @param {string} index - 选中的菜单项索引 (el-menu-item 的 index 属性)
     */
    onSelect(index) {
      switch (index) {
        case 'profile':
          this.goProfile()
          break
        case 'home':
          this.goHome()
          break
        case 'recent':
          this.goRecent()
          break
        case 'schedule':
          this.goSchedule()
          break
        case 'datapanel':
          this.goDataPanel()
          break
        case 'tools':
          this.goTools()
          break
      }
    },
    
    /**
     * 路由跳转辅助方法：个人中心
     * 
     * 跳转到 'userinformation' 路由。
     * 使用 catch 捕获 NavigationDuplicated 错误（当尝试跳转到当前路由时触发）。
     */
    goProfile () {
      if (this.$route.name !== 'userinformation') {
        this.$router.push({ name: 'userinformation' }).catch(() => {})
      }
    },
    
    /**
     * 路由跳转辅助方法：主页
     * 
     * 跳转到 'home' 路由。
     */
    goHome () {
      if (this.$route.name !== 'home') {
        this.$router.push({ name: 'home' }).catch(() => {})
      }
    },
    
    /**
     * 路由跳转辅助方法：最近待办
     * 
     * 跳转到 'recent' 路由。
     */
    goRecent () {
      if (this.$route.name !== 'recent') {
        this.$router.push({ name: 'recent' }).catch(() => {})
      }
    },
    
    /**
     * 路由跳转辅助方法：日程概览
     * 
     * 跳转到 'schedule' 路由。
     */
    goSchedule () {
      if (this.$route.name !== 'schedule') {
        this.$router.push({ name: 'schedule' }).catch(() => {})
      }
    },
    
    /**
     * 路由跳转辅助方法：数据面板
     * 
     * 跳转到 'datapanel' 路由。
     */
    goDataPanel () {
      if (this.$route.name !== 'datapanel') {
        this.$router.push({ name: 'datapanel' }).catch(() => {})
      }
    },
    
    /**
     * 路由跳转辅助方法：工具工厂
     * 
     * 跳转到 'tools' 路由。
     */
    goTools () {
      if (this.$route.name !== 'tools') {
        this.$router.push({ name: 'tools' }).catch(() => {})
      }
    }
  }
}
</script>

<style src="@/assets/css/index/sidebar-common-menu.css" scoped></style>
