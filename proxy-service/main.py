"""
EagleEye2 代理服务
路由请求到 MCP 服务器或 Claude Code CLI
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import subprocess
import json
import os
from typing import Optional

app = FastAPI(title="EagleEye2 Proxy", version="1.0.0")

# 配置
MCP_HTTP_URL = os.getenv("MCP_HTTP_URL", "http://localhost:3000/mcp")
CRAWL_OUTPUT_DIR = os.getenv("CRAWL_OUTPUT_DIR", "/home/captain/projects/EagleEye2/crawl_files")


class CrawlRequest(BaseModel):
    listUrl: str
    sourceName: str
    maxArticles: int = 3
    useSkill: bool = False  # True = 用 Claude Code Skill, False = 用 MCP


@app.get("/")
async def root():
    return {"service": "EagleEye2 Proxy", "version": "1.0.0"}


@app.get("/health")
async def health():
    return {"status": "ok"}


@app.post("/api/crawl")
async def crawl(req: CrawlRequest):
    """
    爬取文章
    - useSkill=True: 使用 Claude Code CLI + Skill (支持 AI 分析)
    - useSkill=False: 使用 MCP 服务器 (快速，但功能有限)
    """
    if req.useSkill:
        return await _crawl_with_skill(req)
    else:
        return await _crawl_with_mcp(req)


async def _crawl_with_skill(req: CrawlRequest):
    """使用 Claude Code CLI + Skill"""
    prompt = f"使用 eagleeye-crawler skill 爬取 {req.listUrl} 的最新{req.maxArticles}篇文章，来源标识为 {req.sourceName}"

    try:
        result = subprocess.run(
            [
                "claude",
                "--dangerously-skip-permissions",
                "-p", prompt,
                "--output-format=json"
            ],
            capture_output=True,
            text=True,
            timeout=300,  # 5分钟超时
            cwd="/home/captain/projects/EagleEye2"
        )

        if result.returncode != 0:
            raise HTTPException(status_code=500, detail=f"Claude CLI error: {result.stderr}")

        return {"success": True, "method": "skill", "data": result.stdout}

    except subprocess.TimeoutExpired:
        raise HTTPException(status_code=504, detail="Request timeout")
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


async def _crawl_with_mcp(req: CrawlRequest):
    """使用 MCP 服务器 (暂时返回提示，MCP 需要完善)"""
    # TODO: 等 MCP 实现完整后，直接调用 HTTP 接口
    raise HTTPException(
        status_code=501,
        detail="MCP mode not implemented yet. Please use useSkill=True"
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
