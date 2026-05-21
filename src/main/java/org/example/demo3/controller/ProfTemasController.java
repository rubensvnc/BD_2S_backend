package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import org.example.demo3.dao.DependenciaTemaDAO;
import org.example.demo3.dao.TemaDAO;
import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.SemestreLetivo;
import org.example.demo3.entity.Tema;

import java.time.LocalDate;
import java.util.List;

public class ProfTemasController {

    //TABELA DE TEMAS
    @FXML private TableView<Tema> tabelaTemas;
    @FXML private TableColumn<Tema, Integer> colTemaPrior;
    @FXML private TableColumn<Tema, String> colTemaNome;
    @FXML private TableColumn<Tema, Integer> colTemaMin;
    @FXML private TableColumn<Tema, Integer> colTemaMax;
    @FXML private TableColumn<Tema, String> colTemaAval;
    @FXML private TableColumn<Tema, String> colTemaOpc;
    @FXML private TableColumn<Tema, String> colTemaAcoes;
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
    @FXML private ListView<Tema> listTemasDisponiveis;
    @FXML private ListView<Tema> listDependencias;
    //DAOS
    private TemaDAO temaDAO = new TemaDAO();
    private DependenciaTemaDAO dependenciaTemaDAO = new DependenciaTemaDAO();
    // INITIALIZE
    @FXML
    public void initialize() {
        configurarSpinners();
        listarTemasnaTabela();
        carregarTemas();
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
        limparFormulario();
    }

    @FXML
    private void handleSalvarTema() {
        try {

            errTemaNome.setText("");

            if (tfTemaNome.getText().isBlank()) {
                errTemaNome.setText("Digite o nome do tema.");
                return;
            }
            Tema tema = new Tema();

            // IDs FIXOS PARA TESTE
            tema.setDisciplina_id(1);
            tema.setSemestre_letivo_id(3);

            // CAMPOS DO FORMULÁRIO
            tema.setNome(tfTemaNome.getText());
            tema.setQtd_min_aulas(spTemaMin.getValue());
            tema.setQtd_max_aulas(spTemaMax.getValue());
            tema.setPrioridade(spTemaPrioridade.getValue());

            // CHECKBOX → INT
            tema.setEh_avaliacao(cbTemaAvaliacao.isSelected() ? 1 : 0);
            tema.setEh_opcional(cbTemaOpcional.isSelected() ? 1 : 0);

            // INSERT NO BANCO
            temaDAO.inserirTema(tema);

            // FEEDBACK
            lblFeedbackTema.setText("Tema salvo com sucesso!");

            // LIMPAR FORM
            handleLimparTema();
            carregarTemas();

        } catch (Exception e) {

            lblFeedbackTema.setText("Erro ao salvar tema.");

            System.out.println(e.getMessage());
        }
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

    @FXML
    private void limparFormulario() {
        tfTemaNome.clear();
        spTemaMin.getValueFactory().setValue(1);
        spTemaMax.getValueFactory().setValue(1);
        spTemaPrioridade.getValueFactory().setValue(1);
        cbTemaAvaliacao.setSelected(false);
        cbTemaOpcional.setSelected(false);
    }


    private void carregarTemas() {

        List<Tema> temas = temaDAO.listarTemas();

        tabelaTemas.getItems().clear();

        tabelaTemas.getItems().addAll(temas);
    }

    private void configurarSpinners() {
        spTemaMin.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );

        spTemaMax.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1)
        );

        spTemaPrioridade.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1)
        );
    }

    private void listarTemasnaTabela() {
        colTemaPrior.setCellValueFactory(
                new PropertyValueFactory<>("prioridade")
        );

        colTemaNome.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );

        colTemaMin.setCellValueFactory(
                new PropertyValueFactory<>("qtd_min_aulas")
        );

        colTemaMax.setCellValueFactory(
                new PropertyValueFactory<>("qtd_max_aulas")
        );

        colTemaAval.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getEh_avaliacao() == 1 ? "Sim" : "Não"
                )
        );

        colTemaOpc.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getEh_opcional() == 1 ? "Sim" : "Não"
                )
        );

    }

}