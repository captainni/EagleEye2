"""
Claude Agent SDK 包装器
统一封装 Claude Agent SDK 调用，提供日志记录和错误处理
"""
import logging
import time
from typing import Optional

import anyio
from claude_agent_sdk import query, ClaudeAgentOptions
from claude_agent_sdk import (
    AssistantMessage,
    TextBlock,
    ToolUseBlock,
    ToolResultBlock,
    ResultMessage,
    CLINotFoundError,
    ProcessError,
    CLIJSONDecodeError,
)


async def query_claude(
    prompt: str,
    logger: logging.Logger,
    timeout: int = 600,
    allowed_tools: Optional[list[str]] = None,
    working_dir: str = "/home/captain/projects/EagleEye2",
    max_turns: int = 50,
) -> str:
    """
    使用 Claude Agent SDK 执行查询

    Args:
        prompt: 发送给 Claude 的提示词
        logger: 日志记录器（使用现有的 claude_logger）
        timeout: 超时时间（秒）
        allowed_tools: 允许使用的工具列表（如 ["Read", "Write", "Bash"]）
        working_dir: 工作目录
        max_turns: 最大对话轮数

    Returns:
        str: Claude 的完整输出（文本部分）

    Raises:
        CLINotFoundError: Claude Code CLI 未安装
        ProcessError: Claude 进程执行失败
        CLIJSONDecodeError: JSON 解析失败
        TimeoutError: 超时
    """
    start_time = time.time()
    task_id = f"claude_query_{int(start_time)}"

    logger.info("=" * 50)
    logger.info(f"[{task_id}] 开始 Claude SDK 查询")
    logger.info(f"[{task_id}] Prompt 长度: {len(prompt)} 字符")
    logger.info(f"[{task_id}] 超时设置: {timeout}s")
    logger.info(f"[{task_id}] 工作目录: {working_dir}")

    # 配置 SDK 选项
    options = ClaudeAgentOptions(
        allowed_tools=allowed_tools or ["Read", "Write", "Bash", "Glob", "Grep"],
        max_turns=max_turns,
        cwd=working_dir,
        permission_mode="bypassPermissions",  # 自动接受所有操作
    )

    # 收集所有输出
    output_parts = []
    tool_calls = []

    try:
        # 使用 anyio 的超时机制
        with anyio.fail_after(timeout):
            async for message in query(prompt=prompt, options=options):
                # 记录收到的消息类型
                message_type = type(message).__name__
                logger.debug(f"[{task_id}] 收到消息: {message_type}")

                # 处理不同类型的消息
                if isinstance(message, AssistantMessage):
                    for block in message.content:
                        if isinstance(block, TextBlock):
                            # 文本输出
                            output_parts.append(block.text)
                            logger.debug(f"[{task_id}] 文本块: {len(block.text)} 字符")
                        elif isinstance(block, ToolUseBlock):
                            # 工具调用
                            tool_info = {
                                "name": block.name,
                                "input": str(block.input)[:200],  # 截断过长的输入
                            }
                            tool_calls.append(tool_info)
                            logger.info(f"[{task_id}] 工具调用: {block.name}")

                elif isinstance(message, ResultMessage):
                    # 最终结果消息
                    elapsed = time.time() - start_time
                    logger.info(f"[{task_id}] 执行完成")
                    logger.info(f"[{task_id}] 耗时: {elapsed:.1f}s")
                    logger.info(f"[{task_id}] 总轮数: {message.num_turns}")
                    if hasattr(message, 'total_cost_usd') and message.total_cost_usd:
                        logger.info(f"[{task_id}] 成本: ${message.total_cost_usd:.4f}")
                    if message.is_error:
                        logger.warning(f"[{task_id}] 标记为错误")

                elif isinstance(message, ToolResultBlock):
                    # 工具执行结果
                    logger.debug(f"[{task_id}] 工具结果: {len(str(message.content))} 字符")

        # 汇总日志
        if tool_calls:
            logger.info(f"[{task_id}] 工具调用总数: {len(tool_calls)}")
            for i, call in enumerate(tool_calls, 1):
                logger.info(f"[{task_id}]   {i}. {call['name']}")

        full_output = "".join(output_parts)
        logger.info(f"[{task_id}] 输出总长度: {len(full_output)} 字符")
        logger.info("=" * 50)

        return full_output

    except TimeoutError as e:
        elapsed = time.time() - start_time
        logger.error(f"[{task_id}] 超时: 已耗时 {elapsed:.1f}s, 限制 {timeout}s")
        logger.error("=" * 50)
        raise TimeoutError(f"Claude SDK query timeout after {timeout}s") from e

    except CLINotFoundError as e:
        elapsed = time.time() - start_time
        logger.error(f"[{task_id}] CLI 未找到: {e}")
        logger.error("=" * 50)
        raise

    except ProcessError as e:
        elapsed = time.time() - start_time
        logger.error(f"[{task_id}] 进程错误: 退出码 {e.exit_code}, 耗时 {elapsed:.1f}s")
        logger.error("=" * 50)
        raise

    except CLIJSONDecodeError as e:
        elapsed = time.time() - start_time
        logger.error(f"[{task_id}] JSON 解析错误: {e}, 耗时 {elapsed:.1f}s")
        logger.error("=" * 50)
        raise

    except Exception as e:
        elapsed = time.time() - start_time
        logger.error(f"[{task_id}] 未知错误: {type(e).__name__}: {e}, 耗时 {elapsed:.1f}s")
        logger.error("=" * 50)
        raise
