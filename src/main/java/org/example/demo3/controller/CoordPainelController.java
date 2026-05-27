package org.example.demo3.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.example.demo3.entity.Disciplina;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

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

    @FXML
    public void initialize() {

    }

    //MÉTODOS DA ABA DE DISCIPLINAS
    @FXML
    void handleNovaDisciplina(ActionEvent event) {
        // Lógica para preparar formulário de nova disciplina
    }

    @FXML
    void handleSelecionarDisciplina(MouseEvent event) {
        // Lógica ao clicar na tabela (popular campos para edição)
    }

    @FXML
    void handleLimparDisc(ActionEvent event) {
        // Limpar campos de texto e seletores
    }

    @FXML
    void handleSalvarDisciplina(ActionEvent event) {
        // Validar e persistir os dados da disciplina
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



}
