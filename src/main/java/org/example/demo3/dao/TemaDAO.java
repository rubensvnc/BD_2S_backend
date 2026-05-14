package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Tema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TemaDAO {

    public List<Tema> listarPorDisciplina(int idDisciplina) throws SQLException {
        String sql = "SELECT * FROM tema WHERE disciplina_id = ? AND deleted_at IS NULL ORDER BY prioridade ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Tema> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idDisciplina);

            rs = ps.executeQuery();
            while (rs.next()) {
                Tema t = new Tema();
                t.setId_tema(rs.getInt("id_tema"));
                t.setDisciplina_id(rs.getInt("disciplina_id"));
                t.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                t.setNome(rs.getString("nome"));
                t.setEh_avaliacao(rs.getInt("eh_avaliacao"));
                t.setQtd_min_aulas(rs.getInt("qtd_min_aulas"));
                t.setQtd_max_aulas(rs.getInt("qtd_max_aulas"));
                t.setPrioridade(rs.getInt("prioridade"));
                t.setEh_opcional(rs.getInt("eh_opcional"));
                t.setDeleted_at(rs.getObject("deleted_at", LocalDate.class));

                lista.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }

    public Tema buscarPorId(int idTema) throws SQLException {
        String sql = "SELECT * FROM tema WHERE id_tema = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idTema);

            rs = ps.executeQuery();
            if (rs.next()) {
                return new Tema(
                        rs.getInt("id_tema"),
                        rs.getInt("disciplina_id"),
                        rs.getInt("semestre_letivo_id"),
                        rs.getString("nome"),
                        rs.getInt("eh_avaliacao"),
                        rs.getInt("qtd_min_aulas"),
                        rs.getInt("qtd_max_aulas"),
                        rs.getInt("prioridade"),
                        rs.getInt("eh_opcional"),
                        rs.getObject("deleted_at", LocalDate.class)
                );
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar tema: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return null;
    }

    public int inserirTema(Tema t) throws SQLException {
        String sql = "INSERT INTO tema (disciplina_id, semestre_letivo_id, nome, eh_avaliacao, " +
                "qtd_min_aulas, qtd_max_aulas, prioridade, eh_opcional) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setInt(1, t.getDisciplina_id());
            ps.setInt(2, t.getSemestre_letivo_id());
            ps.setString(3, t.getNome());
            ps.setInt(4, t.getEh_avaliacao());
            ps.setInt(5, t.getQtd_min_aulas());
            ps.setInt(6, t.getQtd_max_aulas());
            ps.setInt(7, t.getPrioridade());
            ps.setInt(8, t.getEh_opcional());

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir tema: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return -1;
    }
}