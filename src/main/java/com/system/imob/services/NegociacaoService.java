package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.NegociacaoRequestDTO;
import com.system.imob.dtos.requests.NegociacaoStatusRequestDTO;
import com.system.imob.dtos.responses.NegociacaoResponseDTO;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.models.Cliente;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imovel;
import com.system.imob.models.Negociacao;
import com.system.imob.repositories.ClienteRepository;
import com.system.imob.repositories.ImovelRepository;
import com.system.imob.repositories.NegociacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NegociacaoService {
    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private NegociacaoRepository negociacaoRepository;

    @Autowired
    private AuthUtil authUtil;

    public NegociacaoResponseDTO criarNegociacao(NegociacaoRequestDTO dto){
        Corretor corretor = authUtil.getCorretorLogado();

        Imovel imovel = imovelRepository.findById(dto.imovelId())
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // O imóvel precisa pertencer à imobiliária do corretor logado
        if (!imovel.getImobiliaria().getId().equals(corretor.getImobiliaria().getId())) {
            throw new AccessDeniedException("Este imóvel não pertence à sua imobiliária");
        }

        Negociacao negociacao = new Negociacao();
        negociacao.setFinalidade(dto.finalidade());
        negociacao.setStatusNegocio(dto.statusNegocio());
        negociacao.setDataInicio(dto.dataInicio());
        negociacao.setDataFim(dto.dataFim());
        negociacao.setValor(dto.valor());
        negociacao.setMotivoPerda(dto.motivoPerda());
        negociacao.setDataUltimaInteracao(LocalDate.now());
        negociacao.setImovel(imovel);
        negociacao.setCliente(cliente);
        negociacao.setCorretor(corretor);

        Negociacao salva = negociacaoRepository.save(negociacao);

        return toResponseDTO(salva);
    }

    public List<NegociacaoResponseDTO> listarNegociacoes(){
        Corretor logado = authUtil.getCorretorLogado();

        List<Negociacao> negociacoes;
        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            negociacoes = negociacaoRepository.findByImovelImobiliariaId(logado.getImobiliaria().getId());
        } else {
            negociacoes = negociacaoRepository.findByCorretorId(logado.getId());
        }

        return negociacoes.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public NegociacaoResponseDTO buscarNegociacaoPorId(Long id){
        Negociacao negociacao = negociacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negociação não encontrada"));

        verificarAcesso(negociacao);
        return toResponseDTO(negociacao);
    }

    public NegociacaoResponseDTO atualizarStatus(Long id, NegociacaoStatusRequestDTO dto){
        Negociacao negociacao = negociacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negociação não encontrada"));

        verificarAcesso(negociacao);

        negociacao.setStatusNegocio(dto.statusNegocio());
        negociacao.setMotivoPerda(dto.motivoPerda());
        negociacao.setDataUltimaInteracao(LocalDate.now());

        Negociacao atualizada = negociacaoRepository.save(negociacao);

        return toResponseDTO(atualizada);
    }

    // Verifica se o corretor logado pode acessar essa negociação
    private void verificarAcesso(Negociacao negociacao) {
        Corretor logado = authUtil.getCorretorLogado();

        boolean mesmaImobiliaria = negociacao.getImovel().getImobiliaria().getId()
                .equals(logado.getImobiliaria().getId());

        // ADMIN acessa qualquer negociação da própria imobiliária
        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            if (!mesmaImobiliaria) {
                throw new AccessDeniedException("Esta negociação não pertence à sua imobiliária");
            }
            return;
        }

        // CORRETOR só acessa as que ele mesmo criou
        boolean criadoPorEle = negociacao.getCorretor().getId().equals(logado.getId());
        if (!criadoPorEle) {
            throw new AccessDeniedException("Você não tem acesso a esta negociação");
        }
    }

    private NegociacaoResponseDTO toResponseDTO(Negociacao negociacao){
        return new NegociacaoResponseDTO(negociacao.getId(), negociacao.getFinalidade(),
                negociacao.getStatusNegocio(), negociacao.getDataInicio(), negociacao.getDataFim(),
                negociacao.getValor(), negociacao.getMotivoPerda(), negociacao.getDataUltimaInteracao(),
                negociacao.getImovel().getId(), negociacao.getCliente().getId(),
                negociacao.getCorretor().getId());
    }
}
