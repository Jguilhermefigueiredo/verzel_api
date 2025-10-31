package main.java.com.sdr.sdr_agent.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;

/**
 * Utilitário simples para converter JSON em Map.
 * Usa Jackson, que já é dependência padrão do Spring Boot.
 */
public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            System.err.println("Erro ao converter JSON para Map: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
