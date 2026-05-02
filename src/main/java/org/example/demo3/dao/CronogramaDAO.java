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

    public List<CronogramaExibicaoDTO> listarCronogramaCursoSemestre(int idProfessor, int idCurso, int idSemestre) throws SQLException{
        String sql = "SELECT ci.data_prevista, d.nome, t.nome, ci.qtd_aulas, ci.status_aula, dc.descricao, t.eh_avaliacao " +
                "FROM cronograma_item ci " +
                "JOIN cronograma c ON ci.cronograma_id = c.id_cronograma " +
                "JOIN tema t ON ci.tema_id = t.id_tema " +
                "JOIN disciplina d ON t.disciplina_id = d.id_disciplina " +
                "LEFT JOIN data_cancelada dc ON ci.id_data_cancelada = dc.id_data_cancelada " +
                "WHERE c.usuario_id = ? AND c.curso_id = ? AND c.grade_semestre = ? " +
                "ORDER BY ci.data_prevista ASC";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<CronogramaExibicaoDTO> lista_cf = new ArrayList<>();
        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idProfessor);
            ps.setInt(2, idCurso);
            ps.setInt(3, idSemestre);

            rs = ps.executeQuery();
            while (rs.next()) {
                CronogramaExibicaoDTO dto = new CronogramaExibicaoDTO();
                dto.setData(rs.getDate("data_prevista"));
                dto.setNomeDisciplina(rs.getString("d.nome"));
                dto.setNomeTema(rs.getString("t.nome"));
                dto.setQtdAulas(rs.getInt("qtd_aulas"));
                dto.setStatus(rs.getString("status_aula"));
                dto.setMotivo(rs.getString("descricao")); // Pode vir nulo devido ao LEFT JOIN
                dto.setAvaliacao(rs.getBoolean("eh_avaliacao"));

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
}
