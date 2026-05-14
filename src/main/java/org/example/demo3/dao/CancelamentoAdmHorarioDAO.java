package org.example.demo3.dao;


import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CancelamentoAdm;
import org.example.demo3.entity.CancelamentoAdmHorario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}