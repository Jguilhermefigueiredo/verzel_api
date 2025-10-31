package main.java.com.sdr.sdr_agent.lead;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "leads")
@Data

public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String empresa;
    @Column(unique = true, nullable = false)
    private String email;
    private String cargo;
    private String necessidade;
    private String orcamento;
    private String prazo;
    private String autoridadeDecisao;
    @Column(nullable = false)
    private Boolean interesseConfirmado = false;
    private String meetingLink;
    private OffsetDateTime meetingDatetime;
    @Column(columnDefinition = "TEXT")
    private String followUp;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(nullable = false)
    private boolean active = true;
}

