"""
LLM 模型类型枚举
"""
from enum import IntEnum


class LlmModelTypeEnum(IntEnum):
    """LLM 模型类型枚举"""
    CHAT = 1  # chat模型
    EMBEDDING = 2  # embedding模型
    RERANK = 3  # rerank模型
