"""
任务审计实体，对应表 `task_audit`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TaskAudit(BaseEntity):
    """任务审计实体"""
    audit_id: Optional[str] = None  # 审计日志id（UUID）
    task_id: Optional[str] = None  # 关联的主任务id（UUID）
    operator_id: Optional[str] = None  # 操作人id（UUID）
    action_type: Optional[str] = None  # 操作类型（CREATE, UPDATE, DELETE, COMPLETE...）
    action_description: Optional[str] = None  # 操作详情
    
    class Config:
        from_attributes = True
