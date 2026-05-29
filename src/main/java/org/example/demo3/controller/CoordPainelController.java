package org.example.demo3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.*;
import org.example.demo3.entity.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class CoordPainelController {

    // ════════════════════════════════════════════════════════════════════════
    // CAMPOS FXML — ABA 1: DISCIPLINAS
    // ════════════════════════════════════════════════════════════════════════

    @FXML private TableView<Disciplina>            tabelaDisciplinas;
    @FXML private TableColumn<Disciplina, String>  colDiscNome;
    @FXML private TableColumn<Disciplina, Integer> colDiscSemCurso;
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

    @FXML private ComboBox<Usuario>    cbAtribProf;
    @FXML private ComboBox<Disciplina> cbAtribDisc;
    @FXML private GridPane             gradeAtribuicao;
    @FXML private Label                lblConflito;
    @FXML private Label                lblFeedbackAtrib;
    @FXML private Button               btnSalvarAtrib;

    // ════════════════════════════════════════════════════════════════════════
    // CAMPOS FXML — PLANEJAMENTOS
    // ════════════════════════════════════════════════════════════════════════

    @FXML private Label       lblTituloCoordVisor;
    @FXML private TabPane     tabVisorCoord;
    @FXML private TreeView<?> treePlanoCoord;
    @FXML private VBox        painelEstatCoord;

    // ════════════════════════════════════════════════════════════════════════
    // DAOs
    // ════════════════════════════════════════════════════════════════════════

    private final DisciplinaDAO          disciplinaDAO          = new DisciplinaDAO();
    private final UsuarioDAO             usuarioDAO             = new UsuarioDAO();
    private final UsuarioTipoDAO         usuarioTipoDAO         = new UsuarioTipoDAO();
    private final AtribuicaoProfessorDAO atribuicaoProfessorDAO = new AtribuicaoProfessorDAO();
    private final HorarioCursoDAO        horarioCursoDAO        = new HorarioCursoDAO();
    private final AtribuicaoHorarioDAO   atribuicaoHorarioDAO   = new AtribuicaoHorarioDAO();
    private final SemestreLetivoDAO      semestreLetivoDAO      = new SemestreLetivoDAO();

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

    // ════════════════════════════════════════════════════════════════════════
    // INICIALIZAÇÃO
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        configurarIds();
        configurarTabelaDisciplinas();
        configurarSpinnerDisciplina();
        configurarTabelaProfessores();
        recarregarTabelaProfessores();
        configurarAbaAtribuicoes();
    }

    private void configurarIds() {
        this.idDisciplinaAtual    = logado.getIdDisciplina();
        this.idCursoAtual         = logado.getIdCurso();
        this.anoAtual             = logado.getAno();
        this.anoSemestre          = logado.getAnoSemestre();
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

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar a disciplina:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeletarDisciplina() {
        // TODO: soft-delete da disciplina selecionada
    }

    // ── Helpers — Aba 1 ─────────────────────────────────────────────────────

    private void configurarTabelaDisciplinas() {
        colDiscNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colDiscSemCurso.setCellValueFactory(new PropertyValueFactory<>("semestre_curso"));
        colDiscCH.setCellValueFactory(new PropertyValueFactory<>("carga_horaria_minima"));
        colDiscProf.setCellValueFactory(new PropertyValueFactory<>("nome"));
    }

    private void configurarSpinnerDisciplina() {
        spDiscCH.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(80, 999, 80));
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
            boolean jaProfessor = usuarioTipoDAO.listarUsuariosTipo().stream()
                    .anyMatch(ut -> ut.getUsuario_id().equals(logado.getId_usuario())
                            && "PROF".equals(ut.getTipo()));

            if (!jaProfessor) {
                UsuarioTipo usuarioTipo = new UsuarioTipo();
                usuarioTipo.setUsuario_id(logado.getId_usuario());
                usuarioTipo.setTipo("PROF");
                usuarioTipoDAO.inserirUsuarioTipo(usuarioTipo);
            }

            exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso",
                    jaProfessor ? "Você já está cadastrado como professor."
                            : "Você foi adicionado como professor com sucesso.");
            recarregarTabelaProfessores();

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro",
                    "Falha ao se atribuir como professor:\n" + e.getMessage());
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

                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Professor cadastrado com sucesso.");
            }

            handleLimparProf();
            recarregarTabelaProfessores();

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
    }

    private void recarregarTabelaProfessores() {
        try {
            tabelaProfessores.getItems().setAll(usuarioDAO.listarTodosProfessores());
        } catch (SQLException e) {
            System.err.println("Erro ao recarregar tabela de professores: " + e.getMessage());
        }
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
        lblConflito.setVisible(false);
        lblConflito.setManaged(false);
    }

    @FXML
    private void handleSalvarAtribuicao(ActionEvent event) {
        Usuario prof  = cbAtribProf.getValue();
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

        try {
            if (atribuicaoAtual != null) {
                // ── EDIÇÃO ───────────────────────────────────────────────────
                horariosSelecionados.forEach(ah ->
                        ah.setAtribuicao_id(atribuicaoAtual.getId_atribuicao_professor()));
                atribuicaoHorarioDAO.substituirHorarios(
                        atribuicaoAtual.getId_atribuicao_professor(), horariosSelecionados);

            } else {
                // ── INSERÇÃO ─────────────────────────────────────────────────
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

        } catch (SQLException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro",
                    "Falha ao salvar atribuição:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Helpers — Aba 3 ─────────────────────────────────────────────────────

    private void configurarAbaAtribuicoes() {
        try {
            idSemestreLetivoAtual = semestreLetivoDAO.getIdSemestreLetivo(anoAtual, anoSemestre);
            if (idSemestreLetivoAtual == null) return;

            cbAtribProf.getItems().setAll(usuarioDAO.listarTodosProfessores());
            cbAtribProf.setConverter(new StringConverter<>() {
                @Override public String toString(Usuario u)    { return u != null ? u.getNome() : ""; }
                @Override public Usuario fromString(String s)  { return null; }
            });

            cbAtribDisc.getItems().setAll(disciplinaDAO.listarDisciplinasPorCurso(idCursoAtual));
            cbAtribDisc.setConverter(new StringConverter<>() {
                @Override public String toString(Disciplina d)    { return d != null ? d.getNome() : ""; }
                @Override public Disciplina fromString(String s)  { return null; }
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

        if (prof == null || disc == null || idSemestreLetivoAtual == null) return;

        try {
            List<HorarioCurso> horarios = horarioCursoDAO
                    .listarHorariosPorCurso(idCursoAtual, idSemestreLetivoAtual)
                    .stream()
                    .filter(h -> "aula".equals(h.getTipo()))
                    .toList();

            if (horarios.isEmpty()) return;

            atribuicaoAtual = atribuicaoProfessorDAO
                    .buscarPorDisciplinaESemestre(disc.getId_disciplina(), idSemestreLetivoAtual);

            Set<String> marcados = new HashSet<>();
            if (atribuicaoAtual != null) {
                atribuicaoHorarioDAO
                        .listarPorAtribuicao(atribuicaoAtual.getId_atribuicao_professor())
                        .forEach(ah -> marcados.add(ah.getDia_semana() + "_" + ah.getHorario_curso_id()));
            }

            // Cabeçalho
            String[] dias = {"Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
            for (int col = 0; col < dias.length; col++) {
                Label lblDia = new Label(dias[col]);
                lblDia.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
                gradeAtribuicao.add(lblDia, col + 1, 0);
            }

            // Linhas de horários
            for (int row = 0; row < horarios.size(); row++) {
                HorarioCurso hc = horarios.get(row);

                Label lblHorario = new Label(hc.getHora_inicio() + " – " + hc.getHora_fim());
                lblHorario.setStyle("-fx-font-size: 11px;");
                gradeAtribuicao.add(lblHorario, 0, row + 1);

                for (int dia = 1; dia <= 6; dia++) {
                    String chave = dia + "_" + hc.getId_horario_curso();
                    CheckBox cb = new CheckBox();
                    cb.setSelected(marcados.contains(chave));
                    mapaCheckBoxes.put(chave, cb);

                    final int diaFinal     = dia;
                    final int horarioId    = hc.getId_horario_curso();
                    cb.setOnAction(e -> verificarConflito(prof, diaFinal, horarioId, cb));

                    gradeAtribuicao.add(cb, dia, row + 1);
                }
            }

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

            String nomeConflito = atribuicaoHorarioDAO.buscarConflito(
                    idSemestreLetivoAtual, diaSemana, horarioCursoId, atribuicaoIdExcluir);

            if (nomeConflito != null) {
                lblConflito.setText("⚠ Conflito com " + nomeConflito + " neste horário.");
                lblConflito.setVisible(true);
                lblConflito.setManaged(true);
                cb.setSelected(false);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar conflito: " + e.getMessage());
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