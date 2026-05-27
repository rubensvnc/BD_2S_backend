package org.example.demo3.dto;

public class AdmCursoExibicao {
    private String nome;
    private String turno;
    private Integer qtd_semestres;
    private String nome_coord;

    public AdmCursoExibicao(String nome, String turno, Integer qtd_semestres, String nome_coord) {
        this.nome = nome;
        this.turno = turno;
        this.qtd_semestres = qtd_semestres;
        this.nome_coord = nome_coord;
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

    public String getNome_coord() {
        return nome_coord;
    }

    public void setNome_coord(String nome_coord) {
        this.nome_coord = nome_coord;
    }
}
