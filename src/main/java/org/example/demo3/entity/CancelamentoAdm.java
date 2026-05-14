package org.example.demo3.entity;


import java.time.LocalDate;

public class CancelamentoAdm{
    private Integer id_cancelamento_adm;
    private Integer adm_id;
    private Integer semestre_letivo_id;
    private LocalDate data;
    private String turno;
    private Boolean dia_inteiro;
    private String motivo;
    private LocalDate criado_em;
    private LocalDate deletado_em;

    public CancelamentoAdm(){};

    public CancelamentoAdm(Integer id_cancelamento_adm, Integer adm_id, Integer semestre_letivo_id,
                           LocalDate data, String turno, Boolean dia_inteiro, String motivo,
                           LocalDate criado_em, LocalDate deletado_em) {
        this.id_cancelamento_adm = id_cancelamento_adm;
        this.adm_id = adm_id;
        this.semestre_letivo_id = semestre_letivo_id;
        this.data = data;
        this.turno = turno;
        this.dia_inteiro = dia_inteiro;
        this.motivo = motivo;
        this.criado_em = criado_em;
        this.deletado_em = deletado_em;
    }

    public Integer getId_cancelamento_adm() {
        return id_cancelamento_adm;
    }

    public void setId_cancelamento_adm(Integer id_cancelamento_adm) {
        this.id_cancelamento_adm = id_cancelamento_adm;
    }

    public Integer getAdm_id() {
        return adm_id;
    }

    public void setAdm_id(Integer adm_id) {
        this.adm_id = adm_id;
    }

    public Integer getSemestre_letivo_id() {
        return semestre_letivo_id;
    }

    public void setSemestre_letivo_id(Integer semestre_letivo_id) {
        this.semestre_letivo_id = semestre_letivo_id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public Boolean getDia_inteiro() {
        return dia_inteiro;
    }

    public void setDia_inteiro(Boolean dia_inteiro) {
        this.dia_inteiro = dia_inteiro;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDate getCriado_em() {
        return criado_em;
    }

    public void setCriado_em(LocalDate criado_em) {
        this.criado_em = criado_em;
    }

    public LocalDate getDeletado_em() {
        return deletado_em;
    }

    public void setDeletado_em(LocalDate deletado_em) {
        this.deletado_em = deletado_em;
    }
}