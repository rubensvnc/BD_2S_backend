package org.example.demo3.entity;


public class CancelamentoAdmHorario{
    private Integer id_cancelamento_adm_horario;
    private Integer cancelamento_adm_id;
    private Integer horario_curso_id;

    public CancelamentoAdmHorario(){};

    public CancelamentoAdmHorario(Integer id_cancelamento_adm_horario,
                                  Integer cancelamento_adm_id, Integer horario_curso_id) {
        this.id_cancelamento_adm_horario = id_cancelamento_adm_horario;
        this.cancelamento_adm_id = cancelamento_adm_id;
        this.horario_curso_id = horario_curso_id;
    }

    public Integer getId_cancelamento_adm_horario() {
        return id_cancelamento_adm_horario;
    }

    public void setId_cancelamento_adm_horario(Integer id_cancelamento_adm_horario) {
        this.id_cancelamento_adm_horario = id_cancelamento_adm_horario;
    }

    public Integer getCancelamento_adm_id() {
        return cancelamento_adm_id;
    }

    public void setCancelamento_adm_id(Integer cancelamento_adm_id) {
        this.cancelamento_adm_id = cancelamento_adm_id;
    }

    public Integer getHorario_curso_id() {
        return horario_curso_id;
    }

    public void setHorario_curso_id(Integer horario_curso_id) {
        this.horario_curso_id = horario_curso_id;
    }
}