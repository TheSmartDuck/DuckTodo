"""
任务族实体，对应表 `task_group`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TaskGroup(BaseEntity):
    """任务族实体"""
    task_group_id: Optional[str] = None  # 任务族id（UUID）
    team_id: Optional[str] = None  # 所属项目团队id（UUID）
    group_name: Optional[str] = None  # 任务族名称，要求2位以上
    group_description: Optional[str] = None  # 任务族描述
    group_status: Optional[int] = None  # 任务族状态，0-禁用，1-正常
    
    class Config:
        from_attributes = True
