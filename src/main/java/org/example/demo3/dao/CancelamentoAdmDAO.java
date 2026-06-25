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

    public CancelamentoAdm recuperarCancelamentoAdm(LocalDate data, Integer slId) throws SQLException{
        String sql = "SELECT * FROM cancelamento_adm " +
                "WHERE data = ? AND semestre_letivo_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, data);
            stmt.setInt(2, slId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CancelamentoAdm cadm = new CancelamentoAdm(
                        rs.getInt("id_cancelamento_adm"),
                        rs.getInt("adm_id"),
                        rs.getInt("semestre_letivo_id"),
                        rs.getObject("data", LocalDate.class),
                        rs.getString("turno"),
                        rs.getBoolean("dia_inteiro"),
                        rs.getString("motivo"),
                        rs.getObject("criado_em", LocalDate.class),
                        rs.getObject("deletado_em", LocalDate.class)
                );
                return cadm;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar datas bloqueadas: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public List<CancelamentoAdm> recuperarTodosCancelamentoAdm(Integer slId){
        String sql = "SELECT * FROM cancelamento_adm " +
                "WHERE semestre_letivo_id = ? AND deletado_em IS NULL";

        List<CancelamentoAdm> listCadm = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CancelamentoAdm cadm = new CancelamentoAdm(
                        rs.getInt("id_cancelamento_adm"),
                        rs.getInt("adm_id"),
                        rs.getInt("semestre_letivo_id"),
                        rs.getObject("data", LocalDate.class),
                        rs.getString("turno"),
                        rs.getBoolean("dia_inteiro"),
                        rs.getString("motivo"),
                        rs.getObject("criado_em", LocalDate.class),
                        rs.getObject("deletado_em", LocalDate.class)
                );
                listCadm.add(cadm);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar datas bloqueadas: " + e.getMessage());
        }
        return listCadm;
    }

    public List<LocalDate> listarDatasCancelamentosPorSemestre(Integer sl){
        String sql = "SELECT cadm.data FROM cancelamento_adm cadm WHERE semestre_letivo_id = ? " +
                "AND cadm.deletado_em IS NULL ORDER BY cadm.data;";

        List<LocalDate> datas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sl);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datas.add(rs.getObject("data", LocalDate.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datas;
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
        String sqlInsert = """
        INSERT INTO cancelamento_adm (adm_id, semestre_letivo_id, data, turno, dia_inteiro, motivo, criado_em) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        String sqlUpdate = """
        UPDATE cancelamento_adm 
        SET adm_id = ?, turno = ?, dia_inteiro = ?, motivo = ?, criado_em = ?, deletado_em = NULL
        WHERE semestre_letivo_id = ? AND data = ? AND deletado_em IS NOT NULL
        """;

        String sqlVerificar = """
        SELECT id_cancelamento_adm FROM cancelamento_adm 
        WHERE semestre_letivo_id = ? AND data = ? AND deletado_em IS NOT NULL
        """;

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (CancelamentoAdm cadm : cancelamentoAdms) {
                boolean existeDeletado = false;

                try (PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificar)) {
                    stmtVerificar.setInt(1, cadm.getSemestre_letivo_id());
                    stmtVerificar.setObject(2, cadm.getData());

                    try (ResultSet rs = stmtVerificar.executeQuery()) {
                        existeDeletado = rs.next();
                    }
                }

                if (existeDeletado) {
                    try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                        stmtUpdate.setInt(1, cadm.getAdm_id());
                        stmtUpdate.setString(2, cadm.getTurno());
                        stmtUpdate.setBoolean(3, cadm.getDia_inteiro());
                        stmtUpdate.setString(4, cadm.getMotivo());
                        stmtUpdate.setObject(5, cadm.getCriado_em());
                        stmtUpdate.setInt(6, cadm.getSemestre_letivo_id());
                        stmtUpdate.setObject(7, cadm.getData());

                        stmtUpdate.executeUpdate();
                    }
                } else {
                    try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                        stmtInsert.setInt(1, cadm.getAdm_id());
                        stmtInsert.setInt(2, cadm.getSemestre_letivo_id());
                        stmtInsert.setObject(3, cadm.getData());
                        stmtInsert.setString(4, cadm.getTurno());
                        stmtInsert.setBoolean(5, cadm.getDia_inteiro());
                        stmtInsert.setString(6, cadm.getMotivo());
                        stmtInsert.setObject(7, cadm.getCriado_em());

                        stmtInsert.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao salvar cancelamento Adm em cadeia: " + e.getMessage());
            throw e;
        }
    }

    public void excluirEmLote(List<CancelamentoAdm> listaCadm) throws SQLException{
        String sql = """
        UPDATE cancelamento_adm 
        SET turno = ?, dia_inteiro = ?, motivo = ?, deletado_em = ?, adm_id = ?
        WHERE semestre_letivo_id = ? AND data = ? AND deletado_em IS NULL
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (CancelamentoAdm cadm: listaCadm){
                stmt.setString(1, cadm.getTurno());
                stmt.setBoolean(2, cadm.getDia_inteiro());
                stmt.setString(3, cadm.getMotivo());
                stmt.setObject(4, LocalDate.now());
                stmt.setInt(5, cadm.getAdm_id());
                stmt.setInt(6, cadm.getSemestre_letivo_id());
                stmt.setObject(7, cadm.getData());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Erro ao excluir (soft_delete) cadm em lote: " + e.getMessage());
            throw e;
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