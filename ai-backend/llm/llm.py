"""
LLM 统一调用接口
基于配置调用不同模型提供商的客户端
"""
from typing import Optional, Dict, Any, List, Union
from llm.base import BaseChatLLM, BaseEmbedLLM, BaseRerankLLM
from llm.llm_openai import OpenAIChatLLM, OpenAIEmbedLLM, OpenAIRerankLLM
from llm.llm_bailian import BailianChatLLM, BailianEmbedLLM, BailianRerankLLM
from llm.llm_siliconflow import SiliconFlowChatLLM, SiliconFlowEmbedLLM, SiliconFlowRerankLLM
from llm.llm_openai_compatible import OpenAICompatibleChatLLM, OpenAICompatibleEmbedLLM, OpenAICompatibleRerankLLM
from models.entity.user_llm_config import UserLlmConfig
from common.enums.llm_model_type_enum import LlmModelTypeEnum
from utils.logger_util import logger


class ChatLLmService:
    """Chat LLM 服务类，提供对话和文本生成功能"""
    
    def __init__(self, config: UserLlmConfig):
        """
        初始化 Chat LLM 服务
        
        Args:
            config: UserLlmConfig 配置对象（模型类型应为 CHAT）
        
        Raises:
            ValueError: 模型类型不是 CHAT
        """
        if config.llm_model_type != LlmModelTypeEnum.CHAT:
            raise ValueError(f"ChatLLmService 需要 CHAT 类型的模型配置，当前类型: {config.llm_model_type}")
        
        self.config = config
        self.llm: Optional[BaseChatLLM] = None
        self._init_llm()
    
    def _init_llm(self):
        """根据 provider 初始化对应的 LLM 客户端"""
        try:
            if not self.config.llm_api_key:
                raise ValueError("LLM API Key 不能为空")
            
            provider = (self.config.llm_provider or "").lower().strip()
            api_key = self.config.llm_api_key
            base_url = self.config.llm_api_url if self.config.llm_api_url else None
            model_name = self.config.llm_model_name or "gpt-3.5-turbo"
            temperature = float(self.config.llm_model_temperature) if self.config.llm_model_temperature else 0.7
            max_tokens = 2000  # 默认值
            
            logger.info(f"Creating Chat LLM client: provider={provider}, model={model_name}, base_url={base_url}")
            
            if provider == "openai":
                self.llm = OpenAIChatLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name,
                    temperature=temperature,
                    max_tokens=max_tokens
                )
            elif provider == "bailian":
                self.llm = BailianChatLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name,
                    temperature=temperature,
                    max_tokens=max_tokens
                )
            elif provider == "siliconflow":
                self.llm = SiliconFlowChatLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name,
                    temperature=temperature,
                    max_tokens=max_tokens
                )
            elif provider == "openai-compatible" or base_url:
                if not base_url:
                    raise ValueError("使用 OpenAI 兼容模式时，base_url 不能为空")
                self.llm = OpenAICompatibleChatLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name,
                    temperature=temperature,
                    max_tokens=max_tokens
                )
            else:
                raise ValueError(f"不支持的 LLM 提供商: {provider}，或未提供 base_url")
            
            logger.info(f"Chat LLM Service initialized: provider={provider}")
        except Exception as e:
            logger.error(f"Failed to initialize Chat LLM Service: {e}")
            self.llm = None
    
    def generate(self, prompt: str, **kwargs) -> str:
        """
        生成文本
        
        Args:
            prompt: 输入提示词
            **kwargs: 其他参数
        
        Returns:
            生成的文本
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        return self.llm.generate(prompt, **kwargs)
    
    def chat(self, messages: List[Dict[str, str]], **kwargs) -> str:
        """
        对话生成
        
        Args:
            messages: 消息列表
            **kwargs: 其他参数
        
        Returns:
            生成的回复
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        return self.llm.chat(messages, **kwargs)
    
    async def generate_async(self, prompt: str, **kwargs) -> str:
        """
        异步生成文本
        
        Args:
            prompt: 输入提示词
            **kwargs: 其他参数
        
        Returns:
            生成的文本
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        return await self.llm.generate_async(prompt, **kwargs)
    
    async def chat_async(self, messages: List[Dict[str, str]], **kwargs) -> str:
        """
        异步对话生成
        
        Args:
            messages: 消息列表
            **kwargs: 其他参数
        
        Returns:
            生成的回复
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        return await self.llm.chat_async(messages, **kwargs)
    
    async def chat_stream(self, messages: List[Dict[str, str]], **kwargs):
        """
        流式对话生成
        
        Args:
            messages: 消息列表
            **kwargs: 其他参数
        
        Yields:
            生成的文本片段（流式输出）
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        async for chunk in self.llm.chat_stream(messages, **kwargs):
            yield chunk
    
    def test_connectivity(self) -> Union[bool, str]:
        """
        测试 Chat LLM 的连通性
        
        Returns:
            Union[bool, str]: 测试结果
            - True: 测试成功
            - 错误信息字符串: 测试失败时的错误信息
        """
        if not self.llm:
            return "LLM 未初始化，无法进行测试"
        
        try:
            logger.info(f"Testing Chat LLM connectivity: provider={self.config.llm_provider}, model={self.config.llm_model_name}")
            test_messages = [
                {"role": "user", "content": "你好"}
            ]
            chat_response = self.llm.chat(test_messages)
            if chat_response and len(chat_response.strip()) > 0:
                logger.info("Chat test passed")
                return True
            else:
                error_msg = "返回结果为空"
                logger.warning(f"Chat test failed: {error_msg}")
                return error_msg
        except Exception as e:
            error_msg = f"Chat 测试失败: {str(e)}"
            logger.error(error_msg)
            return error_msg


class EmbedLLmService:
    """Embedding LLM 服务类，提供文本嵌入向量生成功能"""
    
    def __init__(self, config: UserLlmConfig):
        """
        初始化 Embedding LLM 服务
        
        Args:
            config: UserLlmConfig 配置对象（模型类型应为 EMBEDDING）
        
        Raises:
            ValueError: 模型类型不是 EMBEDDING
        """
        if config.llm_model_type != LlmModelTypeEnum.EMBEDDING:
            raise ValueError(f"EmbedLLmService 需要 EMBEDDING 类型的模型配置，当前类型: {config.llm_model_type}")
        
        self.config = config
        self.llm: Optional[BaseEmbedLLM] = None
        self._init_llm()
    
    def _init_llm(self):
        """根据 provider 初始化对应的 LLM 客户端"""
        try:
            if not self.config.llm_api_key:
                raise ValueError("LLM API Key 不能为空")
            
            provider = (self.config.llm_provider or "").lower().strip()
            api_key = self.config.llm_api_key
            base_url = self.config.llm_api_url if self.config.llm_api_url else None
            model_name = self.config.llm_model_name or "text-embedding-ada-002"
            
            logger.info(f"Creating Embedding LLM client: provider={provider}, model={model_name}, base_url={base_url}")
            
            if provider == "openai":
                self.llm = OpenAIEmbedLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            elif provider == "bailian":
                self.llm = BailianEmbedLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            elif provider == "siliconflow":
                self.llm = SiliconFlowEmbedLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            elif provider == "openai-compatible" or base_url:
                if not base_url:
                    raise ValueError("使用 OpenAI 兼容模式时，base_url 不能为空")
                self.llm = OpenAICompatibleEmbedLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            else:
                raise ValueError(f"不支持的 LLM 提供商: {provider}，或未提供 base_url")
            
            logger.info(f"Embedding LLM Service initialized: provider={provider}")
        except Exception as e:
            logger.error(f"Failed to initialize Embedding LLM Service: {e}")
            self.llm = None
    
    def embed(self, text: str) -> List[float]:
        """
        生成文本嵌入向量
        
        Args:
            text: 输入文本
        
        Returns:
            嵌入向量列表
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        return self.llm.embed(text)
    
    async def embed_async(self, text: str) -> List[float]:
        """
        异步生成文本嵌入向量
        
        Args:
            text: 输入文本
        
        Returns:
            嵌入向量列表
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        return await self.llm.embed_async(text)
    
    def test_connectivity(self) -> Union[bool, str]:
        """
        测试 Embedding LLM 的连通性
        
        Returns:
            Union[bool, str]: 测试结果
            - True: 测试成功
            - 错误信息字符串: 测试失败时的错误信息
        """
        if not self.llm:
            return "LLM 未初始化，无法进行测试"
        
        try:
            logger.info(f"Testing Embedding LLM connectivity: provider={self.config.llm_provider}, model={self.config.llm_model_name}")
            test_text = "测试文本"
            embedding = self.llm.embed(test_text)
            if embedding and isinstance(embedding, list) and len(embedding) > 0:
                logger.info(f"Embedding test passed: dimension={len(embedding)}")
                return True
            else:
                error_msg = "返回结果为空或格式错误"
                logger.warning(f"Embedding test failed: {error_msg}")
                return error_msg
        except Exception as e:
            error_msg = f"Embedding 测试失败: {str(e)}"
            logger.error(error_msg)
            return error_msg


class RerankLLmService:
    """Rerank LLM 服务类，提供文档重排序功能"""
    
    def __init__(self, config: UserLlmConfig):
        """
        初始化 Rerank LLM 服务
        
        Args:
            config: UserLlmConfig 配置对象（模型类型应为 RERANK）
        
        Raises:
            ValueError: 模型类型不是 RERANK
        """
        if config.llm_model_type != LlmModelTypeEnum.RERANK:
            raise ValueError(f"RerankLLmService 需要 RERANK 类型的模型配置，当前类型: {config.llm_model_type}")
        
        self.config = config
        self.llm: Optional[BaseRerankLLM] = None
        self._init_llm()
    
    def _init_llm(self):
        """根据 provider 初始化对应的 LLM 客户端"""
        try:
            if not self.config.llm_api_key:
                raise ValueError("LLM API Key 不能为空")
            
            provider = (self.config.llm_provider or "").lower().strip()
            api_key = self.config.llm_api_key
            base_url = self.config.llm_api_url if self.config.llm_api_url else None
            model_name = self.config.llm_model_name or "text-embedding-ada-002"
            
            logger.info(f"Creating Rerank LLM client: provider={provider}, model={model_name}, base_url={base_url}")
            
            if provider == "openai":
                self.llm = OpenAIRerankLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            elif provider == "bailian":
                self.llm = BailianRerankLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            elif provider == "siliconflow":
                self.llm = SiliconFlowRerankLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            elif provider == "openai-compatible" or base_url:
                if not base_url:
                    raise ValueError("使用 OpenAI 兼容模式时，base_url 不能为空")
                self.llm = OpenAICompatibleRerankLLM(
                    api_key=api_key,
                    base_url=base_url,
                    model_name=model_name
                )
            else:
                raise ValueError(f"不支持的 LLM 提供商: {provider}，或未提供 base_url")
            
            logger.info(f"Rerank LLM Service initialized: provider={provider}")
        except Exception as e:
            logger.error(f"Failed to initialize Rerank LLM Service: {e}")
            self.llm = None
    
    def rerank(self, query: str, documents: List[str], top_n: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        对文档列表进行重排序
        
        Args:
            query: 查询文本
            documents: 文档列表
            top_n: 返回前 N 个结果（None 表示返回全部）
        
        Returns:
            排序后的文档列表，每个元素包含 document、score、index
        
        Raises:
            RuntimeError: LLM 未初始化
        """
        if not self.llm:
            raise RuntimeError("LLM 未初始化，请检查配置")
        return self.llm.rerank(query, documents, top_n)
    
    def test_connectivity(self) -> Union[bool, str]:
        """
        测试 Rerank LLM 的连通性
        
        Returns:
            Union[bool, str]: 测试结果
            - True: 测试成功
            - 错误信息字符串: 测试失败时的错误信息
        """
        if not self.llm:
            return "LLM 未初始化，无法进行测试"
        
        try:
            logger.info(f"Testing Rerank LLM connectivity: provider={self.config.llm_provider}, model={self.config.llm_model_name}")
            test_query = "测试查询"
            test_documents = [
                "这是第一个测试文档",
                "这是第二个测试文档",
                "这是第三个测试文档"
            ]
            rerank_results = self.llm.rerank(test_query, test_documents, top_n=2)
            if rerank_results and isinstance(rerank_results, list) and len(rerank_results) > 0:
                logger.info(f"Rerank test passed: result_count={len(rerank_results)}")
                return True
            else:
                error_msg = "返回结果为空或格式错误"
                logger.warning(f"Rerank test failed: {error_msg}")
                return error_msg
        except Exception as e:
            error_msg = f"Rerank 测试失败: {str(e)}"
            logger.error(error_msg)
            return error_msg


