package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.dto.AdmCursoExibicao;
import org.example.demo3.entity.Tema;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TemaDAO {

        //INSERE UM TEMA
    public Integer inserirTemaRetornandoId(Tema tema) {
        String sql = "INSERT INTO tema (nome, qtd_min_aulas, qtd_max_aulas, " +
                "prioridade, eh_avaliacao, eh_opcional, disciplina_id, semestre_letivo_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, tema.getNome());
            ps.setInt(2, tema.getQtd_min_aulas());
            ps.setInt(3, tema.getQtd_max_aulas());
            ps.setInt(4, tema.getPrioridade());
            ps.setInt(5, tema.getEh_avaliacao());
            ps.setInt(6, tema.getEh_opcional());
            ps.setInt(7, tema.getDisciplina_id());
            ps.setInt(8, tema.getSemestre_letivo_id());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

        //LISTA TODOS OS TEMAS
    public List<Tema> listarTemas() {
                List<Tema> temas = new ArrayList<>();
                String sql = """
                    SELECT * FROM tema
                    WHERE deletado_em IS NULL
                    """;
        try ( Connection connection = DatabaseConnection.getConnection();
              PreparedStatement stmt = connection.prepareStatement(sql);
              ResultSet rs = stmt.executeQuery() ) {
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

        //LISTA OS TEMAS POR DISCIPLINA E SEMESTRE
        public List<Tema> listarTemasPorDisciplinaESemestre(int idDisciplina, int idSemestre) {
            List<Tema> lista = new ArrayList<>();
            String sql = "SELECT * FROM tema WHERE disciplina_id = ? AND semestre_letivo_id = ? AND deletado_em IS NULL ORDER BY prioridade ASC";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, idDisciplina);
                stmt.setInt(2, idSemestre);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Tema t = new Tema();
                        t.setId_tema(rs.getInt("id_tema"));
                        t.setNome(rs.getString("nome"));
                        t.setQtd_min_aulas(rs.getInt("qtd_min_aulas"));
                        t.setQtd_max_aulas(rs.getInt("qtd_max_aulas"));
                        t.setPrioridade(rs.getInt("prioridade"));
                        t.setEh_avaliacao(rs.getInt("eh_avaliacao"));
                        t.setEh_opcional(rs.getInt("eh_opcional"));
                        t.setDisciplina_id(rs.getInt("disciplina_id"));
                        t.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                        lista.add(t);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return lista;
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

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement stmt =
                        connection.prepareStatement(sql)
        ) {

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

            System.out.println(
                    "Erro ao editar tema: "
                            + e.getMessage()
            );
        }
    }

    public Tema buscarPorId(Integer idTema) throws SQLException{
        String sql = """
            SELECT * FROM tema WHERE id_tema = ?;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTema);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tema tema = new Tema(
                        rs.getInt("id_tema"),
                        rs.getInt("disciplina_id"),
                        rs.getInt("semestre_letivo_id"),
                        rs.getString("nome"),
                        rs.getInt("eh_avaliacao"),
                        rs.getInt("qtd_min_aulas"),
                        rs.getInt("qtd_max_aulas"),
                        rs.getInt("prioridade"),
                        rs.getInt("eh_opcional"),
                        rs.getObject("deletado_em", LocalDate.class)
                    );
                    return tema;
                }
            }
        }
        return null;
    }

    //EXCLUÍ TEMA
    public void excluirTema(Integer idTema) {

        String sql = """
        UPDATE tema
        SET deletado_em = CURRENT_DATE
        WHERE id_tema = ?
        """;

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement stmt =
                        connection.prepareStatement(sql)
        ) {

            stmt.setInt(1, idTema);

            stmt.executeUpdate();

            System.out.println("Tema excluído com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao excluir tema: " + e.getMessage());
        }
    }


}