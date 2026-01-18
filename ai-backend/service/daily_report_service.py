"""
每日日报服务
提供生成每日日报的功能
"""
from typing import Dict, List, Optional, Any
from datetime import date, timedelta
import asyncio
import json
import uuid
import time
from db import (
    task_user_relation_database,
    user_llm_config_database,
    task_database,
    task_group_database,
    user_tool_config_database
)
from db.database import db
from models.entity.user_llm_config import UserLlmConfig
from models.entity.user_tool_config import UserToolConfig
from models.entity.task import Task
from models.entity.child_task import ChildTask
from llm.llm import ChatLLmService
from common.enums.task_status_enum import TaskStatusEnum
from common.enums.llm_model_type_enum import LlmModelTypeEnum
from utils.logger_util import logger
from prompt.daily_report_prompt import (
    DAILY_REPORT_THINKING_PROMPT,
    DAILY_REPORT_TODAY_FINISH_PROMPT,
    DAILY_REPORT_TOMORROW_TODO_PROMPT
)

def get_today_completed_tasks_with_children(user_id: str, target_date: Optional[date] = None) -> List[Dict[str, Any]]:
    """
    获取今日已完成的任务与子任务
    
    如果子任务已完成，其父级主任务也会出现在结果中。
    
    Args:
        user_id: 用户ID
        target_date: 目标日期（默认为今天）
    
    Returns:
        列表，每个元素包含：
        - task: Task 对象
        - child_tasks: ChildTask 对象列表
    """
    if target_date is None:
        target_date = date.today()
    
    today_str = target_date.strftime('%Y-%m-%d')
    
    conn = db.get_connection()
    
    # 确保读取最新数据：设置事务隔离级别为 READ COMMITTED
    # 这样可以避免读取到旧数据（REPEATABLE READ 隔离级别可能导致读取到快照数据）
    try:
        cursor_setup = conn.cursor()
        cursor_setup.execute("SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED")
        cursor_setup.close()
    except Exception as e:
        logger.warning(f"设置事务隔离级别失败: {e}，继续执行查询")
    
    # 1. 查询今日完成的子任务（直接 JOIN task_user_relation 表）
    child_task_sql = """
        SELECT ct.* 
        FROM child_task ct
        LEFT JOIN task_user_relation tur ON ct.task_id = tur.task_id
        WHERE (tur.user_id = %s OR ct.child_task_assignee_id = %s)
          AND ct.finish_time IS NOT NULL
          AND DATE(ct.finish_time) = %s
          AND ct.child_task_status = %s
          AND ct.is_delete = 0
        ORDER BY ct.finish_time DESC
    """
    
    child_task_params = (user_id, user_id, today_str, TaskStatusEnum.COMPLETED)
    cursor = conn.cursor()
    try:
        cursor.execute(child_task_sql, child_task_params)
        completed_child_tasks_data = cursor.fetchall()
    finally:
        cursor.close()
    
    # 2. 收集所有相关的任务ID（包括子任务的父任务ID）
    task_ids_set = set()
    
    # 从完成的子任务中提取父任务ID
    for child_task_data in completed_child_tasks_data:
        if child_task_data.get('task_id'):
            task_ids_set.add(child_task_data['task_id'])
    
    # 3. 查询今日完成的任务（直接 JOIN task_user_relation 表，并 JOIN task_group 表获取任务族名称）
    task_sql = """
        SELECT t.*, tg.group_name as task_group_name
        FROM task t
        INNER JOIN task_user_relation tur ON t.task_id = tur.task_id
        LEFT JOIN task_group tg ON t.task_group_id = tg.task_group_id
        WHERE tur.user_id = %s
          AND t.finish_time IS NOT NULL
          AND DATE(t.finish_time) = %s
          AND t.task_status = %s
          AND t.is_delete = 0
        ORDER BY t.finish_time DESC
    """
    
    task_params = (user_id, today_str, TaskStatusEnum.COMPLETED)
    cursor2 = conn.cursor()
    try:
        cursor2.execute(task_sql, task_params)
        completed_tasks_data = cursor2.fetchall()
        
        # 将今日完成的任务ID也加入集合
        for task_data in completed_tasks_data:
            if task_data.get('task_id'):
                task_ids_set.add(task_data['task_id'])
    finally:
        cursor2.close()
    
    # 4. 查询所有相关任务（包括子任务的父任务，即使父任务不是今日完成的或未完成），并 JOIN task_group 表获取任务族名称
    result_tasks = {}
    if task_ids_set:
        task_ids_list = list(task_ids_set)
        task_placeholders = ','.join(['%s'] * len(task_ids_list))
        all_task_sql = f"""
            SELECT t.*, tg.group_name as task_group_name
            FROM task t
            LEFT JOIN task_group tg ON t.task_group_id = tg.task_group_id
            WHERE t.task_id IN ({task_placeholders})
              AND t.is_delete = 0
        """
        
        cursor3 = conn.cursor()
        try:
            cursor3.execute(all_task_sql, tuple(task_ids_list))
            all_tasks_data = cursor3.fetchall()
            
            # 转换为 Task 对象并存入字典
            for task_data in all_tasks_data:
                # 提取 task_group_name（如果存在），但不修改原始字典
                task_group_name = task_data.get('task_group_name')
                # 创建任务字典的副本，移除 task_group_name（因为 Task 模型没有这个字段）
                task_data_copy = {k: v for k, v in task_data.items() if k != 'task_group_name'}
                task = Task(**task_data_copy)
                # 将 task_group_name 添加到任务字典中
                task_dict = task.dict()
                if task_group_name:
                    task_dict['task_group_name'] = task_group_name
                result_tasks[task.task_id] = {
                    'task': task_dict,
                    'child_tasks': []
                }
        finally:
            cursor3.close()
    
    # 5. 将子任务按父任务分组（确保所有已完成的子任务都有对应的父任务，即使父任务未完成）
    for child_task_data in completed_child_tasks_data:
        child_task = ChildTask(**child_task_data)
        parent_task_id = child_task.task_id
        
        if not parent_task_id:
            # 如果子任务没有父任务ID，跳过
            continue
            
        if parent_task_id in result_tasks:
            # 父任务已在结果中，直接添加子任务
            result_tasks[parent_task_id]['child_tasks'].append(child_task)
        else:
            # 如果父任务不在结果中，需要查询父任务（无论父任务状态如何，只要未删除）
            # 这样可以确保即使父任务未完成，已完成的子任务也能显示
            parent_task = task_database.get_by_id(parent_task_id)
            if parent_task:
                # 查询父任务的任务族名称
                task_group_name = None
                if parent_task.task_group_id:
                    task_group = task_group_database.get_by_id(parent_task.task_group_id)
                    if task_group:
                        task_group_name = task_group.group_name
                # 父任务存在且未删除，添加到结果中（无论父任务状态如何）
                task_dict = parent_task.dict()
                if task_group_name:
                    task_dict['task_group_name'] = task_group_name
                result_tasks[parent_task_id] = {
                    'task': task_dict,
                    'child_tasks': [child_task]
                }
            else:
                # 如果父任务不存在或已删除，记录警告但跳过该子任务
                logger.warning(f"父任务 {parent_task_id} 不存在或已删除，但子任务 {child_task.child_task_id} 已完成，跳过该子任务")
    
    # 6. 将今日完成的任务添加到结果中（如果还没有）
    for task_data in completed_tasks_data:
        # 提取 task_group_name（如果存在），但不修改原始字典
        task_group_name = task_data.get('task_group_name')
        # 创建任务字典的副本，移除 task_group_name（因为 Task 模型没有这个字段）
        task_data_copy = {k: v for k, v in task_data.items() if k != 'task_group_name'}
        task = Task(**task_data_copy)
        if task.task_id not in result_tasks:
            task_dict = task.dict()
            if task_group_name:
                task_dict['task_group_name'] = task_group_name
            result_tasks[task.task_id] = {
                'task': task_dict,
                'child_tasks': []
            }
    
    # 7. 转换为列表格式
    result = list(result_tasks.values())
    
    return result

