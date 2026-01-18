"""
第三方 OpenAI 兼容 LLM 实现
用于支持其他兼容 OpenAI API 的第三方服务
"""
from typing import List, Dict, Optional, Any, AsyncIterator
import requests
import asyncio
# LangChain 导入（兼容不同版本）
try:
    from langchain_openai import ChatOpenAI, OpenAIEmbeddings
    from langchain_core.messages import HumanMessage, SystemMessage, AIMessage
except ImportError:
    try:
        from langchain.chat_models import ChatOpenAI
        from langchain.embeddings import OpenAIEmbeddings
        from langchain.schema import HumanMessage, SystemMessage, AIMessage
    except ImportError:
        ChatOpenAI = None
        OpenAIEmbeddings = None
        HumanMessage = None
        SystemMessage = None
        AIMessage = None
from llm.base import BaseChatLLM, BaseEmbedLLM, BaseRerankLLM
from utils.logger_util import logger


class OpenAICompatibleChatLLM(BaseChatLLM):
    """第三方 OpenAI 兼容 Chat LLM 实现"""
    
    def __init__(self, api_key: str, base_url: str,
                 model_name: str = "gpt-3.5-turbo", temperature: float = 0.7,
                 max_tokens: int = 2000):
        """
        初始化第三方 OpenAI 兼容 Chat LLM
        
        Args:
            api_key: API Key
            base_url: API 基础 URL（必填，用于自定义服务地址）
            model_name: 模型名称
            temperature: 温度参数
            max_tokens: 最大 token 数
        """
        if not base_url:
            raise ValueError("base_url is required for OpenAI compatible LLM")
        
        self.api_key = api_key
        self.base_url = base_url.rstrip('/')  # 移除末尾斜杠
        self.model_name = model_name
        self.temperature = temperature
        self.max_tokens = max_tokens
        
        # 检查 LangChain 是否安装
        if ChatOpenAI is None:
            raise ImportError("LangChain 未安装，请运行: uv add langchain langchain-openai")
        
        # 使用 OpenAI 兼容接口
        try:
            self.chat_model = ChatOpenAI(
                model=model_name,
                temperature=temperature,
                max_tokens=max_tokens,
                openai_api_key=api_key,
                openai_api_base=self.base_url
            )
            logger.info(f"OpenAI Compatible Chat LLM initialized: model={model_name}, base_url={self.base_url}")
        except Exception as e:
            logger.error(f"Failed to initialize OpenAI Compatible Chat LLM: {e}")
            raise
    
    def generate(self, prompt: str, **kwargs) -> str:
        """生成文本"""
        try:
            temperature = kwargs.get("temperature", self.temperature)
            max_tokens = kwargs.get("max_tokens", self.max_tokens)
            
            messages = [HumanMessage(content=prompt)]
            response = self.chat_model.invoke(messages)
            
            if hasattr(response, 'content'):
                return response.content
            return str(response)
        except Exception as e:
            logger.error(f"OpenAI Compatible generate error: {e}")
            raise
    
    def chat(self, messages: List[Dict[str, str]], **kwargs) -> str:
        """对话生成"""
        try:
            # 转换消息格式
            langchain_messages = []
            for msg in messages:
                role = msg.get("role", "user")
                content = msg.get("content", "")
                
                if role == "system":
                    langchain_messages.append(SystemMessage(content=content))
                elif role == "assistant":
                    langchain_messages.append(AIMessage(content=content))
                else:
                    langchain_messages.append(HumanMessage(content=content))
            
            response = self.chat_model.invoke(langchain_messages)
            
            if hasattr(response, 'content'):
                return response.content
            return str(response)
        except Exception as e:
            logger.error(f"OpenAI Compatible chat error: {e}")
            raise
    
    async def generate_async(self, prompt: str, **kwargs) -> str:
        """异步生成文本"""
        try:
            loop = asyncio.get_event_loop()
            from concurrent.futures import ThreadPoolExecutor
            executor = ThreadPoolExecutor()
            
            result = await loop.run_in_executor(executor, self.generate, prompt, **kwargs)
            executor.shutdown(wait=False)
            return result
        except Exception as e:
            logger.error(f"OpenAI Compatible generate_async error: {e}")
            raise
    
    async def chat_async(self, messages: List[Dict[str, str]], **kwargs) -> str:
        """异步对话生成"""
        try:
            loop = asyncio.get_event_loop()
            from concurrent.futures import ThreadPoolExecutor
            executor = ThreadPoolExecutor()
            
            result = await loop.run_in_executor(executor, self.chat, messages, **kwargs)
            executor.shutdown(wait=False)
            return result
        except Exception as e:
            logger.error(f"OpenAI Compatible chat_async error: {e}")
            raise
    
    async def chat_stream(self, messages: List[Dict[str, str]], **kwargs) -> AsyncIterator[str]:
        """流式对话生成"""
        try:
            langchain_messages = []
            for msg in messages:
                role = msg.get("role", "user")
                content = msg.get("content", "")
                
                if role == "system":
                    langchain_messages.append(SystemMessage(content=content))
                elif role == "assistant":
                    langchain_messages.append(AIMessage(content=content))
                else:
                    langchain_messages.append(HumanMessage(content=content))
            
            loop = asyncio.get_event_loop()
            from concurrent.futures import ThreadPoolExecutor
            executor = ThreadPoolExecutor()
            
            def _stream_chat():
                try:
                    if hasattr(self.chat_model, 'stream'):
                        for chunk in self.chat_model.stream(langchain_messages):
                            if hasattr(chunk, 'content'):
                                yield chunk.content
                            else:
                                yield str(chunk)
                    else:
                        response = self.chat_model.invoke(langchain_messages)
                        if hasattr(response, 'content'):
                            yield response.content
                        else:
                            yield str(response)
                except Exception as e:
                    logger.error(f"OpenAI Compatible chat_stream error: {e}")
                    raise
            
            import queue
            q = queue.Queue()
            exception_holder = [None]
            
            def _run_stream():
                try:
                    for chunk in _stream_chat():
                        q.put(chunk)
                    q.put(None)
                except Exception as e:
                    exception_holder[0] = e
                    q.put(None)
            
            future = loop.run_in_executor(executor, _run_stream)
            
            while True:
                try:
                    chunk = await asyncio.wait_for(
                        loop.run_in_executor(None, q.get, True, 0.1),
                        timeout=0.2
                    )
                    if chunk is None:
                        break
                    if exception_holder[0]:
                        raise exception_holder[0]
                    yield chunk
                except (asyncio.TimeoutError, queue.Empty):
                    if future.done():
                        while not q.empty():
                            chunk = q.get_nowait()
                            if chunk is None:
                                break
                            if exception_holder[0]:
                                raise exception_holder[0]
                            yield chunk
                        break
                    await asyncio.sleep(0.01)
                    continue
                except Exception as e:
                    logger.error(f"OpenAI Compatible chat_stream iteration error: {e}")
                    raise
            
            executor.shutdown(wait=False)
                    
        except Exception as e:
            logger.error(f"OpenAI Compatible chat_stream error: {e}")
            raise


