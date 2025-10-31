package main.java.com.sdr.sdr_agent.controller;

import main.java.com.sdr.sdr_agent.openai.ChatCompletionResponse;
import main.java.com.sdr.sdr_agent.services.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/openai")
public class OpenAIController {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIController.class);

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    // ---------- GERAR TEXTO COM OPENAI (ASSÍNCRONO) ----------
    @PostMapping("/generate")
    public Mono<ResponseEntity<String>> generateTextAsync(@RequestBody String prompt) {
        return openAIService.generateTextAsync(prompt)
                .map(response -> {
                    if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                        logger.warn("Nenhuma resposta gerada pela OpenAI para o prompt: {}", prompt);
                        return ResponseEntity.ok("Nenhuma resposta gerada");
                    }
                    return ResponseEntity.ok(response.getChoices().get(0).getMessage().getContent());
                })
                .onErrorResume(e -> {
                    logger.error("Erro ao chamar OpenAI: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(500)
                            .body("Erro ao chamar OpenAI: " + e.getMessage()));
                });
    }
}
