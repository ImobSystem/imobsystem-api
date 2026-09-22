package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.config.JwtUtil;
import com.system.imob.dtos.requests.CorretorRequestDTO;
import com.system.imob.dtos.requests.LoginRequestDTO;
import com.system.imob.dtos.requests.RegistroRequestDTO;
import com.system.imob.dtos.responses.CaptacaoResponseDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.dtos.responses.CorretorMetricasDTO;
import com.system.imob.dtos.responses.CorretorResponseDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.dtos.responses.LoginResponseDTO;
import com.system.imob.dtos.responses.RegistroResponseDTO;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.enums.StatusNegocio;
import com.system.imob.enums.StatusPlano;
import com.system.imob.enums.TipoPlano;
import com.system.imob.models.Cliente;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imobiliaria;
import com.system.imob.models.Imovel;
import com.system.imob.repositories.ClienteRepository;
import com.system.imob.repositories.CorretorRepository;
import com.system.imob.repositories.ImobiliariaRepository;
import com.system.imob.repositories.ImovelRepository;
import com.system.imob.repositories.NegociacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CorretorService {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private CorretorRepository corretorRepository;
    @Autowired
    private ImobiliariaRepository imobiliariaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ImovelRepository imovelRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private NegociacaoRepository negociacaoRepository;

    public LoginResponseDTO login (LoginRequestDTO dto){
        Corretor corretor = corretorRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Email ou senha inválidos"));


        if (!passwordEncoder.matches(dto.senha(), corretor.getSenha())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        String token = jwtUtil.gerarToken(corretor);

        return new LoginResponseDTO(token, corretor.getEmail(), corretor.getPerfil());
    }

    public List<CaptacaoResponseDTO> listarCaptacoes() {
        Corretor logado = authUtil.getCorretorLogado();

        if (logado.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas ADMIN pode ver captações");
        }

        Long imobiliariaId = logado.getImobiliaria().getId();
        List<Corretor> corretores = corretorRepository.findByImobiliariaId(imobiliariaId);

        return corretores.stream()
                .map(c -> new CaptacaoResponseDTO(
                        c.getId(),
                        c.getNome(),
                        imovelRepository.countByCorretorId(c.getId())))
                .toList();
    }
    @Transactional
    public RegistroResponseDTO registrar(RegistroRequestDTO dto){
        corretorRepository.findByEmail(dto.emailAdmin())
                .ifPresent(c -> {
                    throw new RuntimeException("Já existe uma conta com este e-mail");
                });

        Imobiliaria imobiliaria = new Imobiliaria();
        imobiliaria.setNome(dto.nomeImobiliaria());
        imobiliaria.setCnpj(dto.cnpj());
        imobiliaria.setEmail(dto.emailImobiliaria());
        imobiliaria.setTelefone(dto.telefone());
        imobiliaria.setStatusPlano(StatusPlano.ATIVO);
        imobiliaria.setPlano(TipoPlano.BASICO);
        imobiliaria.setDataVencimento(LocalDate.now().plusDays(30));
        Imobiliaria imobiliariaSalva = imobiliariaRepository.save(imobiliaria);

        Corretor corretor = new Corretor();
        corretor.setNome(dto.nomeAdmin());
        corretor.setEmail(dto.emailAdmin());
        corretor.setSenha(passwordEncoder.encode(dto.senha()));
        corretor.setCreci(dto.creci());
        corretor.setPerfil(PerfilUsuario.ADMIN);
        corretor.setImobiliaria(imobiliariaSalva);
        Corretor corretorSalvo = corretorRepository.save(corretor);

        String token = jwtUtil.gerarToken(corretorSalvo);

        return new RegistroResponseDTO(
                token,
                corretorSalvo.getEmail(),
                corretorSalvo.getPerfil().name(),
                imobiliariaSalva.getId());
    }

    public CorretorResponseDTO cadastrarCorretor(CorretorRequestDTO dto){
        Corretor corretor = new Corretor();
        corretor.setNome(dto.nome());
        corretor.setEmail(dto.email());
        corretor.setSenha(passwordEncoder.encode(dto.senha())); // TODO: encriptar quando o JWT entrar
        corretor.setCreci(dto.creci());
        corretor.setPerfil(dto.perfil());
        Imobiliaria imobiliaria = imobiliariaRepository.findById(dto.imobiliariaId())
                .orElseThrow(() -> new RuntimeException("Imobiliaria não encontrada"));
        corretor.setImobiliaria(imobiliaria);

        Corretor corretorSalvo = corretorRepository.save(corretor);

        return toResponseDTO(corretorSalvo);
    }

    public CorretorResponseDTO buscarCorretorPorId(Long id){
        Corretor corretor = verificarAcessoAoPerfil(id);
        return toResponseDTO(corretor);
    }

    public List<ImovelResponseDTO> listarImoveisDoCorretor(Long corretorId){
        verificarAcessoAoPerfil(corretorId);

        return imovelRepository.findByCorretorId(corretorId).stream()
                .map(this::toImovelResponseDTO)
                .toList();
    }

    public List<ClienteResponseDTO> listarClientesDoCorretor(Long corretorId){
        verificarAcessoAoPerfil(corretorId);

        return clienteRepository.findByCorretorId(corretorId).stream()
                .map(this::toClienteResponseDTO)
                .toList();
    }

    public CorretorMetricasDTO buscarMetricasDoCorretor(Long corretorId){
        Corretor corretor = verificarAcessoAoPerfil(corretorId);

        return new CorretorMetricasDTO(
                corretor.getId(),
                corretor.getNome(),
                imovelRepository.countByCorretorId(corretorId),
                clienteRepository.countByCorretorId(corretorId),
                negociacaoRepository.countByCorretorId(corretorId),
                negociacaoRepository.countByCorretorIdAndStatusNegocio(corretorId, StatusNegocio.GANHO));
    }

    // Só o ADMIN acessa o perfil de um corretor, e só dentro da própria imobiliária
    private Corretor verificarAcessoAoPerfil(Long corretorId){
        Corretor logado = authUtil.getCorretorLogado();

        if (logado.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas ADMIN pode acessar perfil de corretores");
        }

        Corretor corretor = corretorRepository.findById(corretorId)
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));

        if (!corretor.getImobiliaria().getId().equals(logado.getImobiliaria().getId())) {
            throw new AccessDeniedException("Este corretor não pertence à sua imobiliária");
        }

        return corretor;
    }

    public List<CorretorResponseDTO> listarCorretores(){
        Long imobiliariaId = authUtil.getImobiliariaId();
        return corretorRepository.findByImobiliariaId(imobiliariaId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public CorretorResponseDTO atualizarCorretorPorId(Long id, CorretorRequestDTO dto){
        // TODO: tratar atualização sem alterar senha
        Corretor corretor = corretorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));
        corretor.setNome(dto.nome());
        corretor.setEmail(dto.email());
        corretor.setSenha(passwordEncoder.encode(dto.senha()));
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

    private ImovelResponseDTO toImovelResponseDTO(Imovel imovel){
        return new ImovelResponseDTO(imovel.getId(), imovel.getEndereco(),
                imovel.getCEP(), imovel.getArea_m2(), imovel.getFinalidade(),
                imovel.getStatusImovel(), imovel.getImobiliaria().getId(), imovel.getFotos() != null
                ? imovel.getFotos().stream().map(f -> f.getUrl()).toList()
                : List.of());
    }

    private ClienteResponseDTO toClienteResponseDTO(Cliente cliente){
        return new ClienteResponseDTO(cliente.getId(), cliente.getNome(),
                cliente.getCpf(), cliente.getEmail(), cliente.getTelefone(), cliente.getTipoCliente());
    }
}
