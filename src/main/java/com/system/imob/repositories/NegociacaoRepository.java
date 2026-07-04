package com.system.imob.repositories;

import com.system.imob.models.Negociacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NegociacaoRepository extends JpaRepository<Negociacao, Long> {
    // Negociacao não tem imobiliaria direta — navega via imovel.imobiliaria.id
    List<Negociacao> findByImovelImobiliariaId(Long imobiliariaId);
    List<Negociacao> findByCorretorId(Long corretorId);
}
