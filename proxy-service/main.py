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
import logging
import time
from datetime import datetime
from typing import Optional
from pathlib import Path

app = FastAPI(title="EagleEye2 Proxy", version="1.0.0")

# 配置
MCP_HTTP_URL = os.getenv("MCP_HTTP_URL", "http://localhost:3000/mcp")
CRAWL_OUTPUT_DIR = os.getenv("CRAWL_OUTPUT_DIR", "/home/captain/projects/EagleEye2/crawl_files")
CRAWL_TIMEOUT = int(os.getenv("CRAWL_TIMEOUT", "300"))  # 默认5分钟超时
ANALYSIS_TIMEOUT = int(os.getenv("ANALYSIS_TIMEOUT", "300"))  # 默认5分钟超时

# 日志目录
LOG_DIR = Path("/home/captain/projects/EagleEye2/logs")
LOG_DIR.mkdir(exist_ok=True)

# 配置 Proxy Service 日志
proxy_logger = logging.getLogger("proxy")
proxy_logger.setLevel(logging.INFO)
proxy_handler = logging.FileHandler(LOG_DIR / "proxy.log", encoding="utf-8")
proxy_handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(message)s", datefmt="%Y-%m-%d %H:%M:%S"))
proxy_logger.addHandler(proxy_handler)

# 配置 Claude CLI 日志
claude_logger = logging.getLogger("claude-cli")
claude_logger.setLevel(logging.INFO)
claude_handler = logging.FileHandler(LOG_DIR / "claude-cli.log", encoding="utf-8")
claude_handler.setFormatter(logging.Formatter("%(asctime)s [%(levelname)s] %(message)s", datefmt="%Y-%m-%d %H:%M:%S"))
claude_logger.addHandler(claude_handler)

proxy_logger.info("Proxy Service 启动")


class CrawlRequest(BaseModel):
    listUrl: str
    sourceName: str
    maxArticles: int = 3
    useSkill: bool = False  # True = 用 Claude Code Skill, False = 用 MCP


class AnalysisRequest(BaseModel):
    content: str  # 政策文章的 Markdown 内容
    products: Optional[str] = None  # 用户产品列表的 JSON 字符串（可选）


class CompetitorAnalysisRequest(BaseModel):
    content: str  # 竞品文章的 Markdown 内容
    userProducts: Optional[str] = None  # 用户产品列表的 JSON 字符串（可选）


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
    claude_logger.info("=" * 50)
    claude_logger.info("开始政策分析任务")
    proxy_logger.info("收到政策分析请求")

    try:
        data = await req.json()
        content = data.get("content")
        products = data.get("products")

        if not content:
            raise HTTPException(status_code=400, detail="Missing required field: content")

        claude_logger.info(f"内容长度: {len(content)} 字符")
        claude_logger.info(f"有产品信息: {'是' if products else '否'}")

        # 构建产品上下文（如果有）
        products_context = ""
        if products:
            products_context = f"""

## 用户产品信息
以下是用户的产品信息，请分析政策与这些产品的相关度：
{products}
"""

        # 构建完整的 prompt（强化 JSON 格式要求）
        prompt = f"""请分析以下政策文章。

【输出格式要求 - 必须严格遵守】
1. 只返回纯 JSON 对象，格式：{{"key": "value"}}
2. 绝对不要使用任何 markdown 标记，包括：
   - ❌ 不要用 ```json 或 ``` 包裹
   - ❌ 不要用 **粗体** 或其他格式
   - ❌ 不要添加任何解释文字
3. 输出必须从 {{ 开始，以 }} 结束
4. 中文引号必须转义或使用英文引号

{products_context}

## 政策文章内容
{content}

请直接返回 JSON（不要有任何额外内容）：
{{
  "policyType": "政策类型",
  "importance": "重要程度（高|中|低）",
  "relevance": "与产品的相关度（高|中|低）",
  "areas": ["领域1", "领域2"],
  "summary": "政策摘要",
  "keyPoints": ["关键条款1", "关键条款2"],
  "impactAnalysis": "影响分析",
  "suggestions": [
    {{"suggestion": "建议内容", "reason": "原因"}}
  ]
}}
"""

        proc = None
        start_time = time.time()
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

            pid = proc.pid
            claude_logger.info(f"启动 Claude Code CLI, PID={pid}")

            # 等待进程完成，带超时控制
            try:
                stdout, stderr = await asyncio.wait_for(
                    proc.communicate(),
                    timeout=ANALYSIS_TIMEOUT
                )
            except asyncio.TimeoutError:
                elapsed = time.time() - start_time
                claude_logger.error(f"超时终止: PID={pid}, 已耗时={elapsed:.1f}s, 超时限制={ANALYSIS_TIMEOUT}s")
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
            elapsed = time.time() - start_time
            if proc.returncode != 0:
                error_msg = stderr.decode() if stderr else "Unknown error"
                claude_logger.error(f"Claude Code CLI 执行失败: PID={pid}, 退出码={proc.returncode}, 耗时={elapsed:.1f}s")
                claude_logger.error(f"错误信息: {error_msg}")
                raise HTTPException(
                    status_code=500,
                    detail=f"Claude CLI error (exit code {proc.returncode}): {error_msg}"
                )

            claude_logger.info(f"Claude Code CLI 执行成功: PID={pid}, 退出码=0, 耗时={elapsed:.1f}s")

            # 尝试解析返回结果为 JSON
            result_text = stdout.decode().strip()
            claude_logger.info(f"输出长度: {len(result_text)} 字符")
            claude_logger.info("=" * 50)

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
            elapsed = time.time() - start_time
            claude_logger.error(f"任务异常: {str(e)}, 耗时={elapsed:.1f}s")
            claude_logger.info("=" * 50)
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
        claude_logger.error(f"分析失败: {str(e)}")
        claude_logger.info("=" * 50)
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")


