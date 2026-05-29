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

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, email);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao remover tipo do usuário: " + e.getMessage());
            throw e;
        }
    }

    public void inserirUsuarioTipo(UsuarioTipo usuarioTipo) {
        String sql = "INSERT INTO usuario_tipo (usuario_id, tipo) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, usuarioTipo.getUsuario_id());
            ps.setString(2, usuarioTipo.getTipo());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao inserir tipo de usuário: " + e.getMessage());
        }
    }

    public List<UsuarioTipo> listarUsuariosTipo() {
        List<UsuarioTipo> usuariosTipo = new ArrayList<>();
        String sql = "SELECT * FROM usuario_tipo";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UsuarioTipo usuarioTipo = new UsuarioTipo();
                usuarioTipo.setUsuario_id(rs.getInt("usuario_id"));
                usuarioTipo.setTipo(rs.getString("tipo"));
                usuariosTipo.add(usuarioTipo);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar tipos de usuário: " + e.getMessage());
        }
        return usuariosTipo;
    }

    public void excluirUsuarioTipo(Integer usuarioId, String tipo) {
        String sql = "DELETE FROM usuario_tipo WHERE usuario_id = ? AND tipo = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tipo);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir tipo de usuário: " + e.getMessage());
        }
    }
}