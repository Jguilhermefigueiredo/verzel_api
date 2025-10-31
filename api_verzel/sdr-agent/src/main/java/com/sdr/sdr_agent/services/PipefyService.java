package main.java.com.sdr.sdr_agent.services;

import main.java.com.sdr.sdr_agent.lead.Lead;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PipefyService {

    private final WebClient webClient;
    private final Long pipeId;

    public PipefyService(@Qualifier("pipefyClient") WebClient webClient,
                        @Value("${pipefy.pipe.id}") Long pipeId) {
        this.webClient = webClient;
        this.pipeId = pipeId;
    }

    // Método assíncrono
    public CompletableFuture<String> upsertLeadAsync(Lead lead) {
        return CompletableFuture.supplyAsync(() -> upsertLead(lead));
    }


    public boolean updateMeetingInfo(String cardId, String meetingLink, String meetingDatetime, boolean interesseConfirmado) {
        // Implement update mutation for Pipefy card fields. Use real field ids/keys.
        String mutation = String.format("mutation { updateCard(input: { id: \"%s\", fields_attributes: [{field_id:\"meeting_link\", field_value:\"%s\"}, {field_id:\"meeting_datetime\", field_value:\"%s\"}, {field_id:\"interesse_confirmado\", field_value:%s}] }) { card { id } } }", cardId, meetingLink, meetingDatetime, interesseConfirmado ? "true" : "false");
        String resp = webClient.post()
                .bodyValue(Map.of("query", mutation))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return resp != null && resp.contains("\"id\"");
    }

    // Método interno que verifica se existe card e cria ou atualiza
    private String upsertLead(Lead lead) {
        // 1️⃣ Consulta card pelo email (campo único)
        String queryCheck = "{ \"query\": \"{ cards(pipe_id: " + pipeId + ", query: \\\"" + lead.getEmail() + "\\\") { edges { node { id } } } }\" }";

        String existingCardResponse = webClient.post()
                .bodyValue(queryCheck)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        boolean exists = existingCardResponse != null && existingCardResponse.contains("id");

        // 2️⃣ Cria ou atualiza card
        String mutation = exists ? buildUpdateMutation(lead, extractCardId(existingCardResponse))
                : buildCreateMutation(lead);

        return webClient.post()
                .bodyValue(Map.of("query", mutation))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // Extrai o ID do card retornado pela query
    private String extractCardId(String response) {
        // Forma simples de extrair "id" do JSON (recomendo usar biblioteca JSON em produção)
        int idIndex = response.indexOf("\"id\":\"");
        if (idIndex == -1) return null;
        int start = idIndex + 6;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

    private String buildCreateMutation(Lead lead) {
        return "mutation { createCard(input: { pipe_id: " + pipeId +
                ", title: \"" + lead.getNome() + " - " + lead.getEmpresa() + "\"," +
                " fields_attributes: [" +
                "{field_id: \\\"nome\\\", field_value: \\\"" + lead.getNome() + "\\\"}," +
                "{field_id: \\\"empresa\\\", field_value: \\\"" + lead.getEmpresa() + "\\\"}," +
                "{field_id: \\\"cargo\\\", field_value: \\\"" + lead.getCargo() + "\\\"}," +
                "{field_id: \\\"necessidade\\\", field_value: \\\"" + lead.getNecessidade() + "\\\"}," +
                "{field_id: \\\"orcamento\\\", field_value: \\\"" + lead.getOrcamento() + "\\\"}," +
                "{field_id: \\\"prazo\\\", field_value: \\\"" + lead.getPrazo() + "\\\"}," +
                "{field_id: \\\"autoridadeDecisao\\\", field_value: \\\"" + (lead.getAutoridadeDecisao()) + "\\\"}" +
                "] }) { card { id title } } }";
    }

    private String buildUpdateMutation(Lead lead, String cardId) {
        return "mutation { updateCard(input: { card_id: " + cardId +
                ", fields_attributes: [" +
                "{field_id: \\\"nome\\\", field_value: \\\"" + lead.getNome() + "\\\"}," +
                "{field_id: \\\"empresa\\\", field_value: \\\"" + lead.getEmpresa() + "\\\"}," +
                "{field_id: \\\"cargo\\\", field_value: \\\"" + lead.getCargo() + "\\\"}," +
                "{field_id: \\\"necessidade\\\", field_value: \\\"" + lead.getNecessidade() + "\\\"}," +
                "{field_id: \\\"orcamento\\\", field_value: \\\"" + lead.getOrcamento() + "\\\"}," +
                "{field_id: \\\"prazo\\\", field_value: \\\"" + lead.getPrazo() + "\\\"}," +
                "{field_id: \\\"autoridadeDecisao\\\", field_value: \\\"" + (lead.getAutoridadeDecisao()) + "\\\"}" +
                "] }) { card { id title } } }";
    }
}