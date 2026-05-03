package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.demo3.dao.UsuarioDAO;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML TextField txtUsuario, txtSenha;
    @FXML Button btnLogar;

    @FXML
    public void Logar(){
        String email = txtUsuario.getText();
        String senha = txtSenha.getText();

        if (email != null && !email.isEmpty() && senha != null && !senha.isEmpty()) {

            try {
                UsuarioDAO dao = new UsuarioDAO();

                String tipoUsuario = dao.buscarTipoPorEmailESenha(email, senha);

                if (tipoUsuario != null) {
                    System.out.println("Login bem-sucedido! Tipo: " + tipoUsuario);

                    if (tipoUsuario.equals("ADM")) {
                        trocarTela("/dashboard_adm.fxml", "Painel Administrativo");
                    } else {
                        if (tipoUsuario.equals("PROF")) {
                            trocarTela("/dashboard_professor.fxml", "Dashboard Professor");
                        }
                        // Abrir Painel Geral
                    }
                } else {
                    System.out.println("Usuário ou senha inválidos.");
                }
            } catch (SQLException e){

            }

        } else {
            System.out.println("Por favor, preencha todos os campos.");
        }
    }

    private void trocarTela(String fxml, String titulo) {
        try {
            // 1. Carrega o novo arquivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene novaCena = new Scene(loader.load());

            // 2. Pega a Janela (Stage) atual através do botão
            Stage stage = (Stage) btnLogar.getScene().getWindow();

            // 3. Troca o conteúdo e o título
            stage.setScene(novaCena);
            stage.setTitle(titulo);
            stage.centerOnScreen(); // Opcional: centraliza a nova janela
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
