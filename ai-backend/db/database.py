"""
数据库连接配置和基础操作类
"""
import pymysql
from typing import Optional, List, Dict, Any
from utils.config_util import config


class Database:
    """数据库连接管理类"""
    
    def __init__(self):
        db_config = config['database']
        self.host = db_config['host']
        self.port = db_config['port']
        self.user = db_config['user']
        self.password = db_config['password']
        self.database = db_config['database']
        self.connection: Optional[pymysql.Connection] = None
    
    def connect(self):
        """建立数据库连接"""
        try:
            self.connection = pymysql.connect(
                host=self.host,
                port=self.port,
                user=self.user,
                password=self.password,
                database=self.database,
                charset='utf8mb4',
                cursorclass=pymysql.cursors.DictCursor
            )
            # 设置事务隔离级别为 READ COMMITTED，确保读取最新数据
            # 避免 REPEATABLE READ 隔离级别导致的读取旧数据问题
            cursor = self.connection.cursor()
            try:
                cursor.execute("SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED")
                self.connection.commit()
            finally:
                cursor.close()
            return self.connection
        except Exception as e:
            print(f"数据库连接失败: {e}")
            raise
    
    def close(self):
        """关闭数据库连接"""
        if self.connection:
            self.connection.close()
            self.connection = None
    
    def get_connection(self):
        """获取数据库连接"""
        if not self.connection or not self.connection.open:
            self.connect()
        return self.connection


# 全局数据库实例
db = Database()


class BaseDAO:
    """基础 DAO 类，提供通用的 CRUD 操作"""
    
    def __init__(self, table_name: str):
        self.table_name = table_name
    
    def execute_query(self, sql: str, params: Optional[tuple] = None) -> List[Dict[str, Any]]:
        """执行查询语句"""
        conn = db.get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute(sql, params)
            return cursor.fetchall()
        finally:
            cursor.close()
    
    def execute_update(self, sql: str, params: Optional[tuple] = None) -> int:
        """执行更新语句（INSERT/UPDATE/DELETE）"""
        conn = db.get_connection()
        cursor = conn.cursor()
        try:
            cursor.execute(sql, params)
            conn.commit()
            return cursor.rowcount
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            cursor.close()
    
    def find_by_id(self, id: int) -> Optional[Dict[str, Any]]:
        """根据 ID 查询"""
        sql = f"SELECT * FROM {self.table_name} WHERE id = %s"
        results = self.execute_query(sql, (id,))
        return results[0] if results else None
    
    def find_all(self, limit: Optional[int] = None, offset: int = 0) -> List[Dict[str, Any]]:
        """查询所有记录"""
        sql = f"SELECT * FROM {self.table_name}"
        if limit:
            sql += f" LIMIT {limit} OFFSET {offset}"
        return self.execute_query(sql)
    
    def insert(self, data: Dict[str, Any]) -> int:
        """插入记录"""
        keys = list(data.keys())
        values = list(data.values())
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
    
    def update_by_id(self, id: int, data: Dict[str, Any]) -> int:
        """根据 ID 更新记录"""
        keys = list(data.keys())
        values = list(data.values())
        set_clause = ', '.join([f"{key} = %s" for key in keys])
        sql = f"UPDATE {self.table_name} SET {set_clause} WHERE id = %s"
        params = tuple(values) + (id,)
        return self.execute_update(sql, params)
    
    def delete_by_id(self, id: int) -> int:
        """根据 ID 删除记录"""
        sql = f"DELETE FROM {self.table_name} WHERE id = %s"
        return self.execute_update(sql, (id,))
