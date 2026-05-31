package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.AtribuicaoHorario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AtribuicaoHorarioDAO {

    public void salvar(AtribuicaoHorario horario) throws SQLException {
        String sql = """
                INSERT INTO atribuicao_horario (atribuicao_id, dia_semana, horario_curso_id)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, horario.getAtribuicao_id());
            ps.setInt(2, horario.getDia_semana());
            ps.setInt(3, horario.getHorario_curso_id());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    horario.setId_atribuicao_horario(rs.getInt(1));
                }
            }
        }
    }

    public void salvarLote(List<AtribuicaoHorario> horarios) throws SQLException {
        if (horarios == null || horarios.isEmpty()) return;

        String sql = """
                INSERT INTO atribuicao_horario (atribuicao_id, dia_semana, horario_curso_id)
                VALUES (?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (AtribuicaoHorario h : horarios) {
                ps.setInt(1, h.getAtribuicao_id());
                ps.setInt(2, h.getDia_semana());
                ps.setInt(3, h.getHorario_curso_id());
                ps.addBatch();
            }

            ps.executeBatch();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;
                while (rs.next() && i < horarios.size()) {
                    horarios.get(i).setId_atribuicao_horario(rs.getInt(1));
                    i++;
                }
            }
        }
    }

    public void substituirHorarios(int atribuicaoId, List<AtribuicaoHorario> novosHorarios)
            throws SQLException {

        String sqlDelete = "DELETE FROM atribuicao_horario WHERE atribuicao_id = ?";
        String sqlInsert = """
                INSERT INTO atribuicao_horario (atribuicao_id, dia_semana, horario_curso_id)
                VALUES (?, ?, ?)
                """;

        Connection con = DatabaseConnection.getConnection();
        boolean autoCommitOriginal = con.getAutoCommit();

        try {
            con.setAutoCommit(false);

            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setInt(1, atribuicaoId);
                psDel.executeUpdate();
            }

            if (novosHorarios != null && !novosHorarios.isEmpty()) {
                try (PreparedStatement psIns = con.prepareStatement(sqlInsert,
                        Statement.RETURN_GENERATED_KEYS)) {

                    for (AtribuicaoHorario h : novosHorarios) {
                        psIns.setInt(1, h.getAtribuicao_id());
                        psIns.setInt(2, h.getDia_semana());
                        psIns.setInt(3, h.getHorario_curso_id());
                        psIns.addBatch();
                    }
                    psIns.executeBatch();

                    try (ResultSet rs = psIns.getGeneratedKeys()) {
                        int i = 0;
                        while (rs.next() && i < novosHorarios.size()) {
                            novosHorarios.get(i).setId_atribuicao_horario(rs.getInt(1));
                            i++;
                        }
                    }
                }
            }

            con.commit();

        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitOriginal);
        }
    }

    public List<AtribuicaoHorario> listarPorAtribuicao(int atribuicaoId) throws SQLException {
        String sql = """
                SELECT id_atribuicao_horario, atribuicao_id, dia_semana, horario_curso_id
                FROM atribuicao_horario
                WHERE atribuicao_id = ?
                ORDER BY dia_semana, horario_curso_id
                """;

        List<AtribuicaoHorario> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, atribuicaoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<AtribuicaoHorario> listarPorProfessorESemestre(int professorId, int semestreLetivoId)
            throws SQLException {

        String sql = """
                SELECT ah.id_atribuicao_horario,
                       ah.atribuicao_id,
                       ah.dia_semana,
                       ah.horario_curso_id
                FROM atribuicao_horario ah
                INNER JOIN atribuicao_professor ap
                        ON ap.id_atribuicao_professor = ah.atribuicao_id
                WHERE ap.professor_id = ?
                  AND ap.semestre_letivo_id = ?
                ORDER BY ap.disciplina_id, ah.dia_semana, ah.horario_curso_id
                """;

        List<AtribuicaoHorario> lista = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, professorId);
            ps.setInt(2, semestreLetivoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }


    // Em AtribuicaoHorarioDAO — substitui buscarConflito e existeConflito
    public String buscarConflitoParaProfessor(int professorId, int semestreLetivoId,
                                              int diaSemana, int horarioCursoId,
                                              int excluirAtribuicaoId) throws SQLException {
        // Busca hora_inicio e hora_fim do horário sendo avaliado
        String sqlHorario = """
            SELECT hc.hora_inicio, hc.hora_fim,
                   sl.ano, sl.numero_semestre
            FROM horario_curso hc
            INNER JOIN semestre_letivo sl
                    ON sl.id_semestre_letivo = hc.semestre_letivo_id
            WHERE hc.id_horario_curso = ?
            """;

        String horaInicio;
        String horaFim;
        int ano;
        int numeroSemestre;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlHorario)) {

            ps.setInt(1, horarioCursoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                horaInicio      = rs.getString("hora_inicio");
                horaFim         = rs.getString("hora_fim");
                ano             = rs.getInt("ano");
                numeroSemestre  = rs.getInt("numero_semestre");
            }
        }

        // Verifica se o professor já tem um horário com o mesmo dia + hora + ano + semestre
        // em qualquer curso/coordenador, excluindo a atribuição atual (edição)
        String sqlConflito = """
            SELECT d.nome AS nome_disciplina
            FROM atribuicao_horario ah
            INNER JOIN atribuicao_professor ap
                    ON ap.id_atribuicao_professor = ah.atribuicao_id
            INNER JOIN disciplina d
                    ON d.id_disciplina = ap.disciplina_id
            INNER JOIN horario_curso hc
                    ON hc.id_horario_curso = ah.horario_curso_id
            INNER JOIN semestre_letivo sl
                    ON sl.id_semestre_letivo = hc.semestre_letivo_id
            WHERE ap.professor_id    = ?
              AND sl.ano             = ?
              AND sl.numero_semestre = ?
              AND ah.dia_semana      = ?
              AND hc.hora_inicio     = ?
              AND hc.hora_fim        = ?
              AND ah.atribuicao_id  != ?
            LIMIT 1
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlConflito)) {

            ps.setInt(1, professorId);
            ps.setInt(2, ano);
            ps.setInt(3, numeroSemestre);
            ps.setInt(4, diaSemana);
            ps.setString(5, horaInicio);
            ps.setString(6, horaFim);
            ps.setInt(7, excluirAtribuicaoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome_disciplina");
                }
            }
        }
        return null;
    }

    /**
     * Verifica se o horário está ocupado por outro professor
     * no mesmo curso + semestre_curso + ano + semestre letivo + dia + hora.
     * Retorna o nome do professor que já ocupa o horário, ou null se livre.
     */
    public String buscarConflitoNoCurso(int cursoId, int semestreCurso,
                                        int semestreLetivoId, int diaSemana,
                                        int horarioCursoId, int excluirAtribuicaoId)
            throws SQLException {

        // Passo 1: extrai hora_inicio, hora_fim, ano e numero_semestre do horário atual
        String sqlHorario = """
            SELECT hc.hora_inicio, hc.hora_fim,
                   sl.ano, sl.numero_semestre
            FROM horario_curso hc
            INNER JOIN semestre_letivo sl
                    ON sl.id_semestre_letivo = hc.semestre_letivo_id
            WHERE hc.id_horario_curso = ?
            """;

        String horaInicio;
        String horaFim;
        int ano;
        int numeroSemestre;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlHorario)) {
            ps.setInt(1, horarioCursoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                horaInicio     = rs.getString("hora_inicio");
                horaFim        = rs.getString("hora_fim");
                ano            = rs.getInt("ano");
                numeroSemestre = rs.getInt("numero_semestre");
            }
        }

        // Passo 2: verifica se outro professor já ocupa esse dia+hora
        // na mesma combinação curso + semestre_curso + ano + semestre letivo
        String sqlConflito = """
            SELECT u.nome AS nome_professor
            FROM atribuicao_horario ah
            INNER JOIN atribuicao_professor ap
                    ON ap.id_atribuicao_professor = ah.atribuicao_id
            INNER JOIN disciplina d
                    ON d.id_disciplina = ap.disciplina_id
            INNER JOIN horario_curso hc
                    ON hc.id_horario_curso = ah.horario_curso_id
            INNER JOIN semestre_letivo sl
                    ON sl.id_semestre_letivo = hc.semestre_letivo_id
            INNER JOIN usuario u
                    ON u.id_usuario = ap.professor_id
            WHERE d.curso_id        = ?
              AND d.semestre_curso  = ?
              AND sl.ano            = ?
              AND sl.numero_semestre= ?
              AND ah.dia_semana     = ?
              AND hc.hora_inicio    = ?
              AND hc.hora_fim       = ?
              AND ah.atribuicao_id != ?
            LIMIT 1
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlConflito)) {
            ps.setInt(1, cursoId);
            ps.setInt(2, semestreCurso);
            ps.setInt(3, ano);
            ps.setInt(4, numeroSemestre);
            ps.setInt(5, diaSemana);
            ps.setString(6, horaInicio);
            ps.setString(7, horaFim);
            ps.setInt(8, excluirAtribuicaoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("nome_professor");
            }
        }
        return null;
    }

    public void excluirPorAtribuicao(int atribuicaoId) throws SQLException {
        String sql = "DELETE FROM atribuicao_horario WHERE atribuicao_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, atribuicaoId);
            ps.executeUpdate();
        }
    }

    public void excluir(int idAtribuicaoHorario) throws SQLException {
        String sql = "DELETE FROM atribuicao_horario WHERE id_atribuicao_horario = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idAtribuicaoHorario);
            ps.executeUpdate();
        }
    }

    private AtribuicaoHorario mapear(ResultSet rs) throws SQLException {
        AtribuicaoHorario ah = new AtribuicaoHorario();
        ah.setId_atribuicao_horario(rs.getInt("id_atribuicao_horario"));
        ah.setAtribuicao_id(rs.getInt("atribuicao_id"));
        ah.setDia_semana(rs.getInt("dia_semana"));
        ah.setHorario_curso_id(rs.getInt("horario_curso_id"));
        return ah;
    }
}
