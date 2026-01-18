"""
任务附件实体，对应表 `task_file`
"""
from typing import Optional
from datetime import datetime
from models.entity.base import BaseEntity


class TaskFile(BaseEntity):
    """任务附件实体"""
    task_file_id: Optional[str] = None  # 任务附件id（UUID）
    task_id: Optional[str] = None  # 所属任务id（UUID）
    uploader_user_id: Optional[str] = None  # 上传者用户id（UUID）
    task_file_name: Optional[str] = None  # 附件名称
    task_file_path: Optional[str] = None  # 附件存储路径（URL或本地路径）
    task_file_type: Optional[str] = None  # 附件类型（MIME类型或分类）
    task_file_size: Optional[int] = None  # 附件大小（字节）
    task_file_status: Optional[int] = None  # 附件状态，0-禁用，1-正常
    task_file_remark: Optional[str] = None  # 附件备注
    upload_time: Optional[datetime] = None  # 上传时间
    
    class Config:
        from_attributes = True
