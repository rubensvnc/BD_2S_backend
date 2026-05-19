package org.example.demo3;

public class UsuarioAtual {
    private static UsuarioAtual instancia;
    private Integer id_usuario;
    private String tipo;

    private UsuarioAtual() {}

    public static UsuarioAtual getInstancia() {
        if (instancia == null) {
            instancia = new UsuarioAtual();
        }
        return instancia;
    }

    // Métodos para limpar a sessão no Logout
    public void limparSessao() {
        this.id_usuario = null;
        this.tipo = null;
    }

    public Integer getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
