"""
子任务实体，对应表 `child_task`
"""
from typing import Optional
from datetime import date, datetime
from models.entity.base import BaseEntity


class ChildTask(BaseEntity):
    """子任务实体"""
    child_task_id: Optional[str] = None  # 子任务id（UUID）
    task_id: Optional[str] = None  # 父任务id（UUID）
    child_task_name: Optional[str] = None  # 子任务标题，要求2位以上（标题即为描述）
    child_task_status: Optional[int] = None  # 子任务状态，0-已禁用，1-未开始，2-进行中，3-已完成，4-已取消
    child_task_index: Optional[int] = None  # 子任务索引，用于用户界面排序
    child_task_assignee_id: Optional[str] = None  # 子任务指派成员用户id
    due_time: Optional[date] = None  # 截止时间
    finish_time: Optional[date] = None  # 完成时间
    
    class Config:
        from_attributes = True
