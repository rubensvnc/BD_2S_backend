package org.example.demo3.entity;

import java.time.LocalDate;

public class SemestreLetivo {
    private int id;
    private int ano;
    private String numeroSemestre; // Corresponde ao ENUM '1' ou '2'
    private LocalDate dataInicio;
    private LocalDate dataFim;

    //CONSTRUTOR
    public SemestreLetivo(int id, int ano, String numeroSemestre, LocalDate dataInicio, LocalDate dataFim) {
        this.id = id;
        this.ano = ano;
        this.numeroSemestre = numeroSemestre;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
    }

    //GETTERS
    public int getId() { return id; }
    public int getAno() { return ano; }
    public String getNumeroSemestre() { return numeroSemestre; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }

    //SETTERS
    public void setId(int id) { this.id = id; }
    public void setAno(int id) { this.ano = ano; }
    public void setNumeroSemestre(String numeroSemestre) { this.numeroSemestre = numeroSemestre; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    /**
     * Mantemos a lógica do "rótulo" no toString para que o ComboBox
     * continue exibindo algo como "2026/1"
     */
    @Override
    public String toString() {
        return ano + "/" + numeroSemestre;
    }
}