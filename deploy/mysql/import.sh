#!/bin/bash
# 在远端服务器上执行的导入脚本

echo "=== 开始导入 EagleEye 数据库 ==="

# 检查 Docker MySQL 容器是否运行
if ! docker ps | grep -q my-mysql; then
    echo "错误: MySQL 容器未运行，请先启动"
    exit 1
fi

# 导入数据库
echo "正在导入数据库..."
docker exec -i my-mysql mysql -u root -p123456 eagleeye < eagleeye_full_dump.sql

if [ $? -eq 0 ]; then
    echo "=== 数据库导入成功 ==="
else
    echo "=== 数据库导入失败 ==="
    exit 1
fi
