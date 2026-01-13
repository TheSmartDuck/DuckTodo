CREATE TABLE `task` (
  `task_id` varchar(128) NOT NULL COMMENT '任务id（UUID）',
  `task_group_id` varchar(128) DEFAULT NULL COMMENT '所属任务族id（UUID）',
  `team_id` varchar(128) NOT NULL COMMENT '所属项目团队id（UUID）',
  `task_name` varchar(255) NOT NULL COMMENT '任务标题，要求2位以上',
  `task_description` TEXT DEFAULT NULL COMMENT '任务描述',
  `task_status` tinyint(1) NOT NULL COMMENT '任务状态，0为已禁用，1为未开始，2为进行中，3为已完成，5为已取消',
  `task_priority` tinyint(1) NOT NULL COMMENT '任务优先级，0为无，1为P3|低优先级，2为P2|中优先级，3为P1|高优先级，4为P0|紧急优先级',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `due_time` datetime NOT NULL COMMENT '截止时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_id`)
)COMMENT='任务表';
