package org.example.demo3.entity;

public class Disciplina {
    private Long id_disciplina;
    private Long id_curso;
    private String nome;
    private Integer carga_horaria_semestral;
    private Integer semestre_grade;
    private Boolean ativo;

    public Long getId_disciplina() {
        return id_disciplina;
    }

    public void setId_disciplina(Long id_disciplina) {
        this.id_disciplina = id_disciplina;
    }

    public Long getId_curso() {
        return id_curso;
    }

    public void setId_curso(Long id_curso) {
        this.id_curso = id_curso;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getCarga_horaria_semestral() {
        return carga_horaria_semestral;
    }

    public void setCarga_horaria_semestral(Integer carga_horaria_semestral) {
        this.carga_horaria_semestral = carga_horaria_semestral;
    }

    public Integer getSemestre_grade() {
        return semestre_grade;
    }

    public void setSemestre_grade(Integer semestre_grade) {
        this.semestre_grade = semestre_grade;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
