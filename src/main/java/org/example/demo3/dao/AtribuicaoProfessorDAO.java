package.org.example.demo3.dao;

import.org.example.demo3.DatabaseConnection;
import.org.example.demo3.entity.AtribuicaoProfessor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AtribuicaoProfessorDAO {

    public void salvar(AtribuicaoProfessor atribuicao) throws SQLException {
        String sql = """
                INSERT INTO atribuicao_professor (disciplina_id, professor_id, semestre_letivo_id)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, atribuicao.getDisciplina_id());
            ps.setInt(2, atribuicao.getProfessor_id());
            ps.setInt(3, atribuicao.getSemestre_letivo_id());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    atribuicao.setId_atribuicao_professor(rs.getInt(1));
                }
            }
        }
    }

    public AtribuicaoProfessor buscarPorDisciplinaESemestre(int disciplinaId, int semestreLetivoId)
            throws SQLException {

        String sql = """
                SELECT id_atribuicao_professor, disciplina_id, professor_id, semestre_letivo_id
                FROM atribuicao_professor
                WHERE disciplina_id = ? AND semestre_letivo_id = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, disciplinaId);
            ps.setInt(2, semestreLetivoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public AtribuicaoProfessor buscarPorId(int idAtribuicao) throws SQLException {
        String sql = """
                SELECT id_atribuicao_professor, disciplina_id, professor_id, semestre_letivo_id
                FROM atribuicao_professor
                WHERE id_atribuicao_professor = ?
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAtribuicao);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    public List<AtribuicaoProfessor> listarPorProfessorESemestre(int professorId, int semestreLetivoId)
            throws SQLException {

        String sql = """
                SELECT id_atribuicao_professor, disciplina_id, professor_id, semestre_letivo_id
                FROM atribuicao_professor
                WHERE professor_id = ? AND semestre_letivo_id = ?
                ORDER BY disciplina_id
                """;

        return executarListagem(sql, professorId, semestreLetivoId);
    }

    public List<AtribuicaoProfessor> listarPorCursoESemestre(int cursoId, int semestreLetivoId)
            throws SQLException {

        String sql = """
                SELECT ap.id_atribuicao_professor,
                       ap.disciplina_id,
                       ap.professor_id,
                       ap.semestre_letivo_id
                FROM atribuicao_professor ap
                INNER JOIN disciplina d ON d.id_disciplina = ap.disciplina_id
                WHERE d.curso_id = ?
                  AND ap.semestre_letivo_id = ?
                  AND d.deletado_em IS NULL
                ORDER BY d.nome
                """;

        return executarListagem(sql, cursoId, semestreLetivoId);
    }

    public List<AtribuicaoProfessor> listarTodosPorProfessor(int professorId) throws SQLException {
        String sql = """
                SELECT id_atribuicao_professor, disciplina_id, professor_id, semestre_letivo_id
                FROM atribuicao_professor
                WHERE professor_id = ?
                ORDER BY semestre_letivo_id, disciplina_id
                """;

        List<AtribuicaoProfessor> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, professorId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public boolean existeConflito(int professorId, int semestreLetivoId,
                                   int diaSemana, int horarioCursoId) throws SQLException {
        String sql = """
                SELECT 1
                FROM atribuicao_horario ah
                INNER JOIN atribuicao_professor ap
                        ON ap.id_atribuicao_professor = ah.atribuicao_id
                WHERE ap.professor_id = ?
                  AND ap.semestre_letivo_id = ?
                  AND ah.dia_semana = ?
                  AND ah.horario_curso_id = ?
                LIMIT 1
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, professorId);
            ps.setInt(2, semestreLetivoId);
            ps.setInt(3, diaSemana);
            ps.setInt(4, horarioCursoId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void excluir(int idAtribuicao) throws SQLException {
        String sql = "DELETE FROM atribuicao_professor WHERE id_atribuicao_professor = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAtribuicao);
            ps.executeUpdate();
        }
    }

    private AtribuicaoProfessor mapear(ResultSet rs) throws SQLException {
        AtribuicaoProfessor a = new AtribuicaoProfessor();
        a.setId_atribuicao_professor(rs.getInt("id_atribuicao_professor"));
        a.setDisciplina_id(rs.getInt("disciplina_id"));
        a.setProfessor_id(rs.getInt("professor_id"));
        a.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
        return a;
    }


    private List<AtribuicaoProfessor> executarListagem(String sql, int param1, int param2)
            throws SQLException {

        List<AtribuicaoProfessor> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, param1);
            ps.setInt(2, param2);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }
}
