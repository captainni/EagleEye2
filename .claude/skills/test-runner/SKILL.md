---
name: test-runner
description: 自动化 EagleEye2 测试环境准备和浏览器测试流程。当用户说"测试"、"清理环境测试"、"重启测试"、"帮我测一下"等触发时使用。一键完成清空任务日志、清空日志文件、重启所有服务，然后使用 Dev Browser 技能进行浏览器自动化测试。
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

### 5. 完整爬取分析端到端测试
**场景：触发爬虫 → 监控执行 → 智能分析 → 验证结果**

详细步骤请参考 `references/test_scenarios.md` 中的 "完整爬取分析端到端测试" 章节。

**测试流程概览**：
1. 在爬虫管理 → 配置管理中点击"金融界银行动态"的【立即触发】
2. 切换到任务监控，观察爬取执行状态，监控日志
3. 爬取完成后，点击【智能分析】按钮，等待分析完成
4. 验证监管政策/竞品动态页面是否有新卡片生成
5. 检查卡片详情的完整性和准确性

## 浏览器自动化测试

本技能使用 **Dev Browser Skill** 进行浏览器自动化测试。

### 启动 Dev Browser 服务器

```bash
# 方式1：如果已在 dev-browser 安装目录
./server.sh &

# 方式2：从任意位置启动（指定版本号）
cd ~/.claude/plugins/cache/dev-browser-marketplace/dev-browser/66682fb0513a/skills/dev-browser
./server.sh &
```

等待看到 `Ready` 消息后即可开始编写测试脚本。

### 编写测试脚本

所有脚本需在 `skills/dev-browser/` 目录下执行，使用 `@/` 导入别名：

```bash
cd ~/.claude/plugins/cache/dev-browser-marketplace/dev-browser/66682fb0513a/skills/dev-browser

npx tsx <<'EOF'
import { connect, waitForPageLoad } from "@/client.js";

const client = await connect();
const page = await client.page("eagleeye-test", { viewport: { width: 1920, height: 1080 } });

// 导航到页面
await page.goto("http://localhost:8088");
await waitForPageLoad(page);

// 点击元素
await page.evaluate(() => {
  const link = Array.from(document.querySelectorAll('a')).find(l => l.textContent.includes('爬虫管理'));
  if (link) link.click();
});

// 等待并截图
await page.waitForTimeout(2000);
await page.screenshot({ path: "/tmp/screenshot.png" });

// 获取页面信息
const title = await page.title();
console.log("Page Title:", title);

await client.disconnect();
EOF
```

### 常用操作

**导航页面**
```typescript
await page.goto("http://localhost:8088/admin/crawler");
await page.waitForTimeout(2000); // 等待页面加载
```

**点击元素**
```typescript
await page.evaluate(() => {
  const buttons = Array.from(document.querySelectorAll('button'));
  const targetBtn = buttons.find(b => b.textContent.includes('立即触发'));
  if (targetBtn) targetBtn.click();
});
```

**填写表单**
```typescript
await page.evaluate((text) => {
  const input = document.querySelector('input[type="text"]');
  if (input) {
    input.value = text;
    input.dispatchEvent(new Event('input', { bubbles: true }));
  }
}, '搜索关键词');
```

**获取页面内容**
```typescript
const content = await page.evaluate(() => {
  return document.body.textContent?.slice(0, 500);
});
console.log(content);
```

**截图**
```typescript
await page.screenshot({ path: "/tmp/page.png" });       // 整页截图
await page.screenshot({ path: "/tmp/full.png", fullPage: true }); // 完整页面
```

**轮询等待状态**
```typescript
for (let i = 0; i < 20; i++) {
  await page.reload();
  await page.waitForTimeout(3000);

  const status = await page.evaluate(() => {
    // 检查页面状态
    return document.body.textContent?.includes('已完成');
  });

  if (status) break;
}
```

### 核心原则

1. **小脚本原则**：每个脚本只做一件事
2. **状态保持**：页面状态在脚本间保持
3. **描述性命名**：使用有意义的页面名称如 `"login"`, `"crawler-monitor"`
4. **记得断开**：`await client.disconnect()` - 页面会持久化
5. **浏览器上下文**：`page.evaluate()` 中运行的是纯 JavaScript

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
