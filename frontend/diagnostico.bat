@echo off
echo ============================================
echo   DIAGNOSTICO MAPA INTEGRADO
echo ============================================
echo.

echo [1/5] Verificando archivo .env...
if exist ".env" (
    echo [OK] Archivo .env existe
    type .env
) else (
    echo [ERROR] Archivo .env NO encontrado
    echo Por favor, asegurate de que existe el archivo .env en el directorio frontend
)
echo.

echo [2/5] Verificando package.json...
if exist "package.json" (
    echo [OK] package.json existe
) else (
    echo [ERROR] package.json NO encontrado
)
echo.

echo [3/5] Verificando node_modules...
if exist "node_modules" (
    echo [OK] node_modules existe
) else (
    echo [AVISO] node_modules NO encontrado
    echo Ejecuta: npm install
)
echo.

echo [4/5] Verificando servicios...
if exist "src\services\api.js" (
    echo [OK] api.js existe
) else (
    echo [ERROR] api.js NO encontrado
)

if exist "src\services\proyectosService.js" (
    echo [OK] proyectosService.js existe
) else (
    echo [ERROR] proyectosService.js NO encontrado
)

if exist "src\services\puntosInteresService.js" (
    echo [OK] puntosInteresService.js existe
) else (
    echo [ERROR] puntosInteresService.js NO encontrado
)

if exist "src\services\zonasService.js" (
    echo [OK] zonasService.js existe
) else (
    echo [ERROR] zonasService.js NO encontrado
)
echo.

echo [5/5] Verificando componente MapaIntegrado...
if exist "src\components\common\MapaIntegrado.vue" (
    echo [OK] MapaIntegrado.vue existe
) else (
    echo [ERROR] MapaIntegrado.vue NO encontrado
)

if exist "src\views\MapaIntegradoView.vue" (
    echo [OK] MapaIntegradoView.vue existe
) else (
    echo [ERROR] MapaIntegradoView.vue NO encontrado
)
echo.

echo ============================================
echo   DIAGNOSTICO COMPLETADO
echo ============================================
echo.
echo PROXIMOS PASOS:
echo 1. Si todo esta OK, ejecuta: npm run dev
echo 2. Abre http://localhost:5173
echo 3. Inicia sesion
echo 4. Ve a "Mapa Integrado"
echo 5. Abre la consola del navegador (F12) para ver los logs
echo.
pause

