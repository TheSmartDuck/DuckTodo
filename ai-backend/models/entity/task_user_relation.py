"""
任务与用户关系实体，对应表 `task_user_relation`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TaskUserRelation(BaseEntity):
    """任务与用户关系实体"""
    task_user_relation_id: Optional[str] = None  # 任务协助者关系id（UUID）
    task_id: Optional[str] = None  # 任务id（UUID）
    user_id: Optional[str] = None  # 协助者用户id（UUID）
    if_owner: Optional[int] = None  # 是否为任务创建者，0-否，1-是
    
    class Config:
        from_attributes = True
