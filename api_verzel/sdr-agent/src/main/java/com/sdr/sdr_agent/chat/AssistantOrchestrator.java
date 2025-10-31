
package main.java.com.sdr.sdr_agent.chat;

import main.java.com.sdr.sdr_agent.canonicals.LeadInput;
import main.java.com.sdr.sdr_agent.lead.Lead;
import main.java.com.sdr.sdr_agent.services.CalendlyService;
import main.java.com.sdr.sdr_agent.services.LeadServices;
import main.java.com.sdr.sdr_agent.services.OpenAIService;
import main.java.com.sdr.sdr_agent.services.PipefyService;
import main.java.com.sdr.sdr_agent.utils.JsonUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AssistantOrchestrator {

    private final LeadServices leadServices;
    private final PipefyService pipefyService;
    private final CalendlyService calendlyService;
    private final OpenAIService openAIService;

    public AssistantOrchestrator(LeadServices leadServices, PipefyService pipefyService,
                                 CalendlyService calendlyService, OpenAIService openAIService) {
        this.leadServices = leadServices;
        this.pipefyService = pipefyService;
        this.calendlyService = calendlyService;
        this.openAIService = openAIService;
    }
    public main.java.com.sdr.sdr_agent.chat.OrchestratorResult processarRespostaAssistente(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                return main.java.com.sdr.sdr_agent.chat.OrchestratorResult.error("Nenhuma escolha retornada pelo modelo.");
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            if (message == null) {
                return main.java.com.sdr.sdr_agent.chat.OrchestratorResult.error("Mensagem do modelo está vazia.");
            }

            // Verifica se há uma função a ser chamada
            Map<String, Object> functionCall = (Map<String, Object>) message.get("function_call");
            if (functionCall != null) {
                String functionName = (String) functionCall.get("name");
                String argumentsJson = (String) functionCall.get("arguments");

                Map<String, Object> args = JsonUtils.parseJsonToMap(argumentsJson);

                switch (functionName) {
                    case "registrarLead":
                        return registrarLeadFromMap(args);
                    case "oferecerHorarios":
                        return oferecerHorariosFromMap();
                    case "agendarReuniao":
                        return agendarReuniaoFromMap(args);
                    default:
                        return main.java.com.sdr.sdr_agent.chat.OrchestratorResult.error("Função desconhecida: " + functionName);
                }
            }

            // Caso não haja função, apenas retorna o conteúdo textual
            String content = (String) message.get("content");
            return OrchestratorResult.text(content != null ? content : "Sem conteúdo retornado.");

        } catch (Exception e) {
            return OrchestratorResult.error("Erro ao processar resposta: " + e.getMessage());
        }
    }

    public Lead registrarLead(LeadInput input) {
        Lead lead = leadServices.createOrUpdateFromInput(input);
        // upsert in pipefy asynchronously (existing method upsertLeadAsync)
        pipefyService.upsertLeadAsync(lead);
        return lead;
    }

    public List<String> oferecerHorarios() {
        return calendlyService.getAvailableSlotsNext7Days();
    }

    public CalendlyService.ScheduleResult agendarReuniao(String slot, Lead lead) {
        CalendlyService.ScheduleResult res = calendlyService.scheduleMeeting(lead, slot);
        if (res != null) {
            // update pipefy with meeting info if cardId exists on lead
            if (lead.getId() != null) {
                // Placeholder: in production, persist and update by cardId
                pipefyService.updateMeetingInfo(String.valueOf(lead.getId()), res.meetingLink().toString(), res.meetingDatetime().toString(), true);
            }
            lead.setMeetingLink(res.meetingLink());
            lead.setMeetingDatetime(res.meetingDatetime());
            lead.setInteresseConfirmado(true);
            leadServices.save(lead);
        }
        return res;
    }

    // Métodos auxiliares para processar chamadas de função

    private OrchestratorResult registrarLeadFromMap(Map<String, Object> args) {
        try {
            LeadInput input = new LeadInput();
            input.setNome((String) args.get("nome"));
            input.setEmail((String) args.get("email"));
            input.setEmpresa((String) args.get("empresa"));
            input.setCargo((String) args.getOrDefault("cargo", "Não informado"));
            input.setNecessidade((String) args.getOrDefault("necessidade", ""));
            input.setOrcamento((String) args.getOrDefault("orcamento", "Não definido"));
            input.setPrazo((String) args.getOrDefault("prazo", "Não definido"));
            input.setAutoridadeDecisao((String) args.getOrDefault("autoridadeDecisao", "Não informado"));

            Lead lead = registrarLead(input);

            Map<String, Object> data = Map.of(
                    "leadId", lead.getId(),
                    "nome", lead.getNome(),
                    "email", lead.getEmail()
            );

            return OrchestratorResult.payload("Lead registrado com sucesso.", data);
        } catch (Exception e) {
            return OrchestratorResult.error("Erro ao registrar lead: " + e.getMessage());
        }
    }

    private OrchestratorResult oferecerHorariosFromMap() {
        try {
            List<String> horarios = oferecerHorarios();

            if (horarios.isEmpty()) {
                return OrchestratorResult.text("No momento não há horários disponíveis. Por favor, tente novamente mais tarde.");
            }

            Map<String, Object> data = Map.of("horarios", horarios);
            return OrchestratorResult.payload("Horários disponíveis carregados.", data);
        } catch (Exception e) {
            return OrchestratorResult.error("Erro ao buscar horários: " + e.getMessage());
        }
    }

    private OrchestratorResult agendarReuniaoFromMap(Map<String, Object> args) {
        try {
            String slot = (String) args.get("slot");
            String email = (String) args.get("email");

            if (slot == null || email == null) {
                return OrchestratorResult.error("Parâmetros insuficientes: slot e email são obrigatórios.");
            }

            // Busca lead pelo email
            Lead lead = leadServices.createOrUpdateFromInput(buildLeadInputFromArgs(args));

            CalendlyService.ScheduleResult result = agendarReuniao(slot, lead);

            if (result != null) {
                Map<String, Object> data = Map.of(
                        "meetingLink", result.meetingLink(),
                        "meetingDatetime", result.meetingDatetime().toString(),
                        "leadId", lead.getId()
                );
                return OrchestratorResult.payload("Reunião agendada com sucesso!", data);
            } else {
                return OrchestratorResult.error("Não foi possível agendar a reunião.");
            }
        } catch (Exception e) {
            return OrchestratorResult.error("Erro ao agendar reunião: " + e.getMessage());
        }
    }

    private LeadInput buildLeadInputFromArgs(Map<String, Object> args) {
        LeadInput input = new LeadInput();
        input.setNome((String) args.getOrDefault("nome", "Cliente"));
        input.setEmail((String) args.get("email"));
        input.setEmpresa((String) args.getOrDefault("empresa", "Não informado"));
        input.setCargo((String) args.getOrDefault("cargo", "Não informado"));
        input.setNecessidade((String) args.getOrDefault("necessidade", ""));
        input.setOrcamento((String) args.getOrDefault("orcamento", "Não definido"));
        input.setPrazo((String) args.getOrDefault("prazo", "Não definido"));
        input.setAutoridadeDecisao((String) args.getOrDefault("autoridadeDecisao", "Não informado"));
        return input;
    }
}
