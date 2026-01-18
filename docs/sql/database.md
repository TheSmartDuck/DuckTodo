# user (用户表)

Todo软件用户表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| user_id | varchar(128) | YES | NULL | 用户id（UUID） |
| user_name | varchar(255) | YES | NULL | 用户名，要求2位以上，不可重复 |
| user_email | varchar(255) | YES | NULL | 用户邮箱，要求邮箱格式，不可重复 |
| user_phone | varchar(255) | YES | NULL | 用户手机，要求11位，不可重复 |
| user_sex | tinyint(1) | YES | NULL | 用户性别，0-女，1-男 |
| user_avatar | varchar(1024) | YES | NULL | 用户头像，存储路径 |
| user_remark | varchar(1024) | NO | NULL | 用户备注 |
| last_login_time | datetime | NO | NULL | 上次登录时间 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
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
```

# user_security (用户安全信息表)

用户安全信息表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| user_id | varchar(128) | YES | NULL | 用户id（UUID） |
| user_password | varchar(255) | YES | NULL | 密码（Argon2(原始密码+密码加盐)，原始密码需8位以上且包含英文） |
| user_password_salt | varchar(128) | YES | NULL | 密码加盐（四位数字+英文小写） |
| user_accesskey | varchar(255) | NO | NULL | 用户AK |
| user_secretkey | varchar(255) | NO | NULL | 用户SK |
| sso_source | varchar(64) | NO | NULL | 单点登录来源（如：google, github, wechat等） |
| sso_uid | varchar(255) | NO | NULL | 单点登录唯一标识 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `user_security` (
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `user_password` varchar(255) NOT NULL COMMENT '密码（Argon2(原始密码+密码加盐)，原始密码需8位以上且包含英文）',
  `user_password_salt` varchar(128) NOT NULL COMMENT '密码加盐（四位数字+英文小写）',
  `user_accesskey` varchar(255) DEFAULT NULL COMMENT '用户AK',
  `user_secretkey` varchar(255) DEFAULT NULL COMMENT '用户SK',
  `sso_source` varchar(64) DEFAULT NULL COMMENT '单点登录来源（如：google, github, wechat等）',
  `sso_uid` varchar(255) DEFAULT NULL COMMENT '单点登录唯一标识',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
)COMMENT='用户安全信息表';
```

# user_llm_config (用户LLM配置表)

用户LLM配置表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| user_llm_config_id | varchar(128) | YES | NULL | 用户LLM配置id（UUID） |
| user_id | varchar(128) | YES | NULL | 用户id（UUID） |
| llm_provider | varchar(64) | NO | NULL | LLM提供商（如：openai, modelscope, ollama等） |
| llm_api_key | varchar(255) | NO | NULL | LLM API Key |
| llm_api_url | varchar(1024) | NO | NULL | LLM API URL（用于自建或兼容接口） |
| llm_model_name | varchar(255) | NO | NULL | LLM 模型名称 |
| llm_model_temperature | decimal(2,1) | NO | NULL | LLM 模型温度（0.0-1.0） |
| llm_model_thinking | tinyint(1) | YES | 0 | 是否支持Thinking，0-不支持，1-支持 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `user_llm_config` (
  `user_llm_config_id` varchar(128) NOT NULL COMMENT '用户LLM配置id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `llm_provider` varchar(64) DEFAULT NULL COMMENT 'LLM提供商（如：openai, modelscope, ollama等）',
  `llm_api_key` varchar(255) DEFAULT NULL COMMENT 'LLM API Key',
  `llm_api_url` varchar(1024) DEFAULT NULL COMMENT 'LLM API URL（用于自建或兼容接口）',
  `llm_model_name` varchar(255) DEFAULT NULL COMMENT 'LLM 模型名称',
  `llm_model_temperature` decimal(2,1) DEFAULT NULL COMMENT 'LLM 模型温度（0.0-1.0）',
  `llm_model_thinking` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否支持Thinking，0-不支持，1-支持',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_llm_config_id`)
)COMMENT='用户LLM配置表';
```

# user_dingtalk_robot (用户钉钉机器人配置表)

用户钉钉机器人配置表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| user_dingtalk_robot_id | varchar(128) | YES | NULL | 用户钉钉机器人配置id（UUID） |
| user_id | varchar(128) | YES | NULL | 用户id（UUID） |
| robot_name | varchar(255) | YES | NULL | 机器人名称 |
| dingtalk_robot_token | varchar(255) | NO | NULL | 钉钉机器人Token |
| dingtalk_robot_secret | varchar(255) | NO | NULL | 钉钉机器人Secret |
| dingtalk_robot_keyword | varchar(255) | NO | NULL | 钉钉机器人关键字 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `user_dingtalk_robot` (
  `user_dingtalk_robot_id` varchar(128) NOT NULL COMMENT '用户钉钉机器人配置id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `robot_name` varchar(255) NOT NULL COMMENT '机器人名称',
  `dingtalk_robot_token` varchar(255) DEFAULT NULL COMMENT '钉钉机器人Token',
  `dingtalk_robot_secret` varchar(255) DEFAULT NULL COMMENT '钉钉机器人Secret',
  `dingtalk_robot_keyword` varchar(255) DEFAULT NULL COMMENT '钉钉机器人关键字',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_dingtalk_robot_id`)
)COMMENT='用户钉钉机器人配置表';
```

# user_tool_config (用户工具配置表)

用户工具配置表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| id | varchar(128) | YES | NULL | 用户工具配置id（UUID） |
| user_id | varchar(128) | YES | NULL | 用户id（UUID） |
| tool_name | varchar(512) | NO | NULL | 工具名称 |
| config_json | text | NO | NULL | 工具配置JSON数据 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `user_tool_config` (
  `id` varchar(128) NOT NULL COMMENT '用户工具配置id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `tool_name` varchar(512) DEFAULT NULL COMMENT '工具名称',
  `config_json` text COMMENT '工具配置JSON数据',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) COMMENT='用户工具配置表';
```

