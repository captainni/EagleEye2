"""
EagleEye2 代理服务
路由请求到 MCP 服务器或 Claude Code CLI
"""
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import asyncio
import subprocess
import json
import os
import signal
from typing import Optional

app = FastAPI(title="EagleEye2 Proxy", version="1.0.0")

# 配置
MCP_HTTP_URL = os.getenv("MCP_HTTP_URL", "http://localhost:3000/mcp")
CRAWL_OUTPUT_DIR = os.getenv("CRAWL_OUTPUT_DIR", "/home/captain/projects/EagleEye2/crawl_files")
CRAWL_TIMEOUT = int(os.getenv("CRAWL_TIMEOUT", "300"))  # 默认5分钟超时


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
    """使用 Claude Code CLI + Skill（使用 asyncio 正确管理进程）"""
    prompt = f"使用 eagleeye-crawler skill 爬取 {req.listUrl} 的最新{req.maxArticles}篇文章，来源标识为 {req.sourceName}"

    proc = None
    try:
        # 使用 asyncio.create_subprocess_exec 替代 subprocess.run
        proc = await asyncio.create_subprocess_exec(
            "claude",
            "--dangerously-skip-permissions",
            "-p", prompt,
            "--output-format=json",
            stdout=asyncio.subprocess.PIPE,
            stderr=asyncio.subprocess.PIPE,
            cwd="/home/captain/projects/EagleEye2"
        )

        # 等待进程完成，带超时控制
        try:
            stdout, stderr = await asyncio.wait_for(
                proc.communicate(),
                timeout=CRAWL_TIMEOUT
            )
        except asyncio.TimeoutError:
            # 超时：强制终止进程
            if proc:
                try:
                    proc.kill()
                    await proc.wait()
                except:
                    pass
            raise HTTPException(
                status_code=504,
                detail=f"Request timeout after {CRAWL_TIMEOUT} seconds"
            )

        # 检查返回码
        if proc.returncode != 0:
            error_msg = stderr.decode() if stderr else "Unknown error"
            raise HTTPException(
                status_code=500,
                detail=f"Claude CLI error (exit code {proc.returncode}): {error_msg}"
            )

        return {
            "success": True,
            "method": "skill",
            "data": stdout.decode() if stdout else ""
        }

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        # 确保进程被清理（双重保险）
        if proc and proc.returncode is None:
            try:
                proc.kill()
                await proc.wait()
            except:
                pass


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
