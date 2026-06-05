package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CancelamentoAdm;
import org.example.demo3.entity.DataBloqueada;

import java.sql.*;
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

    public Integer recuperarIdCancelamento(CancelamentoAdm cadm) {
        String sql = """
            SELECT id_cancelamento_adm 
            FROM cancelamento_adm 
            WHERE adm_id = ? 
              AND semestre_letivo_id = ? 
              AND data = ? 
              AND (turno = ? OR (turno IS NULL AND ? IS NULL)) 
              AND dia_inteiro = ? 
              AND motivo = ? 
              AND criado_em = ? 
              AND deletado_em IS NULL;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cadm.getAdm_id());
            ps.setInt(2, cadm.getSemestre_letivo_id());
            ps.setObject(3, cadm.getData());

            if (cadm.getTurno() == null || cadm.getTurno().equalsIgnoreCase("Dia inteiro")) {
                ps.setNull(4, java.sql.Types.VARCHAR);
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, cadm.getTurno());
                ps.setString(5, cadm.getTurno());
            }

            ps.setBoolean(6, cadm.getDia_inteiro());
            ps.setString(7, cadm.getMotivo());
            ps.setObject(8, cadm.getCriado_em());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_cancelamento_adm");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retorna null se não encontrar o registro exato
    }

    public void atualizarEmLote(List<CancelamentoAdm> listaDatas) throws SQLException {
        String sql = "UPDATE cancelamento_adm SET adm_id = ?, motivo = ? " +
                "WHERE data = ? AND semestre_letivo_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (CancelamentoAdm cadm : listaDatas) {
                stmt.setInt(1, cadm.getAdm_id());
                stmt.setString(2, cadm.getMotivo());
                stmt.setObject(3, cadm.getData());
                stmt.setInt(4, cadm.getSemestre_letivo_id());

                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar lote de cancelamentos dia_inteiro bloqueadas: " + e.getMessage());
            throw e;
        }
    }

    public String recuperarMotivoData(LocalDate data, Integer slId) throws SQLException{
        String sql = "SELECT motivo FROM cancelamento_adm " +
                "WHERE data = ? AND semestre_letivo_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, data);
            stmt.setInt(2, slId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("motivo");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar datas bloqueadas: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public List<LocalDate> listarDatasDiaInteiroMotivoComumSL(Integer slId, String motivo) throws SQLException{
        String sql = "SELECT data FROM cancelamento_adm " +
                "WHERE semestre_letivo_id = ? " +
                "AND motivo = ? AND dia_inteiro = 1 ORDER BY data ASC";
        List<LocalDate> listaDatas = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slId);
            stmt.setString(2, motivo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                listaDatas.add(rs.getObject("data", LocalDate.class));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar datas bloqueadas: " + e.getMessage());
            throw e;
        }
        return listaDatas;
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

    public void salvarEmLote(List<CancelamentoAdm> cancelamentoAdms) throws SQLException {
        String sql = """
            INSERT INTO cancelamento_adm (adm_id, semestre_letivo_id, data, turno, dia_inteiro, motivo, criado_em) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CancelamentoAdm cadm: cancelamentoAdms){
                stmt.setInt(1, cadm.getAdm_id());
                stmt.setInt(2, cadm.getSemestre_letivo_id());
                stmt.setObject(3, cadm.getData());
                stmt.setString(4, cadm.getTurno());
                stmt.setBoolean(5, cadm.getDia_inteiro());
                stmt.setString(6, cadm.getMotivo());
                stmt.setObject(7, cadm.getCriado_em());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Erro ao salvar cancelamento Adm em cadeia: " + e.getMessage());
            throw e;
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