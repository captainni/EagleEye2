# EagleEye2 智能爬虫业务需求书

## 1. 功能概述

EagleEye2 智能爬虫是一个用于爬取金融资讯网站文章列表并生成结构化 Markdown 文件的工具。该工具支持：

- 智能提取文章列表链接（使用 AI 分析，无需配置 CSS 选择器）
- 批量爬取文章详细内容
- 结构化数据提取（标题、作者、发布时间、正文、标签、分类等）
- 生成格式规范的 Markdown 文件
- 批次化管理（每次爬取独立文件夹）

## 2. 输入参数规范

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| `listUrl` | string | 是 | - | 文章列表页面 URL |
| `sourceName` | string | 是 | - | 来源标识（小写字母、数字、下划线，如 `eastmoney_bank`） |
| `maxArticles` | number | 否 | 3 | 最大爬取文章数（1-20） |
| `outputDir` | string | 否 | `/home/captain/projects/EagleEye2/crawl_files` | 输出目录路径 |

### 参数说明

#### `category` 分类字段（AI 自动判定）

**重要**：`category` 分类字段由 **AI 分析文章内容后自动判定**，不是用户输入参数。

- `policy`（政策类）：文章内容涉及政府政策、监管规则、央行措施、法律法规、行业标准等
- `competitor`（竞品类）：文章内容涉及竞品公司动态、产品发布、营销活动、财报业绩、市场份额等

每篇文章独立判定分类，一个批次可能同时包含政策和竞品文章。

## 3. 输出文件结构

```
crawl_files/
└── {YYYYMMDD_HHmmss}_{sourceName}/
    ├── 01_{分类前缀}_{标题}.md
    ├── 02_{分类前缀}_{标题}.md
    ├── 03_{分类前缀}_{标题}.md
    ├── ...
    ├── summary.md          # 批次汇总报告
    └── metadata.json       # 批次元数据
```

**说明**：批次文件夹命名不包含分类，因为一个批次可能同时包含政策和竞品文章。

### 示例目录结构

```
crawl_files/
└── 20251227_105530_eastmoney_bank/
    ├── 01_政策_央行发布新政策.md
    ├── 02_竞品_某银行新产品.md
    ├── 03_政策_银保监会通知.md
    ├── summary.md
    └── metadata.json
```

## 4. 文件命名规则

### 4.1 批次文件夹命名

格式：`{timestamp}_{sourceName}`

- `timestamp`：15位时间戳，格式 `YYYYMMDD_HHmmss`
- `sourceName`：来源标识

示例：`20251227_105530_eastmoney_bank`

**说明**：批次文件夹命名不包含分类，因为一个批次可能同时包含政策和竞品文章。每篇文章由 AI 独立判定分类。

### 4.2 文章文件命名

格式：`{序号}_{分类前缀}_{标题}.md`

- `序号`：两位数字，从 01 开始
- `分类前缀`：`政策` 或 `竞品`
- `标题`：清理后的文章标题（移除特殊字符，空格替换为下划线，限制50字符）

示例：
- `01_政策_央行发布新政策.md`
- `02_竞品_某公司新产品发布.md`

## 5. Markdown 文件格式

### 5.1 文章文件格式

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

### 5.2 汇总文件格式 (summary.md)

```markdown
# 爬取批次汇总

**批次信息**
- 批次ID：20251227_105530_eastmoney_bank
- 来源：eastmoney_bank
- 列表URL：https://bank.eastmoney.com/a/czzyh.html
- 爬取时间：2025-12-27 10:55:30
- 文章数量：3

## 文章列表

1. [政策] 央行发布新政策 (5000字)
2. [竞品] 某银行新产品 (4200字)
3. [政策] 银保监会通知 (3500字)

## 统计信息

- 总字数：12700
- 平均字数：4233
- 分类统计：政策 2 篇，竞品 1 篇
```

## 6. metadata.json 格式

```json
{
  "batchId": "20251227_105530_eastmoney_bank",
  "sourceName": "eastmoney_bank",
  "listUrl": "https://bank.eastmoney.com/a/czzyh.html",
  "crawlTime": "2025-12-27T10:55:30+08:00",
  "articleCount": 3,
  "totalWords": 12700,
  "articles": [
    {
      "filename": "01_政策_央行发布新政策.md",
      "title": "央行发布新政策",
      "url": "https://finance.eastmoney.com/a/20251227/article1.html",
      "category": "policy",
      "publishTime": "2025-12-27 10:00:00",
      "author": "张三",
      "tags": ["货币政策", "金融监管"],
      "wordCount": 5000
    },
    {
      "filename": "02_竞品_某银行新产品.md",
      "title": "某银行新产品",
      "url": "https://finance.eastmoney.com/a/20251227/article2.html",
      "category": "competitor",
      "publishTime": "2025-12-27 09:30:00",
      "author": "李四",
      "tags": ["产品发布"],
      "wordCount": 4200
    },
    {
      "filename": "03_政策_银保监会通知.md",
      "title": "银保监会通知",
      "url": "https://finance.eastmoney.com/a/20251227/article3.html",
      "category": "policy",
      "publishTime": "2025-12-27 08:00:00",
      "author": "王五",
      "tags": ["监管政策"],
      "wordCount": 3500
    }
  ],
  "categoryStats": {
    "policy": 2,
    "competitor": 1
  },
  "errors": []
}
```

