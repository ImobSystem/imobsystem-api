package com.system.imob.models;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Imovel {
    @ManyToOne
    @JoinColumn(name = "corretor_id")
    Corretor corretor;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    // tipo imovel
    String endereco;
    String CEP; 
    Double area_m2;
    @Enumerated(EnumType.STRING)
    Finalidade finalidade;
    @Enumerated(EnumType.STRING)
    StatusImovel statusImovel;
    @ManyToOne
    @JoinColumn(name = "imobiliaria_id")
    Imobiliaria imobiliaria;
    @OneToMany(mappedBy = "imovel", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FotoImovel> fotos;
}
