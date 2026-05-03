package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.DataRestritaTodos;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataRestritaTodosDAO {

    public boolean existeData(LocalDate data) throws SQLException {
        String sql = "SELECT COUNT(*) FROM data_restrita_todos WHERE data_bloqueio = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(data));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<DataRestritaTodos> listarTodos() throws SQLException {
        List<DataRestritaTodos> lista = new ArrayList<>();
        String sql = "SELECT id_data_restrita, adm_id, data_bloqueio, descricao FROM data_restrita_todos";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new DataRestritaTodos(
                        rs.getInt("id_data_restrita"),
                        rs.getInt("adm_id"),
                        rs.getDate("data_bloqueio").toLocalDate(),
                        rs.getString("descricao")
                ));
            }
        }
        return lista;
    }

    public void salvar(DataRestritaTodos data) throws SQLException {
        String sql = "INSERT INTO data_restrita_todos (adm_id, data_bloqueio, descricao) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, data.getAdm_id()); // ID do usuário logado
            stmt.setDate(2, Date.valueOf(data.getData_bloqueio()));
            stmt.setString(3, data.getDescricao());
            stmt.executeUpdate();
        }
    }

    public void excluir(Integer idDataRestrita) throws SQLException {
        String sql = "DELETE FROM data_restrita_todos WHERE id_data_restrita = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idDataRestrita);
            stmt.executeUpdate();
        }
    }
}