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
    public List<SemestreLetivo> listarProfessorAnoESemestreAno(int professorId) throws SQLException {
        String sql = """
            SELECT DISTINCT sl.id_semestre_letivo, sl.ano, sl.numero_semestre 
            FROM atribuicao_professor AS ap 
            INNER JOIN semestre_letivo AS sl ON sl.id_semestre_letivo = ap.semestre_letivo_id 
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
                sl.setId_semestre_letivo(rs.getInt("id_semestre_letivo"));

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

    public List<SemestreLetivo> listarAdmsAnoESemestreAno() throws SQLException {
        String sql = """
            SELECT DISTINCT id_semestre_letivo, ano, numero_semestre, FROM semestre_letivo;
            """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<SemestreLetivo> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);

            rs = ps.executeQuery();
            while (rs.next()) {
                SemestreLetivo sl = new SemestreLetivo();
                sl.setAno(rs.getInt("ano"));
                sl.setNumero_semestre(rs.getInt("numero_semestre"));
                sl.setId_semestre_letivo(rs.getInt("id_semestre_letivo"));

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

    public List<SemestreLetivo> listarCoordenadorAnoESemestreAno(int coordId) throws SQLException {
        String sql = """
            SELECT DISTINCT sl.id_semestre_letivo, sl.ano, sl.numero_semestre FROM horario_curso AS hc INNER JOIN 
            semestre_letivo AS sl ON hc.semestre_letivo_id = sl.id_semestre_letivo 
            INNER JOIN curso AS c ON c.id_curso = hc.curso_id WHERE c.coordenador_id = ?;
            """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<SemestreLetivo> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, coordId);

            rs = ps.executeQuery();
            while (rs.next()) {
                SemestreLetivo sl = new SemestreLetivo();
                sl.setAno(rs.getInt("sl.ano"));
                sl.setNumero_semestre(rs.getInt("sl.numero_semestre"));
                sl.setId_semestre_letivo(rs.getInt("sl.id_semestre_letivo"));

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