const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  publicPath: '/',
  runtimeCompiler: false,
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  css: {
    extract: process.env.NODE_ENV === 'production' ? {
      ignoreOrder: true
    } : false
  },
  chainWebpack: (config) => {
    if (process.env.NODE_ENV === 'production') {
      // 配置 CSS 压缩器，避免解析错误
      // 问题：postcss-selector-parser 在解析某些选择器时遇到 / 字符会报错
      config.optimization.minimizer('css').tap((args) => {
        // 使用更安全的配置，禁用可能导致解析错误的优化
        args[0].minimizerOptions = {
          preset: [
            'default',
            {
              // 保留注释，避免解析错误
              discardComments: false,
              // 禁用选择器压缩，避免 postcss-selector-parser 解析错误
              minifySelectors: false,
              // 禁用其他可能导致问题的优化
              normalizeWhitespace: false,
              normalizePositions: false,
              normalizeRepeatStyle: false
            }
          ]
        }
        return args
      })
    }
  }
})
