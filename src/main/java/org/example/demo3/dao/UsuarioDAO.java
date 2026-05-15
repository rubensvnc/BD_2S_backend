package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection connection;

    public UsuarioDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void inserirUsuario(Usuario usuario) {

        String sql = """
            INSERT INTO usuario (
                nome,
                email,
                senha_hash,
                criado_em
            ) VALUES (?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha_hash());
            stmt.setDate(4, Date.valueOf(usuario.getCriado_em()));

            stmt.executeUpdate();

            System.out.println("Usuário inserido com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inserir usuário: " + e.getMessage());
        }
    }

    public List<Usuario> listarUsuarios() {

        List<Usuario> usuarios = new ArrayList<>();

        String sql = """
            SELECT *
            FROM usuario
            WHERE deletado_em IS NULL
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Usuario usuario = new Usuario();

                usuario.setId_usuario(rs.getInt("id_usuario"));
                usuario.setNome(rs.getString("nome"));
                usuario.setEmail(rs.getString("email"));
                usuario.setSenha_hash(rs.getString("senha_hash"));

                usuario.setCriado_em(
                        rs.getDate("criado_em").toLocalDate()
                );

                usuarios.add(usuario);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários: " + e.getMessage());
        }

        return usuarios;
    }

    public void editarUsuario(Usuario usuario) {

        String sql = """
            UPDATE usuario
            SET
                nome = ?,
                email = ?,
                senha_hash = ?
            WHERE id_usuario = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha_hash());
            stmt.setInt(4, usuario.getId_usuario());

            stmt.executeUpdate();

            System.out.println("Usuário editado com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao editar usuário: " + e.getMessage());
        }
    }

    public void excluirUsuario(Integer idUsuario) {

        String sql = """
            UPDATE usuario
            SET deletado_em = CURRENT_DATE
            WHERE id_usuario = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();

            System.out.println("Usuário excluído com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao excluir usuário: " + e.getMessage());
        }
    }
}
