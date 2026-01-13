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
      // 配置 CSS 压缩器，避免解析注释中的 // 或 Grid 语法中的 / 时出错
      config.optimization.minimizer('css').tap((args) => {
        args[0].minimizerOptions = {
          preset: [
            'default',
            {
              // 保留注释，避免解析错误（注释中的 // 可能导致压缩器解析失败）
              discardComments: false,
              // 禁用可能导致解析错误的优化
              normalizeWhitespace: false,
              // 禁用某些可能导致 Grid 语法解析错误的优化
              colormin: true,
              convertValues: true,
              discardEmpty: true,
              discardOverridden: true,
              discardUnused: false,
              mergeId: false,
              mergeLonghand: true,
              mergeRules: true,
              minifyFontValues: true,
              minifyGradients: true,
              minifyParams: true,
              minifySelectors: true,
              normalizeCharset: true,
              normalizeDisplayValues: true,
              normalizePositions: true,
              normalizeRepeatStyle: true,
              normalizeString: true,
              normalizeTimingFunctions: true,
              normalizeUnicode: true,
              normalizeUrl: true,
              orderedValues: true,
              reduceIdents: false,
              reduceInitial: true,
              reduceTransforms: true,
              svgo: false,
              uniqueSelectors: true
            }
          ]
        }
        return args
      })
    }
  }
})
