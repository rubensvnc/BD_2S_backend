package org.example.demo3.entity;

import java.time.LocalDate;

public class Tema {
    private Integer id_tema;
    private Integer disciplina_id;
    private Integer semestre_letivo_id;
    private String nome;
    private Integer eh_avaliacao;
    private Integer qtd_min_aulas;
    private Integer qtd_max_aulas;
    private Integer prioridade;
    private Integer eh_opcional;
    private LocalDate deletado_em;

    public Tema() {
    }

    public Tema(Integer id_tema, Integer disciplina_id, Integer semestre_letivo_id, String nome,
                Integer eh_avaliacao, Integer qtd_min_aulas, Integer qtd_max_aulas,
                Integer prioridade, Integer eh_opcional, LocalDate deletado_em) {
        this.id_tema = id_tema;
        this.disciplina_id = disciplina_id;
        this.semestre_letivo_id = semestre_letivo_id;
        this.nome = nome;
        this.eh_avaliacao = eh_avaliacao;
        this.qtd_min_aulas = qtd_min_aulas;
        this.qtd_max_aulas = qtd_max_aulas;
        this.prioridade = prioridade;
        this.eh_opcional = eh_opcional;
        this.deletado_em = deletado_em;
    }

    // Getters e Setters
    public Integer getId_tema() {
        return id_tema;
    }

    public void setId_tema(Integer id_tema) {
        this.id_tema = id_tema;
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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getEh_avaliacao() {
        return eh_avaliacao;
    }

    public void setEh_avaliacao(Integer eh_avaliacao) {
        this.eh_avaliacao = eh_avaliacao;
    }

    public Integer getQtd_min_aulas() {
        return qtd_min_aulas;
    }

    public void setQtd_min_aulas(Integer qtd_min_aulas) {
        this.qtd_min_aulas = qtd_min_aulas;
    }

    public Integer getQtd_max_aulas() {
        return qtd_max_aulas;
    }

    public void setQtd_max_aulas(Integer qtd_max_aulas) {
        this.qtd_max_aulas = qtd_max_aulas;
    }

    public Integer getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Integer prioridade) {
        this.prioridade = prioridade;
    }

    public Integer getEh_opcional() {
        return eh_opcional;
    }

    public void setEh_opcional(Integer eh_opcional) {
        this.eh_opcional = eh_opcional;
    }

    public LocalDate getDeletado_em() {
        return deletado_em;
    }

    public void setDeletado_em(LocalDate deletado_em) {
        this.deletado_em = deletado_em;
    }
}