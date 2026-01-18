"""
任务节点 Database
提供 TaskNode 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task_node import TaskNode


class TaskNodeDatabase(EnhancedBaseDatabase):
    """任务节点 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task_node',
            primary_key='task_node_id'
        )
    
    def find_by_task_id(self, task_id: str) -> List[dict]:
        """根据任务ID查询节点"""
        return self.find_by_condition({'task_id': task_id})
    
    def find_by_child_task_id(self, child_task_id: str) -> List[dict]:
        """根据子任务ID查询节点"""
        return self.find_by_condition({'child_task_id': child_task_id})
    
    def find_by_task_group_id(self, task_group_id: str) -> List[dict]:
        """根据任务组ID查询节点"""
        return self.find_by_condition({'task_group_id': task_group_id})
    
    def create(self, task_node: TaskNode) -> int:
        """创建节点"""
        data = task_node.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, task_node_id: str, task_node: TaskNode) -> int:
        """更新节点"""
        data = task_node.dict(exclude_none=True)
        return self.update_by_id(task_node_id, data)
    
    def get_by_id(self, task_node_id: str) -> Optional[TaskNode]:
        """根据ID获取节点"""
        result = self.find_by_id(task_node_id)
        if result:
            return TaskNode(**result)
        return None


# 全局实例
task_node_database = TaskNodeDatabase()
