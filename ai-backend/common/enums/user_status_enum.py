"""
用户状态枚举
"""
from enum import IntEnum


class UserStatusEnum(IntEnum):
    """用户状态枚举"""
    DISABLED = 0  # 禁用
    NORMAL = 1  # 正常
    INVITING = 2  # 邀请中
    REJECTED = 3  # 已拒绝
