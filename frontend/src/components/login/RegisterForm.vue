<template>
  <div class="register-form-content">
    <el-form :model="form" :rules="rules" ref="form" label-width="0px">
      <el-form-item prop="userName">
        <el-input v-model="form.userName" placeholder="请输入用户名" clearable autocomplete="off" />
      </el-form-item>
      <el-form-item prop="userEmail">
        <el-input v-model="form.userEmail" placeholder="请输入邮箱" clearable autocomplete="off" />
      </el-form-item>
      <el-form-item prop="userPassword">
        <el-input v-model="form.userPassword" placeholder="请输入密码" type="password" show-password autocomplete="off" />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input v-model="form.confirmPassword" placeholder="请再次输入密码" type="password" show-password autocomplete="off" />
      </el-form-item>
      <el-form-item prop="userPhone">
        <el-input v-model="form.userPhone" placeholder="请输入手机号" clearable autocomplete="off" />
      </el-form-item>
      <el-form-item prop="userSex">
        <el-select v-model="form.userSex" placeholder="选择性别" clearable>
          <el-option
            v-for="item in userSexOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <div class="register-form-actions">
        <el-button class="register-form-btn register-form-register" type="primary" @click="handleRegister" :loading="loading" :disabled="loading" round>注  册</el-button>
        <el-button class="register-form-btn register-form-back" @click="$emit('switch-login')" round>返  回</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { Message } from 'element-ui'
import baseApi from '@/api/base-api'
import { isValidPhone, isValidPassword, inMapKeys } from '@/utils/validation'
import { userSexByCode } from '@/utils/enums'

/**
 * RegisterForm 组件
 * 处理用户注册逻辑，包含信息录入及校验
 */
export default {
  name: 'RegisterForm',
  data() {
    return {
      /** 是否正在加载中 */
      loading: false,
      /** 性别选项列表 */
      userSexOptions: Object.entries(userSexByCode).map(([key, value]) => ({ value: Number(key), label: value })),
      /** 表单数据对象 */
      form: {
        /** 用户名 */
        userName: '',
        /** 用户邮箱 */
        userEmail: '',
        /** 用户密码 */
        userPassword: '',
        /** 确认密码 */
        confirmPassword: '',
        /** 用户手机号 */
        userPhone: '',
        /** 用户性别 */
        userSex: ''
      },
      /** 表单验证规则 */
      rules: {
        userName: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        userEmail: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: ['blur', 'change'] }
        ],
        userPassword: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { validator: (rule, value, callback) => {
              if (!isValidPassword(value)) {
                callback(new Error('密码需8位以上且包含英文'))
              } else {
                callback()
              }
            }, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: (rule, value, callback) => {
              if (value !== this.form.userPassword) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            }, trigger: ['blur', 'change'] }
        ],
        userPhone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { validator: (rule, value, callback) => {
              if (!isValidPhone(value)) {
                callback(new Error('手机号格式不正确'))
              } else {
                callback()
              }
            }, trigger: ['blur', 'change'] }
        ],
        userSex: [
          { required: true, message: '请选择性别', trigger: 'change' },
          { type: 'number', message: '性别必须为数值', trigger: 'change' },
          { validator: (rule, value, callback) => {
              if (!inMapKeys(userSexByCode, value)) {
                callback(new Error('性别参数无效'))
              } else {
                callback()
              }
            }, trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    /**
     * 处理注册提交
     * 1. 验证表单
     * 2. 调用注册接口
     * 3. 处理注册成功逻辑（自动登录或跳转）
     */
    handleRegister() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          const params = {
            userName: this.form.userName,
            userEmail: this.form.userEmail,
            userPassword: this.form.userPassword,
            userPhone: this.form.userPhone,
            userSex: this.form.userSex
          }
          
          if (this.loading) return
          try {
            this.loading = true
            const res = await baseApi.register(params)
            const { token, user } = res || {}
            if (token) {
              localStorage.setItem('token', token)
            }
            Message.success(user ? `注册成功，欢迎 ${user.userName}` : '注册成功，已自动登录')
            this.$router.replace('/')
          } catch (err) {
            console.error(err)
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style src="@/assets/css/login/register-form.css"></style>
