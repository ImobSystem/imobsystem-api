package com.system.imob.models;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import com.system.imob.enums.TipoImovel;
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

    @Enumerated(EnumType.STRING)
    private TipoImovel tipoImovel;

    private Double valor;              // preço de venda ou aluguel

    private Integer quartos;
    private Integer banheiros;
    private Integer vagasGaragem;

    private String bairro;
    private String cidade;
    private String estado;             // sigla UF (ex: "PE")

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private Boolean publicarPortais;   // se o imóvel deve aparecer no feed XML dos portais
}
