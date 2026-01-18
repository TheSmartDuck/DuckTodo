<template>
  <!-- LLM配置卡片根容器 -->
  <div class="llm-config-card-container">
    <!-- 左侧内容区域 -->
    <div class="llm-config-card-left-content">
      <!-- 卡片头部：标题和副标题 -->
      <div class="llm-config-card-header-row">
        <h2 class="llm-config-card-title">LLM CONFIG</h2>
        <span class="llm-config-card-subtitle">:: LLM配置</span>
      </div>
      
      <!-- 配置类型统计 -->
      <div class="llm-config-card-stats">
        <!-- Chat模型统计 -->
        <div class="llm-config-card-stat-item">
          <span class="llm-config-card-stat-label">CHAT</span>
          <span class="llm-config-card-stat-value">{{ chatConfigs.length }}</span>
        </div>
        <!-- Embedding模型统计 -->
        <div class="llm-config-card-stat-item">
          <span class="llm-config-card-stat-label">EMBED</span>
          <span class="llm-config-card-stat-value">{{ embedConfigs.length }}</span>
        </div>
        <!-- Rerank模型统计 -->
        <div class="llm-config-card-stat-item">
          <span class="llm-config-card-stat-label">RERANK</span>
          <span class="llm-config-card-stat-value">{{ rerankConfigs.length }}</span>
        </div>
      </div>
      
      <!-- 卡片操作按钮区域 -->
      <div class="llm-config-card-actions">
        <!-- 管理配置按钮 -->
        <div class="llm-config-card-action-btn" @click="goManage">
          <i class="el-icon-setting"></i> MANAGE
        </div>
      </div>
    </div>
    
    <!-- 右侧装饰区域 -->
    <div class="llm-config-card-right-deco">
      <!-- 大图标圆圈 -->
      <div class="llm-config-card-big-icon-circle">
        <i class="el-icon-cpu"></i>
      </div>
      <div class="llm-config-card-deco-text">AI</div>
    </div>

    <!-- 管理配置抽屉 -->
    <el-drawer
      title="LLM CONFIG / LLM配置管理"
      :visible.sync="showManageDrawer"
      direction="rtl"
      size="800px"
      custom-class="llm-config-card-retro-drawer"
      :append-to-body="true"
      @closed="resetForm">
      <div class="llm-config-card-manage-drawer">
        <!-- 标签页：按模型类型分类 -->
        <el-tabs v-model="activeTab" type="card" class="llm-config-card-tabs">
          <!-- Chat模型标签页 -->
          <el-tab-pane label="CHAT" name="chat">
            <div class="llm-config-card-tab-content">
              <div class="llm-config-card-list-header">
                <span class="llm-config-card-list-title">Chat模型配置</span>
                <el-button size="mini" type="primary" @click="openEditDrawer(1)">
                  <i class="el-icon-plus"></i> 添加配置
                </el-button>
              </div>
              <div class="llm-config-card-list">
                <div 
                  v-for="config in chatConfigs" 
                  :key="config.userLlmConfigId"
                  class="llm-config-card-item">
                  <div class="llm-config-card-item-info">
                    <div class="llm-config-card-item-name">{{ config.llmModelName }}</div>
                    <div class="llm-config-card-item-meta">
                      <span>{{ config.llmProvider }}</span>
                      <span class="llm-config-card-item-separator">|</span>
                      <span>{{ config.llmApiUrl }}</span>
                    </div>
                  </div>
                  <div class="llm-config-card-item-actions">
                    <el-button size="mini" @click="testConnectivity(config)">
                      <i class="el-icon-connection"></i> 测试
                    </el-button>
                    <el-button size="mini" @click="openEditDrawer(1, config)">
                      <i class="el-icon-edit"></i> 编辑
                    </el-button>
                    <el-button size="mini" type="danger" @click="handleDelete(config)">
                      <i class="el-icon-delete"></i> 删除
                    </el-button>
                  </div>
                </div>
                <div v-if="chatConfigs.length === 0" class="llm-config-card-empty">
                  暂无Chat模型配置
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- Embedding模型标签页 -->
          <el-tab-pane label="EMBED" name="embed">
            <div class="llm-config-card-tab-content">
              <div class="llm-config-card-list-header">
                <span class="llm-config-card-list-title">Embedding模型配置</span>
                <el-button size="mini" type="primary" @click="openEditDrawer(2)">
                  <i class="el-icon-plus"></i> 添加配置
                </el-button>
              </div>
              <div class="llm-config-card-list">
                <div 
                  v-for="config in embedConfigs" 
                  :key="config.userLlmConfigId"
                  class="llm-config-card-item">
                  <div class="llm-config-card-item-info">
                    <div class="llm-config-card-item-name">{{ config.llmModelName }}</div>
                    <div class="llm-config-card-item-meta">
                      <span>{{ config.llmProvider }}</span>
                      <span class="llm-config-card-item-separator">|</span>
                      <span>{{ config.llmApiUrl }}</span>
                    </div>
                  </div>
                  <div class="llm-config-card-item-actions">
                    <el-button size="mini" @click="testConnectivity(config)">
                      <i class="el-icon-connection"></i> 测试
                    </el-button>
                    <el-button size="mini" @click="openEditDrawer(2, config)">
                      <i class="el-icon-edit"></i> 编辑
                    </el-button>
                    <el-button size="mini" type="danger" @click="handleDelete(config)">
                      <i class="el-icon-delete"></i> 删除
                    </el-button>
                  </div>
                </div>
                <div v-if="embedConfigs.length === 0" class="llm-config-card-empty">
                  暂无Embedding模型配置
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <!-- Rerank模型标签页 -->
          <el-tab-pane label="RERANK" name="rerank">
            <div class="llm-config-card-tab-content">
              <div class="llm-config-card-list-header">
                <span class="llm-config-card-list-title">Rerank模型配置</span>
                <el-button size="mini" type="primary" @click="openEditDrawer(3)">
                  <i class="el-icon-plus"></i> 添加配置
                </el-button>
              </div>
              <div class="llm-config-card-list">
                <div 
                  v-for="config in rerankConfigs" 
                  :key="config.userLlmConfigId"
                  class="llm-config-card-item">
                  <div class="llm-config-card-item-info">
                    <div class="llm-config-card-item-name">{{ config.llmModelName }}</div>
                    <div class="llm-config-card-item-meta">
                      <span>{{ config.llmProvider }}</span>
                      <span class="llm-config-card-item-separator">|</span>
                      <span>{{ config.llmApiUrl }}</span>
                    </div>
                  </div>
                  <div class="llm-config-card-item-actions">
                    <el-button size="mini" @click="testConnectivity(config)">
                      <i class="el-icon-connection"></i> 测试
                    </el-button>
                    <el-button size="mini" @click="openEditDrawer(3, config)">
                      <i class="el-icon-edit"></i> 编辑
                    </el-button>
                    <el-button size="mini" type="danger" @click="handleDelete(config)">
                      <i class="el-icon-delete"></i> 删除
                    </el-button>
                  </div>
                </div>
                <div v-if="rerankConfigs.length === 0" class="llm-config-card-empty">
                  暂无Rerank模型配置
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>

    <!-- 编辑配置抽屉 -->
    <el-drawer
      :title="editDrawerTitle"
      :visible.sync="showEditDrawer"
      direction="rtl"
      size="600px"
      custom-class="llm-config-card-retro-drawer"
      :append-to-body="true"
      @closed="resetForm">
      <div class="llm-config-card-edit-drawer">
        <!-- 编辑表单 -->
        <el-form :model="editForm" :rules="rules" ref="editForm" label-width="120px" size="small">
          <!-- 模型类型字段（编辑时禁用） -->
          <el-form-item label="模型类型" prop="llmModelType">
            <el-select v-model="editForm.llmModelType" :disabled="!!editingConfig" style="width:100%" popper-class="llm-config-card-retro-dropdown">
              <el-option label="CHAT" :value="1" />
              <el-option label="EMBEDDING" :value="2" />
              <el-option label="RERANK" :value="3" />
            </el-select>
          </el-form-item>
          <!-- 提供商字段 -->
          <el-form-item label="模型提供商" prop="llmProvider">
            <el-select v-model="editForm.llmProvider" style="width:100%" popper-class="llm-config-card-retro-dropdown">
              <el-option label="OpenAi" value="openai" />
              <el-option label="阿里百炼" value="bailian" />
              <el-option label="硅基流动" value="siliconflow" />
              <el-option label="OpenAi兼容" value="openai-compatible" />
            </el-select>
          </el-form-item>
          <!-- API Key字段 -->
          <el-form-item label="KEY" prop="llmApiKey">
            <el-input v-model="editForm.llmApiKey" type="password" show-password placeholder="LLM API Key" />
          </el-form-item>
          <!-- API URL字段：仅在OpenAi兼容时显示 -->
          <el-form-item v-if="editForm.llmProvider === 'openai-compatible'" label="模型URL" prop="llmApiUrl">
            <el-input v-model="editForm.llmApiUrl" placeholder="LLM API地址" />
          </el-form-item>
          <!-- 模型名称字段 -->
          <el-form-item label="模型名称" prop="llmModelName">
            <el-input v-model="editForm.llmModelName" placeholder="模型名称" />
          </el-form-item>
          <!-- 温度字段：仅在CHAT模型时显示 -->
          <el-form-item v-if="editForm.llmModelType === 1" label="模型温度" prop="llmModelTemperature">
            <el-input 
              v-model="editForm.llmModelTemperature" 
              placeholder="0.0 - 1.0"
              @input="handleTemperatureInput"
              style="width:100%" />
          </el-form-item>
          <!-- 思考模式字段（仅Chat模型） -->
          <el-form-item v-if="editForm.llmModelType === 1" label="是否需要推理" prop="llmModelThinking">
            <el-select v-model="editForm.llmModelThinking" style="width:100%" popper-class="llm-config-card-retro-dropdown">
              <el-option label="DISABLED" :value="0" />
              <el-option label="ENABLED" :value="1" />
            </el-select>
          </el-form-item>
        </el-form>
        <!-- 抽屉操作按钮 -->
        <div class="llm-config-card-drawer-actions" style="margin-top: 20px; text-align: right;">
          <el-button size="small" @click="testConnectivity(editForm)" :loading="testing">
            <i class="el-icon-connection"></i> 测试连通性
          </el-button>
          <el-button size="small" @click="showEditDrawer=false">CANCEL</el-button>
          <el-button size="small" type="primary" @click="submitEdit" :loading="saving">SAVE</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getMyLlmConfigList, createLlmConfig, updateLlmConfig, deleteLlmConfig } from '@/api/llm-config-api'
