#!/bin/bash
# EagleEye2 服务重启脚本
# 一键重启所有服务

set -e

echo "=== EagleEye2 服务重启 ==="
echo ""

# 停止所有服务
./scripts/stop-all.sh

echo ""
echo "等待服务完全停止..."
sleep 3
echo ""

# 启动所有服务
./scripts/start-all.sh
