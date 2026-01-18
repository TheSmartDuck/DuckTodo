"""
统一返回码枚举，用于描述业务/HTTP 状态码。

约定含义：
- OK(200)：请求成功，返回正常数据
- BAD_REQUEST(400)：参数错误或业务校验失败（包括权限不足）
- UNAUTHORIZED(401)：未授权或令牌失效
- NOT_FOUND(404)：资源不存在或已删除
- INTERNAL_ERROR(500)：服务器内部错误

注意：禁止使用 403 状态码，权限相关错误统一使用 BAD_REQUEST(400)
"""
from enum import IntEnum


class ResultCode(IntEnum):
    """统一返回码枚举"""
    OK = 200  # 成功
    BAD_REQUEST = 400  # 请求参数错误或业务校验失败
    UNAUTHORIZED = 401  # 未授权或令牌失效
    NOT_FOUND = 404  # 资源不存在
    INTERNAL_ERROR = 500  # 服务器内部错误
