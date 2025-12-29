---
name: competitor-analyzer
description: 竞品文章分析 AI 工具。读取竞品文章 Markdown 内容，使用 Claude AI 分析提取关键信息，生成结构化分析结果。支持结合用户产品信息评估相关度，包括竞品公司、动态类型、重要性、相关度、标签、摘要、关键要点（用于高亮）、市场影响、竞争态势分析和可执行建议。
---

# Competitor Analyzer Skill

## 功能
分析竞品动态文章，提取关键信息用于入库和高亮显示。支持根据用户产品信息评估竞品动态与我方产品的相关度。

## 使用方法
用户提供竞品文章的 Markdown 内容，即可进行深度分析。如需评估相关度，可同时提供用户产品信息。

## 输出格式
JSON 格式，包含：`company`、`type`、`importance`、`relevance`、`tags`、`summary`、`keyPoints`、`marketImpact`、`competitiveAnalysis`、`ourSuggestions`

## 输入参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `markdownContent` | string | 是 | 竞品文章的完整 Markdown 内容 |
| `userProducts` | string | 否 | 用户产品列表的 JSON 字符串（用于评估相关度） |

## 输出示例
```json
{
  "company": "平安银行",
  "type": "政策响应",
  "importance": "高",
  "relevance": "高",
  "tags": ["信用修复", "政策响应", "零售信贷"],
  "summary": "平安银行积极响应国家一次性信用修复政策，成立专项工作小组统筹推进政策落地。通过升级系统功能、优化服务流程、加强宣传引导等多措并举，确保政策红利精准惠及符合条件的客户。",
  "keyPoints": [
    "成立由零售信贷风险管理部牵头的专项工作小组",
    "口袋银行APP上线自主查询逾期信息功能",
    "建立跨部门每周沟通机制",
    "在官网和微信公众号发布政策指引"
  ],
  "marketImpact": "该响应措施显示平安银行在政策执行方面的敏捷性，可能提升其在信用修复领域的品牌形象。系统功能的快速上线体现了其科技能力的优势。",
  "competitiveAnalysis": "此举可能提升平安银行在信用修复领域的市场份额，对我们构成直接竞争压力。建议关注其执行效果和客户反馈，评估我们的跟进策略。",
  "ourSuggestions": [
    {
      "suggestion": "立即成立信用修复政策专项工作组",
      "reason": "竞品已快速响应，我们需要跟进以保持市场竞争力"
    },
    {
      "suggestion": "优化APP自助查询功能",
      "reason": "提升客户体验，减少客服咨询压力"
    }
  ]
}
```

## 详细分析标准

完整的分析标准、判定规则和注意事项请参见 [REFERENCE.md](./REFERENCE.md)。
