@echo off
REM Genera la aplicacion lista para copiar a la laptop de la sucursal.
REM Requiere: JDK 17+ (incluye jpackage) y Maven en el PATH.
REM Resultado: dist\SistemaInventarioPOS\SistemaInventarioPOS.exe (con su propio Java incluido).
REM Para un instalador .msi en lugar de una carpeta, instala WiX Toolset 3 y ejecuta: empaquetar.bat msi

setlocal
set TIPO=app-image
if /I "%1"=="msi" set TIPO=msi

call mvn -q clean package
if %errorlevel% neq 0 (
    echo Error al compilar o en las pruebas
    exit /b 1
)

if exist dist rmdir /s /q dist
mkdir dist\entrada
copy target\sistema-inventario-pos.jar dist\entrada\ >nul

set OPCIONES_MSI=
if "%TIPO%"=="msi" set OPCIONES_MSI=--win-menu --win-shortcut --win-dir-chooser --win-per-user-install

REM Las jlink-options conservan runtime\bin\javaw.exe (firmado por el proveedor de Java):
REM "Iniciar sistema.cmd" lo usa para arrancar aunque Windows bloquee el .exe sin firma.
jpackage --type %TIPO% --name SistemaInventarioPOS --app-version 1.1.0 ^
    --input dist\entrada --main-jar sistema-inventario-pos.jar --main-class com.inventario.Launcher ^
    --jlink-options "--strip-debug --no-man-pages --no-header-files" ^
    --dest dist %OPCIONES_MSI%
if %errorlevel% neq 0 (
    echo Error en jpackage
    exit /b 1
)

if "%TIPO%"=="app-image" (
    > "dist\SistemaInventarioPOS\Iniciar sistema.cmd" (
        echo @start "" "%%~dp0runtime\bin\javaw.exe" -jar "%%~dp0app\sistema-inventario-pos.jar" %%*
    )
    REM Doble clic en la laptop: crea en el escritorio el acceso directo "Sistema POS" a javaw.exe
    > "dist\SistemaInventarioPOS\Crear acceso directo.cmd" (
        echo @powershell -NoProfile -Command "$d='%%~dp0'; $s=(New-Object -ComObject WScript.Shell).CreateShortcut([Environment]::GetFolderPath('Desktop')+'\Sistema POS.lnk'); $s.TargetPath=$d+'runtime\bin\javaw.exe'; $s.Arguments='-jar \"'+$d+'app\sistema-inventario-pos.jar\"'; $s.WorkingDirectory=$d; $s.IconLocation=$d+'SistemaInventarioPOS.exe,0'; $s.Description='Sistema de Inventario y Punto de Venta'; $s.Save()"
        echo @echo Acceso directo "Sistema POS" creado en el escritorio.
        echo @pause
    )
)

rmdir /s /q dist\entrada
echo.
echo Listo. Revisa la carpeta dist
