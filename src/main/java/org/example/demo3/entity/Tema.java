package org.example.demo3.entity;

import javafx.beans.property.*;
import java.util.ArrayList;
import java.util.List;

public class Tema {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty disciplinaId = new SimpleIntegerProperty(); // Banco: disciplina_id
    private final StringProperty nome = new SimpleStringProperty();
    private final IntegerProperty prioridade = new SimpleIntegerProperty();
    private final IntegerProperty qtdMinAulas = new SimpleIntegerProperty(1);
    private final IntegerProperty qtdMaxAulas = new SimpleIntegerProperty(2);
    private final BooleanProperty ehAvaliacao = new SimpleBooleanProperty(false); // Banco: eh_avaliacao
    private final BooleanProperty ehOpcional = new SimpleBooleanProperty(false);   // Banco: eh_opcional

    private String nomeDisciplina; // Auxiliar para o JOIN na TableView

    //LISTA DE DEPENDENCIAS
    private List<Tema> dependencias = new ArrayList<>();

    public Tema() {}

    public Tema(int id, String nome) {
        setId(id);
        setNome(nome);
    }

    //GETTERS E SETTERS

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getDisciplinaId() { return disciplinaId.get(); }
    public void setDisciplinaId(int value) { disciplinaId.set(value); }
    public IntegerProperty disciplinaIdProperty() { return disciplinaId; }

    public String getNome() { return nome.get(); }
    public void setNome(String value) { nome.set(value); }
    public StringProperty nomeProperty() { return nome; }

    public int getPrioridade() { return prioridade.get(); }
    public void setPrioridade(int value) { prioridade.set(value); }
    public IntegerProperty prioridadeProperty() { return prioridade; }

    public int getQtdMinAulas() { return qtdMinAulas.get(); }
    public void setQtdMinAulas(int value) { qtdMinAulas.set(value); }
    public IntegerProperty qtdMinAulasProperty() { return qtdMinAulas; }

    public int getQtdMaxAulas() { return qtdMaxAulas.get(); }
    public void setQtdMaxAulas(int value) { qtdMaxAulas.set(value); }
    public IntegerProperty qtdMaxAulasProperty() { return qtdMaxAulas; }

    public boolean isEhAvaliacao() { return ehAvaliacao.get(); }
    public void setEhAvaliacao(boolean value) { ehAvaliacao.set(value); }
    public BooleanProperty ehAvaliacaoProperty() { return ehAvaliacao; }

    public boolean isEhOpcional() { return ehOpcional.get(); }
    public void setEhOpcional(boolean value) { ehOpcional.set(value); }
    public BooleanProperty ehOpcionalProperty() { return ehOpcional; }

    //DEPENDENCIAS

    public List<Tema> getDependencias() { return dependencias; }
    public void setDependencias(List<Tema> dependencias) { this.dependencias = dependencias; }

    public void adicionarDependencia(Tema tema) {
        if (tema != null && tema.getId() != this.getId() && !this.dependencias.contains(tema)) {
            this.dependencias.add(tema);
        }
    }

    //CAMPO AUXILIAR PARA EXIBIÇÃO NA TABELA
    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    @Override
    public String toString() { return getNome(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tema tema = (Tema) o;
        return getId() != 0 && getId() == tema.getId();
    }
}