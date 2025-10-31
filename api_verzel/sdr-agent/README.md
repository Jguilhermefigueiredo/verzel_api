# SDR Agent - Assistente Virtual Automatizado

Agente SDR (Sales Development Representative) automatizado que atende leads, coleta informações e agenda reuniões via Calendly, integrando com OpenAI e Pipefy.

---

## 🎯 Funcionalidades

- ✅ Chat conversacional via WebSocket e REST API
- ✅ Coleta de dados do lead (nome, empresa, email, necessidades)
- ✅ Sugestão de horários disponíveis para reunião
- ✅ Agendamento automático via Calendly
- ✅ Registro de leads no Pipefy
- ✅ Persistência de conversas no SQLite
- ✅ API REST completa para integração frontend

---

## 📋 Requisitos

- **Java 21**
- **Maven 3.8+**
- **Credenciais das APIs:**
  - OpenAI API Key
  - Calendly API Token
  - Pipefy API Token

---

## ⚙️ Configuração

### 1. Clonar o repositório

```bash
git clone https://github.com/Jguilhermefigueiredo/api_verzel.git
cd api_verzel/sdr-agent
```

### 2. Configurar variáveis de ambiente

#### Windows:
```powershell
.\setup-env.ps1
```

#### Linux/Mac:
```bash
chmod +x setup-env.sh
./setup-env.sh
```

Ou copie manualmente:
```bash
cp application-local.properties.example application-local.properties
```

### 3. Editar credenciais

Abra `application-local.properties` e preencha:

```properties
# OpenAI
spring.ai.openai.api-key=sk-proj-SUA-CHAVE-AQUI
openai.api.key=sk-proj-SUA-CHAVE-AQUI

# Calendly
calendly.api.token=SEU-TOKEN-CALENDLY

# Pipefy
pipefy.api.token=SEU-TOKEN-PIPEFY
pipefy.pipe.id=SEU-PIPE-ID
```

#### Onde obter:
- **OpenAI**: https://platform.openai.com/api-keys
- **Calendly**: https://calendly.com/integrations/api_webhooks
- **Pipefy**: https://app.pipefy.com/tokens

---

## 🚀 Como Rodar

### Desenvolvimento:

```bash
mvn spring-boot:run
```

### Produção:

```bash
mvn clean package
java -jar target/sdr-agent-0.0.1-SNAPSHOT.jar
```

**Aplicação estará disponível em:** `http://localhost:8080`

---

## 📡 Endpoints Principais

### Webchat API (`/api/webchat`)

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/session` | POST | Inicia nova sessão de chat |
| `/message` | POST | Envia mensagem e recebe resposta |
| `/history/{sessionId}` | GET | Recupera histórico da conversa |
| `/slots` | GET | Lista horários disponíveis |
| `/schedule` | POST | Agenda reunião |
| `/health` | GET | Status do serviço |

### Exemplo de uso:

```bash
# 1. Iniciar sessão
curl -X POST http://localhost:8080/api/webchat/session

# 2. Enviar mensagem
curl -X POST http://localhost:8080/api/webchat/message \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "uuid-da-sessao",
    "message": "Olá, gostaria de agendar uma reunião",
    "role": "user"
  }'

# 3. Ver horários disponíveis
curl http://localhost:8080/api/webchat/slots

# 4. Agendar reunião
curl -X POST http://localhost:8080/api/webchat/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "uuid-da-sessao",
    "slot": "2025-10-30T10:00:00-03:00",
    "nome": "João Silva",
    "email": "joao@empresa.com",
    "empresa": "Empresa XYZ"
  }'
