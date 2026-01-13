CREATE TABLE `user` (
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `user_name` varchar(255) NOT NULL COMMENT '用户名，要求2位以上，不可重复',
  `user_email` varchar(255) NOT NULL COMMENT '用户邮箱，要求邮箱格式，不可重复',
  `user_phone` varchar(255) NOT NULL COMMENT '用户手机，要求11位，不可重复',
  `user_sex` tinyint(1) NOT NULL COMMENT '用户性别，0-女，1-男',
  `user_avatar` varchar(1024) NOT NULL COMMENT '用户头像，存储路径',
  `user_remark` varchar(1024) DEFAULT NULL COMMENT '用户备注',
  `last_login_time` datetime DEFAULT NULL COMMENT '上次登录时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
)COMMENT='用户表';
