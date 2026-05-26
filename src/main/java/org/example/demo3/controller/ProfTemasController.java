package org.example.demo3.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.DependenciaTemaDAO;
import org.example.demo3.dao.SemestreLetivoDAO;
import org.example.demo3.dao.TemaDAO;
import org.example.demo3.entity.DependenciaTema;
import org.example.demo3.entity.SemestreLetivo;
import org.example.demo3.entity.Tema;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private Tema temaSelecionado;
    private UsuarioAtual logado = UsuarioAtual.getInstancia();

    private Integer anoAtual;
    private Integer anoSemestre;
    private Integer idDisciplinaAtual;
    private Integer idSemestreAtual;

    // =========================================================================
    // INICIALIZAÇÃO
    // =========================================================================

    @FXML
    public void initialize() {

        // ── NOVO: captura os valores já selecionados antes de registrar os listeners ──
        this.idDisciplinaAtual = logado.getIdDisciplina();
        this.anoAtual          = logado.getAno();
        this.anoSemestre       = logado.getAnoSemestre();

        if (this.anoAtual != null && this.anoSemestre != null) {
            try {
                SemestreLetivoDAO slDao = new SemestreLetivoDAO();
                this.idSemestreAtual = slDao.getIdSemestreLetivo(this.anoAtual, this.anoSemestre);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        // ────────────────────────────────────────────────────────────────────────────

        logado.idDisciplinaProperty().addListener((obs, velho, novo) -> {
            if (novo != null) {
                this.idDisciplinaAtual = novo;
                carregarTemas();
            }
        });

        logado.anoProperty().addListener((obs, velho, novo) -> {
            if (novo != null) {
                this.anoAtual = novo;
                atualizarIdSemestreLetivo();
            }
        });

        logado.anoSemestreProperty().addListener((obs, velho, novo) -> {
            if (novo != null) {
                this.anoSemestre = novo;
                atualizarIdSemestreLetivo();
            }
        });

        configurarSpinners();
        configurarTabela();
        configurarListViews();
        carregarTemas(); // agora já tem idDisciplinaAtual e idSemestreAtual populados

        tabelaTemas.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, antigo, novo) -> {
                    if (novo != null) handleSelecionarTema();
                });
    }

    // =========================================================================
    // CONFIGURAÇÃO DE COMPONENTES
    // =========================================================================

    private void atualizarIdSemestreLetivo() {
        if (this.anoAtual != null && this.anoSemestre != null) {
            try {
                SemestreLetivoDAO slDao = new SemestreLetivoDAO();
                this.idSemestreAtual = slDao.getIdSemestreLetivo(this.anoAtual, this.anoSemestre);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 3. Após atualizar o ID do Semestre, recarregamos a tabela
        carregarTemas();
    }

    private void configurarSpinners() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factoryMin =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        SpinnerValueFactory.IntegerSpinnerValueFactory factoryMax =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);

        factoryMin.maxProperty().bind(factoryMax.valueProperty());
        spTemaMin.setValueFactory(factoryMin);
        spTemaMax.setValueFactory(factoryMax);

        spTemaPrioridade.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
    }

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
                setGraphic(empty ? null : btnDelete);
            }
        });
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

    // =========================================================================
    // AÇÕES DO FORMULÁRIO
    // =========================================================================

    @FXML
    private void handleSalvarTema() {
        System.out.println("id:"+idDisciplinaAtual);
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

    @FXML
    private void handleNovoTema() {
        if (idDisciplinaAtual == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Selecione a disciplina antes de adicionar um novo Tema.");
            return;
        }
        temaSelecionado = null;
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
        atualizarPainelDependencias();
    }

    private void handleDeletarTema(Tema tema) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja excluir o tema \"" + tema.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta != ButtonType.OK) return;

            try {
                dependenciaTemaDAO.deletarDependenciasPorTema(tema.getId_tema());
                temaDAO.excluirTema(tema.getId_tema());

                if (temaSelecionado != null
                        && temaSelecionado.getId_tema().equals(tema.getId_tema())) {
                    temaSelecionado = null;
                    limparFormulario();
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

    private Tema construirTemaDoFormulario() {
        Tema tema = new Tema();
        tema.setDisciplina_id(idDisciplinaAtual);          // Integer → compatível com o DAO
        tema.setSemestre_letivo_id(idSemestreAtual);
        tema.setNome(tfTemaNome.getText());
        tema.setQtd_min_aulas(spTemaMin.getValue());
        tema.setQtd_max_aulas(spTemaMax.getValue());
        tema.setPrioridade(spTemaPrioridade.getValue());
        tema.setEh_avaliacao(cbTemaAvaliacao.isSelected() ? 1 : 0);
        tema.setEh_opcional(cbTemaOpcional.isSelected()   ? 1 : 0);
        return tema;
    }

    private void preencherFormulario(Tema tema) {
        tfTemaNome.setText(tema.getNome());
        spTemaMin       .getValueFactory().setValue(tema.getQtd_min_aulas());
        spTemaMax       .getValueFactory().setValue(tema.getQtd_max_aulas());
        spTemaPrioridade.getValueFactory().setValue(tema.getPrioridade());
        cbTemaAvaliacao.setSelected(tema.getEh_avaliacao() == 1);
        cbTemaOpcional .setSelected(tema.getEh_opcional()  == 1);
    }

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

    private boolean modoEdicao() {
        return temaSelecionado != null && temaSelecionado.getId_tema() != null;
    }

    private void carregarTemas() {
        if (idDisciplinaAtual != null && idSemestreAtual != null) {
            tabelaTemas.getItems().setAll(
                    temaDAO.listarTemasPorDisciplinaESemestre(idDisciplinaAtual, idSemestreAtual)
            );
        } else {
            tabelaTemas.getItems().clear();
        }
    }

    private void atualizarPainelDependencias() {
        System.out.println("Semestre letivo: "+idSemestreAtual);
        System.out.println("Disciplina : "+idDisciplinaAtual);
        System.out.println("Tema: "+temaSelecionado.getId_tema());
        listTemasDisponiveis.getItems().clear();
        listDependencias    .getItems().clear();

        if (temaSelecionado == null || idDisciplinaAtual == null || idSemestreAtual == null) return;

        List<Tema> todos = temaDAO.listarTemasPorDisciplinaESemestre(idDisciplinaAtual, idSemestreAtual);
        List<DependenciaTema> vinculos = dependenciaTemaDAO.listarDependenciasTema(temaSelecionado.getId_tema());

        List<Integer> idsDependentes = vinculos.stream()
                .map(DependenciaTema::getTema_dependencia_id)
                .toList();

        for (DependenciaTema vinculo : vinculos) {
            todos.stream()
                    .filter(t -> t.getId_tema().equals(vinculo.getTema_dependencia_id()))
                    .findFirst()
                    .ifPresent(listDependencias.getItems()::add);
        }

        for (Tema t : todos) {
            if (!t.getId_tema().equals(temaSelecionado.getId_tema())
                    && !idsDependentes.contains(t.getId_tema())) {
                listTemasDisponiveis.getItems().add(t);
            }
        }
    }

    private void moverItemEntreListas(ListView<Tema> origem, ListView<Tema> destino) {
        Tema selecionado = origem.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            origem .getItems().remove(selecionado);
            destino.getItems().add(selecionado);
        }
    }

    private void reordenarItem(ListView<Tema> lista, int delta) {
        int index     = lista.getSelectionModel().getSelectedIndex();
        int novoIndex = index + delta;

        if (index < 0 || novoIndex < 0 || novoIndex >= lista.getItems().size()) return;

        Tema tema = lista.getItems().remove(index);
        lista.getItems().add(novoIndex, tema);
        lista.getSelectionModel().select(novoIndex);
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }



    @FXML
    private void handleImportarTemas() {
        //OBSERVA SE A DISCIPLINA JÁ FOI SELECIONADA NA TELA PRINCIPAL.
        if (idDisciplinaAtual == null || idSemestreAtual == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Selecione uma disciplina antes de importar temas.");
            return;
        }

        //BLOQUEIA A IMPORTAÇÃO SE A TABELA (ATUAL) ESTIVER COM TEMAS JÁ INSERIDOS.
        List<Tema> temasAtuais = temaDAO.listarTemasPorDisciplinaESemestre(idDisciplinaAtual, idSemestreAtual);
        if (!temasAtuais.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Importação bloqueada",
                    "A importação só é permitida quando não há nenhum tema cadastrado neste semestre.");
            return;
        }

        //BUSCA OS SEMESTRES ANTERIORES DO PROFESSOR (IGNORANDO O SEMESTRE ATUAL)
        List<SemestreLetivo> semestresDisponiveis;
        try {
            SemestreLetivoDAO slDao = new SemestreLetivoDAO();
            semestresDisponiveis = slDao.listarProfessorAnoESemestreAno(logado.getId_usuario());

            // --- NOVO: remove o semestre atual e todos de ano superior ao selecionado ---
            semestresDisponiveis.removeIf(s ->
                    s.getId_semestre_letivo() == idSemestreAtual ||
                            s.getAno() > anoAtual
            );

        } catch (SQLException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao carregar semestres letivos.");
            e.printStackTrace();
            return;
        }

        if (semestresDisponiveis.isEmpty()) {
            exibirAlerta(Alert.AlertType.INFORMATION, "Sem semestres",
                    "Não há semestres anteriores disponíveis para importação.");
            return;
        }

        //To String PARA EXIBIÇÃO
        StringConverter<SemestreLetivo> converter = new StringConverter<>() {
            @Override
            public String toString(SemestreLetivo sl) {
                if (sl == null) return "";
                return sl.getAno() + " - " + sl.getNumero_semestre() + "º semestre";
            }
            @Override
            public SemestreLetivo fromString(String s) { return null; }
        };

        //DIALOG PARA ESCOLHER O SEMESTRE
        ChoiceDialog<SemestreLetivo> dialog = new ChoiceDialog<>(semestresDisponiveis.get(0), semestresDisponiveis);
        dialog.setTitle("Importar Temas");
        dialog.setHeaderText("Selecione o semestre de origem:");
        dialog.setContentText("Semestre:");

        ComboBox<SemestreLetivo> comboBox = (ComboBox<SemestreLetivo>)
                dialog.getDialogPane().lookup(".combo-box");
        if (comboBox != null) {
            comboBox.setConverter(converter);
        }

        Optional<SemestreLetivo> escolha = dialog.showAndWait();
        if (escolha.isEmpty()) return;

        SemestreLetivo semestreOrigem = escolha.get();

        //BUSCA OS TEMAS DO SEMESTRE ESCOLHIDO NO TEMADAO.
        List<Tema> temasOrigem = temaDAO.listarTemasPorDisciplinaESemestre(
                idDisciplinaAtual, semestreOrigem.getId_semestre_letivo()
        );

        if (temasOrigem.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Sem temas",
                    "Não há temas cadastrados para esta disciplina no semestre selecionado.");
            return;
        }

        //POP UP DE CONFIRMAÇÃO
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar importação");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText(
                "Serão importados " + temasOrigem.size() + " tema(s) de \"" +
                        converter.toString(semestreOrigem) + "\".\nDeseja continuar?"
        );
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isEmpty() || resposta.get() != ButtonType.OK) return;

        //INSERE OS TEMAS IMPORTADOS NA TABELA (QUE DEVE ESTAR VAZIA)
        int importados = 0;

        for (Tema original : temasOrigem) {
            Tema novo = new Tema();
            novo.setDisciplina_id(idDisciplinaAtual);
            novo.setSemestre_letivo_id(idSemestreAtual);
            novo.setNome(original.getNome());
            novo.setQtd_min_aulas(original.getQtd_min_aulas());
            novo.setQtd_max_aulas(original.getQtd_max_aulas());
            novo.setPrioridade(original.getPrioridade());
            novo.setEh_avaliacao(original.getEh_avaliacao());
            novo.setEh_opcional(original.getEh_opcional());

            try {
                temaDAO.inserirTemaRetornandoId(novo);
                importados++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //ATUALIZA A TABELA E MOSTRA A MENSAGEM
        carregarTemas();
        exibirAlerta(Alert.AlertType.INFORMATION, "Importação concluída",
                importados + " tema(s) importado(s) com sucesso.");
    }



}