def get_recent_todos(user_id: str, limit: int = 10, days_ahead: int = 3) -> List[Dict[str, Any]]:
    """
    获取最近待办的任务（前N个）
    
    任务与子任务在同一对比维度下进行获取，按照截止时间排序所有的任务和子任务，
    再选出超时或即将到期的任务或子任务。
    
    Args:
        user_id: 用户ID
        limit: 返回的任务数量，默认10个
        days_ahead: 提前天数，默认3天（即截止时间 <= today + 3天）
    
    Returns:
        任务列表，格式：
            [
              {
                "task_name": "",
                "task_status": "",
                "task_group_name": "",
                "child_task_list": [
                  {
                    "child_task_name": "",
                    "child_task_status": "",
                  }
                ]
              }
            ]
    """
    today = date.today()
    deadline = today + timedelta(days=days_ahead)
    today_str = today.strftime('%Y-%m-%d')
    deadline_str = deadline.strftime('%Y-%m-%d')
    
    conn = db.get_connection()
    
    # 确保读取最新数据
    try:
        cursor_setup = conn.cursor()
        cursor_setup.execute("SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED")
        cursor_setup.close()
    except Exception as e:
        logger.warning(f"设置事务隔离级别失败: {e}，继续执行查询")
    
    # 1. 查询用户相关的任务ID
    user_task_relations = task_user_relation_database.find_by_user_id(user_id)
    user_task_ids = [rel['task_id'] for rel in user_task_relations]
    
    # 2. 查询待办任务（未完成、未取消，有截止时间，且截止时间 <= deadline）
    all_items = []  # 存储所有待办项（任务和子任务）
    
    if user_task_ids:
        placeholders = ','.join(['%s'] * len(user_task_ids))
        
        # 查询待办任务
        task_sql = f"""
            SELECT t.*, tg.group_name as task_group_name
            FROM task t
            LEFT JOIN task_group tg ON t.task_group_id = tg.task_group_id
            WHERE t.task_id IN ({placeholders})
              AND t.due_time IS NOT NULL
              AND DATE(t.due_time) <= %s
              AND t.task_status NOT IN (%s, %s)
              AND t.is_delete = 0
            ORDER BY t.due_time ASC
        """
        task_params = tuple(user_task_ids) + (deadline_str, TaskStatusEnum.COMPLETED, TaskStatusEnum.CANCELED)
        cursor = conn.cursor()
        try:
            cursor.execute(task_sql, task_params)
            tasks_data = cursor.fetchall()
            for task_data in tasks_data:
                task_group_name = task_data.get('task_group_name')
                task_data_copy = {k: v for k, v in task_data.items() if k != 'task_group_name'}
                task = Task(**task_data_copy)
                all_items.append({
                    'type': 'task',
                    'due_time': task.due_time,
                    'task': task,
                    'task_group_name': task_group_name
                })
        finally:
            cursor.close()
        
        # 查询待办子任务
        child_task_sql = f"""
            SELECT ct.*
            FROM child_task ct
            WHERE (ct.task_id IN ({placeholders}) OR ct.child_task_assignee_id = %s)
              AND ct.due_time IS NOT NULL
              AND DATE(ct.due_time) <= %s
              AND ct.child_task_status NOT IN (%s, %s)
              AND ct.is_delete = 0
            ORDER BY ct.due_time ASC
        """
        child_task_params = tuple(user_task_ids) + (user_id, deadline_str, TaskStatusEnum.COMPLETED, TaskStatusEnum.CANCELED)
        cursor2 = conn.cursor()
        try:
            cursor2.execute(child_task_sql, child_task_params)
            child_tasks_data = cursor2.fetchall()
            for child_task_data in child_tasks_data:
                child_task = ChildTask(**child_task_data)
                all_items.append({
                    'type': 'child_task',
                    'due_time': child_task.due_time,
                    'child_task': child_task,
                    'parent_task_id': child_task.task_id
                })
        finally:
            cursor2.close()
    else:
        # 如果没有用户相关的任务，只查询用户指派的子任务
        child_task_sql = """
            SELECT ct.*
            FROM child_task ct
            WHERE ct.child_task_assignee_id = %s
              AND ct.due_time IS NOT NULL
              AND DATE(ct.due_time) <= %s
              AND ct.child_task_status NOT IN (%s, %s)
              AND ct.is_delete = 0
            ORDER BY ct.due_time ASC
        """
        child_task_params = (user_id, deadline_str, TaskStatusEnum.COMPLETED, TaskStatusEnum.CANCELED)
        cursor2 = conn.cursor()
        try:
            cursor2.execute(child_task_sql, child_task_params)
            child_tasks_data = cursor2.fetchall()
            for child_task_data in child_tasks_data:
                child_task = ChildTask(**child_task_data)
                all_items.append({
                    'type': 'child_task',
                    'due_time': child_task.due_time,
                    'child_task': child_task,
                    'parent_task_id': child_task.task_id
                })
        finally:
            cursor2.close()
    
    # 3. 按照截止时间排序，取前 limit 个
    all_items.sort(key=lambda x: x['due_time'] if x['due_time'] else date.max)
    selected_items = all_items[:limit]
    
    # 4. 收集需要返回的任务ID（包括子任务的父任务）
    task_ids_set = set()
    for item in selected_items:
        if item['type'] == 'task':
            task_ids_set.add(item['task'].task_id)
        elif item['type'] == 'child_task':
            parent_task_id = item['parent_task_id']
            if parent_task_id:
                task_ids_set.add(parent_task_id)
    
    # 5. 查询所有相关的任务（包括任务族名称）
    tasks_dict = {}
    if task_ids_set:
        task_ids_list = list(task_ids_set)
        task_placeholders = ','.join(['%s'] * len(task_ids_list))
        all_task_sql = f"""
            SELECT t.*, tg.group_name as task_group_name
            FROM task t
            LEFT JOIN task_group tg ON t.task_group_id = tg.task_group_id
            WHERE t.task_id IN ({task_placeholders})
              AND t.is_delete = 0
        """
        cursor3 = conn.cursor()
        try:
            cursor3.execute(all_task_sql, tuple(task_ids_list))
            all_tasks_data = cursor3.fetchall()
            for task_data in all_tasks_data:
                task_group_name = task_data.get('task_group_name')
                task_data_copy = {k: v for k, v in task_data.items() if k != 'task_group_name'}
                task = Task(**task_data_copy)
                tasks_dict[task.task_id] = {
                    'task': task,
                    'task_group_name': task_group_name,
                    'child_tasks': []
                }
        finally:
            cursor3.close()
    
    # 6. 查询选中任务的所有待办子任务（用于任务类型的项）
    selected_task_ids = set()
    selected_child_task_ids = set()
    for item in selected_items:
        if item['type'] == 'task':
            selected_task_ids.add(item['task'].task_id)
        elif item['type'] == 'child_task':
            selected_child_task_ids.add(item['child_task'].child_task_id)
            if item['parent_task_id']:
                selected_task_ids.add(item['parent_task_id'])
    
    # 查询选中任务的所有待办子任务
    child_tasks_by_parent = {}
    if selected_task_ids:
        task_ids_list = list(selected_task_ids)
        task_placeholders = ','.join(['%s'] * len(task_ids_list))
        child_task_sql = f"""
            SELECT ct.*
            FROM child_task ct
            WHERE ct.task_id IN ({task_placeholders})
              AND ct.due_time IS NOT NULL
              AND DATE(ct.due_time) <= %s
              AND ct.child_task_status NOT IN (%s, %s)
              AND ct.is_delete = 0
            ORDER BY ct.due_time ASC
        """
        child_task_params = tuple(task_ids_list) + (deadline_str, TaskStatusEnum.COMPLETED, TaskStatusEnum.CANCELED)
        cursor4 = conn.cursor()
        try:
            cursor4.execute(child_task_sql, child_task_params)
            all_child_tasks_data = cursor4.fetchall()
            for child_task_data in all_child_tasks_data:
                child_task = ChildTask(**child_task_data)
                parent_id = child_task.task_id
                if parent_id not in child_tasks_by_parent:
                    child_tasks_by_parent[parent_id] = []
                child_tasks_by_parent[parent_id].append(child_task)
        finally:
            cursor4.close()
    
    # 7. 将选中的项组织成结果格式
    result_tasks = {}
    
    for item in selected_items:
        if item['type'] == 'task':
            task = item['task']
            task_id = task.task_id
            if task_id not in result_tasks:
                # 获取该任务的所有待办子任务
                child_task_list = []
                if task_id in child_tasks_by_parent:
                    for ct in child_tasks_by_parent[task_id]:
                        child_task_list.append({
                            'child_task_name': ct.child_task_name or '',
                            'child_task_status': ct.child_task_status or ''
                        })
                
                result_tasks[task_id] = {
                    'task_name': task.task_name or '',
                    'task_status': task.task_status or '',
                    'task_group_name': item.get('task_group_name') or '',
                    'child_task_list': child_task_list
                }
        elif item['type'] == 'child_task':
            child_task = item['child_task']
            parent_task_id = item['parent_task_id']
            
            if parent_task_id and parent_task_id in tasks_dict:
                # 父任务存在
                if parent_task_id not in result_tasks:
                    parent_task = tasks_dict[parent_task_id]['task']
                    # 获取该任务的所有待办子任务（包括当前选中的）
                    child_task_list = []
                    if parent_task_id in child_tasks_by_parent:
                        for ct in child_tasks_by_parent[parent_task_id]:
                            child_task_list.append({
                                'child_task_name': ct.child_task_name or '',
                                'child_task_status': ct.child_task_status or ''
                            })
                    
                    result_tasks[parent_task_id] = {
                        'task_name': parent_task.task_name or '',
                        'task_status': parent_task.task_status or '',
                        'task_group_name': tasks_dict[parent_task_id]['task_group_name'] or '',
                        'child_task_list': child_task_list
                    }
                else:
                    # 如果任务已存在，确保当前子任务也在列表中
                    child_task_exists = any(
                        ct['child_task_name'] == child_task.child_task_name 
                        for ct in result_tasks[parent_task_id]['child_task_list']
                    )
                    if not child_task_exists:
                        result_tasks[parent_task_id]['child_task_list'].append({
                            'child_task_name': child_task.child_task_name or '',
                            'child_task_status': child_task.child_task_status or ''
                        })
            else:
                # 如果父任务不在 tasks_dict 中，需要查询父任务
                if parent_task_id:
                    parent_task = task_database.get_by_id(parent_task_id)
                    if parent_task:
                        # 查询任务族名称
                        task_group_name = None
                        if parent_task.task_group_id:
                            task_group = task_group_database.get_by_id(parent_task.task_group_id)
                            if task_group:
                                task_group_name = task_group.group_name
                        
                        if parent_task_id not in result_tasks:
                            # 获取该任务的所有待办子任务
                            child_task_list = []
                            if parent_task_id in child_tasks_by_parent:
                                for ct in child_tasks_by_parent[parent_task_id]:
                                    child_task_list.append({
                                        'child_task_name': ct.child_task_name or '',
                                        'child_task_status': ct.child_task_status or ''
                                    })
                            else:
                                # 如果不在 child_tasks_by_parent 中，至少添加当前子任务
                                child_task_list.append({
                                    'child_task_name': child_task.child_task_name or '',
                                    'child_task_status': child_task.child_task_status or ''
                                })
                            
                            result_tasks[parent_task_id] = {
                                'task_name': parent_task.task_name or '',
                                'task_status': parent_task.task_status or '',
                                'task_group_name': task_group_name or '',
                                'child_task_list': child_task_list
                            }
                        else:
                            # 如果任务已存在，确保当前子任务也在列表中
                            child_task_exists = any(
                                ct['child_task_name'] == child_task.child_task_name 
                                for ct in result_tasks[parent_task_id]['child_task_list']
                            )
                            if not child_task_exists:
                                result_tasks[parent_task_id]['child_task_list'].append({
                                    'child_task_name': child_task.child_task_name or '',
                                    'child_task_status': child_task.child_task_status or ''
                                })
    
    # 8. 转换为列表格式
    result = list(result_tasks.values())
    
    return result

