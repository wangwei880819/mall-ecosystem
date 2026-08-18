# 商城生态运营管理平台 — 阿里云 CentOS 部署手册


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

## 二、功能模块（v2.0）

| 平台 | 路径 | 说明 |
|------|------|------|
| 生态运营管理平台 | /admin | 商户管理、商品管理、订单管理、财务管理、招商CRM、系统管理 |
| C端商城 | /mall | 商品浏览、购物车、下单、支付、个人中心 |
| 风险管控平台 | /risk | 风险监控、事件列表、处置管理、黑名单、规则配置 |
| 商户入驻平台 | /merchant | 商品管理、订单管理、结算管理 |

### 新增功能（v2.0）

- **合同管理**：合同模板管理、平台签署、合同归档
- **保证金管理**：缴纳/退还/扣除审批流程
- **佣金配置**：固定费率/阶梯费率/品类差异化佣金
- **招商CRM**：线索管理、跟进记录、转化漏斗
- **财务增强**：结算单自动佣金计算，从佣金配置读取费率

## 三、阿里云环境要求

| 资源 | 推荐规格 | 用途 |
|------|---------|------|
| ECS | 2核4G, CentOS 7.9+ | 应用服务器 |
| 带宽 | 5Mbps+ | 网站访问 |
| 安全组 | 开放 80 / 443 / 22 | HTTP / HTTPS / SSH |
| MySQL | 云数据库RDS 或 自建 MySQL 8.0 | 数据存储（自建则开 3306） |
| 域名 | 可选，建议绑定 | 生产环境 HTTPS |

## 四、环境安装（CentOS）

### 4.1 安装 Java 17

```bash
yum install -y java-17-openjdk java-17-openjdk-devel

# 验证
java -version
```

### 4.2 安装 MySQL 8.0（如使用自建）

```bash
# 添加 MySQL 官方仓库
rpm -Uvh https://dev.mysql.com/get/mysql80-community-release-el7-3.noarch.rpm

# 安装
yum install -y mysql-community-server

# 启动
systemctl start mysqld
systemctl enable mysqld

# 获取临时密码
grep 'temporary password' /var/log/mysqld.log

# 安全初始化
mysql_secure_installation
```

### 4.3 安装 Nginx

```bash
yum install -y epel-release
yum install -y nginx

# 创建 conf.d 目录（默认可能不存在）
mkdir -p /etc/nginx/conf.d

systemctl start nginx
systemctl enable nginx
```

### 4.4 创建目录结构

```bash
mkdir -p /opt/mall-ecosystem/{backend,frontend/{admin,mall,risk,merchant},uploads}
```

## 五、数据库初始化

### 5.1 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS igou_mall
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'igou'@'localhost' IDENTIFIED BY '12qw!@QW';
GRANT ALL PRIVILEGES ON igou_mall.* TO 'igou'@'localhost';
FLUSH PRIVILEGES;
```

### 5.2 导入表结构和种子数据

```bash
# 首次部署：导入完整 schema + 种子数据
mysql -u igou -p igou_mall < /opt/mall-ecosystem/sql/schema.sql

# 更新部署：导入增量变更脚本（含模拟数据）
mysql -u igou -p igou_mall < /opt/mall-ecosystem/sql/deploy_all.sql
mysql -u igou -p igou_mall < deploy_all.sql
```

### 5.3 配置 DeepSeek API Key（可选，AI功能需要）

```sql
INSERT INTO igou_mall.system_config (`key`, `value`, description)
VALUES ('deepseek.api_key', 'sk-your-api-key', 'DeepSeek API密钥')
ON DUPLICATE KEY UPDATE `value` = 'sk-your-api-key';
```

## 六、部署后端

### 6.1 上传文件

```bash
# 上传 JAR 包
scp deploy/mall-ecosystem-1.0.0.jar root@<ECS_IP>:/opt/mall-ecosystem/backend/

