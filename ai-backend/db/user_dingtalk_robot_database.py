"""
用户钉钉机器人配置 Database
提供 UserDingtalkRobot 实体的 CRUD 操作
"""
from typing import Optional, List
from db.base_database import EnhancedBaseDatabase
from models.entity.user_dingtalk_robot import UserDingtalkRobot


class UserDingtalkRobotDatabase(EnhancedBaseDatabase):
    """用户钉钉机器人配置 Database"""
    
    def __init__(self):
        super().__init__(
            table_name='user_dingtalk_robot',
            primary_key='user_dingtalk_robot_id'
        )
    
    def find_by_user_id(self, user_id: str) -> List[dict]:
        """根据用户ID查询配置"""
        return self.find_by_condition({'user_id': user_id})
    
    def create(self, robot: UserDingtalkRobot) -> int:
        """创建配置"""
        data = robot.dict(exclude_none=True)
        return self.insert(data)
    
    def update(self, robot_id: str, robot: UserDingtalkRobot) -> int:
        """更新配置"""
        data = robot.dict(exclude_none=True)
        return self.update_by_id(robot_id, data)
    
    def get_by_id(self, robot_id: str) -> Optional[UserDingtalkRobot]:
        """根据ID获取配置"""
        result = self.find_by_id(robot_id)
        if result:
            return UserDingtalkRobot(**result)
        return None


# 全局实例
user_dingtalk_robot_database = UserDingtalkRobotDatabase()
