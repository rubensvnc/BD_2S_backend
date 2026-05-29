package org.example.demo3.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.AtribuicaoProfessorDAO;
import org.example.demo3.dao.DisciplinaDAO;
import org.example.demo3.dao.UsuarioDAO;
import org.example.demo3.dao.UsuarioTipoDAO;
import org.example.demo3.entity.AtribuicaoProfessor;
import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.SQLException;
import java.time.LocalDate;

public class CoordPainelController {

    // ════════════════════════════════════════════════════════════════════════
    // CAMPOS FXML — ABA 1: DISCIPLINAS
    // ════════════════════════════════════════════════════════════════════════

    @FXML private TableView<Disciplina>          tabelaDisciplinas;
    @FXML private TableColumn<Disciplina, String>  colDiscNome;
    @FXML private TableColumn<Disciplina, Integer> colDiscSemCurso;
    @FXML private TableColumn<Disciplina, Integer> colDiscCH;
    @FXML private TableColumn<Disciplina, String>  colDiscProf;
    @FXML private TableColumn<Disciplina, Void>    colDiscAcoes;
    @FXML private Label           lblTituloFormDisc;
    @FXML private TextField       tfDiscNome;
    @FXML private Label           errDiscNome;
    @FXML private ComboBox<Integer> cbDiscSemestreCurso;
    @FXML private Spinner<Integer>  spDiscCH;
    @FXML private Label           lblFeedbackDisc;

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

    @FXML private Label      lblTituloCoordVisor;
    @FXML private TabPane    tabVisorCoord;
    @FXML private TreeView<?> treePlanoCoord;
    @FXML private VBox       painelEstatCoord;

    // ════════════════════════════════════════════════════════════════════════
    // ESTADO / DAOs
    // ════════════════════════════════════════════════════════════════════════

    DisciplinaDAO disciplinaDAO = new DisciplinaDAO();
    UsuarioDAO usuarioDAO = new UsuarioDAO();
    UsuarioTipoDAO usuarioTipoDAO = new UsuarioTipoDAO();
    AtribuicaoProfessorDAO atribuicaoProfessorDAO = new AtribuicaoProfessorDAO();
    UsuarioAtual logado = UsuarioAtual.getInstancia();
    private Integer anoAtual;
    private Integer anoSemestre;
    private Integer idDisciplinaAtual;
    private Integer idSemestreAtual;
    private Integer idCursoAtual;


    // null = modo inserção; não-null = modo edição
    private Disciplina disciplinaSelecionadaTabela;
    private Usuario professorSelecionadoTabela; // null = inserção, não-null = edição

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
    }

    // ════════════════════════════════════════════════════════════════════════
    // ABA 1 — DISCIPLINAS  (handlers de botão)
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
        // ── Validações ──────────────────────────────────────────────────────
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

        // ── Monta entidade ──────────────────────────────────────────────────
        Disciplina disciplina = new Disciplina();
        disciplina.setCurso_id(logado.getIdCurso());
        disciplina.setNome(tfDiscNome.getText().trim());
        disciplina.setSemestre_curso(cbDiscSemestreCurso.getValue());
        disciplina.setCarga_horaria_minima(spDiscCH.getValue());
        disciplina.setDeletado_em(null);

        try {
            if (disciplinaSelecionadaTabela != null) {
                // ── EDIÇÃO ───────────────────────────────────────────────────
                disciplina.setId_disciplina(disciplinaSelecionadaTabela.getId_disciplina());
                disciplinaDAO.atualizarDisciplina(disciplina);
                disciplinaSelecionadaTabela = disciplina;
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina alterada com sucesso.");
            } else {
                // ── INSERÇÃO ─────────────────────────────────────────────────
                disciplinaDAO.inserirDisciplina(disciplina);
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina cadastrada com sucesso.");
            }

            tabelaDisciplinas.getItems().setAll(disciplinaDAO.listarDisciplinas());

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar a disciplina:\n" + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void handleDeletarDisciplina() {
        // TODO: remover disciplina selecionada (soft-delete)
    }

    // ── Helpers — ABA 1 ─────────────────────────────────────────────────────

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
    // ABA 2 — PROFESSORES  (handlers de botão)
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
        pfProfSenha.clear(); // senha nunca vem do banco
    }

    @FXML
    private void handleAtribuirMesmo(ActionEvent event) {
        try {
            // Verifica se o coordenador já tem o tipo PROF
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
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao se atribuir como professor:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVerificarEmailProf(KeyEvent event) {
        String email = tfProfEmail.getText().trim();
        // Só consulta a partir de alguns caracteres para não spammar o banco
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
        // ── Validações ──────────────────────────────────────────────────────
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
                professorSelecionadoTabela.setNome(tfProfNome.getText().trim());
                professorSelecionadoTabela.setEmail(tfProfEmail.getText().trim());

                if (!pfProfSenha.getText().isBlank()) {
                    professorSelecionadoTabela.setSenha_hash(pfProfSenha.getText());
                }
                // Se senha em branco, o objeto já carrega o hash original vindo da tabela

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

            professorSelecionadoTabela = null;
            handleLimparProf();
            recarregarTabelaProfessores();

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar o professor:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

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
    // ABA 3 — ATRIBUIÇÕES  (handlers de botão)
    // ════════════════════════════════════════════════════════════════════════

    @FXML
    private void handleAtribContextChange(ActionEvent event) {
        // TODO: Atualizar a grade de horários ao trocar o professor ou a disciplina selecionada
    }

    @FXML
    private void handleLimparGrade(ActionEvent event) {
        // TODO: Desmarcar todos os CheckBoxes da grade de atribuição atual
    }

    @FXML
    private void handleSalvarAtribuicao(ActionEvent event) {
        // TODO: Salvar o vínculo entre professor, disciplina e horários selecionados
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

    private void configurarIds() {
        this.idDisciplinaAtual = logado.getIdDisciplina();
        this.idCursoAtual = logado.getIdCurso();
        this.anoAtual = logado.getAno();
        this.anoSemestre = logado.getAnoSemestre();
    }
}