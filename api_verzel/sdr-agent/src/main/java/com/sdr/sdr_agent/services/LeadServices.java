package main.java.com.sdr.sdr_agent.services;

import main.java.com.sdr.sdr_agent.canonicals.LeadInput;
import main.java.com.sdr.sdr_agent.canonicals.LeadOutput;
import main.java.com.sdr.sdr_agent.exceptions.ResourceNotFoundException;
import main.java.com.sdr.sdr_agent.lead.Lead;
import main.java.com.sdr.sdr_agent.repository.LeadRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data

@Service
public class LeadServices {
    @Autowired
    private LeadRepository repository;
    public Lead findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public LeadOutput createLead(LeadInput input) {
        LocalDateTime now = LocalDateTime.now();

        Lead lead = new Lead();
        lead.setActive(true);
        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);
        lead.setNome(input.getNome());
        lead.setEmail(input.getEmail());
        lead.setEmpresa(input.getEmpresa());
        lead.setCargo(input.getCargo());
        lead.setNecessidade(input.getNecessidade());
        lead.setOrcamento(input.getOrcamento());
        lead.setPrazo(input.getPrazo());
        lead.setAutoridadeDecisao(input.getAutoridadeDecisao());
        lead.getInteresseConfirmado();

                repository.save(lead);

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

    public List<LeadOutput> createLeads(List<LeadInput> inputs) {
        List<LeadOutput> outputs = new ArrayList<>();

        for (LeadInput input : inputs) {
            outputs.add(createLead(input));
        }

        return outputs;
    }

    public LeadOutput retrieveLead(Long id) {
        Lead lead = repository
                .findById(id)
                .filter(Lead::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found (id: " + id + ")"));

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

    public LeadOutput updateLead(Long id, LeadInput input) {
        Lead fetched = repository
                .findById(id)
                .filter(Lead::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found (id: " + id + ")"));

        fetched.setNome(input.getNome());
        fetched.setEmail(input.getEmail());
        fetched.setEmpresa(input.getEmpresa());
        fetched.setCargo(input.getCargo());
        fetched.setNecessidade(input.getNecessidade());
        fetched.setOrcamento(input.getOrcamento());
        fetched.setPrazo(input.getPrazo());
        fetched.setAutoridadeDecisao(input.getAutoridadeDecisao());
        fetched.setUpdatedAt(LocalDateTime.now());

        repository.save(fetched);

        return new LeadOutput(
                fetched.getId(),
                fetched.getNome(),
                fetched.getEmpresa(),
                fetched.getCargo(),
                fetched.getEmail(),
                fetched.getNecessidade(),
                fetched.getOrcamento(),
                fetched.getPrazo(),
                fetched.getAutoridadeDecisao(),
                fetched.getInteresseConfirmado(),
                fetched.getCreatedAt(),
                fetched.getUpdatedAt(),
                fetched.isActive()
        );
    }

    public LeadOutput delete(Long id) {
        Lead fetched = repository
                .findById(id)
                .filter(Lead::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found (id: " + id + ")"));

        fetched.setUpdatedAt(LocalDateTime.now());
        fetched.setActive(false);

        repository.save(fetched);

        return new LeadOutput(
                fetched.getId(),
                fetched.getNome(),
                fetched.getEmpresa(),
                fetched.getCargo(),
                fetched.getEmail(),
                fetched.getNecessidade(),
                fetched.getOrcamento(),
                fetched.getPrazo(),
                fetched.getAutoridadeDecisao(),
                fetched.getInteresseConfirmado(),
                fetched.getCreatedAt(),
                fetched.getUpdatedAt(),
                fetched.isActive()
        );
    }

    public List<LeadOutput> findAll() {
        List<LeadOutput> list = new ArrayList<>();

        repository.findAll().forEach(lead -> {
            if (lead.isActive()) {
                LeadOutput output = new LeadOutput(
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
                list.add(output);
            }
        });

        return list;
    }
    public Lead findEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead não encontrado (id: " + id + ")"));
    }
    private LeadOutput toOutput(Lead lead) {
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
    public Lead createOrUpdateFromInput(LeadInput input) {
        // Verifica se já existe lead com mesmo e-mail
        Optional<Lead> existing = repository.findByEmail(input.getEmail());

        Lead lead;
        if (existing.isPresent()) {
            lead = existing.get();
            // Atualiza dados principais
            lead.setNome(input.getNome());
            lead.setEmpresa(input.getEmpresa());
            lead.setCargo(input.getCargo());
            lead.setNecessidade(input.getNecessidade());
            lead.setOrcamento(input.getOrcamento());
            lead.setPrazo(input.getPrazo());
            lead.setAutoridadeDecisao(input.getAutoridadeDecisao());
        } else {
            // Cria novo lead
            lead = new Lead();
            lead.setNome(input.getNome());
            lead.setEmpresa(input.getEmpresa());
            lead.setCargo(input.getCargo());
            lead.setEmail(input.getEmail());
            lead.setNecessidade(input.getNecessidade());
            lead.setOrcamento(input.getOrcamento());
            lead.setPrazo(input.getPrazo());
            lead.setAutoridadeDecisao(input.getAutoridadeDecisao());
            lead.setInteresseConfirmado(false);
        }

        return repository.save(lead);
    }
    public Lead save(Lead lead) {
        return repository.save(lead);
    }
    public LeadOutput confirmInterest(Long id) {
        Lead lead = repository.findById(id)
                .filter(Lead::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found (id: " + id + ")"));

        lead.setInteresseConfirmado(true);
        lead.setUpdatedAt(LocalDateTime.now());

        Lead saved = repository.save(lead);
        return toOutput(saved);
    }

}