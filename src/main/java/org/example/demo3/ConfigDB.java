package org.example.demo3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigDB {

    private static final Properties properties = new Properties();

    static {
        // 1º tenta ler o db.properties externo (ao lado do .jar)
        File externalConfig = new File("db.properties");
        if (externalConfig.exists()) {
            try (InputStream input = new FileInputStream(externalConfig)) {
                properties.load(input);
                System.out.println("Configuração carregada do arquivo externo.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            // 2º fallback: lê o interno do resources (ambiente de desenvolvimento)
            try (InputStream input = ConfigDB.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (input == null) {
                    System.err.println("Arquivo db.properties não encontrado.");
                } else {
                    properties.load(input);
                    System.out.println("Configuração carregada do resources interno.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getUrl() { return properties.getProperty("db.url"); }
    public static String getName() { return properties.getProperty("db.name"); }
    public static String getUsername() { return properties.getProperty("db.user"); }
    public static String getPassword() { return properties.getProperty("db.password"); }


}