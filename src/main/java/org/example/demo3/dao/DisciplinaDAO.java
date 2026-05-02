package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Disciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    public List<String> listarSemestresProfessorCurso(int idProfessor, int idCurso) throws SQLException {
        String sql = "SELECT DISTINCT d.semestre " +
                "FROM disciplina d " +
                "JOIN atribuicao a ON d.id_disciplina = a.disciplina_id " +
                "WHERE a.usuario_id = ? " +
                "AND d.curso_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> semestres = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idProfessor);
            ps.setInt(2, idCurso);
            rs = ps.executeQuery();

            while (rs.next()) {
                semestres.add(rs.getString("semestre"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar semestres: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return semestres;
    }

}