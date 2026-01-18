"""
用户 Database
提供 User 实体的 CRUD 操作
"""
from typing import Optional, List, Dict, Any
from db.base_database import EnhancedBaseDatabase
from models.entity.user import User


class UserDatabase(EnhancedBaseDatabase):
    """用户 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='user',
            primary_key='user_id'
        )
    
    def find_by_username(self, username: str) -> Optional[Dict[str, Any]]:
        """根据用户名查询"""
        results = self.find_by_condition({'user_name': username}, limit=1)
        return results[0] if results else None
    
    def find_by_email(self, email: str) -> Optional[Dict[str, Any]]:
        """根据邮箱查询"""
        results = self.find_by_condition({'user_email': email}, limit=1)
        return results[0] if results else None
    
    def find_by_phone(self, phone: str) -> Optional[Dict[str, Any]]:
        """根据手机号查询"""
        results = self.find_by_condition({'user_phone': phone}, limit=1)
        return results[0] if results else None
    
    def create(self, user: User) -> int:
        """创建用户"""
        data = user.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, user_id: str, user: User) -> int:
        """更新用户"""
        data = user.dict(exclude_none=True)
        return self.update_by_id(user_id, data)
    
    def get_by_id(self, user_id: str) -> Optional[User]:
        """根据ID获取用户"""
        result = self.find_by_id(user_id)
        if result:
            return User(**result)
        return None
    
    def get_by_username(self, username: str) -> Optional[User]:
        """根据用户名获取用户"""
        result = self.find_by_username(username)
        if result:
            return User(**result)
        return None


# 全局实例
user_database = UserDatabase()