# 上传数据库脚本
scp deploy/sql/*.sql root@<ECS_IP>:/opt/mall-ecosystem/sql/
```

### 6.2 创建配置文件

```bash
cat > /opt/mall-ecosystem/backend/application.yml << 'EOF'
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/igou_mall?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true
    username: igou
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 30000
      connection-timeout: 30000
      max-lifetime: 1800000
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai
    serialization:
      write-dates-as-timestamps: false

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.igou.mall.model.entity
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: true
    lazy-loading-enabled: true
    aggressive-lazy-loading: false

logging:
  level:
    com.igou.mall: INFO
    com.igou.mall.dao: DEBUG

jwt:
  secret: igou_mall_ecosystem_jwt_secret_key_2026
  expiration: 86400000
  refresh-expiration: 604800000

file:
  upload-dir: /opt/mall-ecosystem/uploads
EOF
```

> **注意**：修改 `spring.datasource.password` 和 `jwt.secret` 为实际值。

### 6.3 创建 Systemd 服务

```bash
cat > /etc/systemd/system/mall-ecosystem.service << 'EOF'
[Unit]
Description=Mall Ecosystem Backend
After=network.target mysqld.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/mall-ecosystem/backend
ExecStart=/usr/bin/java -jar -Xmx512m mall-ecosystem-1.0.0.jar --spring.config.location=application.yml
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable mall-ecosystem
systemctl start mall-ecosystem
```

### 6.4 验证

```bash
# 检查启动状态
systemctl status mall-ecosystem
journalctl -u mall-ecosystem -f

# 测试接口
curl http://localhost:8081/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"demo123"}'
```

## 七、部署前端

### 7.1 上传构建产物

```bash
scp -r deploy/frontend/admin/*    root@<ECS_IP>:/opt/mall-ecosystem/frontend/admin/
scp -r deploy/frontend/mall/*     root@<ECS_IP>:/opt/mall-ecosystem/frontend/mall/
scp -r deploy/frontend/risk/*     root@<ECS_IP>:/opt/mall-ecosystem/frontend/risk/
scp -r deploy/frontend/merchant/* root@<ECS_IP>:/opt/mall-ecosystem/frontend/merchant/
```

### 7.2 配置 Nginx

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
    charset utf-8;

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

# 启动 Nginx
systemctl start nginx
systemctl restart nginx
```

## 八、防火墙配置

```bash
# CentOS 7 使用 firewalld
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
firewall-cmd --reload

# 或直接使用 iptables
iptables -I INPUT -p tcp --dport 80 -j ACCEPT
iptables -I INPUT -p tcp --dport 443 -j ACCEPT
service iptables save
```

> **阿里云 ECS 安全组**：还需在阿里云控制台 → ECS → 安全组 → 入方向，添加 80 和 443 端口。

## 九、域名与 HTTPS 配置（可选）

### 9.1 DNS 解析

在域名 DNS 管理中添加 A 记录：

| 主机记录 | 记录类型 | 记录值 |
|---------|---------|--------|
| @ / admin | A | ECS 公网 IP |
| mall | A | ECS 公网 IP |
| risk | A | ECS 公网 IP |
| merchant | A | ECS 公网 IP |

### 9.2 HTTPS 配置（推荐）

```bash
# CentOS 7 安装 certbot
yum install -y epel-release
yum install -y certbot python3-certbot-nginx

# 申请证书
certbot --nginx -d your-domain.com

# 自动续期（crontab）
echo "0 3 * * * certbot renew --quiet && systemctl reload nginx" | crontab -
```

## 十、访问地址

部署完成后，通过以下地址访问：

| 平台 | 地址 | 默认账号 |
|------|------|---------|
| 生态运营管理平台 | `http://<ECS_IP>/admin` | `admin` / `demo123` |
| C端商城 | `http://<ECS_IP>/mall` | 游客可浏览 |
| 风险管控平台 | `http://<ECS_IP>/risk` | `admin` / `demo123` |
| 商户入驻平台 | `http://<ECS_IP>/merchant` | 需注册后登录 |



| 生态运营管理平台/风控稽核管理平台 | `http://101.133.138.189/admin` | `admin` / `demo123` |
| C端商城 | `http://101.133.138.189/mall` | 游客可浏览 |
| 商户入驻平台 | `http://101.133.138.189/merchant` | 15939021497/demo123  | 可申请入驻后、通过管理员审核后登录
http://101.133.138.189/product/list
### 商户入驻平台测试账号

| 手机号 | 商户名称 | 密码 |
|--------|---------|------|
| 13900001111 | 瑞幸咖啡（中国）有限公司 | demo123 |
| 13900002222 | 上海寻梦信息技术有限公司 | demo123 |
| 13900003333 | 深圳腾讯计算机系统有限公司 | demo123 |
| 13900004444 | 阿里巴巴（中国）有限公司 | demo123 |
| 13900005555 | 爱奇艺（北京）科技有限公司 | demo123 |
| 13900006666 | 优酷信息技术（北京）有限公司 | demo123 |

## 十一、运维命令

### 11.1 服务管理

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

# MySQL
systemctl start mysqld
systemctl stop mysqld
systemctl status mysqld
```

### 11.2 更新部署

```bash
# 1. 停止服务
systemctl stop mall-ecosystem

# 2. 替换 JAR
cp new-version.jar /opt/mall-ecosystem/backend/mall-ecosystem-1.0.0.jar

# 3. 替换前端
cp -r frontend-new/dist/* /opt/mall-ecosystem/frontend/admin/

# 4. 启动服务
systemctl start mall-ecosystem
```

### 11.3 日志查看

```bash
# 后端日志
tail -f /opt/mall-ecosystem/backend/logs/*.log
journalctl -u mall-ecosystem -n 100
#查看后台错误日志
journalctl -u mall-ecosystem -n 200 --no-pager | grep -B 2 -A 5 "ERROR\|Caused by\|Exception"

# Nginx 日志
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### 11.4 常用排查

```bash
# 端口占用检查
netstat -tlnp | grep 8081
netstat -tlnp | grep 80

# 进程检查
ps aux | grep java
ps aux | grep nginx

# 内存使用
free -h
top
```

## 十二、构建产物清单

| 文件 | 路径 | 说明 |
|------|------|------|
| 后端 JAR | `deploy/mall-ecosystem-1.0.0.jar` | Spring Boot 应用 |
| 完整 Schema | `deploy/sql/schema.sql` | 首次部署使用 |
| 增量更新脚本 | `deploy/sql/update.sql` | 表结构变更 + 种子数据 |
| 一键部署脚本 | `deploy/sql/deploy_all.sql` | 包含所有脚本 |
| 模拟订单数据 | `deploy/sql/mock_orders.sql` | 100条订单 + 评价 |
| 模拟结算数据 | `deploy/sql/mock_settlements.sql` | 30条结算记录 |
| 模拟商品数据 | `deploy/sql/mock_products.sql` | 商品审核模拟数据 |
| 分类初始化 | `deploy/sql/category_init.sql` | 商品分类数据 |
| 运营平台前端 | `deploy/frontend/admin/` | 生态运营管理平台 |
| C端商城前端 | `deploy/frontend/mall/` | C端商城 |
| 风险管控前端 | `deploy/frontend/risk/` | 风控稽核管理平台 |
| 商户入驻前端 | `deploy/frontend/merchant/` | 商户入驻平台 |

## 十三、一键部署脚本

```bash
#!/bin/bash
# 一键部署脚本 deploy.sh
# 使用方法: chmod +x deploy.sh && ./deploy.sh

set -e

echo "=== 商城生态运营管理平台 一键部署 ==="

# 1. 拷贝文件
echo "[1/4] 拷贝部署文件..."
cp deploy/mall-ecosystem-1.0.0.jar /opt/mall-ecosystem/backend/
cp deploy/sql/*.sql /opt/mall-ecosystem/sql/
cp -r deploy/frontend/admin/* /opt/mall-ecosystem/frontend/admin/
cp -r deploy/frontend/mall/* /opt/mall-ecosystem/frontend/mall/
cp -r deploy/frontend/risk/* /opt/mall-ecosystem/frontend/risk/
cp -r deploy/frontend/merchant/* /opt/mall-ecosystem/frontend/merchant/

# 2. 导入数据库（首次部署用 schema.sql，更新用 deploy_all.sql）
echo "[2/4] 导入数据库..."
mysql -u igou -p igou_mall < /opt/mall-ecosystem/sql/deploy_all.sql

# 3. 重启后端
echo "[3/4] 重启后端服务..."
systemctl restart mall-ecosystem

# 4. 重载 Nginx
echo "[4/4] 重载 Nginx..."
nginx -t && systemctl reload nginx

echo "=== 部署完成 ==="
echo "访问地址: http://$(curl -s ifconfig.me)/admin"
echo "默认账号: admin / demo123"
```
