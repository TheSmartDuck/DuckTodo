<template>
  <div class="login-form-content">
    <el-form :model="form" :rules="rules" ref="form" label-width="0px">
      <el-form-item prop="account">
        <el-input v-model="form.account" placeholder="请输入用户名或邮箱" clearable autocomplete="off" />
      </el-form-item>
      <el-form-item prop="userPassword">
        <el-input v-model="form.userPassword" placeholder="请输入密码" type="password" show-password autocomplete="off" />
      </el-form-item>

      <div class="login-form-actions">
        <el-button class="login-form-btn login-form-register" @click="$emit('switch-register')" round>注  册</el-button>
        <el-button class="login-form-btn login-form-third" type="primary" @click="$emit('third-login')" round>第 三 方 登 入</el-button>
      </div>

      <el-button class="login-form-btn login-form-login" type="success" @click="handleLogin" :loading="loading" :disabled="loading" round>登  入</el-button>
    </el-form>
  </div>
</template>

<script>
import { Message } from 'element-ui'
import baseApi from '@/api/base-api'
import { isValidEmail } from '@/utils/validation'

/**
 * LoginForm 组件
 * 处理用户登录逻辑，包含账号密码输入及校验
 */
export default {
  name: 'LoginForm',
  data() {
    return {
      /** 是否正在加载中 */
      loading: false,
      /** 表单数据对象 */
      form: {
        /** 用户名或邮箱 */
        account: '',
        /** 用户密码 */
        userPassword: ''
      },
      /** 表单验证规则 */
      rules: {
        account: [
          { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
        ],
        userPassword: [
          { required: true, message: '请输入密码', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    /**
     * 处理登录提交
     * 1. 验证表单
     * 2. 根据输入判断是用户名还是邮箱
     * 3. 调用登录接口
     * 4. 处理登录成功/失败逻辑
     */
    handleLogin() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          if (this.loading) return
          
          // 构造参数：根据输入内容判断是 userName 还是 userEmail
          const account = this.form.account
          const params = {
            userPassword: this.form.userPassword
          }
          
          if (isValidEmail(account)) {
            params.userEmail = account
          } else {
            params.userName = account
          }

          try {
            this.loading = true
            const res = await baseApi.login(params)
            const { token, user } = res || {}
            if (token) {
              localStorage.setItem('token', token)
            }
            Message.success(user ? `欢迎 ${user.userName}` : '登录成功')
            this.$emit('success')
          } catch (err) {
            // Error handling is typically done in the API interceptor or global handler
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

<style src="@/assets/css/login/login-form.css" scoped></style>
