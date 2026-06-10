package com.system.imob.models;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Imovel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    // tipo imovel
    String endereco;
    String CEP;
    Double area_m2;
    Finalidade finalidade;
    StatusImovel statusImovel;
    @ManyToOne
    @JoinColumn(name = "imobiliaria_id")
    Imobiliaria imobiliaria;
}
