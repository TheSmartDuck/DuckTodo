"""
任务审计操作类型枚举
"""
from enum import Enum


class TaskAuditActionEnum(str, Enum):
    """任务审计操作类型枚举"""
    CREATE = "CREATE"  # 创建
    UPDATE = "UPDATE"  # 更新
    DELETE = "DELETE"  # 删除
    COMPLETE = "COMPLETE"  # 完成
    CANCEL = "CANCEL"  # 取消
    ARCHIVE = "ARCHIVE"  # 归档
    RESTORE = "RESTORE"  # 恢复
