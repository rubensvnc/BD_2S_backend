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

    public CronogramaItem(Integer id_item, Integer cronograma_id, Integer tema_id,
                          Integer id_data_cancelada, LocalDate data_prevista,
                          Integer qtd_aulas, String status_aula) {
        this.id_item = id_item;
        this.cronograma_id = cronograma_id;
        this.tema_id = tema_id;
        this.id_data_cancelada = id_data_cancelada;
        this.data_prevista = data_prevista;
        this.qtd_aulas = qtd_aulas;
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
