package com.system.imob.models;

import com.system.imob.enums.StatusPlano;
import com.system.imob.enums.TipoPlano;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
public class Imobiliaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String nome;
    String cnpj;
    String email;
    String telefone;
    @Enumerated(EnumType.STRING)
    StatusPlano statusPlano;
    @Enumerated(EnumType.STRING)
    TipoPlano plano;
    LocalDate dataVencimento;
}
