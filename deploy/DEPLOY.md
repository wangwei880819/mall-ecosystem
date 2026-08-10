# 商城生态运营管理平台 — 阿里云部署手册

## 一、系统架构

```
┌─────────────── 用户浏览器 ───────────────┐
│                                          │
│  Nginx (:80)                             │
│  ├── /admin  → frontend/dist             │  生态运营管理平台
│  ├── /mall   → frontend-customer/dist    │  C端商城
│  ├── /risk   → frontend-risk/dist        │  风险管控平台
│  ├── /merchant → frontend-merchant/dist  │  商户入驻平台
│  ├── /api/   → proxy localhost:8081      │  后端API
│  └── /uploads/ → proxy localhost:8081    │  文件上传
│                                          │
│  Java 17 (systemd: mall-ecosystem)       │
│  └── mall-ecosystem-1.0.0.jar :8081      │
│      └── MySQL 8.0 :3306                 │
└──────────────────────────────────────────┘
```

## 二、阿里云环境要求

| 资源 | 推荐规格 | 用途 |
|------|---------|------|
| ECS | 2核4G, CentOS 7.9+ / Ubuntu 22.04 | 应用服务器 |
| 带宽 | 5Mbps+ | 网站访问 |
| 安全组 | 开放 80 / 443 / 22 | HTTP / HTTPS / SSH |
| MySQL | 云数据库RDS 或 自建 MySQL 8.0 | 数据存储（自建则开 3306） |
| 域名 | 可选，建议绑定 | 生产环境 HTTPS |

## 三、环境安装

### 3.1 安装 Java 17

```bash
# CentOS
yum install -y java-17-openjdk java-17-openjdk-devel

# Ubuntu
apt update && apt install -y openjdk-17-jdk

# 验证
java -version
```

### 3.2 安装 MySQL 8.0（如使用自建）

```bash
# CentOS
yum install -y mysql-server
systemctl start mysqld
systemctl enable mysqld

# 初始化密码
grep 'temporary password' /var/log/mysqld.log
mysql_secure_installation
```

### 3.3 安装 Nginx

```bash
# CentOS
yum install -y nginx

# Ubuntu
apt install -y nginx

systemctl enable nginx
```

### 3.4 创建目录结构

```bash
mkdir -p /opt/mall-ecosystem/{backend,frontend/admin,frontend/mall,frontend/risk,frontend/merchant,uploads}
```

## 四、数据库初始化

### 4.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS igou_mall
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'igou'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON igou_mall.* TO 'igou'@'localhost';
FLUSH PRIVILEGES;
```

### 4.2 导入表结构和种子数据

将 `backend/src/main/resources/mysql-schema.sql` 上传到服务器后执行：

```bash
mysql -u igou -p igou_mall < mysql-schema.sql
```

### 4.3 配置 DeepSeek API Key（可选，AI功能需要）

```sql
INSERT INTO igou_mall.system_config (`key`, `value`, description)
VALUES ('deepseek.api_key', 'sk-your-api-key', 'DeepSeek API密钥')
ON DUPLICATE KEY UPDATE `value` = 'sk-your-api-key';
```

## 五、部署后端

### 5.1 上传文件

```bash
# 上传 JAR 包
scp backend/target/mall-ecosystem-1.0.0.jar root@<ECS_IP>:/opt/mall-ecosystem/backend/

# 上传配置文件
scp backend/src/main/resources/application.yml root@<ECS_IP>:/opt/mall-ecosystem/backend/
```

### 5.2 修改配置文件

编辑 `/opt/mall-ecosystem/backend/application.yml`，修改数据库连接和上传路径：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/igou_mall?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true
    username: igou
    password: your_password     # 修改为实际密码

file:
  upload-dir: /opt/mall-ecosystem/uploads
```

### 5.3 创建 Systemd 服务

```bash
cat > /etc/systemd/system/mall-ecosystem.service << 'EOF'
[Unit]
Description=Mall Ecosystem Backend
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/mall-ecosystem/backend
ExecStart=/usr/bin/java -jar mall-ecosystem-1.0.0.jar --spring.config.location=application.yml
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable mall-ecosystem
systemctl start mall-ecosystem
```

### 5.4 验证

```bash
# 检查启动状态
systemctl status mall-ecosystem
journalctl -u mall-ecosystem -f

# 测试接口
curl http://localhost:8081/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"demo123"}'
```

## 六、部署前端

### 6.1 上传构建产物

