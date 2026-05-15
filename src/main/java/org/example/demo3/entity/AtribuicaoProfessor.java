package org.example.demo3.entity;

import java.time.LocalDate;

public class AtribuicaoProfessor {

    private Integer id_atribuicao;
    private Integer professor_id;
    private Integer disciplina_id;
    private Integer semestre_letivo_id;
    private LocalDate criado_em;
    private LocalDate deleted_at;

    public AtribuicaoProfessor() {

    }

    public AtribuicaoProfessor(Integer id_atribuicao,
                               Integer professor_id,
                               Integer disciplina_id,
                               Integer semestre_letivo_id,
                               LocalDate criado_em,
                               LocalDate deleted_at) {
        this.id_atribuicao      = id_atribuicao;
        this.professor_id       = professor_id;
        this.disciplina_id      = disciplina_id;
        this.semestre_letivo_id = semestre_letivo_id;
        this.criado_em          = criado_em;
        this.deleted_at         = deleted_at;
    }

    public Integer getId_atribuicao() {
        return id_atribuicao;
    }

    public void setId_atribuicao(Integer id_atribuicao) {
        this.id_atribuicao = id_atribuicao;
    }

    public Integer getProfessor_id() {
        return professor_id;
    }

    public void setProfessor_id(Integer professor_id) {
        this.professor_id = professor_id;
    }

    public Integer getDisciplina_id() {
        return disciplina_id;
    }

    public void setDisciplina_id(Integer disciplina_id) {
        this.disciplina_id = disciplina_id;
    }

    public Integer getSemestre_letivo_id() {
        return semestre_letivo_id;
    }

    public void setSemestre_letivo_id(Integer semestre_letivo_id) {
        this.semestre_letivo_id = semestre_letivo_id;
    }

    public LocalDate getCriado_em() {
        return criado_em;
    }

    public void setCriado_em(LocalDate criado_em) {
        this.criado_em = criado_em;
    }

    public LocalDate getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(LocalDate deleted_at) {
        this.deleted_at = deleted_at;
    }


    public boolean isAtiva() {
        return deleted_at == null;
    }
}