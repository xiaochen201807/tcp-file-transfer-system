@echo off
chcp 65001 >nul
REM Podman构建和运行脚本

echo 🐳 使用Podman构建TCP Demo原生版本...

REM 检查Podman是否安装
podman --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到Podman
    echo 请安装Podman Desktop: https://podman-desktop.io/
    pause
    exit /b 1
)

echo ✅ Podman已安装

REM 构建Podman镜像
echo 📦 构建Podman镜像...
podman build -f Dockerfile.native -t tcp-demo-native .

if %errorlevel% equ 0 (
    echo ✅ Podman镜像构建成功!
) else (
    echo ❌ Podman镜像构建失败!
    pause
    exit /b 1
)

REM 运行Podman容器
echo 🚀 启动Podman容器...
podman run -it --rm -p 8080:8080 -p 8081:8081 tcp-demo-native

pause