```bash
scp -r frontend/dist/*          root@<ECS_IP>:/opt/mall-ecosystem/frontend/admin/
scp -r frontend-customer/dist/* root@<ECS_IP>:/opt/mall-ecosystem/frontend/mall/
scp -r frontend-risk/dist/*     root@<ECS_IP>:/opt/mall-ecosystem/frontend/risk/
scp -r frontend-merchant/dist/* root@<ECS_IP>:/opt/mall-ecosystem/frontend/merchant/
```

### 6.2 配置 Nginx

```bash
cat > /etc/nginx/conf.d/mall-ecosystem.conf << 'NFEOF'
# 后端 API 代理
upstream mall_backend {
    server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name _;  # 改为你的域名

    client_max_body_size 20m;

    # ==================== 生态运营管理平台 ====================
    location /admin {
        alias /opt/mall-ecosystem/frontend/admin;
        index index.html;
        try_files $uri $uri/ /admin/index.html;
    }

    # ==================== C端商城 ====================
    location /mall {
        alias /opt/mall-ecosystem/frontend/mall;
        index index.html;
        try_files $uri $uri/ /mall/index.html;
    }

    # ==================== 风险管控平台 ====================
    location /risk {
        alias /opt/mall-ecosystem/frontend/risk;
        index index.html;
        try_files $uri $uri/ /risk/index.html;
    }

    # ==================== 商户入驻平台 ====================
    location /merchant {
        alias /opt/mall-ecosystem/frontend/merchant;
        index index.html;
        try_files $uri $uri/ /merchant/index.html;
    }

    # ==================== 后端 API 代理 ====================
    location /api/ {
        proxy_pass http://mall_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }

    location /uploads/ {
        proxy_pass http://mall_backend;
        proxy_set_header Host $host;
    }

    # ==================== 根路径重定向 ====================
    location = / {
        return 301 /admin;
    }
}
NFEOF

# 测试配置
nginx -t

# 重启 Nginx
systemctl restart nginx
```

## 七、域名配置（可选）

### 7.1 DNS 解析

在域名 DNS 管理中添加 A 记录：

| 主机记录 | 记录类型 | 记录值 |
|---------|---------|--------|
| @ / admin | A | ECS 公网 IP |
| mall | A | ECS 公网 IP |
| risk | A | ECS 公网 IP |
| merchant | A | ECS 公网 IP |

### 7.2 HTTPS 配置（推荐）

```bash
# 安装 certbot
yum install -y certbot python3-certbot-nginx

# 申请证书
certbot --nginx -d your-domain.com -d mall.your-domain.com -d risk.your-domain.com -d merchant.your-domain.com

# 自动续期
echo "0 3 * * * certbot renew --quiet" | crontab -
```

## 八、访问地址

部署完成后，通过以下地址访问：

| 平台 | 地址 | 默认账号 |
|------|------|---------|
| 生态运营管理平台 | `http://<ECS_IP>/admin` | `admin` / `demo123` |
| C端商城 | `http://<ECS_IP>/mall` | 游客可浏览 |
| 风险管控平台 | `http://<ECS_IP>/risk` | `admin` / `demo123` |
| 商户入驻平台 | `http://<ECS_IP>/merchant` | 需注册 |

## 九、运维命令

```bash
# 后端
systemctl start mall-ecosystem      # 启动
systemctl stop mall-ecosystem       # 停止
systemctl restart mall-ecosystem    # 重启
systemctl status mall-ecosystem     # 状态
journalctl -u mall-ecosystem -f     # 实时日志

# Nginx
nginx -t                            # 测试配置
systemctl restart nginx             # 重启
nginx -s reload                     # 热重载

# 更新部署
# 1. 停止服务
systemctl stop mall-ecosystem
# 2. 替换 JAR
cp new-version.jar /opt/mall-ecosystem/backend/mall-ecosystem-1.0.0.jar
# 3. 替换前端
cp -r frontend-new/dist/* /opt/mall-ecosystem/frontend/admin/
# 4. 启动服务
systemctl start mall-ecosystem
```

## 十、构建产物清单

| 文件 | 路径 | 大小 |
|------|------|------|
| 后端 JAR | `backend/target/mall-ecosystem-1.0.0.jar` | 28MB |
| 运营平台前端 | `frontend/dist/` | - |
| C端商城前端 | `frontend-customer/dist/` | - |
| 风险管控前端 | `frontend-risk/dist/` | - |
| 商户入驻前端 | `frontend-merchant/dist/` | - |
| 数据库脚本 | `backend/src/main/resources/mysql-schema.sql` | - |