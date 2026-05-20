package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import org.example.demo3.dao.DependenciaTemaDAO;
import org.example.demo3.dao.TemaDAO;

public class ProfTemasController {

    //TABELA DE TEMAS
    @FXML private TableView<?> tabelaTemas;
    @FXML private TableColumn<?, ?> colTemaPrior;
    @FXML private TableColumn<?, ?> colTemaNome;
    @FXML private TableColumn<?, ?> colTemaMin;
    @FXML private TableColumn<?, ?> colTemaMax;
    @FXML private TableColumn<?, ?> colTemaAval;
    @FXML private TableColumn<?, ?> colTemaOpc;
    @FXML private TableColumn<?, ?> colTemaAcoes;
    //FORMULÁRIO
    @FXML private Label lblTituloFormTema;
    @FXML private TextField tfTemaNome;
    @FXML private Label errTemaNome;
    @FXML private Spinner<Integer> spTemaMin;
    @FXML private Spinner<Integer> spTemaMax;
    @FXML private Spinner<Integer> spTemaPrioridade;
    @FXML private CheckBox cbTemaAvaliacao;
    @FXML private CheckBox cbTemaOpcional;
    @FXML private Label lblFeedbackTema;
    // DEPENDÊNCIAS
    @FXML private Label lblTemaSelecionadoDep;
    @FXML private Label lblErrCircular;
    @FXML private ListView<?> listTemasDisponiveis;
    @FXML private ListView<?> listDependencias;

    //DAOS
    private TemaDAO temaDAO = new TemaDAO();
    private DependenciaTemaDAO dependenciaTemaDAO = new DependenciaTemaDAO();

    // INITIALIZE
    @FXML
    public void initialize() {

    }

    // AÇÕES DE TEMAS


    @FXML
    private void handleImportarTemas() {

    }

    @FXML
    private void handleNovoTema() {

    }

    @FXML
    private void handleSelecionarTema() {

    }

    @FXML
    private void handleLimparTema() {

    }

    @FXML
    private void handleSalvarTema() {

    }

    // AÇÕES DE DEPENDÊNCIAS

    @FXML
    private void handleAdicionarDep() {

    }

    @FXML
    private void handleRemoverDep() {

    }

    @FXML
    private void handleSubirDep() {

    }

    @FXML
    private void handleDescerDep() {

    }

    @FXML
    private void handleSalvarDependencias() {

    }

}