package org.example.demo3.entity;

import java.time.LocalDateTime;

public class Curso {

    private Integer id_curso;
    private Integer coordenador_id;
    private String nome;
    private String turno;
    private Integer qtd_semestres;
    private LocalDateTime deletado_em;

    public Curso() {
    }

    public Curso(Integer id_curso,
                 Integer coordenador_id,
                 String nome,
                 String turno,
                 Integer qtd_semestres,
                 LocalDateTime deletado_em) {

        this.id_curso = id_curso;
        this.coordenador_id = coordenador_id;
        this.nome = nome;
        this.turno = turno;
        this.qtd_semestres = qtd_semestres;
        this.deletado_em = deletado_em;
    }

    public Integer getId_curso() {
        return id_curso;
    }

    public void setId_curso(Integer id_curso) {
        this.id_curso = id_curso;
    }

    public Integer getCoordenador_id() {
        return coordenador_id;
    }

    public void setCoordenador_id(Integer coordenador_id) {
        this.coordenador_id = coordenador_id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public Integer getQtd_semestres() {
        return qtd_semestres;
    }

    public void setQtd_semestres(Integer qtd_semestres) {
        this.qtd_semestres = qtd_semestres;
    }

    public LocalDateTime getDeletado_em() {
        return deletado_em;
    }

    public void setDeletado_em(LocalDateTime deletado_em) {
        this.deletado_em = deletado_em;
    }


}

