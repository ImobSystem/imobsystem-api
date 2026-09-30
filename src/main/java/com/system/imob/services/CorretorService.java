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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CorretorService {

    private static final Logger log = LoggerFactory.getLogger(CorretorService.class);

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
    @Autowired
    private ImovelService imovelService;
    private AsaasService asaasService;

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

        // O trial de 30 dias vale independente do Asaas, então uma falha aqui não pode barrar o cadastro.
        // O cliente no Asaas é criado depois, na primeira chamada de assinarPlano().
        try {
            imobiliariaSalva.setAsaasCustomerId(asaasService.criarClienteAsaas(imobiliariaSalva));
            imobiliariaRepository.save(imobiliariaSalva);
        } catch (Exception e) {
            log.error("Erro ao criar cliente no Asaas para a imobiliária {}: {}",
                    imobiliariaSalva.getId(), e.getMessage());
        }

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

    public List<CorretorResponseDTO> listarCorretores(String nome, String email, PerfilUsuario perfil){
        Corretor logado = authUtil.getCorretorLogado();

        if (logado.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas ADMIN pode listar corretores");
        }

        List<Corretor> corretores =
                corretorRepository.findByImobiliariaId(logado.getImobiliaria().getId());

        // Filtro nulo = não enviado, então passa tudo. Assim os três são
        // opcionais e combináveis sem precisar de um if por parâmetro.
        String trechoNome = nome != null && !nome.isBlank() ? nome.trim().toLowerCase() : null;
        String trechoEmail = email != null && !email.isBlank() ? email.trim().toLowerCase() : null;

        return corretores.stream()
                .filter(c -> trechoNome == null
                        || (c.getNome() != null && c.getNome().toLowerCase().contains(trechoNome)))
                .filter(c -> trechoEmail == null
                        || (c.getEmail() != null && c.getEmail().toLowerCase().contains(trechoEmail)))
                .filter(c -> perfil == null || c.getPerfil() == perfil)
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

    public void deletarCorretor(Long id){
        Corretor logado = authUtil.getCorretorLogado();

        if (logado.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas ADMIN pode excluir corretores");
        }

        Corretor corretor = corretorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));

        // Só pode excluir corretor da própria imobiliária
        if (!corretor.getImobiliaria().getId().equals(logado.getImobiliaria().getId())) {
            throw new AccessDeniedException("Este corretor não pertence à sua imobiliária");
        }

        // Não pode excluir a si mesmo
        if (corretor.getId().equals(logado.getId())) {
            throw new RuntimeException("Você não pode excluir sua própria conta");
        }

        // Não pode excluir o último ADMIN — a imobiliária ficaria sem quem gerencia plano e equipe
        if (corretor.getPerfil() == PerfilUsuario.ADMIN) {
            long totalAdmins = corretorRepository.findByImobiliariaId(logado.getImobiliaria().getId())
                    .stream()
                    .filter(c -> c.getPerfil() == PerfilUsuario.ADMIN)
                    .count();
            if (totalAdmins <= 1) {
                throw new RuntimeException("Não é possível excluir o único administrador da imobiliária");
            }
        }

        // Sem isso o delete estouraria numa violação de chave estrangeira,
        // sem dizer ao usuário o que está no caminho
        long imoveis = imovelRepository.countByCorretorId(id);
        long clientes = clienteRepository.countByCorretorId(id);
        long negociacoes = negociacaoRepository.countByCorretorId(id);

        if (imoveis > 0 || clientes > 0 || negociacoes > 0) {
            throw new RuntimeException(
                    "Este corretor possui registros vinculados ("
                    + imoveis + " imóveis, " + clientes + " clientes, " + negociacoes + " negociações). "
                    + "Transfira ou exclua esses registros antes de remover o corretor."
            );
        }

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
        return imovelService.toResponseDTO(imovel);
    }

    private ClienteResponseDTO toClienteResponseDTO(Cliente cliente){
        return new ClienteResponseDTO(cliente.getId(), cliente.getNome(),
                cliente.getCpf(), cliente.getEmail(), cliente.getTelefone(), cliente.getTipoCliente());
    }
}