@app.post("/analyze-competitor")
async def analyze_competitor(req: Request):
    """
    调用 competitor-analyzer skill 分析竞品文章
    """
    claude_logger.info("=" * 50)
    claude_logger.info("开始竞品分析任务")
    proxy_logger.info("收到竞品分析请求")

    try:
        data = await req.json()
        content = data.get("content")
        user_products = data.get("userProducts")

        if not content:
            raise HTTPException(status_code=400, detail="Missing required field: content")

        claude_logger.info(f"内容长度: {len(content)} 字符")
        claude_logger.info(f"有产品信息: {'是' if user_products else '否'}")

        # 构建产品上下文（如果有）
        products_context = ""
        if user_products:
            products_context = f"""

## 用户产品信息
以下是用户的产品信息，请分析竞品动态与我方产品的相关度：
{user_products}
"""

        # 构建完整的 prompt（强化 JSON 格式要求）
        prompt = f"""请分析以下竞品文章。

【输出格式要求 - 必须严格遵守】
1. 只返回纯 JSON 对象，格式：{{"key": "value"}}
2. 绝对不要使用任何 markdown 标记，包括：
   - ❌ 不要用 ```json 或 ``` 包裹
   - ❌ 不要用 **粗体** 或其他格式
   - ❌ 不要添加任何解释文字
3. 输出必须从 {{ 开始，以 }} 结束
4. 中文引号必须转义或使用英文引号

{products_context}

## 竞品文章内容
{content}

请直接返回 JSON（不要有任何额外内容）：
{{
  "company": "竞品公司/机构名称",
  "type": "动态类型（产品更新/营销活动/财报数据/APP更新/利率调整/合作动态/政策响应）",
  "importance": "重要程度（高|中|低）",
  "relevance": "与我方产品的相关度（高|中|低）",
  "tags": ["标签1", "标签2"],
  "summary": "动态摘要",
  "keyPoints": ["关键要点1（必须是原文语句）", "关键要点2"],
  "marketImpact": "市场影响分析",
  "competitiveAnalysis": "竞争态势分析",
  "ourSuggestions": [
    {{"suggestion": "建议内容", "reason": "原因"}}
  ]
}}
"""

        proc = None
        start_time = time.time()
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

            pid = proc.pid
            claude_logger.info(f"启动 Claude Code CLI, PID={pid}")

            # 等待进程完成，带超时控制
            try:
                stdout, stderr = await asyncio.wait_for(
                    proc.communicate(),
                    timeout=ANALYSIS_TIMEOUT
                )
            except asyncio.TimeoutError:
                elapsed = time.time() - start_time
                claude_logger.error(f"超时终止: PID={pid}, 已耗时={elapsed:.1f}s, 超时限制={ANALYSIS_TIMEOUT}s")
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
            elapsed = time.time() - start_time
            if proc.returncode != 0:
                error_msg = stderr.decode() if stderr else "Unknown error"
                claude_logger.error(f"Claude Code CLI 执行失败: PID={pid}, 退出码={proc.returncode}, 耗时={elapsed:.1f}s")
                claude_logger.error(f"错误信息: {error_msg}")
                raise HTTPException(
                    status_code=500,
                    detail=f"Claude CLI error (exit code {proc.returncode}): {error_msg}"
                )

            claude_logger.info(f"Claude Code CLI 执行成功: PID={pid}, 退出码=0, 耗时={elapsed:.1f}s")

            # 尝试解析返回结果为 JSON
            result_text = stdout.decode().strip()
            claude_logger.info(f"输出长度: {len(result_text)} 字符")
            claude_logger.info("=" * 50)

            try:
                # 尝试直接解析
                result = json.loads(result_text)
                return JSONResponse(content=result)
            except json.JSONDecodeError as e:
                # 记录原始输出用于调试
                claude_logger.warning(f"JSON 解析失败，原始输出: {result_text[:500]}...")
                claude_logger.warning(f"JSON 解析错误: {e}")

                # 如果直接解析失败，尝试提取 JSON 部分
                start_idx = result_text.find("{")
                end_idx = result_text.rfind("}") + 1
                if start_idx >= 0 and end_idx > start_idx:
                    json_str = result_text[start_idx:end_idx]
                    try:
                        result = json.loads(json_str)
                        claude_logger.info("JSON 提取解析成功")
                        return JSONResponse(content=result)
                    except json.JSONDecodeError as e2:
                        claude_logger.error(f"JSON 提取后仍解析失败: {e2}")
                        claude_logger.error(f"提取的 JSON 片段: {json_str[:500]}...")
                        raise HTTPException(status_code=500, detail=f"JSON parse error: {e2}")
                else:
                    # 无法解析为 JSON，返回原始文本
                    claude_logger.error(f"无法找到 JSON 结构，返回原始文本")
                    return {"rawOutput": result_text}

        except HTTPException:
            raise
        except Exception as e:
            elapsed = time.time() - start_time
            claude_logger.error(f"任务异常: {str(e)}, 耗时={elapsed:.1f}s")
            claude_logger.info("=" * 50)
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
        claude_logger.error(f"分析失败: {str(e)}")
        claude_logger.info("=" * 50)
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")



