package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

import java.time.LocalDate;
import java.util.List;

public class AdmCoordenadoresAdmsController {

    @FXML private TableView<Usuario> tabelaCoordenadores;
    @FXML private TableColumn<Usuario, String> colCoordNome;
    @FXML private TableColumn<Usuario, String> colCoordEmail;
    @FXML private TableColumn<Usuario, String> colCoordCurso;

    @FXML private Label lblTituloFormCoord;
    @FXML private TextField tfCoordNome;
    @FXML private TextField tfCoordEmail;
    @FXML private PasswordField pfCoordSenha;
    @FXML private ComboBox<String> cbCoordCurso;

    @FXML private TableView<Usuario> tabelaAdms;
    @FXML private TableColumn<Usuario, String> colAdmNome;
    @FXML private TableColumn<Usuario, String> colAdmEmail;

    @FXML private Label lblTituloFormAdm;
    @FXML private TextField tfAdmNome;
    @FXML private TextField tfAdmEmail;
    @FXML private PasswordField pfAdmSenha;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final UsuarioTipoDAO usuarioTipoDAO = new UsuarioTipoDAO();

    private final ObservableList<Usuario> listaCoordenadoresFX = FXCollections.observableArrayList();
    private final ObservableList<Usuario> listaAdmsFX = FXCollections.observableArrayList();

    private Usuario coordenadorSelecionado;
    private Usuario admSelecionado;

    @FXML
    public void initialize() {
        colCoordNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCoordEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colCoordCurso != null) {
            colCoordCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        }

        colAdmNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colAdmEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        tabelaCoordenadores.setItems(listaCoordenadoresFX);
        tabelaAdms.setItems(listaAdmsFX);

        atualizarTabelas();
    }

    private void atualizarTabelas() {
        try {
            listaCoordenadoresFX.clear();
            listaAdmsFX.clear();

            List<Usuario> todosUsuarios = usuarioDAO.listarUsuariosComTipo();

            for (Usuario user : todosUsuarios) {

                if ("COORD".equalsIgnoreCase(user.getTipo())) {
                    listaCoordenadoresFX.add(user);
                } else if ("ADM".equalsIgnoreCase(user.getTipo())) {
                    listaAdmsFX.add(user);
                }
            }
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao carregar dados do banco: " + e.getMessage());
        }
    }

    @FXML
    public void handleSelecionarCoordenador(MouseEvent event) {
        Usuario selecionado = tabelaCoordenadores.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            coordenadorSelecionado = selecionado;
            lblTituloFormCoord.setText("Editar Coordenador");
            tfCoordNome.setText(coordenadorSelecionado.getNome());
            tfCoordEmail.setText(coordenadorSelecionado.getEmail());
            pfCoordSenha.clear();
        }
    }

    @FXML
    public void handleSelecionarAdm(MouseEvent event) {
        Usuario selecionado = tabelaAdms.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            admSelecionado = selecionado;
            lblTituloFormAdm.setText("Editar Administrador");
            tfAdmNome.setText(admSelecionado.getNome());
            tfAdmEmail.setText(admSelecionado.getEmail());
            pfAdmSenha.clear();
        }
    }

    @FXML
    public void handleNovoCoordenador() {
        coordenadorSelecionado = null;
        tabelaCoordenadores.getSelectionModel().clearSelection();
        lblTituloFormCoord.setText("Novo Coordenador");
        handleLimparFormCoord();
    }

    @FXML
    public void handleSalvarCoordenador() {
        String nome = tfCoordNome.getText().trim();
        String email = tfCoordEmail.getText().trim();
        String senha = pfCoordSenha.getText();

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Nome e Email são obrigatórios.");
            return;
        }

        try {
            if (coordenadorSelecionado == null) {
                if (usuarioDAO.emailJaExiste(email)) {
                    exibirAlerta("Erro", "Este e-mail já está cadastrado.");
                    return;
                }

                Usuario novoUsuario = new Usuario();
                novoUsuario.setNome(nome);
                novoUsuario.setEmail(email);
                novoUsuario.setSenha_hash(senha);
                novoUsuario.setCriado_em(LocalDate.now());

                int novoId = usuarioDAO.inserirUsuarioRetornandoId(novoUsuario);
                if (novoId != -1) {
                    UsuarioTipo ut = new UsuarioTipo();
                    ut.setUsuario_id(novoId);
                    ut.setTipo("COORD");

                    usuarioTipoDAO.inserirUsuarioTipo(ut);
                    exibirAlerta("Sucesso", "Coordenador cadastrado com sucesso!");
                } else {
                    exibirAlerta("Erro", "Falha interna ao gerar chave para o Usuário.");
                }
            } else {
                coordenadorSelecionado.setNome(nome);
                coordenadorSelecionado.setEmail(email);
                if (!senha.isEmpty()) coordenadorSelecionado.setSenha_hash(senha);

                usuarioDAO.editarUsuario(coordenadorSelecionado);
                exibirAlerta("Sucesso", "Dados do coordenador atualizados!");
            }
            atualizarTabelas();
            handleNovoCoordenador();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar Coordenador: " + e.getMessage());
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
            atualizarTabelas();
            handleNovoCoordenador();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao excluir: " + e.getMessage());
        }
    }

    @FXML
    public void handleNovoAdm() {
        admSelecionado = null;
        tabelaAdms.getSelectionModel().clearSelection();
        lblTituloFormAdm.setText("Novo Administrador");
        handleLimparFormAdm();
    }

    @FXML
    public void handleSalvarAdm() {
        String nome = tfAdmNome.getText().trim();
        String email = tfAdmEmail.getText().trim();
        String senha = pfAdmSenha.getText();

        if (nome.isEmpty() || email.isEmpty()) {
            exibirAlerta("Erro", "Campos obrigatórios vazios.");
            return;
        }

        try {
            if (admSelecionado == null) {
                if (usuarioDAO.emailJaExiste(email)) {
                    exibirAlerta("Erro", "Este e-mail já está cadastrado.");
                    return;
                }

                Usuario novoAdm = new Usuario();
                novoAdm.setNome(nome);
                novoAdm.setEmail(email);
                novoAdm.setSenha_hash(senha);
                novoAdm.setCriado_em(LocalDate.now());

                int novoId = usuarioDAO.inserirUsuarioRetornandoId(novoAdm);
                if (novoId != -1) {
                    UsuarioTipo ut = new UsuarioTipo();
                    ut.setUsuario_id(novoId);
                    ut.setTipo("ADM");

                    usuarioTipoDAO.inserirUsuarioTipo(ut);
                    exibirAlerta("Sucesso", "Novo Administrador cadastrado!");
                } else {
                    exibirAlerta("Erro", "Falha interna ao gerar chave para o Administrador.");
                }
            } else {
                admSelecionado.setNome(nome);
                admSelecionado.setEmail(email);
                if (!senha.isEmpty()) admSelecionado.setSenha_hash(senha);

                usuarioDAO.editarUsuario(admSelecionado);
                exibirAlerta("Sucesso", "Administrador atualizado!");
            }
            atualizarTabelas();
            handleNovoAdm();
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao processar Administrador: " + e.getMessage());
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
            atualizarTabelas();
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