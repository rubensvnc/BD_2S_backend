package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Atribuicao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AtribuicaoDAO {
    public List<Atribuicao> listarPorProfessorCurso(int profId, int cursoId, int semestreId) throws SQLException {
        String sql = "SELECT a.* FROM atribuicao a " +
                "JOIN disciplina d ON a.disciplina_id = d.id_disciplina " +
                "WHERE a.usuario_id = ? AND d.curso_id = ? AND a.semestre_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Atribuicao> lista = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, profId);
            ps.setInt(2, cursoId);
            ps.setInt(3, semestreId);

            rs = ps.executeQuery();

            while (rs.next()) {
                Atribuicao atribuicao = new Atribuicao(
                        rs.getInt("id_atribuicao"),
                rs.getInt("usuario_id"),
                rs.getInt("disciplina_id"),
                rs.getInt("semestre_id"),
                rs.getString("dia_semana") // Nota: O banco armazena como ENUM
            );
                lista.add(atribuicao);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar atribuições: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }
}
