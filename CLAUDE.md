# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 请始终用中文与用户对话！

## 项目概述

EagleEye2 是一个金融资讯智能跟踪平台，采用前后端分离架构。

**技术栈：**
- 后端：Spring Boot 2.7.17 + MyBatis Plus + MySQL + Redis + RabbitMQ
- 前端：Vue 3 + TypeScript + Element Plus + Vite
- API 文档：Knife4j (http://localhost:9090/api/doc.html)

## 开发命令

### 后端（基于 Maven）
```bash
# 构建项目
./mvnw clean package

# 运行应用（开发模式）
./mvnw spring-boot:run

# 运行测试
./mvnw test

# 跳过测试构建
./mvnw clean package -DskipTests
```

### 前端（位于 web/ 目录）
```bash
cd web

# 安装依赖
npm install

# 启动开发服务器（端口 8088）
npm run dev

# 构建生产版本
npm run build

# 预览构建结果
npm run preview

# 代码检查和修复
npm run lint

# TypeScript 类型检查
npx vue-tsc --noEmit
```

### 端口配置
- 后端 API：9090
- 前端开发服务器：8088
- 前端 API 代理：`/api` -> `http://localhost:9090/api`

## 代码架构

### 后端结构
```
src/main/java/com/eagleeye/
├── common/api/          # 通用 API 封装（CommonResult 响应格式）
├── config/              # 配置类（Spring Security、Redis、RabbitMQ 等）
├── controller/          # 控制器层
│   ├── common/         # 通用控制器
│   ├── competitor/     # 竞品追踪
│   ├── crawler/        # 爬虫管理
│   ├── dashboard/      # 仪表盘
│   ├── policy/         # 政策监控
│   └── requirement/    # 需求管理
├── model/              # 数据模型
│   ├── dto/            # 数据传输对象
│   ├── entity/         # 实体类（对应数据库表）
│   └── vo/             # 视图对象
├── repository/         # 数据访问层（MyBatis Plus）
├── service/            # 服务层（业务逻辑）
└── util/               # 工具类
```

### 前端结构
```
web/src/
├── api/                # API 请求模块
├── components/         # 可复用组件
│   ├── admin/          # 管理端组件（爬虫管理）
│   ├── common/         # 通用组件
│   ├── competitor/     # 竞品组件
│   ├── dashboard/      # 仪表盘组件
│   ├── layout/         # 布局组件
│   ├── policy/         # 政策组件
│   ├── requirement/    # 需求组件
│   └── settings/       # 设置组件
├── pages/              # 页面组件
├── router/             # 路由配置
├── services/           # 业务服务
├── stores/             # Pinia 状态管理
├── types/              # TypeScript 类型定义
└── utils/              # 工具函数
```

## 核心业务模块

1. **监管政策监控**：实时抓取央行、银保监会等权威渠道的政策信息
2. **竞品动态追踪**：监控同业产品发布、营销活动、财报等
3. **需求池管理**：将有价值的分析建议转化为产品需求
4. **仪表盘**：展示关键指标和汇总信息
5. **爬虫管理**（管理员）：配置爬虫任务和规则

## 开发规范

### API 响应格式
所有 API 返回统一格式的响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 分层架构
- **Controller 层**：处理 HTTP 请求，参数验证
- **Service 层**：业务逻辑处理
- **Repository 层**：数据访问（基于 MyBatis Plus）

### 数据库约定
- 字段命名：snake_case（如 `create_time`）
- 支持逻辑删除（`deleted` 字段）
- 自动填充：创建时间、更新时间

### 前端路径别名
- `@/` 指向 `web/src/`

## 环境配置

- 开发环境配置：`src/main/resources/application-dev.yml`
- 生产环境配置：`src/main/resources/application-prod.yml`
- 默认激活配置：`application.yml` 中 `spring.profiles.active`

## 注意事项

1. **MyBatis Plus**：使用其自动填充、逻辑删除等功能
2. **Redis**：用于缓存和会话管理
3. **RabbitMQ**：用于异步处理爬虫任务等耗时操作
4. **Spring Security**：API 认证和授权
5. **前端路由**：API 请求通过 Vite 代理转发到后端

---

## 开发环境启动命令

### 1. Docker MySQL 数据库
```bash
# 启动 MySQL 容器（如果未运行）
docker start my-mysql

# 查看状态
docker ps | grep mysql

# 数据库连接信息
# Host: localhost:3306
# User: captain / Pass: 123456
# Database: eagleeye
```

### 2. 后端服务 (Spring Boot)
```bash
# 项目根目录执行
mvn spring-boot:run

# 或后台运行
nohup mvn spring-boot:run > logs/backend.log 2>&1 &

# 停止服务
ps aux | grep "spring-boot:run" | grep -v grep | awk '{print $2}' | xargs -r kill

# 查看日志
tail -f logs/backend.log

# 端口: 9090
# API 文档: http://localhost:9090/api/doc.html
```

### 3. 前端服务 (Vue 3 + Vite)
```bash
cd web
npm run dev

# 或后台运行
nohup npm run dev > ../logs/frontend.log 2>&1 &

# 停止服务
ps aux | grep "vite" | grep web | grep -v grep | awk '{print $2}' | xargs -r kill

# 端口: 8088 (如果被占用会自动使用 8089, 8090...)
# 访问: http://localhost:8088
```

### 4. Proxy Service (FastAPI - Claude Code Skills 代理)
```bash
cd /home/captain/projects/EagleEye2
source venv/bin/activate  # 激活虚拟环境
python proxy-service/main.py

# 或后台运行
nohup ./venv/bin/python proxy-service/main.py > logs/proxy.log 2>&1 &

# 停止服务
ps aux | grep "proxy-service/main.py" | grep -v grep | awk '{print $2}' | xargs -r kill

# 端口: 8000
# Health: http://localhost:8000/health

# 日志目录
# logs/proxy.log - Proxy Service 日志
# logs/claude-cli.log - Claude Code CLI 执行日志
```

### 5. 快捷启动脚本（推荐）

```bash
# 一键启动所有服务
./scripts/start-all.sh

# 一键停止所有服务
./scripts/stop-all.sh

# 一键重启所有服务
./scripts/restart-all.sh

# 查看服务状态
./scripts/status.sh
```

**脚本特性：**
- 自动创建 logs 目录
- 防止重复启动
- 统一日志输出到 `logs/` 目录
- 彩色状态显示

### 6. 手动启动顺序（逐步启动）

```bash
# 1. 启动 MySQL
docker start my-mysql

# 2. 启动后端
mvn spring-boot:run &

# 3. 启动前端
cd web && npm run dev &

# 4. 启动 proxy-service
source venv/bin/activate
python proxy-service/main.py &
```

### 7. 数据库清理命令
```bash
# 清空任务日志表
docker exec my-mysql mysql --default-character-set=utf8mb4 -ucaptain -p123456 eagleeye -e "DELETE FROM crawler_task_log;"

# 查询任务数量
docker exec my-mysql mysql --default-character-set=utf8mb4 -ucaptain -p123456 eagleeye -e "SELECT COUNT(*) FROM crawler_task_log;"
```

---

## 故障排查

### 后端无法启动
- 检查 MySQL 是否运行: `docker ps | grep mysql`
- 检查端口占用: `netstat -tlnp | grep 9090`
- 查看后端日志: `tail -100 logs/backend.log`

### 前端无法访问后端 API
- 检查 CORS 配置: `src/main/java/com/eagleeye/config/CorsConfig.java`
- 确认前端代理配置: `web/vite.config.ts`
- 检查后端是否运行: `curl http://localhost:9090/api/health`

### Proxy Service 无响应
- 检查进程: `ps aux | grep proxy-service`
- 查看 proxy 日志: `tail -100 logs/proxy.log`
- 查看 Claude CLI 日志: `tail -100 logs/claude-cli.log`
- 测试健康检查: `curl http://localhost:8000/health`

### 爬虫任务显示失败或文章数为 0
- 检查 proxy-service 是否运行
- 检查 Claude Code CLI 是否可用: `claude --version`
- 查看爬取结果目录: `ls -la crawl_files/`
- 查看 Claude CLI 日志: `tail -100 logs/claude-cli.log`
- 查看后端日志中的错误: `tail -100 logs/backend.log | grep -i error`

---

## Dev Browser 技能 - 浏览器自动化工具

Dev Browser 是已安装的浏览器自动化插件，可以通过 Claude 控制 Windows Chrome 浏览器。

### 使用方法

直接用自然语言告诉 Claude 要做什么：

```bash
# 示例指令
帮我用dev browser技能： 
打开 https://github.com
访问百度并截图
使用扩展打开 http://localhost:8088
登录这个网站并提取数据
测试前端登录功能
```

### 关键词

- **打开/访问/进入** 网址
- **截图**
- **点击/填写** 表单
- **登录** 网站
- **测试** 页面
- **提取** 数据

### 启动扩展模式（持久化后台运行）

```bash
# 启动中继服务器（后台运行，关闭终端也不会停止）
cd ~/.claude/plugins/cache/dev-browser-marketplace/dev-browser/*/skills/dev-browser
nohup npm run start-extension > /tmp/dev-browser-relay.log 2>&1 &

# 查看日志
tail -f /tmp/dev-browser-relay.log

# 停止服务
pkill -f "start-relay"
```

然后在 Windows Chrome 中激活 Dev Browser 扩展。扩展会自动连接并保持连接状态。

