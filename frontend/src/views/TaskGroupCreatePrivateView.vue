<template>
  <!-- 创建私有任务组页面根容器 -->
  <div class="create-private-page">
    <!-- 背景可视化元素：装饰性图表和数据可视化 -->
    <div class="create-private-bg-visuals">
      <!-- 柱状图元素 -->
      <div class="create-private-bg-chart-bar"></div>
      <!-- 正弦波曲线 -->
      <div class="create-private-bg-chart-line"></div>
      <!-- 散点图数据点 -->
      <div class="create-private-bg-chart-scatter"></div>
      <!-- 网格叠加层 -->
      <div class="create-private-bg-grid-overlay"></div>
      <!-- 极坐标图 -->
      <div class="create-private-bg-polar-chart"></div>
      <!-- 浮动数据块 -->
      <div class="create-private-bg-data-block create-private-b1"></div>
      <div class="create-private-bg-data-block create-private-b2"></div>
      <div class="create-private-bg-data-block create-private-b3"></div>
      <!-- 连接线 -->
      <div class="create-private-bg-connect-line create-private-l1"></div>
      <div class="create-private-bg-connect-line create-private-l2"></div>
    </div>

    <!-- 左侧：技术控制面板 -->
    <div class="create-private-page-left">
      <div class="create-private-tech-panel">
        <!-- 装饰性角落标记 -->
        <div class="create-private-corner-marker create-private-tl"></div>
        <div class="create-private-corner-marker create-private-tr"></div>
        <div class="create-private-corner-marker create-private-bl"></div>
        <div class="create-private-corner-marker create-private-br"></div>

        <!-- 技术面板头部 -->
        <div class="create-private-tech-header">
          <div class="create-private-header-decoration"></div>
          <div class="create-private-header-title">NEW TASK GROUP // <span class="create-private-accent">新建任务族</span></div>
          <div class="create-private-header-id">NO. 编号: {{ Math.floor(Math.random() * 10000).toString().padStart(4, '0') }}</div>
        </div>

        <!-- 技术面板主体：表单区域 -->
        <div class="create-private-tech-body">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="small" class="create-private-rhine-form">
            
            <!-- 表单区域标题：基础信息 -->
            <div class="create-private-form-section-title">
              <span class="create-private-icon">01</span>
              BASIC INFO 基础信息
            </div>
            
            <!-- 名称输入组 -->
            <el-form-item prop="groupName" class="create-private-rhine-input-group">
              <div class="create-private-input-label">NAME 名称</div>
              <el-input v-model="form.groupName" placeholder="INPUT GROUP NAME 输入任务族名称..." class="create-private-rhine-input"  />
            </el-form-item>
            
            <!-- 描述输入组 -->
            <el-form-item prop="groupDescription" class="create-private-rhine-input-group">
              <div class="create-private-input-label">DESCRIPTION 描述</div>
              <el-input type="textarea" :rows="3" v-model="form.groupDescription" placeholder="INPUT DESCRIPTION 输入描述信息..." class="create-private-rhine-input" />
            </el-form-item>
            
            <!-- 表单区域标题：主题色 -->
            <div class="create-private-form-section-title">
              <span class="create-private-icon">02</span>
              THEME COLOR 主题色
            </div>

            <!-- 颜色选择器 -->
            <el-form-item prop="groupColor">
              <div class="create-private-color-selector-container">
                <button
                  v-for="(c, i) in presetColors"
                  :key="c + '_' + i"
                  class="create-private-color-chip"
                  :class="{ 'create-private-active': form.groupColor === c }"
                  :style="{ backgroundColor: c }"
                  @click.prevent="form.groupColor = c"
                  type="button"
                >
                  <div class="create-private-chip-marker" v-if="form.groupColor === c"></div>
                </button>
              </div>
            </el-form-item>

            <!-- 技术分隔线 -->
            <div class="create-private-tech-divider"></div>

            <!-- 操作按钮行 -->
            <div class="create-private-action-row">
              <button class="create-private-rhine-btn create-private-secondary" @click.prevent="resetForm" :disabled="submitting">RESET 重置</button>
              <button class="create-private-rhine-btn create-private-primary" @click.prevent="onSubmit" :disabled="submitting">
                {{ submitting ? 'CONFIRM 确认中...' : 'CONFIRM 确认' }}
              </button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 右侧：数据可视化预览 -->
    <div class="create-private-page-right">
      <div class="create-private-info-card-container">
        <div class="create-private-info-card">
          <!-- 卡片头部 -->
          <div class="create-private-card-header">
            <span class="create-private-card-type">PREVIEW 效果预览</span>
            <span class="create-private-card-status">ACTIVE 活跃</span>
          </div>
          
          <!-- 卡片可视化区域：圆形预览 -->
          <div class="create-private-card-visual">
            <div class="create-private-visual-circle-outer">
              <div class="create-private-visual-circle-inner" :style="{ borderColor: form.groupColor }">
                <div class="create-private-visual-content" :style="{ color: form.groupColor }">
                  {{ (form.groupName || '无').substring(0, 1).toUpperCase() }}
                </div>
              </div>
            </div>
          </div>

          <!-- 卡片数据行：名称 -->
          <div class="create-private-card-data-row">
            <span class="create-private-label">NAME 名称</span>
            <span class="create-private-value" :style="{ color: form.groupColor }">{{ form.groupName || 'PENDING 等待输入' }}</span>
          </div>
          
          <!-- 卡片数据行：主题色 -->
          <div class="create-private-card-data-row">
             <span class="create-private-label">THEME 主题</span>
             <div class="create-private-color-preview-bar" :style="{ backgroundColor: form.groupColor }"></div>
             <span class="create-private-value-small">{{ form.groupColor }}</span>
          </div>

          <!-- 卡片底部装饰 -->
          <div class="create-private-card-footer-deco">
             <div class="create-private-barcode"></div>
             <div class="create-private-deco-text">CREATE PRIVATE TASK GROUP // 创建 私人任务族</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Message } from 'element-ui'
