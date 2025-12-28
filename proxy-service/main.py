"""
EagleEye2 代理服务
路由请求到 MCP 服务器或 Claude Code CLI
"""
from fastapi import FastAPI, HTTPException, Request
from fastapi.responses import JSONResponse
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
ANALYSIS_TIMEOUT = int(os.getenv("ANALYSIS_TIMEOUT", "300"))  # 默认5分钟超时


class CrawlRequest(BaseModel):
    listUrl: str
    sourceName: str
    maxArticles: int = 3
    useSkill: bool = False  # True = 用 Claude Code Skill, False = 用 MCP


class AnalysisRequest(BaseModel):
    content: str  # 政策文章的 Markdown 内容
    products: Optional[str] = None  # 用户产品列表的 JSON 字符串（可选）


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


@app.post("/analyze-policy")
async def analyze_policy(req: Request):
    """
    调用 policy-analyzer skill 分析政策文章
    """
    try:
        data = await req.json()
        content = data.get("content")
        products = data.get("products")

        if not content:
            raise HTTPException(status_code=400, detail="Missing required field: content")

        # 构建产品上下文（如果有）
        products_context = ""
        if products:
            products_context = f"""

## 用户产品信息
以下是用户的产品信息，请分析政策与这些产品的相关度：
{products}
"""

        # 构建完整的 prompt
        prompt = f"""请使用 policy-analyzer skill 分析以下政策文章，并以 JSON 格式返回分析结果：
{products_context}

## 政策文章内容
{content}

请确保返回结果包含以下字段：
- policyType: 政策类型
- importance: 重要程度
- relevance: 与产品的相关度（高|中|低）
- areas: 相关领域标签数组
- summary: 政策摘要
- keyPoints: 关键条款数组（用于高亮显示）
- impactAnalysis: 影响分析
- suggestions: 建议数组，每条包含 suggestion 和 reason
"""

        proc = None
        try:
            # 调用 Claude Code CLI
            proc = await asyncio.create_subprocess_exec(
                "claude",
                "--dangerously-skip-permissions",
                "-p", prompt,
                stdout=asyncio.subprocess.PIPE,
                stderr=asyncio.subprocess.PIPE,
                cwd="/home/captain/projects/EagleEye2"
            )

            # 等待进程完成，带超时控制
            try:
                stdout, stderr = await asyncio.wait_for(
                    proc.communicate(),
                    timeout=ANALYSIS_TIMEOUT
                )
            except asyncio.TimeoutError:
                if proc:
                    try:
                        proc.kill()
                        await proc.wait()
                    except:
                        pass
                raise HTTPException(
                    status_code=504,
                    detail=f"Analysis timeout after {ANALYSIS_TIMEOUT} seconds"
                )

            # 检查返回码
            if proc.returncode != 0:
                error_msg = stderr.decode() if stderr else "Unknown error"
                raise HTTPException(
                    status_code=500,
                    detail=f"Claude CLI error (exit code {proc.returncode}): {error_msg}"
                )

            # 尝试解析返回结果为 JSON
            result_text = stdout.decode().strip()
            try:
                # 尝试直接解析
                result = json.loads(result_text)
                return JSONResponse(content=result)
            except json.JSONDecodeError:
                # 如果直接解析失败，尝试提取 JSON 部分
                # Claude CLI 可能会在输出前后添加其他内容
                start_idx = result_text.find("{")
                end_idx = result_text.rfind("}") + 1
                if start_idx >= 0 and end_idx > start_idx:
                    json_str = result_text[start_idx:end_idx]
                    result = json.loads(json_str)
                    return JSONResponse(content=result)
                else:
                    # 无法解析为 JSON，返回原始文本
                    return {"rawOutput": result_text}

        except HTTPException:
            raise
        except Exception as e:
            raise HTTPException(status_code=500, detail=str(e))

        finally:
            # 确保进程被清理
            if proc and proc.returncode is None:
                try:
                    proc.kill()
                    await proc.wait()
                except:
                    pass

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")



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
