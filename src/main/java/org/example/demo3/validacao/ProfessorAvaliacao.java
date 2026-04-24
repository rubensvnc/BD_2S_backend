package main.java.org.example.demo3.validacao;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class ProfessorAvaliacao {

    private String nome;
    private Set<LocalDate> datasIndisponiveis = new HashSet<>();

    public Set<LocalDate> getDatasIndisponiveis() {
        return datasIndisponiveis;
    }

    public void adicionarDataIndisponivel(LocalDate data) {
        datasIndisponiveis.add(data);
    }
}