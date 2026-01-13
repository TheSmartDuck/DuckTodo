<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<style>
/* 全局重置：统一清除内外边框与内边距 */
:root, html, body, #app {
  padding: 0px;
  border: 0px;
  margin: 0px;
}
*, *::before, *::after {
  padding: 0px;
  border: 0px;
  box-sizing: border-box;
}
#app {
  font-family: 'SimHei', 'Heiti SC', 'Heiti TC', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

/* 全局：将刷新图标替换为鸭子动图（统一样式） */
.el-icon-refresh {
  display: inline-block;
  width: 16px;
  height: 16px;
  line-height: 16px;
  vertical-align: middle;
  background: url(~@/assets/imgs/loading-duck.gif) no-repeat center;
  background-size: contain;
  cursor: pointer;
  font-size: 0 !important; /* 隐藏原图标字形，避免叠加 */
  color: transparent !important; /* 防止继承颜色影响可见性 */
}

/* 全局 Element UI Message 样式覆写 - 磁带未来主义风格 */
.el-message {
  border-radius: 0 !important;
  border: 3px solid #000 !important;
  box-shadow: 6px 6px 0 #000 !important;
  padding: 15px 20px !important;
  min-width: 300px !important;
  background-color: #fcfcfc !important; /* 纯净背景，高对比度 */
  top: 40px !important; /* 调整顶部距离 */
}

/* 针对不同类型的背景微调（可选，这里统一用白色背景强调复古感，通过图标和文字区分） */
.el-message--success,
.el-message--warning,
.el-message--error,
.el-message--info {
  background-color: #fcfcfc !important;
  border-color: #000 !important;
}

/* 消息内容文本 */
.el-message__content {
  font-family: 'SimHei', 'Heiti SC', 'Heiti TC', sans-serif !important;
  font-weight: bold !important;
  font-size: 16px !important;
  color: #000 !important; /* 黑色文字，类似打印效果 */
}

/* 关闭按钮 */
.el-message__closeBtn {
  color: #000 !important;
  font-weight: 900 !important;
  font-size: 16px !important;
}
.el-message__closeBtn:hover {
  color: #ff6b00 !important; /* 悬停橙色 */
}

/* 调整图标大小和颜色，使其更清晰 */
.el-message .el-icon-success { color: #67C23A !important; font-size: 18px !important; }
.el-message .el-icon-warning { color: #E6A23C !important; font-size: 18px !important; }
.el-message .el-icon-error { color: #F56C6C !important; font-size: 18px !important; }
.el-message .el-icon-info { color: #909399 !important; font-size: 18px !important; }
</style>
