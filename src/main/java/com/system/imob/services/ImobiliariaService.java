package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.AtualizarLogoRequestDTO;
import com.system.imob.dtos.requests.ImobiliariaRequestDTO;
import com.system.imob.dtos.responses.ImobiliariaResponseDTO;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imobiliaria;
import com.system.imob.repositories.ImobiliariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImobiliariaService {

    // Limite de ~500KB de imagem em Base64 para não inchar o banco
    private static final int MAX_LOGO_BASE64_LENGTH = 700_000;

    @Autowired
    private ImobiliariaRepository imobiliariaRepository;

    @Autowired
    private AuthUtil authUtil;

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

    // Retorna a imobiliária do corretor logado (qualquer perfil)
    public ImobiliariaResponseDTO buscarMinhaImobiliaria() {
        Corretor corretor = authUtil.getCorretorLogado();
        return toResponseDTO(corretor.getImobiliaria());
    }

    // Só o ADMIN pode atualizar a logo da própria imobiliária
    public ImobiliariaResponseDTO atualizarLogo(AtualizarLogoRequestDTO dto) {
        Corretor corretor = authUtil.getCorretorLogado();

        if (corretor.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas o ADMIN pode atualizar a logo da imobiliária");
        }

        String logoBase64 = dto.logoBase64();
        if (logoBase64 != null && logoBase64.length() > MAX_LOGO_BASE64_LENGTH) {
            throw new RuntimeException("Logo muito grande. O tamanho máximo permitido é de aproximadamente 500KB");
        }

        Imobiliaria imobiliaria = corretor.getImobiliaria();
        imobiliaria.setLogoBase64(logoBase64);

        Imobiliaria salva = imobiliariaRepository.save(imobiliaria);
        return toResponseDTO(salva);
    }

    private ImobiliariaResponseDTO toResponseDTO(Imobiliaria imobiliaria) {
        return new ImobiliariaResponseDTO(imobiliaria.getId(), imobiliaria.getNome(),
                imobiliaria.getCnpj(), imobiliaria.getEmail(), imobiliaria.getTelefone(),
                imobiliaria.getStatusPlano(), imobiliaria.getPlano(),
                imobiliaria.getDataVencimento(), imobiliaria.getLogoBase64());
    }
}