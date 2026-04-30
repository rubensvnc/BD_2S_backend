package org.example.demo3.dto;

import java.util.Date;

public class CronogramaExibicaoDTO {
    private Date data;
    private String nomeDisciplina;
    private String nomeTema;
    private int qtdAulas;
    private String status;
    private String motivo;
    private boolean isAvaliacao;

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public String getNomeTema() {
        return nomeTema;
    }

    public void setNomeTema(String nomeTema) {
        this.nomeTema = nomeTema;
    }

    public int getQtdAulas() {
        return qtdAulas;
    }

    public void setQtdAulas(int qtdAulas) {
        this.qtdAulas = qtdAulas;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isAvaliacao() {
        return isAvaliacao;
    }

    public void setAvaliacao(boolean avaliacao) {
        isAvaliacao = avaliacao;
    }
}
