package com.system.imob.repositories;

import com.system.imob.models.Corretor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CorretorRepository extends JpaRepository<Corretor, Long> {
    Optional<Corretor> findByEmail(String email);

    List<Corretor> findByImobiliariaId(Long imobiliariaId);
}
