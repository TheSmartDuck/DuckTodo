pipeline {
  agent any
  options { timestamps() }
  environment {
    DOCKER_IMAGE = 'ducktodo:latest'
  }
  parameters {
    string(name: 'GIT_URL', defaultValue: 'https://your-git-server.com/your-org/DuckTodo.git', description: 'git地址')
    string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'git分支')
    string(name: 'GITEA_CREDENTIALS_ID', defaultValue: 'git-credentials-id', description: 'git账号用户名密码凭证id')
    string(name: 'HARBOR_REGISTRY', defaultValue: 'your-harbor-registry.com', description: 'Harbor注册地址')
    string(name: 'HARBOR_PROJECT', defaultValue: 'ducktodo', description: 'Harbor项目')
    string(name: 'HARBOR_REPO', defaultValue: 'ducktodo', description: 'Harbor仓库名')
    string(name: 'HARBOR_CREDENTIALS_ID', defaultValue: 'harbor-credentials-id', description: 'Harbor用户名密码凭证id')
    string(name: 'IMAGE_TAG', defaultValue: 'v1.0', description: '镜像标签')
  }
  stages {
    stage('Checkout') {
      steps {
        echo "============ 正在拉取分支: ${params.GIT_BRANCH} ====Ciallo～(∠・ω< )⌒★"
        checkout scmGit(branches: [[name: "*/${params.GIT_BRANCH}"]], extensions: [], userRemoteConfigs: [[credentialsId: "${params.GITEA_CREDENTIALS_ID}", url: "${params.GIT_URL}"]])
        echo "============ 拉取分支完成: ${params.GIT_BRANCH} ====Ciallo～(∠・ω< )⌒★"
      }
    }
    stage('Build Backend') {
      steps {
        echo "============ 正在构建后端 ====Ciallo～(∠・ω< )⌒★"
        dir('backend') {
          sh 'mvn -DskipTests -Dspring.profiles.active=prod clean package'
        }
        echo "============ 构建后端完成 ====Ciallo～(∠・ω< )⌒★"
      }
    }
    stage('Build Frontend') {
      steps {
        echo "============ 正在构建前端 ====Ciallo～(∠・ω< )⌒★"
        dir('frontend') {
          sh 'npm install --legacy-peer-deps'
          sh 'npm run build'
        }
        echo "============ 构建前端完成 ====Ciallo～(∠・ω< )⌒★"
      }
    }
    stage('Install AI Backend Dependencies') {
      steps {
        echo "============ 正在安装 ai-backend 依赖 ====Ciallo～(∠・ω< )⌒★"
        dir('ai-backend') {
          sh '''
            if ! command -v uv &> /dev/null; then
              echo "正在安装 uv..."
              curl -LsSf https://astral.sh/uv/install.sh | sh
              export PATH="$HOME/.cargo/bin:$PATH"
            fi
            uv sync --frozen --no-dev
          '''
        }
        echo "============ ai-backend 依赖安装完成 ====Ciallo～(∠・ω< )⌒★"
      }
    }
    stage('Build Docker Image') {
      steps {
        echo "============ 正在构建Docker镜像 ====Ciallo～(∠・ω< )⌒★"
        script {
          def backEndJarPath = sh(script: "ls backend/target/*.jar | head -n 1", returnStdout: true).trim()
          def frontEndDir = 'frontend/dist'
          def dockerImage = env.DOCKER_IMAGE

          sh "docker build -f docker/Dockerfile --build-arg JAR_FILE=${backEndJarPath} --build-arg FRONTEND_DIR=${frontEndDir} -t ${dockerImage} ."
        }
        echo "============ 构建Docker镜像完成 ====Ciallo～(∠・ω< )⌒★"
      }
    }
    stage('Push Docker Image to Harbor') {
      steps {
        echo "============ 正在推送镜像至Harbor ====Ciallo～(∠・ω< )⌒★"
        script {
          def harborImage = "${params.HARBOR_REGISTRY}/${params.HARBOR_PROJECT}/${params.HARBOR_REPO}:${params.IMAGE_TAG}"
          sh "docker tag ${env.DOCKER_IMAGE} ${harborImage}"
          withCredentials([usernamePassword(credentialsId: params.HARBOR_CREDENTIALS_ID, usernameVariable: 'HARBOR_USER', passwordVariable: 'HARBOR_PASS')]) {
            sh "echo \$HARBOR_PASS | docker login ${params.HARBOR_REGISTRY} -u \$HARBOR_USER --password-stdin"
          }
          sh "docker push ${harborImage}"
          echo "============ 已推送至Harbor，正在删除本地该版本镜像 ====Ciallo～(∠・ω< )⌒★"
          sh "docker rmi ${harborImage} || true"
          sh "docker rmi ${env.DOCKER_IMAGE} || true"
        }
        echo "============ 推送镜像至Harbor完成 ====Ciallo～(∠・ω< )⌒★"
      }
    }
  }
}
