package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.SemestreLetivo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    private Connection connection;

    public DisciplinaDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Disciplina> listarDisciplinasCurso(int professorId, int ano, int semestreAno, String cursoNome) throws SQLException {
        String sql = """
            SELECT DISTINCT d.id_disciplina, d.semestre_curso, d.nome FROM atribuicao_professor 
            AS ap INNER JOIN semestre_letivo AS sl ON sl.id_semestre_letivo = ap.semestre_letivo_id 
            INNER JOIN disciplina AS d ON ap.disciplina_id = d.id_disciplina 
            INNER JOIN curso AS c ON d.curso_id = c.id_curso WHERE ap.professor_id = ? 
            AND sl.ano = ? AND sl.numero_semestre = ? AND c.nome = ?
        """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Disciplina> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, professorId);
            ps.setInt(2, ano);
            ps.setInt(3, semestreAno);
            ps.setString(4, cursoNome);

            rs = ps.executeQuery();
            while (rs.next()) {
                Disciplina d = new Disciplina();
                d.setSemestre_curso(rs.getInt("d.semestre_curso"));
                d.setNome(rs.getString("d.nome"));
                d.setId_disciplina(rs.getInt("id_disciplina"));

                lista.add(d);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }

        return lista;
    }

    public void inserirDisciplina(Disciplina disciplina) {

        String sql = """
            INSERT INTO disciplina (
                curso_id,
                nome,
                semestre_curso,
                carga_horaria_minima,
                deletado_em
            ) VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, disciplina.getCurso_id());
            stmt.setString(2, disciplina.getNome());
            stmt.setInt(3, disciplina.getSemestre_curso());
            stmt.setInt(4, disciplina.getCarga_horaria_minima());

            if (disciplina.getDeletado_em() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(disciplina.getDeletado_em()));
            } else {
                stmt.setTimestamp(5, null);
            }

            stmt.executeUpdate();

            System.out.println("Disciplina inserida com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inserir disciplina: " + e.getMessage());
        }
    }

    public List<Disciplina> listarDisciplinas() {

        List<Disciplina> disciplinas = new ArrayList<>();

        String sql = """
            SELECT *
            FROM disciplina
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Disciplina disciplina = new Disciplina();

                disciplina.setId_disciplina(rs.getInt("id_disciplina"));
                disciplina.setCurso_id(rs.getInt("curso_id"));
                disciplina.setNome(rs.getString("nome"));
                disciplina.setSemestre_curso(rs.getInt("semestre_curso"));
                disciplina.setCarga_horaria_minima(rs.getInt("carga_horaria_minima"));

                Timestamp deletadoEm = rs.getTimestamp("deletado_em");

                if (deletadoEm != null) {
                    disciplina.setDeletado_em(deletadoEm.toLocalDateTime());
                }

                disciplinas.add(disciplina);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar disciplinas: " + e.getMessage());
        }

        return disciplinas;
    }

    public void atualizarDisciplina(Disciplina disciplina) {

        String sql = """
            UPDATE disciplina
            SET curso_id = ?,
                nome = ?,
                semestre_curso = ?,
                carga_horaria_minima = ?,
                deletado_em = ?
            WHERE id_disciplina = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, disciplina.getCurso_id());
            stmt.setString(2, disciplina.getNome());
            stmt.setInt(3, disciplina.getSemestre_curso());
            stmt.setInt(4, disciplina.getCarga_horaria_minima());

            if (disciplina.getDeletado_em() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(disciplina.getDeletado_em()));
            } else {
                stmt.setTimestamp(5, null);
            }

            stmt.setInt(6, disciplina.getId_disciplina());

            stmt.executeUpdate();

            System.out.println("Disciplina atualizada com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar disciplina: " + e.getMessage());
        }
    }

    public void excluirDisciplina(Integer idDisciplina) {

        String sql = """
            DELETE FROM disciplina
            WHERE id_disciplina = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idDisciplina);

            stmt.executeUpdate();

            System.out.println("Disciplina removida com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao remover disciplina: " + e.getMessage());
        }
    }
}