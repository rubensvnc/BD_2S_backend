package org.example.demo3.dao;

import org.example.demo3.SlotPlanejamento;
import org.example.demo3.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlotPlanejamentoDAO {

    private Connection getConnection() {
        return DatabaseConnection.getConnection();
    }

    public List<Map<String, Object>> buscarDadosMixados(int ano, int semestre, int idCurso, int idDisciplina) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        String sql = "SELECT sp.id_slot_planejamento, sp.planejamento_id, sp.data, sp.horario_curso_id, " +
                "       sp.tema_id, sp.status, sp.motivo_cancelamento, sp.cancelamento_adm_id, " +
                "       hc.hora_inicio, t.nome AS nome_tema " +
                "FROM slot_planejamento sp " +
                "INNER JOIN planejamento p ON sp.planejamento_id = p.id_planejamento " +
                "INNER JOIN atribuicao_professor ap ON p.atribuicao_professor_id = ap.id_atribuicao_professor " +
                "INNER JOIN semestre_letivo sl ON ap.semestre_letivo_id = sl.id_semestre_letivo " +
                "INNER JOIN disciplina d ON ap.disciplina_id = d.id_disciplina " +
                "INNER JOIN curso c ON d.curso_id = c.id_curso " +
                "INNER JOIN horario_curso hc ON sp.horario_curso_id = hc.id_horario_curso " +
                "LEFT JOIN tema t ON sp.tema_id = t.id_tema " +
                "WHERE sl.ano = ? AND sl.numero_semestre = ? AND c.id_curso = ? AND d.id_disciplina = ? " +
                "ORDER BY sp.data ASC, hc.hora_inicio ASC";

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ano);
            stmt.setInt(2, semestre);
            stmt.setInt(3, idCurso);
            stmt.setInt(4, idDisciplina);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SlotPlanejamento slot = new SlotPlanejamento(
                            rs.getInt("id_slot_planejamento"),
                            rs.getInt("planejamento_id"),
                            rs.getDate("data").toLocalDate(),
                            rs.getInt("horario_curso_id"),
                            rs.getInt("tema_id"),
                            rs.getString("status"),
                            rs.getString("motivo_cancelamento"),
                            rs.getInt("cancelamento_adm_id")
                    );

                    Map<String, Object> linha = new HashMap<>();
                    linha.put("entidade", slot);
                    linha.put("hora_inicio", rs.getString("hora_inicio"));
                    linha.put("nome_tema", rs.getString("nome_tema"));

                    resultado.add(linha);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
        return resultado;
    }

    public void atualizarStatusEmLote(List<Integer> ids, String novoStatus, String motivo) {
        String sql = "UPDATE slot_planejamento SET status = ?, motivo_cancelamento = ? WHERE id_slot_planejamento = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (Integer id : ids) {
                stmt.setString(1, novoStatus);
                stmt.setString(2, motivo);
                stmt.setInt(3, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}