"""
任务边实体，对应表 `task_edge`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TaskEdge(BaseEntity):
    """任务边实体"""
    task_edge_id: Optional[str] = None  # 任务边id（UUID）
    source_node_id: Optional[str] = None  # 源节点id（UUID）
    target_node_id: Optional[str] = None  # 目标节点id（UUID）
    edge_type: Optional[str] = None  # 边类型
    edge_description: Optional[str] = None  # 边描述
    
    class Config:
        from_attributes = True
