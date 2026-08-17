package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.config.JwtUtil;
import com.system.imob.dtos.requests.CorretorRequestDTO;
import com.system.imob.dtos.requests.LoginRequestDTO;
import com.system.imob.dtos.requests.RegistroRequestDTO;
import com.system.imob.dtos.responses.CorretorResponseDTO;
import com.system.imob.dtos.responses.LoginResponseDTO;
import com.system.imob.dtos.responses.RegistroResponseDTO;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.enums.StatusPlano;
import com.system.imob.enums.TipoPlano;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imobiliaria;
import com.system.imob.repositories.CorretorRepository;
import com.system.imob.repositories.ImobiliariaRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public LoginResponseDTO login (LoginRequestDTO dto){
        Corretor corretor = corretorRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Email ou senha inválidos"));


        if (!passwordEncoder.matches(dto.senha(), corretor.getSenha())) {
            throw new RuntimeException("Email ou senha inválidos");
        }

        String token = jwtUtil.gerarToken(corretor);

        return new LoginResponseDTO(token, corretor.getEmail(), corretor.getPerfil());
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
        Corretor corretor = corretorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Corretor não encontrado"));
        return toResponseDTO(corretor);
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
}
