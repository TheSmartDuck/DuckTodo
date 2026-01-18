"""
用户LLM配置实体类，对应表 `user_llm_config`
"""
from typing import Optional
from datetime import datetime
from decimal import Decimal
from models.entity.base import BaseEntity


class UserLlmConfig(BaseEntity):
    """用户LLM配置实体类"""
    user_llm_config_id: Optional[str] = None  # 用户LLM配置id（UUID）
    user_id: Optional[str] = None  # 用户id（UUID）
    llm_provider: Optional[str] = None  # LLM提供商（如：openai, modelscope, ollama等）
    llm_api_key: Optional[str] = None  # LLM API Key
    llm_api_url: Optional[str] = None  # LLM API URL（用于自建或兼容接口）
    llm_model_name: Optional[str] = None  # LLM 模型名称
    llm_model_temperature: Optional[float] = None  # LLM 模型温度（0.0-1.0）
    llm_model_thinking: Optional[int] = None  # 是否支持Thinking，0-不支持，1-支持
    llm_model_type: Optional[int] = 1  # LLM 模型类型，1-chat模型，2-embedding模型，3-rerank模型
    
    class Config:
        from_attributes = True
