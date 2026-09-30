package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.ImovelRequestDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.enums.Finalidade;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.enums.StatusImovel;
import com.system.imob.enums.TipoImovel;
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
        aplicarDados(imovel, dto);

        Corretor corretor = authUtil.getCorretorLogado();
        imovel.setCorretor(corretor);
        imovel.setImobiliaria(corretor.getImobiliaria());

        Imovel imovelSalvo = imovelRepository.save(imovel);
        return toResponseDTO(imovelSalvo);
    }

    public List<ImovelResponseDTO> listar(String endereco, StatusImovel status,
                                          Finalidade finalidade, TipoImovel tipo) {
        Corretor logado = authUtil.getCorretorLogado();

        List<Imovel> imoveis;
        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            imoveis = imovelRepository.findByImobiliariaId(logado.getImobiliaria().getId());
        } else {
            imoveis = imovelRepository.findByCorretorId(logado.getId());
        }

        // Filtro nulo = não enviado, então passa tudo. Assim os quatro são
        // opcionais e combináveis sem precisar de um if por parâmetro.
        String trecho = endereco != null && !endereco.isBlank()
                ? endereco.trim().toLowerCase()
                : null;

        return imoveis.stream()
                .filter(i -> trecho == null
                        || (i.getEndereco() != null && i.getEndereco().toLowerCase().contains(trecho)))
                .filter(i -> status == null || i.getStatusImovel() == status)
                .filter(i -> finalidade == null || i.getFinalidade() == finalidade)
                .filter(i -> tipo == null || i.getTipoImovel() == tipo)
                .map(this::toResponseDTO)
                .toList();
    }

    public ImovelResponseDTO atualizarImovel(Long id, ImovelRequestDTO dto) {
        Imovel imovel = imovelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        verificarAcesso(imovel);

        aplicarDados(imovel, dto);

        Imovel imovelSalvo = imovelRepository.save(imovel);
        return toResponseDTO(imovelSalvo);
    }

    // Copia o DTO para a entidade — usado no cadastro e na atualização
    private void aplicarDados(Imovel imovel, ImovelRequestDTO dto) {
        imovel.setEndereco(dto.endereco());
        imovel.setCEP(dto.CEP());
        imovel.setArea_m2(dto.area_m2());
        imovel.setFinalidade(dto.finalidade());
        imovel.setStatusImovel(dto.statusImovel());

        imovel.setTipoImovel(dto.tipoImovel());
        imovel.setValor(dto.valor());
        imovel.setQuartos(dto.quartos());
        imovel.setBanheiros(dto.banheiros());
        imovel.setVagasGaragem(dto.vagasGaragem());
        imovel.setBairro(dto.bairro());
        imovel.setCidade(dto.cidade());
        imovel.setEstado(dto.estado());
        imovel.setDescricao(dto.descricao());
        imovel.setPublicarPortais(dto.publicarPortais() != null ? dto.publicarPortais() : false);
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

    // Público porque o CorretorService também devolve imóveis — com 18 campos,
    // manter duas cópias desse mapeamento é pedir pra uma delas ficar pra trás
    public ImovelResponseDTO toResponseDTO(Imovel imovel) {
        return new ImovelResponseDTO(imovel.getId(), imovel.getEndereco(),
                imovel.getCEP(), imovel.getArea_m2(), imovel.getFinalidade(),
                imovel.getStatusImovel(), imovel.getImobiliaria().getId(), imovel.getFotos() != null
                ? imovel.getFotos().stream().map(f -> f.getUrl()).toList()
                : List.of(),
                imovel.getTipoImovel(), imovel.getValor(), imovel.getQuartos(),
                imovel.getBanheiros(), imovel.getVagasGaragem(), imovel.getBairro(),
                imovel.getCidade(), imovel.getEstado(), imovel.getDescricao(),
                imovel.getPublicarPortais());
    }
}