package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.LocalDateStringConverter;
import org.example.demo3.DatabaseConnection;
import org.example.demo3.UsuarioAtual;
import org.example.demo3.dao.*;
import org.example.demo3.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdmCalendarioBloqueiosController {

    @FXML private DatePicker dpInicioSemestre;
    @FXML private DatePicker dpFimSemestre;
    @FXML private DatePicker dpTcc;
    @FXML private DatePicker dpFeira;
    @FXML private TableView<Sprint> tabelaSprints;
    @FXML private TableColumn<Sprint, Integer> colSprintNum;
    @FXML private TableColumn<Sprint, LocalDate> colSprintInicio;
    @FXML private TableColumn<Sprint, LocalDate> colSprintReview;
    @FXML private Label lblFeedbackCalendario;
    @FXML private TabPane tabPane;
    @FXML private Tab tabBloqueios;
    @FXML private ComboBox<String> cbMes;
    @FXML private GridPane gridDias;
    @FXML private VBox boxConfigCancelamento;
    @FXML private ComboBox<String> cbTurno;
    @FXML private TextField tfMotivoCancelamento;
    @FXML private Label lblFeedbackCancelamento;
    @FXML private ListView<CancelamentoAdm> listCancelamentos;
    @FXML private Button btnDeletar;
    @FXML private FlowPane flowHorarios;
    @FXML private VBox painelHorarios;
    @FXML private CheckBox checkFeriado;
    @FXML private Button btnCancelar;
    @FXML private Button btnDesfazer;

    private final SprintDAO sprintDAO = new SprintDAO();
    private final CancelamentoAdmDAO cancelamentoDAO = new CancelamentoAdmDAO();
    private final CancelamentoAdmHorarioDAO cancelamentoAdmHDAO = new CancelamentoAdmHorarioDAO();
    private final DataBloqueadaDAO databDao = new DataBloqueadaDAO();
    private final SemestreLetivoDAO slDao = new SemestreLetivoDAO();
    private final TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
    private final HorarioCursoDAO hcDao = new HorarioCursoDAO();

    private final UsuarioAtual logado = UsuarioAtual.getInstancia();

    private final ObservableList<Sprint> listaSprintsFX = FXCollections.observableArrayList();
    private final ObservableList<CancelamentoAdm> listaCancelamentosFX = FXCollections.observableArrayList();
    private LocalDate dataSelecionadaNoCalendario;

    private SemestreLetivo slAtual;

    private List<Button> listaBtnDia = new ArrayList<>();
    private List<DataBloqueada> linhasDataBloqueadaRecuperadaBanco = new ArrayList<>();
    private List<CancelamentoAdm> linhasCancelamentoAdmRecuperadoBanco = new ArrayList<>();
    private List<LocalTime> listCheckHorariosSelecionados = new ArrayList<>();

    private Map<String, List<TemplateHorarioTurno>> mapaTurnoListTHT = new LinkedHashMap<>();
    private Map<LocalDate, String> mapaBotaoPressionadoEstilo = new LinkedHashMap<>();
    private Month mesSelecionadoTipoMonth;
    private Boolean reacaoEmCadeiaBtns = false;
    private int anoSelecionado;
    private int anoSemestreSelecionado;
    private int idSemestreAtual;
    private int ID_ADM_LOGADO;

    private final String laranjaCheio = "-fx-border-color: transparent; -fx-background-color: #FFA500;";
    private final String laranjaBorda = "-fx-border-color: #FFA500; -fx-background-color: -fx-control-inner-background;";
    private final String vermelhoCheio = "-fx-border-color: transparent; -fx-background-color: #FF0000;";
    private final String vermelhoBorda = "-fx-border-color: #FF0000; -fx-background-color: -fx-control-inner-background;";
    private final String amareloCheio = "-fx-border-color: transparent; -fx-background-color: #FFFF00;";
    private final String corDefault = "-fx-border-color: transparent; -fx-background-color: #D3D3D3;";
    private String corBotaoSelecionado;


    @FXML
    public void initialize() {
        //REMOVER DEPOIS:
        logado.setId_usuario(1);
        logado.setAno(2026);
        logado.setAnoSemestre(1);


        anoSelecionado = logado.getAno();
        anoSemestreSelecionado = logado.getAnoSemestre();
        atualizarSemestreAtual(anoSelecionado, anoSemestreSelecionado);

        logado.anoProperty().addListener((obs, oldVal, newVal) ->{
            if (newVal != null) {
                System.out.println("Ano: "+newVal);
                this.anoSelecionado = newVal;
                atualizarSemestreAtual(anoSelecionado, anoSemestreSelecionado);
            };
        });

        logado.anoSemestreProperty().addListener((obs, oldVal, newVal) ->{
            if (newVal != null) {
                System.out.println("AnoSemestre: "+newVal);
                this.anoSemestreSelecionado = newVal;
                atualizarSemestreAtual(anoSelecionado, anoSemestreSelecionado);
            };
        });

        ID_ADM_LOGADO = logado.getId_usuario();
        configurarTabelaSprints();

        int anoFiltro = (logado.getAno() != null) ? logado.getAno() : LocalDate.now().getYear();
        int numeroSemestre = (logado.getAnoSemestre() != null) ? logado.getAnoSemestre() : 1;

        carregarDadosPorAnoESemestre(anoFiltro, numeroSemestre);

        //CARREGAR AO INICIAR ABA CANCELAMENTOS:
        linhasDataBloqueadaRecuperadaBanco = databDao.listarDataBloqueadaPorSemestre(idSemestreAtual);
        linhasCancelamentoAdmRecuperadoBanco = cancelamentoDAO.recuperarTodosCancelamentoAdm(idSemestreAtual);

        recuperarHorariosTurnos();
        carregarMesesCbMes();
        configurarDadosConfigCancelamento();

    }

    // ==========================================
    // ----------- METODOS GERAIS ---------------
    // ==========================================

    public void atualizarSemestreAtual(int ano, int anoSemestre){
        try {
            idSemestreAtual = slDao.getIdSemestreLetivo(ano, anoSemestre);
            slAtual = slDao.listarSLPorId(idSemestreAtual);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    //╔═══════════════════════════════════════════════════════════════════╗
    //║             METODOS DA ABA CALENDARIO DO SEMESTRE                 ║
    //╚═══════════════════════════════════════════════════════════════════╝

    public void carregarDadosPorAnoESemestre(int anoFiltro, int numeroSemestre) {
        listaSprintsFX.clear();


        try {
            Integer id = slDao.getIdSemestreLetivo(anoFiltro, numeroSemestre);
            this.idSemestreAtual = (id != null) ? id : -1;
        } catch (SQLException e) {
            this.idSemestreAtual = -1;
        }

        buscarEPreencherDatasMacro(this.idSemestreAtual, anoFiltro, numeroSemestre);

        var sprintsBanco = sprintDAO.buscarSprintsPorSemestre(this.idSemestreAtual);

        if (sprintsBanco.isEmpty()) {
            int mesBase = (numeroSemestre == 2) ? 8 : 2;
            for (int i = 1; i <= 3; i++) {
                Sprint s = new Sprint();
                s.setNumero(i);
                s.setSemestre_letivo_id(this.idSemestreAtual);
                s.setData_inicio(LocalDate.of(anoFiltro, mesBase, 10).plusMonths(i - 1));
                s.setData_fim(LocalDate.of(anoFiltro, mesBase, 10).plusMonths(i - 1).plusDays(25));
                s.setData_review(LocalDate.of(anoFiltro, mesBase, 10).plusMonths(i - 1).plusDays(27));
                listaSprintsFX.add(s);
            }
        } else {
            listaSprintsFX.addAll(sprintsBanco);
        }
        tabelaSprints.setItems(listaSprintsFX);
    }


    private void buscarEPreencherDatasMacro(int idSemestre, int anoFiltro, int numeroSemestre) {
        SemestreLetivo sl = slDao.listarSLPorId(idSemestre);

        if (sl != null && sl.getData_inicio() != null) {
            dpInicioSemestre.setValue(sl.getData_inicio());
            dpFimSemestre.setValue(sl.getData_fim());
            dpTcc.setValue(sl.getData_tg());
            dpFeira.setValue(sl.getData_feira());
            return;
        }

        int mesInicio = (numeroSemestre == 2) ? 8 : 2;
        int mesFim = (numeroSemestre == 2) ? 12 : 6;
        dpInicioSemestre.setValue(LocalDate.of(anoFiltro, mesInicio, 1));
        dpFimSemestre.setValue(LocalDate.of(anoFiltro, mesFim, 30));
        dpTcc.setValue(LocalDate.of(anoFiltro, mesFim, 15));
        dpFeira.setValue(LocalDate.of(anoFiltro, mesFim, 20));
    }

    private void configurarTabelaSprints() {
        tabelaSprints.setEditable(true);
        colSprintNum.setCellValueFactory(new PropertyValueFactory<>("numero"));

        colSprintInicio.setCellValueFactory(new PropertyValueFactory<>("data_inicio"));
        colSprintInicio.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        colSprintInicio.setOnEditCommit(event -> {
            Sprint sprint = event.getRowValue();
            if (sprint != null) {
                sprint.setData_inicio(event.getNewValue());
                tabelaSprints.refresh();
            }
        });

        colSprintReview.setCellValueFactory(new PropertyValueFactory<>("data_review"));
        colSprintReview.setCellFactory(TextFieldTableCell.forTableColumn(new LocalDateStringConverter()));
        colSprintReview.setOnEditCommit(event -> {
            Sprint sprint = event.getRowValue();
            if (sprint != null) {
                sprint.setData_review(event.getNewValue());
                tabelaSprints.refresh();
            }
        });
    }

    public void abrirAbaBloqueios() {
        tabPane.getSelectionModel().select(tabBloqueios);
    }


    @FXML
    public void handleSalvarCalendario() {
        if (dpInicioSemestre.getValue() == null || dpFimSemestre.getValue() == null ||
                dpFeira.getValue() == null || dpTcc.getValue() == null) {
            exibirFeedback(lblFeedbackCalendario, "Erro: Datas macro do semestre e marcos obrigatorios sao obrigatorios!", true);
            return;
        }

        LocalDate inicio = dpInicioSemestre.getValue();
        LocalDate fim = dpFimSemestre.getValue();
        LocalDate feira = dpFeira.getValue();
        LocalDate tcc = dpTcc.getValue();

        if (inicio.isAfter(fim)) {
            exibirFeedback(lblFeedbackCalendario, "Erro: Data de inicio nao pode ser apos o fim do semestre!", true);
            return;
        }

        if (feira.isBefore(inicio) || feira.isAfter(fim) || tcc.isBefore(inicio) || tcc.isAfter(fim)) {
            exibirFeedback(lblFeedbackCalendario, "Erro: As datas da feira e do TG devem estar contidas dentro do intervalo do semestre!", true);
            return;
        }

        if (listaSprintsFX.size() != 3) {
            exibirFeedback(lblFeedbackCalendario, "Erro: O sistema exige a configuracao de exatamente 3 sprints obrigatorias!", true);
            return;
        }

        for (Sprint sprint : listaSprintsFX) {
            sprint.setSemestre_letivo_id(this.idSemestreAtual);
            if (sprint.getData_inicio() == null || sprint.getData_review() == null) {
                exibirFeedback(lblFeedbackCalendario, "Erro: Todas as datas das 3 sprints devem estar preenchidas!", true);
                return;
            }
            if (sprint.getData_inicio().isBefore(inicio) || sprint.getData_review().isAfter(fim)) {
                exibirFeedback(lblFeedbackCalendario, "Erro: Os prazos das sprints nao podem extrapolar os limites do semestre!", true);
                return;
            }
            if (sprint.getData_inicio().isAfter(sprint.getData_review())) {
                exibirFeedback(lblFeedbackCalendario, "Erro: Data de inicio da Sprint " + sprint.getNumero() + " nao pode ser posterior a Review!", true);
                return;
            }
            if (sprint.getData_fim() == null) {
                sprint.setData_fim(sprint.getData_review());
            }
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int anoSemestre = inicio.getMonthValue() >= 7 ? 2 : 1;


                SemestreLetivo sl = new SemestreLetivo();
                sl.setCriado_por_adm_id(ID_ADM_LOGADO);
                sl.setAno(inicio.getYear());
                sl.setNumero_semestre(anoSemestre);
                sl.setData_inicio(inicio);
                sl.setData_fim(fim);
                sl.setData_tg(tcc);
                sl.setData_feira(feira);

                this.idSemestreAtual = slDao.salvarOuAtualizarSemestre(connection, sl);

                for (Sprint sprint : listaSprintsFX) {
                    sprint.setSemestre_letivo_id(this.idSemestreAtual);
                }

                sprintDAO.salvarOuAtualizarSprintsEmLote(connection, listaSprintsFX);

                connection.commit();

                logado.setAno(inicio.getYear());
                logado.setAnoSemestre(anoSemestre);

                carregarDadosPorAnoESemestre(inicio.getYear(), anoSemestre);
                exibirFeedback(lblFeedbackCalendario, "Calendario e as 3 Sprints salvos com sucesso!", false);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            exibirFeedback(lblFeedbackCalendario, "Falha ao salvar lote do calendario: " + e.getMessage(), true);
        }
    }

    //╔═══════════════════════════════════════════════════════════════════╗
    //║          METODOS DA ABA BLOQUEIOS E CANCELAMENTOS                 ║
    //╚═══════════════════════════════════════════════════════════════════╝

    // ------------- CRUD:

    public List<DataBloqueada> prepararListaDataBloqueada(String motivo){
        List<DataBloqueada> datasSelecionadas = new ArrayList<>();
        for (LocalDate data: mapaBotaoPressionadoEstilo.keySet()){
            DataBloqueada dataB = new DataBloqueada();
            dataB.setAdmId(logado.getId_usuario());
            dataB.setData(data);
            dataB.setMotivo(motivo);
            dataB.setSemestreLetivoId(idSemestreAtual);

            datasSelecionadas.add(dataB);
        }
        return datasSelecionadas;
    }

    public List<CancelamentoAdm> prepararListaCancelamentoDiaInteiro(String motivo){
        List<CancelamentoAdm> cancelamentosSelecionados = new ArrayList<>();

        for (LocalDate data: mapaBotaoPressionadoEstilo.keySet()){
            CancelamentoAdm cadm = new CancelamentoAdm();
            cadm.setAdm_id(logado.getId_usuario());
            cadm.setMotivo(motivo);
            cadm.setData(data);
            cadm.setSemestre_letivo_id(idSemestreAtual);
            cadm.setDia_inteiro(true);
            cadm.setCriado_em(LocalDate.now());

            cancelamentosSelecionados.add(cadm);
        }
        return cancelamentosSelecionados;
    }

    public List<CancelamentoAdm> prepararListaCancelamentoHorario(String motivo, String turno){
        List<CancelamentoAdm> cancelamentosSelecionados = new ArrayList<>();

        for (LocalDate data: mapaBotaoPressionadoEstilo.keySet()){
            CancelamentoAdm cadm = new CancelamentoAdm();
            cadm.setAdm_id(logado.getId_usuario());
            cadm.setMotivo(motivo);
            cadm.setData(data);
            cadm.setSemestre_letivo_id(idSemestreAtual);
            cadm.setTurno(turno);
            cadm.setDia_inteiro(false);
            cadm.setCriado_em(LocalDate.now());

            cancelamentosSelecionados.add(cadm);
        }
        return cancelamentosSelecionados;
    }

    public void adicionarFeriadosBanco(String motivo){
        List<DataBloqueada> datasSelecionadas = prepararListaDataBloqueada(motivo);
        try {

            if (checkFeriado.isSelected()){
                databDao.salvarEmLote(datasSelecionadas);
            }

            linhasDataBloqueadaRecuperadaBanco = databDao.listarDataBloqueadaPorSemestre(idSemestreAtual);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void adicionarCancelamentosDiaInteiroBanco(String motivo){
        List<CancelamentoAdm> cancelamentosSelecionados = prepararListaCancelamentoDiaInteiro(motivo);

        try {
            cancelamentoDAO.salvarEmLote(cancelamentosSelecionados);

            linhasCancelamentoAdmRecuperadoBanco = cancelamentoDAO.recuperarTodosCancelamentoAdm(idSemestreAtual);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void adicionarCancelamentoHorariosBanco(String motivo, String turno){
        List<CancelamentoAdm> cancelamentosSelecionados = prepararListaCancelamentoHorario(motivo, turno);
        try {
            cancelamentoDAO.salvarEmLote(cancelamentosSelecionados);

            linhasCancelamentoAdmRecuperadoBanco = cancelamentoDAO.recuperarTodosCancelamentoAdm(idSemestreAtual);
        } catch (SQLException e){
            e.printStackTrace();
        }


        List<HorarioCurso> listHorarioCursoTurno = hcDao.listarHorarioCursoPorHoraInicio(idSemestreAtual,
                listCheckHorariosSelecionados);
        List<CancelamentoAdm> listCancelamentoAdmSelecionados = linhasCancelamentoAdmRecuperadoBanco.stream()
                .filter(lambdaCadm -> mapaBotaoPressionadoEstilo.containsKey(lambdaCadm.getData()))
                .toList();

        List<CancelamentoAdmHorario> listaCadmH = new ArrayList<>();
        for (CancelamentoAdm cadm: listCancelamentoAdmSelecionados){
            for (HorarioCurso hc: listHorarioCursoTurno){
                CancelamentoAdmHorario cAdmH = new CancelamentoAdmHorario();
                cAdmH.setCancelamento_adm_id(cadm.getId_cancelamento_adm());
                cAdmH.setHorario_curso_id(hc.getId_horario_curso());

                listaCadmH.add(cAdmH);
            }
        }

        cancelamentoAdmHDAO.salvarEmLote(listaCadmH);
    }

    public void atualizarValoresFeriadoBanco(String motivo){
        try{
            List<DataBloqueada> datasSelecionadas = prepararListaDataBloqueada(motivo);
            databDao.atualizarEmLote(datasSelecionadas);
            linhasDataBloqueadaRecuperadaBanco = databDao.listarDataBloqueadaPorSemestre(idSemestreAtual);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void atualizarValoresCancelamentoDiaInteiroBanco(String motivo){
        List<CancelamentoAdm> cancelamentosSelecionados = prepararListaCancelamentoDiaInteiro(motivo);

        try{
            cancelamentoDAO.atualizarEmLote(cancelamentosSelecionados);
            linhasCancelamentoAdmRecuperadoBanco = cancelamentoDAO.recuperarTodosCancelamentoAdm(idSemestreAtual);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void atualizarValoresCancelamentoComHorarios(String motivo, String turno){
        try {
            // Atualiza motivo/turno nas linhas de cancelamento_adm
            List<CancelamentoAdm> cancelamentosAtualizados = prepararListaCancelamentoHorario(motivo, turno);
            cancelamentoDAO.atualizarEmLote(cancelamentosAtualizados);

            linhasCancelamentoAdmRecuperadoBanco = cancelamentoDAO.recuperarTodosCancelamentoAdm(idSemestreAtual);

            // Pega os cadms (com id real do banco) que correspondem às datas selecionadas
            List<CancelamentoAdm> listCadmSelecionados = linhasCancelamentoAdmRecuperadoBanco.stream()
                    .filter(c -> mapaBotaoPressionadoEstilo.containsKey(c.getData()))
                    .toList();

            // Horários atualmente salvos no banco para esses cadms
            List<CancelamentoAdmHorario> listCadmHAtual =
                    cancelamentoAdmHDAO.listarHorariosDeCancelamentos(listCadmSelecionados);

            // Horários que DEVERIAM estar selecionados (estado atual da UI)
            List<HorarioCurso> listHorarioCursoDesejado =
                    hcDao.listarHorarioCursoPorHoraInicio(idSemestreAtual, listCheckHorariosSelecionados);
            Set<Integer> idsDesejados = listHorarioCursoDesejado.stream()
                    .map(HorarioCurso::getId_horario_curso)
                    .collect(Collectors.toSet());

            List<CancelamentoAdmHorario> listaParaInserir = new ArrayList<>();
            List<CancelamentoAdmHorario> listaParaRemover = new ArrayList<>();

            // Para cada cadm, calcula o diff entre o que está no banco e o que deveria estar
            for (CancelamentoAdm cadm : listCadmSelecionados){
                Integer idCadm = cadm.getId_cancelamento_adm();

                Set<Integer> idsAtuais = listCadmHAtual.stream()
                        .filter(cah -> idCadm.equals(cah.getCancelamento_adm_id()))
                        .map(CancelamentoAdmHorario::getHorario_curso_id)
                        .collect(Collectors.toSet());

                // Está em "desejado" mas não em "atual" -> precisa INSERIR
                for (Integer idHorario : idsDesejados){
                    if (!idsAtuais.contains(idHorario)){
                        CancelamentoAdmHorario novo = new CancelamentoAdmHorario();
                        novo.setCancelamento_adm_id(idCadm);
                        novo.setHorario_curso_id(idHorario);
                        listaParaInserir.add(novo);
                    }
                }

                // Está em "atual" mas não em "desejado" -> precisa REMOVER
                for (Integer idHorario : idsAtuais){
                    if (!idsDesejados.contains(idHorario)){
                        CancelamentoAdmHorario remover = new CancelamentoAdmHorario();
                        remover.setCancelamento_adm_id(idCadm);
                        remover.setHorario_curso_id(idHorario);
                        listaParaRemover.add(remover);
                    }
                }
            }

            if (!listaParaInserir.isEmpty()){
                cancelamentoAdmHDAO.salvarEmLote(listaParaInserir);
            }
            if (!listaParaRemover.isEmpty()){
                cancelamentoAdmHDAO.excluirEmLote(listaParaRemover);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }


    public void excluirFeriadoBanco(String motivo){
        try{
            List<DataBloqueada> datasSelecionadas = prepararListaDataBloqueada(motivo);
            databDao.excluirEmLote(datasSelecionadas);
            linhasDataBloqueadaRecuperadaBanco = databDao.listarDataBloqueadaPorSemestre(idSemestreAtual);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void excluirCancelamentoDiaInteiroBanco(String motivo){
        List<CancelamentoAdm> cancelamentosSelecionados = prepararListaCancelamentoDiaInteiro(motivo);
        try{
            cancelamentoDAO.excluirEmLote(cancelamentosSelecionados);
            linhasCancelamentoAdmRecuperadoBanco = cancelamentoDAO.recuperarTodosCancelamentoAdm(idSemestreAtual);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void configurarDadosConfigCancelamento(){
        cbTurno.setItems(FXCollections.observableArrayList("Dia inteiro", "manha", "noite", "Ambos turnos"));
        cbTurno.setValue("Dia inteiro");
        checkFeriado.setSelected(false);
    }

    public void verificarPodeAbrirConfigCancelamento(){
        if (!mapaBotaoPressionadoEstilo.isEmpty()){
            boxConfigCancelamento.setManaged(true);
            boxConfigCancelamento.setVisible(true);
        } else {
            boxConfigCancelamento.setManaged(false);
            boxConfigCancelamento.setVisible(false);
        }
    }

    public void resetarDadosConfigCancelamento(){
        linhasDataBloqueadaRecuperadaBanco = databDao.listarDataBloqueadaPorSemestre(idSemestreAtual);
        linhasCancelamentoAdmRecuperadoBanco = cancelamentoDAO.recuperarTodosCancelamentoAdm(idSemestreAtual);

        checkFeriado.setSelected(false);
        cbTurno.setDisable(false);
        resetarDadosInterfaceConfigCancelamento();
        tfMotivoCancelamento.setText("");
        btnCancelar.setText("Cancelar Datas");
        btnCancelar.setOnAction(event -> {
            handleCancelarDatas();
        });
    }

    public void resetarDadosInterfaceConfigCancelamento(){

        listCheckHorariosSelecionados.clear();

        flowHorarios.getChildren().clear();
        painelHorarios.setManaged(false);
        painelHorarios.setVisible(false);
        cbTurno.setValue("Dia inteiro");
    }

    public void preencherDadosConfigCancelamentoFeriado(String motivo){
        tfMotivoCancelamento.setText(motivo);
        checkFeriado.setSelected(true);
        handleFeriados();
        btnCancelar.setText("Editar feriado");
        btnCancelar.setOnAction(event -> {
            if (checkFeriado.isSelected()) {
                atualizarValoresFeriadoBanco(tfMotivoCancelamento.getText());
                resetarDadosConfigCancelamento();
                handleDesfazerSelecao();
            }
        });
    }


    public void preencherDadosConfigCancelamentoDiaInteiro(String motivo){
        tfMotivoCancelamento.setText(motivo);
        checkFeriado.setSelected(false);
        cbTurno.setValue("Dia inteiro");
        btnCancelar.setText("Editar cancelamento");
        btnCancelar.setOnAction(event -> {
            atualizarValoresCancelamentoDiaInteiroBanco(tfMotivoCancelamento.getText());
            resetarDadosConfigCancelamento();
            handleDesfazerSelecao();
        });
    }

    public void preencherDadosConfigCancelamentoComHorarios(String motivo, String turno){
        tfMotivoCancelamento.setText(motivo);
        checkFeriado.setSelected(false);

        String valorCombo = (turno == null) ? "Ambos turnos" : turno;
        cbTurno.setValue(valorCombo);

        painelHorarios.setManaged(true);
        painelHorarios.setVisible(true);
        gerarCheckboxHorarios(valorCombo);

        btnCancelar.setText("Editar cancelamento");
        btnCancelar.setOnAction(event -> {
            String turnoParaSalvar = "Ambos turnos".equals(cbTurno.getValue()) ? null : cbTurno.getValue();
            atualizarValoresCancelamentoComHorarios(tfMotivoCancelamento.getText(), turnoParaSalvar);
            resetarDadosConfigCancelamento();
            handleDesfazerSelecao();
        });
    }

    public void atualizarGridBtnDia(){
        for (Button btnDia: listaBtnDia){
            LocalDate dataDesteBotao = LocalDate.of
                    (anoSelecionado, mesSelecionadoTipoMonth, Integer.parseInt(btnDia.getText()));
            if (mapaBotaoPressionadoEstilo.containsKey(dataDesteBotao)){
                btnDia.setStyle(mapaBotaoPressionadoEstilo.get(dataDesteBotao));
            }
        }
    }

    public void bloquearBtnDiasNaoComuns(){
        if (!mapaBotaoPressionadoEstilo.isEmpty()) {
            for (Button btnDia : listaBtnDia) {
                LocalDate dataDesteBotao = LocalDate.of
                        (anoSelecionado, mesSelecionadoTipoMonth, Integer.parseInt(btnDia.getText()));
                btnDia.setDisable(!mapaBotaoPressionadoEstilo.containsKey(dataDesteBotao));
            }
        } else{
            for (Button btnDia : listaBtnDia) {
                btnDia.setDisable(false);
            }
            resetarDadosConfigCancelamento();
        }
        atualizarGridBtnDia();
    }

    public void bloquearBtnDiasNaoAmarelos(){
        if (!mapaBotaoPressionadoEstilo.isEmpty()){
            for (Button btnDia: listaBtnDia){
                String estilo = btnDia.getStyle();

                if (estilo.contains("FFFF00") || estilo.contains("D3D3D3")){
                    btnDia.setDisable(false);
                } else{
                    btnDia.setDisable(true);
                }
            }
        } else {
            for (Button btnDia : listaBtnDia) {
                btnDia.setDisable(false);
            }
            resetarDadosConfigCancelamento();
        }
        atualizarGridBtnDia();
    }

    public void selecionarBotoesFeriadoMotivoIgual(LocalDate dataBtnPressionado){
        System.out.println("ENTROU EM 'selecionarBotoesFeriadoMotivoIgual'");
        try{
            String motivo = databDao.recuperarMotivoData(dataBtnPressionado, idSemestreAtual);
            List<LocalDate> datasMotivoIgual = databDao.listarDatasMotivoComumSL(idSemestreAtual, motivo);

            for (LocalDate data: datasMotivoIgual){
                mapaBotaoPressionadoEstilo.put(data, laranjaBorda);
            }

            preencherDadosConfigCancelamentoFeriado(motivo);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void selecionarBotoesCancelamentoMotivoHorariosIgual(CancelamentoAdm cadmPrimeiroSelecionado){
        String turno = cadmPrimeiroSelecionado.getTurno();
        String motivo = cadmPrimeiroSelecionado.getMotivo();
        Integer id = cadmPrimeiroSelecionado.getId_cancelamento_adm();

        List<CancelamentoAdm> listCAdmFiltroInicial = linhasCancelamentoAdmRecuperadoBanco.stream()
                .filter(lambdaCadm -> Objects.equals(turno, lambdaCadm.getTurno()))
                .filter(lambdaCadm -> motivo.equals(lambdaCadm.getMotivo()))
                .toList();

        System.out.println("\n\nLIST FILTRO INICIAL: ");
        for (CancelamentoAdm c : listCAdmFiltroInicial){
            System.out.println("DATA: "+c.getData());
        }

        List<CancelamentoAdmHorario> listCadmH = cancelamentoAdmHDAO.
                listarHorariosDeCancelamentos(listCAdmFiltroInicial);

        Set<Integer> idsHorarioCursoPrimeiroSelecionado = listCadmH.stream()
                .filter(lambdaCadmH -> id.equals(lambdaCadmH.getCancelamento_adm_id()))
                .map(CancelamentoAdmHorario::getHorario_curso_id)
                .collect(Collectors.toSet());

        System.out.println("\n\nLIST IDS HORARIO BOTAO INICIAL: ");
        for (Integer c : idsHorarioCursoPrimeiroSelecionado){
            System.out.println("ID: "+c);
        }

        Set<Integer> idsCancelamentosCompartilhados = listCadmH.stream()
                .collect(Collectors.groupingBy(
                        CancelamentoAdmHorario::getCancelamento_adm_id,
                        Collectors.mapping(CancelamentoAdmHorario::getHorario_curso_id, Collectors.toSet())
                ))
                .entrySet().stream()
                .filter(e -> e.getValue().equals(idsHorarioCursoPrimeiroSelecionado))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        System.out.println("\n\nLIST IDS CADM: ");
        for (Integer c : idsCancelamentosCompartilhados){
            System.out.println("ID: "+c);
        }

        List<CancelamentoAdm> listCAdmFiltroFinal = listCAdmFiltroInicial.stream()
                .filter(ca -> idsCancelamentosCompartilhados.contains(ca.getId_cancelamento_adm()))
                .toList();

        System.out.println("\n\nLIST FINAL: ");
        for (CancelamentoAdm c : listCAdmFiltroFinal){
            System.out.println("DATA: "+c.getData());
        }

        Set<LocalDate> datasSelecionadasFinais = listCAdmFiltroFinal.stream()
                .map(CancelamentoAdm::getData)
                .collect(Collectors.toSet());

        for (LocalDate data : datasSelecionadasFinais) {
            mapaBotaoPressionadoEstilo.put(data, vermelhoBorda);
        }

        List<HorarioCurso> listHorarioCurso = hcDao.
                listarHorarioCursoPorIds(idsHorarioCursoPrimeiroSelecionado.stream().toList());

        listCheckHorariosSelecionados.clear();
        listCheckHorariosSelecionados = new ArrayList<>(listHorarioCurso.stream()
                .map(HorarioCurso::getHora_inicio)
                .toList());

        preencherDadosConfigCancelamentoComHorarios(motivo, turno);

    }

    public void selecionarBotoesCancelamentoMotivoIgual(LocalDate dataBtnPressionado){
        System.out.println("\n\nENTROU EM 'selecionarBotoesCancelamentoMotivoIgual'");
        try{
            CancelamentoAdm cadm = cancelamentoDAO.recuperarCancelamentoAdm(dataBtnPressionado, idSemestreAtual);
            List<LocalDate> datasFiltradas;


            String motivo = cadm.getMotivo();
            if (cadm.getDia_inteiro() != true){
                selecionarBotoesCancelamentoMotivoHorariosIgual(cadm);
            } else {
                System.out.println("MOTIVO DO botao "+ dataBtnPressionado+": "+motivo);
                datasFiltradas = linhasCancelamentoAdmRecuperadoBanco.stream()
                        .filter(lambdaCadm -> motivo.equals(lambdaCadm.getMotivo()))
                        .map(CancelamentoAdm::getData)
                        .toList();

                for (LocalDate data : datasFiltradas) {
                    mapaBotaoPressionadoEstilo.put(data, vermelhoBorda);
                }

                preencherDadosConfigCancelamentoDiaInteiro(motivo);
            }

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void alternarEstiloBotao(Button btn, LocalDate data, String estiloAntes, String estiloDepois) {
        if (btn.getStyle().equals(estiloAntes)) {
            btn.setStyle(estiloDepois);
            mapaBotaoPressionadoEstilo.put(data, estiloDepois);
        } else {
            btn.setStyle(estiloAntes);
            mapaBotaoPressionadoEstilo.remove(data);
        }
    }

    private void processarCliqueBotao(Button btnDia, LocalDate dataDesteBotao) {
        String estilo = btnDia.getStyle();
        boolean mapaVazioAntesDoClique = mapaBotaoPressionadoEstilo.isEmpty();

        if (mapaVazioAntesDoClique) {
            corBotaoSelecionado = estilo;
            btnDeletar.setManaged(true);
            btnDeletar.setVisible(true);
        }

        if (estilo.contains("FFA500")) {
            if (mapaVazioAntesDoClique) {
                selecionarBotoesFeriadoMotivoIgual(dataDesteBotao);
            }
            alternarEstiloBotao(btnDia, dataDesteBotao, laranjaCheio, laranjaBorda);
            bloquearBtnDiasNaoComuns();
        }

        else if (estilo.contains("FF0000")) {
            if (mapaVazioAntesDoClique) {
                selecionarBotoesCancelamentoMotivoIgual(dataDesteBotao);
            }
            alternarEstiloBotao(btnDia, dataDesteBotao, vermelhoCheio, vermelhoBorda);
            bloquearBtnDiasNaoComuns();
        }

        else if (estilo.contains("D3D3D3") || estilo.contains("FFFF00")) {
            alternarEstiloBotao(btnDia, dataDesteBotao, corDefault, amareloCheio);
            bloquearBtnDiasNaoAmarelos();
        }

        verificarPodeAbrirConfigCancelamento();
    }

    public void verificarEstadoBotoesClick(Month mes) {
        for (Button btnDia : listaBtnDia) {
            LocalDate dataDesteBotao = LocalDate.of(anoSelecionado, mes, Integer.parseInt(btnDia.getText()));

            btnDia.setOnAction(event -> processarCliqueBotao(btnDia, dataDesteBotao));

            if (corBotaoSelecionado != null) {
                if (corBotaoSelecionado.contains("FFA500") || corBotaoSelecionado.contains("FF0000")) {
                    bloquearBtnDiasNaoComuns();
                } else if (corBotaoSelecionado.contains("D3D3D3") || corBotaoSelecionado.contains("FFFF00")) {
                    bloquearBtnDiasNaoAmarelos();
                }
            }
        }
        atualizarGridBtnDia();
    }

    public void alterarCorBotoesUsandoValoresBanco(Month mes){
        for (Button btnDia: listaBtnDia){
            LocalDate dataDesteBotao = LocalDate.of
                    (anoSelecionado, mes, Integer.parseInt(btnDia.getText()));

            boolean possuiDataBloqueada = linhasDataBloqueadaRecuperadaBanco.stream()
                    .anyMatch(lambdaDataB -> dataDesteBotao.equals(lambdaDataB.getData()));
            boolean possuiDataCancelamentoAdm = linhasCancelamentoAdmRecuperadoBanco.stream()
                    .anyMatch(lambdaCadm -> dataDesteBotao.equals(lambdaCadm.getData()));


            if (possuiDataBloqueada){
                if (!mapaBotaoPressionadoEstilo.containsKey(dataDesteBotao))
                    btnDia.setStyle(laranjaCheio);
                else {
                    btnDia.setStyle(laranjaBorda);
                }
            } else if (possuiDataCancelamentoAdm){
                if (!mapaBotaoPressionadoEstilo.containsKey(dataDesteBotao))
                    btnDia.setStyle(vermelhoCheio);
                else {
                    btnDia.setStyle(vermelhoBorda);
                }
            } else {
                btnDia.setStyle(corDefault);
            }
        }
    }

    public List<String> listarNomesDosMeses(LocalDate inicio, LocalDate fim) {
        List<String> listaMeses = new ArrayList<>();

        LocalDate atual = inicio.withDayOfMonth(1);

        while (!atual.isAfter(fim.withDayOfMonth(1))) {
            String nomeMes = atual.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));

            nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1);
            listaMeses.add(nomeMes);
            atual = atual.plusMonths(1);
        }

        return listaMeses;
    }

    public void carregarMesesCbMes(){
        if (slAtual != null){
            LocalDate dataInicioSemestre = slAtual.getData_inicio();
            LocalDate dataFimSemestre = slAtual.getData_fim();

            ObservableList opcoesMeses = FXCollections.observableArrayList
                    (listarNomesDosMeses(dataInicioSemestre, dataFimSemestre));
            cbMes.setItems(opcoesMeses);
        }
    }

    public void gerarBotoesDia(Month mes){
        listaBtnDia.clear();
        gridDias.getChildren().clear();

        LocalDate dataInicial = LocalDate.of(anoSelecionado, mes, 1);
        int qtdDiasMes = YearMonth.of(anoSelecionado, mes).lengthOfMonth();
        int posCol = 0;
        int posLinha = 0;

        if (mes == slAtual.getData_inicio().getMonth()){
            dataInicial = slAtual.getData_inicio();
        } else if (mes == slAtual.getData_fim().getMonth()){
            qtdDiasMes = slAtual.getData_fim().getDayOfMonth();
        }

        for (int nrDia = dataInicial.getDayOfMonth(); nrDia <= qtdDiasMes; nrDia++){
            if (posCol > 5) {
                posLinha++;
                posCol = 0;
            }

            Button btnDia = new Button(String.valueOf(nrDia));

            gridDias.add(btnDia, posCol, posLinha);
            listaBtnDia.add(btnDia);
            posCol+=1;
        }
    }

    // ------- CHECKBOXES

    private void atualizarCbTurnoConformeSelecaoHorarios(){
        Set<LocalTime> selecionados = new HashSet<>(listCheckHorariosSelecionados);

        boolean temManha = mapaTurnoListTHT.get("manha").stream()
                .map(TemplateHorarioTurno::getHora_inicio).anyMatch(selecionados::contains);
        boolean temNoite = mapaTurnoListTHT.get("noite").stream()
                .map(TemplateHorarioTurno::getHora_inicio).anyMatch(selecionados::contains);

        String turnoAtual = cbTurno.getValue();

        if (temManha && temNoite && !"Ambos turnos".equals(turnoAtual)){
            // passou a usar os dois turnos -> exibe combo e os dois painéis juntos
            cbTurno.setValue("Ambos turnos");
            gerarCheckboxHorarios("Ambos turnos");
        } else if (!(temManha && temNoite) && "Ambos turnos".equals(turnoAtual)){
            // desmarcou até restar só um turno -> volta pro turno único
            String turnoRestante = temManha ? "manha" : (temNoite ? "noite" : null);
            if (turnoRestante != null){
                cbTurno.setValue(turnoRestante);
                gerarCheckboxHorarios(turnoRestante);
            }
        }
    }

    private void adicionarCheckboxesDoTurno(String turno){
        for (TemplateHorarioTurno tht : mapaTurnoListTHT.get(turno)){
            CheckBox checkBoxHorario = new CheckBox();
            checkBoxHorario.setText(tht.getHora_inicio() + " - " + tht.getHora_fim());
            checkBoxHorario.setSelected(listCheckHorariosSelecionados.contains(tht.getHora_inicio()));

            checkBoxHorario.setOnAction(event -> {
                if (listCheckHorariosSelecionados.contains(tht.getHora_inicio())){
                    listCheckHorariosSelecionados.remove(tht.getHora_inicio());
                } else {
                    listCheckHorariosSelecionados.add(tht.getHora_inicio());
                }
                atualizarCbTurnoConformeSelecaoHorarios();
            });

            flowHorarios.getChildren().add(checkBoxHorario);
        }
    }

    private void gerarCheckboxHorarios(String turno){
        flowHorarios.getChildren().clear();

        if ("Ambos turnos".equals(turno)){
            adicionarCheckboxesDoTurno("manha");
            adicionarCheckboxesDoTurno("noite");
        } else {
            adicionarCheckboxesDoTurno(turno);
        }
    }

    private void recuperarHorariosTurnos(){
        List<TemplateHorarioTurno> listaTHT = thtDao.listarTodosHorariosTurnos();
        List<TemplateHorarioTurno> listaTurnoManha = listaTHT.stream()
                .filter(lambdaTHT -> "manha".equals(lambdaTHT.getTurno()))
                .toList();
        List<TemplateHorarioTurno> listaTurnoNoite = listaTHT.stream()
                .filter(lambdaTHT -> "noite".equals(lambdaTHT.getTurno()))
                .toList();

        mapaTurnoListTHT.put("manha", listaTurnoManha);
        mapaTurnoListTHT.put("noite", listaTurnoNoite);
    }

    @FXML
    public void handleCancelamentoSelecaoMes(){
        String mesSelecionadoCbMes = cbMes.getValue();

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("MMMM")
                .withLocale(Locale.of("pt", "BR"));
        mesSelecionadoTipoMonth = Month.from(formatador.parse(mesSelecionadoCbMes.toLowerCase()));

        gerarBotoesDia(mesSelecionadoTipoMonth);
        alterarCorBotoesUsandoValoresBanco(mesSelecionadoTipoMonth);
        verificarEstadoBotoesClick(mesSelecionadoTipoMonth);
    }

    @FXML
    public void handleDesfazerSelecao(){
        mapaBotaoPressionadoEstilo.clear();
        bloquearBtnDiasNaoComuns();
        verificarPodeAbrirConfigCancelamento();
        alterarCorBotoesUsandoValoresBanco(mesSelecionadoTipoMonth);
        resetarDadosInterfaceConfigCancelamento();
    }

    @FXML
    public void handleFeriados(){
        if (checkFeriado.isSelected()){
            cbTurno.setDisable(true);
            cbTurno.setValue("Dia inteiro");
        } else {
            cbTurno.setDisable(false);
        }
    }

    @FXML
    public void handleSelecaoTurno(){
        flowHorarios.getChildren().clear();
        painelHorarios.setManaged(false);
        painelHorarios.setVisible(false);

        if (!cbTurno.getValue().equals("Dia inteiro")){
            painelHorarios.setManaged(true);
            painelHorarios.setVisible(true);

            gerarCheckboxHorarios(cbTurno.getValue());
        }
    }

    @FXML
    public void handleCancelarDatas() {
        if (checkFeriado.isSelected()){
            adicionarFeriadosBanco(tfMotivoCancelamento.getText());
        } else if (cbTurno.getValue().equals("Dia inteiro")){
            adicionarCancelamentosDiaInteiroBanco(tfMotivoCancelamento.getText());
        } else if (!listCheckHorariosSelecionados.isEmpty()){
            String turnoParaSalvar = "Ambos turnos".equals(cbTurno.getValue()) ? null : cbTurno.getValue();
            adicionarCancelamentoHorariosBanco(tfMotivoCancelamento.getText(), turnoParaSalvar);
        } else {
            // popup: precisa selecionar ao menos um checkbox
        }

        resetarDadosConfigCancelamento();
        handleDesfazerSelecao();
    }
    @FXML
    public void handleDeletarCancelamento() {
        if (checkFeriado.isSelected()){
            excluirFeriadoBanco(tfMotivoCancelamento.getText());
        } else if (cbTurno.getValue().equals("Dia inteiro")){
            excluirCancelamentoDiaInteiroBanco(tfMotivoCancelamento.getText());
        }

        resetarDadosConfigCancelamento();
        handleDesfazerSelecao();
    }

    // --- METODOS UTILITARIOS GENERICOS ---

    private void exibirFeedback(Label label, String mensagem, boolean isErro) {
        label.setText(mensagem);
        label.setVisible(true);
        label.setManaged(true);
        label.setStyle(isErro ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
