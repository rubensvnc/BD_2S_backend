package org.example.demo3.dao;


import org.example.demo3.DatabaseConnection;
import org.example.demo3.dto.CronogramaExibicaoDTO;
import org.example.demo3.entity.CronogramaFinal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CronogramaDAO {

    public List<CronogramaExibicaoDTO> listarCronogramaCompleto(int idProfessor, int idSemestre, int idCurso) throws SQLException{
        String sql = "SELECT c.data_aula, d.nome AS disciplina_nome, t.nome AS tema_nome, " +
                "a.quantidade_aulas, c.status, c.motivo_cancelamento, t.is_avaliacao " +
                "FROM cronograma_final c " +
                "JOIN atribuicao a ON c.id_atribuicao = a.id_atribuicao " +
                "JOIN disciplina d ON a.id_disciplina = d.id_disciplina " +
                "JOIN tema t ON c.id_tema = t.id_tema " +
                "WHERE a.id_usuario = ? " +
                "AND a.id_semestre = ? " +
                "AND d.id_curso = ? " +
                "ORDER BY c.data_aula ASC";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<CronogramaExibicaoDTO> lista_cf = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idProfessor);
            ps.setInt(2, idSemestre);
            ps.setInt(3, idCurso);

            rs = ps.executeQuery();
            while (rs.next()){
                CronogramaExibicaoDTO dto = new CronogramaExibicaoDTO();
                dto.setData(rs.getDate("data_aula"));
                dto.setNomeDisciplina(rs.getString("disciplina_nome"));
                dto.setNomeTema(rs.getString("tema_nome"));
                dto.setQtdAulas(rs.getInt("quantidade_aulas"));
                dto.setStatus(rs.getString("status"));
                dto.setMotivo(rs.getString("motivo_cancelamento"));
                dto.setAvaliacao(rs.getBoolean("is_avaliacao"));

                lista_cf.add(dto);

            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar cronograma: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista_cf;
    }

    public void inserirItemCronograma(CronogramaFinal item) throws SQLException {
        String sql = "INSERT INTO cronograma_final " +
                "(id_atribuicao, data_aula, id_tema, status, motivo_cancelamento, is_sabado_letivo) " +
                "VALUES (?,?,?,?,?,?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getConnection();

            ps = conn.prepareStatement(sql);
            ps.setLong(1, item.getId_atribuicao());
            ps.setDate(2, item.getData_aula());
            ps.setLong(3, item.getId_tema());
            ps.setString(4, item.getStatus());
            ps.setString(5, item.getMotivo_cancelamento());
            ps.setBoolean(6, item.getIs_sabado_letivo());

            ps.executeUpdate();

        } catch (SQLException e){
            System.err.println("Erro ao inserir linha: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
    }

}
