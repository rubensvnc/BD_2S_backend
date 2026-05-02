package org.example.demo3.entity;

public class Curso {
    // Usamos Integer para os IDs para manter compatibilidade com DAOs que tratam nulos
    private Integer id;
    private Integer coordId;      // Corresponde a coord_id (FK)
    private String nome;
    private String turno;     // Corresponde ao ENUM 'MANHA'/'NOITE'
    private Integer qtdSemestres; // Corresponde a qtd_semestres
    private boolean ativo;

    // Construtor padrão (Necessário para muitos DAOs e Frameworks)
    public Curso() {}

    // Construtor completo da dev_wander
    public Curso(Integer id, Integer coordId, String nome, String turno, Integer qtdSemestres, boolean ativo) {
        this.id = id;
        this.coordId = coordId;
        this.nome = nome;
        this.turno = turno;
        this.qtdSemestres = qtdSemestres;
        this.ativo = ativo;
    }

    // --- GETTERS E SETTERS ---

    // Alias para compatibilidade com a develop (caso algum código use getId_curso)
    public Integer getId_curso() { return id; }
    public void setId_curso(Integer id_curso) { this.id = id_curso; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCoordId() { return coordId; }
    public void setCoordId(Integer coordId) { this.coordId = coordId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public Integer getQtdSemestres() { return qtdSemestres; }
    public void setQtdSemestres(Integer qtdSemestres) { this.qtdSemestres = qtdSemestres; }

    // Padronização do Getter de Booleano (getAtivo para compatibilidade)
    public boolean isAtivo() { return ativo; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    @Override
    public String toString() {
        return nome != null ? nome : "Curso sem nome";
    }
}