import { testLlmConnectivity as testAiLlmConnectivity } from '@/api/ai-llm-api'
import { isNonEmpty } from '@/utils/validation'

/**
 * LLM配置卡片组件
 * 
 * 功能：
 * 1. 显示三种类型的LLM配置统计（Chat、Embedding、Rerank）
 * 2. 支持管理LLM配置（添加、编辑、删除）
 * 3. 支持测试LLM配置连通性（通过 ai-backend）
 * 4. 按模型类型分类显示配置列表
 * 
 * 数据来源：
 * - 通过 llm-config-api 的 getMyLlmConfigList API 获取配置列表
 * - 通过 llm-config-api 的 createLlmConfig、updateLlmConfig、deleteLlmConfig API 管理配置
 * - 通过 ai-llm-api 的 testLlmConnectivity API 测试连通性（调用 ai-backend）
 */
export default {
  name: 'UserLLmConfigCard',
  data() {
    /**
     * 提供商验证函数
     */
    const validateProvider = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('提供商不能为空'))
      }
      callback()
    }
    
    /**
     * API Key验证函数
     */
    const validateApiKey = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('API Key不能为空'))
      }
      callback()
    }
    
    /**
     * API URL验证函数
     * 仅在OpenAi兼容时必填
     */
    const validateApiUrl = (rule, value, callback) => {
      // 获取当前表单的提供商值
      const provider = this.editForm.llmProvider
      // 只有在OpenAi兼容时才需要验证URL
      if (provider === 'openai-compatible') {
        // 检查值是否为空
        if (!value || (typeof value === 'string' && value.trim() === '')) {
          return callback(new Error('LLM API地址不能为空'))
        }
        // 值有效，通过验证
        callback()
      } else {
        // 非OpenAi兼容时，直接通过验证（不需要URL）
        callback()
      }
    }
    
    /**
     * 模型名称验证函数
     */
    const validateModelName = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('模型名称不能为空'))
      }
      callback()
    }
    
    /**
     * 模型类型验证函数
     */
    const validateModelType = (rule, value, callback) => {
      if (!value || ![1, 2, 3].includes(value)) {
        return callback(new Error('模型类型不能为空'))
      }
      callback()
    }
    
    /**
     * 温度验证函数
     * 仅在CHAT模型时验证
     */
    const validateTemperature = (rule, value, callback) => {
      // 获取当前表单的模型类型值
      const modelType = this.editForm.llmModelType
      // 只有在CHAT模型时才需要验证温度
      if (modelType !== 1) {
        return callback()
      }
      if (value === '' || value === null || value === undefined) {
        return callback()
      }
      const num = Number(value)
      if (isNaN(num)) {
        return callback(new Error('温度必须是数字'))
      }
      if (num < 0 || num > 1) {
        return callback(new Error('温度必须在0.0-1.0之间'))
      }
      callback()
    }

    return {
      // 配置列表
      configList: [],
      // 是否显示管理抽屉
      showManageDrawer: false,
      // 是否显示编辑抽屉
      showEditDrawer: false,
      // 当前编辑的配置
      editingConfig: null,
      // 活动标签页
      activeTab: 'chat',
      // 编辑表单数据
      editForm: {
        llmModelType: 1,           // 模型类型（1-chat, 2-embedding, 3-rerank）
        llmProvider: 'openai',     // 提供商（默认OpenAi）
        llmApiKey: '',              // API Key
        llmApiUrl: '',              // API URL
        llmModelName: '',           // 模型名称
        llmModelTemperature: 0.7,   // 温度（默认0.7）
        llmModelThinking: 0         // 思考模式（0-否, 1-是，仅Chat模型）
      },
      // 表单验证规则
      rules: {
        llmModelType: [
          { required: true, validator: validateModelType, trigger: 'change' }
        ],
        llmProvider: [
          { required: true, validator: validateProvider, trigger: 'blur' }
        ],
        llmApiKey: [
          { required: true, validator: validateApiKey, trigger: 'blur' }
        ],
        llmApiUrl: [
          { validator: validateApiUrl, trigger: ['blur', 'change'] }
        ],
        llmModelName: [
          { required: true, validator: validateModelName, trigger: 'blur' }
        ],
        llmModelTemperature: [
          { validator: validateTemperature, trigger: 'blur' }
        ]
      },
      // 保存状态
      saving: false,
      // 测试状态
      testing: false,
      // 加载状态
      loading: false
    }
  },
  watch: {
    /**
     * 监听提供商变化
     * 当不是OpenAi兼容时，清空URL字段
     * 当是OpenAi兼容时，重新验证URL字段
     */
    'editForm.llmProvider'(newVal) {
      if (newVal !== 'openai-compatible') {
        this.editForm.llmApiUrl = ''
        // 清除URL字段的验证错误
        if (this.$refs.editForm) {
          this.$refs.editForm.clearValidate('llmApiUrl')
        }
      } else {
        // 当切换到OpenAi兼容时，重新验证URL字段
        this.$nextTick(() => {
          if (this.$refs.editForm) {
            this.$refs.editForm.validateField('llmApiUrl')
          }
        })
      }
    },
    /**
     * 监听模型类型变化
     * 当不是CHAT类型时，清空温度字段
     */
    'editForm.llmModelType'(newVal) {
      if (newVal !== 1) {
        this.editForm.llmModelTemperature = ''
        // 清除温度字段的验证错误
        if (this.$refs.editForm) {
          this.$refs.editForm.clearValidate('llmModelTemperature')
        }
      }
    }
  },
  mounted() {
    /**
     * 组件挂载时自动加载配置列表
     * 这样卡片上就能显示各类型模型的数量
     */
    this.loadConfigs()
  },
  computed: {
    /**
     * Chat模型配置列表
     * 过滤出模型类型为1的配置
     */
    chatConfigs() {
      return (this.configList || []).filter(c => c.llmModelType === 1)
    },
    
    /**
     * Embedding模型配置列表
     * 过滤出模型类型为2的配置
     */
    embedConfigs() {
      return (this.configList || []).filter(c => c.llmModelType === 2)
    },
    
    /**
     * Rerank模型配置列表
     * 过滤出模型类型为3的配置
     */
    rerankConfigs() {
      return (this.configList || []).filter(c => c.llmModelType === 3)
    },
    
    /**
     * 编辑抽屉标题
     * 根据是否编辑模式显示不同标题
     */
    editDrawerTitle() {
      if (this.editingConfig) {
        return 'EDIT LLM CONFIG / 编辑LLM配置'
      }
      const typeMap = { 1: 'CHAT', 2: 'EMBEDDING', 3: 'RERANK' }
      return `ADD ${typeMap[this.editForm.llmModelType]} CONFIG / 添加${typeMap[this.editForm.llmModelType]}配置`
    }
  },
  methods: {
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载配置列表
     * 从API获取当前用户的所有LLM配置
     */
    async loadConfigs() {
      try {
        this.loading = true
        const res = await getMyLlmConfigList()
        this.configList = Array.isArray(res) ? res : []
      } catch (e) {
        this.configList = []
        console.error('Failed to load LLM configs', e)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * ========== 抽屉控制方法 ==========
     */
    
    /**
     * 打开管理抽屉
     * 加载配置列表并显示管理抽屉
     */
    async goManage() {
      await this.loadConfigs()
      this.showManageDrawer = true
    },
    
    /**
     * 打开编辑抽屉
     * 初始化编辑表单数据，并显示编辑抽屉
     * @param {number} modelType - 模型类型（1-chat, 2-embedding, 3-rerank）
     * @param {Object} config - 要编辑的配置对象（可选，如果提供则为编辑模式）
     */
    openEditDrawer(modelType, config = null) {
      this.editingConfig = config
      if (config) {
        // 编辑模式：填充现有配置数据
        this.editForm = {
          llmModelType: config.llmModelType || modelType,
          llmProvider: config.llmProvider || '',
          llmApiKey: config.llmApiKey || '',
          llmApiUrl: config.llmApiUrl || '',
          llmModelName: config.llmModelName || '',
          llmModelTemperature: config.llmModelTemperature !== undefined ? config.llmModelTemperature : 0.7,
          llmModelThinking: config.llmModelThinking !== undefined ? config.llmModelThinking : 0
        }
      } else {
        // 新增模式：使用默认值
        this.editForm = {
          llmModelType: modelType,
          llmProvider: 'openai',    // 默认OpenAi
          llmApiKey: '',
          llmApiUrl: '',
          llmModelName: '',
          llmModelTemperature: 0.7,
          llmModelThinking: 0
        }
      }
      // 如果提供商不是OpenAi兼容，清除URL字段的验证错误
      this.$nextTick(() => {
        if (this.$refs.editForm && this.editForm.llmProvider !== 'openai-compatible') {
          this.$refs.editForm.clearValidate('llmApiUrl')
        }
      })
      // 显示编辑抽屉
      this.showEditDrawer = true
      // 清除表单验证状态
      this.$nextTick(() => {
        if (this.$refs.editForm) {
          this.$refs.editForm.clearValidate()
        }
      })
    },
    
    /**
     * 重置表单
     * 当抽屉关闭时重置表单字段
     */
    resetForm() {
      this.editingConfig = null
      if (this.$refs.editForm) {
        this.$refs.editForm.resetFields()
      }
    },
    
    /**
     * ========== 表单提交方法 ==========
     */
    
    /**
     * 提交编辑
     * 验证表单并提交创建或更新配置
     */
    async submitEdit() {
      if (!this.$refs.editForm) {
        this.$message && this.$message.error('表单未初始化')
        return
      }
      
      // 如果提供商不是OpenAi兼容，先清除URL字段的验证错误
      if (this.editForm.llmProvider !== 'openai-compatible') {
        this.$refs.editForm.clearValidate('llmApiUrl')
      }
      
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            this.saving = true
            const payload = {
              llmModelType: this.editForm.llmModelType,
              llmProvider: this.editForm.llmProvider,
              llmApiKey: this.editForm.llmApiKey,
              llmModelName: this.editForm.llmModelName
            }
            
            // API URL：仅在OpenAi兼容时添加
            if (this.editForm.llmProvider === 'openai-compatible' && this.editForm.llmApiUrl) {
              payload.llmApiUrl = this.editForm.llmApiUrl
            }
            
            // 温度：仅在CHAT模型时添加
            if (this.editForm.llmModelType === 1 && this.editForm.llmModelTemperature !== undefined && this.editForm.llmModelTemperature !== null && this.editForm.llmModelTemperature !== '') {
              const temp = Number(this.editForm.llmModelTemperature)
              if (!isNaN(temp) && temp >= 0 && temp <= 1) {
                payload.llmModelTemperature = temp
              }
            }
            
            // 思考模式（仅Chat模型）
            if (this.editForm.llmModelType === 1 && this.editForm.llmModelThinking !== undefined) {
              payload.llmModelThinking = this.editForm.llmModelThinking
            }
            
            // 在提交前进行连通性测试
            try {
              this.testing = true
              const testConfig = {
                llmModelType: this.editForm.llmModelType,
                llmProvider: this.editForm.llmProvider,
                llmApiKey: this.editForm.llmApiKey,
                llmModelName: this.editForm.llmModelName
              }
              
              // API URL：仅在OpenAi兼容时添加
              if (this.editForm.llmProvider === 'openai-compatible' && this.editForm.llmApiUrl) {
                testConfig.llmApiUrl = this.editForm.llmApiUrl
              }
              
              // 温度：仅在CHAT模型时添加
              if (this.editForm.llmModelType === 1 && this.editForm.llmModelTemperature !== undefined && this.editForm.llmModelTemperature !== null && this.editForm.llmModelTemperature !== '') {
                testConfig.llmModelTemperature = Number(this.editForm.llmModelTemperature)
              }
              
              // 思考模式（仅Chat模型）
              if (this.editForm.llmModelType === 1 && this.editForm.llmModelThinking !== undefined) {
                testConfig.llmModelThinking = this.editForm.llmModelThinking
              }
              
              // 测试连通性
              await testAiLlmConnectivity(testConfig)
              this.$message && this.$message.success('连通性测试通过')
            } catch (testErr) {
              // 连通性测试失败，不提交配置
              this.$message && this.$message.error(testErr.message || '连通性测试失败，请检查配置信息')
              return
            } finally {
              this.testing = false
            }
            
            // 连通性测试通过后，提交配置
            if (this.editingConfig) {
              // 更新配置
              await updateLlmConfig(this.editingConfig.userLlmConfigId, payload)
              this.$message && this.$message.success('配置更新成功')
            } else {
              // 创建配置
              await createLlmConfig(payload)
              this.$message && this.$message.success('配置创建成功')
            }
            
            this.showEditDrawer = false
            // 重新加载配置列表
            await this.loadConfigs()
          } catch (err) {
            this.$message && this.$message.error(err.message || '操作失败')
          } finally {
            this.saving = false
          }
        } else {
          return false
        }
      })
    },
    
    /**
     * ========== 配置操作方法 ==========
     */
    
    /**
     * 删除配置
     * 确认后删除指定的配置
     * @param {Object} config - 要删除的配置对象
     */
    async handleDelete(config) {
      if (!config || !config.userLlmConfigId) {
        this.$message && this.$message.error('配置信息不完整')
        return
      }
      
      try {
        await this.$confirm('确定要删除此配置吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await deleteLlmConfig(config.userLlmConfigId)
        this.$message && this.$message.success('配置删除成功')
        // 重新加载配置列表
        await this.loadConfigs()
      } catch (err) {
        if (err !== 'cancel') {
          this.$message && this.$message.error(err.message || '删除失败')
        }
      }
    },
    
    /**
     * ========== 输入处理方法 ==========
     */
    
    /**
     * 处理温度输入
     * 限制只能输入0-1之间的数字（包括小数）
     * @param {string} value - 输入的值
     */
    handleTemperatureInput(value) {
      if (value === '' || value === null || value === undefined) {
        this.editForm.llmModelTemperature = ''
        return
      }
      
      // 移除所有非数字和小数点的字符
      let cleaned = String(value).replace(/[^\d.]/g, '')
      
      // 确保只有一个小数点
      const parts = cleaned.split('.')
      if (parts.length > 2) {
        cleaned = parts[0] + '.' + parts.slice(1).join('')
      }
      
      // 限制小数位数（最多1位）
      if (parts.length === 2 && parts[1].length > 1) {
        cleaned = parts[0] + '.' + parts[1].substring(0, 1)
      }
      
      // 如果只是小数点或空字符串，允许输入（用户可能正在输入）
      if (cleaned === '' || cleaned === '.') {
        this.editForm.llmModelTemperature = cleaned
        return
      }
      
      // 转换为数字并限制范围
      const num = parseFloat(cleaned)
      if (!isNaN(num)) {
        if (num < 0) {
          this.editForm.llmModelTemperature = 0
        } else if (num > 1) {
          this.editForm.llmModelTemperature = 1
        } else {
          // 保留一位小数，但如果用户正在输入，保留原始格式
          const rounded = Math.round(num * 10) / 10
          // 如果四舍五入后的值等于原值，保持用户输入的格式
          if (Math.abs(rounded - num) < 0.0001) {
            this.editForm.llmModelTemperature = cleaned
          } else {
            this.editForm.llmModelTemperature = rounded
          }
        }
      }
    },
    
    /**
     * ========== 配置操作方法 ==========
     */
    
    /**
     * 测试连通性
     * 测试指定配置的连通性（通过 ai-backend）
     * @param {Object} config - 要测试的配置对象
     */
    async testConnectivity(config) {
      if (!config) {
        this.$message && this.$message.error('配置信息不完整')
        return
      }
      
      try {
        this.testing = true
        const testConfig = {
          llmModelType: config.llmModelType,
          llmProvider: config.llmProvider,
          llmApiKey: config.llmApiKey,
          llmModelName: config.llmModelName
        }
        
        // API URL：仅在OpenAi兼容时添加
        if (config.llmProvider === 'openai-compatible' && config.llmApiUrl) {
          testConfig.llmApiUrl = config.llmApiUrl
        }
        
        // 温度：仅在CHAT模型时添加
        if (config.llmModelType === 1 && config.llmModelTemperature !== undefined) {
          testConfig.llmModelTemperature = config.llmModelTemperature
        }
        if (config.llmModelThinking !== undefined) {
          testConfig.llmModelThinking = config.llmModelThinking
        }
        
        // 使用 ai-backend 的接口测试连通性
        await testAiLlmConnectivity(testConfig)
        this.$message && this.$message.success('连通性测试成功')
      } catch (err) {
        this.$message && this.$message.error(err.message || '连通性测试失败')
      } finally {
        this.testing = false
      }
    }
  }
}
</script>

<style>
@import '@/assets/css/user-information/user-llm-config-card.css';
</style>