async def generate_daily_report(user_id: str, llm_config_id: str, today_finish_task_list: Optional[List[Dict[str, Any]]] = None) -> Dict[str, Any]:
    """
    生成每日日报
    
    Args:
        user_id: 用户ID
        llm_config_id: LLM配置ID
        today_finish_task_list: 今日完成任务列表，格式：
            [
              {
                "task_name": "",
                "task_status": "",
                "task_group_name": "",
                "child_task_list": [
                  {
                    "child_task_name": "",
                    "child_task_status": "",
                  }
                ]
              }
            ]
            如果为 None，则调用 get_today_completed_tasks_with_children 获取
    
    Returns:
        包含今日完成报告、明日待办报告、思考报告的字典：
        {
            "today_finish_tasks_report": "",
            "tomorrow_todo_tasks_report": "",
            "think_report": "",
        }
    """
    # 开始计时
    start_time = time.perf_counter()
    logger.info(f"开始生成每日日报 - 用户ID: {user_id}, LLM配置ID: {llm_config_id}")
    
    # 1. 基于用户id和用户输入的llm_config_id获取对应的llm_config配置信息，并生成相应的chat_service
    step_start = time.perf_counter()
    llm_config = None
    if llm_config_id:
        llm_config = user_llm_config_database.get_by_id(llm_config_id)
        if not llm_config:
            logger.warning(f"LLM配置 {llm_config_id} 不存在")
            return {
                "today_finish_tasks_report": "",
                "tomorrow_todo_tasks_report": "",
                "think_report": "LLM配置不存在，无法生成日报"
            }
    
    if not llm_config:
        logger.warning(f"用户 {user_id} 的LLM配置不可用")
        return {
            "today_finish_tasks_report": "",
            "tomorrow_todo_tasks_report": "",
            "think_report": "LLM配置不可用，无法生成日报"
        }
    
    chat_service = ChatLLmService(llm_config)
    step_time = time.perf_counter() - step_start
    
    # 2. 获取今日完成的任务
    # 仅允许在today_finish_task_list为null时，调用get_today_completed_tasks_with_children获取今日完成
    step_start = time.perf_counter()
    if today_finish_task_list is None:
        today_completed_tasks = get_today_completed_tasks_with_children(user_id)

        today_completed_formatted = []
        for item in today_completed_tasks:
            if 'task' in item:
                # get_today_completed_tasks_with_children 返回的格式
                task = item['task']
                child_tasks = item.get('child_tasks', [])
                
                child_task_list = []
                for child_task in child_tasks:
                    if isinstance(child_task, ChildTask):
                        child_task_list.append({
                            'child_task_name': child_task.child_task_name or '',
                            'child_task_status': child_task.child_task_status or ''
                        })
                    elif isinstance(child_task, dict):
                        child_task_list.append({
                            'child_task_name': child_task.get('child_task_name', ''),
                            'child_task_status': child_task.get('child_task_status', '')
                        })
                
                today_completed_formatted.append({
                    'task_name': task.get('task_name', '') if isinstance(task, dict) else task.task_name or '',
                    'task_status': task.get('task_status', '') if isinstance(task, dict) else task.task_status or '',
                    'task_group_name': task.get('task_group_name', '') if isinstance(task, dict) else '',
                    'child_task_list': child_task_list
                })
            else:
                # get_recent_todos 返回的格式（已经是统一格式）
                today_completed_formatted.append(item)
    else:
        # 直接使用传入的 today_finish_task_list
        today_completed_formatted = today_finish_task_list
    
    step_time = time.perf_counter() - step_start
    
    # 3. 基于用户id，调用get_recent_todos方法，获取用户最近未完成的任务清单
    step_start = time.perf_counter()
    tomorrow_todos_formatted = get_recent_todos(user_id, limit=10, days_ahead=3)
    step_time = time.perf_counter() - step_start
    
    # 4. 将任务列表转换为 JSON 字符串（直接使用原始数据，不进行格式化）
    step_start = time.perf_counter()
    today_tasks_json = json.dumps(today_completed_formatted, ensure_ascii=False) if today_completed_formatted else "[]"
    tomorrow_tasks_json = json.dumps(tomorrow_todos_formatted, ensure_ascii=False) if tomorrow_todos_formatted else "[]"
    
    # 5. 构建 prompt 消息（直接传入原始 JSON 数据）
    today_prompt = DAILY_REPORT_TODAY_FINISH_PROMPT.format(task_list_json=today_tasks_json)
    tomorrow_prompt = DAILY_REPORT_TOMORROW_TODO_PROMPT.format(task_list_json=tomorrow_tasks_json)
    step_time = time.perf_counter() - step_start
    
    # 6. 异步分别调用chat_async，获得用户的今日完成清单和明日待办清单
    step_start = time.perf_counter()
    today_messages = [{"role": "user", "content": today_prompt}]
    tomorrow_messages = [{"role": "user", "content": tomorrow_prompt}]
    
    # 异步并行执行
    today_report, tomorrow_report = await asyncio.gather(
        chat_service.chat_async(today_messages),
        chat_service.chat_async(tomorrow_messages)
    )
    
    today_report = today_report.strip() if today_report else ""
    tomorrow_report = tomorrow_report.strip() if tomorrow_report else ""
    step_time = time.perf_counter() - step_start
    
    # 7. 基于今日完成清单和明日待办清单，调用chat_async生成思考
    step_start = time.perf_counter()
    thinking_prompt = DAILY_REPORT_THINKING_PROMPT.format(
        today_report=today_report if today_report else '无',
        tomorrow_report=tomorrow_report if tomorrow_report else '无'
    )
    
    thinking_messages = [{"role": "user", "content": thinking_prompt}]
    think_report = await chat_service.chat_async(thinking_messages)
    think_report = think_report.strip() if think_report else ""
    step_time = time.perf_counter() - step_start
    
    # 8. 计算总耗时并返回生成的日报内容字符串
    total_time = time.perf_counter() - start_time
    logger.info(f"每日日报生成完成 - 用户ID: {user_id}, 总耗时: {total_time:.2f}秒")
    
    return {
        "today_finish_tasks_report": today_report,
        "tomorrow_todo_tasks_report": tomorrow_report,
        "think_report": think_report
    }


