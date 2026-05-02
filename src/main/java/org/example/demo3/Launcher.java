package org.example.demo3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Launcher extends Application{
    public static void main(String args[]){
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("dashboard_professor.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1440, 820);

        stage.setTitle("TelaProfessor");
        stage.setScene(scene);
        stage.show();
    }
}