async def _crawl_with_skill(req: CrawlRequest):
    """使用 Claude Code CLI + Skill（使用 asyncio 正确管理进程）"""
    prompt = f"使用 eagleeye-crawler skill 爬取 {req.listUrl} 的最新{req.maxArticles}篇文章，来源标识为 {req.sourceName}"

    claude_logger.info("=" * 50)
    claude_logger.info("开始爬虫任务")
    claude_logger.info(f"参数: listUrl={req.listUrl}, sourceName={req.sourceName}, maxArticles={req.maxArticles}")
    proxy_logger.info(f"收到爬虫请求: {req.sourceName}, {req.maxArticles}篇文章")

    proc = None
    start_time = time.time()
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

        pid = proc.pid
        claude_logger.info(f"启动 Claude Code CLI, PID={pid}")

        # 等待进程完成，带超时控制
        try:
            stdout, stderr = await asyncio.wait_for(
                proc.communicate(),
                timeout=CRAWL_TIMEOUT
            )
        except asyncio.TimeoutError:
            # 超时：强制终止进程
            elapsed = time.time() - start_time
            claude_logger.error(f"超时终止: PID={pid}, 已耗时={elapsed:.1f}s, 超时限制={CRAWL_TIMEOUT}s")
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
        elapsed = time.time() - start_time
        if proc.returncode != 0:
            error_msg = stderr.decode() if stderr else "Unknown error"
            claude_logger.error(f"Claude Code CLI 执行失败: PID={pid}, 退出码={proc.returncode}, 耗时={elapsed:.1f}s")
            claude_logger.error(f"错误信息: {error_msg}")
            raise HTTPException(
                status_code=500,
                detail=f"Claude CLI error (exit code {proc.returncode}): {error_msg}"
            )

        claude_logger.info(f"Claude Code CLI 执行成功: PID={pid}, 退出码=0, 耗时={elapsed:.1f}s")
        stdout_text = stdout.decode() if stdout else ""
        claude_logger.info(f"输出长度: {len(stdout_text)} 字符")
        claude_logger.info("=" * 50)

        return {
            "success": True,
            "method": "skill",
            "data": stdout_text
        }

    except HTTPException:
        raise
    except Exception as e:
        elapsed = time.time() - start_time
        claude_logger.error(f"任务异常: {str(e)}, 耗时={elapsed:.1f}s")
        claude_logger.info("=" * 50)
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
