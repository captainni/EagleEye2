#!/bin/bash
# EagleEye2 服务启动脚本
# 一键启动所有服务

set -e

# 项目根目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "=== EagleEye2 服务启动 ==="
echo ""

# 1. 创建 logs 目录并清空旧日志
echo "[1/5] 创建 logs 目录并清空旧日志..."
mkdir -p logs
rm -f logs/*.log
echo "      logs 目录已就绪，旧日志已清空"
echo ""

# 2. 启动 MySQL (Docker)
echo "[2/5] 启动 MySQL..."
if docker start my-mysql 2>/dev/null; then
    echo "      MySQL 已启动"
elif docker ps | grep -q my-mysql; then
    echo "      MySQL 已在运行"
else
    echo "      警告: MySQL 容器不存在或未配置"
fi
echo ""

# 3. 启动后端
echo "[3/5] 启动后端服务..."
if pgrep -f "spring-boot:run" > /dev/null; then
    echo "      停止旧后端进程..."
    pkill -f "spring-boot:run" || true
    sleep 2
fi
nohup mvn spring-boot:run > logs/backend.log 2>&1 &
echo "      后端启动中... (日志: logs/backend.log)"
echo ""

# 4. 启动前端
echo "[4/5] 启动前端服务..."
if pgrep -f "EagleEye2/web.*vite" > /dev/null; then
    echo "      停止旧前端进程..."
    pkill -f "EagleEye2/web.*vite" || true
    sleep 2
fi
cd web
nohup npm run dev > ../logs/frontend.log 2>&1 &
cd "$PROJECT_DIR"
echo "      前端启动中... (日志: logs/frontend.log)"
echo ""

# 5. 启动 Proxy Service
echo "[5/5] 启动 Proxy Service..."
if pgrep -f "proxy-service/main.py" > /dev/null; then
    echo "      停止旧 Proxy Service 进程..."
    pkill -f "proxy-service/main.py" || true
    sleep 2
fi
nohup ./venv/bin/python proxy-service/main.py > logs/proxy.log 2>&1 &
echo "      Proxy Service 启动中... (日志: logs/proxy.log)"
echo ""

# 等待服务启动并显示状态
echo "等待服务启动..."
sleep 5
echo ""

# 显示服务状态
./scripts/status.sh
