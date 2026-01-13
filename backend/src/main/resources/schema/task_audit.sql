CREATE TABLE `task_audit` (
  `audit_id` varchar(128) NOT NULL COMMENT '审计日志id（UUID）',
  `task_id` varchar(128) NOT NULL COMMENT '关联的主任务id（UUID）',
  `operator_id` varchar(128) NOT NULL COMMENT '操作人id（UUID）',
  `action_type` varchar(64) NOT NULL COMMENT '操作类型（CREATE, UPDATE, DELETE）',
  `action_description` TEXT NOT NULL COMMENT '操作详情，格式为：[时间]：用户名-进行的操作（如：将任务名称由“xxxxx”修改为“xxxxx”）',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '操作时间',
  PRIMARY KEY (`audit_id`)
)COMMENT='任务审计表';
