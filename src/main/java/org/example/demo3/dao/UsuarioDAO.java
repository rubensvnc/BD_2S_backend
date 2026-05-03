package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    public String buscarTipoPorEmailESenha(String email, String senha) throws SQLException{
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String tipo = null;

        // SQL que une as tabelas e filtra pelas credenciais
        String sql = "SELECT ut.tipo " +
                "FROM usuario u " +
                "JOIN usuario_tipo ut ON u.id_usuario = ut.usuario_id " +
                "WHERE u.email = ? AND u.senha = ? AND u.ativo = 1";

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, senha);
            rs = ps.executeQuery();

            if (rs.next()) {
                tipo = rs.getString("tipo");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao retornar tipo de usuario: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }

        return tipo;
    }
}
