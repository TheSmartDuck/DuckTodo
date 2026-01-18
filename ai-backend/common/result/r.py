"""
通用返回类 R（支持泛型数据载荷）。

统一后端返回结构，约定字段：
- success：是否成功
- code：业务/HTTP 状态码（默认 200/400/500 等）
- message：提示信息
- data：业务数据（泛型）
- timestamp：服务器时间戳（毫秒）
"""
from typing import Generic, TypeVar, Optional
from datetime import datetime
from pydantic import BaseModel, Field
from common.enums.result_code import ResultCode

T = TypeVar('T')


class R(BaseModel, Generic[T]):
    """
    通用返回类 R（支持泛型数据载荷）
    
    统一后端返回结构，约定字段：
    - success：是否成功
    - code：业务/HTTP 状态码（默认 200/400/500 等）
    - message：提示信息
    - data：业务数据（泛型）
    - timestamp：服务器时间戳（毫秒）
    """
    
    success: bool = Field(default=False, description="是否成功")
    code: int = Field(default=200, description="业务/HTTP 状态码")
    message: str = Field(default="", description="提示信息")
    data: Optional[T] = Field(default=None, description="业务数据")
    timestamp: int = Field(default_factory=lambda: int(datetime.now().timestamp() * 1000), description="服务器时间戳（毫秒）")
    
    class Config:
        from_attributes = True
    
    # ===== 工厂方法（成功） =====
    @classmethod
    def ok(cls, data: Optional[T] = None, message: Optional[str] = None) -> "R[T]":
        """
        成功返回
        
        Args:
            data: 业务数据（可选）
            message: 提示信息（可选，默认为 "OK"）
        
        Returns:
            R 实例
        
        使用示例：
            R.ok()  # 无数据，默认消息 "OK"
            R.ok(data={"key": "value"})  # 有数据，默认消息 "OK"
            R.ok(data={"key": "value"}, message="操作成功")  # 有数据和自定义消息
        """
        if message is None:
            message = "OK"
        
        return cls(
            success=True,
            code=ResultCode.OK,
            message=message,
            data=data,
            timestamp=int(datetime.now().timestamp() * 1000)
        )
    
    @classmethod
    def ok_msg(cls, message: str) -> "R[T]":
        """
        成功返回（仅消息）
        
        Args:
            message: 提示信息
        
        Returns:
            R 实例
        """
        return cls(
            success=True,
            code=ResultCode.OK,
            message=message,
            data=None,
            timestamp=int(datetime.now().timestamp() * 1000)
        )
    
    # ===== 工厂方法（失败/异常） =====
    @classmethod
    def fail(cls, message: str, code: Optional[int] = None) -> "R[T]":
        """
        失败返回
        
        Args:
            message: 错误信息
            code: 错误码（可选，默认使用 BAD_REQUEST）
        
        Returns:
            R 实例
        """
        if code is None:
            code = ResultCode.BAD_REQUEST
        
        return cls(
            success=False,
            code=code,
            message=message,
            data=None,
            timestamp=int(datetime.now().timestamp() * 1000)
        )
    
    @classmethod
    def fail_with_code(cls, result_code: ResultCode, message: str) -> "R[T]":
        """
        失败返回（枚举版）：使用统一的 ResultCode 管理码值
        
        Args:
            result_code: ResultCode 枚举值
            message: 错误信息
        
        Returns:
            R 实例
        """
        return cls.fail(message=message, code=result_code)
    
    @classmethod
    def error(cls, message: str) -> "R[T]":
        """
        服务器内部错误
        
        Args:
            message: 错误信息
        
        Returns:
            R 实例
        """
        return cls.fail_with_code(ResultCode.INTERNAL_ERROR, message)
    
    @classmethod
    def unauthorized(cls, message: str) -> "R[T]":
        """
        未授权错误
        
        Args:
            message: 错误信息
        
        Returns:
            R 实例
        """
        return cls.fail_with_code(ResultCode.UNAUTHORIZED, message)
    
    @classmethod
    def not_found(cls, message: str) -> "R[T]":
        """
        资源不存在错误
        
        Args:
            message: 错误信息
        
        Returns:
            R 实例
        """
        return cls.fail_with_code(ResultCode.NOT_FOUND, message)
    
    @classmethod
    def forbidden(cls, message: str) -> "R[T]":
        """
        权限不足（已废弃，禁止使用 403）
        权限相关错误统一使用 fail() 返回 BAD_REQUEST(400)
        
        Args:
            message: 错误信息
        
        Returns:
            R 实例
        
        Deprecated:
            使用 R.fail(message) 替代
        """
        # 改为返回 BAD_REQUEST(400) 而不是 FORBIDDEN(403)
        return cls.fail_with_code(ResultCode.BAD_REQUEST, message)
    
    # ===== 链式补充 =====
    def with_data(self, data: T) -> "R[T]":
        """
        链式设置数据
        
        Args:
            data: 业务数据
        
        Returns:
            self
        """
        self.data = data
        return self
    
    def with_message(self, message: str) -> "R[T]":
        """
        链式设置消息
        
        Args:
            message: 提示信息
        
        Returns:
            self
        """
        self.message = message
        return self
    
    def with_code(self, code: int) -> "R[T]":
        """
        链式设置错误码
        
        Args:
            code: 错误码
        
        Returns:
            self
        """
        self.code = code
        return self
