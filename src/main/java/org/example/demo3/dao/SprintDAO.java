package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Sprint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SprintDAO {

    private Connection connection;

    public SprintDAO() {
        this.connection = DatabaseConnection.getConnection();
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

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

        try (PreparedStatement stmt = connection.prepareStatement(sql);
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, sprint.getSemestre_letivo_id());
            stmt.setInt(2, sprint.getNumero());
            stmt.setDate(3, Date.valueOf(sprint.getData_inicio()));
            stmt.setDate(4, Date.valueOf(sprint.getData_fim()));
            stmt.setDate(5, Date.valueOf(sprint.getData_review()));
            stmt.setInt(6, sprint.getId_sprint());

            stmt.executeUpdate();

            System.out.println("Sprint atualizada com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar sprint: " + e.getMessage());
        }
    }

    public void excluirSprint(Integer idSprint) {

        String sql = """
            DELETE FROM sprint
            WHERE id_sprint = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idSprint);

            stmt.executeUpdate();

            System.out.println("Sprint removida com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao remover sprint: " + e.getMessage());
        }
    }
}