#!/usr/bin/env powershell
# Script para gerar chave secreta no Windows (PowerShell)
# Uso: .\gerar-chave-secreta.ps1

[System.Security.Cryptography.RNGCryptoServiceProvider] | Out-Null

# Gerar 64 bytes (512 bits) de aleatoriedade
$bytes = New-Object byte[] 64
[System.Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes)

# Converter para hexadecimal
$secretKey = -join ($bytes | ForEach-Object { "{0:x2}" -f $_ })

Write-Host ""
Write-Host "🔐 Chave Secreta Gerada com Sucesso!" -ForegroundColor Green
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""
Write-Host $secretKey -ForegroundColor Cyan
Write-Host ""
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""
Write-Host "📋 Instruções:" -ForegroundColor Green
Write-Host "1. Copie a chave acima (Ctrl+C)"
Write-Host "2. Acesse: https://seu-dockploy.com"
Write-Host "3. Settings → Environment Variables"
Write-Host "4. Nome: APP_SIGNED_URL_SECRET"
Write-Host "5. Valor: Cole a chave"
Write-Host "6. Deploy"
Write-Host ""
Write-Host "⚠️  IMPORTANTE:" -ForegroundColor Yellow
Write-Host "   - NÃO compartilhe esta chave"
Write-Host "   - NÃO coloque no GIT"
Write-Host "   - Guarde em local seguro"
Write-Host ""

# Copiar para clipboard (opcional)
if ($PSVersionTable.PSVersion.Major -ge 6) {
    $secretKey | Set-Clipboard
    Write-Host "✅ Chave copiada para clipboard!" -ForegroundColor Green
}
