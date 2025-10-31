#!/bin/bash

# ==========================================
# Script de Configuração de Ambiente
# ==========================================
# Este script ajuda a configurar as variáveis
# de ambiente para o projeto SDR Agent

echo "🚀 Configurando ambiente do SDR Agent..."
echo ""

# Verifica se o arquivo já existe
if [ -f "application-local.properties" ]; then
    echo "⚠️  Arquivo application-local.properties já existe!"
    read -p "Deseja sobrescrever? (s/N): " resposta
    if [ "$resposta" != "s" ] && [ "$resposta" != "S" ]; then
        echo "❌ Operação cancelada."
        exit 0
    fi
fi

# Copia o template
cp application-local.properties.example application-local.properties

echo "✅ Arquivo application-local.properties criado!"
echo ""
echo "📝 Agora você precisa editar o arquivo e preencher com suas credenciais:"
echo ""
echo "   nano application-local.properties"
echo "   ou"
echo "   vim application-local.properties"
echo ""
echo "🔑 Onde obter as credenciais:"
echo ""
echo "   OpenAI:   https://platform.openai.com/api-keys"
echo "   Calendly: https://calendly.com/integrations/api_webhooks"
echo "   Pipefy:   https://app.pipefy.com/tokens"
echo ""
echo "📖 Para mais informações, leia: CONFIGURACAO.md"
echo ""

