package com.system.imob.models;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusNegocio;
import com.system.imob.models.Cliente;
import com.system.imob.models.Imovel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@Entity
public class Negociacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Finalidade finalidade;
    StatusNegocio statusNegocio;
    LocalDate dataInicio;
    LocalDate dataFim;
    Double valor;

    @ManyToOne
    @JoinColumn(name = "imovel_id")
    Imovel imovel;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    Cliente cliente;
}