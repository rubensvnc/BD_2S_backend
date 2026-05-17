package org.example.demo3;

import java.time.LocalDate;

public class SlotPlanejamento {

    private Integer id_slot_planejamento;
    private Integer planejamento_id;
    private LocalDate data;
    private Integer horario_curso_id;
    private Integer tema_id;
    private String status;
    private String motivo_cancelamento;
    private Integer cancelamento_adm_id;


    public SlotPlanejamento() {
    }


    public SlotPlanejamento(Integer id_slot_planejamento, Integer planejamento_id, LocalDate data,
                            Integer horario_curso_id, Integer tema_id, String status,
                            String motivo_cancelamento, Integer cancelamento_adm_id) {
        this.id_slot_planejamento = id_slot_planejamento;
        this.planejamento_id = planejamento_id;
        this.data = data;
        this.horario_curso_id = horario_curso_id;
        this.tema_id = tema_id;
        this.status = status;
        this.motivo_cancelamento = motivo_cancelamento;
        this.cancelamento_adm_id = cancelamento_adm_id;
    }


    public Integer getId_slot_planejamento() {
        return id_slot_planejamento;
    }

    public void setId_slot_planejamento(Integer id_slot_planejamento) {
        this.id_slot_planejamento = id_slot_planejamento;
    }

    public Integer getPlanejamento_id() {
        return planejamento_id;
    }

    public void setPlanejamento_id(Integer planejamento_id) {
        this.planejamento_id = planejamento_id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Integer getHorario_curso_id() {
        return horario_curso_id;
    }

    public void setHorario_curso_id(Integer horario_curso_id) {
        this.horario_curso_id = horario_curso_id;
    }

    public Integer getTema_id() {
        return tema_id;
    }

    public void setTema_id(Integer tema_id) {
        this.tema_id = tema_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMotivo_cancelamento() {
        return motivo_cancelamento;
    }

    public void setMotivo_cancelamento(String motivo_cancelamento) {
        this.motivo_cancelamento = motivo_cancelamento;
    }

    public Integer getCancelamento_adm_id() {
        return cancelamento_adm_id;
    }

    public void setCancelamento_adm_id(Integer cancelamento_adm_id) {
        this.cancelamento_adm_id = cancelamento_adm_id;
    }
}