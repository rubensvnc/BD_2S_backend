package org.example.demo3.controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;
import java.net.URL;
import java.util.ResourceBundle;

public class TesteHamburguer implements Initializable {

    @FXML private VBox sideMenu;
    @FXML private StackPane containerFoto;
    private boolean menuAberto = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // 1. Carrega a imagem
            Image imagem = new Image(getClass().getResourceAsStream("/icon_img/user_logo_neutral.png"));

            // 2. Cria o Quadrado com bordas curvas (Rounded Rectangle)
            Rectangle clip = new Rectangle(80, 80);
            clip.setArcWidth(30);  // Curvatura suave horizontal
            clip.setArcHeight(30); // Curvatura suave vertical

            // 3. Preenchimento proporcional (Evita que a imagem estique)
            // Parâmetros: imagem, x, y, width, height, proportional (true)
            clip.setFill(new ImagePattern(imagem, 0, 0, 1, 1, true));

            // 4. Estética de borda moderna
            clip.setStroke(javafx.scene.paint.Color.BLACK);
            clip.setStrokeWidth(1.5);

            // Adiciona ao container
            containerFoto.getChildren().add(clip);

        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem: " + e.getMessage());
        }
    }

    @FXML
    void handleMenu(MouseEvent event) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideMenu);
        if (!menuAberto) {
            slide.setToX(250);
            menuAberto = true;
        } else {
            slide.setToX(0);
            menuAberto = false;
        }
        slide.play();
    }
}