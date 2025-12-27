@echo off
chcp 65001 >nul
echo === 开始初始化EagleEye数据库 ===

set SCRIPT_DIR=%~dp0
set SQL_FILE=%SCRIPT_DIR%schema.sql

if not exist "%SQL_FILE%" (
    echo 错误：SQL文件不存在 %SQL_FILE%
    goto :error
)

echo 使用Docker容器my-mysql执行SQL脚本...

docker exec -i my-mysql mysql -u root -p123456 < "%SQL_FILE%"

if %ERRORLEVEL% EQU 0 (
    echo === 数据库初始化成功 ===
    echo 现在你可以通过以下命令连接到数据库：
    echo docker exec -it my-mysql mysql -u captain -p123456 eagleeye
) else (
    echo === 数据库初始化失败，请检查错误信息 ===
    goto :error
)

goto :end

:error
echo 执行过程中出现错误，请检查以上信息
exit /b 1

:end
echo 初始化脚本执行完成
pause 