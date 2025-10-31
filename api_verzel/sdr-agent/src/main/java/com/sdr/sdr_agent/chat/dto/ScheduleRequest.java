package main.java.com.sdr.sdr_agent.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRequest {
    private String sessionId;
    private String slot;
    private String nome;
    private String email;
    private String empresa;
}

