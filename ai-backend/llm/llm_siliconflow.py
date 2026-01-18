"""
硅基流动 LLM 实现
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


class SiliconFlowChatLLM(BaseChatLLM):
    """硅基流动 Chat LLM 实现（兼容 OpenAI API）"""
    
    def __init__(self, api_key: str, base_url: Optional[str] = None,
                 model_name: str = "deepseek-chat", temperature: float = 0.7,
                 max_tokens: int = 2000):
        """
        初始化硅基流动 Chat LLM
        
        Args:
            api_key: API Key
            base_url: API 基础 URL（默认使用硅基流动地址）
            model_name: 模型名称
            temperature: 温度参数
            max_tokens: 最大 token 数
        """
        self.api_key = api_key
        self.base_url = base_url or "https://api.siliconflow.cn/v1"
        self.model_name = model_name
        self.temperature = temperature
        self.max_tokens = max_tokens
        
        # 检查 LangChain 是否安装
        if ChatOpenAI is None:
            raise ImportError("LangChain 未安装，请运行: uv add langchain langchain-openai")
        
        # 硅基流动使用 OpenAI 兼容接口
        try:
            self.chat_model = ChatOpenAI(
                model=model_name,
                temperature=temperature,
                max_tokens=max_tokens,
                openai_api_key=api_key,
                openai_api_base=self.base_url
            )
            logger.info(f"SiliconFlow Chat LLM initialized: model={model_name}, base_url={self.base_url}")
        except Exception as e:
            logger.error(f"Failed to initialize SiliconFlow Chat LLM: {e}")
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
            logger.error(f"SiliconFlow generate error: {e}")
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
            logger.error(f"SiliconFlow chat error: {e}")
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
            logger.error(f"SiliconFlow generate_async error: {e}")
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
            logger.error(f"SiliconFlow chat_async error: {e}")
            raise
    
    async def chat_stream(self, messages: List[Dict[str, str]], **kwargs) -> AsyncIterator[str]:
        """流式对话生成"""
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
                    logger.error(f"SiliconFlow chat_stream error: {e}")
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
                    logger.error(f"SiliconFlow chat_stream iteration error: {e}")
                    raise
            
            executor.shutdown(wait=False)
                    
        except Exception as e:
            logger.error(f"SiliconFlow chat_stream error: {e}")
            raise


class SiliconFlowEmbedLLM(BaseEmbedLLM):
    """硅基流动 Embedding LLM 实现"""
    
    def __init__(self, api_key: str, base_url: Optional[str] = None,
                 model_name: str = "text-embedding-ada-002"):
        """
        初始化硅基流动 Embedding LLM
        
        Args:
            api_key: API Key
            base_url: API 基础 URL（默认使用硅基流动地址）
            model_name: 模型名称
        """
        self.api_key = api_key
        self.base_url = base_url or "https://api.siliconflow.cn/v1"
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
            logger.info(f"SiliconFlow Embedding LLM initialized: model={model_name}, base_url={self.base_url}")
        except Exception as e:
            logger.error(f"Failed to initialize SiliconFlow Embedding LLM: {e}")
            raise
    
    def embed(self, text: str) -> List[float]:
        """生成文本嵌入向量"""
        try:
            embeddings = self.embedding_model.embed_query(text)
            return embeddings
        except Exception as e:
            logger.error(f"SiliconFlow embed error: {e}")
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
            logger.error(f"SiliconFlow embed_async error: {e}")
            raise


class SiliconFlowRerankLLM(BaseRerankLLM):
    """硅基流动 Rerank LLM 实现（使用专门的 rerank API）"""
    
    def __init__(self, api_key: str, base_url: Optional[str] = None,
                 model_name: str = "BAAI/bge-reranker-v2-m3"):
        """
        初始化硅基流动 Rerank LLM
        
        Args:
            api_key: API Key
            base_url: API 基础 URL（默认使用硅基流动地址）
            model_name: Rerank 模型名称（默认使用 bge-reranker-v2-m3）
        """
        self.api_key = api_key
        self.base_url = base_url or "https://api.siliconflow.cn/v1"
        self.model_name = model_name
        self.rerank_url = f"{self.base_url}/rerank"
        
        if not api_key:
            raise ValueError("API Key 不能为空")
        
        logger.info(f"SiliconFlow Rerank LLM initialized: model={model_name}, base_url={self.base_url}")
    
    def rerank(self, query: str, documents: List[str], top_n: Optional[int] = None) -> List[Dict[str, Any]]:
        """
        对文档列表进行重排序
        使用硅基流动的 rerank API
        """
        try:
            if not documents:
                return []
            
            if not query:
                raise ValueError("查询文本不能为空")
            
            # 准备请求数据
            # 硅基流动 rerank API 格式：{"model": "model_name", "query": "query_text", "documents": ["doc1", "doc2", ...]}
            payload = {
                "model": self.model_name,
                "query": query,
                "documents": documents
            }
            
            # 设置请求头
            headers = {
                "Authorization": f"Bearer {self.api_key}",
                "Content-Type": "application/json"
            }
            
            # 发送 POST 请求
            response = requests.post(
                self.rerank_url,
                json=payload,
                headers=headers,
                timeout=30
            )
            
            # 检查响应状态
            response.raise_for_status()
            result = response.json()
            
            # 解析响应结果
            # 硅基流动 rerank API 返回格式通常为：
            # {"results": [{"index": 0, "relevance_score": 0.95}, ...]}
            scores = []
            
            if "results" in result:
                # 标准格式：results 数组包含 index 和 relevance_score
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
                # 备用格式：data 数组
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
            else:
                # 如果格式不符合预期，记录警告并返回空结果
                logger.warning(f"Unexpected rerank response format: {result}")
                return []
            
            # 按分数降序排序
            scores.sort(key=lambda x: x["score"], reverse=True)
            
            # 返回前 top_n 个结果
            if top_n is not None and top_n > 0:
                return scores[:top_n]
            return scores
            
        except requests.exceptions.RequestException as e:
            logger.error(f"SiliconFlow rerank API request error: {e}")
            if hasattr(e, 'response') and e.response is not None:
                try:
                    error_detail = e.response.json()
                    logger.error(f"API error response: {error_detail}")
                except:
                    logger.error(f"API error response text: {e.response.text}")
            raise ValueError(f"Rerank API 请求失败: {str(e)}")
        except Exception as e:
            logger.error(f"SiliconFlow rerank error: {e}")
            raise
