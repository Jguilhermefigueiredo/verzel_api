package main.java.com.sdr.sdr_agent.chat;

import main.java.com.sdr.sdr_agent.lead.Lead;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    private final ChatRepository repository;

    public ChatService(ChatRepository repository) {
        this.repository = repository;
    }
    public ChatMessage save(ChatMessage msg) {
        return repository.save(msg);
    }

    public void saveMessage(String sessionId, String content) {
        repository.save(new ChatMessage(null, sessionId, content, LocalDateTime.now(), null));
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return repository.findBySessionIdOrderByTimestampAsc(sessionId);
    }
    public List<ChatMessage> getMessagesByLead(Lead lead) {
        return repository.findByLeadOrderByTimestampAsc(lead);
    }
}
