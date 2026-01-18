"""
用户角色枚举
"""
from enum import IntEnum


class UserRoleEnum(IntEnum):
    """用户角色枚举"""
    OWNER = 0  # 创建者
    MANAGER = 1  # 管理者
    MEMBER = 2  # 普通成员
