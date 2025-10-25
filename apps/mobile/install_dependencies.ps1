# Script PowerShell para instalar dependências Flutter
# Execute: .\install_dependencies.ps1

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Flutter Dependencies Installation" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Navegar para o diretório mobile
Set-Location -Path "C:\repo\wine-reviewer\apps\mobile"

Write-Host "[1/2] Downloading dependencies (flutter pub get)..." -ForegroundColor Yellow
flutter pub get

if ($LASTEXITCODE -eq 0) {
    Write-Host "[✓] Dependencies downloaded successfully!" -ForegroundColor Green
} else {
    Write-Host "[✗] Failed to download dependencies" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/2] Verifying installation..." -ForegroundColor Yellow
flutter pub outdated

Write-Host ""
Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Installation Complete!" -ForegroundColor Green
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Claude Code will create the project structure (lib/features/, lib/core/, etc.)"
Write-Host "2. Then you can run: flutter run (to test on device/emulator)"
Write-Host ""
