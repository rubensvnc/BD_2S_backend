package org.example.demo3.entity;

public class Tema {
    private Long id_tema;
    private Long id_disciplina;
    private String nome;
    private Integer qtd_min_aulas;
    private Integer qtd_max_aulas;
    private Boolean is_avaliacao;
    private Integer prioridade;
    private Boolean opcional;
    private Boolean ativo;

    public Long getId_tema() {
        return id_tema;
    }

    public void setId_tema(Long id_tema) {
        this.id_tema = id_tema;
    }

    public Long getId_disciplina() {
        return id_disciplina;
    }

    public void setId_disciplina(Long id_disciplina) {
        this.id_disciplina = id_disciplina;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public Boolean getIs_avaliacao() {
        return is_avaliacao;
    }

    public void setIs_avaliacao(Boolean is_avaliacao) {
        this.is_avaliacao = is_avaliacao;
    }

    public Integer getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Integer prioridade) {
        this.prioridade = prioridade;
    }

    public Boolean getOpcional() {
        return opcional;
    }

    public void setOpcional(Boolean opcional) {
        this.opcional = opcional;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
