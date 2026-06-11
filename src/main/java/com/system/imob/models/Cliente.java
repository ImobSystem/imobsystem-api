package com.system.imob.models;

import com.system.imob.enums.TipoCliente;
import jakarta.persistence.*;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String nome;
    String cpf;
    String email;
    Integer telefone;
    TipoCliente tipoCliente;
    @ManyToOne
    @JoinColumn(name = "imobiliaria_id")
    Imobiliaria imobiliaria;
}
