package org.example.demo3.controller;

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
    @FXML private CheckBox checkUsarProfessor;
    @FXML private TableView<AdmCursoExibicao> tabelaCursos;
    @FXML private TableColumn<AdmCursoExibicao, String> colCursoNome;
    @FXML private TableColumn<AdmCursoExibicao, String> colCursoTurno;
    @FXML private TableColumn<AdmCursoExibicao, Integer> colCursoSemestres;
    @FXML private TableColumn<AdmCursoExibicao, String> colCursoCoordenador;
    @FXML private TableColumn<AdmCursoExibicao, String> colCursoAcoes;
    @FXML private Label lblTituloHorarios;
    @FXML private Button btnSalvarCurso;
    @FXML private Button btnSalvarHorario;
    @FXML private Label lblProcessoSalvarCurso;
    @FXML private TableView<TemplateHorarioTurno> tabelaHorarios;
    @FXML private TableColumn<TemplateHorarioTurno, String> colHTipo;
    @FXML private TableColumn<TemplateHorarioTurno, Integer> colHNumero;
    @FXML private TableColumn<TemplateHorarioTurno, java.time.LocalTime> colHInicio;
    @FXML private TableColumn<TemplateHorarioTurno, java.time.LocalTime> colHFim;
    @FXML private TableColumn<TemplateHorarioTurno, Void> colHAcao;
    @FXML private Button btnDeletarHorarios;

    // Label do CheckBox — alterado via lookup no initialize
    // (sem fx:id no FXML, por isso usamos referência direta ao CheckBox)

    private UsuarioAtual logado = UsuarioAtual.getInstancia();
    private Integer ano;
    private Integer anoSemestre;

    private Integer idCursoProcessando;
    private Integer idSemestreLetivoProcessando;
    private List<TemplateHorarioTurno> thtProcessando = new ArrayList<>();
    private ObservableList<TemplateHorarioTurno> linhasHorarios = FXCollections.observableArrayList();

    private String nomeCursoProcessando;
    private String turnoProcessando;
    private Integer qtdSemestresProcessando;

    private Integer idProfSelecionado;

    // DAOS:
    private UsuarioDAO uDao = new UsuarioDAO();
    private UsuarioTipoDAO utDao = new UsuarioTipoDAO();
    private CursoDAO cDao = new CursoDAO();
    private HorarioCursoDAO hcDao = new HorarioCursoDAO();

    @FXML
    public void initialize() {

        logado.usuarioAdm();

        this.ano = logado.getAno();
        this.anoSemestre = logado.getAnoSemestre();

        logado.anoProperty().addListener((obs, velho, novo) -> {
            if (novo != null) { this.ano = novo; carregarCursos(); }
        });

        logado.anoSemestreProperty().addListener((obs, velho, novo) -> {
            if (novo != null) { this.anoSemestre = novo; carregarCursos(); }
        });

        if (this.ano != null && this.anoSemestre != null) {
            carregarCursos();
        }

        tabelaCursos.getSelectionModel().selectedItemProperty().addListener(
                (observable, linhaAntiga, linhaSelecionadaCurso) -> {
                    if (linhaSelecionadaCurso != null) {
                        thtProcessando.clear();
                        linhasHorarios.clear();
                        preencherDadosEdicaoCurso(linhaSelecionadaCurso);
                    }
                });

        btnSalvarCurso.setOnAction(event -> handleSalvarCurso());

        cbProfessorCurso.setDisable(true);

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
                        btnDeletar.setOnAction(event -> {
                            AdmCursoExibicao cursoSelecionado = getTableView().getItems().get(getIndex());
                            handleDeletarCurso(cursoSelecionado);
                        });
                    }
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btnDeletar);
                    }
                };
            }
        });

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
                        btnDeletar.setOnAction(event -> {
                            TemplateHorarioTurno thtSelecionado = getTableView().getItems().get(getIndex());
                            handleDeletarHorario(thtSelecionado);
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btnDeletar);
                    }
                };
            }
        });

        habilitarEdicaoCelulaHorarios();
    }

    // =========================================================================
    //  CARREGAMENTO DE DADOS
    // =========================================================================

    /**
     * Carrega TODOS os cursos cadastrados, independente de possuírem
     * horários vinculados. Usa listarTodosCursosDTO() no DAO, que não
     * faz INNER JOIN com horario_curso.
     */
    private void carregarCursos() {
        try {
            CursoDAO cDao = new CursoDAO();
            List<AdmCursoExibicao> lista = cDao.listarTodosCursosDTO();
            tabelaCursos.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarHorarios() {
        if (idCursoProcessando != null && idSemestreLetivoProcessando != null) {
            try {

                List<HorarioCurso> listaHc = hcDao.listarHorariosPorCurso(
                        idCursoProcessando, idSemestreLetivoProcessando);

                thtProcessando.clear();
                for (HorarioCurso hc : listaHc) {
                    TemplateHorarioTurno tht = new TemplateHorarioTurno();
                    tht.setTipo(hc.getTipo());
                    tht.setNumero_ordem(hc.getNumero_ordem());
                    tht.setHora_inicio(hc.getHora_inicio());
                    tht.setHora_fim(hc.getHora_fim());
                    thtProcessando.add(tht);
                }

                linhasHorarios.clear();
                linhasHorarios.setAll(thtProcessando);
                tabelaHorarios.setItems(linhasHorarios);

            } catch (SQLException e) {
                e.printStackTrace();
                exibirAlerta("Erro", "Não foi possível atualizar a tabela de horários.", Alert.AlertType.ERROR);
            }
        } else {
            linhasHorarios.clear();
            tabelaHorarios.setItems(null);
        }
    }

    public void carregarProfessores(){
        UsuarioDAO uDao = new UsuarioDAO();
        ObservableList opcoesProfs = FXCollections.observableArrayList();

        try{
            List<Usuario> professores = uDao.listarProfSemestreLetivo(ano, anoSemestre);
            for (Usuario prof: professores){
                opcoesProfs.add(prof.getEmail());
            }

            cbProfessorCurso.setItems(opcoesProfs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //  AÇÕES DO FORMULÁRIO DE CURSO
    // =========================================================================

    @FXML
    public void handleNovoCurso() {
        reiniciarValoresDadosCurso();
        tabelaHorarios.setItems(null);
        painelFormCurso.setExpanded(true);

        btnSalvarCurso.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        btnSalvarCurso.setText("Salvar Curso");
        btnSalvarCurso.setOnAction(event -> handleSalvarCurso());

        btnSalvarHorario.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnSalvarHorario.setText("\uD83D\uDCBE Salvar Horários");
        btnSalvarHorario.setOnAction(event -> handleSalvarHorarios());

        btnDeletarHorarios.setManaged(false);
        btnDeletarHorarios.setVisible(false);
    }

    @FXML
    public void handleSelecionarCurso(MouseEvent event) {
        // Tratado pelo listener do selectedItemProperty no initialize
    }

    @FXML
    public void handleTurnoChange() {
        // Gerenciamento visual de exclusividade tratado pelo próprio ToggleButton
    }

    @FXML
    public void handleCancelarCurso() {
        if (btnSalvarCurso.isDisabled()) {
            try {
                CursoDAO cDAO = new CursoDAO();
                UsuarioTipoDAO utDao = new UsuarioTipoDAO();

                cDAO.deletarCursoProcessando(idCursoProcessando);

                if (checkUsarProfessor.isSelected() && idProfSelecionado != null) {
                    utDao.excluirUsuarioTipo(idProfSelecionado, "COORD");
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

    /**
     * Disparado pelo CheckBox "Atribuir a Coordenador".
     * Carrega apenas coordenadores sem curso vinculado.
     */
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
        try{
            if (checkUsarProfessor.isSelected()) {
                String selecionado = cbProfessorCurso.getValue();
                if (selecionado != null) {
                    idProfSelecionado = uDao.buscarUsuarioPorEmailUnico(selecionado).getId_usuario();
                }
            } else {
                idProfSelecionado = null;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    @FXML
    public void handleSalvarCurso() {
        nomeCursoProcessando     = tfCursoNome.getText();
        qtdSemestresProcessando  = spQtdSemestres.getValue();
        turnoProcessando         = tbManha.isSelected() ? "manha" : "noite";

        try {
            if (checkUsarProfessor.isSelected() && idProfSelecionado != null) {
                if (!utDao.usuarioPossuiTipoAtivo(idProfSelecionado, "COORD")) {
                    utDao.inserirUsuarioTipo(new UsuarioTipo(idProfSelecionado, "COORD"));
                }
            }

            if (checkUsarProfessor.isSelected() && idProfSelecionado != null) {
                this.idCursoProcessando = cDao.inserirCursoRetornaId(
                        idProfSelecionado, nomeCursoProcessando,
                        turnoProcessando, qtdSemestresProcessando);
            } else {
                this.idCursoProcessando = cDao.inserirCursoRetornaId(
                        nomeCursoProcessando, turnoProcessando, qtdSemestresProcessando);
            }

            lblTituloHorarios.setText("Horários — Curso selecionado: " + nomeCursoProcessando);
            btnSalvarCurso.setDisable(true);
            lblProcessoSalvarCurso.setVisible(true);
            lblProcessoSalvarCurso.setManaged(true);
            alterarEstadoEdicaoDadosCurso(true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void preencherDadosEdicaoCurso(AdmCursoExibicao c) {
        painelFormCurso.setExpanded(true);

        tfCursoNome.setText(c.getNome());
        if ("manha".equals(c.getTurno())) {
            tbManha.setSelected(true);
            tbNoite.setSelected(false);
        } else {
            tbNoite.setSelected(true);
            tbManha.setSelected(false);
        }

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

        AdmCursoExibicao linhaAnterior = new AdmCursoExibicao(
                c.getNome(), c.getTurno(), c.getQtd_semestres(), c.getEmail());

        btnSalvarCurso.setOnAction(event -> handleEditarCurso(linhaAnterior));

        try {
            HorarioCursoDAO hcDao = new HorarioCursoDAO();
            CursoDAO cDao         = new CursoDAO();
            SemestreLetivoDAO slDao = new SemestreLetivoDAO();

            idCursoProcessando         = cDao.listarIdCurso(c.getNome());
            idSemestreLetivoProcessando = slDao.getIdSemestreLetivo(
                    logado.getAno(), logado.getAnoSemestre());

            List<HorarioCurso> listaHc = hcDao.listarHorariosPorCurso(
                    idCursoProcessando, idSemestreLetivoProcessando);

            for (HorarioCurso hc : listaHc) {
                TemplateHorarioTurno tht = new TemplateHorarioTurno();
                tht.setTipo(hc.getTipo());
                tht.setNumero_ordem(hc.getNumero_ordem());
                tht.setHora_inicio(hc.getHora_inicio());
                tht.setHora_fim(hc.getHora_fim());
                thtProcessando.add(tht);
            }

            linhasHorarios.setAll(thtProcessando);
            tabelaHorarios.setItems(linhasHorarios);

            btnSalvarHorario.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
            btnSalvarHorario.setText("Editar Horarios");
            btnSalvarHorario.setOnAction(event -> handleEditarHorario());

            btnDeletarHorarios.setManaged(true);
            btnDeletarHorarios.setVisible(true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleEditarCurso(AdmCursoExibicao linhaAnterior) {
        CursoDAO cDao  = new CursoDAO();
        UsuarioDAO uDao = new UsuarioDAO();

        String  turno     = tbManha.isSelected() ? "manha" : "noite";
        String  emailCoord = checkUsarProfessor.isSelected()
                ? cbProfessorCurso.getValue()
                : null;

        AdmCursoExibicao dadosAlterados = new AdmCursoExibicao(
                tfCursoNome.getText(), turno,
                spQtdSemestres.getValueFactory().getValue(), emailCoord);

        try {
            Integer idCoord = null;
            if (emailCoord != null) {
                // email puro (modo edição) ou nome+email (modo seleção do ComboBox)
                String emailBusca = emailCoord.contains("(")
                        ? emailCoord.replaceAll(".*\\((.*)\\)", "$1")
                        : emailCoord;
                Usuario u = uDao.buscarUsuarioPorEmailUnico(emailBusca);
                if (u != null) idCoord = u.getId_usuario();
            }

            cDao.alterarCurso(dadosAlterados, idCoord, linhaAnterior.getNome());
            painelFormCurso.setExpanded(false);
            carregarCursos();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleEditarHorario() {
        HorarioCursoDAO hcDao = new HorarioCursoDAO();
        try {
            hcDao.removerHorariosCursoSL(idCursoProcessando, idSemestreLetivoProcessando);
            hcDao.inserirTemplateHorarioCurso(
                    new ArrayList<>(tabelaHorarios.getItems()),
                    idCursoProcessando, idSemestreLetivoProcessando);
        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Não é possível alterar",
                    "Estes horários não podem ser alterados ou removidos porque existem dependências vinculadas.",
                    Alert.AlertType.WARNING);
        }
    }

    public void handleDeletarCurso(AdmCursoExibicao linhaCurso) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja mesmo excluir o curso " + linhaCurso.getNome() + "?",
                ButtonType.YES, ButtonType.NO);
        alerta.showAndWait();

        if (alerta.getResult() == ButtonType.YES) {
            try {
                CursoDAO cDao = new CursoDAO();
                int idCurso   = cDao.listarIdCurso(linhaCurso.getNome());
                cDao.deletarCursoProcessando(idCurso);
                carregarCursos();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleDeletarHorario(TemplateHorarioTurno tht) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja mesmo excluir esse Horário?", ButtonType.YES, ButtonType.NO);
        alerta.showAndWait();

        if (alerta.getResult() == ButtonType.YES) {
            try {
                HorarioCursoDAO hcDao = new HorarioCursoDAO();
                hcDao.removerHorarioOrdemCursoSL(
                        tht.getNumero_ordem(), idCursoProcessando, idSemestreLetivoProcessando);
                carregarHorarios();
            } catch (SQLException e) {
                e.printStackTrace();
                exibirAlerta("Erro ao deletar",
                        "Não foi possível deletar o horário devido a dependências.",
                        Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void deletarTodosHorarios() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja mesmo excluir todos os horários?", ButtonType.YES, ButtonType.NO);
        alerta.showAndWait();

        if (alerta.getResult() == ButtonType.YES) {
            try {
                HorarioCursoDAO hcDao = new HorarioCursoDAO();
                hcDao.removerHorariosCursoSL(idCursoProcessando, idSemestreLetivoProcessando);
                carregarHorarios();
            } catch (SQLException e) {
                e.printStackTrace();
                exibirAlerta("Erro ao deletar",
                        "Não foi possível deletar os horários devido a dependências.",
                        Alert.AlertType.ERROR);
            }
        }
    }

    // =========================================================================
    //  HORÁRIOS
    // =========================================================================

    public void habilitarEdicaoCelulaHorarios() {
        colHTipo.setCellFactory(ComboBoxTableCell.forTableColumn("aula", "intervalo"));
        colHTipo.setOnEditCommit(event -> {
            event.getRowValue().setTipo(event.getNewValue());
            tabelaHorarios.refresh();
        });

        colHNumero.setCellFactory(TextFieldTableCell.forTableColumn(
                new javafx.util.converter.IntegerStringConverter()));
        colHNumero.setOnEditCommit(event -> {
            event.getRowValue().setNumero_ordem(event.getNewValue());
            tabelaHorarios.refresh();
        });

        javafx.util.StringConverter<LocalTime> timeConverter = new javafx.util.StringConverter<>() {
            @Override public String toString(LocalTime t)       { return t != null ? t.toString() : ""; }
            @Override public LocalTime fromString(String s) {
                try { return LocalTime.parse(s); } catch (Exception ex) { return null; }
            }
        };

        colHInicio.setCellFactory(TextFieldTableCell.forTableColumn(timeConverter));
        colHInicio.setOnEditCommit(event -> {
            event.getRowValue().setHora_inicio(event.getNewValue());
            tabelaHorarios.refresh();
        });

        colHFim.setCellFactory(TextFieldTableCell.forTableColumn(timeConverter));
        colHFim.setOnEditCommit(event -> {
            event.getRowValue().setHora_fim(event.getNewValue());
            tabelaHorarios.refresh();
        });
    }

    @FXML
    public void handleAplicarTemplate() {
        SemestreLetivoDAO slDao = new SemestreLetivoDAO();
        TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
        try {
            thtProcessando             = thtDao.listarPorTurno(turnoProcessando);
            idSemestreLetivoProcessando = slDao.getIdSemestreLetivo(
                    logado.getAno(), logado.getAnoSemestre());
            linhasHorarios = FXCollections.observableArrayList(thtProcessando);
            tabelaHorarios.setItems(linhasHorarios);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handlePropagarTurno() {
        if (!linhasHorarios.isEmpty()) {
            TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
            thtDao.deletarTemplateTurno("manha");
            thtDao.salvarListaTemplate(tabelaHorarios.getItems());
        }
    }

    @FXML
    public void handleAdicionarLinhaHorario() {
        TemplateHorarioTurno nova = new TemplateHorarioTurno();
        if (!linhasHorarios.isEmpty()) {
            TemplateHorarioTurno ultimo = tabelaHorarios.getItems().getLast();
            nova.setTurno(ultimo.getTurno());
            nova.setTipo(ultimo.getTipo());
            nova.setNumero_ordem(ultimo.getNumero_ordem() + 1);
            nova.setHora_inicio(ultimo.getHora_inicio());
            nova.setHora_fim(ultimo.getHora_fim());
        } else {
            nova.setTurno(turnoProcessando);
            nova.setTipo("aula");
            nova.setNumero_ordem(1);
            nova.setHora_inicio(LocalTime.now());
            nova.setHora_fim(LocalTime.now());
        }
        linhasHorarios.add(nova);
        tabelaHorarios.setItems(linhasHorarios);
    }

    @FXML
    public void handleSalvarHorarios() {
        try {
            if (idSemestreLetivoProcessando == null) {
                SemestreLetivoDAO slDao = new SemestreLetivoDAO();
                idSemestreLetivoProcessando = slDao.getIdSemestreLetivo(
                        logado.getAno(), logado.getAnoSemestre());
            }
            List<TemplateHorarioTurno> horariosLista = tabelaHorarios.getItems();
            hcDao.inserirTemplateHorarioCurso(horariosLista,
                    idCursoProcessando, idSemestreLetivoProcessando);

            alterarEstadoEdicaoDadosCurso(false);
            btnSalvarCurso.setDisable(false);
            lblProcessoSalvarCurso.setVisible(false);
            lblProcessoSalvarCurso.setManaged(false);
            carregarCursos();
        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro ao salvar", "Não foi possível salvar os horários.", Alert.AlertType.ERROR);
        }
    }

    // =========================================================================
    //  UTILITÁRIOS
    // =========================================================================

    public void alterarEstadoEdicaoDadosCurso(Boolean estado) {
        tfCursoNome.setDisable(estado);
        tbManha.setDisable(estado);
        tbNoite.setDisable(estado);
        spQtdSemestres.setDisable(estado);
        checkUsarProfessor.setDisable(estado);
        cbProfessorCurso.setDisable(!checkUsarProfessor.isSelected() || estado);
    }

    public void reiniciarValoresDadosCurso() {
        tfCursoNome.setText("");
        tbManha.setSelected(true);
        tbNoite.setSelected(false);
        spQtdSemestres.getValueFactory().setValue(1);
        checkUsarProfessor.setSelected(false);
        cbProfessorCurso.setDisable(true);
        cbProfessorCurso.setItems(null);
        idProfSelecionado = null;
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}