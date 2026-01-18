"""
任务审计 Database
提供 TaskAudit 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task_audit import TaskAudit


class TaskAuditDatabase(EnhancedBaseDatabase):
    """任务审计 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task_audit',
            primary_key='audit_id'
        )
    
    def find_by_task_id(self, task_id: str) -> List[dict]:
        """根据任务ID查询审计记录"""
        return self.find_by_condition({'task_id': task_id})
    
    def find_by_operator_id(self, operator_id: str) -> List[dict]:
        """根据操作人ID查询审计记录"""
        return self.find_by_condition({'operator_id': operator_id})
    
    def find_by_action_type(self, action_type: str) -> List[dict]:
        """根据操作类型查询审计记录"""
        return self.find_by_condition({'action_type': action_type})
    
    def create(self, task_audit: TaskAudit) -> int:
        """创建审计记录"""
        data = task_audit.dict(exclude_none=True)
        return self.insert(data)
    
    def get_by_id(self, audit_id: str) -> Optional[TaskAudit]:
        """根据ID获取审计记录"""
        result = self.find_by_id(audit_id)
        if result:
            return TaskAudit(**result)
        return None


# 全局实例
task_audit_database = TaskAuditDatabase()
