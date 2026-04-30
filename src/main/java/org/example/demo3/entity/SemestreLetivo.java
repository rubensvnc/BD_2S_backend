package org.example.demo3.entity;

import java.util.Date;

public class SemestreLetivo {
    private Long id_semestre;
    private String rotulo;
    private Date data_inicio;
    private Date data_fim;
    private Boolean ativo;

    public Long getId_semestre() {
        return id_semestre;
    }

    public void setId_semestre(Long id_semestre) {
        this.id_semestre = id_semestre;
    }

    public String getRotulo() {
        return rotulo;
    }

    public void setRotulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public Date getData_inicio() {
        return data_inicio;
    }

    public void setData_inicio(Date data_inicio) {
        this.data_inicio = data_inicio;
    }

    public Date getData_fim() {
        return data_fim;
    }

    public void setData_fim(Date data_fim) {
        this.data_fim = data_fim;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
