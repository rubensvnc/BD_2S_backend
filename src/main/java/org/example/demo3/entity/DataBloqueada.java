package org.example.demo3.entity;
import java.time.LocalDate;

public class DataBloqueada {
    private LocalDate data_bloqueio;
    private String descricao;
    private Long id_restricao;
    private Long id_referencia;


    public DataBloqueada(LocalDate data_bloqueio, String descricao, Long id_restricao, Long id_referencia) {
        this.data_bloqueio = data_bloqueio;
        this.descricao = descricao;
        this.id_restricao = id_restricao;
        this.id_referencia = id_referencia;
    }

    public LocalDate getData_bloqueio() {
        return data_bloqueio;
    }

    public String getDescricao() {
        return descricao;
    }

    public Long getId_restricao() {
        return id_restricao;
    }

    public Long getId_referencia() {
        return id_referencia;
    }
}