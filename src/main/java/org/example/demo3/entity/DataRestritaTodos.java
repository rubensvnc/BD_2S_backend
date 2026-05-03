package org.example.demo3.entity;

import java.time.LocalDate;

public class DataRestritaTodos {
    private Integer id_data_restrita;
    private Integer adm_id;
    private LocalDate data_bloqueio;
    private String descricao;

    public DataRestritaTodos(Integer id_data_restrita, Integer adm_id, LocalDate data_bloqueio, String descricao) {
        this.id_data_restrita = id_data_restrita;
        this.adm_id = adm_id;
        this.data_bloqueio = data_bloqueio;
        this.descricao = descricao;
    }

    public DataRestritaTodos(Integer adm_id, LocalDate data_bloqueio, String descricao) {
        this.adm_id = adm_id;
        this.data_bloqueio = data_bloqueio;
        this.descricao = descricao;
    }

    public Integer getId_data_restrita() { return id_data_restrita; }
    public Integer getAdm_id() { return adm_id; }
    public LocalDate getData_bloqueio() { return data_bloqueio; }
    public String getDescricao() { return descricao; }
}