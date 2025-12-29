---
name: policy-analyzer
description: 政策文章分析 AI 工具。读取政策文章 Markdown 内容，使用 Claude AI 分析提取关键信息，生成结构化分析结果。支持结合用户产品信息评估相关度，包括政策类型、重要程度、相关度、领域标签、摘要、关键条款（用于高亮）、影响分析和可执行建议。
---

# Policy Analyzer Skill

## 功能
分析金融政策文章，提取关键信息用于入库和高亮显示。支持根据用户产品信息评估政策与产品的相关度。

## 使用方法
用户提供政策文章的 Markdown 内容，即可进行深度分析。如需评估相关度，可同时提供用户产品信息。

## 输出格式
JSON 格式，包含：`policyType`、`importance`、`relevance`、`areas`、`summary`、`keyPoints`、`impactAnalysis`、`suggestions`

## 输入参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `markdownContent` | string | 是 | 政策文章的完整 Markdown 内容 |
| `products` | string | 否 | 用户产品列表的 JSON 字符串（用于评估相关度） |

## 输出示例
```json
{
  "policyType": "监管政策",
  "importance": "高",
  "relevance": "高",
  "areas": ["数据安全", "个人信息保护", "风险管理"],
  "summary": "该指导意见要求银行保险机构建立健全数据安全管理制度...",
  "keyPoints": [
    "建立数据分类分级管理制度",
    "建立数据安全技术防护体系",
    "制定数据安全事件应急预案"
  ],
  "impactAnalysis": "该政策对银行保险机构的数据治理体系提出更高要求...",
  "suggestions": [
    {
      "suggestion": "完善数据分类分级标准和管理制度",
      "reason": "满足监管合规要求，夯实数据安全管理基础"
    }
  ]
}
```

## 详细分析标准

完整的分析标准、判定规则和注意事项请参见 [REFERENCE.md](./REFERENCE.md)。

