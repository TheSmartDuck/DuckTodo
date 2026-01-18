"""
用户安全信息实体类，对应表 `user_security`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class UserSecurity(BaseEntity):
    """用户安全信息实体类"""
    user_id: Optional[str] = None  # 用户id（UUID）
    user_password: Optional[str] = None  # 密码（Argon2(原始密码+密码加盐)，原始密码需8位以上且包含英文）
    user_password_salt: Optional[str] = None  # 密码加盐（四位数字+英文小写）
    user_accesskey: Optional[str] = None  # 用户AK
    user_secretkey: Optional[str] = None  # 用户SK
    sso_source: Optional[str] = None  # 单点登录来源（如：google, github, wechat等）
    sso_uid: Optional[str] = None  # 单点登录唯一标识
    
    class Config:
        from_attributes = True
