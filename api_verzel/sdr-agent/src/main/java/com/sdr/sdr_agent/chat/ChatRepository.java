package main.java.com.sdr.sdr_agent.chat;

import main.java.com.sdr.sdr_agent.lead.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);
    List<ChatMessage> findByLeadOrderByTimestampAsc(Lead lead);
}
