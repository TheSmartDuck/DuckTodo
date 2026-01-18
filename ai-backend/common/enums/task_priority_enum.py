"""
任务优先级枚举
"""
from enum import IntEnum


class TaskPriorityEnum(IntEnum):
    """任务优先级枚举"""
    P0 = 0  # P0|紧急优先级
    P1 = 1  # P1|高优先级
    P2 = 2  # P2|中优先级
    P3 = 3  # P3|低优先级
    P4 = 4  # P4|最低优先级
