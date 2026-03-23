package org.example.demo3;


public class PlanejamentoDia {
    private Integer id;
    private String data;
    private String disciplina;
    private String tema;
    private String obs;
    private Integer aulas;
    private String dia_semana;

    public PlanejamentoDia(Integer id, String data, String disciplina, String tema, String obs, Integer aulas, String dia_semana) {
        this.id = id;
        this.data = data;
        this.disciplina = disciplina;
        this.tema = tema;
        this.obs = obs;
        this.aulas = aulas;
        this.dia_semana = dia_semana;
    }

    public Integer getId() {
        return id;
    }

    public String getData() {
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
