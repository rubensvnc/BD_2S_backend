package org.example.demo3.entity;

public class AtribuicaoHorario {

    private Integer id_atribuicao_horario;
    private Integer atribuicao_id;
    private Integer horario_curso_id;
    private Integer dia_semana;

    public AtribuicaoHorario() {

    }

    public AtribuicaoHorario(Integer id_atribuicao_horario,
                             Integer atribuicao_id,
                             Integer horario_curso_id,
                             Integer dia_semana) {
        this.id_atribuicao_horario  = id_atribuicao_horario;
        this.atribuicao_id = atribuicao_id;
        this.horario_curso_id       = horario_curso_id;
        this.dia_semana             = dia_semana;
    }

    public Integer getId_atribuicao_horario() {
        return id_atribuicao_horario;
    }

    public void setId_atribuicao_horario(Integer id_atribuicao_horario) {
        this.id_atribuicao_horario = id_atribuicao_horario;
    }

    public Integer getAtribuicao_id() {
        return atribuicao_id;
    }

    public void setAtribuicao_id(Integer atribuicao_id) {
        this.atribuicao_id = atribuicao_id;
    }

    public Integer getHorario_curso_id() {
        return horario_curso_id;
    }

    public void setHorario_curso_id(Integer horario_curso_id) {
        this.horario_curso_id = horario_curso_id;
    }

    public Integer getDia_semana() {
        return dia_semana;
    }

    public void setDia_semana(Integer dia_semana) {
        this.dia_semana = dia_semana;
    }
}