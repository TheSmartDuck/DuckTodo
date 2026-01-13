CREATE TABLE `user_security` (
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `user_password` varchar(255) NOT NULL COMMENT '密码（Argon2(原始密码+密码加盐)，原始密码需8位以上且包含英文）',
  `user_password_salt` varchar(128) NOT NULL COMMENT '密码加盐（四位数字+英文小写）',
  `user_accesskey` varchar(255) DEFAULT NULL COMMENT '用户AK',
  `user_secretkey` varchar(255) DEFAULT NULL COMMENT '用户SK',
  `sso_source` varchar(64) DEFAULT NULL COMMENT '单点登录来源（如：google, github, wechat等）',
  `sso_uid` varchar(255) DEFAULT NULL COMMENT '单点登录唯一标识',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
)COMMENT='用户安全信息表';
