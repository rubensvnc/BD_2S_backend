package org.example.demo3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main2 extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // O caminho deve ser relativo à pasta resources [cite: 70, 71]
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/dashboard_adm.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("SwiftPlan - Dashboard ADM");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}