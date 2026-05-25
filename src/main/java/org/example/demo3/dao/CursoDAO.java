package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Curso;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    public CursoDAO() {}

    public Curso buscarCursoCoordenador(int coordenadorId) throws SQLException {
        String sql = "SELECT nome FROM curso WHERE coordenador_id = ?;";
        Curso c = new Curso();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coordenadorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    c.setNome(rs.getString("nome"));
                }
            }
        }
        return c;
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
}