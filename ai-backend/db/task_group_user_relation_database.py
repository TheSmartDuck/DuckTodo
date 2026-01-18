"""
任务组用户关系 Database
提供 TaskGroupUserRelation 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task_group_user_relation import TaskGroupUserRelation


class TaskGroupUserRelationDatabase(EnhancedBaseDatabase):
    """任务组用户关系 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task_group_user_relation',
            primary_key='task_group_user_relation_id'
        )
    
    def find_by_task_group_id(self, task_group_id: str) -> List[dict]:
        """根据任务组ID查询关系"""
        return self.find_by_condition({'task_group_id': task_group_id})
    
    def find_by_user_id(self, user_id: str) -> List[dict]:
        """根据用户ID查询关系"""
        return self.find_by_condition({'user_id': user_id})
    
    def find_by_task_group_and_user(self, task_group_id: str, user_id: str) -> Optional[dict]:
        """根据任务组ID和用户ID查询关系"""
        results = self.find_by_condition({
            'task_group_id': task_group_id,
            'user_id': user_id
        }, limit=1)
        return results[0] if results else None
    
    def create(self, relation: TaskGroupUserRelation) -> int:
        """创建关系"""
        data = relation.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, relation_id: str, relation: TaskGroupUserRelation) -> int:
        """更新关系"""
        data = relation.dict(exclude_none=True)
        return self.update_by_id(relation_id, data)
    
    def get_by_id(self, relation_id: str) -> Optional[TaskGroupUserRelation]:
        """根据ID获取关系"""
        result = self.find_by_id(relation_id)
        if result:
            return TaskGroupUserRelation(**result)
        return None


# 全局实例
task_group_user_relation_database = TaskGroupUserRelationDatabase()
