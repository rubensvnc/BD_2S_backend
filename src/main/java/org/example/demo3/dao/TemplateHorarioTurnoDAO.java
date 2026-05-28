package org.example.demo3.dao;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.*;

public class TemplateHorarioTurnoDAO {

    public void deletarTemplateTurno(String turno) {
        String sql = """
                DELETE FROM template_horario_turno WHERE turno = ?
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setString(1, turno);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao deletar dados no banco: " + e.getMessage());
        }
    }

    public void salvarListaTemplate(List<TemplateHorarioTurno> listaTHT){
        String sql = """
                INSERT INTO template_horario_turno 
                    (turno, tipo, numero_ordem, hora_inicio, hora_fim) VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            for (TemplateHorarioTurno tht: listaTHT){
                ps.setString(1, tht.getTurno());
                ps.setString(2, tht.getTipo());
                ps.setInt(3, tht.getNumero_ordem());
                ps.setObject(4, tht.getHora_inicio());
                ps.setObject(5, tht.getHora_fim());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Erro ao inserir dados no banco: " + e.getMessage());
        }
    }

    public void salvar(TemplateHorarioTurno objeto) throws SQLException {
        String sql = "INSERT INTO template_horario_turno (turno, tipo, numero_ordem, hora_inicio, hora_fim) VALUES (?, ?, ?, ?, ?)";


        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, objeto.getTurno());
        stmt.setString(2, objeto.getTipo());
        stmt.setInt(3, objeto.getNumero_ordem());
        stmt.setTime(4, Time.valueOf(objeto.getHora_inicio()));
        stmt.setTime(5, Time.valueOf(objeto.getHora_fim()));

        stmt.execute();


        stmt.close();
        conn.close();
    }

    public List<TemplateHorarioTurno> listarPorTurno(String turno) throws SQLException {
        String sql = """
            SELECT * FROM template_horario_turno WHERE turno = ? ORDER BY numero_ordem ASC;
            """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<TemplateHorarioTurno> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, turno);

            rs = ps.executeQuery();
            while (rs.next()) {
                TemplateHorarioTurno tht = new TemplateHorarioTurno();
                tht.setId_template(rs.getInt("id_template"));
                tht.setTurno(rs.getString("turno"));
                tht.setTipo(rs.getString("tipo"));
                tht.setNumero_ordem(rs.getInt("numero_ordem"));
                tht.setHora_inicio(rs.getObject("hora_inicio", LocalTime.class));
                tht.setHora_fim(rs.getObject("hora_fim", LocalTime.class));

                lista.add(tht);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }

        return lista;
    }

    public List<TemplateHorarioTurno> listar() throws SQLException {
        String sql = "SELECT * FROM template_horario_turno";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<TemplateHorarioTurno> lista = new ArrayList<>();

        while (rs.next()) {
            TemplateHorarioTurno t = new TemplateHorarioTurno(
                    rs.getInt("id_template"),
                    rs.getString("turno"),
                    rs.getString("tipo"),
                    rs.getInt("numero_ordem"),
                    rs.getTime("hora_inicio").toLocalTime(),
                    rs.getTime("hora_fim").toLocalTime()
            );
            lista.add(t);
        }

        rs.close();
        stmt.close();
        conn.close();

        return lista;
    }
}