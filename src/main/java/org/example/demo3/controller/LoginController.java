package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class LoginController {

    // Ajustado para bater com o fx:id do FXML
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginSenha;
    @FXML private VBox loginPane;
    @FXML private VBox primeiroAcessoPane;
    @FXML private TextField admNome;
    @FXML private TextField admEmail;
    @FXML private TextField admSenha;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final UsuarioTipoDAO utDao = new UsuarioTipoDAO();

    @FXML
    public void initialize() {
        try {
            if (!usuarioDAO.existeAdministrador()) {
                // Se não houver ADM, esconde o login e mostra o cadastro
                loginPane.setVisible(false);
                loginPane.setManaged(false);
                primeiroAcessoPane.setVisible(true);
                primeiroAcessoPane.setManaged(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleLogin() {
        String email = loginEmail.getText().trim();
        String senha = loginSenha.getText(); // Mantém a senha pura

        if (email.isEmpty() || senha.isEmpty()) {
            exibirAlerta("Campos vazios", "Por favor, preencha o e-mail e a senha.");
            return;
        }

        try {
            Map<String, Object> usuario = usuarioDAO.buscarUsuarioPorEmail(email);

            if (usuario != null) {
                String senhaBanco = (String) usuario.get("senha_hash"); // Aqui agora guardará a senha pura
                String senhaDigitada = loginSenha.getText();

                if (senhaDigitada.equals(senhaBanco)) { // Comparação direta de String
                    UsuarioAtual logado = UsuarioAtual.getInstancia();
                    logado.setId_usuario((Integer) usuario.get("id_usuario"));
                    logado.setTipo((String) usuario.get("tipo"));

                    abrirMainShell();
                } else {
                    exibirAlerta("Erro de Autenticação", "Senha incorreta.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro no Banco", "Falha ao consultar o banco de dados.");
        }
    }

    private void abrirMainShell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main_shell.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginEmail.getScene().getWindow();

            Scene scene = new Scene(root, 1440, 620);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            exibirAlerta("Erro de Carregamento", "Não foi possível abrir o painel principal.");
        }
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void exibirAlertaSucesso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    void handleCadastrarPrimeiroAdm() {
        Usuario novo = new Usuario();
        UsuarioTipo ut = new UsuarioTipo();

        novo.setNome(admNome.getText());
        novo.setEmail(admEmail.getText());
        novo.setSenha_hash(admSenha.getText());
        novo.setCriado_em(LocalDate.now());

        if(admNome.getText() != null && admEmail.getText() != null && admSenha.getText() != null){
            primeiroAcessoPane.setVisible(false);
            primeiroAcessoPane.setManaged(false);

            UsuarioAtual logado = UsuarioAtual.getInstancia();
            try {
                usuarioDAO.inserirUsuario(novo);

                Usuario u = usuarioDAO.buscarUsuarioPorEmailUnico(admEmail.getText());

                ut.setTipo("ADM");
                ut.setUsuario_id(u.getId_usuario());
                utDao.inserirUsuarioTipo(ut);

                logado.setId_usuario(u.getId_usuario());
                logado.setTipo("ADM");
                abrirMainShell();
            } catch (SQLException e){
                e.printStackTrace();
            }


            exibirAlertaSucesso("Sucesso", "Administrador cadastrado! Agora faça login.");
        } else {
            exibirAlerta("FALHA", "Preencha todos os campos");
        }

    }
}