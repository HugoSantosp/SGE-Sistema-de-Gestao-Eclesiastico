#!/usr/bin/env powershell
# Script de teste de Signed URLs em DEV (Windows)
# Uso: .\teste-dev-signed-urls.ps1

$BASE_URL = "http://localhost:8080"

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🔐 Teste de Signed URLs - DEV" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Teste 1: Acesso sem token (deve retornar 403)
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "Teste 1: Acesso SEM token" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

try {
    $response = Invoke-WebRequest -Uri "$BASE_URL/api/uploads/teste.png" -Method HEAD -ErrorAction SilentlyContinue
    $statusCode = $response.StatusCode
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
}

if ($statusCode -eq 403) {
    Write-Host "✅ Retornou 403 Forbidden (correto)" -ForegroundColor Green
} else {
    Write-Host "❌ Esperado 403, recebeu $statusCode" -ForegroundColor Red
    Write-Host "⚠️  signed-url.enabled pode estar false" -ForegroundColor Yellow
}

Write-Host ""

# Teste 2: Login
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "Teste 2: Login e obter JWT" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

$loginBody = @{
    user = "admin@example.com"
    senha = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $jwtToken = $loginResponse.token
    
    Write-Host "✅ Login realizado com sucesso" -ForegroundColor Green
    Write-Host "ℹ️  JWT: $($jwtToken.Substring(0, 50))..." -ForegroundColor Cyan
} catch {
    Write-Host "❌ Falha no login. Verifique credenciais." -ForegroundColor Red
    Write-Host "   Erro: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Teste 3: Obter perfil com URL assinada
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "Teste 3: Obter perfil com URL assinada" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host ""

$headers = @{
    Authorization = "Bearer $jwtToken"
}

try {
    $perfilResponse = Invoke-RestMethod -Uri "$BASE_URL/auth/me" -Method GET -Headers $headers
    $fotoUrl = $perfilResponse.foto
    
    Write-Host "✅ Perfil obtido com sucesso" -ForegroundColor Green
    Write-Host "ℹ️  Foto URL: $fotoUrl" -ForegroundColor Cyan
    
    # Verificar se contém token e expires
    if ($fotoUrl -match "token=" -and $fotoUrl -match "expires=") {
        Write-Host "✅ URL contém token e expires (assinatura correta)" -ForegroundColor Green
    } else {
        Write-Host "❌ URL NÃO contém token/expires" -ForegroundColor Red
        Write-Host "⚠️  Implementação pode estar incompleta" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Falha ao obter perfil" -ForegroundColor Red
    Write-Host "   Erro: $_" -ForegroundColor Red
}

Write-Host ""

# Teste 4: Acessar foto com token válido
if ($fotoUrl) {
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host "Teste 4: Acessar foto COM token válido" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host ""
    
    $fullUrl = "$BASE_URL$fotoUrl"
    
    try {
        $response = Invoke-WebRequest -Uri $fullUrl -Method HEAD -ErrorAction SilentlyContinue
        $statusCode = $response.StatusCode
        
        if ($statusCode -eq 200) {
            Write-Host "✅ Foto acessada com sucesso (200 OK)" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Status code: $statusCode" -ForegroundColor Yellow
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        
        if ($statusCode -eq 404) {
            Write-Host "⚠️  Arquivo não encontrado (404)" -ForegroundColor Yellow
            Write-Host "ℹ️  Usuário tem foto configurada mas arquivo não existe no disco" -ForegroundColor Cyan
        } else {
            Write-Host "❌ Erro ao acessar foto: $statusCode" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    
    # Teste 5: Acessar foto SEM token
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host "Teste 5: Acessar foto SEM token" -ForegroundColor Yellow
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
    Write-Host ""
    
    # Extrair apenas o caminho (sem token/expires)
    $filePath = $fotoUrl -replace '\?.*$', ''
    $filePathFull = "$BASE_URL$filePath"
    
    try {
        $response = Invoke-WebRequest -Uri $filePathFull -Method HEAD -ErrorAction SilentlyContinue
        $statusCode = $response.StatusCode
        
        Write-Host "❌ Esperado 403, recebeu $statusCode" -ForegroundColor Red
        Write-Host "⚠️  Validação de token pode estar desabilitada" -ForegroundColor Yellow
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        
        if ($statusCode -eq 403) {
            Write-Host "✅ Acesso bloqueado sem token (403 Forbidden)" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Status code inesperado: $statusCode" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "📊 Resumo dos Testes" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Implementação de Signed URLs testada!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Próximos Passos:" -ForegroundColor Yellow
Write-Host "1. Se todos os testes passaram, faça commit" -ForegroundColor White
Write-Host "2. Push para o repositório" -ForegroundColor White
Write-Host "3. Deploy no Dockploy (produção)" -ForegroundColor White
Write-Host "4. Testar em produção" -ForegroundColor White
Write-Host ""
