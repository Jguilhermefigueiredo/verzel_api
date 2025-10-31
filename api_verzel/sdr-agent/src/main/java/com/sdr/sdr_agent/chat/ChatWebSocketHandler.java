package main.java.com.sdr.sdr_agent.chat;


import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.Map;

public class ChatWebSocketHandler extends TextWebSocketHandler{
    private final ChatService chatService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(main.java.com.sdr.sdr_agent.chat.ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> data = mapper.readValue(message.getPayload(), Map.class);
        String sessionId = (String) data.get("sessionId");
        String content = (String) data.get("content");

        chatService.saveMessage(sessionId, content);

        session.sendMessage(new TextMessage(mapper.writeValueAsString(Map.of(
                "sessionId", sessionId,
                "content", content,
                "timestamp", System.currentTimeMillis()
        ))));
    }
}
