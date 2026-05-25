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

    // -------------------------------------------------------------------------
    // FXML — TABELA
    // -------------------------------------------------------------------------
    @FXML private TableView<Tema>            tabelaTemas;
    @FXML private TableColumn<Tema, Integer> colTemaPrior;
    @FXML private TableColumn<Tema, String>  colTemaNome;
    @FXML private TableColumn<Tema, Integer> colTemaMin;
    @FXML private TableColumn<Tema, Integer> colTemaMax;
    @FXML private TableColumn<Tema, String>  colTemaAval;
    @FXML private TableColumn<Tema, String>  colTemaOpc;
    @FXML private TableColumn<Tema, Void>    colTemaAcoes;

    // -------------------------------------------------------------------------
    // FXML — FORMULÁRIO
    // -------------------------------------------------------------------------
    @FXML private TextField        tfTemaNome;
    @FXML private Spinner<Integer> spTemaMin;
    @FXML private Spinner<Integer> spTemaMax;
    @FXML private Spinner<Integer> spTemaPrioridade;
    @FXML private CheckBox         cbTemaAvaliacao;
    @FXML private CheckBox         cbTemaOpcional;
    @FXML private Label            lblTituloFormTema;
    @FXML private Label            lblFeedbackTema;
    @FXML private Label            errTemaNome;

    // -------------------------------------------------------------------------
    // FXML — DEPENDÊNCIAS
    // -------------------------------------------------------------------------
    @FXML private Label          lblTemaSelecionadoDep;
    @FXML private Label          lblErrCircular;
    @FXML private ListView<Tema> listTemasDisponiveis;
    @FXML private ListView<Tema> listDependencias;

    // -------------------------------------------------------------------------
    // ESTADO
    // -------------------------------------------------------------------------
    private final TemaDAO            temaDAO            = new TemaDAO();
    private final DependenciaTemaDAO dependenciaTemaDAO = new DependenciaTemaDAO();

    private Tema    temaSelecionado;
    private Integer idDisciplinaAtual;
    private Integer idSemestreAtual;

    // =========================================================================
    // INICIALIZAÇÃO
    // =========================================================================

    // Ponto de entrada do JavaFX: configura componentes e registra listener de seleção
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

    // Recebe disciplina e semestre do shell e dispara o carregamento inicial
    public void setDadosIniciais(Integer idDisciplina, Integer idSemestre) {
        this.idDisciplinaAtual = idDisciplina;
        this.idSemestreAtual   = idSemestre;
        handleNovoTema();
        carregarTemas();
    }

    // =========================================================================
    // CONFIGURAÇÃO DE COMPONENTES
    // =========================================================================

    // Configura os spinners de min/max com vínculo entre si e o de prioridade
    private void configurarSpinners() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factoryMin =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        SpinnerValueFactory.IntegerSpinnerValueFactory factoryMax =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);

        factoryMin.maxProperty().bind(factoryMax.valueProperty()); // min nunca ultrapassa max
        spTemaMin.setValueFactory(factoryMin);
        spTemaMax.setValueFactory(factoryMax);

        spTemaPrioridade.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
    }

    // Liga cada coluna da tabela ao seu campo na entidade Tema
    private void configurarTabela() {
        colTemaPrior.setCellValueFactory(new PropertyValueFactory<>("prioridade"));
        colTemaNome .setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTemaMin  .setCellValueFactory(new PropertyValueFactory<>("qtd_min_aulas"));
        colTemaMax  .setCellValueFactory(new PropertyValueFactory<>("qtd_max_aulas"));

        colTemaAval.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEh_avaliacao() == 1 ? "Sim" : "Não"));
        colTemaOpc.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEh_opcional()  == 1 ? "Sim" : "Não"));

        configurarColunaAcoes();
    }

    // Injeta o botão DELETE em cada linha da coluna de ações
    private void configurarColunaAcoes() {
        colTemaAcoes.setCellFactory(col -> new TableCell<>() {

            private final Button btnDelete = new Button("DELETE");

            {
                btnDelete.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;" +
                                "-fx-cursor: hand;"
                );
                btnDelete.setOnAction(e ->
                        handleDeletarTema(getTableView().getItems().get(getIndex()))
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete); // oculta o botão em linhas vazias
            }
        });
    }

    // Define o cell factory das duas listas de dependências para exibir o nome do tema
    private void configurarListViews() {
        listTemasDisponiveis.setCellFactory(lv -> celulaNomeTema());
        listDependencias    .setCellFactory(lv -> celulaNomeTema());
    }

    // Cria uma célula de ListView que renderiza apenas o nome do tema
    private ListCell<Tema> celulaNomeTema() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Tema tema, boolean empty) {
                super.updateItem(tema, empty);
                setText(empty || tema == null ? null : tema.getNome());
            }
        };
    }

    // =========================================================================
    // AÇÕES DO FORMULÁRIO
    // =========================================================================

    // Salva ou atualiza um tema conforme o modo atual (novo vs. edição)
    @FXML
    private void handleSalvarTema() {
        if (tfTemaNome.getText().isBlank()) {
            errTemaNome.setText("Digite o nome do tema.");
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Preencha todos os campos.");
            return;
        }

        if (idDisciplinaAtual == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Selecione a disciplina antes de salvar um Tema.");
            return;
        }

        try {
            Tema tema = construirTemaDoFormulario();

            if (modoEdicao()) {
                tema.setId_tema(temaSelecionado.getId_tema());
                temaDAO.editarTema(tema);
                temaSelecionado = tema;
                lblFeedbackTema.setText("Tema atualizado com sucesso!");
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Tema alterado com sucesso.");
            } else {
                Integer idGerado = temaDAO.inserirTemaRetornandoId(tema);
                tema.setId_tema(idGerado);
                temaSelecionado = tema;
                lblFeedbackTema.setText("Tema salvo com sucesso!");
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Tema salvo com sucesso.");
            }

            carregarTemas();
            atualizarPainelDependencias();

        } catch (Exception e) {
            lblFeedbackTema.setText("Erro ao salvar tema.");
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar tema.");
            System.out.println(e.getMessage());
        }
    }

    // Reseta o estado para criação de um novo tema, bloqueando se não houver disciplina
    @FXML
    private void handleNovoTema() {
        if (idDisciplinaAtual == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Selecione a disciplina antes de adicionar um novo Tema.");
            return;
        }
        temaSelecionado = null;
        limparFormulario();
    }

    // Limpa os campos do formulário sem alterar o tema selecionado
    @FXML
    private void handleLimparTema() {
        limparFormulario();
    }

    // Preenche o formulário e o painel de dependências com o tema clicado na tabela
    @FXML
    private void handleSelecionarTema() {
        Tema tema = tabelaTemas.getSelectionModel().getSelectedItem();
        if (tema == null) return;

        temaSelecionado = tema;
        lblTituloFormTema.setText("Editar Tema");
        lblTemaSelecionadoDep.setText(tema.getNome());

        preencherFormulario(tema);
        atualizarPainelDependencias();
    }

    // Exibe confirmação e executa o soft-delete do tema com suas dependências
    private void handleDeletarTema(Tema tema) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja excluir o tema \"" + tema.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta != ButtonType.OK) return;

            try {
                dependenciaTemaDAO.deletarDependenciasPorTema(tema.getId_tema()); // remove FKs primeiro
                temaDAO.excluirTema(tema.getId_tema());

                if (temaSelecionado != null
                        && temaSelecionado.getId_tema().equals(tema.getId_tema())) {
                    temaSelecionado = null;
                    limparFormulario(); // reseta UI se o tema ativo foi deletado
                }

                carregarTemas();
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Tema excluído com sucesso.");

            } catch (Exception e) {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao excluir o tema.");
                System.out.println(e.getMessage());
            }
        });
    }

    // =========================================================================
    // AÇÕES DE DEPENDÊNCIAS
    // =========================================================================

    // Move tema selecionado de "disponíveis" para "dependências"
    @FXML
    private void handleAdicionarDep() {
        moverItemEntreListas(listTemasDisponiveis, listDependencias);
    }

    // Move tema selecionado de "dependências" de volta para "disponíveis"
    @FXML
    private void handleRemoverDep() {
        moverItemEntreListas(listDependencias, listTemasDisponiveis);
    }

    // Sobe o item selecionado uma posição na lista de dependências
    @FXML
    private void handleSubirDep() {
        reordenarItem(listDependencias, -1);
    }

    // Desce o item selecionado uma posição na lista de dependências
    @FXML
    private void handleDescerDep() {
        reordenarItem(listDependencias, +1);
    }

    // Persiste a lista de dependências atual, substituindo as anteriores
    @FXML
    private void handleSalvarDependencias() {
        if (temaSelecionado == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Selecione um tema antes de salvar dependências.");
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

            dependenciaTemaDAO.salvarDependencias(temaSelecionado.getId_tema(), dependencias);

            lblFeedbackTema.setText("Dependências salvas!");
            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Dependência(s) salva(s).");

        } catch (Exception e) {
            lblFeedbackTema.setText("Erro ao salvar dependências.");
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar dependência(s).");
            System.out.println(e.getMessage());
        }
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================

    // Monta um objeto Tema com os valores atuais do formulário
    private Tema construirTemaDoFormulario() {
        Tema tema = new Tema();
        tema.setDisciplina_id(idDisciplinaAtual);
        tema.setSemestre_letivo_id(idSemestreAtual);
        tema.setNome(tfTemaNome.getText());
        tema.setQtd_min_aulas(spTemaMin.getValue());
        tema.setQtd_max_aulas(spTemaMax.getValue());
        tema.setPrioridade(spTemaPrioridade.getValue());
        tema.setEh_avaliacao(cbTemaAvaliacao.isSelected() ? 1 : 0);
        tema.setEh_opcional(cbTemaOpcional.isSelected()   ? 1 : 0);
        return tema;
    }

    // Preenche os campos do formulário com os dados de um tema existente
    private void preencherFormulario(Tema tema) {
        tfTemaNome.setText(tema.getNome());
        spTemaMin       .getValueFactory().setValue(tema.getQtd_min_aulas());
        spTemaMax       .getValueFactory().setValue(tema.getQtd_max_aulas());
        spTemaPrioridade.getValueFactory().setValue(tema.getPrioridade());
        cbTemaAvaliacao.setSelected(tema.getEh_avaliacao() == 1);
        cbTemaOpcional .setSelected(tema.getEh_opcional()  == 1);
    }

    // Limpa o formulário e ajusta o título conforme o modo (novo vs. edição)
    private void limparFormulario() {
        if (temaSelecionado == null) {
            lblTituloFormTema.setText("Novo Tema");
            lblTemaSelecionadoDep.setText("—");
            listDependencias    .getItems().clear();
            listTemasDisponiveis.getItems().clear();
        }
        tfTemaNome.clear();
        spTemaMin       .getValueFactory().setValue(1);
        spTemaMax       .getValueFactory().setValue(1);
        spTemaPrioridade.getValueFactory().setValue(1);
        cbTemaAvaliacao.setSelected(false);
        cbTemaOpcional .setSelected(false);
    }

    // Retorna true se há um tema com ID válido selecionado (modo edição)
    private boolean modoEdicao() {
        return temaSelecionado != null && temaSelecionado.getId_tema() != null;
    }

    // Recarrega a tabela filtrando por disciplina e semestre atuais
    private void carregarTemas() {
        if (idDisciplinaAtual != null && idSemestreAtual != null) {
            tabelaTemas.getItems().setAll(
                    temaDAO.listarTemasPorDisciplinaESemestre(idDisciplinaAtual, idSemestreAtual)
            );
        } else {
            tabelaTemas.getItems().clear();
        }
    }

    // Atualiza as listas de dependências separando vinculadas das disponíveis
    private void atualizarPainelDependencias() {
        listTemasDisponiveis.getItems().clear();
        listDependencias    .getItems().clear();

        if (temaSelecionado == null || idDisciplinaAtual == null || idSemestreAtual == null) return;

        List<Tema> todos = temaDAO.listarTemasPorDisciplinaESemestre(idDisciplinaAtual, idSemestreAtual);
        List<DependenciaTema> vinculos = dependenciaTemaDAO.listarDependenciasTema(temaSelecionado.getId_tema());

        List<Integer> idsDependentes = vinculos.stream()
                .map(DependenciaTema::getTema_dependencia_id)
                .toList();

        // Adiciona dependências já vinculadas respeitando a ordem salva
        for (DependenciaTema vinculo : vinculos) {
            todos.stream()
                    .filter(t -> t.getId_tema().equals(vinculo.getTema_dependencia_id()))
                    .findFirst()
                    .ifPresent(listDependencias.getItems()::add);
        }

        // Adiciona os demais temas (exceto o próprio) como disponíveis
        for (Tema t : todos) {
            if (!t.getId_tema().equals(temaSelecionado.getId_tema())
                    && !idsDependentes.contains(t.getId_tema())) {
                listTemasDisponiveis.getItems().add(t);
            }
        }
    }

    // Move o item selecionado de uma ListView para outra
    private void moverItemEntreListas(ListView<Tema> origem, ListView<Tema> destino) {
        Tema selecionado = origem.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            origem .getItems().remove(selecionado);
            destino.getItems().add(selecionado);
        }
    }

    // Desloca o item selecionado na lista pelo delta informado (+1 desce, -1 sobe)
    private void reordenarItem(ListView<Tema> lista, int delta) {
        int index    = lista.getSelectionModel().getSelectedIndex();
        int novoIndex = index + delta;

        if (index < 0 || novoIndex < 0 || novoIndex >= lista.getItems().size()) return;

        Tema tema = lista.getItems().remove(index);
        lista.getItems().add(novoIndex, tema);
        lista.getSelectionModel().select(novoIndex);
    }

    // Exibe um Alert modal com tipo, título e mensagem parametrizados
    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}