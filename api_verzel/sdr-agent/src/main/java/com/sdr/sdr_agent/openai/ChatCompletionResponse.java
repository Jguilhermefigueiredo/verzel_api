/*package main.java.com.sdr.sdr_agent.openai;

import java.util.List;

public class ChatCompletionResponse {
    private List<Choice> choices;

    // getters e setters

    public static class Choice {
        private ChatMessage message;

        // getters e setters
    }

    public static class ChatMessage {
        private String role;
        private String content;

        // getters e setters
    }
}*/

package main.java.com.sdr.sdr_agent.openai;

import java.util.List;

public class ChatCompletionResponse {
    private List<Choice> choices;

    // getters e setters

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    public static class Choice {
        private ChatMessage message;

        public ChatMessage getMessage() { return message; }
        public void setMessage(ChatMessage message) { this.message = message; }
    }

    public static class ChatMessage {
        private String role;
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}


