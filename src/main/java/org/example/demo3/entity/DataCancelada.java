package org.example.demo3.entity;

import java.time.LocalDate;

public class DataCancelada {
    private Integer id_data_cancelada;
    private Integer usuario_id;
    private LocalDate data_bloqueio;
    private String  descricao;
    private String tipo;

    public DataCancelada(Integer id_data_cancelada, Integer usuario_id, LocalDate data_bloqueio, String descricao, String tipo) {
        this.id_data_cancelada = id_data_cancelada;
        this.usuario_id = usuario_id;
        this.data_bloqueio = data_bloqueio;
        this.descricao = descricao;
        this.tipo = tipo;
    }

    public Integer getId_data_cancelada() {
        return id_data_cancelada;
    }

    public Integer getUsuario_id() {
        return usuario_id;
    }

    public LocalDate getData_bloqueio() {
        return data_bloqueio;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getTipo() {
        return tipo;
    }
}
