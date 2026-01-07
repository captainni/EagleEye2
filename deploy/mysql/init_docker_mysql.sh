#!/bin/bash
# 在远端服务器上首次部署 Docker MySQL

echo "=== 初始化 Docker MySQL 容器 ==="

# 停止并删除旧容器（如果存在）
docker stop my-mysql 2>/dev/null
docker rm my-mysql 2>/dev/null

# 创建数据目录
sudo mkdir -p /data/mysql
sudo chown -R 999:999 /data/mysql

# 启动 MySQL 容器
docker run -d \
  --name my-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=eagleeye \
  -e MYSQL_USER=captain \
  -e MYSQL_PASSWORD=123456 \
  -v /data/mysql:/var/lib/mysql \
  mysql:8.0

echo "等待 MySQL 启动..."
sleep 20

# 检查容器状态
docker ps | grep my-mysql

echo "=== Docker MySQL 容器已启动 ==="
echo "现在可以执行 ./import.sh 导入数据"
