"""
任务节点状态枚举
"""
from enum import IntEnum


class TaskNodeStatusEnum(IntEnum):
    """任务节点状态枚举"""
    DISABLED = 0  # 禁用
    NORMAL = 1  # 正常
