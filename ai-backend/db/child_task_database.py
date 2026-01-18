"""
子任务 Database
提供 ChildTask 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.child_task import ChildTask


class ChildTaskDatabase(EnhancedBaseDatabase):
    """子任务 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='child_task',
            primary_key='child_task_id'
        )
    
    def find_by_task_id(self, task_id: str) -> List[dict]:
        """根据任务ID查询子任务"""
        return self.find_by_condition({'task_id': task_id})
    
    def find_by_assignee_id(self, assignee_id: str) -> List[dict]:
        """根据指派者ID查询子任务"""
        return self.find_by_condition({'child_task_assignee_id': assignee_id})
    
    def create(self, child_task: ChildTask) -> int:
        """创建子任务"""
        data = child_task.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, child_task_id: str, child_task: ChildTask) -> int:
        """更新子任务"""
        data = child_task.dict(exclude_none=True)
        return self.update_by_id(child_task_id, data)
    
    def get_by_id(self, child_task_id: str) -> Optional[ChildTask]:
        """根据ID获取子任务"""
        result = self.find_by_id(child_task_id)
        if result:
            return ChildTask(**result)
        return None


# 全局实例
child_task_database = ChildTaskDatabase()
