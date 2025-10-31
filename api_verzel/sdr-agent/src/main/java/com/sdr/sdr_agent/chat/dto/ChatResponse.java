package main.java.com.sdr.sdr_agent.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String sessionId;
    private String message;
    private String type; // "text", "error", "payload"
    private Map<String, Object> data;
    private LocalDateTime timestamp;

    public ChatResponse(String sessionId, String message, String type) {
        this.sessionId = sessionId;
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
}
