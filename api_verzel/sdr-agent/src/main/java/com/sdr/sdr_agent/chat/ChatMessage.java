package main.java.com.sdr.sdr_agent.chat;

import main.java.com.sdr.sdr_agent.lead.Lead;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private String content;
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "lead_id")
    private Lead lead;
}

   /* public ChatMessage() {
    }

    public ChatMessage(Long id, String sessionId, String content, Lead lead) {
        this.id = id;
        this.sessionId = sessionId;
        this.content = content;
        this.lead = lead;
    }

    // Getters e setters
    public Long getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Lead getLead() { return lead; }

}*/
