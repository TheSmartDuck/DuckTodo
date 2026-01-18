"""
是否拥有者枚举
"""
from enum import IntEnum


class IfOwnerEnum(IntEnum):
    """是否拥有者枚举"""
    IS_NOT_OWNER = 0  # 否
    IS_OWNER = 1  # 是
