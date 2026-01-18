"""
任务节点实体，对应表 `task_node`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TaskNode(BaseEntity):
    """任务节点实体"""
    task_node_id: Optional[str] = None  # 任务节点id（UUID）
    team_id: Optional[str] = None  # 所属团队id（UUID）
    task_group_id: Optional[str] = None  # 所属任务组id（UUID）
    task_id: Optional[str] = None  # 所属任务id（UUID）
    child_task_id: Optional[str] = None  # 所属子任务id（UUID）
    node_name: Optional[str] = None  # 节点名称
    node_type: Optional[str] = None  # 节点类型
    node_description: Optional[str] = None  # 节点描述
    extra_data: Optional[str] = None  # 节点数据
    node_status: Optional[int] = None  # 节点状态，0-禁用，1-正常
    
    class Config:
        from_attributes = True
