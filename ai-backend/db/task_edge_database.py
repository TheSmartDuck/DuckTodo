"""
任务边 Database
提供 TaskEdge 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task_edge import TaskEdge


class TaskEdgeDatabase(EnhancedBaseDatabase):
    """任务边 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task_edge',
            primary_key='task_edge_id'
        )
    
    def find_by_source_node(self, source_node_id: str) -> List[dict]:
        """根据源节点ID查询边"""
        return self.find_by_condition({'source_node_id': source_node_id})
    
    def find_by_target_node(self, target_node_id: str) -> List[dict]:
        """根据目标节点ID查询边"""
        return self.find_by_condition({'target_node_id': target_node_id})
    
    def create(self, task_edge: TaskEdge) -> int:
        """创建边"""
        data = task_edge.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, task_edge_id: str, task_edge: TaskEdge) -> int:
        """更新边"""
        data = task_edge.dict(exclude_none=True)
        return self.update_by_id(task_edge_id, data)
    
    def get_by_id(self, task_edge_id: str) -> Optional[TaskEdge]:
        """根据ID获取边"""
        result = self.find_by_id(task_edge_id)
        if result:
            return TaskEdge(**result)
        return None


# 全局实例
task_edge_database = TaskEdgeDatabase()
