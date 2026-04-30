package org.example.demo3.entity;

import java.time.LocalDate;

public class Datas_Restritas {
    private Integer id_restricao; 
    private LocalDate data_bloqueio; 
    private String descricao; 
    private Integer id_referencia; 

    
    public Datas_Restritas(Integer id_restricao, LocalDate data_bloqueio, String descricao, Integer id_referencia) {
        this.id_restricao = id_restricao;
        this.data_bloqueio = data_bloqueio;
        this.descricao = descricao;
        this.id_referencia = id_referencia;
    }

    
    public Integer getId_restricao() { return id_restricao; }
    public LocalDate getData_bloqueio() { return data_bloqueio; }
    public String getDescricao() { return descricao; }
    public Integer getId_referencia() { return id_referencia; }
}
