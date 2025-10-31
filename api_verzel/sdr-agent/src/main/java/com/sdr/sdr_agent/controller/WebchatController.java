package main.java.com.sdr.sdr_agent.controller;

import main.java.com.sdr.sdr_agent.chat.AssistantOrchestrator;
import main.java.com.sdr.sdr_agent.chat.ChatMessage;
import main.java.com.sdr.sdr_agent.chat.ChatService;
import main.java.com.sdr.sdr_agent.chat.dto.*;
import main.java.com.sdr.sdr_agent.services.CalendlyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/webchat")
public class WebchatController {

    private final ChatService chatService;
    private final AssistantOrchestrator orchestrator;

    public WebchatController(ChatService chatService, AssistantOrchestrator orchestrator) {
        this.chatService = chatService;
        this.orchestrator = orchestrator;
    }

    /**
     * Inicia uma nova sessão de chat
     * POST /api/webchat/session
     */
    @PostMapping("/session")
    public ResponseEntity<SessionResponse> startSession() {
        String sessionId = UUID.randomUUID().toString();
        String welcomeMessage = "Olá! 👋 Sou o assistente virtual da nossa empresa. " +
                "Estou aqui para entender suas necessidades e, se necessário, agendar uma reunião com nossa equipe. " +
                "Como posso ajudá-lo hoje?";

        log.info("Nova sessão criada: {}", sessionId);

        // Salva mensagem de boas-vindas
        ChatMessage welcomeMsg = new ChatMessage();
        welcomeMsg.setSessionId(sessionId);
        welcomeMsg.setContent(welcomeMessage);
        welcomeMsg.setTimestamp(LocalDateTime.now());
        chatService.save(welcomeMsg);

        return ResponseEntity.ok(new SessionResponse(sessionId, LocalDateTime.now(), welcomeMessage));
    }

    /**
     * Envia uma mensagem e recebe resposta do assistente
     * POST /api/webchat/message
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        try {
            log.info("Mensagem recebida na sessão {}: {}", request.getSessionId(), request.getMessage());

            // Salva mensagem do usuário
            ChatMessage userMsg = new ChatMessage();
            userMsg.setSessionId(request.getSessionId());
            userMsg.setContent(request.getMessage());
            userMsg.setTimestamp(LocalDateTime.now());
            chatService.save(userMsg);

            // Aqui você integraria com OpenAI para processar a mensagem
            // Por enquanto, retorna uma resposta simples
            String responseText = processarMensagem(request.getMessage());

            // Salva resposta do assistente
            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setSessionId(request.getSessionId());
            assistantMsg.setContent(responseText);
            assistantMsg.setTimestamp(LocalDateTime.now());
            chatService.save(assistantMsg);

            ChatResponse response = new ChatResponse(
                    request.getSessionId(),
                    responseText,
                    "text"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem: {}", e.getMessage());
            return ResponseEntity.ok(new ChatResponse(
                    request.getSessionId(),
                    "Desculpe, ocorreu um erro ao processar sua mensagem. Por favor, tente novamente.",
                    "error"
            ));
        }
    }

    /**
     * Obtém o histórico de mensagens de uma sessão
     * GET /api/webchat/history/{sessionId}
     */
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        log.info("Buscando histórico da sessão: {}", sessionId);
        List<ChatMessage> history = chatService.getHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    /**
     * Lista horários disponíveis para agendamento
     * GET /api/webchat/slots
     */
    @GetMapping("/slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots() {
        log.info("Buscando horários disponíveis");
        List<String> slots = orchestrator.oferecerHorarios();

        Map<String, Object> response = new HashMap<>();
        response.put("slots", slots);
        response.put("count", slots.size());
        response.put("message", slots.isEmpty()
                ? "Nenhum horário disponível no momento"
                : "Horários disponíveis para os próximos dias úteis");

        return ResponseEntity.ok(response);
    }

    /**
     * Agenda uma reunião
     * POST /api/webchat/schedule
     */
    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> scheduleMeeting(@RequestBody ScheduleRequest request) {
        try {
            log.info("Agendando reunião para {} no horário {}", request.getEmail(), request.getSlot());

            Map<String, Object> args = new HashMap<>();
            args.put("slot", request.getSlot());
            args.put("email", request.getEmail());
            args.put("nome", request.getNome());
            args.put("empresa", request.getEmpresa());

            // Processa através do orchestrator
            var result = orchestrator.processarRespostaAssistente(
                    Map.of("choices", List.of(
                            Map.of("message", Map.of(
                                    "function_call", Map.of(
                                            "name", "agendarReuniao",
                                            "arguments", buildJsonString(args)
                                    )
                            ))
                    ))
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.getType().equals("payload"));
            response.put("message", result.getMessage());
            response.put("data", result.getData());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erro ao agendar reunião: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erro ao agendar reunião: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Health check do webchat
     * GET /api/webchat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "webchat");
        status.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }

    // Métodos auxiliares

    private String processarMensagem(String mensagem) {
        // Processamento básico de mensagem
        // Em produção, integrar com OpenAI
        String lower = mensagem.toLowerCase();

        if (lower.contains("oi") || lower.contains("olá") || lower.contains("ola")) {
            return "Olá! Como posso ajudá-lo hoje?";
        }

        if (lower.contains("horário") || lower.contains("reunião") || lower.contains("agendar")) {
            return "Ficaria feliz em agendar uma reunião! Posso verificar os horários disponíveis para você. " +
                    "Antes disso, poderia me informar seu nome, empresa e o que você precisa?";
        }

        if (lower.contains("obrigado") || lower.contains("valeu")) {
            return "Por nada! Estou aqui para ajudar. Há algo mais que eu possa fazer por você?";
        }

        return "Entendo. Poderia me contar um pouco mais sobre suas necessidades? " +
                "Assim posso direcionar melhor nossa conversa.";
    }

    private String buildJsonString(Map<String, Object> map) {
        // Constrói JSON simples
        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (i > 0) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
            i++;
        }
        json.append("}");
        return json.toString();
    }
}

