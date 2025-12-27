#!/bin/bash

# EagleEye数据库初始化脚本
# 作者：开发团队
# 创建日期：2025-04-26

echo "=== 开始初始化EagleEye数据库 ==="

# 确保脚本可执行
chmod +x "$0"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SQL_FILE="$SCRIPT_DIR/schema.sql"

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "错误：SQL文件不存在 $SQL_FILE"
    exit 1
fi

echo "使用Docker容器my-mysql执行SQL脚本..."

# 使用Docker执行SQL脚本
docker exec -i my-mysql mysql -u root -p123456 < "$SQL_FILE"

# 检查执行结果
if [ $? -eq 0 ]; then
    echo "=== 数据库初始化成功 ==="
    echo "现在你可以通过以下命令连接到数据库："
    echo "docker exec -it my-mysql mysql -u captain -p123456 eagleeye"
else
    echo "=== 数据库初始化失败，请检查错误信息 ==="
fi

# 创建Windows批处理版本，便于Windows用户使用
echo "@echo off" > "$SCRIPT_DIR/init_database.bat"
echo "echo === 开始初始化EagleEye数据库 ===" >> "$SCRIPT_DIR/init_database.bat"
echo "set SCRIPT_DIR=%~dp0" >> "$SCRIPT_DIR/init_database.bat"
echo "set SQL_FILE=%SCRIPT_DIR%init_database.sql" >> "$SCRIPT_DIR/init_database.bat"
echo "echo 使用Docker容器my-mysql执行SQL脚本..." >> "$SCRIPT_DIR/init_database.bat"
echo "docker exec -i my-mysql mysql -u root -p123456 < \"%SQL_FILE%\"" >> "$SCRIPT_DIR/init_database.bat"
echo "if %ERRORLEVEL% EQU 0 (" >> "$SCRIPT_DIR/init_database.bat"
echo "    echo === 数据库初始化成功 ===" >> "$SCRIPT_DIR/init_database.bat"
echo "    echo 现在你可以通过以下命令连接到数据库：" >> "$SCRIPT_DIR/init_database.bat"
echo "    echo docker exec -it my-mysql mysql -u captain -p123456 eagleeye" >> "$SCRIPT_DIR/init_database.bat"
echo ") else (" >> "$SCRIPT_DIR/init_database.bat"
echo "    echo === 数据库初始化失败，请检查错误信息 ===" >> "$SCRIPT_DIR/init_database.bat"
echo ")" >> "$SCRIPT_DIR/init_database.bat"
echo "pause" >> "$SCRIPT_DIR/init_database.bat"

echo "已创建Windows批处理文件: $SCRIPT_DIR/init_database.bat" 