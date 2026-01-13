module.exports = {
  root: true,
  env: {
    node: true
  },
  extends: [
    'plugin:vue/essential',
    '@vue/standard'
  ],
  parserOptions: {
    parser: '@babel/eslint-parser'
  },
  rules: {
    // 取消 ESLint 相关限制：关闭常见阻断规则
    'no-console': 'off',
    'no-debugger': 'off',
    'eol-last': 'off',
    'prefer-promise-reject-errors': 'off'
  }
}
