package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Semestre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SemestreDAO {
    public Semestre buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM semestre WHERE id_semestre = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            rs = ps.executeQuery();
            if (rs.next()) {
                return new Semestre(
                        rs.getInt("id_semestre"),
                rs.getInt("ano"),
                rs.getString("numero_semestre"),
                rs.getDate("data_inicio").toLocalDate(),
                rs.getDate("data_fim").toLocalDate()
                );
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar semestre por ID: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return null;
    }
}
