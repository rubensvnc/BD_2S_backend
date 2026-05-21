package org.example.demo3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigDB {

    private static final Properties properties = new Properties();

    static {
        // Carrega o arquivo dentro da pasta 'resources'
        try (InputStream input = ConfigDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.err.println("Desculpe, não foi possível encontrar o arquivo db.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getUrl() { return properties.getProperty("db.url"); }
    public static String getName() { return properties.getProperty("db.name"); }
    public static String getUsername() { return properties.getProperty("db.user"); }
    public static String getPassword() { return properties.getProperty("db.password"); }


}