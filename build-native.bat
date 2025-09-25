@echo off
chcp 65001 >nul
REM TCP Demo Native Build Script for Windows
REM 构建原生二进制文件

echo 🚀 开始构建 TCP Demo 原生二进制文件...

REM 检查 GraalVM
where native-image >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到 GraalVM native-image 命令
    echo 请安装 GraalVM JDK 17:
    echo 1. 从 https://www.graalvm.org/downloads/ 下载
    echo 2. 或者使用 SDKMAN: sdk install java 17.0.7-graal
    pause
    exit /b 1
)

echo ✅ GraalVM 已安装

REM 检查Visual Studio Build Tools
where cl.exe >nul 2>nul
if %errorlevel% neq 0 (
    echo ⚠️  警告: 未找到Visual Studio C++编译器
    echo 在Windows上构建原生镜像需要Visual Studio Build Tools
    echo.
    echo 🔧 解决方案:
    echo 1. 安装Visual Studio Build Tools 2022:
    echo    https://visualstudio.microsoft.com/downloads/
    echo 2. 或者使用Podman构建: build-podman-native.bat
    echo.
    echo 是否继续尝试构建? (y/n)
    set /p choice=
    if /i "%choice%" neq "y" (
        echo 构建已取消
        pause
        exit /b 1
    )
)

REM 构建服务端
echo 📦 构建服务端原生二进制文件...
cd tcp-server
call mvn clean package -Pnative -DskipTests

if %errorlevel% equ 0 (
    echo ✅ 服务端构建成功!
    echo 📁 二进制文件位置: tcp-server\target\tcp-server-native.exe
    dir tcp-server\target\tcp-server-native.exe
    
    REM 复制外部配置文件到目标目录
    if exist "..\response-config-external.json" (
        copy "..\response-config-external.json" "tcp-server\target\response-config.json"
        echo 📋 已复制外部配置文件到: tcp-server\target\response-config.json
    )
) else (
    echo ❌ 服务端构建失败!
    pause
    exit /b 1
)

REM 构建客户端
echo 📦 构建客户端原生二进制文件...
cd ..\tcp-client
call mvn clean package -Pnative -DskipTests

if %errorlevel% equ 0 (
    echo ✅ 客户端构建成功!
    echo 📁 二进制文件位置: tcp-client\target\tcp-client-native.exe
    dir tcp-client\target\tcp-client-native.exe
    
    REM 复制外部配置文件到目标目录
    if exist "..\client-config-external.json" (
        copy "..\client-config-external.json" "tcp-client\target\client-config.json"
        echo 📋 已复制外部配置文件到: tcp-client\target\client-config.json
    )
) else (
    echo ❌ 客户端构建失败!
    pause
    exit /b 1
)

echo.
echo 🎉 所有原生二进制文件构建完成!
echo.
echo 🚀 运行方式:
echo 服务端: tcp-server\target\tcp-server-native.exe
echo 客户端: tcp-client\target\tcp-client-native.exe
echo.
echo 💡 优势:
echo - 启动速度: 毫秒级启动 (vs 秒级)
echo - 内存占用: 减少 50-80%%
echo - 无需 JVM: 直接运行二进制文件
echo - 部署简单: 单个文件即可运行

pause
