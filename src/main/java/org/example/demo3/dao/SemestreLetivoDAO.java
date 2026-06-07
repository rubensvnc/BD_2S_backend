package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.SemestreLetivo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SemestreLetivoDAO {

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

    public Integer getIdSemestreLetivo(Integer ano, Integer semestreAno) throws SQLException{
        String sql = """
            SELECT id_semestre_letivo FROM semestre_letivo WHERE ano = ? AND numero_semestre = ?;
            """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, ano);
            ps.setInt(2, semestreAno);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_semestre_letivo");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return null;
    }

    public SemestreLetivo listarSLPorId(Integer id){
        String sql = """
            SELECT * FROM semestre_letivo WHERE id_semestre_letivo = ?;
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                SemestreLetivo sl = new SemestreLetivo();
                sl.setId_semestre_letivo(rs.getInt("id_semestre_letivo"));
                sl.setCriado_por_adm_id(rs.getInt("criado_por_adm_id"));
                sl.setAno(rs.getInt("ano"));
                sl.setNumero_semestre(rs.getInt("numero_semestre"));
                sl.setData_inicio(rs.getObject("data_inicio", LocalDate.class));
                sl.setData_fim(rs.getObject("data_fim", LocalDate.class));
                sl.setData_tg(rs.getObject("data_tg", LocalDate.class));
                sl.setData_feira(rs.getObject("data_feira", LocalDate.class));
                return sl;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
        }
        return null;
    }

    public List<SemestreLetivo> listarAdmsAnoESemestreAno() throws SQLException {
        String sql = """
            SELECT DISTINCT id_semestre_letivo, ano, numero_semestre FROM semestre_letivo;
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

    // --- NOVO MÉTODO ADICIONADO EXTRAÍDO DO CONTROLLER ---
    public int salvarOuAtualizarSemestre(Connection conn, SemestreLetivo sl) throws SQLException {
        String sqlUpsert = """
            INSERT INTO semestre_letivo
                (criado_por_adm_id, ano, numero_semestre, data_inicio, data_fim, data_tg, data_feira)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                data_inicio = VALUES(data_inicio),
                data_fim    = VALUES(data_fim),
                data_tg     = VALUES(data_tg),
                data_feira  = VALUES(data_feira)
        """;

        try (PreparedStatement stmtSem = conn.prepareStatement(sqlUpsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmtSem.setInt(1, sl.getCriado_por_adm_id());
            stmtSem.setInt(2, sl.getAno());
            stmtSem.setInt(3, sl.getNumero_semestre());
            stmtSem.setDate(4, java.sql.Date.valueOf(sl.getData_inicio()));
            stmtSem.setDate(5, java.sql.Date.valueOf(sl.getData_fim()));
            stmtSem.setDate(6, java.sql.Date.valueOf(sl.getData_tg()));
            stmtSem.setDate(7, java.sql.Date.valueOf(sl.getData_feira()));
            stmtSem.executeUpdate();

            try (ResultSet keys = stmtSem.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                } else {
                    Integer id = getIdSemestreLetivo(sl.getAno(), sl.getNumero_semestre());
                    return (id != null) ? id : -1;
                }
            }
        }
    }
}