package org.example.demo3.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.*;
import org.example.demo3.dto.AdmCursoExibicao;
import org.example.demo3.entity.*;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AdmCursosHorariosController {

    // =========================================================================
    //  FXML — FORMULÁRIO DE CURSO
    // =========================================================================

    @FXML private TitledPane       painelFormCurso;
    @FXML private TextField        tfCursoNome;
    @FXML private ToggleButton     tbManha;
    @FXML private ToggleButton     tbNoite;
    @FXML private Spinner<Integer> spQtdSemestres;
    @FXML private CheckBox         checkUsarProfessor;
    @FXML private ComboBox<String> cbProfessorCurso;
    @FXML private Button           btnSalvarCurso;
    @FXML private Label            lblProcessoSalvarCurso;

    // =========================================================================
    //  FXML — TABELA DE CURSOS
    // =========================================================================

    @FXML private TableView<AdmCursoExibicao>        tabelaCursos;
    @FXML private TableColumn<AdmCursoExibicao, String>  colCursoNome;
    @FXML private TableColumn<AdmCursoExibicao, String>  colCursoTurno;
    @FXML private TableColumn<AdmCursoExibicao, Integer> colCursoSemestres;
    @FXML private TableColumn<AdmCursoExibicao, String>  colCursoCoordenador;
    @FXML private TableColumn<AdmCursoExibicao, String>  colCursoAcoes;

    // =========================================================================
    //  FXML — TABELA DE HORÁRIOS
    // =========================================================================

    @FXML private Label           lblTituloHorarios;
    @FXML private Button          btnSalvarHorario;
    @FXML private Button          btnDeletarHorarios;
    @FXML private TableView<TemplateHorarioTurno>           tabelaHorarios;
    @FXML private TableColumn<TemplateHorarioTurno, String>    colHTipo;
    @FXML private TableColumn<TemplateHorarioTurno, Integer>   colHNumero;
    @FXML private TableColumn<TemplateHorarioTurno, LocalTime> colHInicio;
    @FXML private TableColumn<TemplateHorarioTurno, LocalTime> colHFim;
    @FXML private TableColumn<TemplateHorarioTurno, Void>      colHAcao;

    // =========================================================================
    //  ESTADO — BooleanProperty para bindings
    // =========================================================================

    // true → formulário aberto (adição ou edição em andamento)
    private final BooleanProperty modoEdicaoAtivo    = new SimpleBooleanProperty(false);

    // true → aguardando cadastro/edição da grade de horários
    private final BooleanProperty aguardandoHorarios = new SimpleBooleanProperty(false);

    // true → tabela de horários tem ao menos um item
    private final BooleanProperty horariosTemItens   = new SimpleBooleanProperty(false);

    // =========================================================================
    //  ESTADO — dados em processamento
    // =========================================================================

    private final UsuarioAtual logado = UsuarioAtual.getInstancia();
    private Integer ano;
    private Integer anoSemestre;

    private Integer idCursoProcessando;
    private Integer idSemestreLetivoProcessando;
    private Integer idProfSelecionado;

    private String  nomeCursoProcessando;
    private String  turnoProcessando;
    private Integer qtdSemestresProcessando;

    private List<TemplateHorarioTurno>           thtProcessando = new ArrayList<>();
    private ObservableList<TemplateHorarioTurno> linhasHorarios = FXCollections.observableArrayList();

    // =========================================================================
    //  DAOs
    // =========================================================================

    private final UsuarioDAO      uDao  = new UsuarioDAO();
    private final UsuarioTipoDAO  utDao = new UsuarioTipoDAO();
    private final CursoDAO        cDao  = new CursoDAO();
    private final HorarioCursoDAO hcDao = new HorarioCursoDAO();

    // =========================================================================
    //  INICIALIZAÇÃO
    // =========================================================================

    @FXML
    public void initialize() {
        logado.usuarioAdm();
        this.ano         = logado.getAno();
        this.anoSemestre = logado.getAnoSemestre();

        logado.anoProperty().addListener((obs, velho, novo) -> {
            if (novo != null) { this.ano = novo; carregarCursos(); }
        });
        logado.anoSemestreProperty().addListener((obs, velho, novo) -> {
            if (novo != null) { this.anoSemestre = novo; carregarCursos(); }
        });

        if (this.ano != null && this.anoSemestre != null) carregarCursos();

        // Mantém horariosTemItens sincronizado com a lista
        linhasHorarios.addListener((javafx.collections.ListChangeListener<TemplateHorarioTurno>) c ->
                horariosTemItens.set(!linhasHorarios.isEmpty())
        );

        configurarBindings();
        inicializarTabelaCursos();
        inicializarTabelaHorarios();

        cbProfessorCurso.setDisable(true);
    }


    private void configurarBindings() {
        tabelaCursos.disableProperty().bind(modoEdicaoAtivo);
        tabelaHorarios.disableProperty().bind(aguardandoHorarios.not());

        btnSalvarCurso.disableProperty().bind(
                tfCursoNome.textProperty().isEmpty()
                        .or(tbManha.selectedProperty().not().and(tbNoite.selectedProperty().not()))
        );

        // Usa horariosTemItens (BooleanProperty própria) para não perder
        // a referência quando linhasHorarios for substituída em handleAplicarTemplate
        btnSalvarHorario.disableProperty().bind(horariosTemItens.not());
    }

    // =========================================================================
    //  TABELA DE CURSOS — configuração e carregamento
    // =========================================================================

    private void inicializarTabelaCursos() {
        colCursoNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCursoTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        colCursoSemestres.setCellValueFactory(new PropertyValueFactory<>("qtd_semestres"));
        colCursoCoordenador.setCellValueFactory(new PropertyValueFactory<>("email"));

        colCursoAcoes.setCellFactory(new Callback<>() {
            @Override
            public TableCell<AdmCursoExibicao, String> call(TableColumn<AdmCursoExibicao, String> param) {
                return new TableCell<>() {
                    private final Button btnDeletar = new Button("Excluir");
                    {
                        btnDeletar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
                        btnDeletar.setOnAction(e -> handleDeletarCurso(getTableView().getItems().get(getIndex())));
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btnDeletar);
                    }
                };
            }
        });

        tabelaCursos.getSelectionModel().selectedItemProperty().addListener(
                (obs, antigo, selecionado) -> {
                    if (selecionado != null) {
                        modoEdicaoAtivo.set(true);
                        thtProcessando.clear();
                        linhasHorarios.clear();
                        iniciarEdicaoCurso(selecionado);
                    }
                });

        btnSalvarCurso.setOnAction(e -> handleSalvarCurso());
    }

    private void carregarCursos() {
        try {
            tabelaCursos.setItems(FXCollections.observableArrayList(
                    new CursoDAO().listarTodosCursosDTO()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //  TABELA DE HORÁRIOS — configuração e carregamento
    // =========================================================================

    private void inicializarTabelaHorarios() {
        tabelaHorarios.setEditable(true);

        colHTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colHNumero.setCellValueFactory(new PropertyValueFactory<>("numero_ordem"));
        colHInicio.setCellValueFactory(new PropertyValueFactory<>("hora_inicio"));
        colHFim.setCellValueFactory(new PropertyValueFactory<>("hora_fim"));

        colHAcao.setCellFactory(new Callback<>() {
            @Override
            public TableCell<TemplateHorarioTurno, Void> call(TableColumn<TemplateHorarioTurno, Void> param) {
                return new TableCell<>() {
                    private final Button btnDeletar = new Button("Excluir");
                    {
                        btnDeletar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
                        btnDeletar.setOnAction(e -> handleDeletarHorario(getTableView().getItems().get(getIndex())));
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btnDeletar);
                    }
                };
            }
        });

        colHTipo.setCellFactory(ComboBoxTableCell.forTableColumn("aula", "intervalo"));
        colHTipo.setOnEditCommit(e -> { e.getRowValue().setTipo(e.getNewValue()); tabelaHorarios.refresh(); });

        colHNumero.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        colHNumero.setOnEditCommit(e -> { e.getRowValue().setNumero_ordem(e.getNewValue()); tabelaHorarios.refresh(); });

        javafx.util.StringConverter<LocalTime> timeConverter = new javafx.util.StringConverter<>() {
            private final java.time.format.DateTimeFormatter fmt =
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm");
            @Override public String    toString(LocalTime t) { return t != null ? t.format(fmt) : ""; }
            @Override public LocalTime fromString(String s)  {
                try { return LocalTime.parse(s, fmt); } catch (Exception ex) { return null; }
            }
        };
        colHInicio.setCellFactory(TextFieldTableCell.forTableColumn(timeConverter));
        colHInicio.setOnEditCommit(e -> { e.getRowValue().setHora_inicio(e.getNewValue()); tabelaHorarios.refresh(); });

        colHFim.setCellFactory(TextFieldTableCell.forTableColumn(timeConverter));
        colHFim.setOnEditCommit(e -> { e.getRowValue().setHora_fim(e.getNewValue()); tabelaHorarios.refresh(); });
    }

    private void carregarHorarios() {
        if (idCursoProcessando == null || idSemestreLetivoProcessando == null) {
            linhasHorarios.clear();
            tabelaHorarios.setItems(null);
            return;
        }
        try {
            thtProcessando.clear();
            for (HorarioCurso hc : hcDao.listarHorariosPorCurso(idCursoProcessando, idSemestreLetivoProcessando)) {
                TemplateHorarioTurno tht = new TemplateHorarioTurno();
                tht.setTipo(hc.getTipo());
                tht.setNumero_ordem(hc.getNumero_ordem());
                tht.setHora_inicio(hc.getHora_inicio());
                tht.setHora_fim(hc.getHora_fim());
                thtProcessando.add(tht);
            }
            linhasHorarios.setAll(thtProcessando);
            tabelaHorarios.setItems(linhasHorarios);
        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro", "Não foi possível atualizar a tabela de horários.", Alert.AlertType.ERROR);
        }
    }

    private void atualizarVisibilidadeBtnDeletarTodos() {
        boolean visivel = linhasHorarios.size() > 1;
        btnDeletarHorarios.setVisible(visivel);
        btnDeletarHorarios.setManaged(visivel);
    }

    // =========================================================================
    //  FORMULÁRIO DE CURSO — ações do FXML
    // =========================================================================

    @FXML
    public void handleNovoCurso() {
        reiniciarFormularioCurso();
        modoEdicaoAtivo.set(true);
        aguardandoHorarios.set(false);
        tabelaHorarios.setItems(null);
        painelFormCurso.setExpanded(true);
        alterarEstadoEdicaoDadosCurso(false);

        btnSalvarCurso.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        btnSalvarCurso.setText("Salvar Curso");
        btnSalvarCurso.setOnAction(e -> handleSalvarCurso());

        btnSalvarHorario.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnSalvarHorario.setText("\uD83D\uDCBE Salvar Horários");
        btnSalvarHorario.setOnAction(e -> handleSalvarHorarios());

        btnDeletarHorarios.setManaged(false);
        btnDeletarHorarios.setVisible(false);
    }

    @FXML
    public void handleSelecionarCurso(MouseEvent event) {
        // Tratado pelo listener do selectedItemProperty no initialize
    }

    @FXML
    public void handleTurnoChange() {
        // Exclusividade dos ToggleButtons tratada pelo próprio binding
    }

    @FXML
    public void handleCancelarCurso() {
        // Novo curso salvo mas sem grade ainda: confirmar e desfazer
        if (aguardandoHorarios.get() && idCursoProcessando != null && !modoEdicaoEhEdicao()) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Cancelar o cadastro irá excluir o curso que está sendo criado. Deseja continuar?",
                    ButtonType.YES, ButtonType.NO);
            confirmacao.showAndWait();
            if (confirmacao.getResult() != ButtonType.YES) return;

            try {
                cDao.deletarCursoProcessando(idCursoProcessando);
                if (checkUsarProfessor.isSelected() && idProfSelecionado != null)
                    utDao.excluirUsuarioTipo(idProfSelecionado, "COORD");

                lblProcessoSalvarCurso.setVisible(false);
                lblProcessoSalvarCurso.setManaged(false);
                lblTituloHorarios.setText("Horários — selecione um curso à esquerda");
                linhasHorarios.clear();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        encerrarModoEdicao();
    }

    @FXML
    public void usarProfessor() {
        if (checkUsarProfessor.isSelected()) {
            cbProfessorCurso.setDisable(false);
            carregarProfessores();
        } else {
            cbProfessorCurso.setDisable(true);
            idProfSelecionado = null;
        }
    }

    @FXML
    public void handleSelecaoProfessor() {
        if (!checkUsarProfessor.isSelected()) { idProfSelecionado = null; return; }
        try {
            String selecionado = cbProfessorCurso.getValue();
            if (selecionado != null)
                idProfSelecionado = uDao.buscarUsuarioPorEmailUnico(selecionado).getId_usuario();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSalvarCurso() {
        nomeCursoProcessando    = tfCursoNome.getText();
        qtdSemestresProcessando = spQtdSemestres.getValue();

        boolean manhaSel = tbManha.isSelected();
        boolean noiteSel = tbNoite.isSelected();
        boolean ambos    = manhaSel && noiteSel;

        if (ambos) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Você selecionou ambos os turnos. Serão criados dois cursos: " +
                            "um para manhã e outro para noite, cada um com sua grade de horários. Deseja continuar?",
                    ButtonType.YES, ButtonType.NO);
            confirmacao.showAndWait();
            if (confirmacao.getResult() != ButtonType.YES) return;
        }

        try {
            if (checkUsarProfessor.isSelected() && idProfSelecionado != null
                    && !utDao.usuarioPossuiTipoAtivo(idProfSelecionado, "COORD"))
                utDao.inserirUsuarioTipo(new UsuarioTipo(idProfSelecionado, "COORD"));

            if (ambos) {
                criarCursoComTemplate("manha");
                criarCursoComTemplate("noite");
                carregarCursos();
                modoEdicaoAtivo.set(false);
                exibirAlerta("Cursos criados",
                        "Os cursos de manhã e noite foram criados com suas respectivas grades de horários.",
                        Alert.AlertType.INFORMATION);
            } else {
                turnoProcessando = manhaSel ? "manha" : "noite";

                idCursoProcessando = (checkUsarProfessor.isSelected() && idProfSelecionado != null)
                        ? cDao.inserirCursoRetornaId(idProfSelecionado, nomeCursoProcessando, turnoProcessando, qtdSemestresProcessando)
                        : cDao.inserirCursoRetornaId(nomeCursoProcessando, turnoProcessando, qtdSemestresProcessando);

                lblTituloHorarios.setText("Horários — Curso selecionado: " + nomeCursoProcessando);

                // Trava o formulário e libera a tabela de horários
                alterarEstadoEdicaoDadosCurso(true);
                lblProcessoSalvarCurso.setVisible(true);
                lblProcessoSalvarCurso.setManaged(true);

                linhasHorarios.clear();
                tabelaHorarios.setItems(linhasHorarios);
                aguardandoHorarios.set(true);
                painelFormCurso.setExpanded(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        reiniciarFormularioCurso();
        tabelaCursos.getSelectionModel().clearSelection();
    }

    // =========================================================================
    //  EDIÇÃO DE CURSO — fluxo separado ao selecionar da tabela
    // =========================================================================

    /**
     * Chamado quando o usuário seleciona um curso da tabela.
     * Pergunta se deseja alterar os horários antes de abrir o formulário.
     */
    private void iniciarEdicaoCurso(AdmCursoExibicao c) {
        Alert pergunta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja também alterar os horários do curso \"" + c.getNome() + "\"?",
                ButtonType.YES, ButtonType.NO);
        pergunta.setTitle("Alterar horários?");
        pergunta.showAndWait();

        boolean alterarHorarios = pergunta.getResult() == ButtonType.YES;
        preencherFormularioEdicao(c, alterarHorarios);
    }

    private void preencherFormularioEdicao(AdmCursoExibicao c, boolean alterarHorarios) {
        painelFormCurso.setExpanded(true);
        alterarEstadoEdicaoDadosCurso(false);
        tfCursoNome.setText(c.getNome());
        tbManha.setSelected("manha".equals(c.getTurno()));
        tbNoite.setSelected(!"manha".equals(c.getTurno()));
        spQtdSemestres.getValueFactory().setValue(c.getQtd_semestres());

        if (c.getEmail() != null) {
            checkUsarProfessor.setSelected(true);
            cbProfessorCurso.setDisable(false);
            cbProfessorCurso.setItems(FXCollections.observableArrayList(c.getEmail()));
            cbProfessorCurso.setValue(c.getEmail());
        } else {
            checkUsarProfessor.setSelected(false);
            cbProfessorCurso.setDisable(true);
            cbProfessorCurso.setValue(null);
        }

        lblTituloHorarios.setText("Horários — Curso selecionado: " + c.getNome());
        btnSalvarCurso.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        btnSalvarCurso.setText("Editar Curso");

        AdmCursoExibicao snapshot = new AdmCursoExibicao(
                c.getNome(), c.getTurno(), c.getQtd_semestres(), c.getEmail());
        btnSalvarCurso.setOnAction(e -> handleEditarCurso(snapshot));

        try {
            idCursoProcessando         = new CursoDAO().listarIdCurso(c.getNome());
            idSemestreLetivoProcessando = new SemestreLetivoDAO()
                    .getIdSemestreLetivo(logado.getAno(), logado.getAnoSemestre());

            // Carrega horários existentes para exibição
            for (HorarioCurso hc : new HorarioCursoDAO()
                    .listarHorariosPorCurso(idCursoProcessando, idSemestreLetivoProcessando)) {
                TemplateHorarioTurno tht = new TemplateHorarioTurno();
                tht.setTipo(hc.getTipo());
                tht.setNumero_ordem(hc.getNumero_ordem());
                tht.setHora_inicio(hc.getHora_inicio());
                tht.setHora_fim(hc.getHora_fim());
                thtProcessando.add(tht);
            }
            linhasHorarios.setAll(thtProcessando);
            tabelaHorarios.setItems(linhasHorarios);

            if (alterarHorarios) {
                // Mesmo fluxo de novo curso: tabela habilitada, salvar horários obrigatório
                aguardandoHorarios.set(true);

                btnSalvarHorario.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
                btnSalvarHorario.setText("Salvar Alterações de Horários");
                btnSalvarHorario.setOnAction(e -> handleEditarHorario());

                btnDeletarHorarios.setManaged(true);
                btnDeletarHorarios.setVisible(true);
            } else {
                // Apenas edição de dados do curso; horários ficam bloqueados
                aguardandoHorarios.set(false);

                btnSalvarHorario.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                btnSalvarHorario.setText("Horários não serão alterados");
                btnSalvarHorario.setOnAction(null);

                btnDeletarHorarios.setManaged(false);
                btnDeletarHorarios.setVisible(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleEditarCurso(AdmCursoExibicao snapshot) {
        String turno      = tbManha.isSelected() ? "manha" : "noite";
        String emailCoord = checkUsarProfessor.isSelected() ? cbProfessorCurso.getValue() : null;

        AdmCursoExibicao dadosAlterados = new AdmCursoExibicao(
                tfCursoNome.getText(), turno,
                spQtdSemestres.getValueFactory().getValue(), emailCoord);

        try {
            Integer idCoord = null;
            if (emailCoord != null) {
                String emailBusca = emailCoord.contains("(")
                        ? emailCoord.replaceAll(".*\\((.*)\\)", "$1")
                        : emailCoord;
                Usuario u = uDao.buscarUsuarioPorEmailUnico(emailBusca);
                if (u != null) idCoord = u.getId_usuario();
            }

            cDao.alterarCurso(dadosAlterados, idCoord, snapshot.getNome());
            carregarCursos();

            // Se não estava alterando horários, encerra aqui
            if (!aguardandoHorarios.get()) {
                encerrarModoEdicao();
            } else {
                // Trava o formulário e aguarda salvar os horários
                alterarEstadoEdicaoDadosCurso(true);
                painelFormCurso.setExpanded(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDeletarCurso(AdmCursoExibicao linhaCurso) {
        try {
            int idCurso = new CursoDAO().listarIdCurso(linhaCurso.getNome());

            String mensagem = cDao.possuiDependenciasCurso(idCurso)
                    ? "O curso \"" + linhaCurso.getNome() + "\" possui horários, disciplinas e/ou " +
                      "professores vinculados. Todos os dados associados serão removidos. Deseja continuar?"
                    : "Deseja mesmo excluir o curso \"" + linhaCurso.getNome() + "\"?";

            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
            alerta.showAndWait();
            if (alerta.getResult() != ButtonType.YES) return;

            cDao.softDeletarCursoComVinculos(idCurso);
            carregarCursos();
            lblTituloHorarios.setText("Horários — selecione um curso à esquerda");
            encerrarModoEdicao();

        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro", "Não foi possível excluir o curso.", Alert.AlertType.ERROR);
        }
    }

    // =========================================================================
    //  FORMULÁRIO DE CURSO — utilitários
    // =========================================================================

    private void carregarProfessores() {
        ObservableList<String> opcoes = FXCollections.observableArrayList();
        try {
            for (Usuario prof : uDao.listarProfSemestreLetivo(ano, anoSemestre))
                opcoes.add(prof.getEmail());
            cbProfessorCurso.setItems(opcoes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void alterarEstadoEdicaoDadosCurso(boolean desabilitar) {
        tfCursoNome.setDisable(desabilitar);
        tbManha.setDisable(desabilitar);
        tbNoite.setDisable(desabilitar);
        spQtdSemestres.setDisable(desabilitar);
        checkUsarProfessor.setDisable(desabilitar);
        cbProfessorCurso.setDisable(!checkUsarProfessor.isSelected() || desabilitar);
    }

    private void reiniciarFormularioCurso() {
        tfCursoNome.setText("");
        tbManha.setSelected(true);
        tbNoite.setSelected(false);
        spQtdSemestres.getValueFactory().setValue(1);
        checkUsarProfessor.setSelected(false);
        cbProfessorCurso.setDisable(true);
        cbProfessorCurso.setItems(null);
        idProfSelecionado = null;
    }

    /** Reseta todos os estados de edição e volta ao estado ocioso. */
    private void encerrarModoEdicao() {
        alterarEstadoEdicaoDadosCurso(false);
        reiniciarFormularioCurso();
        modoEdicaoAtivo.set(false);
        aguardandoHorarios.set(false);
        tabelaCursos.getSelectionModel().clearSelection();
        linhasHorarios.clear();
        btnDeletarHorarios.setVisible(false);
        btnDeletarHorarios.setManaged(false);
        lblProcessoSalvarCurso.setVisible(false);
        lblProcessoSalvarCurso.setManaged(false);
        painelFormCurso.setExpanded(false);
    }

    /** Retorna true se o modo atual é edição de curso existente (não novo cadastro). */
    private boolean modoEdicaoEhEdicao() {
        return "Editar Curso".equals(btnSalvarCurso.getText());
    }

    // =========================================================================
    //  TABELA DE HORÁRIOS — ações do FXML
    // =========================================================================

    @FXML
    public void handleAplicarTemplate() {
        if (tbManha.isSelected() && tbNoite.isSelected()) {
            exibirAlerta("Turno ambíguo", "Selecione apenas um turno para aplicar o template.", Alert.AlertType.WARNING);
            return;
        }
        try {
            String turno = getTurnoSelecionado();
            List<TemplateHorarioTurno> template = new TemplateHorarioTurnoDAO().listarPorTurno(turno);

            if (template.isEmpty()) {
                exibirAlerta("Template não encontrado",
                        "Não há grade cadastrada para o turno " + turno + ".", Alert.AlertType.WARNING);
                return;
            }

            idSemestreLetivoProcessando = new SemestreLetivoDAO()
                    .getIdSemestreLetivo(logado.getAno(), logado.getAnoSemestre());

            // Atualiza a lista existente em vez de substituí-la,
            // para manter o listener e o binding de horariosTemItens
            linhasHorarios.setAll(template);
            tabelaHorarios.setItems(linhasHorarios);
            atualizarVisibilidadeBtnDeletarTodos();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePropagarTurno() {
        if (tbManha.isSelected() && tbNoite.isSelected()) {
            exibirAlerta("Turno ambíguo", "Selecione apenas um turno para propagar os horários.", Alert.AlertType.WARNING);
            return;
        }
        if (linhasHorarios.isEmpty()) return;

        String turno = getTurnoSelecionado();
        try {
            TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
            if (!thtDao.listarPorTurno(turno).isEmpty()) {
                Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                        "Já existe uma grade para o turno " + turno + ". Deseja sobrescrever?",
                        ButtonType.YES, ButtonType.NO);
                confirmacao.showAndWait();
                if (confirmacao.getResult() != ButtonType.YES) return;
            }

            tabelaHorarios.getItems().forEach(tht -> tht.setTurno(turno));
            thtDao.deletarTemplateTurno(turno);
            thtDao.salvarListaTemplate(tabelaHorarios.getItems());

        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro", "Não foi possível propagar os horários.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleAdicionarLinhaHorario() {
        if (tbManha.isSelected() && tbNoite.isSelected()) {
            exibirAlerta("Turno ambíguo", "Selecione apenas um turno para adicionar horários.", Alert.AlertType.WARNING);
            return;
        }
        TemplateHorarioTurno nova = new TemplateHorarioTurno();
        if (!linhasHorarios.isEmpty()) {
            TemplateHorarioTurno ultimo = tabelaHorarios.getItems().getLast();
            nova.setTurno(ultimo.getTurno());
            nova.setTipo(ultimo.getTipo());
            nova.setNumero_ordem(ultimo.getNumero_ordem() + 1);
            nova.setHora_inicio(ultimo.getHora_inicio());
            nova.setHora_fim(ultimo.getHora_fim());
        } else {
            nova.setTurno(getTurnoSelecionado());
            nova.setTipo("aula");
            nova.setNumero_ordem(1);
            nova.setHora_inicio(LocalTime.now());
            nova.setHora_fim(LocalTime.now());
        }
        linhasHorarios.add(nova);
        tabelaHorarios.setItems(linhasHorarios);
        atualizarVisibilidadeBtnDeletarTodos();
    }

    @FXML
    public void handleSalvarHorarios() {
        try {
            if (idSemestreLetivoProcessando == null)
                idSemestreLetivoProcessando = new SemestreLetivoDAO()
                        .getIdSemestreLetivo(logado.getAno(), logado.getAnoSemestre());

            hcDao.inserirTemplateHorarioCurso(
                    tabelaHorarios.getItems(), idCursoProcessando, idSemestreLetivoProcessando);

            lblProcessoSalvarCurso.setVisible(false);
            lblProcessoSalvarCurso.setManaged(false);
            carregarCursos();

        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro ao salvar", "Não foi possível salvar os horários.", Alert.AlertType.ERROR);
            return;
        }

        encerrarModoEdicao();
    }

    // =========================================================================
    //  TABELA DE HORÁRIOS — lógica interna
    // =========================================================================

    private void handleEditarHorario() {
        if (tabelaHorarios.getItems() == null || tabelaHorarios.getItems().isEmpty()) {
            exibirAlerta("Grade obrigatória",
                    "Um curso não pode ficar sem grade de horários. Adicione ao menos um horário.",
                    Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Alterar os horários removerá atribuições e slots de planejamento vinculados. Deseja continuar?",
                ButtonType.YES, ButtonType.NO);
        confirmacao.showAndWait();
        if (confirmacao.getResult() != ButtonType.YES) return;

        try {
            hcDao.removerHorariosCursoSLEmCascata(idCursoProcessando, idSemestreLetivoProcessando);
            hcDao.inserirTemplateHorarioCurso(
                    new ArrayList<>(tabelaHorarios.getItems()),
                    idCursoProcessando, idSemestreLetivoProcessando);

            carregarCursos();
            encerrarModoEdicao();

        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro ao alterar", "Não foi possível alterar os horários.", Alert.AlertType.ERROR);
        }
    }

    private void handleDeletarHorario(TemplateHorarioTurno tht) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja mesmo excluir esse horário?", ButtonType.YES, ButtonType.NO);
        alerta.showAndWait();
        if (alerta.getResult() != ButtonType.YES) return;

        if (idCursoProcessando != null && idSemestreLetivoProcessando != null) {
            try {
                hcDao.removerHorarioOrdemCursoSL(
                        tht.getNumero_ordem(), idCursoProcessando, idSemestreLetivoProcessando);
            } catch (SQLException e) {
                e.printStackTrace();
                exibirAlerta("Erro ao deletar",
                        "Não foi possível deletar o horário.", Alert.AlertType.ERROR);
                return;
            }
        }
        linhasHorarios.remove(tht);
        atualizarVisibilidadeBtnDeletarTodos();
    }

    @FXML
    private void deletarTodosHorarios() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja mesmo excluir todos os horários?", ButtonType.YES, ButtonType.NO);
        alerta.showAndWait();
        if (alerta.getResult() != ButtonType.YES) return;

        if (idCursoProcessando != null && idSemestreLetivoProcessando != null) {
            try {
                hcDao.removerHorariosCursoSLEmCascata(idCursoProcessando, idSemestreLetivoProcessando);
                carregarHorarios();
            } catch (SQLException e) {
                e.printStackTrace();
                exibirAlerta("Erro ao deletar", "Não foi possível deletar os horários.", Alert.AlertType.ERROR);
                return;
            }
        }
        linhasHorarios.clear();
        atualizarVisibilidadeBtnDeletarTodos();
    }

    // =========================================================================
    //  UTILITÁRIOS GERAIS
    // =========================================================================

    private String getTurnoSelecionado() {
        return tbManha.isSelected() ? "manha" : "noite";
    }

    private void criarCursoComTemplate(String turno) throws SQLException {
        String sufixo    = "manha".equals(turno) ? " - Manhã" : " - Noite";
        String nomeFinal = nomeCursoProcessando + sufixo;

        int idCurso = (checkUsarProfessor.isSelected() && idProfSelecionado != null)
                ? cDao.inserirCursoRetornaId(idProfSelecionado, nomeFinal, turno, qtdSemestresProcessando)
                : cDao.inserirCursoRetornaId(nomeFinal, turno, qtdSemestresProcessando);

        int idSL = new SemestreLetivoDAO().getIdSemestreLetivo(logado.getAno(), logado.getAnoSemestre());
        List<TemplateHorarioTurno> template = new TemplateHorarioTurnoDAO().listarPorTurno(turno);

        if (template.isEmpty()) {
            exibirAlerta("Grade não encontrada — " + turno,
                    "O curso \"" + nomeFinal + "\" foi criado, mas não há grade para esse turno. " +
                            "Selecione-o na tabela e adicione os horários manualmente.", Alert.AlertType.WARNING);
        } else {
            hcDao.inserirTemplateHorarioCurso(template, idCurso, idSL);
        }
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}