package com.system.imob.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
public class FotoImovel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String url;
    @ManyToOne
    @JoinColumn(name = "imovel_id")
    Imovel imovel;
}
