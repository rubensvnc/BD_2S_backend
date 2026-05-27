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
}
