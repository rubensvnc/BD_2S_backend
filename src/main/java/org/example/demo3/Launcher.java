package org.example.demo3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Carregamento do FXML utilizando o caminho absoluto (mais seguro)
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dashboard_professor.fxml"));

        // Define a cena com as dimensões apropriadas para comportar as tabelas
        Scene scene = new Scene(fxmlLoader.load(), 1440, 820);

        stage.setTitle("SwiftPlan - Dashboard Professor");
        stage.setScene(scene);
        stage.show();
    }
}