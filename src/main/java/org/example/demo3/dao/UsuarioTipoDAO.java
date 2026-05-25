package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioTipoDAO {

    public UsuarioTipoDAO() {
    }

    public void removerUsuarioTipo(String email, String tipo) throws SQLException {
        String sql = "DELETE FROM usuario_tipo WHERE tipo = ? AND usuario_id = (SELECT id_usuario FROM usuario WHERE email = ?)";
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, tipo);
                ps.setString(2, email);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            if (connection != null) connection.rollback();
            throw e;
        } finally {
            if (connection != null) connection.close();
        }
    }

    public void inserirUsuarioTipo(UsuarioTipo usuarioTipo) {
        String sql = "INSERT INTO usuario_tipo (usuario_id, tipo) VALUES (?, ?)";
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = DatabaseConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, usuarioTipo.getUsuario_id());
            ps.setString(2, usuarioTipo.getTipo());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<UsuarioTipo> listarUsuariosTipo() {
        List<UsuarioTipo> usuariosTipo = new ArrayList<>();
        String sql = "SELECT * FROM usuario_tipo";
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            connection = DatabaseConnection.getConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                UsuarioTipo usuarioTipo = new UsuarioTipo();
                usuarioTipo.setUsuario_id(rs.getInt("usuario_id"));
                usuarioTipo.setTipo(rs.getString("tipo"));
                usuariosTipo.add(usuarioTipo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return usuariosTipo;
    }

    public void excluirUsuarioTipo(Integer usuarioId, String tipo) {
        String sql = "DELETE FROM usuario_tipo WHERE usuario_id = ? AND tipo = ?";
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = DatabaseConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, usuarioId);
            ps.setString(2, tipo);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) ps.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}