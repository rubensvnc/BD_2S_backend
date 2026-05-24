package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.demo3.dao.DependenciaTemaDAO;
import org.example.demo3.dao.TemaDAO;
import org.example.demo3.entity.DependenciaTema;
import org.example.demo3.entity.Tema;

import java.util.ArrayList;
import java.util.List;

public class ProfTemasController {

    // FXML — TABELA
    @FXML private TableView<Tema>            tabelaTemas;
    @FXML private TableColumn<Tema, Integer> colTemaPrior;
    @FXML private TableColumn<Tema, String>  colTemaNome;
    @FXML private TableColumn<Tema, Integer> colTemaMin;
    @FXML private TableColumn<Tema, Integer> colTemaMax;
    @FXML private TableColumn<Tema, String>  colTemaAval;
    @FXML private TableColumn<Tema, String>  colTemaOpc;
    // FXML — FORMULÁRIO
    @FXML private TextField        tfTemaNome;
    @FXML private Spinner<Integer> spTemaMin;
    @FXML private Spinner<Integer> spTemaMax;
    @FXML private Spinner<Integer> spTemaPrioridade;
    @FXML private CheckBox         cbTemaAvaliacao;
    @FXML private CheckBox         cbTemaOpcional;
    @FXML private Label            lblTituloFormTema;
    @FXML private Label            lblFeedbackTema;
    @FXML private Label            errTemaNome;
    // FXML — DEPENDÊNCIAS
    @FXML private Label            lblTemaSelecionadoDep;
    @FXML private Label            lblErrCircular;
    @FXML private ListView<Tema>   listTemasDisponiveis;
    @FXML private ListView<Tema>   listDependencias;

    //DAOs
    private final TemaDAO temaDAO = new TemaDAO();
    private final DependenciaTemaDAO dependenciaTemaDAO = new DependenciaTemaDAO();
    private Tema temaSelecionado;

