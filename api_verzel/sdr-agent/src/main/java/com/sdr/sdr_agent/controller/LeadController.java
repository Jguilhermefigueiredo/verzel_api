
package main.java.com.sdr.sdr_agent.controller;

import main.java.com.sdr.sdr_agent.canonicals.LeadInput;
import main.java.com.sdr.sdr_agent.canonicals.LeadOutput;
import main.java.com.sdr.sdr_agent.lead.Lead;
import main.java.com.sdr.sdr_agent.services.CalendlyService;
import main.java.com.sdr.sdr_agent.services.LeadServices;
import main.java.com.sdr.sdr_agent.services.OpenAIService;
import main.java.com.sdr.sdr_agent.services.PipefyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/leads")
public class LeadController {

    private static final Logger logger = LoggerFactory.getLogger(LeadController.class);

    private final LeadServices service;
    private final PipefyService pipefyService;
    private final CalendlyService calendlyService;
    private final OpenAIService openAIService;

    @Autowired
    public LeadController(LeadServices service, PipefyService pipefyService, CalendlyService calendlyService,OpenAIService openAIService) {
        this.service = service;
        this.pipefyService = pipefyService;
        this.calendlyService = calendlyService;
        this.openAIService = openAIService;
    }

    // ---------- CRIAR LEAD ----------
    @PostMapping
    public ResponseEntity<LeadOutput> createLead(@RequestBody @Valid LeadInput input) {
        LeadOutput leadOutput = service.createLead(input);
        pipefyService.upsertLeadAsync(convertToLeadModel(leadOutput));

        // Gerar texto via OpenAI (assíncrono)
        openAIService.generateTextAsync("Crie um resumo para este lead: " + leadOutput.nome())
                .subscribe(response -> {
                    if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                        String resumo = response.getChoices().get(0).getMessage().getContent();
                        logger.info("Resumo OpenAI gerado para lead {}: {}", leadOutput.nome(), resumo);
                    } else {
                        logger.warn("Nenhuma resposta OpenAI para lead {}", leadOutput.nome());
                    }
                }, error -> logger.error("Erro ao gerar resumo OpenAI para lead {}: {}", leadOutput.nome(), error.getMessage()));

        return ResponseEntity.ok(leadOutput);
    }

    @PostMapping("/multi")
    public ResponseEntity<List<LeadOutput>> createMultipleLeads(@RequestBody @Valid List<LeadInput> inputs) {
        List<LeadOutput> outputs = service.createLeads(inputs);
        for (LeadOutput output : outputs) {
            pipefyService.upsertLeadAsync(convertToLeadModel(output));
        }
        return ResponseEntity.ok(outputs);
    }

    // ---------- LISTAR / RECUPERAR ----------
    @GetMapping
    public ResponseEntity<List<LeadOutput>> findAll() {

        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeadOutput> retrieveLead(@PathVariable Long id) {
        LeadOutput lead = service.retrieveLead(id);
        if (lead == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(lead);
    }

    // ---------- ATUALIZAR ----------
    @PutMapping("/{id}")
    public ResponseEntity<LeadOutput> updateLead(@PathVariable Long id, @RequestBody @Valid LeadInput input) {
        LeadOutput leadOutput = service.updateLead(id, input);
        pipefyService.upsertLeadAsync(convertToLeadModel(leadOutput));
        return ResponseEntity.ok(leadOutput);
    }

    // ---------- DELETAR ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<LeadOutput> delete(@PathVariable Long id) {

        return ResponseEntity.ok(service.delete(id));
    }

    // ---------- CONFIRMAR INTERESSE ----------
    @PutMapping("/{id}/confirm")
    public ResponseEntity<LeadOutput> confirmInterest(@PathVariable Long id) {
        LeadOutput updatedLead = service.confirmInterest(id);

        // Atualiza/insere no Pipefy
        pipefyService.upsertLeadAsync(convertToLeadModel(updatedLead));

        // Agenda reunião se autoridadeDecisao = "Sim"
        Lead lead = convertToLeadModel(updatedLead);
        if ("Sim".equalsIgnoreCase(lead.getAutoridadeDecisao())) {
            calendlyService.scheduleMeetingAsync(lead);
        }

        // ----------- Gera e salva follow-up via OpenAI (assíncrono) -----------
        openAIService.generateTextAsync("Crie um follow-up para este lead: " + updatedLead.nome())
                .subscribe(
                        response -> {
                            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                                String followUp = response.getChoices().get(0).getMessage().getContent();

                                // Salva o follow-up no Lead
                                lead.setFollowUp(followUp);
                                service.save(lead);

                                logger.info("✅ Follow-up OpenAI gerado e salvo para lead {}: {}", updatedLead.nome(), followUp);
                            } else {
                                logger.warn("⚠️ Nenhuma resposta OpenAI para follow-up do lead {}", updatedLead.nome());
                            }
                        },
                        error -> logger.error("❌ Erro ao gerar follow-up OpenAI para lead {}: {}", updatedLead.nome(), error.getMessage())
                );

        return ResponseEntity.ok(updatedLead);
    }


    // ---------- MÉTODOS DE CONVERSÃO ----------
    private Lead convertToLeadModel(LeadOutput output) {
        Lead lead = new Lead();
        lead.setNome(output.nome());
        lead.setEmpresa(output.empresa());
        lead.setCargo(output.cargo());
        lead.setEmail(output.email());
        lead.setNecessidade(output.necessidade());
        lead.setOrcamento(output.orcamento());
        lead.setPrazo(output.prazo());
        lead.setAutoridadeDecisao(output.autoridadeDecisao()
        /*lead.getInteresseConfirmado(output.interesseConfirmado()*/);
        return lead;
    }

    private Lead convertToLeadModel(LeadInput input) {
        Lead lead = new Lead();
        lead.setNome(input.getNome());
        lead.setEmpresa(input.getEmpresa());
        lead.setCargo(input.getCargo());
        lead.setEmail(input.getEmail());
        lead.setNecessidade(input.getNecessidade());
        lead.setOrcamento(input.getOrcamento());
        lead.setPrazo(input.getPrazo());
        lead.setAutoridadeDecisao(input.getAutoridadeDecisao());
        lead.getInteresseConfirmado();
        return lead;
    }

    private LeadInput convertToLeadInput(LeadOutput output) {
        LeadInput input = new LeadInput();
        input.setNome(output.nome());
        input.setEmpresa(output.empresa());
        input.setCargo(output.cargo());
        input.setEmail(output.email());
        input.setNecessidade(output.necessidade());
        input.setOrcamento(output.orcamento());
        input.setPrazo(output.prazo());
        input.setAutoridadeDecisao(output.autoridadeDecisao());
        return input;
    }
    private LeadOutput toLeadOutput(Lead lead) {
        return new LeadOutput(
                lead.getId(),
                lead.getNome(),
                lead.getEmpresa(),
                lead.getCargo(),
                lead.getEmail(),
                lead.getNecessidade(),
                lead.getOrcamento(),
                lead.getPrazo(),
                lead.getAutoridadeDecisao(),
                lead.getInteresseConfirmado(),
                lead.getCreatedAt(),
                lead.getUpdatedAt(),
                lead.isActive()
        );
    }

}

