"""
任务实体，对应表 `task`
"""
from typing import Optional
from datetime import date, datetime
from models.entity.base import BaseEntity


class Task(BaseEntity):
    """任务实体"""
    task_id: Optional[str] = None  # 任务id（UUID）
    task_group_id: Optional[str] = None  # 所属任务族id（UUID）
    team_id: Optional[str] = None  # 所属项目团队id（UUID）
    task_name: Optional[str] = None  # 任务标题，要求2位以上
    task_description: Optional[str] = None  # 任务描述
    task_status: Optional[int] = None  # 任务状态，0为已禁用，1为未开始，2为进行中，3为已完成，5为已取消
    task_priority: Optional[int] = None  # 任务优先级，0为无，1为P3|低优先级，2为P2|中优先级，3为P1|高优先级，4为P0|紧急优先级
    start_time: Optional[date] = None  # 开始时间
    due_time: Optional[date] = None  # 截止时间
    finish_time: Optional[date] = None  # 完成时间
    
    class Config:
        from_attributes = True
