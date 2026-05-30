package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CancelamentoAdm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CancelamentoAdmDAO {

    public List<CancelamentoAdm> listarCancelamentos(Integer semestre_letivo_id) throws SQLException {
        String sql = "SELECT * FROM cancelamento_adm WHERE semestre_letivo_id = ? AND deletado_em IS NULL ORDER BY criado_em ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<CancelamentoAdm> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, semestre_letivo_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                CancelamentoAdm c = new CancelamentoAdm();
                c.setId_cancelamento_adm(rs.getInt("id_cancelamento_adm"));
                c.setAdm_id(rs.getInt("adm_id"));
                c.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                c.setData(rs.getObject("data", LocalDate.class));
                c.setTurno(rs.getString("turno"));
                c.setDia_inteiro(rs.getBoolean("dia_inteiro"));
                c.setMotivo(rs.getString("motivo"));
                c.setCriado_em(rs.getObject("criado_em", LocalDate.class));
                c.setDeletado_em(rs.getObject("deletado_em", LocalDate.class));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar cancelamentos: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }

    public List<CancelamentoAdm> listarPorSemestre(Integer sl){
        String sql = """
        SELECT id_cancelamento_adm, adm_id, semestre_letivo_id,
               data, turno, dia_inteiro, motivo, criado_em, deletado_em
        FROM cancelamento_adm WHERE semestre_letivo_id = ?
        AND deletado_em IS NULL;
        """;

        List<CancelamentoAdm> cancelamentos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sl);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CancelamentoAdm cadm = new CancelamentoAdm();
                    cadm.setId_cancelamento_adm(rs.getInt("id_cancelamento_adm"));
                    cadm.setAdm_id(rs.getInt("adm_id"));
                    cadm.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                    cadm.setData(rs.getObject("data", LocalDate.class));
                    cadm.setTurno(rs.getString("turno"));
                    cadm.setDia_inteiro(rs.getBoolean("dia_inteiro"));
                    cadm.setMotivo(rs.getString("motivo"));
                    cadm.setCriado_em(rs.getObject("criado_em", LocalDate.class));
                    cadm.setDeletado_em(rs.getObject("deletado_em", LocalDate.class));

                    cancelamentos.add(cadm);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cancelamentos;
    }

    public void salvar(CancelamentoAdm c) throws SQLException {
        String sql = """
            INSERT INTO cancelamento_adm (adm_id, semestre_letivo_id, data, turno, dia_inteiro, motivo, criado_em) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, c.getAdm_id());
            ps.setInt(2, c.getSemestre_letivo_id());
            ps.setObject(3, c.getData());
            ps.setString(4, c.getTurno());
            ps.setBoolean(5, c.getDia_inteiro());
            ps.setString(6, c.getMotivo());
            ps.setObject(7, LocalDate.now()); // criado_em recebe a data atual

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar cancelamento: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
    }

    public void excluir(int idCancelamentoAdm) throws SQLException {
        // Se o seu sistema faz exclusão lógica (update deletado_em), mude para o UPDATE correspondente.
        // Aqui usaremos o DELETE físico para seguir a mesma lógica do DataBloqueadaDAO.
        String sql = "DELETE FROM cancelamento_adm WHERE id_cancelamento_adm = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idCancelamentoAdm);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir cancelamento: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
    }

    public Map<Integer, List<Integer>> listarHorariosCanceladosPorCancelamento(Integer sl) {
        String sql = """
        SELECT cah.cancelamento_adm_id, cah.horario_curso_id
        FROM cancelamento_adm_horario cah
        INNER JOIN cancelamento_adm ca
                ON ca.id_cancelamento_adm = cah.cancelamento_adm_id
        WHERE ca.semestre_letivo_id = ?
          AND ca.dia_inteiro = 0
          AND ca.deletado_em IS NULL;
        """;

        Map<Integer, List<Integer>> resultado = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sl);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer cancelamentoId = rs.getInt("cah.cancelamento_adm_id");
                    Integer horarioId = rs.getInt("cah.horario_curso_id");

                    resultado
                            .computeIfAbsent(cancelamentoId, k -> new ArrayList<>())
                            .add(horarioId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultado;
    }
}