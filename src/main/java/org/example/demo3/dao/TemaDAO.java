package org.example.demo3.dao;

import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.Tema;
import org.example.demo3.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TemaDAO {

    private Connection getConn() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Tema> buscarTodos() throws SQLException {
        List<Tema> lista = new ArrayList<>();

        // JOIN para trazer o nome da disciplina para a TableView
        String sql = "SELECT t.*, d.nome AS nome_disciplina " +
                "FROM tema t " +
                "INNER JOIN disciplina d ON t.disciplina_id = d.id_disciplina";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Tema t = new Tema();
                t.setId(rs.getInt("id_tema"));
                t.setDisciplinaId(rs.getInt("disciplina_id"));
                t.setNome(rs.getString("nome"));
                t.setNomeDisciplina(rs.getString("nome_disciplina"));
                t.setPrioridade(rs.getInt("prioridade"));
                t.setQtdMinAulas(rs.getInt("qtd_min_aulas"));
                t.setQtdMaxAulas(rs.getInt("qtd_max_aulas"));
                t.setEhAvaliacao(rs.getBoolean("eh_avaliacao"));
                t.setEhOpcional(rs.getBoolean("eh_opcional"));

                // Busca as dependências usando a mesma conexão
                t.setDependencias(buscarDependenciasDoTema(conn, t.getId()));
                lista.add(t);
            }
        }
        return lista;
    }

    public void salvar(Tema tema) throws SQLException {
        String sqlTema = "INSERT INTO tema (disciplina_id, nome, prioridade, qtd_min_aulas, qtd_max_aulas, eh_avaliacao, eh_opcional) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlDep = "INSERT INTO tema_dependencia (tema_anterior, tema_posterior) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = getConn();
            conn.setAutoCommit(false);

            PreparedStatement psTema = conn.prepareStatement(sqlTema, Statement.RETURN_GENERATED_KEYS);
            psTema.setInt(1, tema.getDisciplinaId());
            psTema.setString(2, tema.getNome());
            psTema.setInt(3, tema.getPrioridade());
            psTema.setInt(4, tema.getQtdMinAulas());
            psTema.setInt(5, tema.getQtdMaxAulas());
            psTema.setBoolean(6, tema.isEhAvaliacao());
            psTema.setBoolean(7, tema.isEhOpcional());
            psTema.executeUpdate();

            // Recupera o ID gerado para o novo tema
            try (ResultSet rs = psTema.getGeneratedKeys()) {
                if (rs.next()) {
                    tema.setId(rs.getInt(1));
                }
            }

            // Salva as dependências em lote (batch)
            if (tema.getDependencias() != null && !tema.getDependencias().isEmpty()) {
                try (PreparedStatement psDep = conn.prepareStatement(sqlDep)) {
                    for (Tema dep : tema.getDependencias()) {
                        psDep.setInt(1, dep.getId()); // O tema que já existia (anterior)
                        psDep.setInt(2, tema.getId()); // O tema que estamos salvando agora (posterior)
                        psDep.addBatch();
                    }
                    psDep.executeBatch();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.close();
        }
    }

    public List<Disciplina> buscarTodasDisciplinasPorProfessor(int idProfessor) throws SQLException {
        List<Disciplina> lista = new ArrayList<>();

        // SQL filtrando pela tabela de atribuição para garantir que só venham disciplinas do professor
        // Usamos DISTINCT porque um professor pode ter várias atribuições (dias diferentes) para a mesma disciplina
        String sql = "SELECT DISTINCT d.*, c.nome AS nome_curso " +
                "FROM disciplina d " +
                "INNER JOIN curso c ON d.curso_id = c.id_curso " +
                "INNER JOIN atribuicao a ON d.id_disciplina = a.disciplina_id " +
                "WHERE a.usuario_id = ?";

        try (Connection conn = getConn();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Definindo o ID do professor no parâmetro do PreparedStatement
            ps.setInt(1, idProfessor);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Disciplina d = new Disciplina(
                            rs.getInt("id_disciplina"),
                            rs.getInt("curso_id"),
                            rs.getString("nome"),
                            rs.getInt("carga_horaria"),
                            rs.getInt("semestre")
                    );
                    // Se sua entidade tiver o campo nomeCurso, você pode preenchê-lo aqui:
                    // d.setNomeCurso(rs.getString("nome_curso"));
                    lista.add(d);
                }
            }
        }
        return lista;
    }

    private List<Tema> buscarDependenciasDoTema(Connection conn, int idTema) throws SQLException {
        List<Tema> deps = new ArrayList<>();
        String sql = "SELECT t.id_tema, t.nome FROM tema t " +
                "JOIN tema_dependencia td ON td.tema_anterior = t.id_tema " +
                "WHERE td.tema_posterior = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTema);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tema dep = new Tema();
                    dep.setId(rs.getInt("id_tema"));
                    dep.setNome(rs.getString("nome"));
                    deps.add(dep);
                }
            }
        }
        return deps;
    }

    public void excluir(int idTema) throws SQLException {
        // Exclui primeiro as referências na tabela de dependência para evitar erro de Foreign Key
        String sqlDep = "DELETE FROM tema_dependencia WHERE tema_anterior = ? OR tema_posterior = ?";
        String sqlTema = "DELETE FROM tema WHERE id_tema = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psDep = conn.prepareStatement(sqlDep);
                 PreparedStatement psTema = conn.prepareStatement(sqlTema)) {

                psDep.setInt(1, idTema);
                psDep.setInt(2, idTema);
                psDep.executeUpdate();

                psTema.setInt(1, idTema);
                psTema.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Tema> listarPorDisciplina(int discId) throws SQLException {
        String sql = "SELECT * FROM tema WHERE disciplina_id = ? ORDER BY prioridade ASC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Tema> lista = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, discId);
            rs = ps.executeQuery();

            while (rs.next()) {
                Tema tema = new Tema();
                tema.setId(rs.getInt("id_tema"));
                tema.setDisciplinaId(rs.getInt("disciplina_id"));
                tema.setNome(rs.getString("nome"));
                tema.setPrioridade(rs.getInt("prioridade"));
                tema.setQtdMinAulas(rs.getInt("qtd_min_aulas"));
                tema.setQtdMaxAulas(rs.getInt("qtd_max_aulas"));
                tema.setEhAvaliacao(rs.getBoolean("eh_avaliacao"));
                tema.setEhOpcional(rs.getBoolean("eh_opcional"));
                lista.add(tema);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar temas: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
        return lista;
    }

    public boolean verificarDependencia(int temaId) throws SQLException {
        // Verifica se existe algum tema anterior na tabela tema_dependencia para o tema_posterior informado
        String sql = "SELECT 1 FROM tema_dependencia WHERE tema_posterior = ? LIMIT 1";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, temaId);
            rs = ps.executeQuery();

            return rs.next(); // Retorna true se houver dependência cadastrada
        } catch (SQLException e) {
            System.err.println("Erro ao verificar dependência do tema: " + e.getMessage());
            throw e;
        } finally {
            DatabaseConnection.closeConnection();
        }
    }
}