# team (项目团队表)

项目团队表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| team_id | varchar(128) | YES | NULL | 团队id（UUID） |
| team_name | varchar(255) | YES | NULL | 团队名称，要求2位以上，不可重复 |
| team_description | varchar(1024) | NO | NULL | 团队描述 |
| team_avatar | varchar(1024) | NO | NULL | 团队头像，存储路径 |
| team_status | tinyint(1) | YES | NULL | 团队状态，0-已禁用，1-进行中，2-已结束 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
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
```

# team_user_relation (项目团队与成员关系表)

项目团队与成员关系表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| team_user_relation_id | varchar(128) | YES | NULL | 项目团队与成员关系id（UUID） |
| team_id | varchar(128) | YES | NULL | 项目团队id（UUID） |
| user_id | varchar(128) | YES | NULL | 用户id（UUID） |
| user_role | tinyint(1) | YES | NULL | 用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员） |
| user_status | tinyint(1) | YES | NULL | 用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝 |
| team_index | int(11) | YES | NULL | 团队索引，用于用户界面排序 |
| team_color | varchar(64) | YES | NULL | 团队颜色，用于用户界面显示 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| join_time | datetime | YES | NULL | 加入时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
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
```

# task_group (任务族表)

任务族表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| task_group_id | varchar(128) | YES | NULL | 任务族id（UUID） |
| team_id | varchar(128) | YES | NULL | 所属项目团队id（UUID） |
| group_name | varchar(255) | YES | NULL | 任务族名称，要求2位以上 |
| group_description | varchar(1024) | NO | NULL | 任务族描述 |
| group_status | tinyint(1) | YES | NULL | 任务族状态，0-禁用，1-正常 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
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
```

# task_group_user_relation (任务族与用户关系表)

任务族与用户关系表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| task_group_user_relation_id | varchar(128) | YES | NULL | 任务族与用户关系id（UUID） |
| task_group_id | varchar(128) | YES | NULL | 任务族id（UUID） |
| user_id | varchar(128) | YES | NULL | 用户id（UUID） |
| user_role | tinyint(1) | YES | NULL | 用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员） |
| user_status | tinyint(1) | YES | NULL | 用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝 |
| group_index | int(11) | YES | NULL | 任务族索引，用于用户界面排序 |
| group_color | varchar(64) | YES | NULL | 任务族颜色，用于用户界面显示 |
| group_alias | varchar(255) | NO | NULL | 任务族别名（用户自定义显示） |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| join_time | datetime | YES | NULL | 加入时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `task_group_user_relation` (
  `task_group_user_relation_id` varchar(128) NOT NULL COMMENT '任务族与用户关系id（UUID）',
  `task_group_id` varchar(128) NOT NULL COMMENT '任务族id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '用户id（UUID）',
  `user_role` tinyint(1) NOT NULL COMMENT '用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）',
  `user_status` tinyint(1) NOT NULL COMMENT '用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝',
  `group_index` int(11) NOT NULL COMMENT '任务族索引，用于用户界面排序',
  `group_color` varchar(64) NOT NULL COMMENT '任务族颜色，用于用户界面显示',
  `group_alias` varchar(255) DEFAULT NULL COMMENT '任务族别名（用户自定义显示）',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `join_time` datetime NOT NULL COMMENT '加入时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_group_user_relation_id`)
)COMMENT='任务族与用户关系表';
```

# task (任务表)

任务表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| task_id | varchar(128) | YES | NULL | 任务id（UUID） |
| task_group_id | varchar(128) | NO | NULL | 所属任务族id（UUID） |
| team_id | varchar(128) | YES | NULL | 所属项目团队id（UUID） |
| task_name | varchar(255) | YES | NULL | 任务标题，要求2位以上 |
| task_description | TEXT | NO | NULL | 任务描述 |
| task_status | tinyint(1) | YES | NULL | 任务状态，0-已禁用，1-未开始，2-进行中，3-已完成，4-已取消 |
| task_priority | tinyint(1) | YES | NULL | 任务优先级，0-P0（紧急优先级），1-P1（高优先级），2-P2（中优先级），3-P3（低优先级），4-P4（最低优先级） |
| start_time | date | YES | NULL | 开始时间 |
| due_time | date | YES | NULL | 截止时间 |
| finish_time | date | NO | NULL | 完成时间 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `task` (
  `task_id` varchar(128) NOT NULL COMMENT '任务id（UUID）',
  `task_group_id` varchar(128) DEFAULT NULL COMMENT '所属任务族id（UUID）',
  `team_id` varchar(128) NOT NULL COMMENT '所属项目团队id（UUID）',
  `task_name` varchar(255) NOT NULL COMMENT '任务标题，要求2位以上',
  `task_description` TEXT DEFAULT NULL COMMENT '任务描述',
  `task_status` tinyint(1) NOT NULL COMMENT '任务状态，0-已禁用，1-未开始，2-进行中，3-已完成，4-已取消',
  `task_priority` tinyint(1) NOT NULL COMMENT '任务优先级，0-P0（紧急优先级），1-P1（高优先级），2-P2（中优先级），3-P3（低优先级），4-P4（最低优先级）',
  `start_time` date NOT NULL COMMENT '开始时间',
  `due_time` date NOT NULL COMMENT '截止时间',
  `finish_time` date DEFAULT NULL COMMENT '完成时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_id`)
)COMMENT='任务表';
```

# task_user_relation (任务与用户关系表)

任务与用户关系表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| task_user_relation_id | varchar(128) | YES | NULL | 任务协助者关系id（UUID） |
| task_id | varchar(128) | YES | NULL | 任务id（UUID） |
| user_id | varchar(128) | YES | NULL | 协助者用户id（UUID） |
| if_owner | tinyint(1) | YES | NULL | 是否为任务创建者，0-否，1-是 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `task_user_relation` (
  `task_user_relation_id` varchar(128) NOT NULL COMMENT '任务协助者关系id（UUID）',
  `task_id` varchar(128) NOT NULL COMMENT '任务id（UUID）',
  `user_id` varchar(128) NOT NULL COMMENT '协助者用户id（UUID）',
  `if_owner` tinyint(1) NOT NULL COMMENT '是否为任务创建者，0-否，1-是',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`task_user_relation_id`)
)COMMENT='任务与用户关系表';
```

