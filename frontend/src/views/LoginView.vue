<template>
  <div class="login-page">
    <DriftingStardust />
    
    <transition name="login-fade">
      <LoginSuccessAnimation v-if="showSuccessAnim" @finish="onSuccessAnimFinish" />
    </transition>

    <transition name="login-fade">
      <RetroIntro v-if="showIntro" @finish="onIntroFinish" />
    </transition>
    <transition name="login-slide-up">
      <div class="login-box" :class="{ 'login-register-mode': isRegister }" v-if="!showIntro && !showSuccessAnim">
        <img class="login-avatar" src="@/assets/imgs/login-avatar.png" alt="avatar" />
  
        <div class="login-title">
          <span class="login-title-duck">Duck</span>
          <span class="login-title-todo">Todo</span>
          <span class="login-title-logo-cursor">_</span>
        </div>
  
        <div class="login-form">
            <div class="login-switch-wrapper" :style="switchHeight ? { height: switchHeight + 'px' } : {}" ref="switchWrapper">
              <transition name="login-fade-switch" mode="out-in" @before-enter="tsBeforeEnter" @before-leave="tsBeforeLeave">
                
                <LoginForm 
                  v-if="!isRegister" 
                  key="login" 
                  ref="loginBlock"
                  @switch-register="goRegister"
                  @third-login="thirdLogin"
                  @success="handleLoginSuccess"
                />
  
                <RegisterForm 
                  v-else 
                  key="register" 
                  ref="registerBlock"
                  @switch-login="goBack"
                />

              </transition>
            </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { Message } from 'element-ui'
import RetroIntro from '@/components/login/RetroIntro.vue'
import DriftingStardust from '@/components/login/DriftingStardust.vue'
import LoginForm from '@/components/login/LoginForm.vue'
import RegisterForm from '@/components/login/RegisterForm.vue'
import LoginSuccessAnimation from '@/components/login/LoginSuccessAnimation.vue'

export default {
  name: 'LoginView',
  components: {
    RetroIntro,
    DriftingStardust,
    LoginForm,
    RegisterForm,
    LoginSuccessAnimation
  },
  data () {
    return {
      showIntro: true,
      showSuccessAnim: false,
      isRegister: false,
      switchHeight: 0
    }
  },
  watch: {
    // Removed redundant watcher, handled by transition hooks
  },
  methods: {
    /**
     * 切换到注册模式
     */
    goRegister () {
      this.isRegister = true
    },
    /**
     * 返回登录模式
     */
    goBack () {
      this.isRegister = false
    },
    /**
     * 第三方登录（暂未实现）
     */
    thirdLogin () {
      Message.info('第三方登录敬请期待')
    },
    /**
     * 设置切换容器的高度
     * @param {HTMLElement} el 当前过渡元素
     */
    tsSetHeight (el) {
      if (!el) return
      const h = el.offsetHeight
      this.switchHeight = h
    },
    /**
     * 过渡进入前的钩子，设置高度
     */
    tsBeforeEnter (el) { this.tsSetHeight(el) },
    /**
     * 过渡离开前的钩子，设置高度
     */
    tsBeforeLeave (el) { this.tsSetHeight(el) },
    /**
     * 更新切换容器高度，适应当前组件
     */
    updateHeight () {
      this.$nextTick(() => {
        const cmp = this.isRegister ? this.$refs.registerBlock : this.$refs.loginBlock
        if (cmp && cmp.$el) this.switchHeight = cmp.$el.offsetHeight
      })
    },
    /**
     * 介绍动画结束回调
     */
    onIntroFinish () {
      this.showIntro = false
      this.updateHeight()
    },
    /**
     * 处理登录成功
     */
    handleLoginSuccess () {
      this.showSuccessAnim = true
    },
    /**
     * 登录成功动画结束回调
     */
    onSuccessAnimFinish () {
      this.$router.replace('/')
    }
  },
  mounted () {
    if (!this.showIntro) {
      this.updateHeight()
    }
  }
}
</script>

<style src="@/assets/css/login/login-view.css" scoped></style>
