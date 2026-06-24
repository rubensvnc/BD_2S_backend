package org.example.demo3.dao;


import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CancelamentoAdm;
import org.example.demo3.entity.CancelamentoAdmHorario;
import org.example.demo3.entity.DataBloqueada;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CancelamentoAdmHorarioDAO {

    public List<CancelamentoAdmHorario> listarHorariosDeCancelamento(Integer cancelamento_adm_id) throws SQLException {
        String sql = "SELECT * FROM cancelamento_adm_horario WHERE cancelamento_adm_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<CancelamentoAdmHorario> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cancelamento_adm_id);

            rs = ps.executeQuery();
            while (rs.next()) {
                CancelamentoAdmHorario c = new CancelamentoAdmHorario();
                c.setId_cancelamento_adm_horario(rs.getInt("id_cancelamento_adm_horario"));
                c.setCancelamento_adm_id(rs.getInt("cancelamento_adm_id"));
                c.setHorario_curso_id(rs.getInt("horario_curso_id"));
                lista.add(c);
            }

        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }

    public List<CancelamentoAdmHorario> listarTodosHorariosDoSemestre(Integer slId){
        String sql = "SELECT cadmh.* FROM cancelamento_adm_horario cadmh " +
                "INNER JOIN cancelamento_adm cadm ON cadm.id_cancelamento_adm = cancelamento_adm_id " +
                "WHERE cadm.semestre_letivo_id = ?";

        List<CancelamentoAdmHorario> listCadmH = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, slId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                CancelamentoAdmHorario cadmH = new CancelamentoAdmHorario(
                        rs.getInt("id_cancelamento_adm_horario"),
                        rs.getInt("cancelamento_adm_id"),
                        rs.getInt("horario_curso_id")
                );
                listCadmH.add(cadmH);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar horarios datas bloqueadas: " + e.getMessage());
        }
        return listCadmH;
    }

    public List<CancelamentoAdmHorario> listarHorariosDeCancelamentos(List<CancelamentoAdm> listCadm){
        String sql = """
            SELECT * FROM cancelamento_adm_horario
             WHERE cancelamento_adm_id = ?
        """;

        List<CancelamentoAdmHorario> listCadmH = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (CancelamentoAdm cadm : listCadm){
                stmt.setInt(1, cadm.getId_cancelamento_adm());

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    CancelamentoAdmHorario cadmH = new CancelamentoAdmHorario(
                            rs.getInt("id_cancelamento_adm_horario"),
                            rs.getInt("cancelamento_adm_id"),
                            rs.getInt("horario_curso_id")
                    );
                    listCadmH.add(cadmH);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar horarios cadmh 'listarHorariosDeCancelamentos': " + e.getMessage());
        }
        return listCadmH;
    }

    public void salvarEmLote(List<CancelamentoAdmHorario> listaCadmH){
        String sql = "INSERT INTO cancelamento_adm_horario " +
                "(cancelamento_adm_id, horario_curso_id) " +
                "VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (CancelamentoAdmHorario cadmH: listaCadmH){
                stmt.setInt(1, cadmH.getCancelamento_adm_id());
                stmt.setInt(2, cadmH.getHorario_curso_id());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("\n\nErro ao salvar 'CadmH - salvarEmLote': " + e.getMessage() + "\n\n");
        }
    }
}