class OpenAICompatibleEmbedLLM(BaseEmbedLLM):
    """第三方 OpenAI 兼容 Embedding LLM 实现"""
    
    def __init__(self, api_key: str, base_url: str,
                 model_name: str = "text-embedding-ada-002"):
        """
        初始化第三方 OpenAI 兼容 Embedding LLM
        
        Args:
            api_key: API Key
            base_url: API 基础 URL（必填，用于自定义服务地址）
            model_name: 模型名称
        """
        if not base_url:
            raise ValueError("base_url is required for OpenAI compatible LLM")
        
        self.api_key = api_key
        self.base_url = base_url.rstrip('/')  # 移除末尾斜杠
        self.model_name = model_name
        
        # 检查 LangChain 是否安装
        if OpenAIEmbeddings is None:
            raise ImportError("LangChain 未安装，请运行: uv add langchain langchain-openai")
        
        try:
            self.embedding_model = OpenAIEmbeddings(
                model=model_name,
                openai_api_key=api_key,
                openai_api_base=self.base_url
            )
            logger.info(f"OpenAI Compatible Embedding LLM initialized: model={model_name}, base_url={self.base_url}")
        except Exception as e:
            logger.error(f"Failed to initialize OpenAI Compatible Embedding LLM: {e}")
            raise
    
    def embed(self, text: str) -> List[float]:
        """生成文本嵌入向量"""
        try:
            embeddings = self.embedding_model.embed_query(text)
            return embeddings
        except Exception as e:
            logger.error(f"OpenAI Compatible embed error: {e}")
            raise
    
    async def embed_async(self, text: str) -> List[float]:
        """异步生成文本嵌入向量"""
        try:
            loop = asyncio.get_event_loop()
            from concurrent.futures import ThreadPoolExecutor
            executor = ThreadPoolExecutor()
            
            result = await loop.run_in_executor(executor, self.embed, text)
            executor.shutdown(wait=False)
            return result
        except Exception as e:
            logger.error(f"OpenAI Compatible embed_async error: {e}")
            raise


