package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.ImovelRequestDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.enums.Finalidade;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.enums.StatusImovel;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imovel;
import com.system.imob.repositories.ImovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImovelService {

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private AuthUtil authUtil;

    public ImovelResponseDTO cadastrarImovel(ImovelRequestDTO dto) {
        Imovel imovel = new Imovel();
        imovel.setEndereco(dto.endereco());
        imovel.setCEP(dto.CEP());
        imovel.setArea_m2(dto.area_m2());
        imovel.setFinalidade(dto.finalidade());
        imovel.setStatusImovel(dto.statusImovel());

        Corretor corretor = authUtil.getCorretorLogado();
        imovel.setCorretor(corretor);
        imovel.setImobiliaria(corretor.getImobiliaria());

        Imovel imovelSalvo = imovelRepository.save(imovel);
        return toResponseDTO(imovelSalvo);
    }

    public List<ImovelResponseDTO> listar() {
        Corretor logado = authUtil.getCorretorLogado();

        List<Imovel> imoveis;
        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            imoveis = imovelRepository.findByImobiliariaId(logado.getImobiliaria().getId());
        } else {
            imoveis = imovelRepository.findByCorretorId(logado.getId());
        }

        return imoveis.stream().map(this::toResponseDTO).toList();
    }

    public ImovelResponseDTO atualizarImovel(Long id, ImovelRequestDTO dto) {
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        verificarAcesso(imovel);

        imovel.setEndereco(dto.endereco());
        imovel.setCEP(dto.CEP());
        imovel.setArea_m2(dto.area_m2());
        imovel.setFinalidade(dto.finalidade());
        imovel.setStatusImovel(dto.statusImovel());

        Imovel imovelSalvo = imovelRepository.save(imovel);
        return toResponseDTO(imovelSalvo);
    }

    public ImovelResponseDTO buscarImovelPorId(Long id) {
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        verificarAcesso(imovel);
        return toResponseDTO(imovel);
    }

    public void deletarImovelPorId(Long id) {
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        verificarAcesso(imovel);
        imovelRepository.delete(imovel);
    }

    // Verifica se o corretor logado pode acessar esse imóvel
    private void verificarAcesso(Imovel imovel) {
        Corretor logado = authUtil.getCorretorLogado();

        boolean mesmaImobiliaria = imovel.getImobiliaria().getId()
                .equals(logado.getImobiliaria().getId());

        // ADMIN acessa qualquer imóvel da própria imobiliária
        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            if (!mesmaImobiliaria) {
                throw new AccessDeniedException("Este imóvel não pertence à sua imobiliária");
            }
            return;
        }

        // CORRETOR só acessa os que ele mesmo criou
        boolean criadoPorEle = imovel.getCorretor().getId().equals(logado.getId());
        if (!criadoPorEle) {
            throw new AccessDeniedException("Você não tem acesso a este imóvel");
        }
    }

    private ImovelResponseDTO toResponseDTO(Imovel imovel) {
        return new ImovelResponseDTO(imovel.getId(), imovel.getEndereco(),
                imovel.getCEP(), imovel.getArea_m2(), imovel.getFinalidade(),
                imovel.getStatusImovel(), imovel.getImobiliaria().getId(), imovel.getFotos() != null
                ? imovel.getFotos().stream().map(f -> f.getUrl()).toList()
                : List.of());
    }
}