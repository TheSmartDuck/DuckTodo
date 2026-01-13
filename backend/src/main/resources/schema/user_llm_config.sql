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
