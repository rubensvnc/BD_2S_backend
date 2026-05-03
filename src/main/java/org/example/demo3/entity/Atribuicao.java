package org.example.demo3.entity;

public class Atribuicao {
    private Integer id_atribuicao;
    private Integer usuario_id;
    private Integer disciplina_id;
    private Integer semestre_id;
    private String dia_semana;

    public Atribuicao(Integer id_atribuicao, Integer usuario_id, Integer disciplina_id,
                      Integer semestre_id, String dia_semana) {
        this.id_atribuicao = id_atribuicao;
        this.usuario_id = usuario_id;
        this.disciplina_id = disciplina_id;
        this.semestre_id = semestre_id;
        this.dia_semana = dia_semana;
    }

    public Integer getId_atribuicao() {
        return id_atribuicao;
    }

    public Integer getUsuario_id() {
        return usuario_id;
    }

    public Integer getDisciplina_id() {
        return disciplina_id;
    }

    public Integer getSemestre_id() {
        return semestre_id;
    }

    public String getDia_semana() {
        return dia_semana;
    }
}
