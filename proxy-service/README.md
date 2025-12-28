# EagleEye2 代理服务

简单的 FastAPI 代理，将请求路由到 Claude Code CLI 或 MCP 服务器。

## 安装

```bash
cd /home/captain/projects/EagleEye2
python3 -m venv venv
./venv/bin/pip install -r proxy-service/requirements.txt
```

## 运行

```bash
./venv/bin/python proxy-service/main.py
```

服务运行在 `http://localhost:8000`

## API

### POST /api/crawl

爬取文章

**请求:**
```json
{
  "listUrl": "https://bank.eastmoney.com/a/czzyh.html",
  "sourceName": "eastmoney_bank",
  "maxArticles": 3,
  "useSkill": true
}
```

**参数:**
- `listUrl`: 文章列表 URL
- `sourceName`: 来源标识
- `maxArticles`: 最大文章数 (默认 3)
- `useSkill`: true=用 Claude Code Skill, false=用 MCP (目前只支持 true)

**示例:**
```bash
curl -X POST http://localhost:8000/api/crawl \
  -H "Content-Type: application/json" \
  -d '{
    "listUrl": "https://bank.eastmoney.com/a/czzyh.html",
    "sourceName": "eastmoney_bank",
    "maxArticles": 3,
    "useSkill": true
  }'
```
