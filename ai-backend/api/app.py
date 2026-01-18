"""
AI Backend 主入口文件
使用 FastAPI 构建 AI 相关的后端服务
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from api.router import router as api_router

app = FastAPI(
    title="DuckTodo AI Backend",
    description="AI backend service for DuckTodo",
    version="0.1.0"
)

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应配置具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(api_router, prefix="/api/ai")


@app.get("/api/ai/")
async def root():
    """健康检查接口"""
    return {"message": "DuckTodo AI Backend is running"}


@app.get("/api/ai/health")
async def health():
    """健康检查接口"""
    return {"status": "healthy"}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