import { createPrivateTaskGroup } from '@/api/taskgroup-api'

/**
 * 创建私有任务组视图组件
 * 
 * 功能：
 * 1. 创建新的私有任务组
 * 2. 设置任务组名称、描述和主题色
 * 3. 实时预览任务组效果
 * 4. 表单验证和提交
 * 
 * 数据来源：
 * - 通过 taskgroup-api 的 createPrivateTaskGroup API 创建任务组
 * 
 * 组件结构：
 * - 左侧：技术控制面板（表单）
 * - 右侧：数据可视化预览（实时预览效果）
 * - 背景：装饰性图表和数据可视化元素
 */
export default {
  name: 'TaskGroupCreatePrivateView',
  data () {
    return {
      // 提交状态
      submitting: false,
      // 表单数据
      form: {
        groupName: '',           // 任务组名称
        groupDescription: '',    // 任务组描述
        groupColor: '#B2653B'   // 任务组主题色（默认：任务铁锈红）
      },
      // 表单验证规则
      rules: {
        groupName: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          { min: 1, max: 40, message: '长度 1-40', trigger: 'blur' }
        ]
      },
      // 预设颜色列表（主题色 + 扩展色）
      presetColors: [
        '#B2653B', // mission-rust（任务铁锈红）
        '#BA8530', // avionics-mustard（航空芥末黄）
        '#5C7F71', // oxidized-sage（氧化鼠尾草绿）
        '#802520', // alarm-burgundy（警报酒红）
        '#181818', // space-black（深空黑）
        '#409EFF', // 扩展蓝色
        '#E6A23C', // 扩展橙色
        '#67C23A', // 扩展绿色
        '#F56C6C'  // 扩展红色
      ]
    }
  },
  methods: {
    /**
     * ========== 表单操作方法 ==========
     */
    
    /**
     * 重置表单
     * 清空表单数据并清除验证状态
     */
    resetForm () {
      this.form.groupName = ''
      this.form.groupDescription = ''
      this.form.groupColor = '#B2653B' // 重置为默认颜色
      // 清除表单验证状态
      if (this.$refs.formRef) {
        this.$refs.formRef.clearValidate()
      }
    },
    
    /**
     * 提交表单
     * 验证表单并调用API创建任务组
     */
    onSubmit () {
      // 检查表单引用是否存在
      if (!this.$refs.formRef) {
        Message.error('表单未初始化')
        return
      }
      
      // 使用 Element UI 表单验证
      this.$refs.formRef.validate(async (valid) => {
        if (!valid) {
          // 表单验证失败，不执行提交
          return false
        }
        
        // 执行异步提交操作
        this.doSubmit()
      })
    },
    
    /**
     * 执行提交操作
     * 实际的API调用逻辑
     */
    async doSubmit() {
      try {
        this.submitting = true
        // 构建提交数据
        const data = {
          groupName: this.form.groupName,
          groupDescription: this.form.groupDescription,
          groupColor: this.form.groupColor
        }
        // 调用API创建私有任务组
        await createPrivateTaskGroup(data)
        Message.success('创建成功')
        // 触发全局事件，刷新任务组列表
        if (this.$root && this.$root.$emit) {
          this.$root.$emit('refresh-my-taskgroups')
        }
        // 跳转到首页
        this.$router.push('/').catch(() => {})
      } catch (e) {
        // 错误由拦截器处理
        console.error('Create task group failed', e)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style>
@import '@/assets/css/task-group/create-private.css';
</style>
