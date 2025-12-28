#!/bin/bash
# EagleEye2 服务状态检查脚本

# 项目根目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=== EagleEye2 服务状态 ==="
echo ""

# 1. MySQL
echo -n "1. MySQL (Docker): "
if docker ps | grep -q my-mysql; then
    echo -e "${GREEN}运行中${NC}"
    docker ps --format "   table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep my-mysql
else
    echo -e "${RED}未运行${NC}"
fi
echo ""

# 2. 后端
echo -n "2. 后端服务 (端口 9090): "
if pgrep -f "spring-boot:run" > /dev/null; then
    PID=$(pgrep -f "spring-boot:run" | head -1)
    echo -e "${GREEN}运行中${NC} (PID: $PID)"
    if netstat -tlnp 2>/dev/null | grep -q 9090 || ss -tlnp 2>/dev/null | grep -q 9090; then
        echo "   端口 9090: ${GREEN}正常${NC}"
    else
        echo "   端口 9090: ${YELLOW}启动中${NC}"
    fi
else
    echo -e "${RED}未运行${NC}"
fi
echo ""

# 3. 前端
echo -n "3. 前端服务 (端口 8088-8091): "
# 匹配 EagleEye2/web 目录下的 vite 进程
if pgrep -f "EagleEye2/web.*vite" > /dev/null; then
    PID=$(pgrep -f "EagleEye2/web.*vite" -o | head -1)
    echo -e "${GREEN}运行中${NC} (PID: $PID)"
    # 检查可能使用的端口
    for port in 8088 8089 8090 8091; do
        if netstat -tlnp 2>/dev/null | grep -q ":$port " || ss -tlnp 2>/dev/null | grep -q ":$port "; then
            echo "   端口 $port: ${GREEN}监听中${NC}"
            break
        fi
    done
else
    echo -e "${RED}未运行${NC}"
fi
echo ""

# 4. Proxy Service
echo -n "4. Proxy Service (端口 8000): "
if pgrep -f "proxy-service/main.py" > /dev/null; then
    PID=$(pgrep -f "proxy-service/main.py" | head -1)
    echo -e "${GREEN}运行中${NC} (PID: $PID)"
    if netstat -tlnp 2>/dev/null | grep -q 8000 || ss -tlnp 2>/dev/null | grep -q 8000; then
        echo "   端口 8000: ${GREEN}正常${NC}"
    else
        echo "   端口 8000: ${YELLOW}启动中${NC}"
    fi
else
    echo -e "${RED}未运行${NC}"
fi
echo ""

# 5. 日志文件
echo "=== 日志文件 ==="
if [ -d "logs" ]; then
    ls -lh logs/*.log 2>/dev/null | awk '{print "   " $9 " (" $5 ")"}' || echo "   日志目录为空"
    echo ""
    echo "   查看日志命令:"
    echo "   tail -f logs/backend.log      # 后端日志"
    echo "   tail -f logs/frontend.log     # 前端日志"
    echo "   tail -f logs/proxy.log        # Proxy 日志"
    echo "   tail -f logs/claude-cli.log   # Claude CLI 日志"
else
    echo "   logs 目录不存在"
fi
echo ""

# 6. 快速访问链接
echo "=== 快速访问 ==="
echo "   后端 API 文档: http://localhost:9090/api/doc.html"
echo "   前端页面: http://localhost:8088"
echo "   Proxy 健康: http://localhost:8000/health"
