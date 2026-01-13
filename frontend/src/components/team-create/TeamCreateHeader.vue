<template>
  <!-- 创建团队页面头部根容器 -->
  <div class="team-create-header-retro">
    <!-- 左侧装饰标记（伪元素） -->
    
    <!-- 标题徽章 -->
    <div class="team-create-header-badge">
      <!-- 标题过渡动画 -->
      <transition name="team-create-header-fade" mode="out-in">
        <span :key="title">{{ title }}</span>
      </transition>
    </div>
    
    <!-- 连接线 -->
    <div class="team-create-header-line"></div>
    
    <!-- 副标题 -->
    <div class="team-create-header-sub">
      <!-- 副标题过渡动画 -->
      <transition name="team-create-header-fade" mode="out-in">
        <span :key="subtitle">{{ subtitle }}</span>
      </transition>
    </div>
    
    <!-- 操作按钮区域 -->
    <div class="team-create-header-actions" v-show="showBack">
      <el-button 
        class="team-create-header-retro-btn" 
        size="mini" 
        icon="el-icon-back" 
        @click="$emit('back')"
      >
        BACK / 返回
      </el-button>
    </div>
  </div>
</template>

<script>
/**
 * 创建团队页面头部组件
 * 
 * 功能：
 * 1. 显示页面标题和副标题
 * 2. 提供返回按钮（可选）
 * 3. 标题和副标题支持过渡动画
 * 4. 磁带未来主义风格的装饰性设计
 * 
 * Props：
 * - title: 页面标题（字符串）
 * - subtitle: 页面副标题（字符串）
 * - showBack: 是否显示返回按钮（布尔值）
 * 
 * Events：
 * - back: 点击返回按钮时触发
 * 
 * 组件结构：
 * - 标题徽章：黑色背景，白色文字
 * - 连接线：连接标题和副标题的装饰线
 * - 副标题：带边框的副标题文字
 * - 操作按钮：返回按钮（可选显示）
 */
export default {
  name: 'TeamCreateHeader',
  props: {
    // 页面标题
    title: {
      type: String,
      default: ''
    },
    // 页面副标题
    subtitle: {
      type: String,
      default: ''
    },
    // 是否显示返回按钮
    showBack: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style>
@import '@/assets/css/team-create/team-create-header.css';
</style>
