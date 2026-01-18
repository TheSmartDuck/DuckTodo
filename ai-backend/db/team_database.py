"""
团队 Database
提供 Team 实体的 CRUD 操作
"""
from typing import Optional
from db.base_database import EnhancedBaseDatabase
from models.entity.team import Team


class TeamDatabase(EnhancedBaseDatabase):
    """团队 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='team',
            primary_key='team_id'
        )
    
    def find_by_name(self, team_name: str) -> Optional[dict]:
        """根据团队名称查询"""
        results = self.find_by_condition({'team_name': team_name}, limit=1)
        return results[0] if results else None
    
    def create(self, team: Team) -> int:
        """创建团队"""
        data = team.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, team_id: str, team: Team) -> int:
        """更新团队"""
        data = team.dict(exclude_none=True)
        return self.update_by_id(team_id, data)
    
    def get_by_id(self, team_id: str) -> Optional[Team]:
        """根据ID获取团队"""
        result = self.find_by_id(team_id)
        if result:
            return Team(**result)
        return None


# 全局实例
team_database = TeamDatabase()
