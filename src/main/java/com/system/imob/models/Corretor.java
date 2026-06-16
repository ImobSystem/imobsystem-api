package com.system.imob.models;

import com.system.imob.enums.PerfilUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Corretor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String nome;
    String email;
    String senha; // será encriptada quando o JWT entrar

    String creci; // registro profissional do corretor

    @Enumerated(EnumType.STRING)
    PerfilUsuario perfil; // ADMIN ou CORRETOR

    @ManyToOne
    @JoinColumn(name = "imobiliaria_id")
    Imobiliaria imobiliaria;


}