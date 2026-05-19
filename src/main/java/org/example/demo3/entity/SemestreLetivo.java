package org.example.demo3.entity;


import java.time.LocalDate;

public class SemestreLetivo{
    private Integer id_semestre_letivo;
    private Integer criado_por_adm_id;
    private Integer ano;
    private Integer numero_semestre;
    private LocalDate data_inicio;
    private LocalDate data_fim;
    private LocalDate data_tg;
    private LocalDate data_feira;

    public SemestreLetivo(Integer id_semestre_letivo, Integer criado_por_adm_id, Integer ano,
                          Integer numero_semestre, LocalDate data_inicio, LocalDate data_fim,
                          LocalDate data_tg, LocalDate data_feira) {
        this.id_semestre_letivo = id_semestre_letivo;
        this.criado_por_adm_id = criado_por_adm_id;
        this.ano = ano;
        this.numero_semestre = numero_semestre;
        this.data_inicio = data_inicio;
        this.data_fim = data_fim;
        this.data_tg = data_tg;
        this.data_feira = data_feira;
    }

    public SemestreLetivo() {
    }

    public Integer getId_semestre_letivo() {
        return id_semestre_letivo;
    }

    public void setId_semestre_letivo(Integer id_semestre_letivo) {
        this.id_semestre_letivo = id_semestre_letivo;
    }

    public Integer getCriado_por_adm_id() {
        return criado_por_adm_id;
    }

    public void setCriado_por_adm_id(Integer criado_por_adm_id) {
        this.criado_por_adm_id = criado_por_adm_id;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getNumero_semestre() {
        return numero_semestre;
    }

    public void setNumero_semestre(Integer numero_semestre) {
        this.numero_semestre = numero_semestre;
    }

    public LocalDate getData_inicio() {
        return data_inicio;
    }

    public void setData_inicio(LocalDate data_inicio) {
        this.data_inicio = data_inicio;
    }

    public LocalDate getData_fim() {
        return data_fim;
    }

    public void setData_fim(LocalDate data_fim) {
        this.data_fim = data_fim;
    }

    public LocalDate getData_tg() {
        return data_tg;
    }

    public void setData_tg(LocalDate data_tg) {
        this.data_tg = data_tg;
    }

    public LocalDate getData_feira() {
        return data_feira;
    }

    public void setData_feira(LocalDate data_feira) {
        this.data_feira = data_feira;
    }
}