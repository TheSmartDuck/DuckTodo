<template>
  <!-- 基本信息卡片根容器 -->
  <div class="basic-info-card-operator">
    <!-- 左侧内容区域 -->
    <div class="basic-info-card-left-content">
      <!-- 卡片头部：标题和副标题 -->
      <div class="basic-info-card-header-row">
        <h2 class="basic-info-card-title">BASIC INFO</h2>
        <span class="basic-info-card-subtitle">:: 基础信息</span>
      </div>
      
      <!-- 信息网格：显示用户基本信息 -->
      <div class="basic-info-card-info-grid">
        <!-- 邮箱信息项 -->
        <div class="basic-info-card-info-item">
          <span class="basic-info-card-label">MAILBOX</span>
          <span class="basic-info-card-value">{{ user.userEmail || 'N/A' }}</span>
        </div>
        <!-- 电话信息项 -->
        <div class="basic-info-card-info-item">
          <span class="basic-info-card-label">PHONE</span>
          <span class="basic-info-card-value">{{ user.userPhone || 'N/A' }}</span>
        </div>
        <!-- 性别信息项 -->
        <div class="basic-info-card-info-item">
          <span class="basic-info-card-label">GENDER</span>
          <span class="basic-info-card-value">{{ genderText }}</span>
        </div>
        <!-- 状态信息项 -->
        <div class="basic-info-card-info-item">
          <span class="basic-info-card-label">STATUS</span>
          <span class="basic-info-card-value">ACTIVE</span>
        </div>
      </div>
      
      <!-- 卡片操作按钮区域 -->
      <div class="basic-info-card-actions">
        <!-- 编辑按钮 -->
        <div class="basic-info-card-action-btn" @click="goEdit">
           <i class="el-icon-edit"></i> EDIT
        </div>
        <!-- 上传头像按钮 -->
        <el-upload
          :action="'/api/user/me/avatar'"
          :headers="uploadHeaders"
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :on-success="onAvatarUploadSuccess"
          :on-error="onAvatarUploadError"
          accept="image/*"
          class="basic-info-card-inline-upload">
          <div class="basic-info-card-action-btn basic-info-card-action-btn-secondary">
             <i class="el-icon-camera"></i> UPLOAD AVATAR
          </div>
        </el-upload>
      </div>
    </div>
    
    <!-- 右侧装饰区域 -->
    <div class="basic-info-card-right-deco">
       <!-- 大图标圆圈 -->
       <div class="basic-info-card-big-icon-circle">
         <i class="el-icon-user"></i>
       </div>
       <div class="basic-info-card-deco-text">OPERATOR</div>
    </div>

    <!-- 编辑抽屉：修改个人信息 -->
    <el-drawer
      title="EDIT PROFILE / 修改个人信息"
      :visible.sync="showEditDrawer"
      direction="rtl"
      size="600px"
      custom-class="basic-info-card-retro-drawer"
      :append-to-body="true"
      @closed="resetForm">
      <div class="basic-info-card-edit-drawer">
        <!-- 编辑表单 -->
        <el-form :model="editForm" :rules="rules" ref="editForm" label-width="80px" size="small">
          <!-- 用户名字段（禁用） -->
          <el-form-item label="USERNAME">
            <el-input v-model="editForm.userName" disabled />
          </el-form-item>
          <!-- 邮箱字段 -->
          <el-form-item label="EMAIL" prop="userEmail">
            <el-input v-model="editForm.userEmail" />
          </el-form-item>
          <!-- 电话字段 -->
          <el-form-item label="PHONE" prop="userPhone">
            <el-input v-model="editForm.userPhone" />
          </el-form-item>
          <!-- 性别字段 -->
          <el-form-item label="GENDER" prop="userSex">
            <el-select v-model="editForm.userSex" style="width:100%" popper-class="basic-info-card-retro-dropdown">
              <el-option label="MALE" :value="1" />
              <el-option label="FEMALE" :value="0" />
            </el-select>
          </el-form-item>
          <!-- 备注字段 -->
          <el-form-item label="REMARK" prop="userRemark">
            <el-input type="textarea" v-model="editForm.userRemark" :rows="3" />
          </el-form-item>
        </el-form>
        <!-- 抽屉操作按钮 -->
        <div class="basic-info-card-drawer-actions" style="margin-top: 20px; text-align: right;">
          <el-button size="small" @click="showEditDrawer=false">CANCEL</el-button>
          <el-button size="small" type="primary" @click="submitEdit" :loading="saving">SAVE</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { updateMe } from '@/api/user-api'
import { isValidEmail, isValidPhone, isNonEmpty } from '@/utils/validation'

/**
 * 基本信息卡片组件
 * 
 * 功能：
 * 1. 显示用户的基本信息（邮箱、电话、性别、状态）
 * 2. 支持编辑用户信息（打开编辑抽屉）
 * 3. 支持上传用户头像
 * 4. 表单验证（邮箱、电话、性别）
 * 
 * 数据来源：
 * - 通过 props 接收用户信息对象
 * - 通过 user-api 的 updateMe API 更新用户信息
 * - 通过上传接口更新用户头像
 */
