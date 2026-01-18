"""
项目团队实体，对应表 `team`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class Team(BaseEntity):
    """项目团队实体"""
    team_id: Optional[str] = None  # 团队id（UUID）
    team_name: Optional[str] = None  # 团队名称，要求2位以上，不可重复
    team_description: Optional[str] = None  # 团队描述
    team_avatar: Optional[str] = None  # 团队头像，存储路径
    team_status: Optional[int] = None  # 团队状态，0-已禁用，1-进行中，2-已结束
    
    class Config:
        from_attributes = True
