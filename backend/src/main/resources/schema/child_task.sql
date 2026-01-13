CREATE TABLE `child_task` (
  `child_task_id` varchar(128) NOT NULL COMMENT '子任务id（UUID）',
  `task_id` varchar(128) NOT NULL COMMENT '父任务id（UUID）',
  `child_task_name` varchar(255) NOT NULL COMMENT '子任务标题，要求2位以上（标题即为描述）',
  `child_task_status` tinyint(1) NOT NULL COMMENT '子任务状态，0-已禁用，1-未开始，2-进行中，3-已完成，4-已取消',
  `child_task_index` int(11) NOT NULL COMMENT '子任务索引，用于用户界面排序',
  `child_task_assignee_id` varchar(128) DEFAULT NULL COMMENT '子任务指派成员用户id',
  `due_time` datetime NOT NULL COMMENT '截止时间',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`child_task_id`)
)COMMENT='子任务表';
