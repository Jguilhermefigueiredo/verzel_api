package main.java.com.sdr.sdr_agent.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("pipefyClient")
    public WebClient pipefyClient(WebClient.Builder builder,
                                  @Value("${pipefy.api.url}") String baseUrl,
                                  @Value("${pipefy.api.token}") String token) {
        return builder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("openaiClient")
    public WebClient openaiClient(WebClient.Builder builder,
                                  @Value("${openai.api.url}") String baseUrl,
                                  @Value("${openai.api.key}") String key) {
        return builder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + key)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("calendlyClient")
    public WebClient calendlyClient(WebClient.Builder builder,
                                    @Value("${calendly.api.url}") String baseUrl,
                                    @Value("${calendly.api.token}") String token) {
        return builder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