```

### Testes no Postman:

Importe a collection: `Webchat_API.postman_collection.json`

---

## 🗂️ Estrutura do Projeto

```
src/main/java/com/sdr/sdr_agent/
├── canonicals/          # DTOs de entrada/saída
├── chat/                # Lógica de chat e orquestração
│   ├── dto/             # DTOs do webchat
│   ├── AssistantOrchestrator.java
│   ├── ChatService.java
│   └── ChatWebSocketHandler.java
├── config/              # Configurações (WebClient)
├── controller/          # Endpoints REST
│   ├── WebchatController.java
│   ├── ChatController.java
│   └── LeadController.java
├── lead/                # Entidade Lead
├── services/            # Serviços de integração
│   ├── CalendlyService.java
│   ├── PipefyService.java
│   ├── OpenAIService.java
│   └── LeadServices.java
└── repository/          # Acesso ao banco de dados
```

---

## ⚠️ Limitações / Pontos Não Implementados

### 🔴 **Não Implementado:**

1. **Integração Real com OpenAI Assistants API**
   - Atualmente usa respostas básicas pré-programadas
   - Não há chamada real para `gpt-4o-mini` com function calling
   - `AssistantOrchestrator` está preparado mas não conectado à OpenAI

2. **Calendly - Chamadas Reais à API**
   - `scheduleMeeting()` monta o payload mas não faz chamada real
   - Retorna link placeholder: `"https://calendly.com/fake-link"`
   - `getAvailableSlotsNext7Days()` gera horários mock (não consulta API real)

3. **Pipefy - Validação de Field IDs**
   - Os `field_id` nos mutations estão hardcoded como strings genéricas
   - Não foram validados com o Pipe real
   - Pode falhar ao criar/atualizar cards se IDs não corresponderem

4. **WebSocket - Implementação Incompleta**
   - `ChatWebSocketHandler` está configurado mas não integrado
   - Não há endpoint WebSocket funcional
   - Chat funciona apenas via REST

5. **Autenticação / Segurança**
   - Não há autenticação nos endpoints
   - CORS configurado como `*` (aceita qualquer origem)
   - Sem rate limiting
   - Sem validação de tokens de sessão

6. **Interface Frontend**
   - Não existe interface web implementada
   - Apenas API REST disponível
   - Frontend precisa ser desenvolvido separadamente

### 🟡 **Implementado Parcialmente:**

1. **Horários Disponíveis**
   - ✅ Gera horários mock realistas (dias úteis, 9h-17h)
   - ❌ Não consulta disponibilidade real do Calendly

2. **OpenAI Service**
   - ✅ Estrutura pronta para chamadas
   - ❌ Não configurado para usar Assistants API
   - ❌ Sem definição de functions/tools

3. **Processamento de Mensagens**
   - ✅ Salva mensagens no banco
   - ❌ Não há IA processando as mensagens
   - ❌ Respostas são baseadas em regras simples (if/else)

---

## ✅ O Que Está Funcionando

- ✅ Iniciar sessão de chat com UUID único
- ✅ Enviar e receber mensagens (persistidas no banco)
- ✅ Recuperar histórico de conversas
- ✅ Listar horários disponíveis (mock)
- ✅ CRUD completo de leads
- ✅ Integração básica com Pipefy (estrutura pronta)
- ✅ Persistência com SQLite/JPA
- ✅ Logs informativos

---

## 🔧 Como Completar a Implementação

### 1. **Integrar OpenAI Assistants API**

```java
// Em OpenAIService.java, implementar:
public Map<String, Object> callAssistant(String message, List<ChatMessage> history) {
    // Criar thread
    // Adicionar mensagem
    // Executar assistant com functions definidas
    // Processar tool_calls se houver
    // Retornar resposta
}
```

### 2. **Implementar Calendly Real**

```java
// Em CalendlyService.java:
public List<String> getAvailableSlotsNext7Days() {
    // GET https://api.calendly.com/user_availability_schedules
    // Parsear resposta
    // Retornar slots reais
}

public ScheduleResult scheduleMeeting(Lead lead, String slot) {
    // POST https://api.calendly.com/scheduled_events
    // Retornar link real do response
}
```

### 3. **Validar Pipefy Field IDs**

1. Acessar Pipefy e obter IDs reais dos campos
2. Atualizar `PipefyService.buildCreateMutation()`
3. Substituir strings genéricas pelos IDs corretos

### 4. **Adicionar Segurança**

```java
// Criar filtro de autenticação
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    // Validar API key ou JWT
}

// Configurar CORS específico
@CrossOrigin(origins = "https://seu-dominio.com")
```

---

## 🧪 Testes

```bash
# Rodar testes
mvn test

# Com cobertura
mvn test jacoco:report
```

**Nota:** Testes unitários ainda não foram implementados.

---

## 📦 Deploy

### Variáveis de ambiente necessárias:

```bash
OPENAI_API_KEY=sk-proj-...
CALENDLY_API_TOKEN=...
PIPEFY_API_TOKEN=...
PIPEFY_PIPE_ID=123456789
SERVER_PORT=8080
```

### Heroku:
```bash
heroku config:set OPENAI_API_KEY="sk-proj-..."
```

### Vercel/Docker:
Configurar via dashboard ou docker-compose

---

## 📚 Documentação Adicional

- **Postman Collection**: `Webchat_API.postman_collection.json`
- **Configuração de Env**: `application-local.properties.example`
- **Scripts de Setup**: `setup-env.sh` / `setup-env.ps1`

---

## 🐛 Troubleshooting

### Erro: "Could not resolve placeholder"
**Solução:** Criar `application-local.properties` com as credenciais

### Erro: "401 Unauthorized"
**Solução:** Verificar se os tokens estão corretos e não expiraram

### Erro de CORS
**Solução:** Backend já está configurado com `@CrossOrigin(origins = "*")`

### Banco não cria tabelas
**Solução:** Verificar `spring.jpa.hibernate.ddl-auto=update` no application.properties

---

## 📄 Licença

Este projeto foi desenvolvido como parte do desafio Elite Dev IA.

---

## 👤 Autor

[Guilherme Figueiredo](https://github.com/Jguilhermefigueiredo)

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Verificar logs do servidor
2. Testar endpoints com Postman
3. Consultar este README
4. Verificar credenciais no `application-local.properties`

