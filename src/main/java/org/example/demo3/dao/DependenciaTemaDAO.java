package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.DependenciaTema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DependenciaTemaDAO {

    // LISTAR DEPENDÊNCIAS DE UM TEMA
    public List<DependenciaTema> listarDependenciasTema(
            Integer tema_id
    ) {

        String sql = """
                SELECT *
                FROM dependencia_tema
                WHERE tema_id = ?
                ORDER BY ordem
                """;

        List<DependenciaTema> lista =
                new ArrayList<>();

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, tema_id);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                DependenciaTema d =
                        new DependenciaTema();

                d.setId_dependencia_tema(
                        rs.getInt(
                                "id_dependencia_tema"
                        )
                );

                d.setTema_id(
                        rs.getInt("tema_id")
                );

                d.setTema_dependencia_id(
                        rs.getInt(
                                "tema_dependencia_id"
                        )
                );

                d.setOrdem(
                        rs.getInt("ordem")
                );

                lista.add(d);
            }

        } catch (SQLException e) {

            System.out.println(
                    "Erro ao listar dependências: "
                            + e.getMessage()
            );
        }

        return lista;
    }

    // SALVAR DEPENDÊNCIAS (remove antigas e insere novas em uma única transação)
    public void salvarDependencias(Integer temaId, List<DependenciaTema> dependencias) {

        String sqlDelete = """
            DELETE FROM dependencia_tema
            WHERE tema_id = ?
            """;

        String sqlInsert = """
            INSERT INTO dependencia_tema (tema_id, tema_dependencia_id, ordem)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false); // inicia transação

            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
                stmtDelete.setInt(1, temaId);
                stmtDelete.executeUpdate();
            }

            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                for (DependenciaTema dep : dependencias) {
                    stmtInsert.setInt(1, dep.getTema_id());
                    stmtInsert.setInt(2, dep.getTema_dependencia_id());
                    stmtInsert.setInt(3, dep.getOrdem());
                    stmtInsert.addBatch(); // acumula para executar de uma vez
                }
                stmtInsert.executeBatch();
            }

            conn.commit(); // confirma tudo junto
            System.out.println("Dependências salvas com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao salvar dependências: " + e.getMessage());
            // o AutoCommit volta ao padrão ao fechar a conexão, revertendo a transação
        }
    }

    // DELETA TODAS AS DEPENDÊNCIAS DE UM TEMA
    public void deletarDependenciasPorTema(Integer temaId) {
        String sql = "DELETE FROM dependencia_tema WHERE tema_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, temaId);
            stmt.executeUpdate();
            System.out.println("Dependências do tema " + temaId + " removidas.");

        } catch (SQLException e) {
            System.out.println("Erro ao deletar dependências: " + e.getMessage());
        }
    }

}

