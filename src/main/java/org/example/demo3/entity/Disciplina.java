package org.example.demo3.entity;

public class Disciplina {
    private Integer id;
    private Integer cursoId;
    private String nome;
    private Integer cargaHoraria;
    private Integer semestre;
    private Boolean ativo;

    // Construtor vazio (Essencial para compatibilidade)
    public Disciplina() {}

    // Construtor da dev_wander (ajustado para Integer)
    public Disciplina(Integer id, Integer cursoId, String nome, Integer cargaHoraria, Integer semestre) {
        this.id = id;
        this.cursoId = cursoId;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.semestre = semestre;
        this.ativo = true; // Valor padrão
    }

    // --- GETTERS E SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    // Alias para compatibilidade com Long e nomes da develop
    public Long getId_disciplina() { return id != null ? id.longValue() : null; }
    public void setId_disciplina(Long id_disciplina) { this.id = id_disciplina != null ? id_disciplina.intValue() : null; }

    public Integer getCursoId() { return cursoId; }
    public void setCursoId(Integer cursoId) { this.cursoId = cursoId; }

    public Long getId_curso() { return cursoId != null ? cursoId.longValue() : null; }
    public void setId_curso(Long id_curso) { this.cursoId = id_curso != null ? id_curso.intValue() : null; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getCargaHoraria() { return cargaHoraria; }
    public void setCargaHoraria(Integer cargaHoraria) { this.cargaHoraria = cargaHoraria; }

    // Alias para carga horária
    public Integer getCarga_horaria_semestral() { return cargaHoraria; }
    public void setCarga_horaria_semestral(Integer ch) { this.cargaHoraria = ch; }

    public Integer getSemestre() { return semestre; }
    public void setSemestre(Integer semestre) { this.semestre = semestre; }

    // Alias para semestre da grade
    public Integer getSemestre_grade() { return semestre; }
    public void setSemestre_grade(Integer sg) { this.semestre = sg; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() {
        return nome != null ? nome : "";
    }
}