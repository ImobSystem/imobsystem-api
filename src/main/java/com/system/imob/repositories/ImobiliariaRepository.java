package com.system.imob.repositories;

import com.system.imob.models.Imobiliaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImobiliariaRepository extends JpaRepository<Imobiliaria, Long> {
    Optional<Imobiliaria> findByAsaasCustomerId(String asaasCustomerId);
}
