---
name: eagleeye-crawler
description: EagleEye2 智能爬虫工具，用于爬取金融资讯网站文章列表并生成结构化 Markdown 文件。支持政策类(policy)和竞品类(competitor)文章分类。当用户需要爬取网站文章列表、提取文章内容并生成结构化文档时触发此 skill。
---

# EagleEye2 智能爬虫

## 概述

此 skill 提供智能爬虫功能，可以从金融资讯网站爬取文章列表，提取文章内容，并生成结构化的 Markdown 文件。使用 AI 分析页面结构，无需配置 CSS 选择器。

**核心特性：**
- **智能降级策略**：优先使用 webReader（快速），失败时自动切换到 Dev Browser（兼容性强）
- **动态网站检测**：自动识别 Angular.js、React 等动态渲染网站
- **URL 预处理**：自动清理和规范化 URL，提高成功率

## 参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `listUrl` | string | 是 | - | 文章列表页面 URL |
| `sourceName` | string | 是 | - | 来源标识（小写字母、数字、下划线） |
| `maxArticles` | number | 否 | 3 | 最大爬取文章数（1-20） |
| `outputDir` | string | 否 | `/home/captain/projects/EagleEye2/crawl_files` | 输出目录 |

**注意**：`category` 分类字段由 **AI 自动分析判定**，无需用户指定。AI 会根据每篇文章内容判定是 policy（政策类）、competitor（竞品类）还是 other（其他）。

## 执行流程

### 阶段 1：URL 预处理
1. **清理 URL**：
   - 移除锚点（`#` 及后面的内容）
   - 规范化 URL 编码
   - 验证协议（只支持 http/https）

### 阶段 2：智能降级策略

**步骤 1：尝试 webReader（快速模式）**
- 调用 webReader MCP tool 获取页面内容
- 检查返回内容质量：
  - 内容长度 > 1000 字符
  - 包含有效的文章链接
- 如果成功，使用 webReader 完成后续任务
- 如果失败，进入步骤 2

**步骤 2：使用 Dev Browser（兼容模式）**
- 启动 Dev Browser 服务器（如果未运行）
- 使用 Playwright 获取完整的 JavaScript 渲染内容
- 等待动态内容加载完成
- 提取文章列表和详情

### 阶段 3：动态网站检测

根据以下特征自动识别动态网站：
- URL 域名在已知动态网站列表中（如 `nfra.gov.cn`）
- 页面包含 `ng-app`、`data-reactroot` 等特征
- webReader 返回内容但无有效链接

### 阶段 4：批量爬取文章
For 每篇文章：
- 使用相同策略获取文章详情（webReader → Dev Browser 降级）
- **AI 判定分类**：分析文章内容，判定是 policy（政策）、competitor（竞品）还是 other（其他）
- 提取结构化内容（标题、作者、发布时间、正文、标签、分类）
- 生成 Markdown 文件（文件名含分类标识）
- 保存到批次文件夹

### 阶段 5：生成元数据
- 创建 summary.md
- 生成 metadata.json 文件

## 已知动态网站列表

以下网站已知使用 JavaScript 动态渲染，将直接使用 Dev Browser：

| 域名 | 原因 |
|------|------|
| `nfra.gov.cn` | Angular.js 服务端渲染 |
| `gov.cn` 部分站点 | 动态内容加载 |

**可扩展**：在执行过程中动态添加到已知列表

## metadata.json 格式要求

**CRITICAL - JSON 格式规则**

生成 metadata.json 时必须严格遵守以下规则，否则后端无法解析：

### 规则 1：字符串值中的引号处理

**错误示例**（会导致 JSON 解析失败）：
```json
{
  "title": "银行"开门红"揽储手段利器"  // ❌ 字符串内包含未转义的双引号
}
```

**正确示例**（将内部双引号替换单引号）：
```json
{
  "title": "银行'开门红'揽储手段利器"  // ✅ 使用单引号
}
```

### 规则 2：完整的 metadata.json 模板

