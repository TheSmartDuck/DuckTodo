"""
任务附件 Database
提供 TaskFile 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.task_file import TaskFile


class TaskFileDatabase(EnhancedBaseDatabase):
    """任务附件 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='task_file',
            primary_key='task_file_id'
        )
    
    def find_by_task_id(self, task_id: str) -> List[dict]:
        """根据任务ID查询附件"""
        return self.find_by_condition({'task_id': task_id})
    
    def find_by_uploader_id(self, uploader_id: str) -> List[dict]:
        """根据上传者ID查询附件"""
        return self.find_by_condition({'uploader_user_id': uploader_id})
    
    def create(self, task_file: TaskFile) -> int:
        """创建附件记录"""
        data = task_file.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, task_file_id: str, task_file: TaskFile) -> int:
        """更新附件记录"""
        data = task_file.dict(exclude_none=True)
        return self.update_by_id(task_file_id, data)
    
    def get_by_id(self, task_file_id: str) -> Optional[TaskFile]:
        """根据ID获取附件"""
        result = self.find_by_id(task_file_id)
        if result:
            return TaskFile(**result)
        return None


# 全局实例
task_file_database = TaskFileDatabase()
