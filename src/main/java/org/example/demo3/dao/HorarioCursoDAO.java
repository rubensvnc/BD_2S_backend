package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.HorarioCurso;
import org.example.demo3.entity.TemplateHorarioTurno;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HorarioCursoDAO {

    private Connection connection;
    private PreparedStatement ps;

    public HorarioCursoDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void inserirTemplateHorarioCurso(List<TemplateHorarioTurno> thtLista,
                                            Integer cursoId, Integer slId) throws SQLException{

        try{
            for (TemplateHorarioTurno tht: thtLista) {
                String sql = """
                        INSERT INTO horario_curso (
                    curso_id, semestre_letivo_id, tipo, numero_ordem, hora_inicio, hora_fim)
                VALUES (?, ?, ?, ?, ?, ?);
                """;

                ps = connection.prepareStatement(sql);
                ps.setInt(1, cursoId);
                ps.setInt(2, slId);
                ps.setString(3, tht.getTipo());
                ps.setInt(4, tht.getNumero_ordem());
                ps.setObject(5, tht.getHora_inicio());
                ps.setObject(6, tht.getHora_fim());
                ps.executeUpdate();
            }

        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    public void inserirHorarioCurso(HorarioCurso horario) {

        String sql = """
                INSERT INTO horario_curso
                (curso_id, semestre_letivo_id, tipo,
                 numero_ordem, hora_inicio, hora_fim)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

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

        try (PreparedStatement stmt = connection.prepareStatement(sql);
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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

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

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, id);

            stmt.executeUpdate();

            System.out.println("Horário deletado!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}