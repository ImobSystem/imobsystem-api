package com.system.imob.services;

import com.system.imob.dtos.requests.ImobiliariaRequestDTO;
import com.system.imob.dtos.responses.ImobiliariaResponseDTO;
import com.system.imob.models.Imobiliaria;
import com.system.imob.repositories.ImobiliariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImobiliariaService {

    @Autowired
    private ImobiliariaRepository imobiliariaRepository;

    public ImobiliariaResponseDTO cadastrarImobiliaria(ImobiliariaRequestDTO dto) {
        Imobiliaria imobiliaria = new Imobiliaria();
        imobiliaria.setNome(dto.nome());
        imobiliaria.setCnpj(dto.cnpj());
        imobiliaria.setEmail(dto.email());
        imobiliaria.setTelefone(dto.telefone());
        imobiliaria.setStatusPlano(dto.statusPlano());
        imobiliaria.setPlano(dto.plano());
        imobiliaria.setDataVencimento(dto.dataVencimento());

        Imobiliaria salva = imobiliariaRepository.save(imobiliaria);
        return toResponseDTO(salva);
    }

    public ImobiliariaResponseDTO buscarPorId(Long id) {
        Imobiliaria imobiliaria = imobiliariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imobiliária não encontrada"));
        return toResponseDTO(imobiliaria);
    }

    public List<ImobiliariaResponseDTO> listarImobiliarias() {
        return imobiliariaRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private ImobiliariaResponseDTO toResponseDTO(Imobiliaria imobiliaria) {
        return new ImobiliariaResponseDTO(imobiliaria.getId(), imobiliaria.getNome(),
                imobiliaria.getCnpj(), imobiliaria.getEmail(), imobiliaria.getTelefone(),
                imobiliaria.getStatusPlano(), imobiliaria.getPlano(),
                imobiliaria.getDataVencimento());
    }
}