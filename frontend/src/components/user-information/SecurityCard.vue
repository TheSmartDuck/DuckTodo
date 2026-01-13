<template>
  <!-- 安全卡片根容器：深色主题 -->
  <div class="security-card-store">
    <!-- 深色主题卡片内容 -->
    <div class="security-card-store-content">
      <!-- 图标区域 -->
      <div class="security-card-store-icon">
        <div class="security-card-icon-c-circle">
          <i class="el-icon-lock"></i>
        </div>
      </div>
      
      <!-- 信息区域 -->
      <div class="security-card-store-info">
        <h2 class="security-card-store-title">SECURITY <span class="security-card-store-sub">/ 安全中心</span></h2>
        <div class="security-card-store-desc">
          <div class="security-card-desc-line">涉及系统底层访问权限的密钥对</div>
          <div class="security-card-desc-code">PROTOCOL: PROTECTED</div>
        </div>
      </div>

      <!-- 统计区域：显示密钥状态 -->
      <div class="security-card-store-stats">
        <!-- AK状态组 -->
        <div class="security-card-stat-group">
          <span class="security-card-label">AK STATUS</span>
          <div class="security-card-value-row">
            <span class="security-card-value">{{ hasAk ? 'CONFIGURED' : 'EMPTY' }}</span>
            <!-- 复制AK按钮 -->
            <i class="el-icon-document-copy security-card-copy-icon" v-if="hasAk" @click="copyText(keys.userAccesskey, 'AK')" title="Copy AK"></i>
          </div>
        </div>
        <!-- SK状态组 -->
        <div class="security-card-stat-group">
          <span class="security-card-label">SK STATUS</span>
          <div class="security-card-value-row">
            <span class="security-card-value">{{ hasSk ? 'CONFIGURED' : 'EMPTY' }}</span>
            <!-- 复制SK按钮 -->
            <i class="el-icon-document-copy security-card-copy-icon" v-if="hasSk" @click="copyText(keys.userSecretkey, 'SK')" title="Copy SK"></i>
          </div>
        </div>
        <!-- 密码状态组 -->
        <div class="security-card-stat-group">
          <span class="security-card-label">PASSWORD</span>
          <span class="security-card-value">**********</span>
        </div>
      </div>

      <!-- 操作按钮区域 -->
      <div class="security-card-store-actions">
        <!-- 管理密钥按钮 -->
        <div class="security-card-store-btn security-card-store-btn-primary" @click="goEditKeys">
           MANAGE KEYS
        </div>
        <!-- 管理密码按钮 -->
        <div class="security-card-store-btn" @click="goChangePassword">
           MANAGE PASSWORD
        </div>
      </div>
    </div>
    
    <!-- 密钥编辑抽屉 -->
    <el-drawer
      title="ACCESS KEYS / 修改个人AK"
      :visible.sync="showEditKeysDrawer"
      direction="rtl"
      size="600px"
      custom-class="security-card-retro-drawer"
      :append-to-body="true">
      <div class="security-card-edit-drawer">
        <!-- 密钥编辑表单 -->
        <el-form :model="keysForm" label-width="110px" size="small">
          <!-- 访问密钥字段 -->
          <el-form-item label="ACCESS KEY">
            <el-input v-model="keysForm.akSuffix" placeholder="Suffix" />
            <div style="margin-top:5px; text-align: right;">
              <el-button size="mini" @click="generateAkSuffix">AUTO GEN</el-button>
            </div>
          </el-form-item>
          <!-- 秘密密钥字段 -->
          <el-form-item label="SECRET KEY">
            <el-input v-model="keysForm.userSecretkey" placeholder="Full SK" />
            <div style="margin-top:5px; text-align: right;">
              <el-button size="mini" @click="generateSk">AUTO GEN</el-button>
            </div>
          </el-form-item>
        </el-form>
        <!-- 抽屉操作按钮 -->
        <div class="security-card-drawer-actions" style="margin-top: 20px; text-align: right;">
          <el-button size="small" @click="showEditKeysDrawer=false">CANCEL</el-button>
          <el-button size="small" type="danger" :loading="deletingKeys" @click="deleteKeys">DELETE</el-button>
          <el-button size="small" type="primary" :loading="savingKeys" @click="submitKeys">SAVE</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 密码修改抽屉 -->
    <el-drawer
      title="PASSWORD / 修改密码"
      :visible.sync="showPasswordDrawer"
      direction="rtl"
      size="600px"
      custom-class="security-card-retro-drawer"
      :append-to-body="true">
      <div class="security-card-edit-drawer">
        <!-- 密码修改表单 -->
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="140px" size="small">
          <!-- 原密码字段 -->
          <el-form-item label="ORIGINAL PWD" prop="originalPassword">
            <el-input v-model="passwordForm.originalPassword" type="password" show-password placeholder="Old Password" />
          </el-form-item>
          <!-- 新密码字段 -->
          <el-form-item label="NEW PASSWORD" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="New Password" />
          </el-form-item>
          <!-- 确认密码字段 -->
          <el-form-item label="CONFIRM PWD" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="Confirm New Password" />
          </el-form-item>
        </el-form>
        <!-- 抽屉操作按钮 -->
        <div class="security-card-drawer-actions" style="margin-top: 20px; text-align: right;">
          <el-button size="small" @click="showPasswordDrawer=false">CANCEL</el-button>
          <el-button size="small" type="primary" :loading="savingPassword" @click="submitPassword">CHANGE</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { updateAccessKeys, deleteAccessKeys, updatePassword } from '@/api/user-api'

