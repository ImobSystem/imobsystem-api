package com.system.imob.services;

import com.system.imob.dtos.requests.ClienteRequestDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.models.Cliente;
import com.system.imob.models.Imovel;
import com.system.imob.repositories.ClienteRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;


    public ClienteResponseDTO cadastrarCliente (ClienteRequestDTO dto){
        Cliente cliente = new Cliente();
        cliente.setNome(dto.nome());
        cliente.setCpf(dto.cpf());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setTipoCliente(dto.tipoCliente());

        Cliente clienteSalvo = clienteRepository.save(cliente);

        return new ClienteResponseDTO(clienteSalvo.getId(),
                clienteSalvo.getNome(), clienteSalvo.getCpf(), clienteSalvo.getEmail(), clienteSalvo.getTelefone());
    }

    public ClienteResponseDTO buscarClientePorId(Long id){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        return new ClienteResponseDTO(cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getEmail(), cliente.getTelefone());
    }

    public List<ClienteResponseDTO> listarClientes(){
        return clienteRepository.findAll().stream().map(( cliente) -> new ClienteResponseDTO(cliente.getId(), cliente.getNome(), cliente.getCpf(), cliente.getEmail(), cliente.getTelefone())).toList();
    }

    public ClienteResponseDTO atualizarClientePorId(Long id, ClienteRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        cliente.setNome(dto.nome());
        cliente.setCpf(dto.cpf());
        cliente.setEmail(dto.email());
        cliente.setTelefone(dto.telefone());
        cliente.setTipoCliente(dto.tipoCliente());

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return new ClienteResponseDTO(clienteAtualizado.getId(),
                clienteAtualizado.getNome(), clienteAtualizado.getCpf(), clienteAtualizado.getEmail(), clienteAtualizado.getTelefone());
    }

    public void deletarClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        clienteRepository.delete(cliente);
    }
}
