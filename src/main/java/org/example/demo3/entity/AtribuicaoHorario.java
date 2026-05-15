package org.example.demo3.entity;

public class AtribuicaoHorario {

    private Integer id_atribuicao_horario;
    private Integer atribuicao_professor_id;
    private Integer horario_curso_id;
    private String dia_semana;

    public AtribuicaoHorario() {

    }

    public AtribuicaoHorario(Integer id_atribuicao_horario,
                             Integer atribuicao_professor_id,
                             Integer horario_curso_id,
                             String dia_semana) {
        this.id_atribuicao_horario  = id_atribuicao_horario;
        this.atribuicao_professor_id = atribuicao_professor_id;
        this.horario_curso_id       = horario_curso_id;
        this.dia_semana             = dia_semana;
    }

    public Integer getId_atribuicao_horario() {
        return id_atribuicao_horario;
    }

    public void setId_atribuicao_horario(Integer id_atribuicao_horario) {
        this.id_atribuicao_horario = id_atribuicao_horario;
    }

    public Integer getAtribuicao_professor_id() {
        return atribuicao_professor_id;
    }

    public void setAtribuicao_professor_id(Integer atribuicao_professor_id) {
        this.atribuicao_professor_id = atribuicao_professor_id;
    }

    public Integer getHorario_curso_id() {
        return horario_curso_id;
    }

    public void setHorario_curso_id(Integer horario_curso_id) {
        this.horario_curso_id = horario_curso_id;
    }

    public String getDia_semana() {
        return dia_semana;
    }

    public void setDia_semana(String dia_semana) {
        this.dia_semana = dia_semana;
    }
}