package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CronogramaItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CronogramaItemDAO {
    public void inserirItem(CronogramaItem item) throws SQLException {
        String sql = "INSERT INTO cronograma_item (cronograma_id, tema_id, id_data_cancelada, data_prevista, qtd_aulas, status_aula) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, item.getCronograma_id());
            ps.setInt(2, item.getTema_id());

            if (item.getId_data_cancelada() != null) {
                ps.setInt(3, item.getId_data_cancelada());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setDate(4, java.sql.Date.valueOf(item.getData_prevista()));
            ps.setInt(5, item.getQtd_aulas());
            ps.setString(6, item.getStatus_aula());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir item no cronograma: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}
