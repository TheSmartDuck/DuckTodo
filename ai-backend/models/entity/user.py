"""
用户实体类，对应表 `user`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class User(BaseEntity):
    """用户实体类"""
    user_id: Optional[str] = None  # 用户id（UUID）
    user_name: Optional[str] = None  # 用户名，要求2位以上，不可重复
    user_email: Optional[str] = None  # 用户邮箱，要求邮箱格式，不可重复
    user_phone: Optional[str] = None  # 用户手机，要求11位，不可重复
    user_sex: Optional[int] = None  # 用户性别，0-女，1-男
    user_avatar: Optional[str] = None  # 用户头像，存储路径
    user_remark: Optional[str] = None  # 用户备注
    last_login_time: Optional[datetime] = None  # 上次登录时间
    
    class Config:
        from_attributes = True
