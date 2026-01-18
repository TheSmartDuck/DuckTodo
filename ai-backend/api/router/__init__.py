"""
API 路由模块
具体接口路由配置
"""
from fastapi import APIRouter
from api.router.llm_router import router as llm_router
from api.router.daily_report_router import router as daily_report_router

router = APIRouter()

# 注册子路由
router.include_router(llm_router)
router.include_router(daily_report_router)


@router.get("/test")
async def test():
    """测试接口"""
    return {"message": "AI Backend API is working"}
