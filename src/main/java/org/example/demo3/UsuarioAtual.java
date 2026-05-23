package org.example.demo3;

public class UsuarioAtual {
    private static UsuarioAtual instancia;
    private Integer id_usuario;
    private String tipo;

    //CONTEXTO GLOBAL
    private Integer ano;
    private Integer anoSemestre;
    private String curso;
    private Integer semestreCurso;
    private String disciplina;

    private UsuarioAtual() {}

    public static UsuarioAtual getInstancia() {
        if (instancia == null) {
            instancia = new UsuarioAtual();
        }
        return instancia;
    }

    public void usuarioAdm(){
        this.curso = null;
        this.semestreCurso = null;
        this.disciplina = null;
    }

    public void usuarioCoord(){
        this.semestreCurso = null;
        this.disciplina = null;
    }

    public void limparSessao() {
        this.id_usuario = null;
        this.tipo = null;
        this.ano = null;
        this.anoSemestre = null;
        this.curso = null;
        this.semestreCurso = null;
        this.disciplina = null;
    }

    public static void setInstancia(UsuarioAtual instancia) {
        UsuarioAtual.instancia = instancia;
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

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getAnoSemestre() {
        return anoSemestre;
    }

    public void setAnoSemestre(Integer anoSemestre) {
        this.anoSemestre = anoSemestre;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Integer getSemestreCurso() {
        return semestreCurso;
    }

    public void setSemestreCurso(Integer semestreCurso) {
        this.semestreCurso = semestreCurso;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }
}
