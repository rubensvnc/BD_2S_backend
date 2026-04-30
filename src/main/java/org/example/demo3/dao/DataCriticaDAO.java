package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Datas_Restritas;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataCriticaDAO {

    // Regra: Não permitir mais de um feriado na mesma data
    public boolean existeData(LocalDate data) throws SQLException {
        String sql = "SELECT COUNT(*) FROM datas_restritas WHERE data_bloqueio = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(data));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Datas_Restritas> listarTodos() throws SQLException {
        List<Datas_Restritas> lista = new ArrayList<>();
        String sql = "SELECT id_restricao, data_bloqueio, descricao, id_referencia FROM datas_restritas";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(new Datas_Restritas(
                        rs.getInt("id_restricao"),
                        rs.getDate("data_bloqueio").toLocalDate(),
                        rs.getString("descricao"),
                        rs.getObject("id_referencia") != null ? rs.getInt("id_referencia") : null
                ));
            }
        }
        return lista;
    }

    public void salvar(Datas_Restritas data) throws SQLException {
        String sql = "INSERT INTO datas_restritas (data_bloqueio, descricao, id_referencia) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(data.getData_bloqueio()));
            stmt.setString(2, data.getDescricao());
            if (data.getId_referencia() != null) {
                stmt.setInt(3, data.getId_referencia());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    public void excluir(Integer idRestricao) throws SQLException {
        String sql = "DELETE FROM datas_restritas WHERE id_restricao = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRestricao);
            stmt.executeUpdate();
        }
    }
}