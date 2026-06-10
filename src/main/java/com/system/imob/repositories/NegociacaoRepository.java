package com.system.imob.repositories;

import com.system.imob.models.Negociacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegociacaoRepository extends JpaRepository<Negociacao, Long> {
}
