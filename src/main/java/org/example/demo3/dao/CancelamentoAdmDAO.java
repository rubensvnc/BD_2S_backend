package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CancelamentoAdm;
import org.example.demo3.entity.Tema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CancelamentoAdmDAO{

    public List<CancelamentoAdm> listarCancelamentos(Integer semestre_letivo_id) throws SQLException{
        String sql = "SELECT * FROM cancelamento_adm WHERE semestre_letivo_id = ? ORDER BY criado_em ASC";
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
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }
}