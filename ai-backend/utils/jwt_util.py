"""
JWT 工具类
基于 PyJWT 实现，负责 Token 的解析与校验。
封装了用户信息的反序列化，确保 Token 中仅包含非敏感业务字段（排除密码、盐值、AK/SK 等）。
"""
import jwt
from typing import Optional
from models.entity.user import User
from utils.config_util import config
from utils.logger_util import logger
import os


def _get_secret() -> bytes:
    """
    获取签名密钥字节数组
    优先从环境变量 JWT_SECRET 读取，其次从配置读取，最后使用默认值
    
    Returns:
        密钥字节数组
    """
    secret = os.getenv('JWT_SECRET') or config.get('jwt', {}).get('secret', 'ducktodo-secret')
    return secret.encode('utf-8')


def _get_expire_seconds() -> int:
    """
    获取 Token 过期时间（秒）
    优先从环境变量 JWT_EXPIRE_SECONDS 读取，其次从配置读取，最后使用默认值（86400秒，即1天）
    
    Returns:
        过期时间（秒）
    """
    expire_str = os.getenv('JWT_EXPIRE_SECONDS')
    if expire_str:
        try:
            return int(expire_str)
        except ValueError:
            logger.warning(f"Invalid JWT_EXPIRE_SECONDS value: {expire_str}, using default 86400")
            return 86400
    return config.get('jwt', {}).get('expire_seconds', 86400)


def generate_token(user: User, ttl_seconds: Optional[int] = None) -> str:
    """
    生成 JWT Token
    
    Payload 包含字段：
    - uid: 用户ID
    - name: 用户名
    - email: 邮箱
    - phone: 手机号
    - sex: 性别
    - avatar: 头像URL
    - iat: 签发时间
    - exp: 过期时间
    
    Args:
        user: 用户实体（需包含 user_id, user_name 等基础信息）
        ttl_seconds: 过期时间（秒），如果为 None 则使用配置的默认值
    
    Returns:
        签名的 JWT Token 字符串
    
    Raises:
        ValueError: 若 user 为 None
    """
    if user is None:
        raise ValueError("User is None")
    
    if ttl_seconds is None:
        ttl_seconds = _get_expire_seconds()
    
    import time
    now_seconds = int(time.time())
    
    # 构建 Payload
    payload = {
        'uid': user.user_id,
        'name': user.user_name,
        'email': user.user_email,
        'phone': user.user_phone,
        'sex': user.user_sex,
        'avatar': user.user_avatar,
        'iat': now_seconds,
        'exp': now_seconds + ttl_seconds
    }
    
    # 生成 Token
    token = jwt.encode(payload, _get_secret(), algorithm='HS256')
    return token


def parse_token(token: str) -> User:
    """
    解析并验证 Token
    
    会校验签名有效性及是否过期。
    解析成功后，返回仅包含 Payload 中非敏感信息的 User 对象。
    
    Args:
        token: JWT Token 字符串
    
    Returns:
        User 对象（仅包含 user_id, user_name, user_email, user_phone, user_sex, user_avatar）
    
    Raises:
        ValueError: 若 Token 签名无效或已过期
        jwt.InvalidTokenError: 若 Token 格式错误
    """
    if not token:
        raise ValueError("Token is empty")
    
    try:
        # 解析并验证 Token（同时校验签名和过期时间）
        payload = jwt.decode(
            token,
            _get_secret(),
            algorithms=['HS256'],
            options={
                'verify_signature': True,
                'verify_exp': True,
                'verify_iat': True,
                'require_exp': True,
                'require_iat': True
            }
        )
        
        # 提取 Payload 并构建 User 对象
        user = User()
        user.user_id = payload.get('uid')
        user.user_name = payload.get('name')
        user.user_email = payload.get('email')
        user.user_phone = payload.get('phone')
        user.user_sex = payload.get('sex')
        user.user_avatar = payload.get('avatar')
        
        return user
        
    except jwt.ExpiredSignatureError:
        raise ValueError("JWT is expired")
    except jwt.InvalidSignatureError:
        raise ValueError("Invalid JWT signature")
    except jwt.InvalidTokenError as e:
        raise ValueError(f"Invalid JWT token: {str(e)}")
    except Exception as e:
        logger.error(f"Failed to parse JWT token: {e}")
        raise ValueError(f"Failed to parse JWT token: {str(e)}")


def is_valid(token: str) -> bool:
    """
    简单校验 Token 是否有效
    
    检查项：
    1. 格式是否正确
    2. 签名是否正确
    3. 是否在有效期内
    
    Args:
        token: JWT Token 字符串
    
    Returns:
        True 若有效，否则 False
    """
    if not token:
        return False
    
    try:
        jwt.decode(
            token,
            _get_secret(),
            algorithms=['HS256'],
            options={
                'verify_signature': True,
                'verify_exp': True,
                'verify_iat': True
            }
        )
        return True
    except Exception as e:
        logger.debug(f"Token validation failed: {e}")
        return False


def get_user_id_from_token(token: str) -> Optional[str]:
    """
    从 Token 中提取用户ID（不进行完整验证，仅用于快速获取）
    
    Args:
        token: JWT Token 字符串
    
    Returns:
        用户ID，如果解析失败则返回 None
    """
    if not token:
        return None
    
    try:
        # 不验证签名，仅解码 payload（用于快速获取信息）
        # 使用 decode 时设置 verify=False 来跳过验证
        # 注意：即使不验证签名，也需要提供密钥参数（可以是空字符串或任意值）
        payload = jwt.decode(
            token,
            _get_secret(),  # 仍然需要提供密钥，但会跳过验证
            algorithms=['HS256'],
            options={'verify_signature': False, 'verify_exp': False, 'verify_iat': False}
        )
        return payload.get('uid')
    except Exception:
        return None


def get_user_from_header(authorization: Optional[str] = None, token: Optional[str] = None) -> Optional[User]:
    """
    从请求头中获取用户信息
    
    Args:
        authorization: Authorization 头（格式：Bearer <token>）
        token: Token 头（备用）
    
    Returns:
        User 对象，如果获取失败则返回 None
    """
    # 优先从 Authorization 头获取
    if authorization:
        if authorization.startswith("Bearer "):
            token_str = authorization[7:]
        else:
            token_str = authorization
    elif token:
        token_str = token
    else:
        return None
    
    try:
        # 尝试解析 token 获取用户信息
        user = parse_token(token_str)
        return user
    except Exception as e:
        logger.warning(f"Failed to parse token from header: {e}")
        # 如果完整解析失败，返回 None
        return None
