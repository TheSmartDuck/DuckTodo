<template>
  <!-- 创建团队模式选择器根容器 -->
  <div class="team-create-mode-selector-choice-grid">
    <!-- 卡片 01：创建模式 -->
    <div class="team-create-mode-selector-mission-card team-create-mode-selector-create-card" @click="$emit('select', 'create')">
      <!-- 卡片左侧：文字内容 -->
      <div class="team-create-mode-selector-card-left">
        <!-- 英文标题 -->
        <div class="team-create-mode-selector-title-en">CREATE</div>
        <!-- 标题倒影效果 -->
        <div class="team-create-mode-selector-title-reflect">CREATE</div>
        <!-- 中文副标题 -->
        <div class="team-create-mode-selector-subtitle-cn">
          新建项目 <span class="team-create-mode-selector-deco-dots">::</span>
        </div>
      </div>
      <!-- 卡片右侧：雷达图形 -->
      <div class="team-create-mode-selector-card-right">
        <div class="team-create-mode-selector-radar-graphic">
          <!-- 雷达圆环 -->
          <div class="team-create-mode-selector-ring team-create-mode-selector-r1"></div>
          <div class="team-create-mode-selector-ring team-create-mode-selector-r2"></div>
          <div class="team-create-mode-selector-ring team-create-mode-selector-r3"></div>
          <!-- 雷达扇形区域 -->
          <div class="team-create-mode-selector-sector team-create-mode-selector-s1"></div>
          <div class="team-create-mode-selector-sector team-create-mode-selector-s2"></div>
          <!-- 图标覆盖层 -->
          <div class="team-create-mode-selector-icon-overlay">
            <i class="el-icon-plus"></i>
          </div>
        </div>
      </div>
      <!-- 底部装饰条 -->
      <div class="team-create-mode-selector-bottom-strip"></div>
    </div>

    <!-- 卡片 02：加入模式 -->
    <div class="team-create-mode-selector-mission-card team-create-mode-selector-join-card" @click="$emit('select', 'join')">
      <!-- 卡片左侧：文字内容 -->
      <div class="team-create-mode-selector-card-left">
        <!-- 英文标题 -->
        <div class="team-create-mode-selector-title-en">JOIN</div>
        <!-- 标题倒影效果 -->
        <div class="team-create-mode-selector-title-reflect">JOIN</div>
        <!-- 中文副标题 -->
        <div class="team-create-mode-selector-subtitle-cn">
          加入项目 <span class="team-create-mode-selector-deco-dots">::</span>
        </div>
      </div>
      <!-- 卡片右侧：雷达图形 -->
      <div class="team-create-mode-selector-card-right">
        <div class="team-create-mode-selector-radar-graphic">
          <!-- 雷达圆环 -->
          <div class="team-create-mode-selector-ring team-create-mode-selector-r1"></div>
          <div class="team-create-mode-selector-ring team-create-mode-selector-r2"></div>
          <div class="team-create-mode-selector-ring team-create-mode-selector-r3"></div>
          <!-- 雷达扇形区域 -->
          <div class="team-create-mode-selector-sector team-create-mode-selector-s3"></div>
          <div class="team-create-mode-selector-sector team-create-mode-selector-s4"></div>
          <!-- 图标覆盖层 -->
          <div class="team-create-mode-selector-icon-overlay">
            <i class="el-icon-connection"></i>
          </div>
        </div>
      </div>
      <!-- 底部装饰条 -->
      <div class="team-create-mode-selector-bottom-strip"></div>
    </div>
  </div>
</template>

<script>
/**
 * 创建团队模式选择器组件
 * 
 * 功能：
 * 1. 提供两种模式选择：创建新团队或加入现有团队
 * 2. 显示两个交互式卡片，每个卡片包含标题、副标题和雷达图形
 * 3. 悬停时触发动画效果（标题移动、雷达旋转、底部装饰条动画）
 * 
 * Events：
 * - select: 当用户点击卡片时触发，传递模式参数（'create' 或 'join'）
 * 
 * 组件结构：
 * - 选择网格容器：包含两个模式卡片
 * - 创建卡片：用于创建新团队
 * - 加入卡片：用于加入现有团队
 * - 每个卡片包含：左侧文字区域、右侧雷达图形、底部装饰条
 */
export default {
  name: 'TeamCreateModeSelector'
}
</script>

<style>
@import '@/assets/css/team-create/team-create-mode-selector.css';
</style>
