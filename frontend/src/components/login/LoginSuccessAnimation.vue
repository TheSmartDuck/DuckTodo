<template>
  <div class="login-success-container">
    <div class="spinner"></div>
    
    <div class="success-content">
      <!-- Icon -->
      <div class="success-icon-wrapper">
        <div class="success-icon-circle"></div>
        <div class="success-icon-check">✓</div>
      </div>

      <!-- Text Group -->
      <div class="success-text-wrapper">
        <div class="success-line left"></div>
        <div class="success-square left"></div>
        
        <div class="success-text">
          <span class="accent-block"></span>
          LOGIN WAS SUCCESSFUL
          <span class="accent-block"></span>
        </div>
        
        <div class="success-line right"></div>
        <div class="success-square right"></div>
        
        <!-- Info Boxes -->
        <div class="info-box top-left">
          RHINE LAB<br>
          ACCESS GRANTED
        </div>
        
        <div class="info-box bottom-right">
          ID: 9527<br>
          AUTH: ADMIN
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginSuccessAnimation',
  mounted() {
    // Play animation for 3 seconds then finish
    setTimeout(() => {
      this.$emit('finish')
    }, 1500)
  }
}
</script>

<style src="@/assets/css/login/login-success.css" scoped></style>
