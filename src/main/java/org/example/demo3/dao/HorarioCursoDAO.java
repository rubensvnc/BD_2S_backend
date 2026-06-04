package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.dto.AdmCursoExibicao;
import org.example.demo3.entity.HorarioCurso;
import org.example.demo3.entity.TemplateHorarioTurno;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HorarioCursoDAO {

    public List<HorarioCurso> listarHorariosPorAtribuicaoDSemana
            (Integer idAtribuicao, Integer diaSemana) throws SQLException{
        String sql = """
        SELECT DISTINCT ah.dia_semana, hc.id_horario_curso, hc.curso_id, 
            hc.semestre_letivo_id, hc.tipo, hc.numero_ordem, hc.hora_inicio, hc.hora_fim 
        FROM atribuicao_horario AS ah INNER JOIN horario_curso AS hc 
        ON hc.id_horario_curso = ah.horario_curso_id 
        WHERE ah.atribuicao_id = ? AND ah.dia_semana = ? ORDER BY ah.dia_semana;
        """;

        List<HorarioCurso> listaHc = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAtribuicao);
            ps.setInt(2, diaSemana);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HorarioCurso hc = new HorarioCurso(
                            rs.getInt("hc.id_horario_curso"),
                            rs.getInt("hc.curso_id"),
                            rs.getInt("hc.semestre_letivo_id"),
                            rs.getString("hc.tipo"),
                            rs.getInt("hc.numero_ordem"),
                            rs.getObject("hc.hora_inicio", java.time.LocalTime.class),
                            rs.getObject("hc.hora_fim", java.time.LocalTime.class)
                    );
                    listaHc.add(hc);
                }
            }
        }
        return listaHc;
    }

    public List<HorarioCurso> listarHorariosPorCurso
            (Integer curso_id, Integer sl) throws SQLException{

        String sql = """
            SELECT * FROM horario_curso WHERE curso_id = ? 
            AND semestre_letivo_id = ? ORDER BY numero_ordem;
        """;

        List<HorarioCurso> listaHc = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, curso_id);
            ps.setInt(2, sl);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HorarioCurso hc = new HorarioCurso(
                            rs.getInt("id_horario_curso"),
                            rs.getInt("curso_id"),
                            rs.getInt("semestre_letivo_id"),
                            rs.getString("tipo"),
                            rs.getInt("numero_ordem"),
                            rs.getObject("hora_inicio", java.time.LocalTime.class),
                            rs.getObject("hora_fim", java.time.LocalTime.class)
                    );
                    listaHc.add(hc);
                }
            }
        }
        return listaHc;
    }

    public void removerHorarioOrdemCursoSL(Integer ordem, Integer curso_id, Integer sl) throws SQLException{
        String sql = """
                DELETE FROM horario_curso WHERE numero_ordem = ? AND curso_id = ? AND semestre_letivo_id = ?;
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, ordem);
            ps.setInt(2, curso_id);
            ps.setInt(3, sl);
            ps.executeUpdate();

        }
    }

    public void removerHorariosCursoSL(Integer curso_id, Integer sl) throws SQLException{
        String sql = """
                DELETE FROM horario_curso WHERE curso_id = ? AND semestre_letivo_id = ?;
            """;

        try (Connection conexao = DatabaseConnection.getConnection();
             PreparedStatement ps = conexao.prepareStatement(sql)) {

            ps.setInt(1, curso_id);
            ps.setInt(2, sl);
            ps.executeUpdate();

        }
    }

    public List<Integer> recuperarIdsHoraInicioFim(LocalTime hi, LocalTime hf) throws SQLException{

        String sql = """
            SELECT DISTINCT id_horario_curso FROM horario_curso 
            WHERE hora_inicio = ? and hora_fim = ?;
        """;

        List<Integer> listaIds = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, hi);
            ps.setObject(2, hf);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaIds.add(rs.getInt("id_horario_curso"));
                }
            }
        }
        return listaIds;
    }

    public void inserirTemplateHorarioCurso(List<TemplateHorarioTurno> thtLista,
                                            Integer cursoId, Integer slId) throws SQLException {
        String sql = """
        INSERT INTO horario_curso (curso_id, semestre_letivo_id, tipo, numero_ordem, hora_inicio, hora_fim)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            for (TemplateHorarioTurno tht : thtLista) {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, cursoId);
                    ps.setInt(2, slId);
                    ps.setString(3, tht.getTipo());
                    ps.setInt(4, tht.getNumero_ordem());
                    ps.setObject(5, tht.getHora_inicio());
                    ps.setObject(6, tht.getHora_fim());
                    ps.executeUpdate();
                }
            }
        }
    }

    public void inserirHorarioCurso(HorarioCurso horario) {

        String sql = """
                INSERT INTO horario_curso
                (curso_id, semestre_letivo_id, tipo,
                 numero_ordem, hora_inicio, hora_fim)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, horario.getCurso_id());
            stmt.setInt(2, horario.getSemestre_letivo_id());
            stmt.setString(3, horario.getTipo());
            stmt.setInt(4, horario.getNumero_ordem());
            stmt.setTime(5, Time.valueOf(horario.getHora_inicio()));
            stmt.setTime(6, Time.valueOf(horario.getHora_fim()));

            stmt.executeUpdate();

            System.out.println("Horário inserido!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<HorarioCurso> listarHorarios() {

        List<HorarioCurso> lista = new ArrayList<>();

        String sql = "SELECT * FROM horario_curso";

        try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {

                HorarioCurso horario = new HorarioCurso();

                horario.setId_horario_curso(rs.getInt("id_horario_curso"));
                horario.setCurso_id(rs.getInt("curso_id"));
                horario.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                horario.setTipo(rs.getString("tipo"));
                horario.setNumero_ordem(rs.getInt("numero_ordem"));
                horario.setHora_inicio(rs.getTime("hora_inicio").toLocalTime());
                horario.setHora_fim(rs.getTime("hora_fim").toLocalTime());

                lista.add(horario);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    public HorarioCurso buscarPorId(Integer id) {

        String sql = "SELECT * FROM horario_curso WHERE id_horario_curso = ?";

        try (Connection connection = DatabaseConnection.getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                HorarioCurso horario = new HorarioCurso();

                horario.setId_horario_curso(rs.getInt("id_horario_curso"));
                horario.setCurso_id(rs.getInt("curso_id"));
                horario.setSemestre_letivo_id(rs.getInt("semestre_letivo_id"));
                horario.setTipo(rs.getString("tipo"));
                horario.setNumero_ordem(rs.getInt("numero_ordem"));
                horario.setHora_inicio(rs.getTime("hora_inicio").toLocalTime());
                horario.setHora_fim(rs.getTime("hora_fim").toLocalTime());

                return horario;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public void atualizarHorario(HorarioCurso horario) {

        String sql = """
                UPDATE horario_curso
                SET curso_id = ?,
                    semestre_letivo_id = ?,
                    tipo = ?,
                    numero_ordem = ?,
                    hora_inicio = ?,
                    hora_fim = ?
                WHERE id_horario_curso = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, horario.getCurso_id());
            stmt.setInt(2, horario.getSemestre_letivo_id());
            stmt.setString(3, horario.getTipo());
            stmt.setInt(4, horario.getNumero_ordem());
            stmt.setTime(5, Time.valueOf(horario.getHora_inicio()));
            stmt.setTime(6, Time.valueOf(horario.getHora_fim()));
            stmt.setInt(7, horario.getId_horario_curso());

            stmt.executeUpdate();

            System.out.println("Horário atualizado!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deletarHorario(Integer id) {

        String sql = "DELETE FROM horario_curso WHERE id_horario_curso = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

            System.out.println("Horário deletado!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}