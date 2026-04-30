import java.sql.*;
import java.time.LocalDate;

public class VerificadorData {

    private static final String URL = "jdbc:mysql://localhost:3306/swiftplan";
    private static final String USER = "root";
    private static final String PASS = "";

    public static String verificarData(LocalDate data) {

        String sql = "SELECT 1 FROM datas_restritas WHERE data_bloqueio = ? AND ativo = 1";

        try (Connection con = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, data);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return "Sim, é uma data restrita";
            } else {
                return "Não, não é uma data restrita";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Erro ao verificar a data";
        }
    }
}