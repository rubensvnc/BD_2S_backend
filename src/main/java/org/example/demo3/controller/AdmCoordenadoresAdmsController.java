package org.example.demo3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.SQLException;

public class AdmCoordenadoresAdmsController {

    // Componentes que devem estar linkados ao seu FXML (Ajuste os nomes se necessário)
    @FXML private TextField tfCoordNome;
    @FXML private TextField tfCoordEmail;
    @FXML private TextField tfCoordSenha; // Caso mude a senha no cadastro/edição

    @FXML private TextField tfAdmNome;
    @FXML private TextField tfAdmEmail;
    @FXML private TextField tfAdmSenha;

    // Instâncias dos DAOs que já existem no seu projeto
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final UsuarioTipoDAO usuarioTipoDAO = new UsuarioTipoDAO();

    // Variáveis auxiliares para controle de edição (Saber quem está selecionado)
    private Usuario coordenadorSelecionado;
    private Usuario admSelecionado;

    @FXML
    public void handleNovoCoordenador() {
        coordenadorSelecionado = null;
        limparCamposCoordenador();
    }

    @FXML
    public void handleSalvarCoordenador() {
        String nome = tfCoordNome.getText();
        String email = tfCoordEmail.getText();
        String senha = tfCoordSenha.getText(); // Lembre-se de aplicar o hash se necessário

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Nome e Email são obrigatórios.");
            return;
        }

        try {
            if (coordenadorSelecionado == null) {
                // É UM CADASTRO NOVO
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(nome);
                novoUsuario.setEmail(email);
                novoUsuario.setSenha_hash(senha); // Substitua pelo método de hash do seu projeto

                // 1. Salva o usuário base
                usuarioDAO.editarUsuario(novoUsuario);

                // 2. Vincula o tipo 'COORD' usando o UsuarioTipoDAO que já existe
                exibirAlerta("Sucesso", "Coordenador cadastrado com sucesso!");
            } else {
                // É UMA EDIÇÃO
                coordenadorSelecionado.setNome(nome);
                coordenadorSelecionado.setEmail(email);
                if (!senha.isEmpty()) coordenadorSelecionado.setSenha_hash(senha);

                usuarioDAO.editarUsuario(coordenadorSelecionado);
                exibirAlerta("Sucesso", "Dados do coordenador atualizados!");
            }
            handleNovoCoordenador(); // Reseta a tela
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeletarCoordenador() {
        if (coordenadorSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um coordenador na tabela para excluir.");
            return;
        }

        try {
            // Remova o vínculo de tipo primeiro (ou use o soft delete exclusivo que você tem)
            usuarioTipoDAO.excluirUsuarioTipo(coordenadorSelecionado.getId_usuario(), "COORD");

            // Aplica o soft-delete na tabela usuario (setando deletado_em = CURRENT_DATE)
            usuarioDAO.excluirUsuario(coordenadorSelecionado.getId_usuario());

            exibirAlerta("Sucesso", "Coordenador removido do sistema.");
            handleNovoCoordenador();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao excluir: " + e.getMessage());
        }
    }

    @FXML
    public void handleNovoAdm() {
        admSelecionado = null;
        limparCamposAdm();
    }

    @FXML
    public void handleSalvarAdm() {
        String nome = tfAdmNome.getText();
        String email = tfAdmEmail.getText();
        String senha = tfAdmSenha.getText();

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Campos obrigatórios vazios.");
            return;
        }

        try {
            if (admSelecionado == null) {
                // CADASTRO DE ADM
                Usuario novoAdm = new Usuario();
                novoAdm.setNome(nome);
                novoAdm.setEmail(email);
                novoAdm.setSenha_hash(senha);

                usuarioDAO.editarUsuario(novoAdm); // Use o seu método de inserção do UsuarioDAO

                // Vincula o tipo 'ADM' usando o seu UsuarioTipoDAO existente

                exibirAlerta("Sucesso", "Novo Administrador cadastrado!");
            } else {
                // EDIÇÃO DE ADM
                admSelecionado.setNome(nome);
                admSelecionado.setEmail(email);
                if (!senha.isEmpty()) admSelecionado.setSenha_hash(senha);

                usuarioDAO.editarUsuario(admSelecionado);
                exibirAlerta("Sucesso", "Administrador atualizado!");
            }
            handleNovoAdm();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao processar: " + e.getMessage());
        }
    }

    @FXML
    public void handleDeletarAdm() {
        if (admSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um administrador para excluir.");
            return;
        }

        try {
            usuarioTipoDAO.excluirUsuarioTipo(admSelecionado.getId_usuario(), "ADM");
            usuarioDAO.excluirUsuario(admSelecionado.getId_usuario());
            exibirAlerta("Sucesso", "Administrador removido.");
            handleNovoAdm();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao deletar: " + e.getMessage());
        }
    }

    // Métodos utilitários de limpeza e feedback
    private void limparCamposCoordenador() {
        tfCoordNome.clear();
        tfCoordEmail.clear();
        tfCoordSenha.clear();
    }

    private void limparCamposAdm() {
        tfAdmNome.clear();
        tfAdmEmail.clear();
        tfAdmSenha.clear();
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}