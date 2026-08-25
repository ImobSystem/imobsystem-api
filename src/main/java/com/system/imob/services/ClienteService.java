package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.ClienteRequestDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.enums.TipoCliente;
import com.system.imob.models.Cliente;
import com.system.imob.models.Corretor;
import com.system.imob.repositories.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuthUtil authUtil;

    public ClienteResponseDTO cadastrarCliente(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNome(dto.nome());
        cliente.setCpf(dto.cpf());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setTipoCliente(dto.tipoCliente());

        Corretor corretor = authUtil.getCorretorLogado();
        cliente.setCorretor(corretor);
        cliente.setImobiliaria(corretor.getImobiliaria());

        Cliente clienteSalvo = clienteRepository.save(cliente);
        return toResponseDTO(clienteSalvo);
    }

    public List<ClienteResponseDTO> listarClientes(String nome, String email, TipoCliente tipo) {
        Corretor logado = authUtil.getCorretorLogado();

        List<Cliente> clientes;
        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            clientes = clienteRepository.findByImobiliariaId(logado.getImobiliaria().getId());
        } else {
            clientes = clienteRepository.findByCorretorId(logado.getId());
        }

        if (nome != null && !nome.isBlank()) {
            clientes = clientes.stream()
                    .filter(c -> c.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
        }
        if (email != null && !email.isBlank()) {
            clientes = clientes.stream()
                    .filter(c -> c.getEmail().toLowerCase().contains(email.toLowerCase()))
                    .toList();
        }
        if (tipo != null) {
            clientes = clientes.stream()
                    .filter(c -> c.getTipoCliente() == tipo)
                    .toList();
        }

        return clientes.stream().map(this::toResponseDTO).toList();
    }

    public ClienteResponseDTO buscarClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        verificarAcesso(cliente);
        return toResponseDTO(cliente);
    }

    public ClienteResponseDTO atualizarClientePorId(Long id, ClienteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        verificarAcesso(cliente);

        cliente.setNome(dto.nome());
        cliente.setCpf(dto.cpf());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setTipoCliente(dto.tipoCliente());

        Cliente clienteAtualizado = clienteRepository.save(cliente);
        return toResponseDTO(clienteAtualizado);
    }

    public void deletarClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        verificarAcesso(cliente);
        clienteRepository.delete(cliente);
    }

    // Verifica se o corretor logado pode acessar esse cliente
    private void verificarAcesso(Cliente cliente) {
        Corretor logado = authUtil.getCorretorLogado();

        boolean mesmaImobiliaria = cliente.getImobiliaria().getId()
                .equals(logado.getImobiliaria().getId());

        // ADMIN acessa qualquer cliente da própria imobiliária
        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            if (!mesmaImobiliaria) {
                throw new AccessDeniedException("Este cliente não pertence à sua imobiliária");
            }
            return;
        }

        // CORRETOR só acessa os que ele mesmo criou
        boolean criadoPorEle = cliente.getCorretor().getId().equals(logado.getId());
        if (!criadoPorEle) {
            throw new AccessDeniedException("Você não tem acesso a este cliente");
        }
    }

    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(cliente.getId(), cliente.getNome(),
                cliente.getCpf(), cliente.getEmail(), cliente.getTelefone(), cliente.getTipoCliente());
    }
}
