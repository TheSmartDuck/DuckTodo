"""
任务状态枚举
"""
from enum import IntEnum


class TaskStatusEnum(IntEnum):
    """任务状态枚举"""
    DISABLED = 0  # 已禁用
    NOT_STARTED = 1  # 未开始
    IN_PROGRESS = 2  # 进行中
    COMPLETED = 3  # 已完成
    CANCELED = 4  # 已取消
