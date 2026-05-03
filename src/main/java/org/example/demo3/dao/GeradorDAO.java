package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CronogramaItem;
import org.example.demo3.entity.Disciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GeradorDAO {

    public List<CronogramaItem> listarItensPorCronograma(int cronogramaId) throws SQLException {
        String sql = "SELECT * FROM cronograma_item WHERE cronograma_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<CronogramaItem> lista = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, cronogramaId);
            rs = ps.executeQuery();

            while (rs.next()) {
                CronogramaItem item = new CronogramaItem();
                item.setId_item(rs.getInt("id_item"));
                item.setCronograma_id(rs.getInt("cronograma_id"));
                item.setTema_id(rs.getInt("tema_id"));
                item.setData_prevista(rs.getDate("data_prevista").toLocalDate());
                item.setQtd_aulas(rs.getInt("qtd_aulas"));
                item.setStatus_aula(rs.getString("status_aula"));
                lista.add(item);
            }
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }

    public Disciplina buscarDisciplinaPorId(int id) throws SQLException {
        String sql = "SELECT * FROM disciplina WHERE id_disciplina = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return new Disciplina(
                        rs.getInt("id_disciplina"),
                        rs.getInt("curso_id"),
                        rs.getString("nome"),
                        rs.getInt("carga_horaria"),
                        rs.getInt("semestre")
                );
            }
        } finally {
            DatabaseConnection.closeConnection();
        }
        return null;
    }
}