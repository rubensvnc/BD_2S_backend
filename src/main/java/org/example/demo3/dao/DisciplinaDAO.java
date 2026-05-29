package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Disciplina;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {


    // INSERÇÃO (SEM RETORNO DO ID)
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

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, disciplina.getCurso_id());
            stmt.setString(2, disciplina.getNome());
            stmt.setInt(3, disciplina.getSemestre_curso());
            stmt.setInt(4, disciplina.getCarga_horaria_minima());
            stmt.setTimestamp(5, disciplina.getDeletado_em() != null
                    ? Timestamp.valueOf(disciplina.getDeletado_em()) : null);

            stmt.executeUpdate();
            System.out.println("Disciplina inserida com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inserir disciplina: " + e.getMessage());
        }
    }


    // INSERÇÃO (RETORNA O ID DA DISCIPLINA INSERIDA)
    public Integer inserirDisciplinaRetornandoId(Disciplina disciplina) {
        String sql = """
            INSERT INTO disciplina (
                curso_id,
                nome,
                semestre_curso,
                carga_horaria_minima,
                deletado_em
            ) VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, disciplina.getCurso_id());
            stmt.setString(2, disciplina.getNome());
            stmt.setInt(3, disciplina.getSemestre_curso());
            stmt.setInt(4, disciplina.getCarga_horaria_minima());
            stmt.setTimestamp(5, disciplina.getDeletado_em() != null
                    ? Timestamp.valueOf(disciplina.getDeletado_em()) : null);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao inserir disciplina: " + e.getMessage());
        }
        return null;
    }

    // LISTA AS DISCIPLINAS
    public List<Disciplina> listarDisciplinas() {
        List<Disciplina> disciplinas = new ArrayList<>();
        String sql = "SELECT * FROM disciplina WHERE deletado_em IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Disciplina d = new Disciplina();
                d.setId_disciplina(rs.getInt("id_disciplina"));
                d.setCurso_id(rs.getInt("curso_id"));
                d.setNome(rs.getString("nome"));
                d.setSemestre_curso(rs.getInt("semestre_curso"));
                d.setCarga_horaria_minima(rs.getInt("carga_horaria_minima"));

                Timestamp deletadoEm = rs.getTimestamp("deletado_em");
                if (deletadoEm != null) d.setDeletado_em(deletadoEm.toLocalDateTime());

                disciplinas.add(d);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar disciplinas: " + e.getMessage());
        }

        return disciplinas;
    }


    // LISTAGEM — disciplinas de um curso/professor/semestre específico
    public List<Disciplina> listarDisciplinasCurso(int professorId, int ano, int semestreAno, Integer id_curso) throws SQLException {
        String sql = """
            SELECT DISTINCT d.id_disciplina, d.semestre_curso, d.nome
            FROM atribuicao_professor AS ap
            INNER JOIN semestre_letivo AS sl ON sl.id_semestre_letivo = ap.semestre_letivo_id
            INNER JOIN disciplina AS d ON ap.disciplina_id = d.id_disciplina
            INNER JOIN curso AS c ON d.curso_id = c.id_curso
            WHERE ap.professor_id = ?
              AND sl.ano = ?
              AND sl.numero_semestre = ?
              AND c.id_curso = ?
            """;

        List<Disciplina> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, professorId);
            ps.setInt(2, ano);
            ps.setInt(3, semestreAno);
            ps.setInt(4, id_curso);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Disciplina d = new Disciplina();
                    d.setId_disciplina(rs.getInt("id_disciplina"));
                    d.setSemestre_curso(rs.getInt("semestre_curso"));
                    d.setNome(rs.getString("nome"));
                    lista.add(d);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar disciplinas do curso: " + e.getMessage());
            throw e;
        }

        return lista;
    }

    // ATUALIZAÇÃO
    public void atualizarDisciplina(Disciplina disciplina) {
        String sql = """
            UPDATE disciplina
            SET curso_id             = ?,
                nome                 = ?,
                semestre_curso       = ?,
                carga_horaria_minima = ?,
                deletado_em          = ?
            WHERE id_disciplina = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, disciplina.getCurso_id());
            stmt.setString(2, disciplina.getNome());
            stmt.setInt(3, disciplina.getSemestre_curso());
            stmt.setInt(4, disciplina.getCarga_horaria_minima());
            stmt.setTimestamp(5, disciplina.getDeletado_em() != null
                    ? Timestamp.valueOf(disciplina.getDeletado_em()) : null);
            stmt.setInt(6, disciplina.getId_disciplina());

            stmt.executeUpdate();
            System.out.println("Disciplina updated com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar disciplina: " + e.getMessage());
        }
    }

    // EXCLUSÃO — soft delete
    public void excluirDisciplina(Integer idDisciplina) {
        String sql = """
            UPDATE disciplina
            SET deletado_em = CURRENT_DATE
            WHERE id_disciplina = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDisciplina);
            stmt.executeUpdate();
            System.out.println("Disciplina removida com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao remover disciplina: " + e.getMessage());
        }
    }


    public int descobrirIdDisciplinaPorNome(String nomeDisciplina) throws SQLException {
        String sql = "SELECT id_disciplina FROM disciplina WHERE nome = ? AND deletado_em IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nomeDisciplina);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_disciplina");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao descobrir ID da disciplina por nome: " + e.getMessage());
            throw e;
        }
        return 0;
    }

    public List<Disciplina> listarDisciplinasPorCurso(Integer cursoId) throws SQLException {
        String sql = """
        SELECT id_disciplina, curso_id, nome, semestre_curso, carga_horaria_minima
        FROM disciplina
        WHERE curso_id = ? AND deletado_em IS NULL
        ORDER BY semestre_curso, nome
        """;

        List<Disciplina> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cursoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Disciplina d = new Disciplina();
                    d.setId_disciplina(rs.getInt("id_disciplina"));
                    d.setCurso_id(rs.getInt("curso_id"));
                    d.setNome(rs.getString("nome"));
                    d.setSemestre_curso(rs.getInt("semestre_curso"));
                    d.setCarga_horaria_minima(rs.getInt("carga_horaria_minima"));
                    lista.add(d);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar disciplinas por curso: " + e.getMessage());
            throw e;
        }

        return lista;
    }


}