"""
用户安全信息 Database
提供 UserSecurity 实体的 CRUD 操作
"""
from typing import Optional
from db.base_database import EnhancedBaseDatabase
from models.entity.user_security import UserSecurity


class UserSecurityDatabase(EnhancedBaseDatabase):
    """用户安全信息 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='user_security',
            primary_key='user_id'
        )
    
    def get_by_user_id(self, user_id: str) -> Optional[UserSecurity]:
        """根据用户ID获取安全信息"""
        result = self.find_by_id(user_id)
        if result:
            return UserSecurity(**result)
        return None
    
    def create(self, security: UserSecurity) -> int:
        """创建安全信息"""
        data = security.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, user_id: str, security: UserSecurity) -> int:
        """更新安全信息"""
        data = security.dict(exclude_none=True)
        return self.update_by_id(user_id, data)


# 全局实例
user_security_database = UserSecurityDatabase()
