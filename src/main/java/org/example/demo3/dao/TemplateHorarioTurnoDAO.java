package org.example.demo3;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TemplateHorarioTurnoDAO {

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