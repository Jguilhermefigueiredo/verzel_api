package main.java.com.sdr.sdr_agent.chat;

import java.util.Map;

/**
 * Representa o resultado de uma operação orquestrada.
 * Pode ser um texto, um erro ou um payload com dados.
 */
public class OrchestratorResult {

    private String type; // "text", "error", "payload"
    private String message;
    private Map<String, Object> data;

    public OrchestratorResult(String type, String message, Map<String, Object> data) {
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public static OrchestratorResult text(String message) {
        return new OrchestratorResult("text", message, null);
    }

    public static OrchestratorResult error(String message) {
        return new OrchestratorResult("error", message, null);
    }

    public static OrchestratorResult payload(String message, Map<String, Object> data) {
        return new OrchestratorResult("payload", message, data);
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "OrchestratorResult{" +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
