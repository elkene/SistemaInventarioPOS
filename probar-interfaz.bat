@echo off
REM Prueba de punta a punta sobre la interfaz real (abre ventanas unos 40 segundos; no toques el mouse).
REM Usa una base de datos temporal: no toca tus datos. Capturas en target\capturas-interfaz.
REM Requiere JDK 17+ y Maven en el PATH.

call mvn -q test -Dui=true -Dtest=PruebaInterfazVentaTest
if %errorlevel% neq 0 (
    echo.
    echo FALLO la prueba de interfaz. Revisa target\surefire-reports y target\capturas-interfaz
    exit /b 1
)
echo.
echo Prueba de interfaz OK. Capturas en target\capturas-interfaz
