# ==========================================
# Script de Configuração de Ambiente (Windows)
# ==========================================
# Este script ajuda a configurar as variáveis
# de ambiente para o projeto SDR Agent

Write-Host "🚀 Configurando ambiente do SDR Agent..." -ForegroundColor Cyan
Write-Host ""

# Verifica se o arquivo já existe
if (Test-Path "application-local.properties") {
    Write-Host "⚠️  Arquivo application-local.properties já existe!" -ForegroundColor Yellow
    $resposta = Read-Host "Deseja sobrescrever? (s/N)"
    if ($resposta -ne "s" -and $resposta -ne "S") {
        Write-Host "❌ Operação cancelada." -ForegroundColor Red
        exit 0
    }
}

# Copia o template
Copy-Item "application-local.properties.example" "application-local.properties"

Write-Host "✅ Arquivo application-local.properties criado!" -ForegroundColor Green
Write-Host ""
Write-Host "📝 Agora você precisa editar o arquivo e preencher com suas credenciais:" -ForegroundColor White
Write-Host ""
Write-Host "   notepad application-local.properties" -ForegroundColor Yellow
Write-Host "   ou" -ForegroundColor White
Write-Host "   code application-local.properties" -ForegroundColor Yellow
Write-Host ""
Write-Host "🔑 Onde obter as credenciais:" -ForegroundColor White
Write-Host ""
Write-Host "   OpenAI:   https://platform.openai.com/api-keys" -ForegroundColor Cyan
Write-Host "   Calendly: https://calendly.com/integrations/api_webhooks" -ForegroundColor Cyan
Write-Host "   Pipefy:   https://app.pipefy.com/tokens" -ForegroundColor Cyan
Write-Host ""
Write-Host "📖 Para mais informações, leia: CONFIGURACAO.md" -ForegroundColor White
Write-Host ""

# Pergunta se deseja abrir o arquivo agora
$abrir = Read-Host "Deseja abrir o arquivo agora? (s/N)"
if ($abrir -eq "s" -or $abrir -eq "S") {
    if (Get-Command "code" -ErrorAction SilentlyContinue) {
        code application-local.properties
    } else {
        notepad application-local.properties
    }
}

