import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.PlanejamentoDia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public boolean DataRestritaDao(LocalDate data, int idProfessor) {
    String sql = "SELECT COUNT(*) FROM (" +
            "  SELECT 1 FROM feriado WHERE dia = ? AND mes = ? " +
            "  UNION ALL " +
            "  SELECT 1 FROM restricao_geral WHERE ? BETWEEN data_inicio AND data_fim " +
            "  UNION ALL " +
            "  SELECT 1 FROM restricao_professor WHERE data_restricao = ? AND professor_id = ?" +
            ") AS restricoes";

    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
        stmt.setInt(1, data.getDayOfMonth());
        stmt.setInt(2, data.getMonthValue());
        stmt.setDate(3, Date.valueOf(data));
        stmt.setDate(4, Date.valueOf(data));
        stmt.setInt(5, idProfessor);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

void main() {
}