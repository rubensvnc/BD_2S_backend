package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Curso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {

    private Connection connection;

    public CursoDAO() {
        this.connection = DatabaseConnection.getConnection();
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