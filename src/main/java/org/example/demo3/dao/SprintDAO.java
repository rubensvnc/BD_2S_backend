package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Sprint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SprintDAO {
    public List<Sprint> listarPorSemestre(int semestreId) throws SQLException {
        String sql = "SELECT * FROM sprint WHERE semestre_id = ? ORDER BY numero ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Sprint> lista = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, semestreId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Sprint sprint = new Sprint(
                        rs.getInt("id_sprint"),
                rs.getInt("semestre_id"),
                rs.getInt("numero"),
                rs.getDate("data_inicio").toLocalDate(),
                rs.getDate("data_fim").toLocalDate(),
                rs.getDate("data_review").toLocalDate()
            );
                lista.add(sprint);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar sprints por semestre: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }
}
