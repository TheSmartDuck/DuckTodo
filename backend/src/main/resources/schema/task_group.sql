CREATE TABLE `task_group` (
  `task_group_id` varchar(128) NOT NULL COMMENT '任务族id（UUID）',
  `team_id` varchar(128) NOT NULL COMMENT '所属项目团队id（UUID）',
  `group_name` varchar(255) NOT NULL COMMENT '任务族名称，要求2位以上',
  `group_description` varchar(1024) DEFAULT NULL COMMENT '任务族描述',
  `group_status` tinyint(1) NOT NULL COMMENT '任务族状态，0-禁用，1-正常',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_group_id`)
)COMMENT='任务族表';
