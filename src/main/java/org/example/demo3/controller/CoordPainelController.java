package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.DisciplinaDAO;
import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.Usuario;

import java.awt.event.ActionEvent;

public class CoordPainelController {

    //ABA DE DISCIPLINAS
    @FXML private TableView<Disciplina> tabelaDisciplinas;
    @FXML private TableColumn<Disciplina, String> colDiscNome;
    @FXML private TableColumn<Disciplina, Integer> colDiscSemCurso;
    @FXML private TableColumn<Disciplina, Integer> colDiscCH;
    @FXML private TableColumn<Disciplina, String> colDiscProf;
    @FXML private TableColumn<Disciplina, Void> colDiscAcoes;
    @FXML private Label lblTituloFormDisc;
    @FXML private TextField tfDiscNome;
    @FXML private Label errDiscNome;
    @FXML private ComboBox<Integer> cbDiscSemestreCurso;
    @FXML private Spinner<Integer> spDiscCH;
    @FXML private Label lblFeedbackDisc;

    //ABA DE PROFESSORES
    @FXML private TableView<Usuario> tabelaProfessores;
    @FXML private TableColumn<Usuario, String> colProfNome;
    @FXML private TableColumn<Usuario, String> colProfEmail;
    @FXML private TableColumn<Usuario, Void> colProfAcoes;
    @FXML private Label lblTituloFormProf;
    @FXML private Button btnAtribuirMesmo;
    @FXML private TextField tfProfNome;
    @FXML private Label errProfNome;
    @FXML private TextField tfProfEmail;
    @FXML private Label avisoEmailDup;
    @FXML private Label errProfEmail;
    @FXML private PasswordField pfProfSenha;
    @FXML private Label errProfSenha;
    @FXML private Label lblFeedbackProf;

    //ABA DE ATRIBUIÇÕES
    @FXML private ComboBox<Usuario> cbAtribProf;
    @FXML private ComboBox<Disciplina> cbAtribDisc;
    @FXML private GridPane gradeAtribuicao;
    @FXML private Label lblConflito;
    @FXML private Label lblFeedbackAtrib;
    @FXML private Button btnSalvarAtrib;

    //PLANEJAMENTOS
    @FXML private Label lblTituloCoordVisor;
    @FXML private TabPane tabVisorCoord;
    @FXML private TreeView<?> treePlanoCoord;
    @FXML private VBox painelEstatCoord;

    //DAOS
    DisciplinaDAO disciplinaDAO = new DisciplinaDAO();

    //ATRIBUTOS DE OUTRAS ENTIDADES
    private UsuarioAtual logado = UsuarioAtual.getInstancia();
    private Integer anoAtual;
    private Integer anoSemestre;
    private Integer idDisciplinaAtual;
    private Integer idSemestreAtual;
    // No bloco de atributos — adicione esta linha junto com os outros campos de estado:
    private Disciplina disciplinaSelecionadaTabela; // null = modo inserção, não-null = modo edição



    @FXML
    public void initialize() {
        configurarTabelaDisciplinas();
        configurarSpinnerDisciplina();

    }

    //MÉTODOS DA ABA DE DISCIPLINAS
    @FXML
    private void handleNovaDisciplina(ActionEvent event) {
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
        // ── Validações ───────────────────────────────────────────────────────────
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

        // ── Monta entidade ───────────────────────────────────────────────────────
        Disciplina disciplina = new Disciplina();
        disciplina.setCurso_id(logado.getIdCurso());
        disciplina.setNome(tfDiscNome.getText().trim());
        disciplina.setSemestre_curso(cbDiscSemestreCurso.getValue());
        disciplina.setCarga_horaria_minima(spDiscCH.getValue());
        disciplina.setDeletado_em(null);

        try {
            if (disciplinaSelecionadaTabela != null) {
                // ── EDIÇÃO ───────────────────────────────────────────────────────
                disciplina.setId_disciplina(disciplinaSelecionadaTabela.getId_disciplina());
                disciplinaDAO.atualizarDisciplina(disciplina);
                disciplinaSelecionadaTabela = disciplina;
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina alterada com sucesso.");
            } else {
                // ── INSERÇÃO ─────────────────────────────────────────────────────
                disciplinaDAO.inserirDisciplina(disciplina);
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Disciplina cadastrada com sucesso.");
            }

            tabelaDisciplinas.getItems().setAll(disciplinaDAO.listarDisciplinas());

        } catch (Exception e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar a disciplina:\n" + e.getMessage());
            System.out.println(e.getMessage());
        }
    }



    //EVENTOS (QUE NÃO SÃO BOTÕES) DA DISCIPLINA
    private void configurarTabelaDisciplinas() {
        colDiscNome.setCellValueFactory(new PropertyValueFactory<>("disciplina"));
        colDiscSemCurso.setCellValueFactory(new PropertyValueFactory<>("semestre_curso"));
        colDiscCH.setCellValueFactory(new PropertyValueFactory<>("carga_horaria_minima"));
        colDiscProf.setCellValueFactory(new PropertyValueFactory<>("nome"));
    }
    private void configurarSpinnerDisciplina() {
        spDiscCH.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
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







    //MÉTODOS DA ABA DE PROFESSORES
    @FXML
    void handleNovoProfessor(ActionEvent event) {
        // Resetar formulário de professor
    }

    @FXML
    void handleSelecionarProfessor(MouseEvent event) {
        // Carregar dados do professor selecionado nos campos
    }

    @FXML
    void handleAtribuirMesmo(ActionEvent event) {
        // Lógica para usar dados do coordenador atual como professor
    }

    @FXML
    void handleVerificarEmailProf(KeyEvent event) {
        // Validação em tempo real (onKeyReleased)
    }

    @FXML
    void handleLimparProf(ActionEvent event) {
        // Limpar campos de cadastro de professor
    }

    @FXML
    void handleSalvarProfessor(ActionEvent event) {
        // Gravar novo professor ou atualizar existente
    }


    //MÉTODOS DE ATRIBUIÇÕES
    @FXML
    void handleAtribContextChange(ActionEvent event) {
        // Atualizar grade quando mudar o professor ou disciplina selecionados
    }

    @FXML
    void handleLimparGrade(ActionEvent event) {
        // Desmarcar todos os horários da grade
    }

    @FXML
    void handleSalvarAtribuicao(ActionEvent event) {
        // Salvar o mapeamento de horários no banco de dados
    }



    //MÉTODOS PARA SEREM CHAMADOS

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }


}
