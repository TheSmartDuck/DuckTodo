CREATE TABLE `user_dingtalk_robot` (
  `user_dingtalk_robot_id` varchar(128) NOT NULL COMMENT '用户钉钉机器人配置id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `robot_name` varchar(255) NOT NULL COMMENT '机器人名称',
  `dingtalk_robot_token` varchar(255) DEFAULT NULL COMMENT '钉钉机器人Token',
  `dingtalk_robot_secret` varchar(255) DEFAULT NULL COMMENT '钉钉机器人Secret',
  `dingtalk_robot_keyword` varchar(255) DEFAULT NULL COMMENT '钉钉机器人关键字',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_dingtalk_robot_id`)
)COMMENT='用户钉钉机器人配置表';
