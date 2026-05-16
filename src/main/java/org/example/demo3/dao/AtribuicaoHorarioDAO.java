package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import  org.example.demo3.entity.AtribuicaoHorario;

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

    public String buscarConflito(int semestreLetivoId, int diaSemana,
                                  int horarioCursoId, int excluirAtribuicaoId) throws SQLException {
        String sql = """
                SELECT u.nome
                FROM atribuicao_horario ah
                INNER JOIN atribuicao_professor ap
                        ON ap.id_atribuicao_professor = ah.atribuicao_id
                INNER JOIN usuario u
                        ON u.id_usuario = ap.professor_id
                WHERE ap.semestre_letivo_id = ?
                  AND ah.dia_semana          = ?
                  AND ah.horario_curso_id    = ?
                  AND ah.atribuicao_id      != ?
                LIMIT 1
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, semestreLetivoId);
            ps.setInt(2, diaSemana);
            ps.setInt(3, horarioCursoId);
            ps.setInt(4, excluirAtribuicaoId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nome");
                }
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
