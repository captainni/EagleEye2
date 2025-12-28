#!/bin/bash
# EagleEye2 服务停止脚本
# 一键停止所有服务

set -e

echo "=== EagleEye2 服务停止 ==="
echo ""

# 计数函数
count_procs() {
    pgrep -f "$1" 2>/dev/null | wc -l
}

# 1. 停止 Proxy Service
echo "[1/4] 停止 Proxy Service..."
if pgrep -f "proxy-service/main.py" > /dev/null; then
    pkill -f "proxy-service/main.py"
    sleep 1
    # 强制清理（如果还在运行）
    pkill -9 -f "proxy-service/main.py" 2>/dev/null || true
    echo "      Proxy Service 已停止"
else
    echo "      Proxy Service 未运行"
fi
echo ""

# 2. 停止前端
echo "[2/4] 停止前端服务..."
if pgrep -f "EagleEye2/web.*vite" > /dev/null; then
    pkill -f "EagleEye2/web.*vite"
    sleep 1
    # 强制清理
    pkill -9 -f "EagleEye2/web.*vite" 2>/dev/null || true
    echo "      前端服务已停止"
else
    echo "      前端服务未运行"
fi
echo ""

# 3. 停止后端
echo "[3/4] 停止后端服务..."
if pgrep -f "spring-boot:run" > /dev/null; then
    pkill -f "spring-boot:run"
    sleep 1
    # 强制清理
    pkill -9 -f "spring-boot:run" 2>/dev/null || true
    echo "      后端服务已停止"
else
    echo "      后端服务未运行"
fi
echo ""

# 4. MySQL (可选停止)
echo "[4/4] MySQL..."
echo "      MySQL 保持运行 (如需停止: docker stop my-mysql)"
echo ""

echo "=== 所有服务已停止 ==="
echo ""

# 显示剩余进程
REMAINING=$(pgrep -f "spring-boot:run|vite.*web|proxy-service/main.py" 2>/dev/null | wc -l)
if [ "$REMAINING" -gt 0 ]; then
    echo "警告: 仍有 $REMAINING 个相关进程在运行"
    pgrep -f "spring-boot:run|vite.*web|proxy-service/main.py" 2>/dev/null
else
    echo "所有服务已干净退出"
fi
