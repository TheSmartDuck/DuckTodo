"""
用户钉钉机器人配置实体类，对应表 `user_dingtalk_robot`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class UserDingtalkRobot(BaseEntity):
    """用户钉钉机器人配置实体类"""
    user_dingtalk_robot_id: Optional[str] = None  # 用户钉钉机器人配置id（UUID）
    user_id: Optional[str] = None  # 用户id（UUID）
    robot_name: Optional[str] = None  # 机器人名称
    dingtalk_robot_token: Optional[str] = None  # 钉钉机器人Token
    dingtalk_robot_secret: Optional[str] = None  # 钉钉机器人Secret
    dingtalk_robot_keyword: Optional[str] = None  # 钉钉机器人关键字
    
    class Config:
        from_attributes = True
