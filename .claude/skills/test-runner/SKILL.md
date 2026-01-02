---
name: test-runner
description: 自动化 EagleEye2 测试环境准备和浏览器测试流程。当用户说"测试"、"清理环境测试"、"重启测试"、"帮我测一下"等触发时使用。一键完成清空任务日志、清空日志文件、重启所有服务，然后使用 Playwright MCP 工具进行浏览器测试。
---

# Test Runner

自动化 EagleEye2 测试环境准备和测试流程。

## 快速开始

执行环境准备脚本：

```bash
.claude/skills/test-runner/scripts/setup_test_env.sh
```

脚本会自动完成：
1. 清空 `crawler_task_log` 表
2. 清空所有日志文件
3. 重启所有服务（MySQL、后端、前端、Proxy Service）
4. 显示服务状态和访问地址

## 测试场景

### 1. 爬虫任务执行测试
- 登录后台管理系统
- 进入爬虫管理页面
- 创建新任务
- 监控任务执行状态
- 检查爬取结果

### 2. 竞品/政策分析测试
- 查看竞品分析列表
- 测试分析功能
- 查看政策监控列表
- 验证分析结果展示

### 3. 前端页面交互测试
- 导航各页面
- 测试搜索、筛选功能
- 表单提交验证
- 响应式布局检查

### 4. 完整端到端测试
- 从爬取到分析到需求转化的完整流程
- 数据流转验证
- 业务逻辑正确性检查

## 测试方式

### 默认方式：Playwright MCP 工具

使用 Playwright MCP 系列工具直接操作浏览器：

```bash
# 导航到前端
mcp__playwright__browser_navigate --url "http://localhost:8088"

# 获取页面快照
mcp__playwright__browser_snapshot

# 点击元素
mcp__playwright__browser_click --element "登录按钮" --ref "xxx"

# 填写表单
mcp__playwright__browser_type --element "用户名" --ref "xxx" --text "admin"

# 截图
mcp__playwright__browser_take_screenshot
```

### 用户指定时：Dev Browser 技能

当用户明确说"用dev browser"、"使用dev browser技能"等关键词时，参考 `CLAUDE.md` 中的 **Dev Browser** 章节，使用 dev-browser 技能控制 Windows Chrome 浏览器。

## 服务地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:8088 |
| 后端 API 文档 | http://localhost:9090/api/doc.html |
| Proxy Service | http://localhost:8000/health |

## 资源

### scripts/

- **setup_test_env.sh**: 一键环境准备脚本（核心）

### references/

- **test_scenarios.md**: 详细测试场景和步骤说明

### assets/

本技能不使用 assets 目录，可删除。
