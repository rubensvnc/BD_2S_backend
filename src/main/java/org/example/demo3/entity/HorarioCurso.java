package org.example.demo3.entity;

import java.time.LocalTime;

public class HorarioCurso {

    private Integer id_horario_curso;
    private Integer curso_id;
    private Integer semestre_letivo_id;
    private String tipo;         // 'aula' ou 'intervalo'
    private Integer numero_ordem;
    private LocalTime hora_inicio;
    private LocalTime hora_fim;

    public HorarioCurso() {
    }

    public HorarioCurso(Integer id_horario_curso, Integer curso_id, Integer semestre_letivo_id,
                        String tipo, Integer numero_ordem,
                        LocalTime hora_inicio, LocalTime hora_fim) {
        this.id_horario_curso = id_horario_curso;
        this.curso_id = curso_id;
        this.semestre_letivo_id = semestre_letivo_id;
        this.tipo = tipo;
        this.numero_ordem = numero_ordem;
        this.hora_inicio = hora_inicio;
        this.hora_fim = hora_fim;
    }

    // Getters e Setters
    public Integer getId_horario_curso() { return id_horario_curso; }
    public void setId_horario_curso(Integer id_horario_curso) { this.id_horario_curso = id_horario_curso; }

    public Integer getCurso_id() { return curso_id; }
    public void setCurso_id(Integer curso_id) { this.curso_id = curso_id; }

    public Integer getSemestre_letivo_id() { return semestre_letivo_id; }
    public void setSemestre_letivo_id(Integer semestre_letivo_id) { this.semestre_letivo_id = semestre_letivo_id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getNumero_ordem() { return numero_ordem; }
    public void setNumero_ordem(Integer numero_ordem) { this.numero_ordem = numero_ordem; }

    public LocalTime getHora_inicio() { return hora_inicio; }
    public void setHora_inicio(LocalTime hora_inicio) { this.hora_inicio = hora_inicio; }

    public LocalTime getHora_fim() { return hora_fim; }
    public void setHora_fim(LocalTime hora_fim) { this.hora_fim = hora_fim; }
}