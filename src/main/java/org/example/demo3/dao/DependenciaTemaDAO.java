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

    // INSERIR DEPENDÊNCIA
    public void inserirDependencia(
            DependenciaTema dependencia
    ) {

        String sql = """
                INSERT INTO dependencia_tema (
                    tema_id,
                    tema_dependencia_id,
                    ordem
                )
                VALUES (?, ?, ?)
                """;

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    dependencia.getTema_id()
            );

            stmt.setInt(
                    2,
                    dependencia.getTema_dependencia_id()
            );

            stmt.setInt(
                    3,
                    dependencia.getOrdem()
            );

            stmt.executeUpdate();

            System.out.println(
                    "Dependência salva com sucesso!"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Erro ao inserir dependência: "
                            + e.getMessage()
            );
        }
    }

    // REMOVER TODAS AS DEPENDÊNCIAS DE UM TEMA
    public void removerDependenciasTema(
            Integer temaId
    ) {

        String sql = """
                DELETE FROM dependencia_tema
                WHERE tema_id = ?
                """;

        try (
                Connection conn =
                        DatabaseConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, temaId);

            stmt.executeUpdate();

            System.out.println(
                    "Dependências removidas!"
            );

        } catch (SQLException e) {

            System.out.println(
                    "Erro ao remover dependências: "
                            + e.getMessage()
            );
        }
    }
}

