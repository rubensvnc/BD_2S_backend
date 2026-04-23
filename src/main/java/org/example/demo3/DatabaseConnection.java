package org.example.demo3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection(String database, String user, String password) throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/"+database, user, password);
    }
}
