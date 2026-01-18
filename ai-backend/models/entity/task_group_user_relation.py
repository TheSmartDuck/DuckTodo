"""
任务族与用户关系实体，对应表 `task_group_user_relation`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TaskGroupUserRelation(BaseEntity):
    """任务族与用户关系实体"""
    task_group_user_relation_id: Optional[str] = None  # 任务族与用户关系id（UUID）
    task_group_id: Optional[str] = None  # 任务族id（UUID）
    user_id: Optional[str] = None  # 用户id（UUID）
    user_role: Optional[int] = None  # 用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）
    user_status: Optional[int] = None  # 用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝
    group_index: Optional[int] = None  # 任务族索引，用于用户界面排序
    group_color: Optional[str] = None  # 任务族颜色，用于用户界面显示
    group_alias: Optional[str] = None  # 任务族别名（用户自定义显示）
    join_time: Optional[datetime] = None  # 加入时间
    
    class Config:
        from_attributes = True
