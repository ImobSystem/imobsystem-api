package com.system.imob.repositories;

import com.system.imob.models.FotoImovel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FotoImovelRepository extends JpaRepository<FotoImovel, Long> {
    List<FotoImovel> findByImovelId(Long imovelId);
}