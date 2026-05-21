package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.demo3.dao.DependenciaTemaDAO;
import org.example.demo3.dao.TemaDAO;
import org.example.demo3.entity.DependenciaTema;
import org.example.demo3.entity.Tema;

import java.util.List;

public class ProfTemasController {

    // TABELA
    @FXML private TableView<Tema> tabelaTemas;

    @FXML private TableColumn<Tema, Integer> colTemaPrior;
    @FXML private TableColumn<Tema, String> colTemaNome;
    @FXML private TableColumn<Tema, Integer> colTemaMin;
    @FXML private TableColumn<Tema, Integer> colTemaMax;
    @FXML private TableColumn<Tema, String> colTemaAval;
    @FXML private TableColumn<Tema, String> colTemaOpc;

    // FORMULÁRIO
    @FXML private TextField tfTemaNome;

    @FXML private Spinner<Integer> spTemaMin;
    @FXML private Spinner<Integer> spTemaMax;
    @FXML private Spinner<Integer> spTemaPrioridade;

    @FXML private CheckBox cbTemaAvaliacao;
    @FXML private CheckBox cbTemaOpcional;

    @FXML private Label lblFeedbackTema;
    @FXML private Label errTemaNome;

    // DEPENDÊNCIAS
    @FXML private Label lblTemaSelecionadoDep;
    @FXML private Label lblErrCircular;

    @FXML private ListView<Tema> listTemasDisponiveis;
    @FXML private ListView<Tema> listDependencias;

    // DAO
    private TemaDAO temaDAO = new TemaDAO();
    private DependenciaTemaDAO dependenciaTemaDAO =
            new DependenciaTemaDAO();

    // TEMA SELECIONADO
    private Tema temaSelecionado;

    // INITIALIZE
    @FXML
    public void initialize() {

        configurarSpinners();

        configurarTabela();

        carregarTemas();

        configurarCliqueTabela();
    }

    // CONFIGURAR CLIQUE NA TABELA
    private void configurarCliqueTabela() {

        tabelaTemas.setOnMouseClicked(event -> {

            Tema tema = tabelaTemas
                    .getSelectionModel()
                    .getSelectedItem();

            if (tema != null) {

                temaSelecionado = tema;

                lblTemaSelecionadoDep.setText(
                        temaSelecionado.getNome()
                );

                carregarTemasDisponiveis();
            }
        });
    }

    // CONFIGURA TABELA
    private void configurarTabela() {

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

        colTemaAval.setCellValueFactory(cellData -> {

            if (cellData.getValue().getEh_avaliacao() == 1) {
                return new SimpleStringProperty("Sim");
            } else {
                return new SimpleStringProperty("Não");
            }
        });

        colTemaOpc.setCellValueFactory(cellData -> {

            if (cellData.getValue().getEh_opcional() == 1) {
                return new SimpleStringProperty("Sim");
            } else {
                return new SimpleStringProperty("Não");
            }
        });
    }

    // CARREGAR TEMAS
    private void carregarTemas() {

        List<Tema> temas = temaDAO.listarTemas();

        tabelaTemas.getItems().clear();

        tabelaTemas.getItems().addAll(temas);
    }

    // CARREGAR TEMAS DISPONÍVEIS
    private void carregarTemasDisponiveis() {

        listTemasDisponiveis.getItems().clear();

        List<Tema> temas = temaDAO.listarTemas();

        for (Tema tema : temas) {

            // NÃO MOSTRAR O PRÓPRIO TEMA
            if (tema.getId_tema() !=
                    temaSelecionado.getId_tema()) {

                listTemasDisponiveis.getItems().add(tema);
            }
        }
    }

    // SPINNERS
    private void configurarSpinners() {

        spTemaMin.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(
                        1, 100, 1
                )
        );

