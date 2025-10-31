package main.java.com.sdr.sdr_agent.repository;

import main.java.com.sdr.sdr_agent.lead.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByEmail(String email);
    List<Lead> findByActiveTrue();

}
