package org.example.demo3.entity;

public class Curso {
    private Integer id_curso;
    private String nome;
    private Boolean ativo;

    @Override
    public String toString() {
        return this.nome; // Agora o ComboBox vai ler o nome do curso
    }

    public Integer getId_curso() {
        return id_curso;
    }

    public void setId_curso(Integer id_curso) {
        this.id_curso = id_curso;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
