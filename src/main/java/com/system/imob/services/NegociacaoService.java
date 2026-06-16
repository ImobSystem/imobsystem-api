package com.system.imob.services;

import com.system.imob.dtos.requests.NegociacaoRequestDTO;
import com.system.imob.dtos.requests.NegociacaoStatusRequestDTO;
import com.system.imob.dtos.responses.NegociacaoResponseDTO;
import com.system.imob.models.Cliente;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imovel;
import com.system.imob.models.Negociacao;
import com.system.imob.repositories.ClienteRepository;
import com.system.imob.repositories.CorretorRepository;
import com.system.imob.repositories.ImovelRepository;
import com.system.imob.repositories.NegociacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private CorretorRepository corretorRepository;

    @Autowired
    private NegociacaoRepository negociacaoRepository;

    public NegociacaoResponseDTO criarNegociacao(NegociacaoRequestDTO dto){
        Imovel imovel = imovelRepository.findById(dto.imovelId())
                .orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Corretor corretor = corretorRepository.findById(dto.corretorId())
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));

        // TODO: após JWT, verificar se o imóvel pertence à imobiliária logada
        if (!imovel.getImobiliaria().getId().equals(dto.imobiliariaId())) {
            throw new RuntimeException("Este imóvel não pertence à sua imobiliária");
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
        return negociacaoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public NegociacaoResponseDTO buscarNegociacaoPorId(Long id){
        Negociacao negociacao = negociacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negociação não encontrada"));
        return toResponseDTO(negociacao);
    }

    public NegociacaoResponseDTO atualizarStatus(Long id, NegociacaoStatusRequestDTO dto){
        Negociacao negociacao = negociacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Negociação não encontrada"));

        negociacao.setStatusNegocio(dto.statusNegocio());
        negociacao.setMotivoPerda(dto.motivoPerda());
        negociacao.setDataUltimaInteracao(LocalDate.now());

        Negociacao atualizada = negociacaoRepository.save(negociacao);

        return toResponseDTO(atualizada);
    }

    private NegociacaoResponseDTO toResponseDTO(Negociacao negociacao){
        return new NegociacaoResponseDTO(negociacao.getId(), negociacao.getFinalidade(),
                negociacao.getStatusNegocio(), negociacao.getDataInicio(), negociacao.getDataFim(),
                negociacao.getValor(), negociacao.getMotivoPerda(), negociacao.getDataUltimaInteracao(),
                negociacao.getImovel().getId(), negociacao.getCliente().getId(),
                negociacao.getCorretor().getId());
    }
}
