"""
任务 Database
提供 Task 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task import Task


class TaskDatabase(EnhancedBaseDatabase):
    """任务 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task',
            primary_key='task_id'
        )
    
    def find_by_team_id(self, team_id: str) -> List[dict]:
        """根据团队ID查询任务"""
        return self.find_by_condition({'team_id': team_id})
    
    def find_by_task_group_id(self, task_group_id: str) -> List[dict]:
        """根据任务组ID查询任务"""
        return self.find_by_condition({'task_group_id': task_group_id})
    
    def find_by_status(self, status: int, team_id: Optional[str] = None) -> List[dict]:
        """根据状态查询任务"""
        conditions = {'task_status': status}
        if team_id:
            conditions['team_id'] = team_id
        return self.find_by_condition(conditions)
    
    def create(self, task: Task) -> int:
        """创建任务"""
        data = task.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, task_id: str, task: Task) -> int:
        """更新任务"""
        data = task.dict(exclude_none=True)
        return self.update_by_id(task_id, data)
    
    def get_by_id(self, task_id: str) -> Optional[Task]:
        """根据ID获取任务"""
        result = self.find_by_id(task_id)
        if result:
            return Task(**result)
        return None


# 全局实例
task_database = TaskDatabase()
