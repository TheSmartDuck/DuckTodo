<template>
  <!-- 用户信息视图根容器 -->
  <div class="user-information-page">
    <div class="user-information-content-wrapper">
      <!-- 顶部区域：身份卡片 -->
      <identity-card :user="user" :has-ak="!!keys.userAccesskey" />
      
      <!-- 分隔线：预信息标题 -->
      <div class="user-information-archive-header">
        <span class="user-information-square">■</span>
        <span class="user-information-title">PRE-INFORMATION</span>
        <span class="user-information-line-deco"></span>
      </div>
      
      <!-- 底部卡片网格 -->
      <div class="user-information-archive-grid">
         <!-- 卡片1：联系方式和基本信息 -->
         <basic-info-card :user="user" @refresh="load" />
         
         <!-- 卡片2：安全和密钥 -->
         <security-card :user="user" :keys="keys" @refresh-keys="loadKeys" />
         
         <!-- 卡片3：LLM配置 -->
         <user-l-lm-config-card />
      </div>
    </div>
  </div>
</template>

<script>
import IdentityCard from '@/components/user-information/IdentityCard'
import BasicInfoCard from '@/components/user-information/BasicInfoCard'
import SecurityCard from '@/components/user-information/SecurityCard'
import UserLLmConfigCard from '@/components/user-information/UserLLmConfigCard'
import { getMe, getAccessKeys } from '@/api/user-api'

/**
 * 用户信息视图组件
 * 
 * 功能：
 * 1. 显示当前用户的身份信息卡片
 * 2. 显示用户的基本信息（联系方式等）
 * 3. 显示用户的安全设置和访问密钥
 * 4. 支持刷新用户信息和密钥数据
 * 
 * 数据来源：
 * - 通过 user-api 的 getMe API 获取当前用户信息
 * - 通过 user-api 的 getAccessKeys API 获取访问密钥
 * 
 * 组件结构：
 * - IdentityCard: 身份卡片组件
 * - BasicInfoCard: 基本信息卡片组件
 * - SecurityCard: 安全卡片组件
 */
export default {
  name: 'UserInformationView',
  components: {
    // 身份卡片组件
    IdentityCard,
    // 基本信息卡片组件
    BasicInfoCard,
    // 安全卡片组件
    SecurityCard,
    // LLM配置卡片组件
    UserLLmConfigCard
  },
  data() {
    return {
      // 当前用户信息对象
      user: {},
      // 访问密钥对象
      keys: {
        userAccesskey: '',    // 用户访问密钥
        userSecretkey: ''     // 用户秘密密钥
      },
      // 加载状态
      loading: false
    }
  },
  /**
   * 路由进入前的守卫
   * 在进入路由时自动加载用户信息
   */
  beforeRouteEnter(to, from, next) {
    next(vm => vm.load())
  },
  methods: {
    /**
     * ========== 数据加载方法 ==========
     */
    
    /**
     * 加载用户信息
     * 从API获取当前用户信息，并同时加载访问密钥
     */
    async load() {
      try {
        this.loading = true
        // 调用API获取当前用户信息
        const res = await getMe()
        this.user = res || {}
        // 加载访问密钥
        await this.loadKeys()
      } catch (e) {
        // 加载失败时重置用户信息为空对象
        this.user = {}
        console.error('Failed to load user information', e)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 加载访问密钥
     * 从API获取用户的访问密钥和秘密密钥
     */
    async loadKeys() {
      try {
        // 调用API获取访问密钥
        const data = await getAccessKeys()
        this.keys = {
          // 用户访问密钥
          userAccesskey: (data && data.userAccesskey) || '',
          // 用户秘密密钥
          userSecretkey: (data && data.userSecretkey) || ''
        }
      } catch (e) {
        // 加载失败时重置密钥为空字符串
        this.keys = { 
          userAccesskey: '', 
          userSecretkey: '' 
        }
        console.error('Failed to load access keys', e)
      }
    }
  }
}
</script>

<style scoped>
@import '@/assets/css/user-information/user-information.css';
</style>
