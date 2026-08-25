package com.system.imob.repositories;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import com.system.imob.models.Imovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {
    List<Imovel> findByStatusImovelAndFinalidade(StatusImovel status, Finalidade finalidade);
    List<Imovel> findByImobiliariaId(Long imobiliariaId);
    List<Imovel> findByCorretorId(Long corretorId);
    Long countByCorretorId(Long corretorId);
}
