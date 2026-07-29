# 商城生态运营系统 — 部署文档

## 一、项目概述

商城生态运营系统包含 **1 个后端服务** + **4 个前端应用**：

| 模块 | 技术栈 | 端口 | 说明 |
|------|--------|------|------|
| `backend` | Spring Boot 3.2.0 + MyBatis + MySQL | **8081** | 统一后端服务 |
| `frontend` | Vue 3 + Element Plus + Vite | **5173** | B端管理后台（运营/审核/系统） |
| `frontend-customer` | Vue 3 + Pinia + Element Plus + Vite | **3000** | C端用户商城 |
| `frontend-merchant` | Vue 3 + Element Plus + Pinia + Vite | **3002** | 商户入驻平台 |
| `frontend-risk` | Vue 3 + Element Plus + ECharts + Vite | **3001** | 风控稽核管理平台 |

---

## 二、环境要求

| 依赖 | 最低版本 | 说明 |
|------|----------|------|
| JDK | 17+ | Spring Boot 3.2 要求 |
| Maven | 3.6+ | 后端构建 |
| Node.js | 18+ | 前端构建（推荐 20 LTS） |
| MySQL | 8.0+ | 数据库 |

---

## 三、数据库初始化

### 3.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS igou_mall
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 3.2 执行建表脚本（按顺序）

```bash
# 1. 核心业务表（25张表 + 种子数据）
mysql -u root -p igou_mall < database/mysql-schema.sql

# 2. RBAC权限表（4张表 + 角色/菜单种子数据）
mysql -u root -p igou_mall < database/rbac-schema.sql
```

### 3.3 修改数据库连接配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/igou_mall?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true
    username: root      # 修改为实际用户名
    password: root      # 修改为实际密码
```

---

## 四、后端服务部署

### 4.1 开发环境启动

```bash
cd backend
mvn spring-boot:run
```

### 4.2 生产环境打包部署

```bash
cd backend

# 打包（跳过测试）
mvn clean package -DskipTests

# 启动
nohup java -jar target/mall-ecosystem-1.0.0.jar > /tmp/backend.log 2>&1 &
```

### 4.3 验证

```bash
curl http://localhost:8081/api/auth/sso/platforms
```

---

## 五、前端应用部署

### 5.1 安装依赖

```bash
# B端管理后台
cd frontend && npm install

# C端商城
cd frontend-customer && npm install

# 商户入驻平台
cd frontend-merchant && npm install

# 风控稽核管理平台
cd frontend-risk && npm install
```

### 5.2 开发环境启动

每个前端项目独立启动，推荐在 4 个终端窗口中分别运行：

```bash
# 终端1：B端管理后台 (http://localhost:5173)
cd frontend && npm run dev

# 终端2：C端商城 (http://localhost:3000)
cd frontend-customer && npm run dev

# 终端3：商户入驻平台 (http://localhost:3002)
cd frontend-merchant && npm run dev

# 终端4：风控稽核管理平台 (http://localhost:3001)
cd frontend-risk && npm run dev
```

### 5.3 生产环境构建

```bash
# B端管理后台
cd frontend && npm run build          # 产物在 frontend/dist/

# C端商城
cd frontend-customer && npm run build # 产物在 frontend-customer/dist/

# 商户入驻平台
cd frontend-merchant && npm run build # 产物在 frontend-merchant/dist/

# 风控稽核管理平台
cd frontend-risk && npm run build     # 产物在 frontend-risk/dist/
```

> 构建产物部署到 Nginx 的配置见「六、Nginx 部署」章节。

---

## 六、Nginx 部署（生产环境推荐）

### 6.1 部署架构

```
                     ┌─────────────────┐
                     │     Nginx       │
                     │  (反向代理)      │
                     └───────┬─────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
   / → frontend/dist   /customer → c-dist   /merchant → m-dist
   /risk → risk-dist   /api → backend:8081
```

### 6.2 Nginx 配置示例

```nginx
server {
    listen 80;
    server_name _;

    # B端管理后台
    location / {
        root /opt/mall-ecosystem/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # C端商城
    location /customer/ {
        alias /opt/mall-ecosystem/frontend-customer/dist/;
        index index.html;
        try_files $uri $uri/ /customer/index.html;
    }

    # 商户入驻平台
    location /merchant/ {
        alias /opt/mall-ecosystem/frontend-merchant/dist/;
        index index.html;
        try_files $uri $uri/ /merchant/index.html;
    }

    # 风控稽核管理平台
    location /risk/ {
        alias /opt/mall-ecosystem/frontend-risk/dist/;
        index index.html;
        try_files $uri $uri/ /risk/index.html;
    }

    # 后端API代理
    location /api/ {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 文件上传代理
    location /uploads/ {
        proxy_pass http://127.0.0.1:8081;
    }

    # 请求大小限制（支持文件上传）
    client_max_body_size 10m;
}
```

---

## 七、一键部署脚本

创建 `deploy.sh`：

```bash
#!/bin/bash
set -e

echo "======== 商城生态运营系统 一键部署 ========"

# 1. 构建后端
echo "[1/5] 构建后端服务..."
cd backend
mvn clean package -DskipTests -q
echo "  ✓ 后端构建完成"

# 2. 安装前端依赖
echo "[2/5] 安装前端依赖..."
cd ../frontend && npm install --silent
cd ../frontend-customer && npm install --silent
cd ../frontend-merchant && npm install --silent
cd ../frontend-risk && npm install --silent
echo "  ✓ 前端依赖安装完成"

# 3. 构建前端
echo "[3/5] 构建前端应用..."
cd ../frontend && npm run build --silent
cd ../frontend-customer && npm run build --silent
cd ../frontend-merchant && npm run build --silent
cd ../frontend-risk && npm run build --silent
echo "  ✓ 前端构建完成"

# 4. 启动后端
echo "[4/5] 启动后端服务..."
cd ../backend
pkill -f mall-ecosystem || true
nohup java -jar target/mall-ecosystem-1.0.0.jar > /tmp/backend.log 2>&1 &
sleep 5
echo "  ✓ 后端服务已启动 (端口 8081)"

# 5. 部署Nginx静态文件
echo "[5/5] 部署前端静态文件..."
# 根据实际Nginx配置，将dist目录复制到对应位置
echo "  ✓ 部署完成"

echo ""
echo "=========================================="
echo "  部署完成！访问地址："
echo "  B端管理后台:   http://localhost:5173"
echo "  C端商城:       http://localhost:3000"
echo "  商户入驻平台:   http://localhost:3002"
echo "  风控稽核平台:   http://localhost:3001"
echo "=========================================="
```

---

## 八、默认账号

| 用户名 | 密码 | 角色 | 可登录平台 |
|--------|------|------|-----------|
| `admin` | `demo123` | 超级管理员 | 全部 7 个平台 |
| `operator01` | `demo123` | 运营专员 | 1,2,3 |
| `auditor01` | `demo123` | 稽核专员 | 1,3,5 |

---

## 九、端口汇总

| 服务 | 端口 | 访问地址 |
|------|------|----------|
| 后端 API | 8081 | `http://localhost:8081` |
| B端管理后台 | 5173 | `http://localhost:5173` |
| C端商城 | 3000 | `http://localhost:3000` |
| 商户入驻平台 | 3002 | `http://localhost:3002` |
| 风控稽核管理平台 | 3001 | `http://localhost:3001` |

> **注意**：所有前端通过 Vite 代理将 `/api` 和 `/uploads` 请求转发至 `http://localhost:8081`，因此开发环境下只需启动后端 + 目标前端即可。