def create_daily_report_tool_config(user_id: str, llm_config_id: str) -> Optional[str]:
    """
    生成并保存每日日报工具配置
    
    Args:
        user_id: 用户ID
        llm_config_id: LLM配置ID
    
    Returns:
        配置ID（如果创建成功），否则返回 None
    """
    try:
        # 1. 获取 LLM 配置信息
        llm_config = user_llm_config_database.get_by_id(llm_config_id)
        if not llm_config:
            logger.warning(f"LLM配置 {llm_config_id} 不存在，无法创建工具配置")
            return None
        
        # 2. 构建 config_json
        config_json = {
            "llm_config_id": llm_config_id,
            "llm_model_name": llm_config.llm_model_name or "",
            "llm_provider": llm_config.llm_provider or ""
        }
        
        # 3. 检查是否已存在该用户的 daily_report 配置
        existing_configs = user_tool_config_database.find_by_condition({
            'user_id': user_id,
            'tool_name': 'daily_report'
        })
        
        # 4. 创建或更新配置
        if existing_configs:
            # 更新现有配置
            existing_config = existing_configs[0]
            config_id = existing_config['id']
            
            # 构建更新数据，将 config_json 转换为 JSON 字符串
            update_data = {
                'user_id': user_id,
                'tool_name': 'daily_report',
                'config_json': json.dumps(config_json, ensure_ascii=False)
            }
            
            user_tool_config_database.update_by_id(config_id, update_data)
            logger.info(f"更新用户 {user_id} 的每日日报工具配置，配置ID: {config_id}")
            return config_id
        else:
            # 创建新配置
            config_id = str(uuid.uuid4())
            
            # 构建插入数据，将 config_json 转换为 JSON 字符串
            insert_data = {
                'id': config_id,
                'user_id': user_id,
                'tool_name': 'daily_report',
                'config_json': json.dumps(config_json, ensure_ascii=False)
            }
            
            user_tool_config_database.insert(insert_data)
            logger.info(f"创建用户 {user_id} 的每日日报工具配置，配置ID: {config_id}")
            return config_id
            
    except Exception as e:
        logger.error(f"创建每日日报工具配置失败: {e}")
        return None


