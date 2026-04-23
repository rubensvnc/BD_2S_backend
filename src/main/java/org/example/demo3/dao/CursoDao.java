package org.example.demo3.dao;

import org.example.demo3.DatabaseConnection;
import org.example.demo3.entity.Curso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CursoDao {

    public static List<Curso> atualizarCursos(){
        List<Curso> cursos = new ArrayList<Curso>();
        Connection con = null;
        try {
            con = DatabaseConnection.getConnection("organizacao_aulas_fatec", "root", "root");
            String select_query = "SELECT * FROM curso";
            PreparedStatement pstm;
            pstm = con.prepareStatement(select_query);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()){
                Curso curso = new Curso();
                curso.setId(rs.getInt("id_curso"));
                curso.setNome(rs.getString("nome"));
                curso.setCoordenador_id(rs.getInt("coordenador_id"));

                cursos.add(curso);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar cursos!", e);
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw new RuntimeException("Erro ao fechar conexão", e);
            }
        }
        return cursos;
    }
}
