const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  publicPath: '/',
  runtimeCompiler: false,
  transpileDependencies: true,
  lintOnSave: false,
  chainWebpack: config => {
    if (process.env.NODE_ENV === 'production') {
      // 禁用CSS压缩以避免postcss-selector-parser解析错误
      // 问题：CSS压缩工具在处理包含 '/' 的CSS属性值（如 grid-row: 1 / 3）时会出错
      const minimizers = config.optimization.minimizers
      minimizers.delete('css')
    }
  },
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
