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
    @FXML private Label lblTituloFormTema;

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
        configurarListViews();

        tabelaTemas.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    if (newValue != null) {
                        handleSelecionarTema();
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
                errTemaNome.setText("Digite o nome do tema.");
                return;
            }

            Tema tema = new Tema();
            tema.setDisciplina_id(1);
            tema.setSemestre_letivo_id(3);
            tema.setNome(tfTemaNome.getText());
            tema.setQtd_min_aulas(spTemaMin.getValue());
            tema.setQtd_max_aulas(spTemaMax.getValue());
            tema.setPrioridade(spTemaPrioridade.getValue());
            tema.setEh_avaliacao(cbTemaAvaliacao.isSelected() ? 1 : 0);
            tema.setEh_opcional(cbTemaOpcional.isSelected() ? 1 : 0);

            if (temaSelecionado != null && temaSelecionado.getId_tema() != null) {
                // MODO EDIÇÃO — atualiza o tema existente
                tema.setId_tema(temaSelecionado.getId_tema());
                temaDAO.editarTema(tema);         // implemente no DAO se não tiver
                temaSelecionado = tema;
                lblFeedbackTema.setText("Tema atualizado com sucesso!");
            } else {
                // MODO CRIAÇÃO — insere e recupera o ID gerado
                Integer idGerado = temaDAO.inserirTemaRetornandoId(tema); // veja abaixo
                tema.setId_tema(idGerado);
                temaSelecionado = tema;
                lblFeedbackTema.setText("Tema salvo com sucesso!");
            }

            carregarTemas();
            carregarTemasDisponiveis();
            listarDependenciasDoTemaSelecionado();

        } catch (Exception e) {
            lblFeedbackTema.setText("Erro ao salvar tema.");
            System.out.println(e.getMessage());
        }
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

            dependenciaTemaDAO.removerDependenciasTema(
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
    private void handleSelecionarTema() {
        Tema tema = tabelaTemas.getSelectionModel().getSelectedItem();
        if (tema == null) return;

        temaSelecionado = tema;
        lblTemaSelecionadoDep.setText(tema.getNome());

        // Preenche o formulário para edição
        lblTituloFormTema.setText("Editar Tema");
        tfTemaNome.setText(tema.getNome());
        spTemaMin.getValueFactory().setValue(tema.getQtd_min_aulas());
        spTemaMax.getValueFactory().setValue(tema.getQtd_max_aulas());
        spTemaPrioridade.getValueFactory().setValue(tema.getPrioridade());
        cbTemaAvaliacao.setSelected(tema.getEh_avaliacao() == 1);
        cbTemaOpcional.setSelected(tema.getEh_opcional() == 1);

        // Atualiza lista de dependências disponíveis
        carregarTemasDisponiveis();
        listarDependenciasDoTemaSelecionado();
    }

    @FXML
    private void handleLimparTema() {
        tfTemaNome.clear();

        spTemaMin.getValueFactory().setValue(1);

        spTemaMax.getValueFactory().setValue(1);

        spTemaPrioridade
                .getValueFactory()
                .setValue(1);

        cbTemaAvaliacao.setSelected(false);

        cbTemaOpcional.setSelected(false);
    }

    @FXML
    private void handleNovoTema() {
        temaSelecionado = null;              // <-- essencial
        lblTituloFormTema.setText("Novo Tema");
        lblTemaSelecionadoDep.setText("—");
        listDependencias.getItems().clear();
        listTemasDisponiveis.getItems().clear();
        handleLimparTema();
    }

    private void listarDependenciasDoTemaSelecionado() {
        listDependencias.getItems().clear();

        if (temaSelecionado == null) return;

        List<DependenciaTema> listaVinculos =
                dependenciaTemaDAO.listarDependenciasTema(temaSelecionado.getId_tema());

        List<Tema> todosOsTemas = temaDAO.listarTemas();

        for (DependenciaTema dep : listaVinculos) {
            for (Tema t : todosOsTemas) {
                if (t.getId_tema().equals(dep.getTema_dependencia_id())) {
                    listDependencias.getItems().add(t);
                    // Remove da lista de disponíveis para não duplicar
                    listTemasDisponiveis.getItems()
                            .removeIf(item -> item.getId_tema().equals(t.getId_tema()));
                    break;
                }
            }
        }
    }

    private void configurarListViews() {
        listTemasDisponiveis.setCellFactory(lv -> new ListCell<Tema>() {
            @Override
            protected void updateItem(Tema tema, boolean empty) {
                super.updateItem(tema, empty);
                setText(empty || tema == null ? null : tema.getNome());
            }
        });

        listDependencias.setCellFactory(lv -> new ListCell<Tema>() {
            @Override
            protected void updateItem(Tema tema, boolean empty) {
                super.updateItem(tema, empty);
                setText(empty || tema == null ? null : tema.getNome());
            }
        });
    }
}
