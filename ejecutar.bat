@echo off
REM Script para ejecutar el Sistema de Inventario POS
REM Requiere Maven y Java instalados

echo.
echo ========================================
echo Sistema de Inventario y POS
echo ========================================
echo.

REM Limpiar y compilar
echo [1/3] Limpiando proyecto...
call mvn clean
if %errorlevel% neq 0 (
    echo Error al limpiar el proyecto
    pause
    exit /b 1
)

echo [2/3] Compilando proyecto...
call mvn compile
if %errorlevel% neq 0 (
    echo Error al compilar el proyecto
    pause
    exit /b 1
)

echo [3/3] Ejecutando aplicacion...
call mvn javafx:run

pause
