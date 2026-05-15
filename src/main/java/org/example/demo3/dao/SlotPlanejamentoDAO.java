package org.example.demo3;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SlotPlanejamentoDAO {

    public void salvar(SlotPlanejamento slot) throws SQLException {

        Connection conn = DatabaseConnection.getConnection();


        String sql = "INSERT INTO slot_planejamento (planejamento_id, data, horario_curso_id, tema_id, status, motivo_cancelamento, cancelamento_adm_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);


        stmt.setInt(1, slot.getPlanejamento_id());
        stmt.setDate(2, Date.valueOf(slot.getData()));
        stmt.setInt(3, slot.getHorario_curso_id());
        stmt.setInt(4, slot.getTema_id());
        stmt.setString(5, slot.getStatus());


        stmt.setString(6, slot.getMotivo_cancelamento());

        if (slot.getCancelamento_adm_id() != null) {
            stmt.setInt(7, slot.getCancelamento_adm_id());
        } else {
            stmt.setNull(7, Types.INTEGER);
        }


        stmt.execute();


        stmt.close();
        conn.close();
    }

    public List<SlotPlanejamento> listar() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String sql = "SELECT * FROM slot_planejamento";
        PreparedStatement stmt = conn.prepareStatement(sql);


        ResultSet rs = stmt.executeQuery();

        List<SlotPlanejamento> lista = new ArrayList<>();

        while (rs.next()) {
            SlotPlanejamento s = new SlotPlanejamento();


            s.setId_slot_planejamento(rs.getInt("id_slot_planejamento"));
            s.setPlanejamento_id(rs.getInt("planejamento_id"));
            s.setData(rs.getDate("data").toLocalDate());
            s.setHorario_curso_id(rs.getInt("horario_curso_id"));
            s.setTema_id(rs.getInt("tema_id"));
            s.setStatus(rs.getString("status"));
            s.setMotivo_cancelamento(rs.getString("motivo_cancelamento"));


            int cancelId = rs.getInt("cancelamento_adm_id");
            if (rs.wasNull()) {
                s.setCancelamento_adm_id(null);
            } else {
                s.setCancelamento_adm_id(cancelId);
            }

            lista.add(s);
        }


        rs.close();
        stmt.close();
        conn.close();

        return lista;
    }
}