package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Sprint;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SprintDAO {

    public List<Sprint> listarPorSemestre(Integer idSemestreLetivo) {
        String sql = """
        SELECT id_sprint, semestre_letivo_id, numero, data_inicio, data_fim, data_review
        FROM sprint
        WHERE semestre_letivo_id = ?
        ORDER BY numero;
        """;

        List<Sprint> sprints = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSemestreLetivo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Sprint sprint = new Sprint();
                    sprint.setId_sprint(rs.getInt("id_sprint"));
                    sprint.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                    sprint.setNumero(rs.getInt("numero"));
                    sprint.setData_inicio(rs.getObject("data_inicio", LocalDate.class));
                    sprint.setData_fim(rs.getObject("data_fim", LocalDate.class));
                    sprint.setData_review(rs.getObject("data_review", LocalDate.class));
                    sprints.add(sprint);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sprints;
    }

    public void inserirSprint(Sprint sprint) {
        String sql = """
            INSERT INTO sprint (
                semestre_letivo_id,
                numero,
                data_inicio,
                data_fim,
                data_review
            ) VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, sprint.getSemestre_letivo_id());
            stmt.setInt(2, sprint.getNumero());
            stmt.setDate(3, Date.valueOf(sprint.getData_inicio()));
            stmt.setDate(4, Date.valueOf(sprint.getData_fim()));
            stmt.setDate(5, Date.valueOf(sprint.getData_review()));

            stmt.executeUpdate();

            System.out.println("Sprint inserida com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inserir sprint: " + e.getMessage());
        }
    }

    public List<Sprint> listarSprints() {
        List<Sprint> sprints = new ArrayList<>();

        String sql = """
            SELECT *
            FROM sprint
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Sprint sprint = new Sprint();

                sprint.setId_sprint(rs.getInt("id_sprint"));
                sprint.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                sprint.setNumero(rs.getInt("numero"));
                sprint.setData_inicio(rs.getDate("data_inicio").toLocalDate());
                sprint.setData_fim(rs.getDate("data_fim").toLocalDate());
                sprint.setData_review(rs.getDate("data_review").toLocalDate());

                sprints.add(sprint);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar sprints: " + e.getMessage());
        }

        return sprints;
    }

    public void atualizarSprint(Sprint sprint) {
        String sql = """
            UPDATE sprint
            SET semestre_letivo_id = ?,
                numero = ?,
                data_inicio = ?,
                data_fim = ?,
                data_review = ?
            WHERE id_sprint = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, sprint.getSemestre_letivo_id());
            stmt.setInt(2, sprint.getNumero());
            stmt.setDate(3, Date.valueOf(sprint.getData_inicio()));
            stmt.setDate(4, Date.valueOf(sprint.getData_fim()));
            stmt.setDate(5, Date.valueOf(sprint.getData_review()));
            stmt.setInt(6, sprint.getId_sprint());

            stmt.executeUpdate();

            System.out.println("Sprint updated com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar sprint: " + e.getMessage());
        }
    }

    public void excluirSprint(Integer idSprint) {
        String sql = """
            DELETE FROM sprint
            WHERE id_sprint = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idSprint);

            stmt.executeUpdate();

            System.out.println("Sprint removida com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao remover sprint: " + e.getMessage());
        }
    }

    public void salvarOuAtualizarSprintsEmLote(Connection connection, List<Sprint> sprints) throws SQLException {
        String sqlInserir = """
            INSERT INTO sprint (semestre_letivo_id, numero, data_inicio, data_fim, data_review)
            VALUES (?, ?, ?, ?, ?)
            """;

        String sqlAtualizar = """
            UPDATE sprint
            SET semestre_letivo_id = ?,
                numero = ?,
                data_inicio = ?,
                data_fim = ?,
                data_review = ?
            WHERE id_sprint = ?
            """;

        try (PreparedStatement stmtIns = connection.prepareStatement(sqlInserir);
             PreparedStatement stmtAtt = connection.prepareStatement(sqlAtualizar)) {

            for (Sprint sprint : sprints) {
                if (sprint.getId_sprint() == null || sprint.getId_sprint() == 0) {
                    stmtIns.setInt(1, sprint.getSemestre_letivo_id());
                    stmtIns.setInt(2, sprint.getNumero());
                    stmtIns.setDate(3, Date.valueOf(sprint.getData_inicio()));
                    stmtIns.setDate(4, Date.valueOf(sprint.getData_fim()));
                    stmtIns.setDate(5, Date.valueOf(sprint.getData_review()));
                    stmtIns.addBatch();
                } else {
                    stmtAtt.setInt(1, sprint.getSemestre_letivo_id());
                    stmtAtt.setInt(2, sprint.getNumero());
                    stmtAtt.setDate(3, Date.valueOf(sprint.getData_inicio()));
                    stmtAtt.setDate(4, Date.valueOf(sprint.getData_fim()));
                    stmtAtt.setDate(5, Date.valueOf(sprint.getData_review()));
                    stmtAtt.setInt(6, sprint.getId_sprint());
                    stmtAtt.addBatch();
                }
            }

            stmtIns.executeBatch();
            stmtAtt.executeBatch();
        }
    }

    public List<Sprint> buscarSprintsPorSemestre(int idSemestreLetivo) {
        List<Sprint> sprints = new ArrayList<>();
        String sql = """
            SELECT * FROM sprint 
            WHERE semestre_letivo_id = ? 
            ORDER BY numero ASC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idSemestreLetivo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sprint sprint = new Sprint();
                    sprint.setId_sprint(rs.getInt("id_sprint"));
                    sprint.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                    sprint.setNumero(rs.getInt("numero"));
                    sprint.setData_inicio(rs.getDate("data_inicio").toLocalDate());
                    sprint.setData_fim(rs.getDate("data_fim").toLocalDate());
                    sprint.setData_review(rs.getDate("data_review").toLocalDate());
                    sprints.add(sprint);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar sprints por semestre: " + e.getMessage());
        }
        return sprints;
    }
}