        spTemaMax.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(
                        1, 100, 1
                )
        );

        spTemaPrioridade.setValueFactory(
                new SpinnerValueFactory
                        .IntegerSpinnerValueFactory(
                        1, 999, 1
                )
        );
    }

    // SALVAR TEMA
    @FXML
    private void handleSalvarTema() {

        try {

            if (tfTemaNome.getText().isBlank()) {

                errTemaNome.setText(
                        "Digite o nome do tema."
                );

                return;
            }

            Tema tema = new Tema();

            // TESTE
            tema.setDisciplina_id(1);
            tema.setSemestre_letivo_id(3);

            tema.setNome(tfTemaNome.getText());

            tema.setQtd_min_aulas(
                    spTemaMin.getValue()
            );

            tema.setQtd_max_aulas(
                    spTemaMax.getValue()
            );

            tema.setPrioridade(
                    spTemaPrioridade.getValue()
            );

            tema.setEh_avaliacao(
                    cbTemaAvaliacao.isSelected() ? 1 : 0
            );

            tema.setEh_opcional(
                    cbTemaOpcional.isSelected() ? 1 : 0
            );

            temaDAO.inserirTema(tema);

            lblFeedbackTema.setText(
                    "Tema salvo com sucesso!"
            );

            limparFormulario();

            carregarTemas();

        } catch (Exception e) {

            lblFeedbackTema.setText(
                    "Erro ao salvar tema."
            );

            System.out.println(e.getMessage());
        }
    }

    // LIMPAR FORMULÁRIO
    @FXML
    private void limparFormulario() {

        tfTemaNome.clear();

        spTemaMin.getValueFactory().setValue(1);

        spTemaMax.getValueFactory().setValue(1);

        spTemaPrioridade
                .getValueFactory()
                .setValue(1);

        cbTemaAvaliacao.setSelected(false);

        cbTemaOpcional.setSelected(false);
    }

    // ADICIONAR DEPENDÊNCIA
    @FXML
    private void handleAdicionarDep() {

        Tema temaSelecionadoLista =
                listTemasDisponiveis
                        .getSelectionModel()
                        .getSelectedItem();

        if (temaSelecionadoLista != null) {

            listDependencias
                    .getItems()
                    .add(temaSelecionadoLista);

            listTemasDisponiveis
                    .getItems()
                    .remove(temaSelecionadoLista);
        }
    }

    // REMOVER DEPENDÊNCIA
    @FXML
    private void handleRemoverDep() {

        Tema temaSelecionadoLista =
                listDependencias
                        .getSelectionModel()
                        .getSelectedItem();

        if (temaSelecionadoLista != null) {

            listTemasDisponiveis
                    .getItems()
                    .add(temaSelecionadoLista);

            listDependencias
                    .getItems()
                    .remove(temaSelecionadoLista);
        }
    }

    // SUBIR DEPENDÊNCIA
    @FXML
    private void handleSubirDep() {

        int index = listDependencias
                .getSelectionModel()
                .getSelectedIndex();

        if (index > 0) {

            Tema tema =
                    listDependencias
                            .getItems()
                            .remove(index);

            listDependencias
                    .getItems()
                    .add(index - 1, tema);

            listDependencias
                    .getSelectionModel()
                    .select(index - 1);
        }
    }

    // DESCER DEPENDÊNCIA
    @FXML
    private void handleDescerDep() {

        int index = listDependencias
                .getSelectionModel()
                .getSelectedIndex();

        if (index <
                listDependencias.getItems().size() - 1
                && index >= 0) {

            Tema tema =
                    listDependencias
                            .getItems()
                            .remove(index);

            listDependencias
                    .getItems()
                    .add(index + 1, tema);

            listDependencias
                    .getSelectionModel()
                    .select(index + 1);
        }
    }

    // SALVAR DEPENDÊNCIAS
    @FXML
    private void handleSalvarDependencias() {

        if (temaSelecionado == null) {

            lblFeedbackTema.setText(
                    "Selecione um tema."
            );

            return;
        }

        try {

            dependenciaTemaDAO
                    .removerDependenciasTema(
                            temaSelecionado.getId_tema()
                    );

            for (int i = 0;
                 i < listDependencias.getItems().size();
                 i++) {

                Tema temaDependencia =
                        listDependencias
                                .getItems()
                                .get(i);

                DependenciaTema dependencia =
                        new DependenciaTema();

                dependencia.setTema_id(
                        temaSelecionado.getId_tema()
                );

                dependencia.setTema_dependencia_id(
                        temaDependencia.getId_tema()
                );

                dependencia.setOrdem(i + 1);

                dependenciaTemaDAO
                        .inserirDependencia(
                                dependencia
                        );
            }

            lblFeedbackTema.setText(
                    "Dependências salvas!"
            );

        } catch (Exception e) {

            lblFeedbackTema.setText(
                    "Erro ao salvar dependências."
            );

            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void handleImportarTemas(){}

    @FXML
    public void handleNovoTema(){}

    @FXML
    public void handleSelecionarTema(){}

    @FXML
    public void handleLimparTema(){}
}
