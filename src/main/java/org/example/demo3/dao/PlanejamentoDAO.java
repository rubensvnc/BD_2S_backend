package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Planejamento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanejamentoDAO {

    private Connection connection;

    public PlanejamentoDAO() {
        this.connection = DatabaseConnection.getConnection();
    }


    public void inserirPlanejamento(Planejamento planejamento) {

        String sql = """
                INSERT INTO planejamento
                (atribuicao_professor_id, gerado_em)
                VALUES (?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, planejamento.getAtribuicao_professor_id());
            stmt.setTimestamp(2, Timestamp.valueOf(planejamento.getGerado_em()));

            stmt.executeUpdate();

            System.out.println("Planejamento inserido com sucesso!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<Planejamento> listarPlanejamentos() {

        List<Planejamento> lista = new ArrayList<>();

        String sql = "SELECT * FROM planejamento";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Planejamento planejamento = new Planejamento();

                planejamento.setId_planejamento(rs.getInt("id_planejamento"));
                planejamento.setAtribuicao_professor_id(rs.getInt("atribuicao_professor_id"));
                planejamento.setGerado_em(rs.getTimestamp("gerado_em").toLocalDateTime());

                lista.add(planejamento);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    public Planejamento buscarPorId(Integer id) {

        String sql = "SELECT * FROM planejamento WHERE id_planejamento = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                Planejamento planejamento = new Planejamento();

                planejamento.setId_planejamento(rs.getInt("id_planejamento"));
                planejamento.setAtribuicao_professor_id(rs.getInt("atribuicao_professor_id"));
                planejamento.setGerado_em(rs.getTimestamp("gerado_em").toLocalDateTime());

                return planejamento;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void atualizarPlanejamento(Planejamento planejamento) {

        String sql = """
                UPDATE planejamento
                SET atribuicao_professor_id = ?,
                    gerado_em = ?
                WHERE id_planejamento = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, planejamento.getAtribuicao_professor_id());
            stmt.setTimestamp(2, Timestamp.valueOf(planejamento.getGerado_em()));
            stmt.setInt(3, planejamento.getId_planejamento());

            stmt.executeUpdate();

            System.out.println("Planejamento atualizado!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deletarPlanejamento(Integer id) {

        String sql = "DELETE FROM planejamento WHERE id_planejamento = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

            System.out.println("Planejamento deletado!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}