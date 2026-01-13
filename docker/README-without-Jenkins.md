# Dockerfile-without-Jenkins 使用说明

## 概述

`Dockerfile-without-Jenkins` 是一个多阶段构建的Dockerfile，可以在不依赖外部Jenkins环境的情况下，直接在Docker容器内完成前后端的构建和打包。

## 特点

- ✅ **完全独立**：不需要外部Jenkins或CI/CD环境
- ✅ **多阶段构建**：优化镜像大小，只保留运行时需要的文件
- ✅ **缓存优化**：利用Docker层缓存，加快构建速度
- ✅ **自动化构建**：在容器内自动完成Maven和npm构建

## 构建阶段说明

### 第一阶段：后端构建（backend-builder）
- 使用 `maven:3.9-eclipse-temurin-21` 镜像
- 构建Spring Boot应用
- 生成JAR文件

### 第二阶段：前端构建（frontend-builder）
- 使用 `node:18-alpine` 镜像
- 安装npm依赖
- 构建Vue.js前端应用
- 生成dist目录

### 第三阶段：运行环境（最终镜像）
- 使用 `nginx:alpine` 作为基础镜像
- 安装JRE和supervisor
- 从前面两个阶段复制构建产物
- 配置Nginx和supervisor

## 使用方法

### 基本构建命令

```bash
# 在项目根目录执行
docker build -f docker/Dockerfile-without-Jenkins -t ducktodo:latest .
```

### 构建参数说明

当前Dockerfile不需要构建参数，所有配置都在Dockerfile内部完成。

### 运行容器

```bash
# 使用docker-compose（推荐）
docker-compose up -d

# 或直接运行
docker run -d \
  --name ducktodo \
  -p 8080:80 \
  -p 8081:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_URL=your-mysql-host \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=DuckTodo \
  -e MYSQL_USERNAME=your-username \
  -e MYSQL_PASSWORD=your-password \
  -e MINIO_ENDPOINT=http://minio:9000 \
  -e MINIO_ACCESS_KEY=your-access-key \
  -e MINIO_SECRET_KEY=your-secret-key \
  -e MINIO_BUCKET=ducktodo \
  -e JWT_SECRET=your-jwt-secret \
  ducktodo:latest
```

## 与原始Dockerfile的区别

| 特性 | Dockerfile（原始） | Dockerfile-without-Jenkins |
|------|-------------------|---------------------------|
| 构建方式 | 依赖外部构建产物 | 容器内完成构建 |
| 需要Jenkins | ✅ 是 | ❌ 否 |
| 需要Maven环境 | ✅ 是 | ❌ 否（容器内） |
| 需要Node.js环境 | ✅ 是 | ❌ 否（容器内） |
| 构建参数 | JAR_FILE, FRONTEND_DIR | 无需参数 |
| 镜像大小 | 较小（只包含运行时） | 稍大（包含构建工具） |
| 构建时间 | 快（使用预构建产物） | 较慢（需要构建） |

## 构建优化建议

### 1. 使用构建缓存

```bash
# 首次构建（会下载依赖）
docker build -f docker/Dockerfile-without-Jenkins -t ducktodo:latest .

# 后续构建（利用缓存，只构建变更部分）
docker build -f docker/Dockerfile-without-Jenkins -t ducktodo:latest .
```

### 2. 使用.dockerignore

在项目根目录创建`.dockerignore`文件，排除不必要的文件：

```
.git
.gitignore
node_modules
target
dist
.env
*.log
README.md
docs
```

### 3. 多阶段构建的优势

- **镜像大小优化**：最终镜像只包含运行时需要的文件
- **安全性**：构建工具不会出现在最终镜像中
- **构建缓存**：每个阶段独立缓存，提高构建效率

## 故障排查

### 构建失败：Maven依赖下载失败

```bash
# 检查网络连接
docker run --rm maven:3.9-eclipse-temurin-21 mvn --version

# 使用国内镜像源（可选）
# 在backend/pom.xml中添加镜像仓库配置
```

### 构建失败：npm install失败

```bash
# 检查Node.js版本兼容性
docker run --rm node:18-alpine node --version

# 清理npm缓存（如果需要）
docker build --no-cache -f docker/Dockerfile-without-Jenkins -t ducktodo:latest .
```

### 运行时错误：找不到JAR文件

```bash
# 检查构建阶段是否成功
docker build --progress=plain -f docker/Dockerfile-without-Jenkins -t ducktodo:latest .

# 检查JAR文件是否存在
docker run --rm ducktodo:latest ls -la /app/
```

## 环境变量配置

容器运行时需要以下环境变量（通过docker-compose.yml或-e参数传入）：

- `SPRING_PROFILES_ACTIVE`: Spring Boot配置环境（默认：prod）
- `MYSQL_URL`: MySQL数据库地址
- `MYSQL_PORT`: MySQL端口（默认：3306）
- `MYSQL_DATABASE`: 数据库名称
- `MYSQL_USERNAME`: 数据库用户名
- `MYSQL_PASSWORD`: 数据库密码
- `MINIO_ENDPOINT`: MinIO服务地址
- `MINIO_ACCESS_KEY`: MinIO访问密钥
- `MINIO_SECRET_KEY`: MinIO秘密密钥
- `MINIO_BUCKET`: MinIO存储桶名称
- `JWT_SECRET`: JWT签名密钥
- `JWT_EXPIRE_SECONDS`: JWT过期时间（秒）

## 注意事项

1. **首次构建时间较长**：需要下载Maven和npm依赖，可能需要10-30分钟
2. **网络要求**：构建过程需要访问Maven中央仓库和npm registry
3. **磁盘空间**：确保有足够的磁盘空间（建议至少5GB）
4. **内存要求**：Maven和npm构建需要足够的内存（建议至少2GB）

## 示例：完整构建和运行流程

```bash
# 1. 克隆项目（如果还没有）
git clone <your-repo-url>
cd DuckTodo

# 2. 构建镜像
docker build -f docker/Dockerfile-without-Jenkins -t ducktodo:latest .

# 3. 创建.env文件（参考env.example）
cp env.example .env
# 编辑.env文件，填入真实的配置值

# 4. 启动服务
docker-compose up -d

# 5. 查看日志
docker-compose logs -f

# 6. 访问应用
# 前端：http://localhost:8080
# 后端API：http://localhost:8081/api
```

## 与Jenkinsfile的对比

### Jenkinsfile方式
```bash
# 需要Jenkins环境
# 1. Jenkins拉取代码
# 2. Jenkins执行 mvn package
# 3. Jenkins执行 npm run build
# 4. Jenkins执行 docker build（使用预构建产物）
```

### Dockerfile-without-Jenkins方式
```bash
# 只需要Docker环境
# 1. docker build（自动完成所有构建步骤）
```

## 总结

`Dockerfile-without-Jenkins` 提供了完全独立的构建方案，适合：
- 本地开发环境
- 没有Jenkins环境的部署场景
- CI/CD环境不可用时的备选方案
- 快速验证和测试

对于生产环境，如果已有Jenkins CI/CD流水线，建议继续使用原始的`Dockerfile`以获得更快的构建速度。
