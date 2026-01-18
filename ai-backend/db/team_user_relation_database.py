"""
团队用户关系 Database
提供 TeamUserRelation 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.team_user_relation import TeamUserRelation


class TeamUserRelationDatabase(EnhancedBaseDatabase):
    """团队用户关系 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='team_user_relation',
            primary_key='team_user_relation_id'
        )
    
    def find_by_team_id(self, team_id: str) -> List[dict]:
        """根据团队ID查询关系"""
        return self.find_by_condition({'team_id': team_id})
    
    def find_by_user_id(self, user_id: str) -> List[dict]:
        """根据用户ID查询关系"""
        return self.find_by_condition({'user_id': user_id})
    
    def find_by_team_and_user(self, team_id: str, user_id: str) -> Optional[dict]:
        """根据团队ID和用户ID查询关系"""
        results = self.find_by_condition({
            'team_id': team_id,
            'user_id': user_id
        }, limit=1)
        return results[0] if results else None
    
    def create(self, relation: TeamUserRelation) -> int:
        """创建关系"""
        data = relation.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, relation_id: str, relation: TeamUserRelation) -> int:
        """更新关系"""
        data = relation.dict(exclude_none=True)
        return self.update_by_id(relation_id, data)
    
    def get_by_id(self, relation_id: str) -> Optional[TeamUserRelation]:
        """根据ID获取关系"""
        result = self.find_by_id(relation_id)
        if result:
            return TeamUserRelation(**result)
        return None


# 全局实例
team_user_relation_database = TeamUserRelationDatabase()
