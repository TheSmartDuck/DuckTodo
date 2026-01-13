CREATE TABLE `task_file` (
  `task_file_id` varchar(128) NOT NULL COMMENT '任务附件id（UUID）',
  `task_id` varchar(128) NOT NULL COMMENT '所属任务id（UUID）',
  `uploader_user_id` varchar(128) NOT NULL COMMENT '上传者用户id（UUID）',
  `task_file_name` varchar(255) NOT NULL COMMENT '附件名称',
  `task_file_path` varchar(1024) NOT NULL COMMENT '附件存储路径（URL或本地路径）',
  `task_file_type` varchar(255) DEFAULT NULL COMMENT '附件类型（MIME类型或分类）',
  `task_file_size` bigint NOT NULL COMMENT '附件大小（字节）',
  `task_file_status` tinyint(1) NOT NULL COMMENT '附件状态，0-禁用，1-正常',
  `task_file_remark` varchar(1024) DEFAULT NULL COMMENT '附件备注',
  `upload_time` datetime NOT NULL COMMENT '上传时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_file_id`)
)COMMENT='任务附件表';
