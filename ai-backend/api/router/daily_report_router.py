"""
每日日报相关路由
提供每日日报相关的接口
"""
from fastapi import APIRouter, Header, Query, Body
from typing import Optional, List, Dict, Any
from datetime import date
from pydantic import BaseModel
from service.daily_report_service import (
    get_today_completed_tasks_with_children,
    generate_daily_report,
    create_daily_report_tool_config,
    update_daily_report_tool_config,
    get_daily_report_tool_config_by_user_id
)
from common.result import R
from utils.jwt_util import get_user_from_header
from utils.logger_util import logger

router = APIRouter(prefix="/daily-report", tags=["每日日报"])


# 请求模型
class GenerateDailyReportRequest(BaseModel):
    """生成每日日报请求模型"""
    llm_config_id: str
    today_finish_task_list: Optional[List[Dict[str, Any]]] = None


class CreateDailyReportToolConfigRequest(BaseModel):
    """创建每日日报工具配置请求模型"""
    llm_config_id: str


class UpdateDailyReportToolConfigRequest(BaseModel):
    """更新每日日报工具配置请求模型"""
    llm_config_id: str


@router.get("/today-completed-tasks")
async def get_today_completed_tasks_with_children_endpoint(
    target_date: Optional[date] = Query(None, description="目标日期（默认为今天），格式：YYYY-MM-DD"),
    authorization: Optional[str] = Header(None, alias="Authorization"),
    token: Optional[str] = Header(None, alias="token")
) -> R[list]:
    """
    获取今日已完成的任务与子任务
    
    如果子任务已完成，其父级主任务也会出现在结果中。
    
    Args:
        target_date: 目标日期（可选，默认为今天）
        authorization: Authorization 头（Bearer token）
        token: Token 头（备用）
    
    Returns:
        R[list]: 包含任务和子任务的列表，每个元素包含：
        - task: Task 对象
        - child_tasks: ChildTask 对象列表
    """
    try:
        # 获取用户信息
        user = get_user_from_header(authorization, token)
        if not user or not user.user_id:
            return R.unauthorized("未提供有效的认证信息")
        
        # 调用服务方法
        result = get_today_completed_tasks_with_children(user.user_id, target_date)
        
        return R.ok(result, "获取成功")
        
    except ValueError as e:
        logger.error(f"获取今日已完成任务失败: {e}")
        return R.fail(str(e))
    except Exception as e:
        logger.error(f"获取今日已完成任务异常: {e}")
        return R.error(f"获取今日已完成任务异常: {str(e)}")


@router.post("/generate")
async def generate_daily_report_endpoint(
    request: GenerateDailyReportRequest = Body(...),
    authorization: Optional[str] = Header(None, alias="Authorization"),
    token: Optional[str] = Header(None, alias="token")
) -> R[Dict[str, Any]]:
    """
    生成每日日报
    
    Args:
        request: 生成日报请求，包含 llm_config_id 和可选的 today_finish_task_list
        authorization: Authorization 头（Bearer token）
        token: Token 头（备用）
    
    Returns:
        R[Dict]: 包含 today_finish_tasks_report, tomorrow_todo_tasks_report, think_report
    """
    try:
        # 获取用户信息
        user = get_user_from_header(authorization, token)
        if not user or not user.user_id:
            return R.unauthorized("未提供有效的认证信息")
        
        # 调用服务方法
        result = await generate_daily_report(
            user_id=user.user_id,
            llm_config_id=request.llm_config_id,
            today_finish_task_list=request.today_finish_task_list
        )
        
        return R.ok(result, "生成成功")
        
    except ValueError as e:
        logger.error(f"生成每日日报失败: {e}")
        return R.fail(str(e))
    except Exception as e:
        logger.error(f"生成每日日报异常: {e}")
        return R.error(f"生成每日日报异常: {str(e)}")


@router.post("/tool-config")
async def create_daily_report_tool_config_endpoint(
    request: CreateDailyReportToolConfigRequest = Body(...),
    authorization: Optional[str] = Header(None, alias="Authorization"),
    token: Optional[str] = Header(None, alias="token")
) -> R[str]:
    """
    创建每日日报工具配置
    
    Args:
        request: 创建配置请求，包含 llm_config_id
        authorization: Authorization 头（Bearer token）
        token: Token 头（备用）
    
    Returns:
        R[str]: 配置ID
    """
    try:
        # 获取用户信息
        user = get_user_from_header(authorization, token)
        if not user or not user.user_id:
            return R.unauthorized("未提供有效的认证信息")
        
        # 调用服务方法
        config_id = create_daily_report_tool_config(
            user_id=user.user_id,
            llm_config_id=request.llm_config_id
        )
        
        if config_id:
            return R.ok(config_id, "创建成功")
        else:
            return R.fail("创建失败")
        
    except ValueError as e:
        logger.error(f"创建每日日报工具配置失败: {e}")
        return R.fail(str(e))
    except Exception as e:
        logger.error(f"创建每日日报工具配置异常: {e}")
        return R.error(f"创建每日日报工具配置异常: {str(e)}")


@router.put("/tool-config")
async def update_daily_report_tool_config_endpoint(
    request: UpdateDailyReportToolConfigRequest = Body(...),
    authorization: Optional[str] = Header(None, alias="Authorization"),
    token: Optional[str] = Header(None, alias="token")
) -> R[str]:
    """
    更新每日日报工具配置
    
    Args:
        request: 更新配置请求，包含 llm_config_id
        authorization: Authorization 头（Bearer token）
        token: Token 头（备用）
    
    Returns:
        R[str]: 配置ID
    """
    try:
        # 获取用户信息
        user = get_user_from_header(authorization, token)
        if not user or not user.user_id:
            return R.unauthorized("未提供有效的认证信息")
        
        # 调用服务方法
        config_id = update_daily_report_tool_config(
            user_id=user.user_id,
            llm_config_id=request.llm_config_id
        )
        
        if config_id:
            return R.ok(config_id, "更新成功")
        else:
            return R.fail("更新失败，配置不存在或LLM配置无效")
        
    except ValueError as e:
        logger.error(f"更新每日日报工具配置失败: {e}")
        return R.fail(str(e))
    except Exception as e:
        logger.error(f"更新每日日报工具配置异常: {e}")
        return R.error(f"更新每日日报工具配置异常: {str(e)}")


@router.get("/tool-config")
async def get_daily_report_tool_config_by_user_id_endpoint(
    authorization: Optional[str] = Header(None, alias="Authorization"),
    token: Optional[str] = Header(None, alias="token")
) -> R[Dict[str, Any]]:
    """
    根据用户ID获取每日日报工具配置
    
    Args:
        authorization: Authorization 头（Bearer token）
        token: Token 头（备用）
    
    Returns:
        R[Dict]: 配置字典，包含 id, user_id, tool_name, config_json 等字段
    """
    try:
        # 获取用户信息
        user = get_user_from_header(authorization, token)
        if not user or not user.user_id:
            return R.unauthorized("未提供有效的认证信息")
        
        # 调用服务方法
        config = get_daily_report_tool_config_by_user_id(user.user_id)
        
        if config:
            return R.ok(config, "获取成功")
        else:
            return R.ok(None, "配置不存在")
        
    except ValueError as e:
        logger.error(f"获取每日日报工具配置失败: {e}")
        return R.fail(str(e))
    except Exception as e:
        logger.error(f"获取每日日报工具配置异常: {e}")
        return R.error(f"获取每日日报工具配置异常: {str(e)}")
