package main.java.com.sdr.sdr_agent.canonicals;

import java.time.LocalDateTime;


public record LeadOutput(
        Long id,
        String nome,
        String empresa,
        String cargo,
        String email,
        String necessidade,
        String orcamento,
        String prazo,
        String autoridadeDecisao,
        Boolean interesseConfirmado,

        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean active
) {}
