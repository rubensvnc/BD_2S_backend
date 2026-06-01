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
    @FXML private ComboBox<String> cbCoordenadorCurso;
    @FXML private TitledPane painelFormCurso;
    @FXML private CheckBox checkUsarCadastroCoordenador;
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

    private Map<String, Integer> mapaCoordExibicaoParaId = new HashMap<>();
    private Integer idCoordSelecionado;

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

        checkUsarCadastroCoordenador.setText("Atribuir a Coordenador");

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

        cbCoordenadorCurso.setDisable(true);

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

        // Listener na lista: exibe "Deletar Todos" só quando há mais de 1 linha
        linhasHorarios.addListener((javafx.collections.ListChangeListener<TemplateHorarioTurno>) change -> {
            boolean mostrar = linhasHorarios.size() > 1;
            btnDeletarHorarios.setManaged(mostrar);
            btnDeletarHorarios.setVisible(mostrar);
        });

        habilitarEdicaoCelulaHorarios();
    }

    // =========================================================================
    //  CARREGAMENTO DE DADOS
    // =========================================================================

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
                HorarioCursoDAO hcDao = new HorarioCursoDAO();
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

    private void carregarCoordenadoresSemCurso() {
        try {
            UsuarioDAO uDao = new UsuarioDAO();
            CursoDAO cDao  = new CursoDAO();

            List<Curso> todosCursos = cDao.listarCursos();
            List<Integer> idsOcupados = new ArrayList<>();
            for (Curso c : todosCursos) {
                if (c.getCoordenador_id() != 0) {
                    idsOcupados.add(c.getCoordenador_id());
                }
            }

            List<Usuario> todosCoords = uDao.listarTodosCoordenadores();

            mapaCoordExibicaoParaId.clear();
            ObservableList<String> opcoes = FXCollections.observableArrayList();

            for (Usuario coord : todosCoords) {
                if (!idsOcupados.contains(coord.getId_usuario())) {
                    String exibicao = coord.getNome() + " (" + coord.getEmail() + ")";
                    opcoes.add(exibicao);
                    mapaCoordExibicaoParaId.put(exibicao, coord.getId_usuario());
                }
            }

            cbCoordenadorCurso.setItems(opcoes);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =========================================================================
    //  LIMPEZA E RESET DO FORMULÁRIO
    // =========================================================================

    /**
     * Limpa completamente o formulário, deseleciona ambas as tabelas e
     * reseta todos os campos de entrada. Deve ser chamado após qualquer
     * adição, edição ou exclusão de curso.
     */
    public void limparFormulario() {
        // Deseleciona as tabelas sem disparar o listener de seleção de cursos
        tabelaCursos.getSelectionModel().clearSelection();
        tabelaHorarios.getSelectionModel().clearSelection();

        // Reseta campos do formulário
        tfCursoNome.setText("");
        tbManha.setSelected(false);
        tbNoite.setSelected(false);
        spQtdSemestres.getValueFactory().setValue(1);
        checkUsarCadastroCoordenador.setSelected(false);
        cbCoordenadorCurso.setDisable(true);
        cbCoordenadorCurso.setItems(null);
        cbCoordenadorCurso.setValue(null);

        // Limpa estado interno
        idCoordSelecionado      = null;
        idCursoProcessando      = null;
        idSemestreLetivoProcessando = null;
        turnoProcessando        = null;
        nomeCursoProcessando    = null;
        qtdSemestresProcessando = null;
        thtProcessando.clear();
        linhasHorarios.clear();
        tabelaHorarios.setItems(null);

        // Reseta botões e labels
        btnSalvarCurso.setDisable(false);
        btnSalvarCurso.setText("Salvar Curso");
        btnSalvarCurso.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        btnSalvarCurso.setOnAction(event -> handleSalvarCurso());

        btnSalvarHorario.setText("\uD83D\uDCBE Salvar Horários");
        btnSalvarHorario.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnSalvarHorario.setOnAction(event -> handleSalvarHorarios());

        lblTituloHorarios.setText("Horários — selecione um curso à esquerda");
        lblProcessoSalvarCurso.setVisible(false);
        lblProcessoSalvarCurso.setManaged(false);

        painelFormCurso.setExpanded(false);

        alterarEstadoEdicaoDadosCurso(false);
    }

    // =========================================================================
    //  AÇÕES DO FORMULÁRIO DE CURSO
    // =========================================================================

    @FXML
    public void handleNovoCurso() {
        limparFormulario();
        // Reabre o painel e prepara botões para criação
        painelFormCurso.setExpanded(true);
        tbManha.setSelected(true);
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

                if (checkUsarCadastroCoordenador.isSelected() && idCoordSelecionado != null) {
                    utDao.excluirUsuarioTipo(idCoordSelecionado, "COORD");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        limparFormulario();
    }

    @FXML
    public void usarCadastroCoordenador() {
        if (checkUsarCadastroCoordenador.isSelected()) {
            cbCoordenadorCurso.setDisable(false);
            carregarCoordenadoresSemCurso();
        } else {
            cbCoordenadorCurso.setDisable(true);
            idCoordSelecionado = null;
        }
    }

    @FXML
    public void handleSelecaoCoordenador() {
        if (checkUsarCadastroCoordenador.isSelected()) {
            String selecionado = cbCoordenadorCurso.getValue();
            if (selecionado != null) {
                idCoordSelecionado = mapaCoordExibicaoParaId.get(selecionado);
            }
        } else {
            idCoordSelecionado = null;
        }
    }

    @FXML
    public void handleSalvarCurso() {
        nomeCursoProcessando    = tfCursoNome.getText();
        qtdSemestresProcessando = spQtdSemestres.getValue();
        turnoProcessando        = tbManha.isSelected() ? "manha" : "noite";

        try {
            CursoDAO cDao = new CursoDAO();

            if (checkUsarCadastroCoordenador.isSelected() && idCoordSelecionado != null) {
                this.idCursoProcessando = cDao.inserirCursoRetornaId(
                        idCoordSelecionado, nomeCursoProcessando,
                        turnoProcessando, qtdSemestresProcessando);
            } else {
                this.idCursoProcessando = cDao.inserirCursoRetornaId(
                        nomeCursoProcessando, turnoProcessando, qtdSemestresProcessando);
            }

            exibirAlerta("Curso salvo",
                    "O curso \"" + nomeCursoProcessando + "\" foi cadastrado com sucesso.\n"
                            + "Agora adicione os horários e clique em \"Salvar Horários\".",
                    Alert.AlertType.INFORMATION);

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
            checkUsarCadastroCoordenador.setSelected(true);
            cbCoordenadorCurso.setDisable(false);
            cbCoordenadorCurso.setItems(FXCollections.observableArrayList(c.getEmail()));
            cbCoordenadorCurso.setValue(c.getEmail());
        } else {
            checkUsarCadastroCoordenador.setSelected(false);
            cbCoordenadorCurso.setDisable(true);
            cbCoordenadorCurso.setValue(null);
        }

        lblTituloHorarios.setText("Horários — Curso selecionado: " + c.getNome());
        btnSalvarCurso.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        btnSalvarCurso.setText("Editar Curso");

        AdmCursoExibicao linhaAnterior = new AdmCursoExibicao(
                c.getNome(), c.getTurno(), c.getQtd_semestres(), c.getEmail());

        btnSalvarCurso.setOnAction(event -> handleEditarCurso(linhaAnterior));

        try {
            HorarioCursoDAO hcDao   = new HorarioCursoDAO();
            CursoDAO cDao           = new CursoDAO();
            SemestreLetivoDAO slDao = new SemestreLetivoDAO();

            idCursoProcessando          = cDao.listarIdCurso(c.getNome());
            turnoProcessando            = c.getTurno();
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
            btnSalvarHorario.setText("Editar Horários");
            btnSalvarHorario.setOnAction(event -> handleEditarHorario());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleEditarCurso(AdmCursoExibicao linhaAnterior) {
        CursoDAO cDao   = new CursoDAO();
        UsuarioDAO uDao = new UsuarioDAO();

        String turno      = tbManha.isSelected() ? "manha" : "noite";
        String emailCoord = checkUsarCadastroCoordenador.isSelected()
                ? cbCoordenadorCurso.getValue()
                : null;

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

            cDao.alterarCurso(dadosAlterados, idCoord, linhaAnterior.getNome());

            exibirAlerta("Curso editado",
                    "O curso \"" + dadosAlterados.getNome() + "\" foi atualizado com sucesso.",
                    Alert.AlertType.INFORMATION);

            carregarCursos();
            limparFormulario();

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

            exibirAlerta("Horários editados",
                    "Os horários foram atualizados com sucesso.",
                    Alert.AlertType.INFORMATION);

            carregarCursos();
            limparFormulario();

        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Não é possível alterar",
                    "Estes horários não podem ser alterados ou removidos porque existem dependências vinculadas.",
                    Alert.AlertType.WARNING);
        }
    }

    public void handleDeletarCurso(AdmCursoExibicao linhaCurso) {
        String mensagem = "Deseja mesmo excluir o curso \"" + linhaCurso.getNome() + "\"?";
        if (linhaCurso.getEmail() != null) {
            mensagem += "\n\nAtenção: este curso está vinculado ao coordenador "
                    + linhaCurso.getEmail()
                    + ".\nO vínculo com o coordenador será removido junto com o curso.";
        }

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar exclusão");
        alerta.setHeaderText(null);
        alerta.showAndWait();

        if (alerta.getResult() == ButtonType.YES) {
            try {
                CursoDAO cDao = new CursoDAO();
                int idCurso   = cDao.listarIdCurso(linhaCurso.getNome());
                cDao.deletarCursoProcessando(idCurso);

                exibirAlerta("Curso excluído",
                        "O curso \"" + linhaCurso.getNome() + "\" foi excluído com sucesso.",
                        Alert.AlertType.INFORMATION);

                carregarCursos();
                limparFormulario();

            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                exibirAlerta("Não foi possível excluir",
                        "O curso \"" + linhaCurso.getNome() + "\" possui dependências "
                                + "(horários, disciplinas ou outros vínculos) que impedem a exclusão.\n"
                                + "Remova as dependências antes de excluir o curso.",
                        Alert.AlertType.ERROR);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleDeletarHorario(TemplateHorarioTurno tht) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja mesmo excluir o horário de ordem " + tht.getNumero_ordem() + "?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar exclusão");
        alerta.setHeaderText(null);
        alerta.showAndWait();

        if (alerta.getResult() == ButtonType.YES) {
            try {
                HorarioCursoDAO hcDao = new HorarioCursoDAO();
                hcDao.removerHorarioOrdemCursoSL(
                        tht.getNumero_ordem(), idCursoProcessando, idSemestreLetivoProcessando);

                exibirAlerta("Horário excluído",
                        "O horário foi removido com sucesso.",
                        Alert.AlertType.INFORMATION);

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
                "Deseja mesmo excluir todos os horários deste curso?",
                ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmar exclusão em massa");
        alerta.setHeaderText(null);
        alerta.showAndWait();

        if (alerta.getResult() == ButtonType.YES) {
            try {
                HorarioCursoDAO hcDao = new HorarioCursoDAO();
                hcDao.removerHorariosCursoSL(idCursoProcessando, idSemestreLetivoProcessando);

                exibirAlerta("Horários excluídos",
                        "Todos os horários foram removidos com sucesso.",
                        Alert.AlertType.INFORMATION);

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

        // Colunas de horário usam célula customizada com máscara automática HH:mm
        colHInicio.setCellFactory(col -> new TimeInputTableCell<>(t -> t.setHora_inicio(t.getHora_inicio())));
        colHInicio.setOnEditCommit(event -> {
            if (event.getNewValue() != null) {
                event.getRowValue().setHora_inicio(event.getNewValue());
                tabelaHorarios.refresh();
            }
        });

        colHFim.setCellFactory(col -> new TimeInputTableCell<>(t -> t.setHora_fim(t.getHora_fim())));
        colHFim.setOnEditCommit(event -> {
            if (event.getNewValue() != null) {
                event.getRowValue().setHora_fim(event.getNewValue());
                tabelaHorarios.refresh();
            }
        });
    }

    // =========================================================================
    //  CÉLULA COM MÁSCARA HH:mm
    //  TextField interno com TextFormatter que:
    //    • só aceita dígitos
    //    • insere ':' automaticamente após os 2 primeiros dígitos
    //    • limita a 5 caracteres (HH:mm)
    //    • valida hora (00-23) e minuto (00-59) ao confirmar com Enter
    // =========================================================================

    private static class TimeInputTableCell<S> extends TableCell<S, LocalTime> {

        private TextField textField;

        /** Callback opcional — não usado no commit (o onEditCommit cuida disso),
         *  mas mantido para extensibilidade futura. */
        @SuppressWarnings("unused")
        private final java.util.function.Consumer<S> setter;

        TimeInputTableCell(java.util.function.Consumer<S> setter) {
            this.setter = setter;
        }

        // ── Exibição normal (fora de edição) ──────────────────────────────────
        @Override
        protected void updateItem(LocalTime item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                if (textField != null)
                    textField.setText(format(item));
                setText(null);
                setGraphic(textField);
            } else {
                setText(format(item));
                setGraphic(null);
            }
        }

        // ── Entrar em modo de edição ──────────────────────────────────────────
        @Override
        public void startEdit() {
            if (!isEditable()
                    || !getTableView().isEditable()
                    || !getTableColumn().isEditable()) return;

            super.startEdit();
            criarTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }

        // ── Cancelar edição ───────────────────────────────────────────────────
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(format(getItem()));
            setGraphic(null);
        }

        // ── Criação do TextField com máscara ──────────────────────────────────
        private void criarTextField() {
            textField = new TextField(format(getItem()));

            // TextFormatter: máscara que insere ':' e limita a 5 chars
            javafx.scene.control.TextFormatter<String> formatter =
                    new javafx.scene.control.TextFormatter<>(change -> {
                        String novoTexto = change.getControlNewText();

                        // Remove tudo que não é dígito ou ':' antes de processar
                        String apenasDigitos = novoTexto.replace(":", "");

                        // Limita a 4 dígitos reais (HHMM)
                        if (apenasDigitos.length() > 4) return null;

                        // Reconstrói com ':' automático após 2 dígitos
                        String reconstruido;
                        if (apenasDigitos.length() <= 2) {
                            reconstruido = apenasDigitos;
                        } else {
                            reconstruido = apenasDigitos.substring(0, 2)
                                    + ":" + apenasDigitos.substring(2);
                        }

                        // Só aceita se o change não inseriu letras
                        String insertedText = change.getText();
                        if (!insertedText.isEmpty() && !insertedText.matches("[0-9:]*"))
                            return null;

                        change.setText(reconstruido);
                        change.setRange(0, change.getControlText().length());
                        // Posiciona o cursor no fim
                        change.setCaretPosition(reconstruido.length());
                        change.setAnchor(reconstruido.length());
                        return change;
                    });

            textField.setTextFormatter(formatter);
            textField.setMaxWidth(70);

            // Enter → confirmar
            textField.setOnAction(e -> commitarValor());

            // Esc → cancelar
            textField.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) cancelEdit();
            });

            // Perda de foco → confirmar automaticamente
            textField.focusedProperty().addListener((obs, eraFocado, estahFocado) -> {
                if (!estahFocado) commitarValor();
            });
        }

        private void commitarValor() {
            LocalTime parsed = parse(textField.getText());
            if (parsed != null) {
                commitEdit(parsed);
            } else {
                // Valor inválido: mantém o original e sai da edição
                cancelEdit();
            }
        }

        // ── Helpers de formato ────────────────────────────────────────────────
        private static String format(LocalTime t) {
            if (t == null) return "";
            return String.format("%02d:%02d", t.getHour(), t.getMinute());
        }

        private static LocalTime parse(String s) {
            if (s == null || s.isBlank()) return null;
            try {
                String[] parts = s.trim().split(":");
                if (parts.length < 2) return null;
                int h = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                if (h < 0 || h > 23 || m < 0 || m > 59) return null;
                return LocalTime.of(h, m);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    /**
     * Lê o turno selecionado nos ToggleButtons do formulário.
     * Valida se há seleção e se existe template salvo para ele no banco.
     * Só prossegue se ambas as condições forem satisfeitas.
     */
    @FXML
    public void handleAplicarTemplate() {
        // ── Validação 1: nenhum turno selecionado ────────────────────────────
        if (!tbManha.isSelected() && !tbNoite.isSelected()) {
            exibirAlerta("Turno não selecionado",
                    "Selecione um turno (Manhã ou Noite) antes de aplicar o template.",
                    Alert.AlertType.WARNING);
            return;
        }

        String turnoSelecionado = tbManha.isSelected() ? "manha" : "noite";

        // ── Validação 2: template não existe no banco para esse turno ────────
        TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
        List<TemplateHorarioTurno> templateEncontrado;
        try {
            templateEncontrado = thtDao.listarPorTurno(turnoSelecionado);
        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro ao carregar template",
                    "Não foi possível buscar o template do banco de dados.",
                    Alert.AlertType.ERROR);
            return;
        }

        if (templateEncontrado == null || templateEncontrado.isEmpty()) {
            String turnoLabel = "manha".equals(turnoSelecionado) ? "Manhã" : "Noite";
            exibirAlerta("Template não encontrado",
                    "Não existe nenhum template cadastrado para o turno \"" + turnoLabel + "\".\n"
                            + "Cadastre os horários manualmente ou propague um template existente.",
                    Alert.AlertType.WARNING);
            return;
        }

        // ── Aplica o template ────────────────────────────────────────────────
        try {
            SemestreLetivoDAO slDao = new SemestreLetivoDAO();
            turnoProcessando            = turnoSelecionado;
            thtProcessando              = templateEncontrado;
            idSemestreLetivoProcessando = slDao.getIdSemestreLetivo(
                    logado.getAno(), logado.getAnoSemestre());

            linhasHorarios = FXCollections.observableArrayList(thtProcessando);

            // Re-registra o listener após recriar a lista
            linhasHorarios.addListener((javafx.collections.ListChangeListener<TemplateHorarioTurno>) change -> {
                boolean mostrar = linhasHorarios.size() > 1;
                btnDeletarHorarios.setManaged(mostrar);
                btnDeletarHorarios.setVisible(mostrar);
            });

            tabelaHorarios.setItems(linhasHorarios);

            // Dispara visibilidade manualmente para o estado inicial
            boolean mostrar = linhasHorarios.size() > 1;
            btnDeletarHorarios.setManaged(mostrar);
            btnDeletarHorarios.setVisible(mostrar);

        } catch (SQLException e) {
            e.printStackTrace();
            exibirAlerta("Erro ao aplicar template",
                    "Não foi possível carregar o semestre letivo.",
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Captura os horários da tabelaHorarios e salva como template do turno
     * do curso atualmente processado, substituindo o template anterior.
     */
    @FXML
    public void handlePropagarTurno() {
        if (linhasHorarios.isEmpty() || turnoProcessando == null) {
            exibirAlerta("Nenhum horário para propagar",
                    "Adicione horários na tabela antes de propagar ao turno.",
                    Alert.AlertType.WARNING);
            return;
        }

        String turnoLabel = "manha".equals(turnoProcessando) ? "Manhã" : "Noite";
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Isso irá substituir o template do turno \"" + turnoLabel
                        + "\" pelos horários atuais. Confirmar?",
                ButtonType.YES, ButtonType.NO);
        confirmacao.setTitle("Propagar ao Turno");
        confirmacao.setHeaderText(null);
        confirmacao.showAndWait();

        if (confirmacao.getResult() != ButtonType.YES) return;

        // Garante que cada linha tem o turno correto antes de persistir
        for (TemplateHorarioTurno tht : tabelaHorarios.getItems()) {
            tht.setTurno(turnoProcessando);
        }

        TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
        thtDao.deletarTemplateTurno(turnoProcessando);
        thtDao.salvarListaTemplate(tabelaHorarios.getItems());

        exibirAlerta("Template propagado",
                "O template do turno \"" + turnoLabel + "\" foi atualizado com sucesso.",
                Alert.AlertType.INFORMATION);
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
            nova.setHora_inicio(LocalTime.now().withSecond(0).withNano(0));
            nova.setHora_fim(LocalTime.now().withSecond(0).withNano(0));
        }
        linhasHorarios.add(nova);
        tabelaHorarios.setItems(linhasHorarios);
    }

    @FXML
    public void handleSalvarHorarios() {
        HorarioCursoDAO hcDao = new HorarioCursoDAO();
        try {
            if (idSemestreLetivoProcessando == null) {
                SemestreLetivoDAO slDao = new SemestreLetivoDAO();
                idSemestreLetivoProcessando = slDao.getIdSemestreLetivo(
                        logado.getAno(), logado.getAnoSemestre());
            }
            hcDao.inserirTemplateHorarioCurso(
                    new ArrayList<>(tabelaHorarios.getItems()),
                    idCursoProcessando, idSemestreLetivoProcessando);

            exibirAlerta("Horários salvos",
                    "Os horários do curso foram salvos com sucesso.",
                    Alert.AlertType.INFORMATION);

            carregarCursos();
            limparFormulario();

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
        checkUsarCadastroCoordenador.setDisable(estado);
        cbCoordenadorCurso.setDisable(!checkUsarCadastroCoordenador.isSelected() || estado);
    }

    // reiniciarValoresDadosCurso mantido para compatibilidade, delega a limparFormulario
    public void reiniciarValoresDadosCurso() {
        limparFormulario();
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}