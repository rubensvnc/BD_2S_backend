package org.example.demo3.entity;

import org.example.demo3.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Planejamento {

    private Integer id_planejamento;
    private Integer atribuicao_professor_id;
    private LocalDateTime gerado_em;

    public Planejamento() {
    }

    public Planejamento(Integer id_planejamento, Integer atribuicao_professor_id, LocalDateTime gerado_em) {
        this.id_planejamento = id_planejamento;
        this.atribuicao_professor_id = atribuicao_professor_id;
        this.gerado_em = gerado_em;
    }

    // Getters e Setters
    public Integer getId_planejamento() { return id_planejamento; }
    public void setId_planejamento(Integer id_planejamento) { this.id_planejamento = id_planejamento; }

    public Integer getAtribuicao_professor_id() { return atribuicao_professor_id; }
    public void setAtribuicao_professor_id(Integer atribuicao_professor_id) { this.atribuicao_professor_id = atribuicao_professor_id; }

    public LocalDateTime getGerado_em() { return gerado_em; }
    public void setGerado_em(LocalDateTime gerado_em) { this.gerado_em = gerado_em; }

}