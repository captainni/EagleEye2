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
