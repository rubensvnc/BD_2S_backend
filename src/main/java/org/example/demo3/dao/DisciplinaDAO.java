package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Tema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    public List<Tema> listarTemasPorProfessorECurso(int idUsuario, int idCurso, int idSemestre) {
        List<Tema> lista = new ArrayList<>();

        // SQL utilizando GROUP_CONCAT para as dependências
        String sql = "SELECT " +
                "  t.id_tema, t.nome, t.prioridade, t.qtd_min_aulas, t.qtd_max_aulas, " +
                "  t.eh_avaliacao, t.eh_opcional, t.disciplina_id, " +
                "  d.nome AS nome_disciplina, " +
                "  (SELECT GROUP_CONCAT(tdep.nome SEPARATOR ', ') " +
                "   FROM tema_dependencia td " +
                "   JOIN tema tdep ON td.tema_anterior = tdep.id_tema " +
                "   WHERE td.tema_posterior = t.id_tema) AS nomes_dependencias " +
                "FROM tema t " +
                "INNER JOIN disciplina d ON t.disciplina_id = d.id_disciplina " +
                "INNER JOIN atribuicao a ON d.id_disciplina = a.disciplina_id " +
                "WHERE a.usuario_id = ? " +
                "  AND d.curso_id = ? " +
                "  AND a.semestre_id = ? " +
                "ORDER BY d.nome, t.prioridade";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idCurso);
            stmt.setInt(3, idSemestre);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Tema tema = new Tema();
                tema.setId(rs.getInt("id_tema"));
                tema.setNome(rs.getString("nome"));
                tema.setPrioridade(rs.getInt("prioridade"));
                tema.setQtdMinAulas(rs.getInt("qtd_min_aulas"));
                tema.setQtdMaxAulas(rs.getInt("qtd_max_aulas"));
                tema.setEhAvaliacao(rs.getBoolean("eh_avaliacao"));
                tema.setEhOpcional(rs.getBoolean("eh_opcional"));
                tema.setDisciplinaId(rs.getInt("disciplina_id"));

                // Preenche o campo auxiliar de nome da disciplina
                tema.setNomeDisciplina(rs.getString("nome_disciplina"));

                // Como sua TableView precisa de uma String na coluna Dependências,
                // vamos usar uma técnica: criar um objeto fictício na lista ou
                // simplesmente passar a String concatenada para um campo auxiliar se preferir.
                // Aqui, vou sugerir usar o campo "dependencias" do SQL diretamente no Controller.
                String deps = rs.getString("nomes_dependencias");
                if (deps != null) {
                    // Adicionamos um "Tema fake" só com o nome concatenado para o toString funcionar
                    // OU lidamos com isso diretamente na TableColumn (melhor prática).
                    tema.adicionarDependencia(new Tema(0, deps));
                }

                lista.add(tema);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }








    public List<String> listarSemestresProfessorCurso(int idProfessor, int idCurso) throws SQLException {
        String sql = "SELECT DISTINCT d.semestre " +
                "FROM disciplina d " +
                "JOIN atribuicao a ON d.id_disciplina = a.disciplina_id " +
                "WHERE a.usuario_id = ? " +
                "AND d.curso_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<String> semestres = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idProfessor);
            ps.setInt(2, idCurso);
            rs = ps.executeQuery();

            while (rs.next()) {
                semestres.add(rs.getString("semestre"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar semestres: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return semestres;
    }

}