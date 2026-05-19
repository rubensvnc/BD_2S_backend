package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.SemestreLetivo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SemestreLetivoDAO{
    public List<SemestreLetivo> listarAnoESemestreAno(int professorId) throws SQLException {
        String sql = """
            SELECT DISTINCT sl.ano, sl.numero_semestre FROM atribuicao_professor
            AS ap INNER JOIN semestre_letivo AS sl ON sl.id_semestre_letivo = semestre_letivo_id 
            WHERE ap.professor_id = ? ORDER BY sl.ano ASC;
            """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<SemestreLetivo> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, professorId);

            rs = ps.executeQuery();
            while (rs.next()) {
                SemestreLetivo sl = new SemestreLetivo();
                sl.setAno(rs.getInt("sl.ano"));
                sl.setNumero_semestre(rs.getInt("sl.numero_semestre"));

                lista.add(sl);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }

        return lista;
    }
}