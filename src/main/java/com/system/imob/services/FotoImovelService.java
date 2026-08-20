package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.FotoImovelRequestDTO;
import com.system.imob.dtos.responses.FotoImovelResponseDTO;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.models.Corretor;
import com.system.imob.models.FotoImovel;
import com.system.imob.models.Imovel;
import com.system.imob.repositories.FotoImovelRepository;
import com.system.imob.repositories.ImovelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FotoImovelService {

    @Autowired
    private FotoImovelRepository fotoImovelRepository;

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private AuthUtil authUtil;

    public FotoImovelResponseDTO adicionarFoto(Long imovelId, FotoImovelRequestDTO dto) {
        Imovel imovel = imovelRepository.findById(imovelId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        verificarAcesso(imovel);

        // Limite de 15 fotos por imóvel
        List<FotoImovel> fotosExistentes = fotoImovelRepository.findByImovelId(imovelId);
        if (fotosExistentes.size() >= 15) {
            throw new RuntimeException("Limite de 15 fotos por imóvel atingido");
        }

        FotoImovel foto = new FotoImovel();
        foto.setUrl(dto.url());
        foto.setImovel(imovel);

        FotoImovel salva = fotoImovelRepository.save(foto);
        return toResponseDTO(salva);
    }

    public List<FotoImovelResponseDTO> listarFotos(Long imovelId) {
        Imovel imovel = imovelRepository.findById(imovelId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        verificarAcesso(imovel);

        return fotoImovelRepository.findByImovelId(imovelId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void removerFoto(Long imovelId, Long fotoId) {
        Imovel imovel = imovelRepository.findById(imovelId)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        verificarAcesso(imovel);

        FotoImovel foto = fotoImovelRepository.findById(fotoId)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada"));

        // Confirma que a foto pertence ao imóvel
        if (!foto.getImovel().getId().equals(imovelId)) {
            throw new RuntimeException("Esta foto não pertence a este imóvel");
        }

        fotoImovelRepository.delete(foto);
    }

    private void verificarAcesso(Imovel imovel) {
        Corretor logado = authUtil.getCorretorLogado();

        boolean mesmaImobiliaria = imovel.getImobiliaria().getId()
                .equals(logado.getImobiliaria().getId());

        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            if (!mesmaImobiliaria) {
                throw new AccessDeniedException("Este imóvel não pertence à sua imobiliária");
            }
            return;
        }

        boolean criadoPorEle = imovel.getCorretor().getId().equals(logado.getId());
        if (!criadoPorEle) {
            throw new AccessDeniedException("Você não tem acesso a este imóvel");
        }
    }

    private FotoImovelResponseDTO toResponseDTO(FotoImovel foto) {
        return new FotoImovelResponseDTO(
                foto.getId(),
                foto.getUrl(),
                foto.getImovel().getId()
        );
    }
}