/**
 * 安全卡片组件
 * 
 * 功能：
 * 1. 显示用户的安全设置信息（访问密钥、秘密密钥、密码状态）
 * 2. 支持管理访问密钥（AK）和秘密密钥（SK）
 * 3. 支持修改用户密码
 * 4. 支持复制密钥到剪贴板
 * 5. 支持自动生成密钥后缀和秘密密钥
 * 
 * 数据来源：
 * - 通过 props 接收用户信息对象和密钥对象
 * - 通过 user-api 的 updateAccessKeys、deleteAccessKeys、updatePassword API 更新安全设置
 * 
 * 组件结构：
 * - 深色主题卡片，显示安全信息
 * - 密钥编辑抽屉：用于编辑AK/SK
 * - 密码修改抽屉：用于修改密码
 */
export default {
  name: 'SecurityCard',
  props: {
    // 用户信息对象
    user: {
      type: Object,
      default: () => ({})
    },
    // 密钥对象
    keys: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      // 是否显示密钥编辑抽屉
      showEditKeysDrawer: false,
      // 是否显示密码修改抽屉
      showPasswordDrawer: false,
      // 密钥编辑表单数据
      keysForm: {
        akSuffix: '',        // AK后缀（不包含用户ID前缀）
        userSecretkey: ''    // 秘密密钥
      },
      // 密码修改表单数据
      passwordForm: {
        originalPassword: '',  // 原密码
        newPassword: '',        // 新密码
        confirmPassword: ''     // 确认密码
      },
      // 保存密钥状态
      savingKeys: false,
      // 删除密钥状态
      deletingKeys: false,
      // 保存密码状态
      savingPassword: false
    }
  },
  computed: {
    /**
     * 是否有访问密钥（AK）
     * 检查访问密钥是否存在且非空
     * @returns {boolean} 是否有AK
     */
    hasAk() {
      const ak = this.keys.userAccesskey
      return !!(ak && ak.trim())
    },
    
    /**
     * 是否有秘密密钥（SK）
     * 检查秘密密钥是否存在且非空
     * @returns {boolean} 是否有SK
     */
    hasSk() {
      const sk = this.keys.userSecretkey
      return !!(sk && sk.trim())
    },
    
    /**
     * 密码修改表单验证规则
     * 动态生成验证规则，确保能访问组件实例
     * @returns {Object} 表单验证规则对象
     */
    passwordRules() {
      /**
       * 确认密码验证函数
       * 验证确认密码是否与新密码一致
       * @param {Object} rule - 验证规则对象
       * @param {string} value - 待验证的值（确认密码）
       * @param {Function} callback - 验证回调函数
       */
      const validateConfirmPassword = (rule, value, callback) => {
        if (!value) {
          return callback(new Error('请再次输入新密码'))
        }
        if (value !== this.passwordForm.newPassword) {
          return callback(new Error('两次输入的新密码不一致'))
        }
        callback()
      }
      
      /**
       * 新密码验证函数
       * 验证新密码是否有效
       * 要求：至少8位且包含英文字母
       * @param {Object} rule - 验证规则对象
       * @param {string} value - 待验证的值（新密码）
       * @param {Function} callback - 验证回调函数
       */
      const validateNewPassword = (rule, value, callback) => {
        if (!value) {
          return callback(new Error('请输入新密码'))
        }
        // 验证密码长度（至少8位）
        if (value.length < 8) {
          return callback(new Error('密码长度不能少于8位'))
        }
        // 验证是否包含英文字母（大小写都可以）
        const hasEnglish = /[a-zA-Z]/.test(value)
        if (!hasEnglish) {
          return callback(new Error('密码必须包含至少一个英文字母'))
        }
        // 如果确认密码已输入，需要重新验证确认密码
        if (this.passwordForm.confirmPassword && this.$refs.passwordForm) {
          this.$refs.passwordForm.validateField('confirmPassword')
        }
        callback()
      }
      
      return {
        originalPassword: [
          { required: true, message: '请输入原密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, validator: validateNewPassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    /**
     * ========== 抽屉控制方法 ==========
     */
    
    /**
     * 打开密钥编辑抽屉
     * 初始化密钥表单数据，提取AK后缀（如果存在）
     */
    goEditKeys() {
      const currentAk = (this.keys.userAccesskey || '').toString()
      const uid = (this.user.userId || '').toString()
      // 如果AK以用户ID开头，提取后缀部分
      const suffix = currentAk.startsWith(uid) ? currentAk.slice(uid.length) : ''
      // 初始化表单数据
      this.keysForm = {
        akSuffix: suffix,
        userSecretkey: this.keys.userSecretkey || ''
      }
      // 显示编辑抽屉
      this.showEditKeysDrawer = true
    },
    
    /**
     * 打开密码修改抽屉
     * 重置密码表单数据并清除验证状态
     */
    goChangePassword() {
      this.passwordForm = {
        originalPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      // 显示密码修改抽屉
      this.showPasswordDrawer = true
      // 清除表单验证状态
      this.$nextTick(() => {
        if (this.$refs.passwordForm) {
          this.$refs.passwordForm.clearValidate()
        }
      })
    },
    
    /**
     * ========== 表单提交方法 ==========
     */
    
    /**
     * 提交密码修改
     * 使用表单验证规则验证表单，然后调用API修改密码
     */
    submitPassword() {
      // 检查表单引用是否存在
      if (!this.$refs.passwordForm) {
        this.$message && this.$message.error('表单未初始化')
        return
      }
      
      // 使用 Element UI 表单验证
      this.$refs.passwordForm.validate((valid) => {
        if (!valid) {
          // 表单验证失败，不执行提交
          return false
        }
        
        // 执行异步提交操作
        this.doSubmitPassword()
      })
    },
    
    /**
     * 执行密码修改提交
     * 实际的API调用逻辑
     */
    async doSubmitPassword() {
      try {
        this.savingPassword = true
        // 调用API修改密码
        await updatePassword({
          originalPassword: this.passwordForm.originalPassword,
          newPassword: this.passwordForm.newPassword
        })
        this.$message && this.$message.success('密码修改成功')
        this.showPasswordDrawer = false
        // 重置表单
        this.passwordForm = {
          originalPassword: '',
          newPassword: '',
          confirmPassword: ''
        }
        // 清除表单验证状态
        if (this.$refs.passwordForm) {
          this.$refs.passwordForm.clearValidate()
        }
      } catch (e) {
        // 错误由拦截器处理
        console.error('Password update failed', e)
      } finally {
        this.savingPassword = false
      }
    },
    
    /**
     * 提交密钥更新
     * 构建完整的AK（用户ID + 后缀）并调用API更新密钥
     */
    async submitKeys() {
      try {
        this.savingKeys = true
        const akSuffix = (this.keysForm.akSuffix || '').toString().trim()
        const uid = (this.user.userId || '').toString()
        const sk = (this.keysForm.userSecretkey || '').toString().trim()
        // 构建更新参数：AK = 用户ID + 后缀
        const params = {
          userAccesskey: akSuffix ? (uid + akSuffix) : null,
          userSecretkey: sk ? sk : null
        }
        // 调用API更新密钥
        await updateAccessKeys(params)
        this.$message && this.$message.success('AK/SK 更新成功')
        this.showEditKeysDrawer = false
        // 触发刷新事件，通知父组件更新密钥数据
        this.$emit('refresh-keys')
      } catch (e) {
        // 错误由拦截器处理
      } finally {
        this.savingKeys = false
      }
    },
    
    /**
     * 删除密钥
     * 调用API删除用户的访问密钥和秘密密钥
     */
    async deleteKeys() {
      try {
        this.deletingKeys = true
        // 调用API删除密钥
        await deleteAccessKeys()
        this.$message && this.$message.success('AK/SK 已删除')
        this.showEditKeysDrawer = false
        // 触发刷新事件，通知父组件更新密钥数据
        this.$emit('refresh-keys')
      } catch (e) {
        // 错误由拦截器处理
      } finally {
        this.deletingKeys = false
      }
    },
    
    /**
     * ========== 工具方法 ==========
     */
    
    /**
     * 复制文本到剪贴板
     * 使用传统的 document.execCommand 方法复制文本
     * @param {string} text - 要复制的文本
     * @param {string} label - 标签名称（用于提示消息）
     */
    copyText(text, label) {
      if (!text) return
      // 创建临时文本域元素
      const textarea = document.createElement('textarea')
      textarea.value = text
      document.body.appendChild(textarea)
      textarea.select()
      try {
        // 执行复制命令
        document.execCommand('copy')
        const msg = label ? `复制${label}成功` : '复制成功'
        this.$message && this.$message.success(msg)
      } catch (err) {
        this.$message && this.$message.error('复制失败')
      }
      // 移除临时元素
      document.body.removeChild(textarea)
    },
    
    /**
     * 生成AK后缀
     * 自动生成12位随机字符串作为AK后缀
     */
    generateAkSuffix() {
      this.keysForm.akSuffix = this.randomString(12)
    },
    
    /**
     * 生成SK
     * 自动生成32位随机字符串作为秘密密钥
     */
    generateSk() {
      this.keysForm.userSecretkey = this.randomString(32)
    },
    
    /**
     * 生成随机字符串
     * 使用指定的字符集生成指定长度的随机字符串
     * @param {number} len - 字符串长度（默认16）
     * @returns {string} 随机字符串
     */
    randomString(len = 16) {
      // 字符集：大写字母、小写字母、数字（排除易混淆字符）
      const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789'
      let out = ''
      for (let i = 0; i < len; i++) {
        out += chars[Math.floor(Math.random() * chars.length)]
      }
      return out
    }
  }
}
</script>

<style>
@import '@/assets/css/user-information/security-card.css';
</style>
