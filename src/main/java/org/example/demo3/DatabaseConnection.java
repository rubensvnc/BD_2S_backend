package org.example.demo3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Verifique se o nome do seu banco é 'swiftplan'
    private static final String URL = "jdbc:mysql://localhost:3306/swiftplan";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Coloque sua senha do MySQL aqui

    public static Connection getConnection() throws SQLException {
        try {
            // Registra o driver explicitamente para evitar erros em algumas versões do JDK
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver JDBC não encontrado!", e);
        }
    }
}