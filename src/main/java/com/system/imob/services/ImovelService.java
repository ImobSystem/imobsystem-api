package com.system.imob.services;

import com.system.imob.dtos.requests.ClienteRequestDTO;
import com.system.imob.dtos.requests.ImovelRequestDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import com.system.imob.models.Cliente;
import com.system.imob.models.Imobiliaria;
import com.system.imob.models.Imovel;
import com.system.imob.repositories.ClienteRepository;
import com.system.imob.repositories.ImobiliariaRepository;
import com.system.imob.repositories.ImovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ImovelService {
    @Autowired
    private ImovelRepository imovelRepository;
    @Autowired
    private ImobiliariaRepository imobiliariaRepository;

    public ImovelResponseDTO cadastrarImovel (ImovelRequestDTO dto){
        Imovel imovel = new Imovel();
        imovel.setEndereco(dto.endereco());
        imovel.setCEP(dto.CEP());
        imovel.setArea_m2(dto.area_m2());
        imovel.setFinalidade(dto.finalidade());
        imovel.setStatusImovel(dto.statusImovel());
        Imobiliaria imobiliaria = imobiliariaRepository.findById(dto.imobiliariaId()).orElseThrow(() -> new RuntimeException("Imobiliaria não encontrada"));
        imovel.setImobiliaria(imobiliaria);

        Imovel imovelSalvo = imovelRepository.save(imovel);

        return new ImovelResponseDTO(imovelSalvo.getId(), imovelSalvo.getEndereco(), imovelSalvo.getCEP(), imovelSalvo.getArea_m2(), imovelSalvo.getFinalidade(), imovelSalvo.getStatusImovel(), imovelSalvo.getImobiliaria().getId());
    }

    public List<ImovelResponseDTO> listarPorStatusETipo (StatusImovel status, Finalidade finalidade){
        return imovelRepository.findByStatusImovelAndFinalidade(status, finalidade)
                .stream()
                .map(imovel -> new ImovelResponseDTO(imovel.getId(),
                        imovel.getEndereco(),
                        imovel.getCEP(),
                        imovel.getArea_m2(),
                        imovel.getFinalidade(),
                        imovel.getStatusImovel(),
                        imovel.getImobiliaria().getId())).toList();
    }

    public ImovelResponseDTO atualizarImovel(Long id, ImovelRequestDTO dto) {
        Imovel imovel = imovelRepository.findById(id).orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));
        imovel.setEndereco(dto.endereco());
        imovel.setCEP(dto.CEP());
        imovel.setArea_m2(dto.area_m2());
        imovel.setFinalidade(dto.finalidade());
        imovel.setStatusImovel(dto.statusImovel());
        Imobiliaria imobiliaria = imobiliariaRepository.findById(dto.imobiliariaId()).orElseThrow(() -> new RuntimeException("Imobiliaria não encontrada"));
        imovel.setImobiliaria(imobiliaria);


        Imovel imovelSalvo = imovelRepository.save(imovel);

        return new ImovelResponseDTO(imovelSalvo.getId(),
                imovelSalvo.getEndereco(),
                imovelSalvo.getCEP(),
                imovelSalvo.getArea_m2(),
                imovelSalvo.getFinalidade(),
                imovelSalvo.getStatusImovel(),
                imovelSalvo.getImobiliaria().getId());
        //TODO: após JWT, substituir dto.imobiliariaId() por imobiliariaId extraído do token
    }

    public ImovelResponseDTO buscarImovelPorId(Long id) {
    Imovel imovelBuscado = imovelRepository.findById(id).orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));
    return new ImovelResponseDTO(imovelBuscado.getId(),
            imovelBuscado.getEndereco(),
            imovelBuscado.getCEP(),
            imovelBuscado.getArea_m2(),
            imovelBuscado.getFinalidade(),
            imovelBuscado.getStatusImovel(),
            imovelBuscado.getImobiliaria().getId());
    }

    public void deletarImovelPorId(Long id) {
        Imovel imovelBuscado = imovelRepository.findById(id).orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));
        imovelRepository.delete(imovelBuscado);
    }
}

