CREATE TABLE `task_edge` (
    `task_edge_id` varchar(128) NOT NULL COMMENT '任务边id（UUID）',
    `source_node_id` varchar(128) NOT NULL COMMENT '源节点id（UUID）',
    `target_node_id` varchar(128) NOT NULL COMMENT '目标节点id（UUID）',
    `edge_type` varchar(255) DEFAULT NULL COMMENT '边类型 （包含：依赖边，关联边，继承边...）',
    `edge_description` varchar(1024) DEFAULT NULL COMMENT '边描述',
    `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`task_edge_id`)
) COMMENT='任务边表';
