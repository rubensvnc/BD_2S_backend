/*
CREATE TABLE template_horario_turno (
        id_template INT AUTO_INCREMENT,
        turno ENUM('manha','noite') NOT NULL,
tipo ENUM('aula','intervalo') NOT NULL,
numero_ordem TINYINT NOT NULL, -- ex: 1 = aula1, 2 = intervalo, 3 = aula2
hora_inicio TIME NOT NULL,
hora_fim TIME NOT NULL,

PRIMARY KEY(id_template),
UNIQUE KEY uq_template (turno, numero_ordem)
);*/
import java.time.LocalTime;


public class TemplateHorarioTurno {

    private Integer id_template;
    private String turno;
    private String tipo;
    private Integer numero_ordem;
    private LocalTime hora_inicio;
    private LocalTime hora_fim;

    public TemplateHorarioTurno() {
    }

    public TemplateHorarioTurno(Integer id_template, String turno, String tipo, Integer numero_ordem, LocalTime hora_inicio, LocalTime hora_fim) {
        this.id_template = id_template;
        this.turno = turno;
        this.tipo = tipo;
        this.numero_ordem = numero_ordem;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
    }

    public Integer getId_template() {
        return id_template;
    }

    public void setId_template(Integer id_template) {
        this.id_template = id_template;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getNumero_ordem() {
        return numero_ordem;
    }

    public void setNumero_ordem(Integer numero_ordem) {
        this.numero_ordem = numero_ordem;
    }

    public LocalTime getHora_inicio() {
        return hora_inicio;
    }

    public void setHora_inicio(LocalTime hora_inicio) {
        this.hora_inicio = hora_inicio;
    }

    public LocalTime getHora_fim() {
        return hora_fim;
    }

    public void setHora_fim(LocalTime hora_fim) {
        this.hora_fim = hora_fim;
    }
}

