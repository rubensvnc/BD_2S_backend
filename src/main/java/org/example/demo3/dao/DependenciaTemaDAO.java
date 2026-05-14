package org.example.demo3.dao;


import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CancelamentoAdmHorario;
import org.example.demo3.entity.DependenciaTema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DependenciaTemaDAO {

    public List<DependenciaTema> listarDependenciasTema(Integer tema_id) throws SQLException {
        String sql = "SELECT * FROM dependencia_tema WHERE tema_id = ? ORDER BY ordem";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<DependenciaTema> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, tema_id);

            rs = ps.executeQuery();
            while (rs.next()){
                DependenciaTema d = new DependenciaTema();
                d.setId_dependencia_tema(rs.getInt("id_dependencia_tema"));
                d.setTema_id(rs.getInt("tema_id"));
                d.setTema_dependencia_id(rs.getInt("tema_dependencia_id"));
                d.setOrdem(rs.getInt("ordem"));
                lista.add(d);
            }
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }
}