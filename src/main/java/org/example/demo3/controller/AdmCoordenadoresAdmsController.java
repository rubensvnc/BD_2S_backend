package org.example.demo3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.Usuario;

public class AdmCoordenadoresAdmsController {

    @FXML private TableView<Usuario> tabelaCoordenadores;
    @FXML private TableColumn<Usuario, String> colCoordNome;
    @FXML private TableColumn<Usuario, String> colCoordEmail;
    @FXML private TableColumn<Usuario, String> colCoordCurso;
    @FXML private TableColumn<Usuario, String> colCoordAcoes;

    @FXML private Label lblTituloFormCoord;
    @FXML private TextField tfCoordNome;
    @FXML private TextField tfCoordEmail;
    @FXML private PasswordField pfCoordSenha; // Corrigido para PasswordField conforme o FXML
    @FXML private ComboBox<String> cbCoordCurso;

    @FXML private Label errCoordNome;
    @FXML private Label errCoordEmail;
    @FXML private Label errCoordSenha;
    @FXML private Label errCoordCurso;
    @FXML private Label lblFeedbackCoord;

    @FXML private TableView<Usuario> tabelaAdms;
    @FXML private TableColumn<Usuario, String> colAdmNome;
    @FXML private TableColumn<Usuario, String> colAdmEmail;
    @FXML private TableColumn<Usuario, String> colAdmAcoes;

    @FXML private Label lblTituloFormAdm;
    @FXML private TextField tfAdmNome;
    @FXML private TextField tfAdmEmail;
    @FXML private PasswordField pfAdmSenha; // Corrigido para PasswordField conforme o FXML

    @FXML private Label errAdmNome;
    @FXML private Label errAdmEmail;
    @FXML private Label errAdmSenha;
    @FXML private Label lblFeedbackAdm;

    // --- INSTÂNCIAS DOS DAOS ---
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final UsuarioTipoDAO usuarioTipoDAO = new UsuarioTipoDAO();

    private Usuario coordenadorSelecionado;
    private Usuario admSelecionado;

    @FXML
    public void handleNovoCoordenador() {
        coordenadorSelecionado = null;
        lblTituloFormCoord.setText("Novo Coordenador");
        handleLimparFormCoord();
    }

    @FXML
    private void handleSelecionarCoordenador(MouseEvent event) {
        if (tabelaCoordenadores != null) {
            coordenadorSelecionado = tabelaCoordenadores.getSelectionModel().getSelectedItem();
            if (coordenadorSelecionado != null) {
                lblTituloFormCoord.setText("Editar Coordenador");
                tfCoordNome.setText(coordenadorSelecionado.getNome());
                tfCoordEmail.setText(coordenadorSelecionado.getEmail());
                pfCoordSenha.clear(); // Senha em branco por segurança na edição
            }
        }
    }

    @FXML
    public void handleSalvarCoordenador() {
        String nome = tfCoordNome.getText();
        String email = tfCoordEmail.getText();
        String senha = pfCoordSenha.getText();

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Nome e Email são obrigatórios.");
            return;
        }

        try {
            if (coordenadorSelecionado == null) {
                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(nome);
                novoUsuario.setEmail(email);
                novoUsuario.setSenha_hash(senha);

                usuarioDAO.editarUsuario(novoUsuario);
                exibirAlerta("Sucesso", "Coordenador cadastrado com sucesso!");
            } else {
                coordenadorSelecionado.setNome(nome);
                coordenadorSelecionado.setEmail(email);
                if (!senha.isEmpty()) coordenadorSelecionado.setSenha_hash(senha);

                usuarioDAO.editarUsuario(coordenadorSelecionado);
                exibirAlerta("Sucesso", "Dados do coordenador atualizados!");
            }
            handleNovoCoordenador();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar: " + e.getMessage());
        }
    }

    @FXML
    public void handleLimparFormCoord() {
        tfCoordNome.clear();
        tfCoordEmail.clear();
        pfCoordSenha.clear();
        if (cbCoordCurso != null) cbCoordCurso.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleDeletarCoordenador() {
        if (coordenadorSelecionado == null) {
            exibirAlerta("Aviso", "Selecione um coordenador na tabela para excluir.");
            return;
        }
        try {
            usuarioTipoDAO.excluirUsuarioTipo(coordenadorSelecionado.getId_usuario(), "COORD");
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
        lblTituloFormAdm.setText("Novo Administrador");
        handleLimparFormAdm();
    }

    @FXML
    private void handleSelecionarAdm(MouseEvent event) {
        if (tabelaAdms != null) {
            admSelecionado = tabelaAdms.getSelectionModel().getSelectedItem();
            if (admSelecionado != null) {
                lblTituloFormAdm.setText("Editar Administrador");
                tfAdmNome.setText(admSelecionado.getNome());
                tfAdmEmail.setText(admSelecionado.getEmail());
                pfAdmSenha.clear();
            }
        }
    }

    @FXML
    public void handleSalvarAdm() {
        String nome = tfAdmNome.getText();
        String email = tfAdmEmail.getText();
        String senha = pfAdmSenha.getText();

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Campos obrigatórios vazios.");
            return;
        }

        try {
            if (admSelecionado == null) {
                Usuario novoAdm = new Usuario();
                novoAdm.setNome(nome);
                novoAdm.setEmail(email);
                novoAdm.setSenha_hash(senha);

                usuarioDAO.editarUsuario(novoAdm);
                exibirAlerta("Sucesso", "Novo Administrador cadastrado!");
            } else {
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
    public void handleLimparFormAdm() {
        tfAdmNome.clear();
        tfAdmEmail.clear();
        pfAdmSenha.clear();
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

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}