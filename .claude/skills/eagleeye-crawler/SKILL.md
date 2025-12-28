---
name: eagleeye-crawler
description: EagleEye2 智能爬虫工具，用于爬取金融资讯网站文章列表并生成结构化 Markdown 文件。支持政策类(policy)和竞品类(competitor)文章分类。当用户需要爬取网站文章列表、提取文章内容并生成结构化文档时触发此 skill。
---

# EagleEye2 智能爬虫

## 概述

此 skill 提供智能爬虫功能，可以从金融资讯网站爬取文章列表，提取文章内容，并生成结构化的 Markdown 文件。使用 AI 分析页面结构，无需配置 CSS 选择器。

## 参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| `listUrl` | string | 是 | - | 文章列表页面 URL |
| `sourceName` | string | 是 | - | 来源标识（小写字母、数字、下划线） |
| `maxArticles` | number | 否 | 3 | 最大爬取文章数（1-20） |
| `outputDir` | string | 否 | `/home/captain/projects/EagleEye2/crawl_files` | 输出目录 |

**注意**：`category` 分类字段由 **AI 自动分析判定**，无需用户指定。AI 会根据每篇文章内容判定是 policy（政策类）还是 competitor（竞品类）。

## 执行流程

1. **验证参数**：检查 URL 有效性、来源标识格式
2. **获取列表页面**：调用 webReader MCP tool 获取列表页面内容
3. **提取文章链接**：使用 Claude AI 分析页面，提取文章标题、链接、发布时间
4. **批量爬取文章**：
   For 每篇文章：
   - 调用 webReader 获取文章详情
   - **AI 判定分类**：分析文章内容，判定是 policy（政策）还是 competitor（竞品）
   - 提取结构化内容（标题、作者、发布时间、正文、标签、分类）
   - 生成 Markdown 文件（文件名含分类标识）
   - 保存到批次文件夹
5. **生成汇总**：创建 summary.md
6. **保存元数据**：生成 metadata.json 文件

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
  "batchId": "20251228_163000_eastmoney_czzyh",
  "source": "来源网站名称",
  "sourceUrl": "https://example.com/list",
  "sourceName": "eastmoney_czzyh",
  "crawlTime": "2025-12-28T16:30:00",
  "articleCount": 3,
  "articles": [
    {
      "index": 1,
      "title": "文章标题（引号用单引号）",
      "filename": "01_政策_文章标题.md",
      "url": "https://example.com/article1",
      "category": "policy",
      "publishTime": "2025-12-28T10:00:00",
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
└── {YYYYMMDD_HHmmss}_{sourceName}/
    ├── 01_{分类前缀}_{标题}.md
    ├── 02_{分类前缀}_{标题}.md
    ├── ...
    ├── summary.md
    └── metadata.json
```

### 文件命名规则

- **批次文件夹**：`{timestamp}_{sourceName}`（不带分类）
  - 示例：`20251227_105530_eastmoney_bank`
- **文章文件**：`{序号}_{分类前缀}_{标题}.md`（每篇文章有独立的分类）
  - 示例：`01_政策_央行发布新政策.md`、`02_竞品_某公司新产品.md`

**说明**：批次文件夹命名不带分类，因为一个批次可能同时包含政策和竞品文章。每篇文章由 AI 单独判定分类。

## Markdown 文件格式

```markdown
# 文章标题

**元数据**
- 分类：[政策|竞品]
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

### 示例 3：完整参数

```
爬取 https://example.com/articles 的文章，
- 来源标识：example_news
- 文章数量：10
- 输出目录：/tmp/crawl
```

**注意**：无需指定分类，AI 会根据每篇文章内容自动判定是政策类还是竞品类。

## 技术依赖

- **webReader MCP tool**：获取网页内容
- **Claude AI 能力**：智能分析页面，提取链接和结构化内容

## 注意事项

1. `sourceName` 只能包含小写字母、数字和下划线
2. 单次最多爬取 20 篇文章
3. 文件名自动清理非法字符，标题限制 50 字符
4. 批次文件夹包含时间戳，避免覆盖
5. **CRITICAL**：生成 metadata.json 时，必须将标题中的双引号替换单引号，否则会导致后端 JSON 解析失败
