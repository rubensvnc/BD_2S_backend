package org.example.demo3;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class UsuarioAtual {
    private static UsuarioAtual instancia;

    // Propriedades do Usuário
    private final ObjectProperty<Integer> id_usuario = new SimpleObjectProperty<>(null);
    private final StringProperty tipo = new SimpleStringProperty(null);

    // Contexto Global
    private final ObjectProperty<Integer> ano = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> anoSemestre = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> id_curso = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> semestreCurso = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Integer> id_disciplina = new SimpleObjectProperty<>(null);

    private UsuarioAtual() {}

    public static UsuarioAtual getInstancia() {
        if (instancia == null) {
            instancia = new UsuarioAtual();
        }
        return instancia;
    }

    public void limparSessao() {
        setId_usuario(null);
        setTipo(null);
        setAno(null);
        setAnoSemestre(null);
        setIdCurso(null);
        setSemestreCurso(null);
        setIdDisciplina(null);
    }

    public void usuarioAdm(){
        setIdCurso(null);
        setSemestreCurso(null);
        setIdDisciplina(null);
    }

    public void usuarioCoord(){
        setSemestreCurso(null);
        setIdDisciplina(null);
    }

    public ObjectProperty<Integer> idDisciplinaProperty() { return id_disciplina; }
    public ObjectProperty<Integer> anoSemestreProperty() { return anoSemestre; }
    public ObjectProperty<Integer> idCursoProperty() { return id_curso; }
    public ObjectProperty<Integer> semestreCursoProperty() { return semestreCurso; }
    public ObjectProperty<Integer> anoProperty() { return ano; }

    public Integer getId_usuario() { return id_usuario.get(); }
    public void setId_usuario(Integer id) { this.id_usuario.set(id); }

    public String getTipo() { return tipo.get(); }
    public void setTipo(String tipo) { this.tipo.set(tipo); }

    public Integer getAno() { return ano.get(); }
    public void setAno(Integer ano) { this.ano.set(ano); }

    public Integer getAnoSemestre() { return anoSemestre.get(); }
    public void setAnoSemestre(Integer anoSemestre) { this.anoSemestre.set(anoSemestre); }

    public Integer getIdCurso() { return id_curso.get(); }
    public void setIdCurso(Integer idCurso) { this.id_curso.set(idCurso); }

    public Integer getSemestreCurso() { return semestreCurso.get(); }
    public void setSemestreCurso(Integer semestreCurso) { this.semestreCurso.set(semestreCurso); }

    public Integer getIdDisciplina() { return id_disciplina.get(); }
    public void setIdDisciplina(Integer idDisciplina) {
        this.id_disciplina.set(idDisciplina);
    }
}