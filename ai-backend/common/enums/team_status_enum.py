"""
团队状态枚举
"""
from enum import IntEnum


class TeamStatusEnum(IntEnum):
    """团队状态枚举"""
    DISABLED = 0  # 已禁用
    IN_PROGRESS = 1  # 进行中
    FINISHED = 2  # 已结束
