CREATE TABLE `task_user_relation` (
  `task_user_relation_id` varchar(128) NOT NULL COMMENT '任务协助者关系id（UUID）',
  `task_id` varchar(128) NOT NULL COMMENT '任务id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '协助者用户id（UUID）',
  `if_owner` tinyint(1) NOT NULL COMMENT '是否为任务创建者，0-否，1-是',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_user_relation_id`)
)COMMENT='任务与用户关系表';
