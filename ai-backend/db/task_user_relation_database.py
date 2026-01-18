"""
任务用户关系 Database
提供 TaskUserRelation 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task_user_relation import TaskUserRelation


class TaskUserRelationDatabase(EnhancedBaseDatabase):
    """任务用户关系 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task_user_relation',
            primary_key='task_user_relation_id'
        )
    
    def find_by_task_id(self, task_id: str) -> List[dict]:
        """根据任务ID查询关系"""
        return self.find_by_condition({'task_id': task_id})
    
    def find_by_user_id(self, user_id: str) -> List[dict]:
        """根据用户ID查询关系"""
        return self.find_by_condition({'user_id': user_id})
    
    def find_by_task_and_user(self, task_id: str, user_id: str) -> Optional[dict]:
        """根据任务ID和用户ID查询关系"""
        results = self.find_by_condition({
            'task_id': task_id,
            'user_id': user_id
        }, limit=1)
        return results[0] if results else None
    
    def create(self, relation: TaskUserRelation) -> int:
        """创建关系"""
        data = relation.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, relation_id: str, relation: TaskUserRelation) -> int:
        """更新关系"""
        data = relation.dict(exclude_none=True)
        return self.update_by_id(relation_id, data)
    
    def get_by_id(self, relation_id: str) -> Optional[TaskUserRelation]:
        """根据ID获取关系"""
        result = self.find_by_id(relation_id)
        if result:
            return TaskUserRelation(**result)
        return None


# 全局实例
task_user_relation_database = TaskUserRelationDatabase()
