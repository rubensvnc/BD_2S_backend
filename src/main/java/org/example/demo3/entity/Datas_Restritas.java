package org.example.demo3.entity;

import java.time.LocalDate;

public class Datas_Restritas {
    private Integer id_restricao; // INT no banco
    private LocalDate data_bloqueio; // DATE no banco
    private String descricao; // VARCHAR(100) no banco
    private Integer id_referencia; // INT no banco (FK)

    // Construtor completo
    public Datas_Restritas(Integer id_restricao, LocalDate data_bloqueio, String descricao, Integer id_referencia) {
        this.id_restricao = id_restricao;
        this.data_bloqueio = data_bloqueio;
        this.descricao = descricao;
        this.id_referencia = id_referencia;
    }

    // Getters
    public Integer getId_restricao() { return id_restricao; }
    public LocalDate getData_bloqueio() { return data_bloqueio; }
    public String getDescricao() { return descricao; }
    public Integer getId_referencia() { return id_referencia; }
}