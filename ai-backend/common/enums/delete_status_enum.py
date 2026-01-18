"""
删除状态枚举
"""
from enum import IntEnum


class DeleteStatusEnum(IntEnum):
    """删除状态枚举"""
    NOT_DELETED = 0  # 未删除
    DELETED = 1  # 已删除
