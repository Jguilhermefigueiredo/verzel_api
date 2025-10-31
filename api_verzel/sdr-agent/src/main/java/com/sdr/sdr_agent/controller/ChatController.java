package main.java.com.sdr.sdr_agent.controller;

import main.java.com.sdr.sdr_agent.chat.ChatMessage;
import main.java.com.sdr.sdr_agent.chat.ChatService;
import main.java.com.sdr.sdr_agent.lead.Lead;
import main.java.com.sdr.sdr_agent.services.LeadServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final LeadServices leadService;

    public ChatController(ChatService chatService, LeadServices leadService) {
        this.chatService = chatService;
        this.leadService = leadService;
    }

    // Salvar mensagem simples
    @PostMapping("/message")
    public ResponseEntity<ChatMessage> saveMessage(@RequestBody ChatMessage msg) {
        ChatMessage saved = chatService.save(msg);
        return ResponseEntity.ok(saved);
    }

    // Obter histórico por sessão anônima
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatService.getHistory(sessionId));
    }

    // Obter histórico vinculado a Lead
    @GetMapping("/lead/{leadId}")
    public ResponseEntity<List<ChatMessage>> getChatByLead(@PathVariable Long leadId) {
        Lead lead = leadService.findEntityById(leadId); // novo método no LeadService
        if (lead == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(chatService.getMessagesByLead(lead));
    }
}