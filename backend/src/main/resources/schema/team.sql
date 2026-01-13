CREATE TABLE `team` (
  `team_id` varchar(128) NOT NULL COMMENT '团队id（UUID）',
  `team_name` varchar(255) NOT NULL COMMENT '团队名称，要求2位以上，不可重复',
  `team_description` varchar(1024) DEFAULT NULL COMMENT '团队描述',
  `team_avatar` varchar(1024) DEFAULT NULL COMMENT '团队头像，存储路径',
  `team_status` tinyint(1) NOT NULL COMMENT '团队状态，0-已禁用，1-进行中，2-已结束',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`team_id`)
)COMMENT='项目团队表';
