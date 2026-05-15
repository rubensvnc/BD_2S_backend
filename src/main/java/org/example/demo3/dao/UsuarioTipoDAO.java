```java id="n7f8qy"
        package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioTipoDAO {

    private Connection connection;

    public UsuarioTipoDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void inserirUsuarioTipo(UsuarioTipo usuarioTipo) {

        String sql = """
            INSERT INTO usuario_tipo (
                usuario_id,
                tipo
            ) VALUES (?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, usuarioTipo.getUsuario_id());
            stmt.setString(2, usuarioTipo.getTipo());

            stmt.executeUpdate();

            System.out.println("Tipo de usuário inserido com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inserir tipo de usuário: " + e.getMessage());
        }
    }

    public List<UsuarioTipo> listarUsuariosTipo() {

        List<UsuarioTipo> usuariosTipo = new ArrayList<>();

        String sql = """
            SELECT *
            FROM usuario_tipo
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                UsuarioTipo usuarioTipo = new UsuarioTipo();

                usuarioTipo.setUsuario_id(rs.getInt("usuario_id"));
                usuarioTipo.setTipo(rs.getString("tipo"));

                usuariosTipo.add(usuarioTipo);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar tipos de usuário: " + e.getMessage());
        }

        return usuariosTipo;
    }

    public void excluirUsuarioTipo(Integer usuarioId, String tipo) {

        String sql = """
            DELETE FROM usuario_tipo
            WHERE usuario_id = ?
            AND tipo = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setString(2, tipo);

            stmt.executeUpdate();

            System.out.println("Tipo de usuário removido com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao remover tipo de usuário: " + e.getMessage());
        }
    }
}
```
