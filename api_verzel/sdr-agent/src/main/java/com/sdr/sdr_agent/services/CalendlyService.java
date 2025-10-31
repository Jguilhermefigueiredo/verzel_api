package main.java.com.sdr.sdr_agent.services;

import main.java.com.sdr.sdr_agent.lead.Lead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CalendlyService {

    public static record ScheduleResult(String meetingLink, OffsetDateTime meetingDatetime) {}

    private final WebClient webClient;

    @Value("${calendly.api.token}")
    private String calendlyApiToken;

    @Value("${calendly.api.url:https://api.calendly.com/scheduled_events}")
    private String calendlyApiUrl;

    public CalendlyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // Chamada assíncrona
    public CompletableFuture<Void> scheduleMeetingAsync(Lead lead) {
        String defaultSlot = OffsetDateTime.now().plusDays(1).toString(); // ou qualquer regra
        return CompletableFuture.runAsync(() -> scheduleMeeting(lead, defaultSlot));
    }


    public ScheduleResult scheduleMeeting(Lead lead, String slot) {
        try {
            Map<String, Object> body = new HashMap<>();
            Map<String, String> invitee = new HashMap<>();

            invitee.put("name", lead.getNome());
            invitee.put("email", lead.getEmail());
            body.put("invitee", invitee);
            body.put("event_type", "30min-meeting");
            body.put("scheduled_slot", slot);

            log.info("Agendando reunião no Calendly para lead: {} no horário: {}", lead.getNome(), slot);

            String response = webClient.post()
                    .uri(calendlyApiUrl)
                    .header("Authorization", "Bearer " + calendlyApiToken)
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnError(err -> log.error("Erro ao agendar reunião no Calendly", err))
                    .block();

            // Aqui poderíamos extrair meetingLink / meetingDatetime da resposta real
            String meetingLink = "https://calendly.com/fake-link"; // placeholder
            OffsetDateTime meetingDatetime = OffsetDateTime.now().plusDays(1);

            log.info("Reunião agendada: link={} data={}", meetingLink, meetingDatetime);

            return new ScheduleResult(meetingLink, meetingDatetime);

        } catch (Exception e) {
            log.error("Erro ao agendar reunião para lead {}: {}", lead.getNome(), e.getMessage());
            return null;
        }
    }


    public List<String> getAvailableSlotsNext7Days() {
        // Gera horários disponíveis para os próximos 7 dias
        // Em produção, substituir por chamada real à API do Calendly
        List<String> slots = new ArrayList<>();
        OffsetDateTime now = OffsetDateTime.now();

        // Gera horários de segunda a sexta, das 9h às 17h
        for (int dia = 1; dia <= 7; dia++) {
            OffsetDateTime data = now.plusDays(dia);

            // Pula fins de semana (sábado = 6, domingo = 7)
            int diaSemana = data.getDayOfWeek().getValue();
            if (diaSemana == 6 || diaSemana == 7) {
                continue;
            }

            // Horários: 9h, 10h, 11h, 14h, 15h, 16h
            int[] horas = {9, 10, 11, 14, 15, 16};
            for (int hora : horas) {
                OffsetDateTime horario = data
                        .withHour(hora)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

                // Adiciona apenas horários futuros
                if (horario.isAfter(now)) {
                    slots.add(horario.toString());
                }
            }
        }

        log.info("Gerados {} horários disponíveis para os próximos 7 dias", slots.size());
        return slots;
    }
}