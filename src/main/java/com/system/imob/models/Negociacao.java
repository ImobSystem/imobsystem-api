package com.system.imob.models;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusNegocio;

import java.util.Date;

public class Negociacao {
    Long id;
    Finalidade finalidade; //Aluguel ou venda
    StatusNegocio statusNegocio; //Aberta, em andamento ou fechada
    Date data_inicio; //caso de aluguel
    Date data_fim; //caso de aluguel
    Double valor;
    Imovel imovel_id;
    Cliente cliente_id;

}
