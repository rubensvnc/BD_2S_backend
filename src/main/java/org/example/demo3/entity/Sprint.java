package org.example.demo3.entity;

import java.time.LocalDate;

public class Sprint {

    private Integer id_sprint;
    private Integer semestre_letivo_id;
    private Integer numero;

    private LocalDate data_inicio;
    private LocalDate data_fim;
    private LocalDate data_review;

    public Sprint() {
    }

    public Sprint(Integer id_sprint,
                  Integer semestre_letivo_id,
                  Integer numero,
                  LocalDate data_inicio,
                  LocalDate data_fim,
                  LocalDate data_review) {

        this.id_sprint = id_sprint;
        this.semestre_letivo_id = semestre_letivo_id;
        this.numero = numero;
        this.data_inicio = data_inicio;
        this.data_fim = data_fim;
        this.data_review = data_review;
    }

    public Integer getId_sprint() {
        return id_sprint;
    }

    public void setId_sprint(Integer id_sprint) {
        this.id_sprint = id_sprint;
    }

    public Integer getSemestre_letivo_id() {
        return semestre_letivo_id;
    }

    public void setSemestre_letivo_id(Integer semestre_letivo_id) {
        this.semestre_letivo_id = semestre_letivo_id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
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

    public LocalDate getData_review() {
        return data_review;
    }

    public void setData_review(LocalDate data_review) {
        this.data_review = data_review;
    }
}