def update_daily_report_tool_config(user_id: str, llm_config_id: str) -> Optional[str]:
    """
    更新每日日报工具配置
    
    Args:
        user_id: 用户ID
        llm_config_id: LLM配置ID
    
    Returns:
        配置ID（如果更新成功），否则返回 None
    """
    try:
        # 1. 获取 LLM 配置信息
        llm_config = user_llm_config_database.get_by_id(llm_config_id)
        if not llm_config:
            logger.warning(f"LLM配置 {llm_config_id} 不存在，无法更新工具配置")
            return None
        
        # 2. 构建 config_json
        config_json = {
            "llm_config_id": llm_config_id,
            "llm_model_name": llm_config.llm_model_name or "",
            "llm_provider": llm_config.llm_provider or ""
        }
        
        # 3. 查找该用户的 daily_report 配置
        existing_configs = user_tool_config_database.find_by_condition({
            'user_id': user_id,
            'tool_name': 'daily_report'
        })
        
        if not existing_configs:
            logger.warning(f"用户 {user_id} 的每日日报工具配置不存在，无法更新")
            return None
        
        # 4. 更新配置
        existing_config = existing_configs[0]
        config_id = existing_config['id']
        
        # 构建更新数据，将 config_json 转换为 JSON 字符串
        update_data = {
            'user_id': user_id,
            'tool_name': 'daily_report',
            'config_json': json.dumps(config_json, ensure_ascii=False)
        }
        
        user_tool_config_database.update_by_id(config_id, update_data)
        logger.info(f"更新用户 {user_id} 的每日日报工具配置，配置ID: {config_id}")
        return config_id
        
    except Exception as e:
        logger.error(f"更新每日日报工具配置失败: {e}")
        return None


def get_daily_report_tool_config_by_user_id(user_id: str) -> Optional[Dict[str, Any]]:
    """
    根据用户ID获取每日日报工具配置
    
    Args:
        user_id: 用户ID
    
    Returns:
        配置字典，包含 id, user_id, tool_name, config_json 等字段，如果不存在则返回 None
    """
    try:
        # 查询该用户的 daily_report 配置
        existing_configs = user_tool_config_database.find_by_condition({
            'user_id': user_id,
            'tool_name': 'daily_report'
        })
        
        if not existing_configs:
            logger.debug(f"用户 {user_id} 的每日日报工具配置不存在")
            return None
        
        # 返回第一个配置（通常一个用户只有一个 daily_report 配置）
        config = existing_configs[0]
        
        # 如果 config_json 是字符串，尝试解析为字典
        if isinstance(config.get('config_json'), str):
            try:
                config['config_json'] = json.loads(config['config_json'])
            except json.JSONDecodeError:
                logger.warning(f"用户 {user_id} 的每日日报工具配置 JSON 解析失败")
                config['config_json'] = {}
        
        return config
        
    except Exception as e:
        logger.error(f"获取用户 {user_id} 的每日日报工具配置失败: {e}")
        return None

