"""
LLM 相关路由
提供 LLM 连通性测试等接口
"""
from fastapi import APIRouter
from models.entity.user_llm_config import UserLlmConfig
from llm.llm import ChatLLmService, EmbedLLmService, RerankLLmService
from common.enums.llm_model_type_enum import LlmModelTypeEnum
from common.result import R
from utils.logger_util import logger

router = APIRouter(prefix="/llm", tags=["LLM"])


@router.post("/test-connectivity")
async def test_llm_connectivity(config: UserLlmConfig) -> R[bool]:
    """
    测试 LLM 模型的连通性
    
    Args:
        config: UserLlmConfig 配置对象（包含模型类型、提供商、API Key 等信息）
    
    Returns:
        R[bool]: 测试结果，成功返回 True，失败返回错误信息（在 message 字段中）
    """
    try:
        # 检查模型类型
        if config.llm_model_type is None:
            return R.fail("模型类型未指定，无法进行测试")
        # 根据模型类型创建对应的服务并测试
        if config.llm_model_type == LlmModelTypeEnum.CHAT:
            try:
                service = ChatLLmService(config)
                result = service.test_connectivity()
                if result is True:
                    return R.ok(True, "Chat LLM 连通性测试成功")
                else:
                    return R.fail(str(result))
            except ValueError as e:
                return R.fail(str(e))
            except Exception as e:
                logger.error(f"Chat LLM 连通性测试失败: {e}")
                return R.error(f"Chat LLM 连通性测试失败: {str(e)}")
        
        elif config.llm_model_type == LlmModelTypeEnum.EMBEDDING:
            try:
                service = EmbedLLmService(config)
                result = service.test_connectivity()
                if result is True:
                    return R.ok(True, "Embedding LLM 连通性测试成功")
                else:
                    return R.fail(str(result))
            except ValueError as e:
                return R.fail(str(e))
            except Exception as e:
                logger.error(f"Embedding LLM 连通性测试失败: {e}")
                return R.error(f"Embedding LLM 连通性测试失败: {str(e)}")
        
        elif config.llm_model_type == LlmModelTypeEnum.RERANK:
            try:
                service = RerankLLmService(config)
                result = service.test_connectivity()
                if result is True:
                    return R.ok(True, "Rerank LLM 连通性测试成功")
                else:
                    return R.fail(str(result))
            except ValueError as e:
                return R.fail(str(e))
            except Exception as e:
                logger.error(f"Rerank LLM 连通性测试失败: {e}")
                return R.error(f"Rerank LLM 连通性测试失败: {str(e)}")
        
        else:
            return R.fail(f"不支持的模型类型: {config.llm_model_type}")
    
    except Exception as e:
        logger.error(f"LLM 连通性测试异常: {e}")
        return R.error(f"LLM 连通性测试异常: {str(e)}")
