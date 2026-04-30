package org.example.demo3.entity;

import java.time.LocalDate;

public class CronogramaFinal {
    private Long id_item;
    private Long id_atribuicao;
    private LocalDate data_aula;
    private Long id_tema;
    private String status;
    private String motivo_cancelamento;
    private Boolean is_sabado_letivo;

    public CronogramaFinal(Long id_item, Long id_atribuicao, LocalDate data_aula, Long id_tema, String status, String motivo_cancelamento, Boolean is_sabado_letivo) {
        this.id_item = id_item;
        this.id_atribuicao = id_atribuicao;
        this.data_aula = data_aula;
        this.id_tema = id_tema;
        this.status = status;
        this.motivo_cancelamento = motivo_cancelamento;
        this.is_sabado_letivo = is_sabado_letivo;
    }

    public Long getId_item() {
        return id_item;
    }

    public Long getId_atribuicao() {
        return id_atribuicao;
    }

    public LocalDate getData_aula() {
        return data_aula;
    }

    public Long getId_tema() {
        return id_tema;
    }

    public String getStatus() {
        return status;
    }

    public String getMotivo_cancelamento() {
        return motivo_cancelamento;
    }

    public Boolean getIs_sabado_letivo() {
        return is_sabado_letivo;
    }
}
