package org.example.demo3;

import java.sql.*;

public class JDBC {
    private static String query;

    public static void main(String[] args) {
        try {
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/teste",
                    "root",
                    "root"
            );
            query = "SELECT ci.data_evento, d.nome, t.nome_tema, ci.observacao, ci.qtd_aulas, ci.dia_semana " +
                    "FROM Cronograma_Itens ci " +
                    "JOIN Temas t ON ci.tema_id = t.id_tema " +
                    "JOIN Disciplinas d ON t.disciplina_id = d.id_disciplina " +
                    "JOIN Cronogramas c ON ci.cronograma_id = c.id_cronograma " +
                    "WHERE c.professor_id = ?";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()){
                System.out.println(resultSet.getString("id_tema"));
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
