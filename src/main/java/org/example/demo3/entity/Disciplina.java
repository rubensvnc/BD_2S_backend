package org.example.demo3.entity;

import java.time.LocalDateTime;

public class Disciplina {

    private Integer id_disciplina;
    private Integer curso_id;
    private String nome;
    private Integer semestre_curso;
    private Integer carga_horaria_minima;
    private LocalDateTime deletado_em;

    public Disciplina() {
    }

    public Disciplina(Integer id_disciplina,
                      Integer curso_id,
                      String nome,
                      Integer semestre_curso,
                      Integer carga_horaria_minima,
                      LocalDateTime deletado_em) {

        this.id_disciplina = id_disciplina;
        this.curso_id = curso_id;
        this.nome = nome;
        this.semestre_curso = semestre_curso;
        this.carga_horaria_minima = carga_horaria_minima;
        this.deletado_em = deletado_em;
    }

    public Integer getId_disciplina() {
        return id_disciplina;
    }

    public void setId_disciplina(Integer id_disciplina) {
        this.id_disciplina = id_disciplina;
    }

    public Integer getCurso_id() {
        return curso_id;
    }

    public void setCurso_id(Integer curso_id) {
        this.curso_id = curso_id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getSemestre_curso() {
        return semestre_curso;
    }

    public void setSemestre_curso(Integer semestre_curso) {
        this.semestre_curso = semestre_curso;
    }

    public Integer getCarga_horaria_minima() {
        return carga_horaria_minima;
    }

    public void setCarga_horaria_minima(Integer carga_horaria_minima) {
        this.carga_horaria_minima = carga_horaria_minima;
    }

    public LocalDateTime getDeletado_em() {
        return deletado_em;
    }

    public void setDeletado_em(LocalDateTime deletado_em) {
        this.deletado_em = deletado_em;
    }


}

