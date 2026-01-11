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


def format_tool_input(tool_name: str, tool_input: dict) -> str:
    """
    格式化工具输入参数为易读的描述

    Args:
        tool_name: 工具名称
        tool_input: 工具输入参数字典

    Returns:
        str: 格式化后的描述（最大 80 字符）
    """
    try:
        if tool_name == "Bash":
            cmd = tool_input.get("command", "")
            # 提取命令的关键部分
            if cmd.startswith("cd "):
                parts = cmd[3:].split()
                return f"cd {parts[0]}" if parts else "cd"
            elif cmd.startswith("curl "):
                # 提取 URL
                import shlex
                parts = shlex.split(cmd)
                for part in parts:
                    if part.startswith("http"):
                        return f"curl {part[:60]}"
                return "curl ..."
            elif "&&" in cmd:
                # 显示第一个命令
                first_cmd = cmd.split("&&")[0].strip()
                return first_cmd[:60]
            else:
                # 显示命令的前 60 个字符
                return cmd[:60]

        elif tool_name == "Read":
            path = tool_input.get("file_path", "")
            return f"Read: {path}"

        elif tool_name == "Write":
            path = tool_input.get("file_path", "")
            return f"Write: {path}"

        elif tool_name == "Edit":
            path = tool_input.get("file_path", "")
            return f"Edit: {path}"

        elif tool_name == "Glob":
            pattern = tool_input.get("pattern", "")
            return f"Glob: {pattern[:50]}"

        elif tool_name == "Grep":
            pattern = tool_input.get("pattern", "")
            path = tool_input.get("path", ".")
            return f"Grep: {pattern[:30]} in {path[:30]}"

        elif tool_name == "Skill":
            skill = tool_input.get("skill", "")
            return f"Skill: {skill}"

        elif tool_name == "TodoWrite":
            return "TodoWrite"

        elif tool_name == "Task":
            subagent_type = tool_input.get("subagent_type", "")
            description = tool_input.get("description", "")
            return f"Task: {subagent_type} - {description[:30]}"

        elif tool_name.startswith("mcp__"):
            # MCP 工具
            return f"MCP: {tool_name[:40]}"

        else:
            # 其他工具，显示输入的字符串表示
            return str(tool_input)[:60]

    except Exception:
        # 格式化失败时返回简单描述
        return f"{tool_name} (input)"


async def query_claude(
    prompt: str,
    logger: logging.Logger,
    timeout: int = 600,
    allowed_tools: Optional[list[str]] = None,
    working_dir: str = "/home/captain/projects/EagleEye2",
    max_turns: int = 50,
    task_name: str = "通用任务",
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
        task_name: 任务名称（用于日志标识，如"政策分析"、"竞品分析"）

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
    logger.info(f"[{task_id}] 任务类型: {task_name}")
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
                                "input": block.input,  # 保存原始字典用于格式化
                            }
                            tool_calls.append(tool_info)
                            # 实时记录格式化的工具描述
                            tool_desc = format_tool_input(block.name, block.input)
                            logger.info(f"[{task_id}] 工具调用: {tool_desc}")

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
                desc = format_tool_input(call['name'], call.get('input', {}))
                logger.info(f"[{task_id}]   {i}. {desc}")

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
