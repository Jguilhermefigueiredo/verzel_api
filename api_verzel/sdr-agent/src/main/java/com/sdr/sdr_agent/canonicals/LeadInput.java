package main.java.com.sdr.sdr_agent.canonicals;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class LeadInput {
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 255, message = "Full name must be between 3 and 255 characters")
    private String nome;

    @NotBlank(message = "Company is required")
    @Size(min = 3, max = 255, message = "Company must be between 3 and 255 characters")
    private String empresa;

    @NotBlank(message = "Job Title is required")
    @Size(min = 3, max = 255, message = "Job Title must be between 3 and 255 characters")
    private String cargo;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @NotBlank(message = "Requirement is required")
    @Size(min = 3, max = 1500, message = "Requirement must be between 3 and 1500 characters")
    private String necessidade;

    @NotBlank(message = "Budget is required")
    @Size(min = 3, max = 255, message = "Budget must be between 3 and 255 characters")
    private String orcamento;

    @NotBlank(message = "Deadline is required")
    @Size(min = 3, max = 255, message = "Deadline must be between 3 and 255 characters")
    private String prazo;

    @NotBlank(message = "Decision-making authority is required")
    @Size(min = 3, max = 255, message = "Decision-making authority must be between 3 and 255 characters")
    private String autoridadeDecisao;

}


    
