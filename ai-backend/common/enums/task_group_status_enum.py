"""
任务族状态枚举
"""
from enum import IntEnum


class TaskGroupStatusEnum(IntEnum):
    """任务族状态枚举"""
    DISABLED = 0  # 禁用
    NORMAL = 1  # 正常
