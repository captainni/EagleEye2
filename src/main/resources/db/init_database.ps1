# Initialize EagleEye Database PowerShell Script
Write-Host "=== Starting EagleEye Database Initialization ===" -ForegroundColor Cyan

$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$SQL_FILE = Join-Path -Path $SCRIPT_DIR -ChildPath "schema.sql"

if (-not (Test-Path $SQL_FILE)) {
    Write-Host "Error: SQL file does not exist at $SQL_FILE" -ForegroundColor Red
    exit 1
}

Write-Host "Using Docker container my-mysql to execute SQL script..." -ForegroundColor Yellow

try {
    Get-Content $SQL_FILE | docker exec -i my-mysql mysql -u root -p123456
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "=== Database initialization successful ===" -ForegroundColor Green
        Write-Host "You can now connect to the database with:" -ForegroundColor Green
        Write-Host "docker exec -it my-mysql mysql -u captain -p123456 eagleeye" -ForegroundColor White
    } else {
        Write-Host "=== Database initialization failed, please check error messages ===" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "An error occurred: $_" -ForegroundColor Red
    exit 1
}

Write-Host "Initialization script completed" -ForegroundColor Cyan
Read-Host "Press Enter to continue" 