```json
{
  "batchId": "20260102_105530_eastmoney_czzyh_a0379e4f",
  "source": "来源网站名称",
  "sourceUrl": "https://example.com/list",
  "sourceName": "eastmoney_czzyh",
  "crawlTime": "2026-01-02T10:55:30",
  "articleCount": 3,
  "articles": [
    {
      "index": 1,
      "title": "文章标题（引号用单引号）",
      "filename": "01_政策_文章标题.md",
      "url": "https://example.com/article1",
      "category": "policy",
      "publishTime": "2026-01-02T10:00:00",
      "author": "作者名",
      "tags": ["标签1", "标签2"]
    }
  ]
}
```

### 规则 3：生成步骤

1. 先在内存中构建完整的 JSON 对象
2. **检查所有 title 字段**，将双引号替换单引号
3. 使用 `Write` 工具写入文件，确保 JSON 格式正确
4. 验证：尝试用 `python3 -m json.tool metadata.json` 验证格式

**示例**：
```python
# 正确的处理方式
title = '银行"开门红"揽储'  # 原始标题
clean_title = title.replace('"', "'")  # 清理后: 银行'开门红'揽储'
```

## 输出结构

```
crawl_files/
└── {YYYYMMDD_HHmmss}_{sourceName}_{shortTaskId}/
    ├── 01_{分类前缀}_{标题}.md
    ├── 02_{分类前缀}_{标题}.md
    ├── ...
    ├── summary.md
    └── metadata.json
```

### 文件命名规则

- **批次文件夹**：`{timestamp}_{sourceName}_{shortTaskId}`
  - `timestamp` 格式：`YYYYMMDD_HHmmss`
  - `shortTaskId`：taskId 的前 8 位（如果提供）
  - 示例：`20260102_105530_eastmoney_bank_a0379e4f`
- **文章文件**：`{序号}_{分类前缀}_{标题}.md`（每篇文章有独立的分类）
  - 示例：`01_政策_央行发布新政策.md`、`02_竞品_某公司新产品.md`、`03_其他_市场行情报道.md`

**说明**：`shortTaskId` 用于标识唯一任务，避免文件夹名冲突。

## Markdown 文件格式

```markdown
# 文章标题

**元数据**
- 分类：[政策|竞品|其他]
- 来源：来源网站名称
- 发布时间：YYYY-MM-DD HH:mm:ss
- 作者：作者名称
- 标签：#标签1 #标签2 #标签3

## 正文

文章正文内容...
```

## 使用示例

### 示例 1：爬取东方财富银行频道文章

```
爬取 https://bank.eastmoney.com/a/czzyh.html 的最新3篇文章，
来源标识为 eastmoney_bank
```

### 示例 2：爬取竞品动态文章

```
爬取 https://finance.eastmoney.com/news/company.html 的前5篇文章，
来源标识为 eastmoney_competitor
```

### 示例 3：爬取动态网站（NFRA）

```
爬取 https://www.nfra.gov.cn/cn/view/pages/ItemList.html?itemPId=923&itemId=928 的文章，
来源标识为 nfra
```

**注意**：无需指定分类，AI 会根据每篇文章内容自动判定是政策类、竞品类还是其他类。

## 技术依赖

- **webReader MCP tool**：快速获取静态网页内容
- **Dev Browser (Playwright)**：获取 JavaScript 动态渲染内容
- **Claude AI 能力**：智能分析页面，提取链接和结构化内容

## 故障排查

### webReader 失败 (-400 错误)

**原因**：
- URL 格式不符合要求
- 域名在黑名单中
- URL 编码问题

**解决方案**：自动降级到 Dev Browser

### 页面内容为空

**原因**：
- 动态渲染网站（Angular.js、React）
- 内容通过 AJAX 异步加载

**解决方案**：自动切换到 Dev Browser

### Dev Browser 启动失败

**检查步骤**：
1. 确认 Playwright 已安装：`npx playwright --version`
2. 检查端口占用：`lsof -i :9222`
3. 查看日志：`tail -f /tmp/dev-browser.log`

## 注意事项

1. `sourceName` 只能包含小写字母、数字和下划线
2. 单次最多爬取 20 篇文章
3. 文件名自动清理非法字符，标题限制 50 字符
4. 批次文件夹包含时间戳和 shortTaskId，避免覆盖
5. **CRITICAL**：生成 metadata.json 时，必须将标题中的双引号替换单引号，否则会导致后端 JSON 解析失败
6. Dev Browser 服务器会在首次使用时自动启动，无需手动操作
