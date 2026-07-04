package com.system.imob.repositories;

import com.system.imob.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByImobiliariaId(Long imobiliariaId);
    List<Cliente> findByCorretorId(Long corretorId);
}
