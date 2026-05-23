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

    /**
     * ADICIONADO: Calcula as métricas globais para alimentar o PieChart e os cards da interface.
     */
    public static Map<String, Object> obterEstatisticasGlobais(int ano, int semestreAno, String nomeCurso, String nomeDisciplina, Integer idProfessor) {
        Map<String, Object> metricas = new HashMap<>();

        String sql = """
            SELECT 
                COUNT(sp.id_slot_planejamento) AS total_aulas,
                SUM(CASE WHEN sp.status = 'ministrada' THEN 1 ELSE 0 END) AS ministradas,
                SUM(CASE WHEN sp.status = 'nao_ministrada' THEN 1 ELSE 0 END) AS pendentes,
                SUM(CASE WHEN sp.status LIKE 'cancelada%' THEN 1 ELSE 0 END) AS canceladas,
                IFNULL(d.carga_horaria_minima, 0) AS ch_minima,
                COUNT(DISTINCT sp.tema_id) AS total_temas
            FROM slot_planejamento sp
            INNER JOIN planejamento p ON sp.planejamento_id = p.id_planejamento
            INNER JOIN atribuicao_professor ap ON p.atribuicao_professor_id = ap.id_atribuicao_professor
            INNER JOIN semestre_letivo sl ON ap.semestre_letivo_id = sl.id_semestre_letivo
            INNER JOIN disciplina d ON ap.disciplina_id = d.id_disciplina
            INNER JOIN curso c ON d.curso_id = c.id_curso
            WHERE sl.ano = ? AND sl.numero_semestre = ? AND c.nome = ? AND d.nome = ? AND ap.professor_id = ?
            GROUP BY d.carga_horaria_minima;
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ano);
            ps.setInt(2, semestreAno);
            ps.setString(3, nomeCurso);
            ps.setString(4, nomeDisciplina);
            ps.setInt(5, idProfessor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    metricas.put("totalAulas", rs.getInt("total_aulas"));
                    metricas.put("ministradas", rs.getInt("ministradas"));
                    metricas.put("pendentes", rs.getInt("pendentes"));
                    metricas.put("canceladas", rs.getInt("canceladas"));
                    metricas.put("chMinima", rs.getInt("ch_minima"));
                    metricas.put("totalTemas", rs.getInt("total_temas"));
                    return metricas;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }

        metricas.put("totalAulas", 0);
        metricas.put("ministradas", 0);
        metricas.put("pendentes", 0);
        metricas.put("canceladas", 0);
        metricas.put("chMinima", 0);
        metricas.put("totalTemas", 0);
        return metricas;
    }
}