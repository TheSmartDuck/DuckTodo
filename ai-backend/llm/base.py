"""
LLM 基础接口
定义统一的 LLM 调用接口
"""
from abc import ABC, abstractmethod
from typing import List, Optional, Dict, Any, AsyncIterator


class BaseChatLLM(ABC):
    """Chat LLM 基础接口"""
    
    @abstractmethod
    def generate(self, prompt: str, **kwargs) -> str:
        """
        生成文本
        
        Args:
            prompt: 输入提示词
            **kwargs: 其他参数（temperature, max_tokens 等）
        
        Returns:
            生成的文本
        """
        pass
    
    @abstractmethod
    def chat(self, messages: List[Dict[str, str]], **kwargs) -> str:
        """
        对话生成
        
        Args:
            messages: 消息列表，格式为 [{"role": "user", "content": "..."}]
            **kwargs: 其他参数
        
        Returns:
            生成的回复
        """
        pass
    
    async def generate_async(self, prompt: str, **kwargs) -> str:
        """
        异步生成文本（默认实现：在线程池中执行同步方法）
        
        Args:
            prompt: 输入提示词
            **kwargs: 其他参数
        
        Returns:
            生成的文本
        """
        import asyncio
        from concurrent.futures import ThreadPoolExecutor
        
        loop = asyncio.get_event_loop()
        executor = ThreadPoolExecutor()
        
        try:
            result = await loop.run_in_executor(executor, self.generate, prompt, **kwargs)
            return result
        finally:
            executor.shutdown(wait=False)
    
    async def chat_async(self, messages: List[Dict[str, str]], **kwargs) -> str:
        """
        异步对话生成（默认实现：在线程池中执行同步方法）
        
        Args:
            messages: 消息列表
            **kwargs: 其他参数
        
        Returns:
            生成的回复
        """
        import asyncio
        from concurrent.futures import ThreadPoolExecutor
        
        loop = asyncio.get_event_loop()
        executor = ThreadPoolExecutor()
        
        try:
            result = await loop.run_in_executor(executor, self.chat, messages, **kwargs)
            return result
        finally:
            executor.shutdown(wait=False)
    
    async def chat_stream(self, messages: List[Dict[str, str]], **kwargs) -> AsyncIterator[str]:
        """
        流式对话生成（默认实现：抛出 NotImplementedError）
        
        Args:
            messages: 消息列表
            **kwargs: 其他参数
        
        Yields:
            生成的文本片段（流式输出）
        """
        raise NotImplementedError("流式对话生成需要子类实现")


class BaseEmbedLLM(ABC):
    """Embedding LLM 基础接口"""
    
    @abstractmethod
    def embed(self, text: str) -> List[float]:
        """
        生成文本嵌入向量（embedding）
        
        Args:
            text: 输入文本
        
        Returns:
            嵌入向量列表
        """
        pass
    
    async def embed_async(self, text: str) -> List[float]:
        """
        异步生成文本嵌入向量（默认实现：在线程池中执行同步方法）
        
        Args:
            text: 输入文本
        
        Returns:
            嵌入向量列表
        """
        import asyncio
        from concurrent.futures import ThreadPoolExecutor
        
        loop = asyncio.get_event_loop()
        executor = ThreadPoolExecutor()
        
        try:
            result = await loop.run_in_executor(executor, self.embed, text)
            return result
        finally:
            executor.shutdown(wait=False)


class BaseRerankLLM(ABC):
    """Rerank LLM 基础接口"""
    
    @abstractmethod
    def rerank(self, query: str, documents: List[str], top_n: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        对文档列表进行重排序（rerank）
        
        Args:
            query: 查询文本
            documents: 文档列表
            top_n: 返回前 N 个结果（None 表示返回全部）
        
        Returns:
            排序后的文档列表，每个元素包含：
            - document: 文档内容
            - score: 相关性分数
            - index: 原始索引
        """
        pass
