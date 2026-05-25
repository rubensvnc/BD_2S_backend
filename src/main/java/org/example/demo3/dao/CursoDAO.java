package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.SemestreLetivo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    private Connection connection;
    private PreparedStatement ps;

    public CursoDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public Curso buscarCursoCoordenador(int coordenadorId) throws SQLException{
        String sql = """
            SELECT nome FROM curso WHERE coordenador_id = ?;
            """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        Curso c = new Curso();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, coordenadorId);

            rs = ps.executeQuery();

            while (rs.next()) {
                c.setNome(rs.getString("nome"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }

        return c;
    }

    public List<Curso> listarCursosProfessor(int professorId, int ano, int semestreAno) throws SQLException {
        String sql = """
            SELECT DISTINCT c.nome FROM atribuicao_professor AS ap INNER JOIN 
            semestre_letivo AS sl ON sl.id_semestre_letivo = ap.semestre_letivo_id 
            INNER JOIN disciplina AS d ON ap.disciplina_id = d.id_disciplina 
            INNER JOIN curso AS c ON d.curso_id = c.id_curso WHERE ap.professor_id = ? 
            AND sl.ano = ? AND sl.numero_semestre = ?;
            """;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Curso> lista = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, professorId);
            ps.setInt(2, ano);
            ps.setInt(3, semestreAno);

            rs = ps.executeQuery();
            while (rs.next()) {
                Curso c = new Curso();
                c.setNome(rs.getString("c.nome"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }

        return lista;
    }

    public Integer inserirCursoRetornaId(String nomeCurso, String turno, Integer qtdSemestre) throws SQLException{
        try {
            String q = "INSERT INTO curso(nome, turno, qtd_semestres)" +
                    "VALUES (?, ?, ?);";
            ps = connection.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nomeCurso);
            ps.setString(2, turno);
            ps.setInt(3, qtdSemestre);
            ps.executeUpdate();

            try {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()){
                    return rs.getInt(1);
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return null;
    }

    public void deletarCursoProcessando(Integer idCurso) throws SQLException{
        String sql = "DELETE FROM curso WHERE id_curso = ?";

        try {
            ps = connection.prepareStatement(sql);
            ps.setInt(1, idCurso);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    public Integer inserirCursoRetornaId(Integer idCoord, String nomeCurso, String turno, Integer qtdSemestre) throws SQLException{
        try {
            String q = "INSERT INTO curso(coordenador_id, nome, turno, qtd_semestres)" +
                    "VALUES (?, ?, ?, ?);";
            ps = connection.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idCoord);
            ps.setString(2, nomeCurso);
            ps.setString(3, turno);
            ps.setInt(4, qtdSemestre);
            ps.executeUpdate();

            try {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()){
                    return rs.getInt(1);
                }
            } catch (Exception e){
                e.printStackTrace();
            }

        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            connection.close();
        }
        return null;
    }

    public void inserirCurso(Curso curso) {

        String sql = """
            INSERT INTO curso (
                coordenador_id,
                nome,
                turno,
                qtd_semestres,
                deletado_em
            ) VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, curso.getCoordenador_id());
            stmt.setString(2, curso.getNome());
            stmt.setString(3, curso.getTurno());
            stmt.setInt(4, curso.getQtd_semestres());

            if (curso.getDeletado_em() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(curso.getDeletado_em()));
            } else {
                stmt.setTimestamp(5, null);
            }

            stmt.executeUpdate();

            System.out.println("Curso inserido com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao inserir curso: " + e.getMessage());
        }
    }

    public List<Curso> listarCursos() {

        List<Curso> cursos = new ArrayList<>();

        String sql = """
            SELECT *
            FROM curso
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                Curso curso = new Curso();

                curso.setId_curso(rs.getInt("id_curso"));
                curso.setCoordenador_id(rs.getInt("coordenador_id"));
                curso.setNome(rs.getString("nome"));
                curso.setTurno(rs.getString("turno"));
                curso.setQtd_semestres(rs.getInt("qtd_semestres"));

                Timestamp deletadoEm = rs.getTimestamp("deletado_em");

                if (deletadoEm != null) {
                    curso.setDeletado_em(deletadoEm.toLocalDateTime());
                }

                cursos.add(curso);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar cursos: " + e.getMessage());
        }

        return cursos;
    }

    public void atualizarCurso(Curso curso) {

        String sql = """
            UPDATE curso
            SET coordenador_id = ?,
                nome = ?,
                turno = ?,
                qtd_semestres = ?,
                deletado_em = ?
            WHERE id_curso = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, curso.getCoordenador_id());
            stmt.setString(2, curso.getNome());
            stmt.setString(3, curso.getTurno());
            stmt.setInt(4, curso.getQtd_semestres());

            if (curso.getDeletado_em() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(curso.getDeletado_em()));
            } else {
                stmt.setTimestamp(5, null);
            }

            stmt.setInt(6, curso.getId_curso());

            stmt.executeUpdate();

            System.out.println("Curso atualizado com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao atualizar curso: " + e.getMessage());
        }
    }

    public void excluirCurso(Integer idCurso) {

        String sql = """
            DELETE FROM curso
            WHERE id_curso = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idCurso);

            stmt.executeUpdate();

            System.out.println("Curso removido com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao remover curso: " + e.getMessage());
        }
    }
}