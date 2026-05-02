package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Curso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    public List<String> listarCursosProfessor(Integer idProfessor) throws SQLException {
        String sql = "SELECT DISTINCT c.nome FROM curso c " +
                "JOIN disciplina d ON c.id_curso = d.curso_id " +
                "JOIN atribuicao a ON d.id_disciplina = a.disciplina_id " +
                "WHERE a.usuario_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> cursos = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idProfessor);
            rs = ps.executeQuery();
            while (rs.next()) {
                cursos.add(rs.getString("nome"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar cursos de um professor: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return cursos;
    }

    public Integer getIdCurso(String nomeCurso) throws SQLException{
        String sql = "SELECT id_curso FROM curso WHERE nome = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, nomeCurso);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_curso");
            } else {
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao recuperar idCurso: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}
