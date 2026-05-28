package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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
    @FXML private CheckBox checkUsarCadastroProfessor;
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

    private Map<String, Integer> mapaProfessores = new HashMap<>();
    private Integer idProfessorSelecionado;

    @FXML
    public void initialize(){

        logado.usuarioAdm();

        this.ano = logado.getAno();
        this.anoSemestre = logado.getAnoSemestre();

        logado.anoProperty().addListener(
                (obs, velho, novo) -> {
                    if (novo != null) {
                        this.ano = novo;
                        carregarCursos();
                    }
                });

        logado.anoSemestreProperty().addListener(
                (obs, velho, novo) -> {
                    if (novo != null) {
                        this.anoSemestre = novo;
                        carregarCursos();
                    }
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

                }
        );

        btnSalvarCurso.setOnAction(event -> {
            handleSalvarCurso();
        });

        cbProfessorCurso.setDisable(true);

        colCursoNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCursoTurno.setCellValueFactory(new PropertyValueFactory<>("turno"));
        colCursoSemestres.setCellValueFactory(new PropertyValueFactory<>("qtd_semestres"));
        colCursoCoordenador.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCursoAcoes.setCellFactory(new Callback<TableColumn<AdmCursoExibicao, String>, TableCell<AdmCursoExibicao, String>>() {
            @Override
            public TableCell<AdmCursoExibicao, String> call(TableColumn<AdmCursoExibicao, String> param) {
                return new TableCell<>() {
                    private final Button btnDeletar = new Button("Excluir");

                    {
                        // Estilização simples para o botão (opcional)
                        btnDeletar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

                        // Ação do Botão
                        btnDeletar.setOnAction(event -> {
                            // Pega o objeto da linha atual da tabela
                            AdmCursoExibicao cursoSelecionado = getTableView().getItems().get(getIndex());

                            // Chama o seu método já existente passando o objeto
                            handleDeletarCurso(cursoSelecionado);
                        });
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnDeletar);
                        }
                    }
                };
            }
        });

        tabelaHorarios.setEditable(true);
        colHTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colHNumero.setCellValueFactory(new PropertyValueFactory<>("numero_ordem"));
        colHInicio.setCellValueFactory(new PropertyValueFactory<>("hora_inicio"));
        colHFim.setCellValueFactory(new PropertyValueFactory<>("hora_fim"));
        colHAcao.setCellFactory(new Callback<TableColumn<TemplateHorarioTurno, Void>, TableCell<TemplateHorarioTurno, Void>>() {
            @Override
            public TableCell<TemplateHorarioTurno, Void> call(TableColumn<TemplateHorarioTurno, Void> param) {
                return new TableCell<>() {
                    private final Button btnDeletar = new Button("Excluir");

                    {
                        // Estilização simples para o botão
                        btnDeletar.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

                        // Ação do Botão
                        btnDeletar.setOnAction(event -> {
                            // Pega o objeto da linha atual da tabela
                            TemplateHorarioTurno thtSelecionado = getTableView().getItems().get(getIndex());

                            // Chama o seu método
                            handleDeletarHorario(thtSelecionado);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) { // <--- CORRIGIDO AQUI (De String para Void)
                        super.updateItem(item, empty); // <--- CORRIGIDO AQUI
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnDeletar);
                        }
                    }
                };
            }
        });

        habilitarEdicaoCelulaHorarios();
    }

    private void carregarHorarios() {
        // Só tenta carregar se houver um curso sendo processado no momento
        if (idCursoProcessando != null && idSemestreLetivoProcessando != null) {
            try {
                HorarioCursoDAO hcDao = new HorarioCursoDAO();
                List<HorarioCurso> listaHc = hcDao.listarHorariosPorCurso(idCursoProcessando, idSemestreLetivoProcessando);

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
            // Se limpar o curso selecionado, limpa a tabela de horários
            linhasHorarios.clear();
            tabelaHorarios.setItems(null);
        }
    }

    public void handleDeletarCurso(AdmCursoExibicao linhaCurso){
        Alert alertaConfirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Deseja mesmo excluir o curso " + linhaCurso.getNome() + "?", ButtonType.YES, ButtonType.NO);
        alertaConfirmacao.showAndWait();

        if (alertaConfirmacao.getResult() == ButtonType.YES) {
            try {
                CursoDAO cDao = new CursoDAO();

                int idCurso = cDao.listarIdCurso(linhaCurso.getNome());
                cDao.deletarCursoProcessando(idCurso);

                carregarCursos();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleDeletarHorario(TemplateHorarioTurno tht){
        Alert alertaConfirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Deseja mesmo excluir esse Horario?", ButtonType.YES, ButtonType.NO);
        alertaConfirmacao.showAndWait();

        if (alertaConfirmacao.getResult() == ButtonType.YES) {
            try {
                HorarioCursoDAO hcDao = new HorarioCursoDAO();

                hcDao.removerHorarioOrdemCursoSL(tht.getNumero_ordem(), idCursoProcessando, idSemestreLetivoProcessando);
                carregarHorarios();
            } catch (SQLException e) {
                e.printStackTrace();
                exibirAlerta("Erro ao deletar", "Não foi possível deletar o curso devido a dependências.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void deletarTodosHorarios(){
        Alert alertaConfirmacao = new Alert(Alert.AlertType.CONFIRMATION, "Deseja mesmo excluir todos os horários?", ButtonType.YES, ButtonType.NO);
        alertaConfirmacao.showAndWait();

        if (alertaConfirmacao.getResult() == ButtonType.YES) {
            try {
                HorarioCursoDAO hcDao = new HorarioCursoDAO();

                hcDao.removerHorariosCursoSL(idCursoProcessando, idSemestreLetivoProcessando);
                carregarHorarios();
            } catch (SQLException e) {
                e.printStackTrace();
                exibirAlerta("Erro ao deletar", "Não foi possível deletar o curso devido a dependências.", Alert.AlertType.ERROR);
            }
        }
    }

    public void habilitarEdicaoCelulaHorarios(){
        colHTipo.setCellFactory(ComboBoxTableCell.forTableColumn());
        colHTipo.setCellFactory(ComboBoxTableCell.forTableColumn("aula", "intervalo"));
        colHTipo.setOnEditCommit(event -> {
            event.getRowValue().setTipo(event.getNewValue());
            tabelaHorarios.refresh();
        });

        colHNumero.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        colHNumero.setOnEditCommit(event -> {
            event.getRowValue().setNumero_ordem(event.getNewValue());
            tabelaHorarios.refresh();
        });

        javafx.util.StringConverter<java.time.LocalTime> timeConverter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(java.time.LocalTime time) {
                return (time != null) ? time.toString() : "";
            }
            @Override
            public java.time.LocalTime fromString(String string) {
                try {
                    return java.time.LocalTime.parse(string);
                } catch (Exception ex) {
                    return null;
                }
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

    public void preencherDadosEdicaoCurso(AdmCursoExibicao c){
        System.out.println(c.getNome());
        painelFormCurso.setExpanded(true);

        tfCursoNome.setText(c.getNome());
        if (c.getTurno().equals("manha")){
            tbManha.setSelected(true);
            tbNoite.setSelected(false);
        } else {
            tbNoite.setSelected(true);
            tbManha.setSelected(false);
        }

        spQtdSemestres.getValueFactory().setValue(c.getQtd_semestres());

        if (c.getEmail() != null){
            checkUsarCadastroProfessor.setSelected(true);
            cbProfessorCurso.setDisable(false);
            cbProfessorCurso.setValue(c.getEmail());
        } else {
            checkUsarCadastroProfessor.setSelected(false);
            cbProfessorCurso.setDisable(true);
            cbProfessorCurso.setValue(c.getEmail());
        }

        lblTituloHorarios.setText("Horários — Curso selecionado: "+c.getNome());
        btnSalvarCurso.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        btnSalvarCurso.setText("Editar Curso");

        AdmCursoExibicao linhaAnterior = new AdmCursoExibicao(
                c.getNome(),
                c.getTurno(),
                c.getQtd_semestres(),
                c.getEmail()
        );

        btnSalvarCurso.setOnAction(event -> {
            handleEditarCurso(linhaAnterior);
        });

        //---- RELACIONADO AOS HORARIOS:
        try{
            HorarioCursoDAO hcDao = new HorarioCursoDAO();
            CursoDAO cDao = new CursoDAO();
            SemestreLetivoDAO slDao = new SemestreLetivoDAO();

            idCursoProcessando = cDao.listarIdCurso(c.getNome());
            idSemestreLetivoProcessando = slDao.getIdSemestreLetivo
                    (logado.getAno(), logado.getAnoSemestre());

            List<HorarioCurso> listaHc = hcDao.listarHorariosPorCurso(
                    idCursoProcessando, idSemestreLetivoProcessando);
            for (HorarioCurso hc: listaHc){
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


            btnSalvarHorario.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
            btnSalvarHorario.setText("Editar Horarios");

            btnSalvarHorario.setOnAction(event -> {
                handleEditarHorario();
            });

            btnDeletarHorarios.setManaged(true);
            btnDeletarHorarios.setVisible(true);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void handleEditarCurso(AdmCursoExibicao linhaAnterior){
        CursoDAO cDao = new CursoDAO();
        UsuarioDAO uDao = new UsuarioDAO();

        String turno;
        String emailProf = null;
        Integer idCoord = null;

        if (tbManha.isSelected()){
            turno = "manha";
        } else {
            turno = "noite";
        }

        if (checkUsarCadastroProfessor.isSelected()){
            emailProf = cbProfessorCurso.getValue();
        }


        AdmCursoExibicao dadosAlterados = new AdmCursoExibicao(
                tfCursoNome.getText(),
                turno,
                spQtdSemestres.getValueFactory().getValue(),
                emailProf
        );

        try {
            if (emailProf != null){
                idCoord = uDao.buscarUsuarioPorEmailUnico(
                        dadosAlterados.getEmail()).getId_usuario();
            }

            cDao.alterarCurso(dadosAlterados, idCoord, linhaAnterior.getNome());
            painelFormCurso.setExpanded(false);
            carregarCursos();

        } catch (SQLException e){
            e.printStackTrace();
        }

    }

    public void handleEditarHorario(){
        List<TemplateHorarioTurno> listaHorariosAlterados = tabelaHorarios.getItems();

        HorarioCursoDAO hcDao = new HorarioCursoDAO();

        try{
            hcDao.removerHorariosCursoSL(idCursoProcessando, idSemestreLetivoProcessando);
            hcDao.inserirTemplateHorarioCurso(
                    listaHorariosAlterados, idCursoProcessando, idSemestreLetivoProcessando
            );
        } catch (SQLException e){
            e.printStackTrace();
            exibirAlerta(
                    "Não é possível alterar",
                    "Estes horários não podem ser alterados ou removidos porque existem outros registros (dependências) vinculados a eles.",
                    Alert.AlertType.WARNING
            );
        }

    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }


    @FXML
    public void handleNovoCurso() {
        reiniciarValoresDadosCurso();
        tabelaHorarios.setItems(null);
        painelFormCurso.setExpanded(true);

        btnSalvarCurso.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        btnSalvarCurso.setText("Salvar Curso");
        btnSalvarCurso.setOnAction(event -> {
            handleSalvarCurso();
        });

        btnSalvarHorario.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnSalvarHorario.setText("\uD83D\uDCBE Salvar Horários");

        btnSalvarHorario.setOnAction(event -> {
            handleSalvarHorarios();
        });

        btnDeletarHorarios.setManaged(false);
        btnDeletarHorarios.setVisible(false);
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
        if (this.ano != null && this.anoSemestre != null) {
            try {
                CursoDAO cDao = new CursoDAO();
                List<AdmCursoExibicao> lista = cDao.listarCursosDTO(this.ano, this.anoSemestre);
                ObservableList linhasCurso = FXCollections.observableArrayList(lista);
                tabelaCursos.setItems(linhasCurso);
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    public void reiniciarValoresDadosCurso(){
        tfCursoNome.setText("");
        tbManha.setSelected(true);
        tbNoite.setSelected(false);
        spQtdSemestres.getValueFactory().setValue(1);
        checkUsarCadastroProfessor.setSelected(false);
        cbProfessorCurso.setDisable(true);
        cbProfessorCurso.setItems(null);
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
        if (!linhasHorarios.isEmpty()){
            List<TemplateHorarioTurno> novoTemplate = tabelaHorarios.getItems();
            TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
            thtDao.deletarTemplateTurno("manha");
            System.out.println("DELETADO COM SUCESSO");
            thtDao.salvarListaTemplate(novoTemplate);
            System.out.println("NOVO TEMPLATE SALVO NO BANCO");
        }
    }

    @FXML
    public void handleAdicionarLinhaHorario() {
        if (!linhasHorarios.isEmpty()){
            TemplateHorarioTurno ultimo_item = tabelaHorarios.getItems().getLast();
            TemplateHorarioTurno nova_linha = new TemplateHorarioTurno();
            nova_linha.setTurno(ultimo_item.getTurno());
            nova_linha.setTipo(ultimo_item.getTipo());
            nova_linha.setNumero_ordem(ultimo_item.getNumero_ordem()+1);
            nova_linha.setHora_inicio(ultimo_item.getHora_inicio());
            nova_linha.setHora_fim(ultimo_item.getHora_fim());

            linhasHorarios.add(nova_linha);
            tabelaHorarios.setItems(linhasHorarios);
        }
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
