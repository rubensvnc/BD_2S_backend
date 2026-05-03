package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.DataCancelada;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataCanceladaDAO {
    public List<DataCancelada> listarCanceladas(int profId) throws SQLException {
        String sql = "SELECT id_data_cancelada, usuario_id, data_bloqueio, descricao, tipo FROM data_cancelada WHERE usuario_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<DataCancelada> lista = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, profId);

            rs = ps.executeQuery();

            while (rs.next()) {
                DataCancelada data = new DataCancelada(
                        rs.getInt("id_data_cancelada"),
                        rs.getInt("usuario_id"),
                        rs.getDate("data_bloqueio").toLocalDate(),
                        rs.getString("descricao"),
                        rs.getString("tipo")
                );
                lista.add(data);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar datas canceladas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }
}
