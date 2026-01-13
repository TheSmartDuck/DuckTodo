CREATE TABLE `team_user_relation` (
  `team_user_relation_id` varchar(128) NOT NULL COMMENT '项目团队与成员关系id（UUID）',
  `team_id` varchar(128) NOT NULL COMMENT '项目团队id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `user_role` tinyint(1) NOT NULL COMMENT '用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）',
  `user_status` tinyint(1) NOT NULL COMMENT '用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝',
  `team_index` int(11) NOT NULL COMMENT '团队索引，用于用户界面排序',
  `team_color` varchar(64) NOT NULL COMMENT '团队颜色，用于用户界面显示',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `join_time` datetime NOT NULL COMMENT '加入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`team_user_relation_id`)
)COMMENT='项目团队与成员关系表';
