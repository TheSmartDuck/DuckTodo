"""
项目团队与成员关系实体，对应表 `team_user_relation`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TeamUserRelation(BaseEntity):
    """项目团队与成员关系实体"""
    team_user_relation_id: Optional[str] = None  # 项目团队与成员关系id（UUID）
    team_id: Optional[str] = None  # 项目团队id（UUID）
    user_id: Optional[str] = None  # 用户id（UUID）
    user_role: Optional[int] = None  # 用户角色，0-owner（创建者），1-manager（管理者），2-member（普通成员）
    user_status: Optional[int] = None  # 用户状态，0-禁用，1-正常，2-邀请中，3-已拒绝
    team_index: Optional[int] = None  # 团队索引，用于用户界面排序
    team_color: Optional[str] = None  # 团队颜色，用于用户界面显示
    join_time: Optional[datetime] = None  # 加入时间
    
    class Config:
        from_attributes = True
