@echo off
REM Script para gerar chave secreta no Windows (CMD)
REM Uso: gerar-chave-secreta.bat

echo.
echo 🔐 Gerando chave secreta...
echo.

REM Usar PowerShell para gerar a chave
for /f "delims=" %%i in ('powershell -NoProfile -Command "$bytes = New-Object byte[] 64; [System.Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes); -join ($bytes ^| ForEach-Object { '{0:x2}' -f $_ })"') do set SECRET_KEY=%%i

echo ========================================================================
echo.
echo 🔐 Chave Secreta Gerada com Sucesso!
echo.
echo %SECRET_KEY%
echo.
echo ========================================================================
echo.
echo 📋 Instruções:
echo 1. Copie a chave acima (Ctrl+C)
echo 2. Acesse: https://seu-dockploy.com
echo 3. Settings → Environment Variables
echo 4. Nome: APP_SIGNED_URL_SECRET
echo 5. Valor: Cole a chave
echo 6. Deploy (Redeploy)
echo.
echo ⚠️  IMPORTANTE:
echo    - NÃO compartilhe esta chave
echo    - NÃO coloque no GIT
echo    - Guarde em local seguro (Bitwarden, 1Password, etc)
echo.
