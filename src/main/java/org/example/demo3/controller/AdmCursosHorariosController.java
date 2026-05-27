package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.*;
import org.example.demo3.dto.AdmCursoExibicao;
import org.example.demo3.entity.Curso;
import org.example.demo3.entity.TemplateHorarioTurno;
import org.example.demo3.entity.Usuario;
import org.example.demo3.entity.UsuarioTipo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdmCursosHorariosController {

    @FXML private TextField tfCursoNome;
    @FXML private ToggleButton tbManha;
    @FXML private ToggleButton tbNoite;
    @FXML private Spinner<Integer> spQtdSemestres;
    @FXML private ComboBox<String> cbProfessorCurso;
    @FXML private TitledPane painelFormCurso;
    @FXML private CheckBox checkUsarCadastroProfessor;
    @FXML private TableView<AdmCursoExibicao> tabelaCursos;
    @FXML private TableColumn<AdmCursoExibicao, String> colCNome;
    @FXML private TableColumn<AdmCursoExibicao, String> colCTurno;
    @FXML private TableColumn<AdmCursoExibicao, Integer> colCQtdSemestres;
    @FXML private TableColumn<AdmCursoExibicao, String> colCCoordNome;
    @FXML private Label lblTituloHorarios;
    @FXML private Button btnSalvarCurso;
    @FXML private Label lblProcessoSalvarCurso;
    @FXML private TableView<TemplateHorarioTurno> tabelaHorarios;
    @FXML private TableColumn<TemplateHorarioTurno, String> colHTipo;
    @FXML private TableColumn<TemplateHorarioTurno, Integer> colHNumero;
    @FXML private TableColumn<TemplateHorarioTurno, java.time.LocalTime> colHInicio;
    @FXML private TableColumn<TemplateHorarioTurno, java.time.LocalTime> colHFim;
    @FXML private TableColumn<TemplateHorarioTurno, Void> colHAcao;

    private UsuarioAtual logado = UsuarioAtual.getInstancia();
    private Integer ano;
    private Integer anoSemestre;

    private Integer idCursoProcessando;
    private Integer idSemestreLetivoProcessando;
    private List<TemplateHorarioTurno> thtProcessando;
    private ObservableList<TemplateHorarioTurno> linhasHorarios;

    private String nomeCursoProcessando;
    private String turnoProcessando;
    private Integer qtdSemestresProcessando;

    private Boolean profsCarregados = false;
    private Map<String, Integer> mapaProfessores = new HashMap<>();
    private Integer idProfessorSelecionado;

    @FXML
    public void initialize(){

        logado.usuarioAdm();

        logado.anoProperty().addListener(
                (obs, velho, novo) -> {
                    if (novo != null) {
                        this.ano = novo;
                    }
                });

        logado.anoSemestreProperty().addListener(
                (obs, velho, novo) -> {
                    if (novo != null) {
                        this.anoSemestre = novo;
                    }
                });

        cbProfessorCurso.setDisable(true);

        //colCNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        //colCTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        //colCQtdSemestres.setCellValueFactory(new PropertyValueFactory<>("qtd_semestres"));
        //colCCoordNome.setCellValueFactory(new PropertyValueFactory<>("nome_coord"));

        colHTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colHNumero.setCellValueFactory(new PropertyValueFactory<>("numero_ordem"));
        colHInicio.setCellValueFactory(new PropertyValueFactory<>("hora_inicio"));
        colHFim.setCellValueFactory(new PropertyValueFactory<>("hora_fim"));
    }


    @FXML
    public void handleNovoCurso() {
        // TODO: Preparar e expandir o formulário lateral para a criação de um novo curso
    }

    @FXML
    public void handleSelecionarCurso(MouseEvent event) {
        // TODO: Capturar o curso selecionado na tabela e atualizar o formulário e a tabela de horários
    }

    @FXML
    public void handleTurnoChange() {
        // TODO: Gerenciar o estado de seleção mútua dos botões de alternância de turno (Manhã/Noite)
    }

    @FXML
    public void handleCancelarCurso() {
        if (btnSalvarCurso.isDisabled()) {
            try {
                CursoDAO cDAO = new CursoDAO();
                UsuarioTipoDAO utDao = new UsuarioTipoDAO();

                cDAO.deletarCursoProcessando(idCursoProcessando);

                System.out.println(cbProfessorCurso.getValue());
                if (checkUsarCadastroProfessor.isSelected() && idProfessorSelecionado != null) {
                    utDao.removerUsuarioTipo(cbProfessorCurso.getValue(), "COORD");
                }

                btnSalvarCurso.setDisable(false);
                lblProcessoSalvarCurso.setVisible(false);
                lblProcessoSalvarCurso.setManaged(false);
                lblTituloHorarios.setText("Horários — selecione um curso à esquerda");
                if (linhasHorarios != null) linhasHorarios.clear();

                alterarEstadoEdicaoDadosCurso(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        alterarEstadoEdicaoDadosCurso(false);
    }

    @FXML
    public void usarCadastroProfessor(){
        if (checkUsarCadastroProfessor.isSelected()){
            cbProfessorCurso.setDisable(false);
            carregarProfs();
        } else {
            cbProfessorCurso.setDisable(true);
        }
    }

    private void carregarCursos(){
        try{
            CursoDAO cDao = new CursoDAO();
            List<AdmCursoExibicao> lista = cDao.listarCursosDTO(this.ano, this.anoSemestre);
            ObservableList linhasCurso = FXCollections.observableArrayList(lista);
            tabelaCursos.setItems(linhasCurso);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void carregarProfs() {
        try {
            UsuarioDAO uDao = new UsuarioDAO();
            List<Usuario> profs = uDao.listarProfSemestreLetivo(this.ano, this.anoSemestre);

            mapaProfessores.clear();
            ObservableList<String> opcoesProfs = FXCollections.observableArrayList();
            for (Usuario prof : profs) {
                opcoesProfs.add(prof.getEmail());
                mapaProfessores.put(prof.getEmail(), prof.getId_usuario());
            }
            cbProfessorCurso.setItems(opcoesProfs);

            // Atualiza controle
            profsCarregados = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSelecaoProfessor(){
        if (checkUsarCadastroProfessor.isSelected()){
            String emailSelecionado = cbProfessorCurso.getValue();
            if (emailSelecionado != null) {
                this.idProfessorSelecionado = mapaProfessores.get(emailSelecionado);
            }
            System.out.println("email: "+emailSelecionado);
        } else {
            this.idProfessorSelecionado = null;
        }

    }

    public void alterarEstadoEdicaoDadosCurso(Boolean estado){
        tfCursoNome.setDisable(estado);
        tbManha.setDisable(estado);
        tbNoite.setDisable(estado);
        spQtdSemestres.setDisable(estado);
        checkUsarCadastroProfessor.setDisable(estado);
        if (checkUsarCadastroProfessor.isSelected()){
            cbProfessorCurso.setDisable(false);
        } else {
            cbProfessorCurso.setDisable(true);
        }

    }

    @FXML
    public void handleSalvarCurso() {
        nomeCursoProcessando = tfCursoNome.getText();
        qtdSemestresProcessando = spQtdSemestres.getValue();


        if (tbManha.isSelected()){
            turnoProcessando = "manha";
        } else {
            turnoProcessando = "noite";
        }

        try {
            CursoDAO cDao = new CursoDAO();
            if (checkUsarCadastroProfessor.isSelected()){
                UsuarioTipoDAO utDao = new UsuarioTipoDAO();

                System.out.println("IdPROFESSOr: "+idProfessorSelecionado);

                utDao.inserirUsuarioTipo(new UsuarioTipo(idProfessorSelecionado, "COORD"));
                this.idCursoProcessando = cDao.inserirCursoRetornaId(idProfessorSelecionado,
                        nomeCursoProcessando, turnoProcessando, qtdSemestresProcessando);

            } else {
                this.idCursoProcessando = cDao.inserirCursoRetornaId(nomeCursoProcessando,
                        turnoProcessando, qtdSemestresProcessando);
            }
            lblTituloHorarios.setText("Horários — Curso selecionado: "+nomeCursoProcessando);
            btnSalvarCurso.setDisable(true);
            lblProcessoSalvarCurso.setVisible(true);
            lblProcessoSalvarCurso.setManaged(true);
            alterarEstadoEdicaoDadosCurso(true);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SEÇÃO DE HORÁRIOS (Painel Direito)
    // ═════════════════════════════════════════════════════════════════════════

    @FXML
    public void handleAplicarTemplate() {
        // TODO: Preencher a tabela com uma estrutura de horários padrão com base no turno do curso
        SemestreLetivoDAO slDao = new SemestreLetivoDAO();
        TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
        HorarioCursoDAO hcDao = new HorarioCursoDAO();
        try {
            thtProcessando = thtDao.listarPorTurno(turnoProcessando);

            idSemestreLetivoProcessando = slDao.getIdSemestreLetivo(logado.getAno(), logado.getAnoSemestre());

            linhasHorarios = FXCollections.observableArrayList(thtProcessando);
            tabelaHorarios.setItems(linhasHorarios);
        } catch (SQLException e){

        }

    }

    @FXML
    public void handlePropagarTurno() {
        // TODO: Replicar a grade de horários atual do curso para todos os outros cursos do mesmo turno
    }

    @FXML
    public void handleAdicionarLinhaHorario() {
        // TODO: Inserir uma nova linha vazia ou editável na TableView de horários
    }

    @FXML
    public void handleSalvarHorarios() {
        // TODO: Validar o encadeamento cronológico das linhas e salvar as alterações na tabela de horários
        HorarioCursoDAO hcDao = new HorarioCursoDAO();
        try {
            hcDao.inserirTemplateHorarioCurso(thtProcessando, idCursoProcessando, idSemestreLetivoProcessando);
            alterarEstadoEdicaoDadosCurso(false);
            btnSalvarCurso.setDisable(false);
            lblProcessoSalvarCurso.setVisible(false);
            lblProcessoSalvarCurso.setManaged(false);
            carregarCursos();
        } catch (SQLException e){

        }
    }
}
