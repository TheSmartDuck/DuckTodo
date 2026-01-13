<template>
  <div class="index-dashboard">
    <!-- 左侧侧边栏区 -->
    <aside class="index-sidebar">
      <!-- 模块：品牌搜索区（品牌、搜索、菜单） -->
      <sidebar-brand />
      
      <!-- 模块：常规菜单（主页/最近/日程/数据/工具/个人中心） -->
      <sidebar-common-menu />

      <!-- 滚动区域：任务族列表与项目团队列表 -->
      <div class="index-sidebar-scroll-area">
        <!-- 子模块：任务族列表（拖拽排序、右键菜单） -->
        <sidebar-task-group-list />

        <!-- 子模块：项目团队列表（拖拽排序、右键菜单） -->
        <sidebar-team-list />
      </div>

      <!-- 子模块：底部页脚（CRT开关、版本号） -->
      <sidebar-footer />
    </aside>

    <!-- 模块：右侧内容区（路由页面渲染） -->
    <!-- 右侧内容区，通过子路由在此渲染具体页面 -->
    <main class="index-content">
      <router-view />
    </main>
  </div>
  
</template>

<script>
import SidebarBrand from '@/components/index/SidebarBrand.vue'
import SidebarCommonMenu from '@/components/index/SidebarCommonMenu.vue'
import SidebarTaskGroupList from '@/components/index/SidebarTaskGroupList.vue'
import SidebarTeamList from '@/components/index/SidebarTeamList.vue'
import SidebarFooter from '@/components/index/SidebarFooter.vue'

/**
 * IndexView 首页布局组件
 * 
 * 应用程序的主布局容器，包含左侧侧边栏和右侧内容区域。
 * 负责组织侧边栏的各个子模块（品牌、菜单、任务列表、团队列表、页脚），
 * 并通过 router-view 渲染右侧的具体业务页面。
 */
export default {
  name: 'IndexView',
  components: {
    /** 侧边栏品牌与搜索模块 */
    SidebarBrand,
    /** 侧边栏通用菜单模块 */
    SidebarCommonMenu,
    /** 侧边栏任务族列表模块 */
    SidebarTaskGroupList,
    /** 侧边栏团队列表模块 */
    SidebarTeamList,
    /** 侧边栏底部页脚模块 */
    SidebarFooter
  },
}
</script>

<style src="@/assets/css/index/index.css" scoped></style>