# child_task (子任务表)

子任务表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| child_task_id | varchar(128) | YES | NULL | 子任务id（UUID） |
| task_id | varchar(128) | YES | NULL | 父任务id（UUID） |
| child_task_name | varchar(255) | YES | NULL | 子任务标题，要求2位以上（标题即为描述） |
| child_task_status | tinyint(1) | YES | NULL | 子任务状态，0-已禁用，1-未开始，2-进行中，3-已完成，4-已取消 |
| child_task_index | int(11) | YES | NULL | 子任务索引，用于用户界面排序 |
| child_task_assignee_id | varchar(128) | NO | NULL | 子任务指派成员用户id |
| due_time | date | YES | NULL | 截止时间 |
| finish_time | date | NO | NULL | 完成时间 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
CREATE TABLE `child_task` (
  `child_task_id` varchar(128) NOT NULL COMMENT '子任务id（UUID）',
  `task_id` varchar(128) NOT NULL COMMENT '父任务id（UUID）',
  `child_task_name` varchar(255) NOT NULL COMMENT '子任务标题，要求2位以上（标题即为描述）',
  `child_task_status` tinyint(1) NOT NULL COMMENT '子任务状态，0-已禁用，1-未开始，2-进行中，3-已完成，4-已取消',
  `child_task_index` int(11) NOT NULL COMMENT '子任务索引，用于用户界面排序',
  `child_task_assignee_id` varchar(128) DEFAULT NULL COMMENT '子任务指派成员用户id',
  `due_time` date NOT NULL COMMENT '截止时间',
  `finish_time` date DEFAULT NULL COMMENT '完成时间',
  `is_delete` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-未删除 1-已删除',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`child_task_id`)
)COMMENT='子任务表';
```

# task_file (任务附件表)

任务附件表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| task_file_id | varchar(128) | YES | NULL | 任务附件id（UUID） |
| task_id | varchar(128) | YES | NULL | 所属任务id（UUID） |
| uploader_user_id | varchar(128) | YES | NULL | 上传者用户id（UUID） |
| task_file_name | varchar(255) | YES | NULL | 附件名称 |
| task_file_path | varchar(1024) | YES | NULL | 附件存储路径（URL或本地路径） |
| task_file_type | varchar(255) | NO | NULL | 附件类型（MIME类型或分类） |
| task_file_size | bigint | YES | NULL | 附件大小（字节） |
| task_file_status | tinyint(1) | YES | NULL | 附件状态，0-禁用，1-正常 |
| task_file_remark | varchar(1024) | NO | NULL | 附件备注 |
| upload_time | datetime | YES | NULL | 上传时间 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句

```sql
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
```

# task_node (任务节点表)

任务节点表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| task_node_id | varchar(128) | YES | NULL | 任务节点id（UUID） |
| team_id | varchar(128) | NO | NULL | 所属团队id（UUID） |
| task_group_id | varchar(128) | NO | NULL | 所属任务组id（UUID） |
| task_id | varchar(128) | NO | NULL | 所属任务id（UUID） |
| child_task_id | varchar(128) | NO | NULL | 所属子任务id（UUID） |
| node_name | varchar(255) | YES | NULL | 节点名称 |
| node_type | varchar(255) | YES | NULL | 节点类型 （包含：任务节点，子任务节点，笔记节点，外链网页节点，外链文件节点，数据节点，密码凭证节点...） |
| node_description | varchar(1024) | NO | NULL | 节点描述 |
| extra_data | TEXT | NO | NULL | 节点数据 |
| node_status | tinyint(1) | YES | NULL | 节点状态，0-禁用，1-正常 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句
``` sql
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
```

# task_edge (任务边表)

任务边表

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| task_edge_id | varchar(128) | YES | NULL | 任务边id（UUID） |
| source_node_id | varchar(128) | YES | NULL | 源节点id（UUID） |
| target_node_id | varchar(128) | YES | NULL | 目标节点id（UUID） |
| edge_type | varchar(255) | YES | NULL | 边类型 （包含：依赖边，关联边，继承边...） |
| edge_description | varchar(1024) | NO | NULL | 边描述 |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | YES | NULL | 创建时间 |
| update_time | datetime | NO | NULL | 更新时间 |

## DDL语句
``` sql
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
```

# task_audit (任务审计表)

任务审计表，用于记录任务和子任务的变更日志

## 数据字典

| 字段名 | 类型 | 非空 | 默认值 | 描述 |
| :--- | :--- | :--- | :--- | :--- |
| audit_id | varchar(128) | YES | NULL | 审计日志id（UUID） |
| task_id | varchar(128) | NO | NULL | 关联的主任务id（UUID） |
| operator_id | varchar(128) | NO | NULL | 操作人id（UUID） |
| action_type | varchar(64) | NO | NULL | 操作类型（CREATE, UPDATE, DELETE, COMPLETE...） |
| action_description | TEXT | NO | NULL | 操作详情，格式为：[时间]：用户名-进行的操作（如：将任务名称由“xxxxx”修改为“xxxxx”） |
| is_delete | tinyint(1) | YES | 0 | 是否删除 0-未删除 1-已删除 |
| create_time | datetime | NO | NULL | 操作时间 |

## DDL语句

```sql
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
```