**说明**：
- 批次元数据不包含顶层 `category` 字段（因为批次可能包含混合分类）
- 每篇文章有独立的 `category` 字段（由 AI 判定）
- 新增 `categoryStats` 字段统计各分类文章数量

## 7. 执行流程

```
1. 参数验证
   ├── 验证 listUrl 有效性
   └── 验证 sourceName 格式

2. 创建批次目录
   ├── 生成批次文件夹名 {timestamp}_{sourceName}
   └── 创建目录结构

3. 获取文章列表
   └── 调用 webReader MCP tool 获取 listUrl 内容

4. 提取文章链接
   ├── 使用 Claude AI 分析页面内容
   ├── 提取文章标题、链接、发布时间
   └── 返回前 maxArticles 篇文章

5. 批量爬取文章
   For 每篇文章：
   ├── 调用 webReader 获取文章详情
   ├── 使用 Claude AI 提取结构化内容
   ├── **AI 判定分类**：分析文章内容，判定是 policy（政策）还是 competitor（竞品）
   ├── 生成 Markdown 文件（文件名含分类标识）
   └── 保存到批次目录

6. 生成汇总
   ├── 创建 summary.md（含分类统计）
   ├── 创建 metadata.json（含每篇文章的分类）
   └── 返回执行结果
```

## 8. 使用示例

### 示例 1：爬取金融资讯文章

**输入**：
- listUrl: `https://bank.eastmoney.com/a/czzyh.html`
- sourceName: `eastmoney_bank`
- maxArticles: `3`

**输出**：
```
成功爬取 3 篇文章
批次: 20251227_105530_eastmoney_bank
位置: /home/captain/projects/EagleEye2/crawl_files/20251227_105530_eastmoney_bank

文章列表:
1. [政策] 央行发布新政策 (5000字)
2. [竞品] 某银行新产品 (4200字)
3. [政策] 银保监会通知 (3500字)

分类统计: 政策 2 篇，竞品 1 篇
```

**说明**：AI 自动分析每篇文章内容并判定分类，用户无需指定 category 参数。

### 示例 2：爬取竞品资讯文章

**输入**：
- listUrl: `https://finance.eastmoney.com/news/company.html`
- sourceName: `eastmoney_competitor`
- maxArticles: `5`

**输出**：
```
成功爬取 5 篇文章
批次: 20251227_110245_eastmoney_competitor
位置: /home/captain/projects/EagleEye2/crawl_files/20251227_110245_eastmoney_competitor

文章列表:
1. [竞品] 某公司新产品发布 (4200字)
2. [竞品] 同业营销活动 (3800字)
3. [政策] 监管新规解读 (2900字)
4. [竞品] 竞品财报解读 (6500字)
5. [竞品] 市场份额变化 (2900字)

分类统计: 竞品 4 篇，政策 1 篇
```

## 9. 技术实现

### 9.1 Claude Code Skill

- 使用官方 skill-creator 规范创建
- 遵循 SKILL.md 格式要求（YAML frontmatter + Markdown body）
- 通过对话式调用或 MCP 调用

### 9.2 MCP 服务器

- 使用官方 `@modelcontextprotocol/sdk` TypeScript SDK
- 支持 stdio 和 HTTP 传输模式
- 提供 `crawl_articles` 工具

### 9.3 依赖工具

- **webReader MCP tool**：获取网页内容
- **Claude AI 能力**：智能分析页面，提取链接和结构化内容

## 10. 注意事项

1. **AI 自动分类**：category 分类字段由 AI 分析文章内容后自动判定，不是用户输入参数
2. **混合分类批次**：一个批次可能同时包含政策和竞品文章，批次文件夹命名不包含分类
3. **文件名清理**：自动移除文件名中的非法字符（`<>:"/\|?*`），空格替换为下划线
4. **字符限制**：标题在文件名中限制为 50 个字符
5. **数量限制**：单次最多爬取 20 篇文章
6. **目录创建**：自动创建不存在的目录结构
7. **错误处理**：错误信息记录在 metadata.json 的 errors 数组中

## 11. 后续集成

- MCP 服务器可独立运行，通过 HTTP 暴露接口
- EagleEye2 后端可通过 HTTP 调用 MCP 服务
- 支持定时任务和手动触发