class OpenAICompatibleRerankLLM(BaseRerankLLM):
    """第三方 OpenAI 兼容 Rerank LLM 实现（优先使用 rerank API，失败则回退到 embedding）"""
    
    def __init__(self, api_key: str, base_url: str,
                 model_name: str = "BAAI/bge-reranker-v2-m3"):
        """
        初始化第三方 OpenAI 兼容 Rerank LLM
        
        Args:
            api_key: API Key
            base_url: API 基础 URL（必填，用于自定义服务地址）
            model_name: Rerank 模型名称（默认使用 bge-reranker-v2-m3）
        """
        if not base_url:
            raise ValueError("base_url is required for OpenAI compatible LLM")
        
        self.api_key = api_key
        self.base_url = base_url.rstrip('/')  # 移除末尾斜杠
        self.model_name = model_name
        self.rerank_url = f"{self.base_url}/rerank"
        self.use_embedding_fallback = False
        
        # 检查 LangChain 是否安装（用于回退方案）
        if OpenAIEmbeddings is None:
            logger.warning("LangChain 未安装，如果 rerank API 不可用将无法回退到 embedding 方式")
        else:
            try:
                self.embedding_model = OpenAIEmbeddings(
                    model=model_name,
                    openai_api_key=api_key,
                    openai_api_base=self.base_url
                )
                self.use_embedding_fallback = True
            except Exception as e:
                logger.warning(f"Failed to initialize embedding model for fallback: {e}")
        
        logger.info(f"OpenAI Compatible Rerank LLM initialized: model={model_name}, base_url={self.base_url}")
    
    def rerank(self, query: str, documents: List[str], top_n: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        对文档列表进行重排序
        优先尝试使用 rerank API，如果失败则回退到 embedding 相似度计算
        """
        try:
            if not documents:
                return []
            
            if not query:
                raise ValueError("查询文本不能为空")
            
            # 优先尝试使用 rerank API
            try:
                payload = {
                    "model": self.model_name,
                    "query": query,
                    "documents": documents
                }
                
                headers = {
                    "Authorization": f"Bearer {self.api_key}",
                    "Content-Type": "application/json"
                }
                
                response = requests.post(
                    self.rerank_url,
                    json=payload,
                    headers=headers,
                    timeout=30
                )
                
                if response.status_code == 200:
                    result = response.json()
                    scores = []
                    
                    if "results" in result:
                        for item in result["results"]:
                            index = item.get("index", 0)
                            score = item.get("relevance_score", item.get("score", 0.0))
                            if 0 <= index < len(documents):
                                scores.append({
                                    "document": documents[index],
                                    "score": float(score),
                                    "index": index
                                })
                    elif "data" in result:
                        for i, item in enumerate(result["data"]):
                            if isinstance(item, dict):
                                score = item.get("relevance_score", item.get("score", 0.0))
                                index = item.get("index", i)
                            else:
                                score = float(item) if isinstance(item, (int, float)) else 0.0
                                index = i
                            if 0 <= index < len(documents):
                                scores.append({
                                    "document": documents[index],
                                    "score": float(score),
                                    "index": index
                                })
                    
                    if scores:
                        scores.sort(key=lambda x: x["score"], reverse=True)
                        if top_n is not None and top_n > 0:
                            return scores[:top_n]
                        return scores
                
                # 如果 rerank API 不可用，记录日志并回退
                logger.warning(f"Rerank API returned status {response.status_code}, falling back to embedding")
                
            except requests.exceptions.RequestException as e:
                logger.warning(f"Rerank API request failed: {e}, falling back to embedding")
            
            # 回退到 embedding 方式
            if not self.use_embedding_fallback:
                raise ValueError("Rerank API 不可用且无法回退到 embedding 方式（LangChain 未安装）")
            
            query_embedding = self.embedding_model.embed_query(query)
            doc_embeddings = self.embedding_model.embed_documents(documents)
            
            import numpy as np
            query_vec = np.array(query_embedding)
            scores = []
            
            for i, doc_embedding in enumerate(doc_embeddings):
                doc_vec = np.array(doc_embedding)
                similarity = np.dot(query_vec, doc_vec) / (np.linalg.norm(query_vec) * np.linalg.norm(doc_vec))
                scores.append({
                    "document": documents[i],
                    "score": float(similarity),
                    "index": i
                })
            
            scores.sort(key=lambda x: x["score"], reverse=True)
            if top_n is not None and top_n > 0:
                return scores[:top_n]
            return scores
            
        except Exception as e:
            logger.error(f"OpenAI Compatible rerank error: {e}")
            raise
