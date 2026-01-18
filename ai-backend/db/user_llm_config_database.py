"""
用户LLM配置 Database
提供 UserLlmConfig 实体的 CRUD 操作
"""
from typing import Optional, List, Dict, Any
from db.base_database import EnhancedBaseDatabase
from models.entity.user_llm_config import UserLlmConfig


class UserLlmConfigDatabase(EnhancedBaseDatabase):
    """用户LLM配置 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='user_llm_config',
            primary_key='user_llm_config_id'
        )
    
    def find_by_user_id(self, user_id: str, 
                       model_type: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        根据用户ID查询配置列表
        
        Args:
            user_id: 用户ID
            model_type: 模型类型（可选，1-chat, 2-embedding, 3-rerank）
        
        Returns:
            配置列表
        """
        conditions = {'user_id': user_id}
        if model_type is not None:
            conditions['llm_model_type'] = model_type
        return self.find_by_condition(conditions)
    
    def find_by_user_id_and_type(self, user_id: str, model_type: int) -> List[Dict[str, Any]]:
        """
        根据用户ID和模型类型查询配置
        
        Args:
            user_id: 用户ID
            model_type: 模型类型（1-chat, 2-embedding, 3-rerank）
        
        Returns:
            配置列表
        """
        return self.find_by_condition({
            'user_id': user_id,
            'llm_model_type': model_type
        })
    
    def create(self, config: UserLlmConfig) -> int:
        """
        创建配置
        
        Args:
            config: UserLlmConfig 实体对象
        
        Returns:
            插入的行数
        """
        data = config.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, config_id: str, config: UserLlmConfig) -> int:
        """
        更新配置
        
        Args:
            config_id: 配置ID
            config: UserLlmConfig 实体对象
        
        Returns:
            更新的行数
        """
        data = config.dict(exclude_none=True)
        return self.update_by_id(config_id, data)
    
    def get_by_id(self, config_id: str) -> Optional[UserLlmConfig]:
        """
        根据ID获取配置
        
        Args:
            config_id: 配置ID
        
        Returns:
            UserLlmConfig 对象或 None
        """
        result = self.find_by_id(config_id)
        if result:
            return UserLlmConfig(**result)
        return None
    
    def get_all_by_user(self, user_id: str) -> List[UserLlmConfig]:
        """
        获取用户的所有配置
        
        Args:
            user_id: 用户ID
        
        Returns:
            UserLlmConfig 对象列表
        """
        results = self.find_by_user_id(user_id)
        return [UserLlmConfig(**r) for r in results]


# 全局实例
user_llm_config_database = UserLlmConfigDatabase()
