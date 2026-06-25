package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.dto.AdmCursoExibicao;
import org.example.demo3.entity.Curso;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    public CursoDAO() {}

    public Integer listarIdCurso(String nomeCurso) throws SQLException{
        String sql = "SELECT id_curso FROM curso WHERE nome = ?;";
        Integer id;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nomeCurso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    id = rs.getInt("id_curso");
                    return id;
                }
            }
        }
        return null;
    }

    public List<AdmCursoExibicao> listarCursosDTO(Integer ano, Integer semestreAno) throws SQLException{
        String sql = """
            SELECT DISTINCT c.nome, c.turno, c.qtd_semestres, u.email 
            FROM curso AS c LEFT JOIN usuario AS u ON c.coordenador_id = u.id_usuario 
            INNER JOIN horario_curso AS hc ON hc.curso_id = c.id_curso 
            INNER JOIN semestre_letivo AS sl ON 
            sl.id_semestre_letivo = hc.semestre_letivo_id 
            WHERE sl.ano = ? AND sl.numero_semestre = ?;
        """;

        List<AdmCursoExibicao> listaCDto = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ano);
            ps.setInt(2, semestreAno);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdmCursoExibicao cDto = new AdmCursoExibicao(
                            rs.getString("c.nome"),
                            rs.getString("c.turno"),
                            rs.getInt("c.qtd_semestres"),
                            rs.getString("u.email")
                    );
                    listaCDto.add(cDto);
                }
            }
        }
        return listaCDto;
    }

    public List<Curso> buscarCursoCoordenador(int coordenadorId) throws SQLException {
        String sql = "SELECT id_curso, nome FROM curso WHERE coordenador_id = ?;"; // ← adiciona id_curso

        List<Curso> listaC = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coordenadorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {  // ← era if, troca para while
                    Curso c = new Curso();
                    c.setId_curso(rs.getInt("id_curso")); // ← adiciona
                    c.setNome(rs.getString("nome"));
                    listaC.add(c);
                }
            }
        }
        return listaC;
    }

    public void removerCoordenadorDeCurso(Integer idCurso) throws SQLException {
        String sql = "UPDATE curso SET coordenador_id = NULL WHERE id_curso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.executeUpdate();
        }
    }

    public List<Curso> listarCursosProfessor(int professorId, int ano, int semestreAno) throws SQLException {
        String sql = """
            SELECT DISTINCT c.nome FROM atribuicao_professor AS ap 
            INNER JOIN semestre_letivo AS sl ON sl.id_semestre_letivo = ap.semestre_letivo_id 
            INNER JOIN disciplina AS d ON ap.disciplina_id = d.id_disciplina 
            INNER JOIN curso AS c ON d.curso_id = c.id_curso 
            WHERE ap.professor_id = ? AND sl.ano = ? AND sl.numero_semestre = ?;
            """;
        List<Curso> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, professorId);
            ps.setInt(2, ano);
            ps.setInt(3, semestreAno);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Curso c = new Curso();
                    c.setNome(rs.getString("nome"));
                    lista.add(c);
                }
            }
        }
        return lista;
    }

    public Integer inserirCursoRetornaId(String nomeCurso, String turno, Integer qtdSemestre) throws SQLException {
        String sql = "INSERT INTO curso(nome, turno, qtd_semestres) VALUES (?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nomeCurso);
            ps.setString(2, turno);
            ps.setInt(3, qtdSemestre);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    public void alterarCurso(AdmCursoExibicao cDto, Integer coord_id, String nomeAntigo){
        String sql = """
        UPDATE curso SET coordenador_id = ?, nome = ?, turno = ?, qtd_semestres = ? WHERE nome = ?;
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (coord_id == null) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, coord_id);
            }

            stmt.setString(2, cDto.getNome());
            stmt.setString(3, cDto.getTurno());
            stmt.setInt(4, cDto.getQtd_semestres());
            stmt.setString(5, nomeAntigo);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Update realizado com sucesso!");
            } else {
                System.out.println("Nenhum registro encontrado com o Nome fornecido.");
            }

        } catch (SQLException e) {
            System.err.println("Erro ao executar update: " + e.getMessage());
        }
    }

    public Integer inserirCursoRetornaId(Integer idCoord, String nomeCurso, String turno, Integer qtdSemestre) throws SQLException {
        String sql = "INSERT INTO curso(coordenador_id, nome, turno, qtd_semestres) VALUES (?, ?, ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCoord);
            ps.setString(2, nomeCurso);
            ps.setString(3, turno);
            ps.setInt(4, qtdSemestre);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    public void deletarCursoProcessando(Integer idCurso) throws SQLException {
        String sql = "DELETE FROM curso WHERE id_curso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            ps.executeUpdate();
        }
    }

    public int buscarQtdSemestresPorId(int idCurso) throws SQLException {
        String sql = "SELECT qtd_semestres FROM curso WHERE id_curso = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCurso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("qtd_semestres");
            }
        }
        return 0;
    }

    public void inserirCurso(Curso curso) throws SQLException {
        String sql = "INSERT INTO curso (coordenador_id, nome, turno, qtd_semestres, deletado_em) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, curso.getCoordenador_id());
            stmt.setString(2, curso.getNome());
            stmt.setString(3, curso.getTurno());
            stmt.setInt(4, curso.getQtd_semestres());
            stmt.setTimestamp(5, curso.getDeletado_em() != null ? Timestamp.valueOf(curso.getDeletado_em()) : null);
            stmt.executeUpdate();
        }
    }

    public List<Curso> listarCursos() throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT * FROM curso";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Curso curso = new Curso();
                curso.setId_curso(rs.getInt("id_curso"));
                curso.setCoordenador_id(rs.getInt("coordenador_id"));
                curso.setNome(rs.getString("nome"));
                curso.setTurno(rs.getString("turno"));
                curso.setQtd_semestres(rs.getInt("qtd_semestres"));
                Timestamp del = rs.getTimestamp("deletado_em");
                if (del != null) curso.setDeletado_em(del.toLocalDateTime());
                cursos.add(curso);
            }
        }
        return cursos;
    }


    public List<AdmCursoExibicao> listarTodosCursosDTO() throws SQLException {
        String sql = """
        SELECT c.nome, c.turno, c.qtd_semestres, u.email
        FROM curso c
        LEFT JOIN usuario u ON u.id_usuario = c.coordenador_id
        WHERE c.deletado_em IS NULL
        ORDER BY c.id_curso ASC
        """;

        List<AdmCursoExibicao> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new AdmCursoExibicao(
                        rs.getString("nome"),
                        rs.getString("turno"),
                        rs.getInt("qtd_semestres"),
                        rs.getString("email")   // NULL vira null automaticamente
                ));
            }
        }
        return lista;
    }

    //MÉTODO PARA CONFERIR SE O CURSO TEM DEPENDÊNCIAS (DISCIPLINAS E/OU COORDENADORES), ESTÁ SENDO UTILIZADO NA TELA DE GERENCIAMENTO DE CURSOS
    public boolean possuiDependenciasCurso(int cursoId) throws SQLException {
        String sql = """
        SELECT EXISTS (
            SELECT 1 FROM disciplina WHERE curso_id = ? AND deletado_em IS NULL
        ) AS tem_disciplina,
        EXISTS (
            SELECT 1 FROM curso WHERE id_curso = ? AND coordenador_id IS NOT NULL
        ) AS tem_coordenador,
        EXISTS (
            SELECT 1 FROM horario_curso WHERE curso_id = ?
        ) AS tem_horario
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cursoId);
            ps.setInt(2, cursoId);
            ps.setInt(3, cursoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("tem_disciplina")
                            || rs.getBoolean("tem_coordenador")
                            || rs.getBoolean("tem_horario");
                }
            }
        }
        return false;
    }

    public void softDeletarCursoComVinculos(int idCurso) throws SQLException {
        String[] hardDeletes = {
                // 1. Remove dependências de temas das disciplinas do curso
                """
        DELETE dt FROM dependencia_tema dt
        INNER JOIN tema t ON t.id_tema = dt.tema_id
        INNER JOIN disciplina d ON d.id_disciplina = t.disciplina_id
        WHERE d.curso_id = ? AND d.deletado_em IS NULL
        """,

                // 2. Remove slots vinculados a temas das disciplinas do curso
                """
                DELETE sp FROM slot_planejamento sp
                INNER JOIN tema t ON t.id_tema = sp.tema_id
                INNER JOIN disciplina d ON d.id_disciplina = t.disciplina_id
                WHERE d.curso_id = ? AND d.deletado_em IS NULL AND t.deletado_em IS NULL
                """,

                // 3. Remove temas das disciplinas do curso
                """
        UPDATE tema t
        INNER JOIN disciplina d ON d.id_disciplina = t.disciplina_id
        SET t.deletado_em = NOW()
        WHERE d.curso_id = ? AND d.deletado_em IS NULL AND t.deletado_em IS NULL
        """,

                // 4. Remove slots vinculados a planejamentos das atribuições do curso
                """
        DELETE sp FROM slot_planejamento sp
        INNER JOIN planejamento p ON p.id_planejamento = sp.planejamento_id
        INNER JOIN atribuicao_professor ap ON ap.id_atribuicao_professor = p.atribuicao_professor_id
        INNER JOIN disciplina d ON d.id_disciplina = ap.disciplina_id
        WHERE d.curso_id = ? AND d.deletado_em IS NULL
        """,

                // 5. Remove planejamentos das atribuições do curso
                """
        DELETE p FROM planejamento p
        INNER JOIN atribuicao_professor ap ON ap.id_atribuicao_professor = p.atribuicao_professor_id
        INNER JOIN disciplina d ON d.id_disciplina = ap.disciplina_id
        WHERE d.curso_id = ? AND d.deletado_em IS NULL
        """,

                // 6. Remove atribuições de horário das atribuições do curso
                """
        DELETE ah FROM atribuicao_horario ah
        INNER JOIN atribuicao_professor ap ON ap.id_atribuicao_professor = ah.atribuicao_id
        INNER JOIN disciplina d ON d.id_disciplina = ap.disciplina_id
        WHERE d.curso_id = ? AND d.deletado_em IS NULL
        """,

                // 7. Remove atribuições de professor das disciplinas do curso
                """
        DELETE ap FROM atribuicao_professor ap
        INNER JOIN disciplina d ON d.id_disciplina = ap.disciplina_id
        WHERE d.curso_id = ? AND d.deletado_em IS NULL
        """,

                // 8. Remove slots vinculados aos horários do curso
                """
        DELETE sp FROM slot_planejamento sp
        INNER JOIN horario_curso hc ON hc.id_horario_curso = sp.horario_curso_id
        WHERE hc.curso_id = ?
        """,

                // 9. Remove cancelamentos de horários específicos do curso
                """
        DELETE cah FROM cancelamento_adm_horario cah
        INNER JOIN horario_curso hc ON hc.id_horario_curso = cah.horario_curso_id
        WHERE hc.curso_id = ?
        """,

                // 10. Remove atribuições de horário vinculadas aos horários do curso
                """
        DELETE ah FROM atribuicao_horario ah
        INNER JOIN horario_curso hc ON hc.id_horario_curso = ah.horario_curso_id
        WHERE hc.curso_id = ?
        """,

                // 11. Remove os horários do curso
                "DELETE FROM horario_curso WHERE curso_id = ?",

                // 12. Desvincula o coordenador (professor continua existindo)
                "UPDATE curso SET coordenador_id = NULL WHERE id_curso = ?"
        };

        String softDeleteDisciplinas =
                "UPDATE disciplina SET deletado_em = NOW() WHERE curso_id = ? AND deletado_em IS NULL";

        String softDeleteCurso =
                "UPDATE curso SET deletado_em = NOW() WHERE id_curso = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String sql : hardDeletes) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, idCurso);
                        ps.executeUpdate();
                    }
                }

                // Soft delete nas disciplinas
                try (PreparedStatement ps = conn.prepareStatement(softDeleteDisciplinas)) {
                    ps.setInt(1, idCurso);
                    ps.executeUpdate();
                }

                // Soft delete no curso
                try (PreparedStatement ps = conn.prepareStatement(softDeleteCurso)) {
                    ps.setInt(1, idCurso);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

}