    @FXML
    public void initialize() {
        configurarSpinners();
        configurarTabela();
        configurarListViews();
        carregarTemas();

        tabelaTemas.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, antigo, novo) -> {
                    if (novo != null) handleSelecionarTema();
                });
    }

    private void configurarSpinners() {
        spTemaMin.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        spTemaMax.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        spTemaPrioridade.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
    }

    private void configurarTabela() {
        colTemaPrior.setCellValueFactory(new PropertyValueFactory<>("prioridade"));
        colTemaNome .setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTemaMin  .setCellValueFactory(new PropertyValueFactory<>("qtd_min_aulas"));
        colTemaMax  .setCellValueFactory(new PropertyValueFactory<>("qtd_max_aulas"));

        colTemaAval.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEh_avaliacao() == 1 ? "Sim" : "Não"));
        colTemaOpc.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEh_opcional()  == 1 ? "Sim" : "Não"));
    }

    private void configurarListViews() {
        listTemasDisponiveis.setCellFactory(lv -> celulaNomeTema());
        listDependencias    .setCellFactory(lv -> celulaNomeTema());
    }

    private ListCell<Tema> celulaNomeTema() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Tema tema, boolean empty) {
                super.updateItem(tema, empty);
                setText(empty || tema == null ? null : tema.getNome());
            }
        };
    }

    //AÇÕES DO FORMULÁRIO DE TEMA
    @FXML
    private void handleSalvarTema() {
        if (tfTemaNome.getText().isBlank()) {
            errTemaNome.setText("Digite o nome do tema.");
            return;
        }

        try {
            Tema tema = construirTemaDoFormulario();

            if (modoEdicao()) {
                tema.setId_tema(temaSelecionado.getId_tema());
                temaDAO.editarTema(tema);
                temaSelecionado = tema;
                lblFeedbackTema.setText("Tema atualizado com sucesso!");
            } else {
                Integer idGerado = temaDAO.inserirTemaRetornandoId(tema);
                tema.setId_tema(idGerado);
                temaSelecionado = tema;
                lblFeedbackTema.setText("Tema salvo com sucesso!");
            }

            carregarTemas();
            atualizarPainelDependencias();

        } catch (Exception e) {
            lblFeedbackTema.setText("Erro ao salvar tema.");
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleNovoTema() {
        temaSelecionado = null;
        lblTituloFormTema.setText("Novo Tema");
        lblTemaSelecionadoDep.setText("—");
        listDependencias    .getItems().clear();
        listTemasDisponiveis.getItems().clear();
        limparFormulario();
    }

    @FXML
    private void handleLimparTema() {
        limparFormulario();
    }

    @FXML
    private void handleSelecionarTema() {
        Tema tema = tabelaTemas.getSelectionModel().getSelectedItem();
        if (tema == null) return;

        temaSelecionado = tema;
        lblTituloFormTema.setText("Editar Tema");
        lblTemaSelecionadoDep.setText(tema.getNome());

        preencherFormulario(tema);
        atualizarPainelDependencias();           // ← unifica as duas chamadas
    }


    //MANUSEIO DAS DEPENDÊNCIAS (DESCER, SUBIR, ENVIAR OU RETIRAR) E SALVAR
    @FXML
    private void handleAdicionarDep() {
        moverItemEntreListas(listTemasDisponiveis, listDependencias);
    }

    @FXML
    private void handleRemoverDep() {
        moverItemEntreListas(listDependencias, listTemasDisponiveis);
    }

    @FXML
    private void handleSubirDep() {
        reordenarItem(listDependencias, -1);
    }

    @FXML
    private void handleDescerDep() {
        reordenarItem(listDependencias, +1);
    }

    @FXML
    private void handleSalvarDependencias() {
        if (temaSelecionado == null) {
            lblFeedbackTema.setText("Selecione um tema.");
            return;
        }

        try {
            List<DependenciaTema> dependencias = new ArrayList<>();
            List<Tema> itens = listDependencias.getItems();

            for (int i = 0; i < itens.size(); i++) {
                DependenciaTema dep = new DependenciaTema();
                dep.setTema_id(temaSelecionado.getId_tema());
                dep.setTema_dependencia_id(itens.get(i).getId_tema());
                dep.setOrdem(i + 1);
                dependencias.add(dep);
            }

            dependenciaTemaDAO.salvarDependencias(
                    temaSelecionado.getId_tema(), dependencias
            );

            lblFeedbackTema.setText("Dependências salvas!");

        } catch (Exception e) {
            lblFeedbackTema.setText("Erro ao salvar dependências.");
            System.out.println(e.getMessage());
        }
    }

    // HELPERS PRIVADOS

    /** Constrói um Tema a partir dos valores atuais do formulário. */
    private Tema construirTemaDoFormulario() {
        Tema tema = new Tema();
        tema.setDisciplina_id(1);
        tema.setSemestre_letivo_id(3);
        tema.setNome(tfTemaNome.getText());
        tema.setQtd_min_aulas(spTemaMin.getValue());
        tema.setQtd_max_aulas(spTemaMax.getValue());
        tema.setPrioridade(spTemaPrioridade.getValue());
        tema.setEh_avaliacao(cbTemaAvaliacao.isSelected() ? 1 : 0);
        tema.setEh_opcional(cbTemaOpcional.isSelected()  ? 1 : 0);
        return tema;
    }

    /** Preenche o formulário com os dados de um Tema existente. */
    private void preencherFormulario(Tema tema) {
        tfTemaNome.setText(tema.getNome());
        spTemaMin      .getValueFactory().setValue(tema.getQtd_min_aulas());
        spTemaMax      .getValueFactory().setValue(tema.getQtd_max_aulas());
        spTemaPrioridade.getValueFactory().setValue(tema.getPrioridade());
        cbTemaAvaliacao.setSelected(tema.getEh_avaliacao() == 1);
        cbTemaOpcional .setSelected(tema.getEh_opcional()  == 1);
    }

    private void limparFormulario() {
        tfTemaNome.clear();
        spTemaMin      .getValueFactory().setValue(1);
        spTemaMax      .getValueFactory().setValue(1);
        spTemaPrioridade.getValueFactory().setValue(1);
        cbTemaAvaliacao.setSelected(false);
        cbTemaOpcional .setSelected(false);
    }

    private boolean modoEdicao() {
        return temaSelecionado != null && temaSelecionado.getId_tema() != null;
    }

    private void carregarTemas() {
        tabelaTemas.getItems().setAll(temaDAO.listarTemas());
    }

    /*
     * Atualiza as duas listas de dependências de uma vez.
     * Substitui as chamadas separadas a carregarTemasDisponiveis()
     * e listarDependenciasDoTemaSelecionado().
     */
    private void atualizarPainelDependencias() {
        listTemasDisponiveis.getItems().clear();
        listDependencias    .getItems().clear();

        if (temaSelecionado == null) return;

        List<Tema>            todos    = temaDAO.listarTemas();
        List<DependenciaTema> vinculos = dependenciaTemaDAO
                .listarDependenciasTema(temaSelecionado.getId_tema());

        List<Integer> idsDependentes = vinculos.stream()
                .map(DependenciaTema::getTema_dependencia_id)
                .toList();

        for (DependenciaTema vinculo : vinculos) {
            todos.stream()
                    .filter(t -> t.getId_tema().equals(vinculo.getTema_dependencia_id()))
                    .findFirst()
                    .ifPresent(listDependencias.getItems()::add);
        }

        // Os que não são dependência vão para disponíveis
        for (Tema t : todos) {
            if (!t.getId_tema().equals(temaSelecionado.getId_tema())
                    && !idsDependentes.contains(t.getId_tema())) {
                listTemasDisponiveis.getItems().add(t);
            }
        }
    }

    /** Move o item selecionado de uma ListView para outra. */
    private void moverItemEntreListas(ListView<Tema> origem, ListView<Tema> destino) {
        Tema selecionado = origem.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            origem  .getItems().remove(selecionado);
            destino .getItems().add(selecionado);
        }
    }

    /** Desloca o item selecionado dentro da lista (delta: -1 sobe, +1 desce). */
    private void reordenarItem(ListView<Tema> lista, int delta) {
        int index = lista.getSelectionModel().getSelectedIndex();
        int novoIndex = index + delta;

        if (index < 0 || novoIndex < 0 || novoIndex >= lista.getItems().size()) return;

        Tema tema = lista.getItems().remove(index);
        lista.getItems().add(novoIndex, tema);
        lista.getSelectionModel().select(novoIndex);
    }
}