export default {
  name: 'BasicInfoCard',
  props: {
    // 用户信息对象
    user: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    /**
     * 邮箱验证函数
     * @param {Object} rule - 验证规则对象
     * @param {string} value - 待验证的值
     * @param {Function} callback - 验证回调函数
     */
    const validateEmail = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('邮箱不能为空'))
      }
      if (!isValidEmail(value)) {
        return callback(new Error('邮箱格式不正确'))
      }
      callback()
    }
    
    /**
     * 电话验证函数
     * @param {Object} rule - 验证规则对象
     * @param {string} value - 待验证的值
     * @param {Function} callback - 验证回调函数
     */
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('手机号不能为空'))
      }
      if (!isValidPhone(value)) {
        return callback(new Error('手机号格式不正确'))
      }
      callback()
    }
    
    /**
     * 性别验证函数
     * @param {Object} rule - 验证规则对象
     * @param {number} value - 待验证的值（0或1）
     * @param {Function} callback - 验证回调函数
     */
    const validateSex = (rule, value, callback) => {
      if (value !== 0 && value !== 1) {
        return callback(new Error('性别不能为空'))
      }
      callback()
    }

    return {
      // 是否显示编辑抽屉
      showEditDrawer: false,
      // 编辑表单数据
      editForm: {
        userName: '',      // 用户名
        userEmail: '',     // 邮箱
        userPhone: '',     // 电话
        userSex: null,     // 性别（0=女，1=男）
        userRemark: ''     // 备注
      },
      // 表单验证规则
      rules: {
        userEmail: [
          { required: true, validator: validateEmail, trigger: 'blur' }
        ],
        userPhone: [
          { required: true, validator: validatePhone, trigger: 'blur' }
        ],
        userSex: [
          { required: true, validator: validateSex, trigger: 'change' }
        ]
      },
      // 保存状态
      saving: false
    }
  },
  computed: {
    /**
     * 性别文本
     * 将性别代码转换为可读的文本
     * @returns {string} 性别文本（MALE/FEMALE/UNKNOWN）
     */
    genderText() {
      const sex = (this.user.userSex || '').toString().trim().toLowerCase()
      if (sex === '1' || sex === 'm' || sex === 'male') return 'MALE'
      if (sex === '0' || sex === 'f' || sex === 'female') return 'FEMALE'
      return 'UNKNOWN'
    },
    
    /**
     * 上传请求头
     * 包含认证令牌，用于头像上传
     * @returns {Object} 请求头对象
     */
    uploadHeaders() {
      const token = localStorage.getItem('token') || ''
      return token ? { Authorization: `Bearer ${token}`, token } : {}
    }
  },
  methods: {
    /**
     * ========== 编辑相关方法 ==========
     */
    
    /**
     * 打开编辑抽屉
     * 初始化编辑表单数据，并显示编辑抽屉
     */
    goEdit() {
      const u = this.user || {}
      // 规范化性别值：转换为数字（0或1）
      let sex = u.userSex
      if (sex === '1' || sex === 1 || String(sex).toLowerCase() === 'male') sex = 1
      else if (sex === '0' || sex === 0 || String(sex).toLowerCase() === 'female') sex = 0
      else sex = null

      // 初始化编辑表单数据
      this.editForm = {
        userName: u.userName || '',
        userEmail: u.userEmail || '',
        userPhone: u.userPhone || '',
        userSex: sex,
        userRemark: u.userRemark || ''
      }
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
      if (this.$refs.editForm) {
        this.$refs.editForm.resetFields()
      }
    },
    
    /**
     * 提交编辑
     * 验证表单并提交更新用户信息
     */
    async submitEdit() {
      this.$refs.editForm.validate(async (valid) => {
        if (valid) {
          try {
            this.saving = true
            // 构建更新载荷，只包含有值的字段
            const payload = {
              userName: this.editForm.userName || undefined,
              userEmail: this.editForm.userEmail || undefined,
              userPhone: this.editForm.userPhone || undefined,
              userSex: (this.editForm.userSex === 0 || this.editForm.userSex === 1) ? this.editForm.userSex : undefined,
              userRemark: this.editForm.userRemark || undefined
            }
            // 调用API更新用户信息
            await updateMe(payload)
            this.$message && this.$message.success('更新成功')
            this.showEditDrawer = false
            // 触发刷新事件，通知父组件更新数据
            this.$emit('refresh')
          } catch (err) {
            this.$message && this.$message.error(err.message || '更新失败')
          } finally {
            this.saving = false
          }
        } else {
          return false
        }
      })
    },
    
    /**
     * ========== 上传相关方法 ==========
     */
    
    /**
     * 上传前验证
     * 检查文件类型和大小
     * @param {File} file - 待上传的文件对象
     * @returns {boolean} 是否允许上传
     */
    beforeAvatarUpload(file) {
      // 检查是否为图片类型
      const isImage = file.type && file.type.startsWith('image/')
      // 检查文件大小是否小于2MB
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isImage) {
        this.$message && this.$message.error('仅支持图片类型文件')
        return false
      }
      if (!isLt2M) {
        this.$message && this.$message.error('图片大小需在 2MB 以内')
        return false
      }
      return true
    },
    
    /**
     * 上传成功回调
     * 处理头像上传成功的响应
     * @param {Object} response - 服务器响应对象
     */
    onAvatarUploadSuccess(response) {
      try {
        // 检查响应是否成功
        const ok = response && (response.success === true || response.code === 0)
        if (!ok) {
          const msg = (response && response.message) || '上传失败'
          this.$message && this.$message.error(msg)
          return
        }
        this.$message && this.$message.success('头像更新成功')
        // 触发刷新事件，通知父组件更新数据
        this.$emit('refresh')
      } catch (e) {
        // 静默处理错误
      }
    },
    
    /**
     * 上传失败回调
     * 处理头像上传失败的错误
     * @param {Error} err - 错误对象
     */
    onAvatarUploadError(err) {
      const msg = err && (err.message || err.toString())
      this.$message && this.$message.error(msg || '上传失败')
    }
  }
}
</script>

<style>
@import '@/assets/css/user-information/basic-info-card.css';
</style>
