package org.example.demo3.entity;

import java.time.LocalDate;

public class Usuario {
    private Integer id_usuario;
    private String nome;
    private String email;
    private String senha_hash;
    private LocalDate criado_em;
    private LocalDate deleted_at;

    public Usuario() {
    }

    public Usuario(Integer id_usuario, String nome, String email, String senha_hash,
                   LocalDate criado_em, LocalDate deleted_at) {
        this.id_usuario = id_usuario;
        this.nome = nome;
        this.email = email;
        this.senha_hash = senha_hash;
        this.criado_em = criado_em;
        this.deleted_at = deleted_at;
    }

    // Getters e Setters
    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha_hash() {
        return senha_hash;
    }

    public void setSenha_hash(String senha_hash) {
        this.senha_hash = senha_hash;
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
}