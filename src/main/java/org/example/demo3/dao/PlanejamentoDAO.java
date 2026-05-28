package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Planejamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanejamentoDAO {


    public static Map<String, Object> obterEstatisticasGlobais(int ano, int semestreAno,
                                                                Integer id_curso, Integer id_disciplina,
                                                                Integer idProfessor) {
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
            WHERE sl.ano = ? AND sl.numero_semestre = ? AND c.id_curso = ? AND d.id_disciplina = ? AND ap.professor_id = ?
            GROUP BY d.carga_horaria_minima;
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ano);
            ps.setInt(2, semestreAno);
            ps.setInt(3, id_curso);
            ps.setInt(4, id_disciplina);
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


    public void inserirPlanejamento(Planejamento planejamento) {

        String sql = """
                INSERT INTO planejamento
                (atribuicao_professor_id, gerado_em)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, planejamento.getAtribuicao_professor_id());
            stmt.setTimestamp(2, Timestamp.valueOf(planejamento.getGerado_em()));

            stmt.executeUpdate();

            System.out.println("Planejamento inserido com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Planejamento> listarPlanejamentos() {

        List<Planejamento> lista = new ArrayList<>();

        String sql = "SELECT * FROM planejamento";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Planejamento planejamento = new Planejamento();

                planejamento.setId_planejamento(rs.getInt("id_planejamento"));
                planejamento.setAtribuicao_professor_id(rs.getInt("atribuicao_professor_id"));
                planejamento.setGerado_em(rs.getTimestamp("gerado_em").toLocalDateTime());

                lista.add(planejamento);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    public Planejamento buscarPorId(Integer id) {

        String sql = "SELECT * FROM planejamento WHERE id_planejamento = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                Planejamento planejamento = new Planejamento();

                planejamento.setId_planejamento(rs.getInt("id_planejamento"));
                planejamento.setAtribuicao_professor_id(rs.getInt("atribuicao_professor_id"));
                planejamento.setGerado_em(rs.getTimestamp("gerado_em").toLocalDateTime());

                return planejamento;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void atualizarPlanejamento(Planejamento planejamento) {

        String sql = """
                UPDATE planejamento
                SET atribuicao_professor_id = ?,
                    gerado_em = ?
                WHERE id_planejamento = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, planejamento.getAtribuicao_professor_id());
            stmt.setTimestamp(2, Timestamp.valueOf(planejamento.getGerado_em()));
            stmt.setInt(3, planejamento.getId_planejamento());

            stmt.executeUpdate();

            System.out.println("Planejamento atualizado!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deletarPlanejamento(Integer id) {

        String sql = "DELETE FROM planejamento WHERE id_planejamento = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

            System.out.println("Planejamento deletado!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}