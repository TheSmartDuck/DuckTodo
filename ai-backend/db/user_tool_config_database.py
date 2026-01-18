"""
用户工具配置 Database
提供 UserToolConfig 实体的 CRUD 操作
"""
from typing import Optional, List, Dict, Any
from db.base_database import EnhancedBaseDatabase
from models.entity.user_tool_config import UserToolConfig


class UserToolConfigDatabase(EnhancedBaseDatabase):
    """用户工具配置 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='user_tool_config',
            primary_key='id'
        )
    
    def find_by_user_id(self, user_id: str) -> List[Dict[str, Any]]:
        """
        根据用户ID查询配置列表
        
        Args:
            user_id: 用户ID
        
        Returns:
            配置列表
        """
        return self.find_by_condition({'user_id': user_id})
    
    def create(self, config: UserToolConfig) -> int:
        """
        创建配置
        
        Args:
            config: UserToolConfig 实体对象
        
        Returns:
            插入的行数
        """
        data = config.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, config_id: str, config: UserToolConfig) -> int:
        """
        更新配置
        
        Args:
            config_id: 配置ID
            config: UserToolConfig 实体对象
        
        Returns:
            更新的行数
        """
        data = config.dict(exclude_none=True)
        return self.update_by_id(config_id, data)
    
    def get_by_id(self, config_id: str) -> Optional[Dict[str, Any]]:
        """
        根据ID获取配置
        
        Args:
            config_id: 配置ID
        
        Returns:
            配置字典或 None
        """
        return self.find_by_id(config_id)
    
    def get_by_user_id(self, user_id: str) -> Optional[Dict[str, Any]]:
        """
        根据用户ID获取配置（返回第一个）
        
        Args:
            user_id: 用户ID
        
        Returns:
            配置字典或 None
        """
        results = self.find_by_user_id(user_id)
        return results[0] if results else None


# 全局实例
user_tool_config_database = UserToolConfigDatabase()
