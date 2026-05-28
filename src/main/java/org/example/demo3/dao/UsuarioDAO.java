package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioDAO {

    private Connection connection;

    public UsuarioDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public Map<String, Object> buscarUsuarioPorEmail(String email) throws SQLException {
        String sql = """
        SELECT u.id_usuario, u.nome, u.senha_hash, ut.tipo 
        FROM usuario u 
        INNER JOIN usuario_tipo ut ON u.id_usuario = ut.usuario_id 
        WHERE u.email = ? AND u.deletado_em IS NULL
        """;

        // CORREÇÃO: Abre a conexão direto no try (Auto-Closeable)
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> usuario = new HashMap<>();
                    usuario.put("id_usuario", rs.getInt("id_usuario"));
                    usuario.put("nome", rs.getString("nome"));
                    usuario.put("senha_hash", rs.getString("senha_hash"));
                    usuario.put("tipo", rs.getString("tipo"));
                    return usuario;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário por e-mail: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public List<Usuario> listarCoordSemestreLetivo (Integer ano, Integer anoSemestre) throws SQLException{

        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT DISTINCT u.nome FROM usuario AS u INNER JOIN usuario_tipo 
            AS ut ON ut.usuario_id = u.id_usuario INNER JOIN curso AS c 
            ON c.coordenador_id = u.id_usuario INNER JOIN horario_curso AS hc 
            ON hc.curso_id = c.id_curso INNER JOIN semestre_letivo AS sl 
            ON sl.id_semestre_letivo = hc.semestre_letivo_id 
            WHERE ut.tipo = "COORD" AND sl.ano = ? AND sl.numero_semestre = ?;
            """;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DatabaseConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, ano);
            ps.setInt(2, anoSemestre);

            rs = ps.executeQuery();
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setNome(rs.getString("u.nome"));

                usuarios.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }

        return usuarios;
    }

    public List<Usuario> listarProfSemestreLetivo (Integer ano, Integer anoSemestre) throws SQLException{
        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT DISTINCT u.id_usuario, u.email FROM usuario AS u INNER JOIN usuario_tipo 
            AS ut ON ut.usuario_id = u.id_usuario INNER JOIN atribuicao_professor 
            AS ap ON ap.professor_id = ut.usuario_id INNER JOIN semestre_letivo 
            AS sl ON sl.id_semestre_letivo = ap.semestre_letivo_id 
            WHERE ut.tipo = "PROF" AND sl.ano = ? AND sl.numero_semestre = ?
            AND u.id_usuario NOT IN (SELECT coordenador_id FROM curso WHERE coordenador_id IS NOT NULL);
            """;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DatabaseConnection.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setInt(1, ano);
            ps.setInt(2, anoSemestre);

            rs = ps.executeQuery();
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId_usuario(rs.getInt("u.id_usuario"));
                u.setEmail(rs.getString("u.email"));

                usuarios.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            connection.close();
        }

        return usuarios;
    }

    //  LISTA professores do curso


    public List<Usuario> listarProfessoresDoCurso(Integer idCurso, Integer ano, Integer anoSemestre) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = """
            SELECT DISTINCT u.id_usuario, u.nome
            FROM usuario AS u
            INNER JOIN usuario_tipo AS ut ON ut.usuario_id = u.id_usuario
            INNER JOIN atribuicao_professor AS ap ON ap.professor_id = u.id_usuario
            INNER JOIN semestre_letivo AS sl ON sl.id_semestre_letivo = ap.semestre_letivo_id
            INNER JOIN disciplina AS d ON d.id_disciplina = ap.disciplina_id
            WHERE ut.tipo = "PROF"
              AND sl.ano = ?
              AND sl.numero_semestre = ?
              AND d.curso_id = ?
              AND u.deletado_em IS NULL
            ORDER BY u.nome
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, ano);
            ps.setInt(2, anoSemestre);
            ps.setInt(3, idCurso);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId_usuario(rs.getInt("id_usuario"));
                    u.setNome(rs.getString("nome"));
                    usuarios.add(u);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar professores do curso: " + e.getMessage());
            throw e;
        }

        return usuarios;
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