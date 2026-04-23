package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.PlanejamentoDia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlanejamentoDiaDao {

    public static List<PlanejamentoDia> atualizarPlanejamentoDia(Integer curso_id, Integer semestre) {
        List<PlanejamentoDia> planejamentos = new ArrayList<>();
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection("organizacao_aulas_fatec", "root", "root");

            String select_query = "SELECT c_i.id_cronograma_i, c_i.data_evento, d.nome, t.nome, " +
                    "c_i.observacao, c_i.qtd_aulas, (DAYOFWEEK(c_i.data_evento) - 1) AS dia_semana_indice" +
                    " FROM cronograma_item AS c_i" +
                    " INNER JOIN tema AS t ON t.id_tema = c_i.tema_id" +
                    " INNER JOIN disciplina AS d ON d.id_disciplina = t.disciplina_id" +
                    " INNER JOIN cronograma AS cr ON cr.id_cronograma = c_i.cronograma_id" +
                    " INNER JOIN curso AS c ON c.id_curso = cr.curso_id" +
                    " WHERE c.id_curso = ? AND cr.semestre = ?";

            PreparedStatement pstm = con.prepareStatement(select_query);
            pstm.setInt(1, curso_id);
            pstm.setInt(2, semestre);

            ResultSet rs = pstm.executeQuery();
            while (rs.next()){
                PlanejamentoDia planejamento = new PlanejamentoDia();
                planejamento.setId(rs.getInt("id_cronograma_i"));

                LocalDate dataEvento = rs.getObject("data_evento", LocalDate.class);
                planejamento.setData(dataEvento);

                String diaSemanaExtenso = dataEvento.getDayOfWeek()
                        .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("pt", "BR"));

                diaSemanaExtenso = diaSemanaExtenso.substring(0, 1).toUpperCase() + diaSemanaExtenso.substring(1);

                planejamento.setDia_semana(diaSemanaExtenso);

                planejamento.setDisciplina(rs.getString("nome"));
                planejamento.setTema(rs.getString("nome"));
                planejamento.setObs(rs.getString("observacao"));
                planejamento.setAulas(rs.getInt("qtd_aulas"));
                planejamentos.add(planejamento);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao atualizar a lista!", e);
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return planejamentos;
    }

    public List<String> corrigirCronograma(Integer cursoId, Integer semestre) {
        List<String> logs = new ArrayList<>();
        // Query enriquecida para pegar dados do curso e disciplina para o log
        String sql = "SELECT ci.id_cronograma_i, ci.data_evento, ci.observacao, ci.qtd_aulas, " +
                "t.nome as tema_nome, d.nome as disc_nome, d.semestre_num, c.nome as curso_nome " +
                "FROM cronograma_item ci " +
                "JOIN tema t ON ci.tema_id = t.id_tema " +
                "JOIN disciplina d ON t.disciplina_id = d.id_disciplina " +
                "JOIN cronograma cr ON ci.cronograma_id = cr.id_cronograma " +
                "JOIN curso c ON cr.curso_id = c.id_curso " + // Correção: cr.curso_id referencia c.id_curso
                "WHERE c.id_curso = ? AND cr.semestre = ? ORDER BY ci.data_evento";

        try (Connection con = DatabaseConnection.getConnection("organizacao_aulas_fatec", "root", "root")) {
            List<LocalDate> feriados = carregarFeriados(con);
            PreparedStatement pstm = con.prepareStatement(sql);
            pstm.setInt(1, cursoId);
            pstm.setInt(2, semestre);
            ResultSet rs = pstm.executeQuery();

            while (rs.next()) {
                LocalDate dataOriginal = rs.getObject("data_evento", LocalDate.class);
                if (isDataProibida(dataOriginal, feriados)) {
                    LocalDate novaData = dataOriginal;
                    while (isDataProibida(novaData, feriados)) {
                        novaData = novaData.plusDays(1);
                    }

                    // Gerar log detalhado
                    String log = String.format(
                            "ALTERAÇÃO: [%s] -> [%s] | Tema: %s | Disc: %s | Semestre: %d | Curso: %s | Obs: %s | Aulas: %d",
                            dataOriginal, novaData, rs.getString("tema_nome"), rs.getString("disc_nome"),
                            rs.getInt("semestre_num"), rs.getString("curso_nome"),
                            rs.getString("observacao"), rs.getInt("qtd_aulas")
                    );
                    logs.add(log);

                    atualizarDataNoBanco(con, rs.getInt("id_cronograma_i"), novaData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    // Identifica o motivo para o log no terminal
    private String getMotivoProibicao(LocalDate data, List<LocalDate> feriados) {
        int dow = data.getDayOfWeek().getValue();
        if (dow == 7) return "DOMINGO";
        if (dow == 6) return "SÁBADO";
        boolean isFeriado = feriados.stream().anyMatch(f -> f.getDayOfMonth() == data.getDayOfMonth()
                && f.getMonth() == data.getMonth());
        if (isFeriado) return "FERIADO";
        return "DESCONHECIDO";
    }

    // Executa o update de forma limpa
    private void atualizarDataNoBanco(Connection con, int id, LocalDate novaData) throws SQLException {
        String sqlUpdate = "UPDATE cronograma_item SET data_evento = ? WHERE id_cronograma_i = ?";
        try (PreparedStatement ps = con.prepareStatement(sqlUpdate)) {
            ps.setObject(1, novaData);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // Carrega feriados para a memória
    private List<LocalDate> carregarFeriados(Connection con) throws SQLException {
        List<LocalDate> lista = new ArrayList<>();
        ResultSet rs = con.createStatement().executeQuery("SELECT dia, mes, ano FROM feriado");
        while (rs.next()) {
            int ano = rs.getObject("ano") != null ? rs.getInt("ano") : LocalDate.now().getYear();
            lista.add(LocalDate.of(ano, rs.getInt("mes"), rs.getInt("dia")));
        }
        return lista;
    }

    // Método auxiliar pequeno e legível
    private boolean isDataProibida(LocalDate data, List<LocalDate> feriados) {
        int dow = data.getDayOfWeek().getValue(); // 1 (Seg) a 7 (Dom)
        boolean isFimSemana = (dow == 6 || dow == 7);
        boolean isFeriado = feriados.stream().anyMatch(f -> f.getDayOfMonth() == data.getDayOfMonth()
                && f.getMonth() == data.getMonth());
        return isFimSemana || isFeriado;
    }
}