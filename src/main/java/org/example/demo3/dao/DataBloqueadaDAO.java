import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBloqueadaDAO {


    public void salvar(DataBloqueada db) throws Exception {

        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/seu_banco", "usuario", "senha");

        String sql = "INSERT INTO data_bloqueada (semestre_letivo_id, data, motivo, adm_id, recorrente) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, db.getSemestreLetivoId());
        stmt.setDate(2, Date.valueOf(db.getData())); // Transforma a data do Java para o SQL
        stmt.setString(3, db.getMotivo());
        stmt.setInt(4, db.getAdmId());
        stmt.setBoolean(5, db.isRecorrente());

        stmt.execute();
        stmt.close();
        conn.close();
    }


    public List<DataBloqueada> listarTodos() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/seu_banco", "usuario", "senha");

        String sql = "SELECT * FROM data_bloqueada";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        List<DataBloqueada> lista = new ArrayList<>();

        while (rs.next()) {
            DataBloqueada db = new DataBloqueada();
            db.setIdDataBloqueada(rs.getInt("id_data_bloqueada"));
            db.setSemestreLetivoId(rs.getInt("semestre_letivo_id"));
            db.setData(rs.getDate("data").toLocalDate());
            db.setMotivo(rs.getString("motivo"));
            db.setAdmId(rs.getInt("adm_id"));
            db.setRecorrente(rs.getBoolean("recorrente"));

            lista.add(db);
        }

        rs.close();
        stmt.close();
        conn.close();

        return lista;
    }


    public void excluir(int id) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/seu_banco", "usuario", "senha");

        String sql = "DELETE FROM data_bloqueada WHERE id_data_bloqueada = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);

        stmt.execute();
        stmt.close();
        conn.close();
    }
}