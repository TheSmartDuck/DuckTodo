FROM nginx:alpine

# 读取配置
ARG JAR_FILE
ARG FRONTEND_DIR

# 下载nginx
RUN set -eux; \
    echo "================ 安装 openjdk21-jre-headless 与初始化 Nginx 目录 ====Ciallo～(∠・ω< )⌒★"; \
    apk add --no-cache openjdk21-jre-headless; \
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
COPY ${FRONTEND_DIR} /usr/share/nginx/html


# 安装 supervisord 管理进程
RUN apk add --no-cache supervisor
COPY docker/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# 暴露端口
EXPOSE 80 8080
# 启动 supervisord
ENTRYPOINT ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisor/conf.d/supervisord.conf"]