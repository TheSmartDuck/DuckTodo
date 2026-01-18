"""
基础 Database 类
提供通用的 CRUD 操作，支持自定义主键字段名
"""
from typing import Optional, List, Dict, Any
from datetime import datetime
from db.database import db, BaseDAO


class EnhancedBaseDatabase(BaseDAO):
    """增强的基础 Database 类，支持自定义主键字段名和软删除"""
    
    def __init__(self, table_name: str, primary_key: str = 'id'):
        """
        初始化 Database
        
        Args:
            table_name: 表名
            primary_key: 主键字段名，默认为 'id'
        """
        super().__init__(table_name)
        self.primary_key = primary_key
    
    def find_by_id(self, id_value: str) -> Optional[Dict[str, Any]]:
        """根据主键 ID 查询（支持软删除过滤）"""
        sql = f"SELECT * FROM {self.table_name} WHERE {self.primary_key} = %s AND is_delete = 0"
        results = self.execute_query(sql, (id_value,))
        return results[0] if results else None
    
    def find_all(self, limit: Optional[int] = None, offset: int = 0, 
                 include_deleted: bool = False) -> List[Dict[str, Any]]:
        """查询所有记录（默认排除已删除）"""
        sql = f"SELECT * FROM {self.table_name}"
        if not include_deleted:
            sql += " WHERE is_delete = 0"
        if limit:
            sql += f" LIMIT {limit} OFFSET {offset}"
        return self.execute_query(sql)
    
    def find_by_condition(self, conditions: Dict[str, Any], 
                          limit: Optional[int] = None, offset: int = 0,
                          include_deleted: bool = False) -> List[Dict[str, Any]]:
        """根据条件查询"""
        where_clauses = []
        params = []
        
        for key, value in conditions.items():
            if value is not None:
                where_clauses.append(f"{key} = %s")
                params.append(value)
        
        if not include_deleted:
            where_clauses.append("is_delete = 0")
        
        sql = f"SELECT * FROM {self.table_name}"
        if where_clauses:
            sql += " WHERE " + " AND ".join(where_clauses)
        if limit:
            sql += f" LIMIT {limit} OFFSET {offset}"
        
        return self.execute_query(sql, tuple(params) if params else None)
    
    def insert(self, data: Dict[str, Any]) -> int:
        """插入记录（自动设置创建时间）"""
        # 移除主键字段（如果是 None）
        insert_data = {k: v for k, v in data.items() if v is not None or k == self.primary_key}
        
        # 设置默认值
        if 'is_delete' not in insert_data:
            insert_data['is_delete'] = 0
        if 'create_time' not in insert_data:
            insert_data['create_time'] = datetime.now()
        if 'update_time' not in insert_data:
            insert_data['update_time'] = datetime.now()
        
        keys = list(insert_data.keys())
        values = list(insert_data.values())
        placeholders = ', '.join(['%s'] * len(keys))
        columns = ', '.join(keys)
        sql = f"INSERT INTO {self.table_name} ({columns}) VALUES ({placeholders})"
        
        conn = db.get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute(sql, tuple(values))
            conn.commit()
            return cursor.lastrowid
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            cursor.close()
    
    def update_by_id(self, id_value: str, data: Dict[str, Any]) -> int:
        """根据主键 ID 更新记录（自动设置更新时间）"""
        # 移除主键字段和 None 值
        update_data = {k: v for k, v in data.items() 
                      if k != self.primary_key and v is not None}
        
        if not update_data:
            return 0
        
        # 设置更新时间
        update_data['update_time'] = datetime.now()
        
        keys = list(update_data.keys())
        values = list(update_data.values())
        set_clause = ', '.join([f"{key} = %s" for key in keys])
        sql = f"UPDATE {self.table_name} SET {set_clause} WHERE {self.primary_key} = %s AND is_delete = 0"
        params = tuple(values) + (id_value,)
        return self.execute_update(sql, params)
    
    def delete_by_id(self, id_value: str, hard_delete: bool = False) -> int:
        """根据主键 ID 删除记录（默认软删除）"""
        if hard_delete:
            sql = f"DELETE FROM {self.table_name} WHERE {self.primary_key} = %s"
            return self.execute_update(sql, (id_value,))
        else:
            # 软删除
            sql = f"UPDATE {self.table_name} SET is_delete = 1, update_time = %s WHERE {self.primary_key} = %s AND is_delete = 0"
            return self.execute_update(sql, (datetime.now(), id_value))
    
    def count(self, include_deleted: bool = False) -> int:
        """统计记录数"""
        sql = f"SELECT COUNT(*) as count FROM {self.table_name}"
        if not include_deleted:
            sql += " WHERE is_delete = 0"
        results = self.execute_query(sql)
        return results[0]['count'] if results else 0
