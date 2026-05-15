package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Tema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TemaDAO {
    private Connection connection;
        //FAZ A CONEXÃO;
        public TemaDAO() {
            this.connection = DatabaseConnection.getConnection();
        }

        //INSERE UM TEMA
        public void inserirTema(Tema tema) {
            String sql = """
                INSERT INTO tema (
                    disciplina_id,
                    semestre_letivo_id,
                    nome,
                    eh_avaliacao,
                    qtd_min_aulas,
                    qtd_max_aulas,
                    prioridade,
                    eh_opcional) VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";
            try(PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, tema.getDisciplina_id());
                stmt.setInt(2, tema.getSemestre_letivo_id());
                stmt.setString(3, tema.getNome());
                stmt.setInt(4, tema.getEh_avaliacao());
                stmt.setInt(5, tema.getQtd_min_aulas());
                stmt.setInt(6, tema.getQtd_max_aulas());
                stmt.setInt(7, tema.getPrioridade());
                stmt.setInt(8, tema.getEh_opcional());

                stmt.executeUpdate();

                System.out.println("Tema inserido com sucesso!");

            }catch (SQLException e) {
                System.out.println("Erro ao inserir tema: " + e.getMessage());
            }
        }
        //LISTA TODOS OS TEMAS
        public List<Tema> listarTemas() {
                List<Tema> temas = new ArrayList<>();
                String sql = """
                    SELECT * FROM tema
                    WHERE deletado_em IS NULL
                    """;
                try (PreparedStatement stmt = connection.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {

                        Tema tema = new Tema();

                        tema.setId_tema(rs.getInt("id_tema"));
                        tema.setDisciplina_id(rs.getInt("disciplina_id"));
                        tema.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                        tema.setNome(rs.getString("nome"));
                        tema.setEh_avaliacao(rs.getInt("eh_avaliacao"));
                        tema.setQtd_min_aulas(rs.getInt("qtd_min_aulas"));
                        tema.setQtd_max_aulas(rs.getInt("qtd_max_aulas"));
                        tema.setPrioridade(rs.getInt("prioridade"));
                        tema.setEh_opcional(rs.getInt("eh_opcional"));

                        temas.add(tema);
                    }
                }catch (SQLException e) {
                    System.out.println("Erro ao listar temas: " + e.getMessage());
                }

                return temas;
        }


    //EDITA TEMA
    public void editarTema(Tema tema) {

        String sql = """
        UPDATE tema
        SET
            disciplina_id = ?,
            semestre_letivo_id = ?,
            nome = ?,
            eh_avaliacao = ?,
            qtd_min_aulas = ?,
            qtd_max_aulas = ?,
            prioridade = ?,
            eh_opcional = ?
        WHERE id_tema = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, tema.getDisciplina_id());
            stmt.setInt(2, tema.getSemestre_letivo_id());
            stmt.setString(3, tema.getNome());
            stmt.setInt(4, tema.getEh_avaliacao());
            stmt.setInt(5, tema.getQtd_min_aulas());
            stmt.setInt(6, tema.getQtd_max_aulas());
            stmt.setInt(7, tema.getPrioridade());
            stmt.setInt(8, tema.getEh_opcional());

            stmt.setInt(9, tema.getId_tema());

            stmt.executeUpdate();

            System.out.println("Tema editado com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao editar tema: " + e.getMessage());
        }
    }

    //EXCLUÍ TEMA
    public void excluirTema(Integer idTema) {

        String sql = """
        UPDATE tema
        SET deletado_em = CURRENT_DATE
        WHERE id_tema = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idTema);

            stmt.executeUpdate();

            System.out.println("Tema excluído com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao excluir tema: " + e.getMessage());
        }
    }


}