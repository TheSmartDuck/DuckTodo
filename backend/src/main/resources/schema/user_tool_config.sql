CREATE TABLE `user_tool_config` (
  `id` varchar(128) NOT NULL COMMENT '用户工具配置id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `tool_name` varchar(512) DEFAULT NULL COMMENT '工具名称',
  `config_json` text COMMENT '工具配置JSON数据',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) COMMENT='用户工具配置表';
