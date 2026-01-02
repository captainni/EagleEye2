#!/bin/bash
# EagleEye2 测试环境准备脚本
# 清理环境并重启所有服务，准备测试

set -e

# 项目根目录
PROJECT_DIR="/home/captain/projects/EagleEye2"
cd "$PROJECT_DIR"

echo "=== EagleEye2 测试环境准备 ==="
echo ""

# 1. 清空 crawler_task_log 表
echo "[1/4] 清空数据库任务日志..."
docker exec my-mysql mysql --default-character-set=utf8mb4 -ucaptain -p123456 eagleeye -e "DELETE FROM crawler_task_log;" 2>/dev/null
REMAINING=$(docker exec my-mysql mysql --default-character-set=utf8mb4 -ucaptain -p123456 eagleeye -e "SELECT COUNT(*) FROM crawler_task_log;" 2>/dev/null | tail -1)
echo "      crawler_task_log 已清空（剩余 $REMAINING 条）"
echo ""

# 2. 清空日志文件和爬取结果
echo "[2/4] 清空日志文件和爬取结果..."
mkdir -p logs
rm -f logs/*.log
rm -rf crawl_files/20* 2>/dev/null
echo "      日志目录已清空"
echo "      爬取结果已清空"
echo ""

# 3. 重启所有服务
echo "[3/4] 重启所有服务..."
./scripts/restart-all.sh
echo ""

# 4. 显示服务状态
echo "[4/4] 检查服务状态..."
echo ""

echo "=== 测试环境准备完成 ==="
echo ""
echo "服务访问地址："
echo "  前端: http://localhost:8088"
echo "  后端 API: http://localhost:9090/api/doc.html"
echo "  Proxy Service: http://localhost:8000/health"
echo ""
