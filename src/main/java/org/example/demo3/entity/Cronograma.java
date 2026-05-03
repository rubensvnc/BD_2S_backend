package org.example.demo3.entity;

public class Cronograma {
    private Integer id_cronograma;
    private Integer usuario_id;
    private Integer curso_id;
    private Integer semestre_id;
    private Integer grade_semestre;

    public Cronograma(){

    }

    public Cronograma(Integer id_cronograma, Integer usuario_id, Integer curso_id,
                      Integer semestre_id, Integer grade_semestre) {
        this.id_cronograma = id_cronograma;
        this.usuario_id = usuario_id;
        this.curso_id = curso_id;
        this.semestre_id = semestre_id;
        this.grade_semestre = grade_semestre;
    }

    public void setId_cronograma(Integer id_cronograma) {
        this.id_cronograma = id_cronograma;
    }

    public void setUsuario_id(Integer usuario_id) {
        this.usuario_id = usuario_id;
    }

    public void setCurso_id(Integer curso_id) {
        this.curso_id = curso_id;
    }

    public void setSemestre_id(Integer semestre_id) {
        this.semestre_id = semestre_id;
    }

    public void setGrade_semestre(Integer grade_semestre) {
        this.grade_semestre = grade_semestre;
    }

    public Integer getId_cronograma() {
        return id_cronograma;
    }

    public Integer getUsuario_id() {
        return usuario_id;
    }

    public Integer getCurso_id() {
        return curso_id;
    }

    public Integer getSemestre_id() {
        return semestre_id;
    }

    public Integer getGrade_semestre() {
        return grade_semestre;
    }
}
