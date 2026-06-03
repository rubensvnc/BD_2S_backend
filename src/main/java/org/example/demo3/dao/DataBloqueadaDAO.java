package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.CancelamentoAdm;
import org.example.demo3.entity.DataBloqueada;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataBloqueadaDAO {

    public void salvar(DataBloqueada db) throws SQLException {
        String sql = "INSERT INTO data_bloqueada (semestre_letivo_id, data, motivo, adm_id, recorrente) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, db.getSemestreLetivoId());
            stmt.setDate(2, Date.valueOf(db.getData()));
            stmt.setString(3, db.getMotivo());
            stmt.setInt(4, db.getAdmId());
            stmt.setBoolean(5, db.isRecorrente());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar data bloqueada: " + e.getMessage());
            throw e;
        }
    }

    public void salvarEmLote(List<DataBloqueada> listaDatas) throws SQLException{
        String sql = "INSERT INTO data_bloqueada (semestre_letivo_id, data, motivo, adm_id, recorrente) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (DataBloqueada db: listaDatas){
                stmt.setInt(1, db.getSemestreLetivoId());
                stmt.setDate(2, Date.valueOf(db.getData()));
                stmt.setString(3, db.getMotivo());
                stmt.setInt(4, db.getAdmId());
                stmt.setBoolean(5, db.isRecorrente());

                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Erro ao salvar data bloqueada em cadeia: " + e.getMessage());
            throw e;
        }
    }

    public List<DataBloqueada> listarTodos() throws SQLException {
        String sql = "SELECT * FROM data_bloqueada ORDER BY data ASC";
        List<DataBloqueada> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DataBloqueada db = new DataBloqueada();
                // ATENÇÃO: Verifique se na sua Entidade o método é 'setIdDataBloqueada' ou 'setId_data_bloqueada'
                db.setIdDataBloqueada(rs.getInt("id_data_bloqueada"));
                db.setSemestreLetivoId(rs.getInt("semestre_letivo_id"));
                db.setData(rs.getDate("data").toLocalDate());
                db.setMotivo(rs.getString("motivo"));
                db.setAdmId(rs.getInt("adm_id"));
                db.setRecorrente(rs.getBoolean("recorrente"));

                lista.add(db);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar datas bloqueadas: " + e.getMessage());
            throw e;
        }
        return lista;
    }

    public List<LocalDate> listarDatasBloqueadasPorSemestre (Integer sl){
        String sql = "SELECT data FROM data_bloqueada WHERE semestre_letivo_id = ?";

        List<LocalDate> datas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sl);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datas.add(rs.getObject("data", LocalDate.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datas;
    }


    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM data_bloqueada WHERE id_data_bloqueada = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao excluir data bloqueada: " + e.getMessage());
            throw e;
        }
    }
}