package main.java.com.sdr.sdr_agent.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String sessionId;
    private LocalDateTime createdAt;
    private String welcomeMessage;
}
