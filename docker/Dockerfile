FROM nginx:alpine

# 读取配置
ARG JAR_FILE
ARG FRONTEND_DIR

# 安装依赖和初始化目录
RUN set -eux; \
    echo "================ 安装 openjdk21-jre-headless、Python 与初始化 Nginx 目录 ====Ciallo～(∠・ω< )⌒★"; \
    apk add --no-cache \
        openjdk21-jre-headless \
        python3 \
        python3-dev \
        py3-pip \
        build-base; \
    mkdir -p /run/nginx /etc/nginx/conf.d

# 创建app目录（前后端放至/app）
WORKDIR /app

# 复制后端jar包
RUN echo "================ 正在复制后端jar包 ====Ciallo～(∠・ω< )⌒★"
COPY ${JAR_FILE} /app/app.jar

# 复制前端
RUN echo "================ 正在复制前端文件 ====Ciallo～(∠・ω< )⌒★"
RUN rm -rf /usr/share/nginx/html/*
COPY docker/nginx.conf /etc/nginx/nginx.conf
COPY ${FRONTEND_DIR}/ /usr/share/nginx/html/

# 安装和部署 ai-backend
RUN echo "================ 正在安装 ai-backend 依赖 ====Ciallo～(∠・ω< )⌒★"
WORKDIR /app/ai-backend
# 复制 ai-backend 源代码
COPY ai-backend/ ./
# 使用 pip 安装依赖
RUN pip3 install --no-cache-dir \
    fastapi>=0.104.0 \
    "uvicorn[standard]>=0.24.0" \
    pymysql>=1.1.0 \
    langchain>=0.1.0 \
    langchain-openai>=0.1.0 \
    pydantic>=2.0.0 \
    python-dotenv>=1.0.0 \
    numpy>=1.24.0 \
    PyJWT>=2.8.0 && \
    echo "================ ai-backend 依赖安装完成 ====Ciallo～(∠・ω< )⌒★"

# 安装 supervisord 管理进程
RUN apk add --no-cache supervisor
COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# 暴露端口（80: nginx, 8080: springboot, 8000: ai-backend）
EXPOSE 80 8080 8000
# 启动 supervisord
ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisor/conf.d/supervisord.conf"]