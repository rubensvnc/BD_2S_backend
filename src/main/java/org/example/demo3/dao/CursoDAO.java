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
        String sql = "SELECT nome FROM curso WHERE coordenador_id = ?;";

        List<Curso> listaC = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coordenadorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Curso c = new Curso();
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

    public Integer buscarQtdSemestresPorId(Curso curso) {
        String sql = "SELECT qtd_semestres FROM curso WHERE id_curso = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, curso.getId_curso());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("qtd_semestres");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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