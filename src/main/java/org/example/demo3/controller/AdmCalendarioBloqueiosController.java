package org.example.demo3.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.time.format.TextStyle;
import java.util.*;

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
    private final CancelamentoAdmHorarioDAO cancelamentoHDAO = new CancelamentoAdmHorarioDAO();
    private final DataBloqueadaDAO databDao = new DataBloqueadaDAO();
    private final SemestreLetivoDAO slDao = new SemestreLetivoDAO();
    private final TemplateHorarioTurnoDAO thtDao = new TemplateHorarioTurnoDAO();
    private final HorarioCursoDAO hcDao = new HorarioCursoDAO();

    private final UsuarioAtual logado = UsuarioAtual.getInstancia();

    private final ObservableList<Sprint> listaSprintsFX = FXCollections.observableArrayList();
    private final ObservableList<CancelamentoAdm> listaCancelamentosFX = FXCollections.observableArrayList();
    private LocalDate dataSelecionadaNoCalendario;

    private List<String> listaMeses;
    private SemestreLetivo slAtual;
    private String mesSelecionado;
    private List<LocalDate> listaDiaBotaoPressionado = new ArrayList<>();
    private List<TemplateHorarioTurno> listaHorariosSelecionados = new ArrayList<>();
    private Map<LocalDate, String> mapaEstadoBotaoDia = new LinkedHashMap<>();
    private String corBotaoSelecionada;
    private List<Button> listaBtnDia = new ArrayList<>();
    private Boolean reacaoEmCadeiaBtns = false;

    private int idSemestreAtual;
    private int ID_ADM_LOGADO;

    @FXML
    public void initialize() {
        //REMOVER ESSA VARIAVEIS DEPOIS:
        logado.setId_usuario(1);
        logado.setTipo("ADM");

        ID_ADM_LOGADO = logado.getId_usuario();
        configurarTabelaSprints();

        int anoFiltro = (logado.getAno() != null) ? logado.getAno() : LocalDate.now().getYear();
        int numeroSemestre = (logado.getAnoSemestre() != null) ? logado.getAnoSemestre() : 1;

        carregarDadosPorAnoESemestre(anoFiltro, numeroSemestre);

        carregarMesesCancelamento(anoFiltro, numeroSemestre);
    }

    // --- METODOS DA ABA CALENDARIO DO SEMESTRE ---

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

    // --- METODOS DA ABA BLOQUEIOS E CANCELAMENTOS ---

    public List<String> listarNomesDosMeses(LocalDate inicio, LocalDate fim) {
        listaMeses = new ArrayList<>();

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

    public void carregarMesesCancelamento(int ano, int anoSemestre){
        try {
            idSemestreAtual = slDao.getIdSemestreLetivo(ano, anoSemestre);
            slAtual = slDao.listarSLPorId(idSemestreAtual);

            LocalDate dataInicio = slAtual.getData_inicio();
            LocalDate dataFim = slAtual.getData_fim();
            ObservableList opcoesMeses = FXCollections.observableArrayList
                    (listarNomesDosMeses(dataInicio, dataFim));
            cbMes.setItems(opcoesMeses);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static Month converterNomeParaMonth(String nome) {
        for (Month m : Month.values()) {
            String nomePt = m.getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
            if (nomePt.equalsIgnoreCase(nome)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Mês inválido: " + nome);
    }

    public void configurarComboboxCbTurno(){
        ObservableList<String> opcoesTurno = FXCollections.observableArrayList("Dia inteiro", "manha", "noite");
        cbTurno.setItems(opcoesTurno);
        cbTurno.setValue("Dia inteiro");

        if (corBotaoSelecionada.equals("FFA500")){
            checkFeriado.setSelected(true);
            cbTurno.setDisable(true);
        }
    }


    public void configurarComboboxCbTurnoFeriado(String motivo){
        ObservableList<String> opcoesTurno = FXCollections.observableArrayList("Dia inteiro", "manha", "noite");
        cbTurno.setItems(opcoesTurno);
        cbTurno.setValue("Dia inteiro");

        if (corBotaoSelecionada.equals("FFA500")){
            checkFeriado.setSelected(true);
            cbTurno.setDisable(true);
            btnCancelar.setText("Editar feriado");
            tfMotivoCancelamento.setText(motivo);

            List<DataBloqueada> listaDb = new ArrayList<>();

            btnCancelar.setOnAction(event -> {
                for (LocalDate data: listaDiaBotaoPressionado){
                    DataBloqueada db = new DataBloqueada();
                    db.setAdmId(logado.getId_usuario());
                    db.setMotivo(tfMotivoCancelamento.getText());
                    db.setData(data);
                    db.setSemestreLetivoId(idSemestreAtual);
                    listaDb.add(db);
                }
                try{
                    databDao.atualizarEmLote(listaDb);
                } catch (SQLException e){
                    e.printStackTrace();
                }
            });
        }
    }

    public void configurarComboboxCbTurnoCancelamento(String motivo){
        ObservableList<String> opcoesTurno = FXCollections.observableArrayList("Dia inteiro", "manha", "noite");
        cbTurno.setItems(opcoesTurno);
        cbTurno.setValue("Dia inteiro");

        if (corBotaoSelecionada.equals("FF0000")){
            checkFeriado.setSelected(false);
            cbTurno.setDisable(true);
            btnCancelar.setText("Editar cancelamento");
            tfMotivoCancelamento.setText(motivo);

            List<CancelamentoAdm> listaCadm = new ArrayList<>();

            btnCancelar.setOnAction(event -> {
                for (LocalDate data: listaDiaBotaoPressionado){
                    CancelamentoAdm cadm = new CancelamentoAdm();
                    cadm.setAdm_id(logado.getId_usuario());
                    cadm.setSemestre_letivo_id(idSemestreAtual);
                    cadm.setData(data);
                    cadm.setDia_inteiro(true);
                    cadm.setMotivo(tfMotivoCancelamento.getText());

                    listaCadm.add(cadm);
                }
                try{
                    cancelamentoDAO.atualizarEmLote(listaCadm);
                } catch (SQLException e){
                    e.printStackTrace();
                }
            });
        }
    }

    public void recuperarCancelamentos(){
        List<LocalDate> datasBloqueadasRecuperadas = databDao.listarDatasBloqueadasPorSemestre(idSemestreAtual);
        List<CancelamentoAdm> datasCanceladasRecuperadas = cancelamentoDAO.listarPorSemestre(idSemestreAtual);

        for (LocalDate data: datasBloqueadasRecuperadas){
            mapaEstadoBotaoDia.put(data, "-fx-border-color: transparent; -fx-background-color: #FFA500;");
        }

        for (CancelamentoAdm cadm: datasCanceladasRecuperadas){
            LocalDate data = cadm.getData();
            if (!mapaEstadoBotaoDia.containsKey(data)){
                mapaEstadoBotaoDia.put(data, "-fx-border-color: transparent; -fx-background-color: #FF0000;");
            }
        }
    }

    public void refletirMudancaNosBotoesRelacionadosFeriado(LocalDate dataPrimeiro, String cor){
        for (LocalDate data : listaDiaBotaoPressionado){
            for (Button btn : listaBtnDia) {
                if (btn.getText().equals(String.valueOf(data.getDayOfMonth()))){
                    if (data != dataPrimeiro) {
                        System.out.println("ativou o botao: " + data);
                        if (cor.equals("FFA500")) {
                            btn.setStyle("-fx-border-color: #FFA500; " +
                                    "-fx-background-color: -fx-control-inner-background;");
                            mapaEstadoBotaoDia.put(data, btn.getStyle());
                        } else if (cor.equals("FF0000")){
                            btn.setStyle("-fx-border-color: #FF0000; " +
                                    "-fx-background-color: -fx-control-inner-background;");
                            mapaEstadoBotaoDia.put(data, btn.getStyle());
                        }
                    }
                }
            }
        }
        reacaoEmCadeiaBtns = true;
    }

    public void preencherCamposConfiguracaoCancelamento(LocalDate dataSelecionada){
        configurarComboboxCbTurno();
        List<LocalDate> datasMotivoIgual = new ArrayList<>();

        if (!corBotaoSelecionada.equals("D3D3D3") && !corBotaoSelecionada.equals("FFFF00")){
            if (reacaoEmCadeiaBtns == false) {
                try {
                    if (corBotaoSelecionada.equals("FFA500")) {
                        String motivo = databDao.recuperarMotivoData(dataSelecionada, idSemestreAtual);
                        tfMotivoCancelamento.setText(motivo);
                        List<LocalDate> resultados = databDao.listarDatasMotivoComumSL(idSemestreAtual, motivo);

                        if (resultados != null) {
                            datasMotivoIgual.addAll(resultados);
                        }
                    }
                    if (corBotaoSelecionada.equals("FF0000")) {
                        String motivo = cancelamentoDAO.recuperarMotivoData(dataSelecionada, idSemestreAtual);
                        tfMotivoCancelamento.setText(motivo);
                        List<LocalDate> resultados = cancelamentoDAO.listarDatasDiaInteiroMotivoComumSL(
                                idSemestreAtual, motivo
                        );

                        if (resultados != null) {
                            datasMotivoIgual.addAll(resultados);
                        }

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                for (LocalDate data : datasMotivoIgual) {
                    if (!listaDiaBotaoPressionado.contains(data)) {
                        listaDiaBotaoPressionado.add(data);
                    }
                }
                refletirMudancaNosBotoesRelacionadosFeriado(dataSelecionada, corBotaoSelecionada);

                if (corBotaoSelecionada.equals("FFA500")){
                    configurarComboboxCbTurnoFeriado(tfMotivoCancelamento.getText());
                } else if(corBotaoSelecionada.equals("FF0000")){
                    configurarComboboxCbTurnoCancelamento(tfMotivoCancelamento.getText());
                }
            }

            listaDiaBotaoPressionado.forEach(System.out::println);
        }
    }

    public void atualizarEstadoBotaoDia(Button btnDia, LocalDate dataDesteBotao){
        if (mapaEstadoBotaoDia.containsKey(dataDesteBotao)){
            switch (mapaEstadoBotaoDia.get(dataDesteBotao)){
                case "-fx-border-color: transparent; -fx-background-color: #FFFF00;" -> {
                    btnDia.setStyle("-fx-border-color: transparent; -fx-background-color: #D3D3D3;");
                    mapaEstadoBotaoDia.remove(dataDesteBotao);
                    listaDiaBotaoPressionado.remove(dataDesteBotao);
                }
                case "-fx-border-color: transparent; -fx-background-color: #FF0000;" -> {
                    btnDia.setStyle("-fx-border-color: #FF0000; -fx-background-color: -fx-control-inner-background;");
                    mapaEstadoBotaoDia.put(dataDesteBotao, btnDia.getStyle());
                    listaDiaBotaoPressionado.add(dataDesteBotao);
                }
                case "-fx-border-color: #FF0000; -fx-background-color: -fx-control-inner-background;" -> {
                    btnDia.setStyle("-fx-border-color: transparent; -fx-background-color: #FF0000;");
                    mapaEstadoBotaoDia.put(dataDesteBotao, btnDia.getStyle());
                    listaDiaBotaoPressionado.remove(dataDesteBotao);
                }
                case "-fx-border-color: transparent; -fx-background-color: #FFA500;" -> {
                    btnDia.setStyle("-fx-border-color: #FFA500; -fx-background-color: -fx-control-inner-background;");
                    mapaEstadoBotaoDia.put(dataDesteBotao, btnDia.getStyle());
                    listaDiaBotaoPressionado.add(dataDesteBotao);
                }
                case "-fx-border-color: #FFA500; -fx-background-color: -fx-control-inner-background;" -> {
                    btnDia.setStyle("-fx-border-color: transparent; -fx-background-color: #FFA500;");
                    mapaEstadoBotaoDia.put(dataDesteBotao, btnDia.getStyle());
                    listaDiaBotaoPressionado.remove(dataDesteBotao);
                }
            }
        } else{
            btnDia.setStyle("-fx-border-color: transparent; -fx-background-color: #FFFF00;");
            mapaEstadoBotaoDia.put(dataDesteBotao, btnDia.getStyle());
            listaDiaBotaoPressionado.add(dataDesteBotao);
        }

        if (listaDiaBotaoPressionado.isEmpty()){
            boxConfigCancelamento.setManaged(false);
            boxConfigCancelamento.setVisible(false);
        } else {
            boxConfigCancelamento.setManaged(true);
            boxConfigCancelamento.setVisible(true);
            preencherCamposConfiguracaoCancelamento(dataDesteBotao);
        }
    }

    public void handleCancelariaFeriados(String motivo){
        List<DataBloqueada> listaDatasBloqueadas = new ArrayList<>();
        try{
            for (LocalDate data: listaDiaBotaoPressionado){
                DataBloqueada datab = new DataBloqueada();
                datab.setData(data);
                datab.setAdmId(logado.getId_usuario());
                datab.setMotivo(motivo);
                datab.setRecorrente(true);
                datab.setSemestreLetivoId(idSemestreAtual);
                listaDatasBloqueadas.add(datab);
            }

            databDao.salvarEmLote(listaDatasBloqueadas);
            mapaEstadoBotaoDia.clear();
            listaDiaBotaoPressionado.clear();

        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void handleCancelarAdm(String motivo){
        List<CancelamentoAdm> listaCancelamentoAdm = new ArrayList<>();
        try{
            for (LocalDate data: listaDiaBotaoPressionado){
                CancelamentoAdm cadm = new CancelamentoAdm();
                cadm.setAdm_id(logado.getId_usuario());
                cadm.setSemestre_letivo_id(idSemestreAtual);
                cadm.setData(data);
                if (!cbTurno.getValue().equals("Dia inteiro")){
                    cadm.setTurno(cbTurno.getValue());
                    cadm.setDia_inteiro(false);
                } else{
                    cadm.setDia_inteiro(true);
                    cadm.setTurno(null);
                }
                cadm.setMotivo(motivo);
                cadm.setCriado_em(LocalDate.now());

                System.out.println(cadm.getTurno());
                listaCancelamentoAdm.add(cadm);
            }

            cancelamentoDAO.salvarEmLote(listaCancelamentoAdm);
            mapaEstadoBotaoDia.clear();
            listaDiaBotaoPressionado.clear();

            if (!listaHorariosSelecionados.isEmpty()) {
                List<CancelamentoAdmHorario> listaCadmHorario = new ArrayList<>();

                for (CancelamentoAdm cadm: listaCancelamentoAdm) {
                    Integer cadm_id = cancelamentoDAO.recuperarIdCancelamento(cadm);
                    for (TemplateHorarioTurno tht : listaHorariosSelecionados) {
                        List<Integer> horarioCursoIds = recuperarIdsCursoHorario(
                                tht.getHora_inicio(), tht.getHora_fim());
                        for (Integer id : horarioCursoIds) {
                            CancelamentoAdmHorario cadmH = new CancelamentoAdmHorario();
                            cadmH.setCancelamento_adm_id(cadm_id);
                            cadmH.setHorario_curso_id(id);
                            listaCadmHorario.add(cadmH);
                        }
                    }
                }

                cancelamentoHDAO.salvarEmLote(listaCadmHorario);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public List<Integer> recuperarIdsCursoHorario(LocalTime hc, LocalTime hf){
        try{
            return hcDao.recuperarIdsHoraInicioFim(hc, hf);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @FXML
    public void handleDesfazerSelecao(){

    }

    @FXML
    public void handleFeriados(){
        if (checkFeriado.isSelected()){
            cbTurno.setValue("Dia inteiro");
            cbTurno.setDisable(true);
            painelHorarios.setManaged(false);
            painelHorarios.setVisible(false);
            listaHorariosSelecionados.clear();
        } else {
            cbTurno.setDisable(false);
            painelHorarios.setManaged(false);
            painelHorarios.setVisible(false);
        }
    }

    @FXML
    public void handleSelecaoTurno(){
        if (cbTurno.getValue() == null) return;
        flowHorarios.getChildren().clear();

        painelHorarios.setManaged(true);
        painelHorarios.setVisible(true);

        if (!cbTurno.getValue().equals("Dia inteiro")){
            try{
                List<TemplateHorarioTurno> listaHorarios = thtDao.listarPorTurno(cbTurno.getValue());
                for (TemplateHorarioTurno tht: listaHorarios){
                    CheckBox horario = new CheckBox();

                    String hora_inicio = String.valueOf(tht.getHora_inicio());
                    String hora_fim = String.valueOf(tht.getHora_fim());

                    horario.setText(hora_inicio + " - "+ hora_fim);
                    flowHorarios.getChildren().add(horario);

                    horario.setOnAction(e -> {
                        if (horario.isSelected()) {
                            listaHorariosSelecionados.add(tht);
                        } else if (!horario.isSelected() && listaHorarios.contains(tht)){
                            listaHorarios.remove(tht);
                        }
                    });
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleCancelamentoSelecaoMes(){
        if (cbMes.getValue() == null) return;
        gridDias.getChildren().clear();
        listaBtnDia.clear();
        if (mapaEstadoBotaoDia.isEmpty()) recuperarCancelamentos();

        Month mesEnum = converterNomeParaMonth(cbMes.getValue());
        int anoRef = slAtual.getData_inicio().getYear();

        LocalDate dataInicioReal = slAtual.getData_inicio();
        LocalDate atual;

        if (mesEnum == dataInicioReal.getMonth()) {
            atual = dataInicioReal;
        } else {
            atual = LocalDate.of(anoRef, mesEnum, 1);
        }

        LocalDate dataFinalReal = slAtual.getData_fim();
        LocalDate ultimoDiaDoMes = atual.withDayOfMonth(atual.lengthOfMonth());

        LocalDate dataLimiteLoop;
        if (dataFinalReal.getMonth() == mesEnum) {
            dataLimiteLoop = dataFinalReal;
        } else {
            dataLimiteLoop = ultimoDiaDoMes;
        }

        int pos_linha = 0;
        int pos_coluna = 0;

        while (!atual.isAfter(dataLimiteLoop)) {
            if (pos_coluna > 5) {
                pos_linha += 1;
                pos_coluna = 0;
            }

            String numeroData = String.valueOf(atual.getDayOfMonth());
            Button btnDia = new Button(numeroData);

            String estiloInicial = mapaEstadoBotaoDia.getOrDefault(atual, "-fx-border-color: transparent; -fx-background-color: #D3D3D3;");
            btnDia.setStyle(estiloInicial);

            LocalDate dataDesteBotao = LocalDate.of(anoRef, mesEnum, Integer.parseInt(btnDia.getText()));

            btnDia.setOnAction(e -> {
                String estiloAtual = btnDia.getStyle();

                if (corBotaoSelecionada == null){
                    if (estiloAtual.contains("#")) {
                        int inicio = estiloAtual.indexOf("#") + 1;
                        int fim = estiloAtual.indexOf(";", inicio);
                        corBotaoSelecionada = estiloAtual.substring(inicio, fim);
                    }
                    atualizarEstadoBotaoDia(btnDia, dataDesteBotao);
                } else {
                    boolean ehMesmaCor = estiloAtual.contains(corBotaoSelecionada);
                    boolean ehDesmarcarAmarelo = corBotaoSelecionada.equals("D3D3D3") && estiloAtual.contains("FFFF00");
                    if (ehMesmaCor || ehDesmarcarAmarelo) {
                        atualizarEstadoBotaoDia(btnDia, dataDesteBotao);
                    }

                    if (listaDiaBotaoPressionado.isEmpty()) {
                        corBotaoSelecionada = null;
                        reacaoEmCadeiaBtns = false;
                        btnCancelar.setText("Cancelar Datas");

                    }
                }
            });

            btnDia.setStyle(mapaEstadoBotaoDia.getOrDefault(atual, estiloInicial));

            btnDia.setId("btn-" + mesEnum.name() + "-" + numeroData);

            listaBtnDia.add(btnDia);
            gridDias.add(btnDia, pos_coluna, pos_linha);

            pos_coluna++;
            atual = atual.plusDays(1);
        }
    }

    @FXML
    public void handleCancelarDatas() {
        String motivo = tfMotivoCancelamento.getText();
        if (checkFeriado.isSelected()){
            handleCancelariaFeriados(motivo);
        } else {
            handleCancelarAdm(motivo);
        }

    }

    @FXML
    public void handleDeletarCancelamento() {

    }

    private void exibirFeedback(Label label, String mensagem, boolean isErro) {
        label.setText(mensagem);
        label.setVisible(true);
        label.setManaged(true);
        label.setStyle(isErro ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}