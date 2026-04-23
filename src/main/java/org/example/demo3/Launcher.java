package org.example.demo3;

import atlantafx.base.theme.PrimerLight;

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
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        //Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("tela_v_planjmnto_prof.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 955, 654);

        String customCss = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(customCss);

        stage.setTitle("TelaProfessor");
        stage.setScene(scene);
        stage.show();
    }
}
