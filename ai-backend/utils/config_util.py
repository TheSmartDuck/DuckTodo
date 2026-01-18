"""
配置工具
读取 .env 文件并生成全局配置字典
"""
import os
from typing import Dict, Any
from dotenv import load_dotenv
from pathlib import Path

# 加载 .env 文件
env_path = Path(__file__).parent.parent / '.env'
load_dotenv(env_path)


def get_config() -> Dict[str, Any]:
    """
    获取全局配置字典
    返回包含所有配置的字典，可在整个 AI 后端中使用
    """
    config = {
        # 数据库配置
        'database': {
            'host': os.getenv('MYSQL_HOST', 'localhost'),
            'port': int(os.getenv('MYSQL_PORT', 3306)),
            'database': os.getenv('MYSQL_DATABASE', 'DuckTodo'),
            'user': os.getenv('MYSQL_USER', os.getenv('MYSQL_USERNAME', 'root')),
            'password': os.getenv('MYSQL_PASSWORD', ''),
        },
        
        # 服务配置
        'server': {
            'host': os.getenv('SERVER_HOST', '0.0.0.0'),
            'port': int(os.getenv('SERVER_PORT', 8000)),
        },
        
        # JWT 配置
        'jwt': {
            'secret': os.getenv('JWT_SECRET', 'ducktodo-secret'),
            'expire_seconds': int(os.getenv('JWT_EXPIRE_SECONDS') or 86400),
        },
    }
    
    return config


# 全局配置实例
config = get_config()
