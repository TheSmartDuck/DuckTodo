"""
基础实体类
包含所有实体类的通用字段
"""
from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class BaseEntity(BaseModel):
    """基础实体类"""
    is_delete: Optional[int] = 0  # 是否删除 0-未删除 1-已删除
    create_time: Optional[datetime] = None  # 创建时间
    update_time: Optional[datetime] = None  # 更新时间
    
    class Config:
        from_attributes = True
        json_encoders = {
            datetime: lambda v: v.isoformat() if v else None
        }
