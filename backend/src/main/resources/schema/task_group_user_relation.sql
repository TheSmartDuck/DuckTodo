CREATE TABLE `task_group_user_relation` (
  `task_group_user_relation_id` varchar(128) NOT NULL COMMENT '任务族与用户关系id（UUID）',
  `task_group_id` varchar(128) NOT NULL COMMENT '任务族id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `user_role` tinyint(1) NOT NULL COMMENT '用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）',
  `user_status` tinyint(1) NOT NULL COMMENT '用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝',
  `group_index` int(11) NOT NULL COMMENT '任务族索引，用于用户界面排序',
  `group_color` varchar(64) NOT NULL COMMENT '任务族颜色，用于用户界面显示',
  `group_alias` varchar(255) DEFAULT NULL COMMENT '任务族别名（用户自定义显示）',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `join_time` datetime NOT NULL COMMENT '加入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_group_user_relation_id`)
)COMMENT='任务族与用户关系表';
