# Script para ejecutar el Sistema de Inventario POS
# Requiere Maven y Java instalados

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Sistema de Inventario y POS" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/3] Limpiando proyecto..." -ForegroundColor Yellow
mvn clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al limpiar el proyecto" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host "[2/3] Compilando proyecto..." -ForegroundColor Yellow
mvn compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error al compilar el proyecto" -ForegroundColor Red
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host "[3/3] Ejecutando aplicacion..." -ForegroundColor Yellow
mvn javafx:run

Read-Host "Presiona Enter para salir"
