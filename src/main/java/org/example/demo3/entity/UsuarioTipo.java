package org.example.demo3.entity;

public class UsuarioTipo {
    private Integer usuario_id;
    private String tipo; // Representa o ENUM ('ADM', 'COORD', 'PROF')

    public UsuarioTipo() {
    }

    public UsuarioTipo(Integer usuario_id, String tipo) {
        this.usuario_id = usuario_id;
        this.tipo = tipo;
    }

    // Getters e Setters
    public Integer getUsuario_id() {
        return usuario_id;
    }

    public void setUsuario_id(Integer usuario_id) {
        this.usuario_id = usuario_id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}