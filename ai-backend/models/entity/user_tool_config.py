"""
用户工具配置实体类，对应表 `user_tool_config`
"""
from typing import Optional, Dict, Any
from datetime import datetime
from models.entity.base import BaseEntity


class UserToolConfig(BaseEntity):
    """用户工具配置实体类"""
    id: Optional[str] = None  # 用户工具配置id（UUID）
    user_id: Optional[str] = None  # 用户id（UUID）
    tool_name: Optional[str] = None  # 工具名称
    config_json: Optional[Dict[str, Any]] = None  # 工具配置JSON数据
    
    class Config:
        from_attributes = True
