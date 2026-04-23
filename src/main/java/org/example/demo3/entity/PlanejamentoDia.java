package org.example.demo3.entity;

import java.time.LocalDate;

public class PlanejamentoDia {
    private Integer id;
    private LocalDate data;
    private String disciplina;
    private String tema;
    private String obs;
    private Integer aulas;
    private String dia_semana;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public void setAulas(Integer aulas) {
        this.aulas = aulas;
    }

    public void setDia_semana(String dia_semana) {
        this.dia_semana = dia_semana;
    }

    public Integer getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public String getTema() {
        return tema;
    }

    public String getObs() {
        return obs;
    }

    public Integer getAulas() {
        return aulas;
    }

    public String getDia_semana() {
        return dia_semana;
    }
}
