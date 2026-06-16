package com.system.imob.services;

import com.system.imob.dtos.requests.CorretorRequestDTO;
import com.system.imob.dtos.responses.CorretorResponseDTO;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imobiliaria;
import com.system.imob.repositories.CorretorRepository;
import com.system.imob.repositories.ImobiliariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CorretorService {
    @Autowired
    private CorretorRepository corretorRepository;
    @Autowired
    private ImobiliariaRepository imobiliariaRepository;

    public CorretorResponseDTO cadastrarCorretor(CorretorRequestDTO dto){
        Corretor corretor = new Corretor();
        corretor.setNome(dto.nome());
        corretor.setEmail(dto.email());
        corretor.setSenha(dto.senha()); // TODO: encriptar quando o JWT entrar
        corretor.setCreci(dto.creci());
        corretor.setPerfil(dto.perfil());
        Imobiliaria imobiliaria = imobiliariaRepository.findById(dto.imobiliariaId())
                .orElseThrow(() -> new RuntimeException("Imobiliaria não encontrada"));
        corretor.setImobiliaria(imobiliaria);

        Corretor corretorSalvo = corretorRepository.save(corretor);

        return toResponseDTO(corretorSalvo);
    }

    public CorretorResponseDTO buscarCorretorPorId(Long id){
        Corretor corretor = corretorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));
        return toResponseDTO(corretor);
    }

    public List<CorretorResponseDTO> listarCorretores(){
        return corretorRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CorretorResponseDTO atualizarCorretorPorId(Long id, CorretorRequestDTO dto){
        Corretor corretor = corretorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));
        corretor.setNome(dto.nome());
        corretor.setEmail(dto.email());
        corretor.setSenha(dto.senha()); // TODO: encriptar quando o JWT entrar
        corretor.setCreci(dto.creci());
        corretor.setPerfil(dto.perfil());
        Imobiliaria imobiliaria = imobiliariaRepository.findById(dto.imobiliariaId())
                .orElseThrow(() -> new RuntimeException("Imobiliaria não encontrada"));
        corretor.setImobiliaria(imobiliaria);

        Corretor corretorAtualizado = corretorRepository.save(corretor);

        return toResponseDTO(corretorAtualizado);
    }

    public void deletarCorretorPorId(Long id){
        Corretor corretor = corretorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));
        corretorRepository.delete(corretor);
    }

    private CorretorResponseDTO toResponseDTO(Corretor corretor){
        return new CorretorResponseDTO(corretor.getId(),
                corretor.getNome(),
                corretor.getEmail(),
                corretor.getCreci(),
                corretor.getPerfil(),
                corretor.getImobiliaria().getId());
    }
}
