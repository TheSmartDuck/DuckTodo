CREATE TABLE `task_node` (
    `task_node_id` varchar(128) NOT NULL COMMENT '任务节点id（UUID）',
    `team_id` varchar(128) DEFAULT NULL COMMENT '所属团队id（UUID）',
    `task_group_id` varchar(128) DEFAULT NULL COMMENT '所属任务组id（UUID）',
    `task_id` varchar(128) DEFAULT NULL COMMENT '所属任务id（UUID）',
    `child_task_id` varchar(128) DEFAULT NULL COMMENT '所属子任务id（UUID）',
    `node_name` varchar(255) DEFAULT NULL COMMENT '节点名称',
    `node_type` varchar(255) DEFAULT NULL COMMENT '节点类型 （包含：任务节点，子任务节点，笔记节点，外链网页节点，外链文件节点，数据节点，密码凭证节点...）',
    `node_description` varchar(1024) DEFAULT NULL COMMENT '节点描述',
    `extra_data` TEXT DEFAULT NULL COMMENT '节点数据',
    `node_status` tinyint(1) DEFAULT NULL COMMENT '节点状态，0-禁用，1-正常',
    `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`task_node_id`)
) COMMENT='任务节点表';
