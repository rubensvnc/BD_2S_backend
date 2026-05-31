package org.example.demo3.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.demo3.SlotPlanejamento;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.*;
import org.example.demo3.entity.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class CoordPainelController {

    // ════════════════════════════════════════════════════════════════════════
    // CAMPOS FXML — ABA 1: DISCIPLINAS
    // ════════════════════════════════════════════════════════════════════════

    @FXML private TableView<Disciplina>            tabelaDisciplinas;
    @FXML private TableColumn<Disciplina, String>  colDiscNome;
    @FXML private TableColumn<Curso, Integer>      colDiscSemCurso;
    @FXML private TableColumn<Disciplina, Integer> colDiscCH;
    @FXML private TableColumn<Disciplina, String>  colDiscProf;
    @FXML private TableColumn<Disciplina, Void>    colDiscAcoes;
    @FXML private Label             lblTituloFormDisc;
    @FXML private TextField         tfDiscNome;
    @FXML private Label             errDiscNome;
    @FXML private ComboBox<Integer> cbDiscSemestreCurso;
    @FXML private Spinner<Integer>  spDiscCH;
    @FXML private Label             lblFeedbackDisc;

    // ════════════════════════════════════════════════════════════════════════
    // CAMPOS FXML — ABA 2: PROFESSORES
    // ════════════════════════════════════════════════════════════════════════

    @FXML private TableView<Usuario>           tabelaProfessores;
    @FXML private TableColumn<Usuario, String> colProfNome;
    @FXML private TableColumn<Usuario, String> colProfEmail;
    @FXML private TableColumn<Usuario, Void>   colProfAcoes;
    @FXML private Label         lblTituloFormProf;
    @FXML private Button        btnAtribuirMesmo;
    @FXML private TextField     tfProfNome;
    @FXML private Label         errProfNome;
    @FXML private TextField     tfProfEmail;
    @FXML private Label         avisoEmailDup;
    @FXML private Label         errProfEmail;
    @FXML private PasswordField pfProfSenha;
    @FXML private Label         errProfSenha;
    @FXML private Label         lblFeedbackProf;

    // ════════════════════════════════════════════════════════════════════════
    // CAMPOS FXML — ABA 3: ATRIBUIÇÕES
    // ════════════════════════════════════════════════════════════════════════

    @FXML private TabPane          tabPanePrincipal;
    @FXML private Tab              tabAtribuicoes;
    @FXML private ComboBox<Usuario>    cbAtribProf;
    @FXML private ComboBox<Disciplina> cbAtribDisc;
    @FXML private GridPane             gradeAtribuicao;
    @FXML private Label                lblConflito;
    @FXML private Label                lblFeedbackAtrib;
    @FXML private Button               btnSalvarAtrib;

    // ════════════════════════════════════════════════════════════════════════
    // CAMPOS FXML — ABA 4: PLANEJAMENTOS
    // ════════════════════════════════════════════════════════════════════════

    @FXML private Label            lblTituloCoordVisor;
    @FXML private TabPane          tabVisorCoord;
    @FXML private TreeView<Object> treePlanoCoord;
    @FXML private VBox             painelEstatCoord;
    @FXML private ListView<Usuario> listaProfessoresEsquerda;

    // Componentes da aba 4 sem fx:id, mapeados dinamicamente

    private Label lblTotalTemas;
    private Label lblAulasGeradas;
    private Label lblAulasMinistradas;
    private Label lblAulasPendentes;
    private Label lblAulasCanceladas;
    private Label lblCargaHorariaMinima;
    private ProgressBar progressConclusao;
    private Label lblPercentual;
    private PieChart chartStatusAulas;

    // ════════════════════════════════════════════════════════════════════════
    // DAOs
    // ════════════════════════════════════════════════════════════════════════

    private final CursoDAO               cursoDAO               = new CursoDAO();
    private final DisciplinaDAO          disciplinaDAO          = new DisciplinaDAO();
    private final UsuarioDAO             usuarioDAO             = new UsuarioDAO();
    private final UsuarioTipoDAO         usuarioTipoDAO         = new UsuarioTipoDAO();
    private final AtribuicaoProfessorDAO atribuicaoProfessorDAO = new AtribuicaoProfessorDAO();
    private final HorarioCursoDAO        horarioCursoDAO        = new HorarioCursoDAO();
    private final AtribuicaoHorarioDAO   atribuicaoHorarioDAO   = new AtribuicaoHorarioDAO();
    private final SemestreLetivoDAO      semestreLetivoDAO      = new SemestreLetivoDAO();
    private final SlotPlanejamentoDAO    slotDAO                = new SlotPlanejamentoDAO();
    private final PlanejamentoDAO        planejamentoDAO        = new PlanejamentoDAO();

    // ════════════════════════════════════════════════════════════════════════
    // ESTADO
    // ════════════════════════════════════════════════════════════════════════

    private final UsuarioAtual logado = UsuarioAtual.getInstancia();

    private Integer anoAtual;
    private Integer anoSemestre;
    private Integer idDisciplinaAtual;
    private Integer idCursoAtual;
    private Integer idSemestreLetivoAtual;

    // Aba 1
    private Disciplina disciplinaSelecionadaTabela;

    // Aba 2
    private Usuario professorSelecionadoTabela;

    // Aba 3
    private AtribuicaoProfessor atribuicaoAtual;
    private final Map<String, CheckBox> mapaCheckBoxes = new HashMap<>();
    private void atualizarBotaoSalvar() {boolean algumMarcado = mapaCheckBoxes.values().stream().anyMatch(CheckBox::isSelected);btnSalvarAtrib.setDisable(!algumMarcado);}

    // Aba 4
    private final IntegerProperty totalTemas           = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasGeradas         = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasMinistradas     = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasPendentes       = new SimpleIntegerProperty(0);
    private final IntegerProperty aulasCanceladas      = new SimpleIntegerProperty(0);
    private final IntegerProperty cargaMinima          = new SimpleIntegerProperty(0);
    private final DoubleProperty  percentualConclusao  = new SimpleDoubleProperty(0.0);
    private Usuario professorSelecionado = new Usuario();

// INICIALIZAÇÃO
// ════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        configurarIds();

        // Aba 3 PRIMEIRO — para idSemestreLetivoAtual ser preenchido antes
        // das abas que dependem dele (colDiscProf)       ← CORRIGIDO (ordem)
        configurarAbaAtribuicoes();

        // Aba 1
        configurarTabelaDisciplinas();
        configurarComboboxCurso();
        configurarSpinnerDisciplina();
        carregarDisciplinas();

        // Aba 2
        configurarTabelaProfessores();
        recarregarTabelaProfessores();

        // Aba 4
        mapearComponentesFxmlOcultos();
        configurarEstatisticasPlanejamento();
        configurarPainelVisualizacaoUS10();
    }

    private void configurarIds() {
        this.idDisciplinaAtual = logado.getIdDisciplina();
        this.idCursoAtual      = logado.getIdCurso();
        this.anoAtual          = logado.getAno();
        this.anoSemestre       = logado.getAnoSemestre();
    }

    // ════════════════════════════════════════════════════════════════════════
    // ABA 1 — DISCIPLINAS
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleNovaDisciplina() {
        disciplinaSelecionadaTabela = null;
        lblTituloFormDisc.setText("Nova Disciplina");
        limparCamposDisc();
    }

    @FXML
    private void handleSelecionarDisciplina(MouseEvent event) {
        Disciplina selecionada = tabelaDisciplinas.getSelectionModel().getSelectedItem();
        if (selecionada == null) return;

        disciplinaSelecionadaTabela = selecionada;
        lblTituloFormDisc.setText("Editar Disciplina");
        tfDiscNome.setText(selecionada.getNome());
        cbDiscSemestreCurso.setValue(selecionada.getSemestre_curso());
        spDiscCH.getValueFactory().setValue(selecionada.getCarga_horaria_minima());
        ocultarErrosDisc();
        ocultarFeedbackDisc();
    }

    @FXML
    private void handleLimparDisc(ActionEvent event) {
        disciplinaSelecionadaTabela = null;
        lblTituloFormDisc.setText("Nova Disciplina");
        limparCamposDisc();
    }

    @FXML
    private void handleSalvarDisciplina(ActionEvent event) {
        if (tfDiscNome.getText().isBlank()) {
            errDiscNome.setText("O nome da disciplina é obrigatório.");
            errDiscNome.setVisible(true);
            errDiscNome.setManaged(true);
            return;
        }
        errDiscNome.setVisible(false);
        errDiscNome.setManaged(false);

        if (cbDiscSemestreCurso.getValue() == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Selecione o semestre do curso.");
            return;
        }

        Disciplina disciplina = new Disciplina();
        disciplina.setCurso_id(logado.getIdCurso());
        disciplina.setNome(tfDiscNome.getText().trim());
        disciplina.setSemestre_curso(cbDiscSemestreCurso.getValue());
        disciplina.setCarga_horaria_minima(spDiscCH.getValue());
        disciplina.setDeletado_em(null);

        try {
            if (disciplinaSelecionadaTabela != null) {
                disciplina.setId_disciplina(disciplinaSelecionadaTabela.getId_disciplina());
                disciplinaDAO.atualizarDisciplina(disciplina);
                disciplinaSelecionadaTabela = disciplina;
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina alterada com sucesso.");
            } else {
                disciplinaDAO.inserirDisciplina(disciplina);
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina cadastrada com sucesso.");
            }
            tabelaDisciplinas.getItems().setAll(disciplinaDAO.listarDisciplinas());
            recarregarDisciplinasUI();

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar a disciplina:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeletarDisciplina(Disciplina disciplina) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);
        confirmacao.setContentText("Deseja excluir a disciplina \"" + disciplina.getNome() + "\"?");

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta != ButtonType.OK) return;

            try {
                disciplinaDAO.excluirDisciplina(disciplina.getId_disciplina()); // getter correto

                if (disciplinaSelecionadaTabela != null
                        && disciplinaSelecionadaTabela.getId_disciplina() == disciplina.getId_disciplina()) {
                    disciplinaSelecionadaTabela = null;
                    limparCamposDisc(); // em vez de handleLimparDisc() que não existe com esse nome
                }

                carregarDisciplinas();
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina excluída com sucesso.");
                recarregarDisciplinasUI();

            } catch (Exception e) {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao excluir a disciplina.");
                System.err.println(e.getMessage());
            }
        });
    }

    private void configurarColunaAcoesDisc() {
        colDiscAcoes.setCellFactory(col -> new TableCell<>() {

            private final Button btnDelete = new Button("EXCLUIR");

            {
                btnDelete.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;" +
                                "-fx-cursor: hand;"
                );
                btnDelete.setOnAction(e ->
                        handleDeletarDisciplina(getTableView().getItems().get(getIndex()))
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }

    // ── Helpers — Aba 1 ─────────────────────────────────────────────────────

    private void configurarTabelaDisciplinas() {
        colDiscNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDiscSemCurso.setCellValueFactory(new PropertyValueFactory<>("semestre_curso"));
        colDiscCH.setCellValueFactory(new PropertyValueFactory<>("carga_horaria_minima"));
        configurarcolDisProf();
        configurarColunaAcoesDisc();
    }

    private void configurarComboboxCurso() {
        cbDiscSemestreCurso.setConverter(new StringConverter<>() {
            @Override public String toString(Integer numero) {
                return (numero == null) ? "" : numero + "º Semestre";
            }
            @Override public Integer fromString(String string) { return null; }
        });
    }

    private void configurarSpinnerDisciplina() {
        spDiscCH.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(80, 999, 80));
    }

    private void carregarDisciplinas() {                              // ← CORRIGIDO
        try {
            tabelaDisciplinas.getItems().setAll(
                    disciplinaDAO.listarDisciplinasPorCurso(idCursoAtual)
            );
        } catch (SQLException e) {
            System.err.println("Erro ao carregar disciplinas: " + e.getMessage());
        }
        preencherComboBoxSemestres();
    }

    private void preencherComboBoxSemestres() {
        cbDiscSemestreCurso.getItems().clear();
        if (this.idCursoAtual == null) return;
        try {
            int qtdSemestres = cursoDAO.buscarQtdSemestresPorId(idCursoAtual);
            for (int i = 1; i <= qtdSemestres; i++) {
                cbDiscSemestreCurso.getItems().add(i);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar semestres do curso: " + e.getMessage());
        }
    }

    private void limparCamposDisc() {
        tfDiscNome.clear();
        cbDiscSemestreCurso.getSelectionModel().clearSelection();
        spDiscCH.getValueFactory().setValue(80);
        ocultarErrosDisc();
        ocultarFeedbackDisc();
    }

    private void ocultarErrosDisc() {
        errDiscNome.setVisible(false);
        errDiscNome.setManaged(false);
    }

    private void ocultarFeedbackDisc() {
        lblFeedbackDisc.setVisible(false);
        lblFeedbackDisc.setManaged(false);
    }

    private void exibirFeedbackDisc(String mensagem, boolean sucesso) {
        lblFeedbackDisc.setText(mensagem);
        lblFeedbackDisc.setStyle(sucesso
                ? "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblFeedbackDisc.setVisible(true);
        lblFeedbackDisc.setManaged(true);
    }

    private void configurarcolDisProf() {
        colDiscProf.setCellValueFactory(cellData -> {
            try {
                String nome = atribuicaoProfessorDAO.buscarNomeProfessorPorDisciplina(
                        cellData.getValue().getId_disciplina(), idSemestreLetivoAtual);
                return new SimpleStringProperty(nome);
            } catch (SQLException e) {
                return new SimpleStringProperty("-");
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // ABA 2 — PROFESSORES
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleNovoProfessor(ActionEvent event) {
        lblTituloFormProf.setText("Novo Professor");
        handleLimparProf();
    }

    @FXML
    private void handleSelecionarProfessor(MouseEvent event) {
        Usuario selecionado = tabelaProfessores.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        professorSelecionadoTabela = selecionado;
        lblTituloFormProf.setText("Editar Professor");
        tfProfNome.setText(selecionado.getNome());
        tfProfEmail.setText(selecionado.getEmail());
        pfProfSenha.clear();
    }

    @FXML
    private void handleAtribuirMesmo(ActionEvent event) {
        try {
            boolean jaProfessor = usuarioTipoDAO.usuarioPossuiTipoAtivo(logado.getId_usuario(), "PROF");

            if (jaProfessor) {
                // Verifica se já tem atribuição no semestre atual
                if (idSemestreLetivoAtual != null) {
                    List<AtribuicaoProfessor> atribuicoes = atribuicaoProfessorDAO
                            .listarPorProfessorESemestre(logado.getId_usuario(), idSemestreLetivoAtual);
                    if (!atribuicoes.isEmpty()) {
                        exibirAlerta(Alert.AlertType.INFORMATION, "Aviso",
                                "Você já está cadastrado como professor e possui atribuições neste semestre.");
                        return;
                    }
                } else {
                    exibirAlerta(Alert.AlertType.INFORMATION, "Aviso",
                            "Você já está cadastrado como professor.");
                    return;
                }
            }

            // Restaura o usuário caso esteja soft-deletado por fluxo anterior com bug
            usuarioDAO.restaurarUsuario(logado.getId_usuario());

            // Insere o tipo PROF com INSERT IGNORE — não explode se já existir
            usuarioTipoDAO.inserirCoordenadorAosProfessores(logado.getId_usuario());

            recarregarTabelaProfessores();
            recarregarProfessoresUI();

            // Popup obrigatório
            Alert popup = new Alert(Alert.AlertType.CONFIRMATION);
            popup.setTitle("Auto-atribuição como professor");
            popup.setHeaderText(null);
            popup.setContentText(
                    "Você foi adicionado como professor.\n" +
                            "Para concluir, é obrigatório atribuir uma disciplina e horário(s).\n\n" +
                            "• Atribuir agora → vai para a aba de Atribuições\n" +
                            "• Cancelar → desfaz e remove as atribuições");

            ButtonType btnAtribuir = new ButtonType("Atribuir agora");
            ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
            popup.getButtonTypes().setAll(btnAtribuir, btnCancelar);

            popup.showAndWait().ifPresent(resposta -> {
                if (resposta == btnAtribuir) {
                    try {
                        // Suspende listener para não disparar carregarGrade() no meio
                        cbAtribProf.setOnAction(null);

                        cbAtribProf.getItems().setAll(usuarioDAO.listarTodosProfessores());

                        cbAtribProf.getItems().stream()
                                .filter(u -> u.getId_usuario().equals(logado.getId_usuario()))
                                .findFirst()
                                .ifPresent(cbAtribProf::setValue);

                        // Reativa listener e dispara grade manualmente
                        cbAtribProf.setOnAction(e -> carregarGrade());
                        carregarGrade();

                    } catch (SQLException e) {
                        System.err.println("Erro ao recarregar combo: " + e.getMessage());
                    }
                    tabPanePrincipal.getSelectionModel().select(tabAtribuicoes);

                } else {
                    // Rollback: não mexe em usuario_tipo por causa da FK do curso
                    // Apenas remove atribuições do semestre caso existam
                    if (idSemestreLetivoAtual != null) {
                        try {
                            List<AtribuicaoProfessor> atribuicoes = atribuicaoProfessorDAO
                                    .listarPorProfessorESemestre(
                                            logado.getId_usuario(), idSemestreLetivoAtual);
                            for (AtribuicaoProfessor ap : atribuicoes) {
                                atribuicaoHorarioDAO.excluirPorAtribuicao(
                                        ap.getId_atribuicao_professor());
                                atribuicaoProfessorDAO.excluir(
                                        ap.getId_atribuicao_professor());
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                    recarregarTabelaProfessores();
                    recarregarProfessoresUI();
                    exibirAlerta(Alert.AlertType.INFORMATION, "Cancelado",
                            "Auto-atribuição desfeita. Nenhum dado foi salvo.");
                }
            });

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro",
                    "Falha ao processar auto-atribuição:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerificarEmailProf(KeyEvent event) {
        String email = tfProfEmail.getText().trim();
        if (email.length() < 3) {
            avisoEmailDup.setVisible(false);
            avisoEmailDup.setManaged(false);
            return;
        }
        try {
            boolean existe = usuarioDAO.emailJaExiste(email);
            avisoEmailDup.setVisible(existe);
            avisoEmailDup.setManaged(existe);
        } catch (SQLException e) {
            System.err.println("Erro ao verificar e-mail: " + e.getMessage());
        }
    }

    @FXML
    private void handleLimparProf() {
        professorSelecionadoTabela = null;
        lblTituloFormProf.setText("Novo Professor");
        tfProfNome.clear();
        tfProfEmail.clear();
        pfProfSenha.clear();
    }

    @FXML
    private void handleSalvarProfessor() {
        if (tfProfNome.getText().isBlank() || tfProfEmail.getText().isBlank()) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Nome e e-mail são obrigatórios.");
            return;
        }
        if (avisoEmailDup.isVisible()) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "O e-mail informado já está cadastrado.");
            return;
        }

        try {
            if (professorSelecionadoTabela != null) {
                // ── EDIÇÃO ───────────────────────────────────────────────────
                professorSelecionadoTabela.setNome(tfProfNome.getText().trim());
                professorSelecionadoTabela.setEmail(tfProfEmail.getText().trim());
                if (!pfProfSenha.getText().isBlank()) {
                    professorSelecionadoTabela.setSenha_hash(pfProfSenha.getText());
                }
                usuarioDAO.editarUsuario(professorSelecionadoTabela);
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Professor atualizado com sucesso.");
                handleLimparProf();
                recarregarTabelaProfessores();

            } else {
                // ── INSERÇÃO ─────────────────────────────────────────────────
                if (pfProfSenha.getText().isBlank()) {
                    exibirAlerta(Alert.AlertType.ERROR, "Erro", "A senha é obrigatória para novo professor.");
                    return;
                }

                Usuario professor = new Usuario();
                professor.setNome(tfProfNome.getText().trim());
                professor.setEmail(tfProfEmail.getText().trim());
                professor.setSenha_hash(pfProfSenha.getText());
                professor.setCriado_em(LocalDate.now());

                int novoId = usuarioDAO.inserirUsuarioRetornandoId(professor);
                if (novoId == -1) {
                    exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao inserir o professor no banco.");
                    return;
                }

                UsuarioTipo usuarioTipo = new UsuarioTipo();
                usuarioTipo.setUsuario_id(novoId);
                usuarioTipo.setTipo("PROF");
                usuarioTipoDAO.inserirUsuarioTipo(usuarioTipo);

                // PopUp: atribuir agora ou cancelar cadastro
                Alert popup = new Alert(Alert.AlertType.CONFIRMATION);
                popup.setTitle("Professor cadastrado");
                popup.setHeaderText(null);
                popup.setContentText("Professor salvo. Mas para concluir o cadastro, é necessário atribuir uma disciplina e horário(s).\n\n" +
                        "• Atribuir agora → vai para a aba de Atribuições\n" +
                        "• Cancelar cadastro → desfaz e remove do banco");

                ButtonType btnAtribuir = new ButtonType("Atribuir agora");
                ButtonType btnCancelar = new ButtonType("Cancelar cadastro", ButtonBar.ButtonData.CANCEL_CLOSE);
                popup.getButtonTypes().setAll(btnAtribuir, btnCancelar);

                popup.showAndWait().ifPresent(resposta -> {
                    if (resposta == btnAtribuir) {
                        try {
                            // Suspende o listener para não disparar carregarGrade() no meio da atualização
                            cbAtribProf.setOnAction(null);                                    // ← ADICIONADO

                            cbAtribProf.getItems().setAll(usuarioDAO.listarTodosProfessores());

                            cbAtribProf.getItems().stream()
                                    .filter(u -> u.getId_usuario().equals(logado.getId_usuario()))
                                    .findFirst()
                                    .ifPresent(cbAtribProf::setValue);

                            // Reativa o listener e dispara a grade manualmente
                            cbAtribProf.setOnAction(e -> carregarGrade());                    // ← ADICIONADO
                            carregarGrade();                                                   // ← ADICIONADO

                        } catch (SQLException e) {
                            System.err.println("Erro ao recarregar combo: " + e.getMessage());
                        }
                        tabPanePrincipal.getSelectionModel().select(tabAtribuicoes);
                    } else {
                        usuarioTipoDAO.excluirUsuarioTipo(logado.getId_usuario(), "PROF");
                        recarregarTabelaProfessores();
                        recarregarProfessoresUI();
                        exibirAlerta(Alert.AlertType.INFORMATION, "Cancelado",
                                "Auto-atribuição desfeita. Nenhum dado foi salvo.");
                    }
                });
            }
            recarregarProfessoresUI();
        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro",
                    "Falha ao salvar o professor:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helpers — Aba 2 ─────────────────────────────────────────────────────

    private void configurarTabelaProfessores() {
        colProfNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colProfEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        configurarColunaAcoesProf();
    }

    private void recarregarTabelaProfessores() {                      // ← CORRIGIDO
        try {
            tabelaProfessores.getItems().setAll(
                    usuarioDAO.listarTodosProfessores()
            );
        } catch (SQLException e) {
            System.err.println("Erro ao recarregar tabela de professores: " + e.getMessage());
        }
    }

    private void handleDeletarProfessor(Usuario professor) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar exclusão");
        confirmacao.setHeaderText(null);

        boolean ehOProprioCoordenador = professor.getId_usuario().equals(logado.getId_usuario());

        if (ehOProprioCoordenador) {
            confirmacao.setContentText(
                    "Deseja remover seu papel de professor?\n" +
                            "Suas atribuições de disciplina e horários serão removidas.\n" +
                            "Seu cadastro como coordenador não será afetado.");
        } else {
            confirmacao.setContentText(
                    "Deseja excluir o professor \"" + professor.getNome() + "\"?");
        }

        confirmacao.showAndWait().ifPresent(resposta -> {
            if (resposta != ButtonType.OK) return;

            try {
                if (ehOProprioCoordenador) {
                    if (idSemestreLetivoAtual != null) {
                        List<AtribuicaoProfessor> atribuicoes = atribuicaoProfessorDAO
                                .listarPorProfessorESemestre(
                                        logado.getId_usuario(), idSemestreLetivoAtual);
                        for (AtribuicaoProfessor ap : atribuicoes) {
                            atribuicaoHorarioDAO.excluirPorAtribuicao(ap.getId_atribuicao_professor());
                            atribuicaoProfessorDAO.excluir(ap.getId_atribuicao_professor());
                        }
                    }
                    usuarioTipoDAO.excluirUsuarioTipoIgnorandoFK(logado.getId_usuario(), "PROF");
                } else {
                    usuarioTipoDAO.excluirUsuarioTipo(professor.getId_usuario(), "PROF");
                    usuarioDAO.excluirUsuario(professor.getId_usuario());
                }

                if (professorSelecionadoTabela != null
                        && professorSelecionadoTabela.getId_usuario().equals(professor.getId_usuario())) {
                    professorSelecionadoTabela = null;
                    handleLimparProf();
                }

                recarregarTabelaProfessores();
                recarregarProfessoresUI();
                recarregarDisciplinasUI();
                carregarDisciplinas();
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                        ehOProprioCoordenador
                                ? "Suas atribuições de professor foram removidas."
                                : "Professor excluído com sucesso.");

            } catch (Exception e) {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao excluir professor.");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void configurarColunaAcoesProf() {
        colProfAcoes.setCellFactory(col -> new TableCell<>() {

            private final Button btnDelete = new Button("EXCLUIR");

            {
                btnDelete.setStyle(
                        "-fx-background-color: #e74c3c;" +
                                "-fx-text-fill: white;" +
                                "-fx-cursor: hand;"
                );
                btnDelete.setOnAction(e ->
                        handleDeletarProfessor(getTableView().getItems().get(getIndex()))
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDelete);
            }
        });
    }


    // ════════════════════════════════════════════════════════════════════════
    // ABA 3 — ATRIBUIÇÕES
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleAtribContextChange(ActionEvent event) {
        carregarGrade();
    }

    @FXML
    private void handleLimparGrade(ActionEvent event) {
        mapaCheckBoxes.values().forEach(cb -> cb.setSelected(false));
        btnSalvarAtrib.setDisable(true);
        lblConflito.setVisible(false);
        lblConflito.setManaged(false);
    }

    @FXML
    private void handleSalvarAtribuicao(ActionEvent event) {
        Usuario    prof = cbAtribProf.getValue();
        Disciplina disc = cbAtribDisc.getValue();

        if (prof == null || disc == null) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Selecione um professor e uma disciplina.");
            return;
        }

        List<AtribuicaoHorario> horariosSelecionados = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : mapaCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                String[] partes = entry.getKey().split("_");
                AtribuicaoHorario ah = new AtribuicaoHorario();
                ah.setDia_semana(Integer.parseInt(partes[0]));
                ah.setHorario_curso_id(Integer.parseInt(partes[1]));
                horariosSelecionados.add(ah);
            }
        }

        if (horariosSelecionados.isEmpty()) {
            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Selecione ao menos um horário.");
            return;
        }

        try {
            // Guarda de conflito antes de salvar
            int atribuicaoIdExcluir = atribuicaoAtual != null
                    ? atribuicaoAtual.getId_atribuicao_professor() : -1;

            for (AtribuicaoHorario ah : horariosSelecionados) {
                HorarioCurso hc = horarioCursoDAO.buscarPorId(ah.getHorario_curso_id());
                String conflito = atribuicaoHorarioDAO.buscarConflitoParaProfessor(
                        prof.getId_usuario(),
                        idSemestreLetivoAtual,
                        ah.getDia_semana(),
                        hc.getNumero_ordem(),
                        atribuicaoIdExcluir);

                if (conflito != null) {
                    exibirAlerta(Alert.AlertType.ERROR, "Conflito de Horário",
                            "O professor já leciona \"" + conflito +
                                    "\" nesse horário neste semestre. Salvo cancelado.");
                    return;
                }
            }

            if (atribuicaoAtual != null) {
                horariosSelecionados.forEach(ah ->
                        ah.setAtribuicao_id(atribuicaoAtual.getId_atribuicao_professor()));
                atribuicaoHorarioDAO.substituirHorarios(
                        atribuicaoAtual.getId_atribuicao_professor(), horariosSelecionados);

            } else {
                AtribuicaoProfessor novaAtrib = new AtribuicaoProfessor();
                novaAtrib.setProfessor_id(prof.getId_usuario());
                novaAtrib.setDisciplina_id(disc.getId_disciplina());
                novaAtrib.setSemestre_letivo_id(idSemestreLetivoAtual);
                atribuicaoProfessorDAO.salvar(novaAtrib);

                horariosSelecionados.forEach(ah ->
                        ah.setAtribuicao_id(novaAtrib.getId_atribuicao_professor()));
                atribuicaoHorarioDAO.salvarLote(horariosSelecionados);

                atribuicaoAtual = novaAtrib;
            }

            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Atribuição salva com sucesso.");
            recarregarTabelaProfessores();
            carregarGrade();
            carregarDisciplinas();

        } catch (SQLException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro",
                    "Falha ao salvar atribuição:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helpers — Aba 3 ─────────────────────────────────────────────────────

    private void recarregarDisciplinasUI() {                          // ← CORRIGIDO
        try {
            List<Disciplina> disciplinas =
                    disciplinaDAO.listarDisciplinasPorCurso(idCursoAtual);
            tabelaDisciplinas.getItems().setAll(disciplinas);
            cbAtribDisc.getItems().setAll(disciplinas);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void recarregarProfessoresUI() {                          // ← CORRIGIDO
        try {
            List<Usuario> profs = usuarioDAO.listarTodosProfessores();
            tabelaProfessores.getItems().setAll(profs);
            cbAtribProf.getItems().setAll(profs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configurarAbaAtribuicoes() {
        try {
            idSemestreLetivoAtual = semestreLetivoDAO.getIdSemestreLetivo(anoAtual, anoSemestre);
            if (idSemestreLetivoAtual == null) return;

            cbAtribProf.getItems().setAll(usuarioDAO.listarTodosProfessores());
            cbAtribProf.setConverter(new StringConverter<>() {
                @Override public String toString(Usuario u)   { return u != null ? u.getNome() : ""; }
                @Override public Usuario fromString(String s) { return null; }
            });

            cbAtribDisc.getItems().setAll(disciplinaDAO.listarDisciplinasPorCurso(idCursoAtual));
            cbAtribDisc.setConverter(new StringConverter<>() {
                @Override public String toString(Disciplina d)   { return d != null ? d.getNome() : ""; }
                @Override public Disciplina fromString(String s) { return null; }
            });

            cbAtribProf.setOnAction(e -> carregarGrade());
            cbAtribDisc.setOnAction(e -> carregarGrade());

        } catch (SQLException e) {
            System.err.println("Erro ao configurar aba de atribuições: " + e.getMessage());
        }
    }

    private void carregarGrade() {
        Usuario    prof = cbAtribProf.getValue();
        Disciplina disc = cbAtribDisc.getValue();

        gradeAtribuicao.getChildren().clear();
        mapaCheckBoxes.clear();
        atribuicaoAtual = null;
        lblConflito.setVisible(false);
        lblConflito.setManaged(false);
        btnSalvarAtrib.setDisable(true);

        if (prof == null || disc == null || idSemestreLetivoAtual == null) return;

        try {
            AtribuicaoProfessor atribuicaoExistente = atribuicaoProfessorDAO
                    .buscarPorDisciplinaESemestre(disc.getId_disciplina(), idSemestreLetivoAtual);

            if (atribuicaoExistente != null
                    && atribuicaoExistente.getProfessor_id() != prof.getId_usuario()) {
                lblConflito.setText("⚠ Esta disciplina já está atribuída a outro professor neste semestre.");
                lblConflito.setVisible(true);
                lblConflito.setManaged(true);
                return;
            }

            atribuicaoAtual = atribuicaoExistente;

            List<HorarioCurso> horarios = horarioCursoDAO
                    .listarHorariosPorCurso(idCursoAtual, idSemestreLetivoAtual)
                    .stream()
                    .filter(h -> "aula".equals(h.getTipo()))
                    .toList();

            if (horarios.isEmpty()) return;

            Set<String> marcados = new HashSet<>();
            if (atribuicaoAtual != null) {
                atribuicaoHorarioDAO
                        .listarPorAtribuicao(atribuicaoAtual.getId_atribuicao_professor())
                        .forEach(ah -> marcados.add(ah.getDia_semana() + "_" + ah.getHorario_curso_id()));
            }

            String[] dias = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
            for (int col = 0; col < dias.length; col++) {
                Label lblDia = new Label(dias[col]);
                lblDia.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
                gradeAtribuicao.add(lblDia, col + 1, 0);
            }

            for (int row = 0; row < horarios.size(); row++) {
                HorarioCurso hc = horarios.get(row);

                Label lblHorario = new Label(hc.getHora_inicio() + " – " + hc.getHora_fim());
                lblHorario.setStyle("-fx-font-size: 11px;");
                gradeAtribuicao.add(lblHorario, 0, row + 1);

                for (int dia = 1; dia <= 6; dia++) {
                    String chave = dia + "_" + hc.getId_horario_curso();
                    CheckBox cb = new CheckBox();
                    boolean jaMarcado = marcados.contains(chave);
                    cb.setSelected(jaMarcado);
                    mapaCheckBoxes.put(chave, cb);

                    int atribuicaoIdExcluir = atribuicaoAtual != null
                            ? atribuicaoAtual.getId_atribuicao_professor() : -1;

                    // Só verifica conflito em horários que NÃO pertencem à atribuição atual
                    if (!jaMarcado) {
                        String conflitoProfessor = atribuicaoHorarioDAO.buscarConflitoParaProfessor(
                                prof.getId_usuario(),
                                idSemestreLetivoAtual,
                                dia,
                                hc.getId_horario_curso(),
                                atribuicaoIdExcluir);

                        String conflitoCurso = (conflitoProfessor == null)
                                ? atribuicaoHorarioDAO.buscarConflitoNoCurso(
                                disc.getCurso_id(),
                                disc.getSemestre_curso(),
                                idSemestreLetivoAtual,
                                dia,
                                hc.getId_horario_curso(),
                                atribuicaoIdExcluir)
                                : null;

                        if (conflitoProfessor != null) {
                            cb.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; " +
                                    "-fx-border-radius: 3px; -fx-padding: 1px;");
                            cb.setTooltip(new Tooltip(
                                    prof.getNome() + " já leciona \"" + conflitoProfessor + "\" neste horário"));
                            cb.setDisable(true);
                        } else if (conflitoCurso != null) {
                            cb.setStyle("-fx-border-color: #e67e22; -fx-border-width: 2px; " +
                                    "-fx-border-radius: 3px; -fx-padding: 1px;");
                            cb.setTooltip(new Tooltip(
                                    "Prof. " + conflitoCurso + " já ocupa este horário neste semestre do curso"));
                            cb.setDisable(true);
                        }
                    }

                    final int diaFinal  = dia;
                    final int horarioId = hc.getId_horario_curso();
                    cb.setOnAction(e -> {
                        verificarConflito(prof, diaFinal, horarioId, cb);
                        atualizarBotaoSalvar();
                    });

                    gradeAtribuicao.add(cb, dia, row + 1);
                }
            }

            atualizarBotaoSalvar();

        } catch (SQLException e) {
            System.err.println("Erro ao carregar grade: " + e.getMessage());
        }
    }

    private void verificarConflito(Usuario prof, int diaSemana, int horarioCursoId, CheckBox cb) {
        if (!cb.isSelected()) {
            lblConflito.setVisible(false);
            lblConflito.setManaged(false);
            return;
        }
        try {
            int atribuicaoIdExcluir = atribuicaoAtual != null
                    ? atribuicaoAtual.getId_atribuicao_professor() : -1;

            // Conflito 1: professor já tem esse horário em outro lugar
            String conflitoProfessor = atribuicaoHorarioDAO.buscarConflitoParaProfessor(
                    prof.getId_usuario(),
                    idSemestreLetivoAtual,
                    diaSemana,
                    horarioCursoId,
                    atribuicaoIdExcluir);

            if (conflitoProfessor != null) {
                lblConflito.setText("⚠ " + prof.getNome() +
                        " já está alocado em \"" + conflitoProfessor + "\" neste horário.");
                lblConflito.setVisible(true);
                lblConflito.setManaged(true);
                cb.setSelected(false);
                return;
            }

            // Conflito 2: outro professor já ocupa o horário no curso+semestre_curso
            Disciplina disc = cbAtribDisc.getValue();
            if (disc != null) {
                String conflitoCurso = atribuicaoHorarioDAO.buscarConflitoNoCurso(
                        disc.getCurso_id(),
                        disc.getSemestre_curso(),
                        idSemestreLetivoAtual,
                        diaSemana,
                        horarioCursoId,
                        atribuicaoIdExcluir);

                if (conflitoCurso != null) {
                    lblConflito.setText("⚠ Prof. " + conflitoCurso +
                            " já ocupa este horário neste semestre do curso.");
                    lblConflito.setVisible(true);
                    lblConflito.setManaged(true);
                    cb.setSelected(false);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro ao verificar conflito: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ABA 4 — PLANEJAMENTOS DOS PROFESSORES
    // ════════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private void mapearComponentesFxmlOcultos() {
        try {
            if (tabPanePrincipal != null && tabPanePrincipal.getTabs().size() >= 4) {
                Tab aba4 = tabPanePrincipal.getTabs().get(3);
                Node conteudoAba = aba4.getContent();

                if (conteudoAba instanceof SplitPane) {
                    SplitPane split = (SplitPane) conteudoAba;
                    if (!split.getItems().isEmpty() && split.getItems().get(0) instanceof VBox) {
                        VBox vboxEsquerdo = (VBox) split.getItems().get(0);
                        for (Node node : vboxEsquerdo.getChildren()) {
                            if (node instanceof ListView) {
                                this.listaProfessoresEsquerda = (ListView<Usuario>) node;
                                break;
                            }
                        }
                    }
                }
            }

            if (painelEstatCoord != null) {
                painelEstatCoord.getChildren().clear();

                lblTotalTemas        = new Label("0");
                lblAulasGeradas      = new Label("0");
                lblAulasMinistradas  = new Label("0");
                lblAulasPendentes    = new Label("0");
                lblAulasCanceladas   = new Label("0");
                lblCargaHorariaMinima = new Label("0");
                progressConclusao    = new ProgressBar(0);
                lblPercentual        = new Label("0.0%");
                chartStatusAulas     = new PieChart();

                GridPane grid = new GridPane();
                grid.setHgap(15);
                grid.setVgap(10);
                grid.add(new Label("Total de Temas:"),       0, 0); grid.add(lblTotalTemas,        1, 0);
                grid.add(new Label("Aulas Geradas:"),        0, 1); grid.add(lblAulasGeradas,       1, 1);
                grid.add(new Label("Aulas Ministradas:"),    0, 2); grid.add(lblAulasMinistradas,   1, 2);
                grid.add(new Label("Aulas Pendentes:"),      2, 0); grid.add(lblAulasPendentes,     3, 0);
                grid.add(new Label("Aulas Canceladas:"),     2, 1); grid.add(lblAulasCanceladas,    3, 1);
                grid.add(new Label("Carga Mínima Curso:"),   2, 2); grid.add(lblCargaHorariaMinima, 3, 2);

                HBox progressoBox = new HBox(10,
                        new Label("Progresso Realizado:"), lblPercentual, progressConclusao);
                progressConclusao.setPrefWidth(200);

                painelEstatCoord.getChildren().addAll(grid, new Separator(), progressoBox, chartStatusAulas);
            }
        } catch (Exception e) {
            System.err.println("Aviso: Falha na varredura de componentes: " + e.getMessage());
        }
    }

    private void configurarEstatisticasPlanejamento() {
        if (lblTotalTemas       != null) lblTotalTemas.textProperty().bind(totalTemas.asString());
        if (lblAulasGeradas     != null) lblAulasGeradas.textProperty().bind(aulasGeradas.asString());
        if (lblAulasMinistradas != null) lblAulasMinistradas.textProperty().bind(aulasMinistradas.asString());
        if (lblAulasPendentes   != null) lblAulasPendentes.textProperty().bind(aulasPendentes.asString());
        if (lblAulasCanceladas  != null) lblAulasCanceladas.textProperty().bind(aulasCanceladas.asString());
        if (lblCargaHorariaMinima != null) lblCargaHorariaMinima.textProperty().bind(cargaMinima.asString());
        if (progressConclusao   != null) progressConclusao.progressProperty().bind(percentualConclusao);
        if (lblPercentual       != null) lblPercentual.textProperty().bind(
                Bindings.concat(Bindings.format("%.1f", percentualConclusao.multiply(100)), "%"));
    }

    private void configurarPainelVisualizacaoUS10() {
        if (listaProfessoresEsquerda != null) {
            listaProfessoresEsquerda.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Usuario item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getEmail());
                }
            });

            listaProfessoresEsquerda.getSelectionModel().selectedItemProperty()
                    .addListener((obs, antigo, selecionado) -> {
                        if (selecionado != null) {
                            montarEstruturaArvoreCentral(selecionado);
                            professorSelecionado = selecionado;
                        }
                    });

            try {
                List<Usuario> proflist = atribuicaoProfessorDAO.listarProfessoresComAtribuicao(logado.getId_usuario());
                listaProfessoresEsquerda.getItems().setAll(proflist);
                if (!proflist.isEmpty()) {
                    listaProfessoresEsquerda.getSelectionModel().selectFirst();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        configurarColorizacaoCustomizadaTree();
    }

    private void montarEstruturaArvoreCentral(Usuario professor) {
        if (treePlanoCoord == null) return;

        try {
            TreeItem<Object> rootNode = new TreeItem<>(professor.getNome());
            rootNode.setExpanded(true);

            System.out.println("PROFESSOR SELECIONADO: "+professorSelecionado);
            this.professorSelecionado = usuarioDAO.buscarUsuarioPorEmailUnico(professor.getEmail());

            if (this.professorSelecionado == null) {
                System.err.println("Erro: Professor não encontrado no banco pelo email: " + professor.getEmail());
                return;
            }

            List<Disciplina> disciplinasDoCurso = disciplinaDAO.listarDisciplinasProfessor(
                    logado.getId_usuario(),
                    this.professorSelecionado.getId_usuario()
            );

            for (Disciplina disc : disciplinasDoCurso) {
                List<Map<String, Object>> dadosBrutos = slotDAO.buscarDadosMixados(
                        anoAtual, anoSemestre, idCursoAtual, disc.getId_disciplina());

                if (dadosBrutos == null || dadosBrutos.isEmpty()) continue;

                TreeItem<Object> discNode = new TreeItem<>(disc.getNome().toUpperCase());
                discNode.setExpanded(true);

                List<SlotVisual> listaVisuais = dadosBrutos.stream().map(map -> new SlotVisual(
                        (SlotPlanejamento) map.get("entidade"),
                        (String) map.get("hora_inicio"),
                        (String) map.get("nome_tema")
                )).collect(Collectors.toList());

                Map<LocalDate, List<SlotVisual>> agrupadosPorData = listaVisuais.stream()
                        .collect(Collectors.groupingBy(
                                v -> v.getSlot().getData(), LinkedHashMap::new, Collectors.toList()));

                String[] diasDaSemana = {"", "Domingo", "Segunda-feira", "Terça-feira",
                        "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado"};

                for (Map.Entry<LocalDate, List<SlotVisual>> entry : agrupadosPorData.entrySet()) {
                    LocalDate data   = entry.getKey();
                    int diaNum       = data.getDayOfWeek().getValue() == 7 ? 1 : data.getDayOfWeek().getValue() + 1;
                    String labelDia  = String.format("%s (%s)", data, diasDaSemana[diaNum]);

                    TreeItem<Object> diaNode = new TreeItem<>(labelDia);
                    diaNode.setExpanded(true);
                    entry.getValue().forEach(v -> diaNode.getChildren().add(new TreeItem<>(v)));
                    discNode.getChildren().add(diaNode);
                }

                rootNode.getChildren().add(discNode);
            }

            treePlanoCoord.setRoot(rootNode);
            treePlanoCoord.setShowRoot(true);

            if (!disciplinasDoCurso.isEmpty()) {
                carregarEstatisticasContextoCoordenador(
                        anoAtual, anoSemestre, idCursoAtual,
                        disciplinasDoCurso.get(0).getId_disciplina(),
                        professor.getId_usuario());
                if (lblTituloCoordVisor != null) {
                    lblTituloCoordVisor.setText("Planejamento Detalhado: " + professor.getNome());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurarColorizacaoCustomizadaTree() {
        if (treePlanoCoord == null) return;

        treePlanoCoord.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else if (item instanceof SlotVisual) {
                    SlotVisual visual = (SlotVisual) item;
                    setText(visual.toString());
                    setStyle(switch (visual.getSlot().getStatus()) {
                        case "nao_ministrada"                        -> "-fx-text-fill: #f1c40f; -fx-font-weight: bold;";
                        case "cancelada_professor", "cancelada_adm"  -> "-fx-text-fill: #e74c3c; -fx-font-weight: bold;";
                        case "ministrada"                            -> "-fx-text-fill: #2ecc71; -fx-font-weight: bold;";
                        default                                      -> "";
                    });
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });
    }

    private void carregarEstatisticasContextoCoordenador(int ano, int semestreAno,
                                                         Integer id_curso, Integer id_disciplina, Integer idProfessor) {
        try {
            Map<String, Object> metricas = planejamentoDAO.obterEstatisticasGlobais(
                    ano, semestreAno, id_curso, id_disciplina, idProfessor);

            this.totalTemas.set(0);
            this.aulasGeradas.set(0);
            this.aulasMinistradas.set(0);
            this.aulasPendentes.set(0);
            this.aulasCanceladas.set(0);
            this.cargaMinima.set(0);
            this.percentualConclusao.set(0.0);
            if (chartStatusAulas != null) chartStatusAulas.getData().clear();

            if (metricas == null || metricas.isEmpty()) return;

            int totalAulas  = toInt(metricas.get("totalAulas"));
            int ministradas = toInt(metricas.get("ministradas"));
            int pendentes   = toInt(metricas.get("pendentes"));
            int canceladas  = toInt(metricas.get("canceladas"));
            int chMinima    = toInt(metricas.get("chMinima"));
            int totalT      = toInt(metricas.get("totalTemas"));

            this.aulasGeradas.set(totalAulas);
            this.aulasMinistradas.set(ministradas);
            this.aulasPendentes.set(pendentes);
            this.aulasCanceladas.set(canceladas);
            this.cargaMinima.set(chMinima);
            this.totalTemas.set(totalT);
            this.percentualConclusao.set((chMinima > 0) ? (double) ministradas / chMinima : 0.0);

            if (chartStatusAulas != null) {
                ObservableList<PieChart.Data> fatias = FXCollections.observableArrayList();
                if (ministradas > 0)
                    fatias.add(new PieChart.Data("Ministradas (" + ministradas + ")", ministradas));
                if (pendentes > 0)
                    fatias.add(new PieChart.Data("Pendentes ("   + pendentes   + ")", pendentes));
                if (canceladas > 0)
                    fatias.add(new PieChart.Data("Canceladas ("  + canceladas  + ")", canceladas));
                chartStatusAulas.setData(fatias);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int toInt(Object valor) {
        if (valor instanceof Number) return ((Number) valor).intValue();
        return 0;
    }

    // ── Wrapper para formatação visual dos slots na TreeView ─────────────────

    private static class SlotVisual {
        private final SlotPlanejamento slot;
        private final String horaInicio;
        private final String nomeTema;

        public SlotVisual(SlotPlanejamento slot, String horaInicio, String nomeTema) {
            this.slot       = slot;
            this.horaInicio = horaInicio;
            this.nomeTema   = nomeTema;
        }

        public SlotPlanejamento getSlot() { return slot; }

        @Override
        public String toString() {
            String tag = switch (slot.getStatus()) {
                case "nao_ministrada"                       -> "[Pendente]";
                case "ministrada"                           -> "[Lecionada]";
                case "cancelada_professor", "cancelada_adm" -> "[Cancelada]";
                default                                     -> "[" + slot.getStatus() + "]";
            };
            String conteudo = (nomeTema != null) ? nomeTema : "Aula sem tema definido";
            String hora     = (horaInicio != null && horaInicio.length() >= 5)
                    ? horaInicio.substring(0, 5) : "00:00";
            return String.format("(%s): %s %s",
                    hora, conteudo, tag);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // UTILITÁRIOS GERAIS
    // ════════════════════════════════════════════════════════════════════════

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}