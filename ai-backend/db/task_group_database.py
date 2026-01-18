"""
任务组 Database
提供 TaskGroup 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task_group import TaskGroup


class TaskGroupDatabase(EnhancedBaseDatabase):
    """任务组 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task_group',
            primary_key='task_group_id'
        )
    
    def find_by_team_id(self, team_id: str) -> List[dict]:
        """根据团队ID查询任务组"""
        return self.find_by_condition({'team_id': team_id})
    
    def create(self, task_group: TaskGroup) -> int:
        """创建任务组"""
        data = task_group.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, task_group_id: str, task_group: TaskGroup) -> int:
        """更新任务组"""
        data = task_group.dict(exclude_none=True)
        return self.update_by_id(task_group_id, data)
    
    def get_by_id(self, task_group_id: str) -> Optional[TaskGroup]:
        """根据ID获取任务组"""
        result = self.find_by_id(task_group_id)
        if result:
            return TaskGroup(**result)
        return None


# 全局实例
task_group_database = TaskGroupDatabase()
