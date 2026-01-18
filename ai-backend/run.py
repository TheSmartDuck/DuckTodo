"""
启动脚本
使用 uvicorn 启动 FastAPI 应用
"""
import uvicorn
from utils.config_util import config

if __name__ == "__main__":
    server_config = config['server']
    host = server_config['host']
    port = server_config['port']
    
    uvicorn.run(
        "api.app:app",
        host=host,
        port=port,
        reload=True,  # 开发环境自动重载
        log_level="info"
    )
