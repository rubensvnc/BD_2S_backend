package org.example.demo3.entity;

import java.time.LocalDate;

public class CronogramaItem {
    private Integer id_item;
    private Integer cronograma_id;
    private Integer tema_id;
    private Integer id_data_cancelada;
    private LocalDate data_prevista;
    private Integer qtd_aulas;
    private String status_aula;

    public void setId_item(Integer id_item) {
        this.id_item = id_item;
    }

    public void setCronograma_id(Integer cronograma_id) {
        this.cronograma_id = cronograma_id;
    }

    public void setTema_id(Integer tema_id) {
        this.tema_id = tema_id;
    }

    public void setId_data_cancelada(Integer id_data_cancelada) {
        this.id_data_cancelada = id_data_cancelada;
    }

    public void setData_prevista(LocalDate data_prevista) {
        this.data_prevista = data_prevista;
    }

    public void setQtd_aulas(Integer qtd_aulas) {
        this.qtd_aulas = qtd_aulas;
    }

    public void setStatus_aula(String status_aula) {
        this.status_aula = status_aula;
    }

    public Integer getId_item() {
        return id_item;
    }

    public Integer getCronograma_id() {
        return cronograma_id;
    }

    public Integer getTema_id() {
        return tema_id;
    }

    public Integer getId_data_cancelada() {
        return id_data_cancelada;
    }

    public LocalDate getData_prevista() {
        return data_prevista;
    }

    public Integer getQtd_aulas() {
        return qtd_aulas;
    }

    public String getStatus_aula() {
        return status_aula;
    }
}
