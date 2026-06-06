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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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

    private SemestreLetivo slAtual;

    private List<TemplateHorarioTurno> listaHorariosSelecionados = new ArrayList<>();
    private List<Button> listaBtnDia = new ArrayList<>();
    private List<LocalDate> datasBloqueadasRecuperadasBanco = new ArrayList<>();
    private Map<LocalDate, String> mapaBotaoPressionadoEstilo = new LinkedHashMap<>();
    private String corBotaoSelecionada;
    private Month mesSelecionadoTipoMonth;
    private Boolean reacaoEmCadeiaBtns = false;
    private int anoSelecionado;
    private int anoSemestreSelecionado;
    private int idSemestreAtual;
    private int ID_ADM_LOGADO;

    private final String laranjaCheio = "-fx-border-color: transparent; -fx-background-color: #FFA500;";
    private final String laranjaBorda = "-fx-border-color: #FFA500; -fx-background-color: -fx-control-inner-background;";
    private final String corInicial = "-fx-border-color: transparent; -fx-background-color: #D3D3D3;";

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
        datasBloqueadasRecuperadasBanco = databDao.listarDatasBloqueadasPorSemestre(idSemestreAtual);
        carregarMesesCbMes();

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


    // ==============================================
    // ----------- METODOS CALENDARIO ---------------
    // ==============================================

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

    // =============================================================
    // ----------- METODOS BLOQUEIOS E CANCELAMENTOS ---------------
    // =============================================================

    // ------------- CRUD:

    public void atualizarValoresFeriadoBanco(String motivo){
        try{
            List<DataBloqueada> datasSelecionadas = new ArrayList<>();
            for (LocalDate data: mapaBotaoPressionadoEstilo.keySet()){
                System.out.println("Data no set: "+data);
                DataBloqueada dataB = new DataBloqueada();
                dataB.setAdmId(logado.getId_usuario());
                dataB.setData(data);
                dataB.setMotivo(motivo);
                dataB.setSemestreLetivoId(idSemestreAtual);

                datasSelecionadas.add(dataB);
            }
            databDao.atualizarEmLote(datasSelecionadas);
            datasBloqueadasRecuperadasBanco = databDao.listarDatasBloqueadasPorSemestre(idSemestreAtual);

        } catch (SQLException e){
            e.printStackTrace();
        }
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
        checkFeriado.setSelected(false);
        cbTurno.setDisable(false);
        tfMotivoCancelamento.setText("");
        btnCancelar.setText("Cancelar Datas");
        btnCancelar.setOnAction(event -> {
            handleCancelarDatas();
        });
    }

    public void preencherDadosConfigCancelamento(String motivo){
        tfMotivoCancelamento.setText(motivo);
        checkFeriado.setSelected(true);
        cbTurno.setDisable(true);
        btnCancelar.setText("Editar feriado");
        btnCancelar.setOnAction(event -> {
            if (checkFeriado.isSelected()) {
                atualizarValoresFeriadoBanco(tfMotivoCancelamento.getText());
                resetarDadosConfigCancelamento();
                handleDesfazerSelecao();
            }
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

    public void bloquearBtnDiasNaoComunsFeriado(){
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
        }
        atualizarGridBtnDia();
    }

    public void selecionarBotoesFeriadoMotivoIgual(LocalDate dataBtnPressionado){
        try{
            String motivo = databDao.recuperarMotivoData(dataBtnPressionado, idSemestreAtual);
            List<LocalDate> datasMotivoIgual = databDao.listarDatasMotivoComumSL(idSemestreAtual, motivo);

            for (LocalDate data: datasMotivoIgual){
                System.out.println("Data encontrada: "+data);
                mapaBotaoPressionadoEstilo.put(data, laranjaBorda);
            }

            preencherDadosConfigCancelamento(motivo);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void verificarEstadoBotoesClick(Month mes){
        for (Button btnDia: listaBtnDia){
            LocalDate dataDesteBotao = LocalDate.of
                    (anoSelecionado, mes, Integer.parseInt(btnDia.getText()));

            btnDia.setOnAction(event -> {
                String estilo = btnDia.getStyle();

                if (estilo.contains("FFA500")){
                    if (mapaBotaoPressionadoEstilo.isEmpty()){
                        selecionarBotoesFeriadoMotivoIgual(dataDesteBotao);
                    }

                    if (estilo.equals(laranjaCheio)){
                        btnDia.setStyle(laranjaBorda);
                        mapaBotaoPressionadoEstilo.put(dataDesteBotao, laranjaBorda);
                    } else {
                        btnDia.setStyle(laranjaCheio);
                        mapaBotaoPressionadoEstilo.remove(dataDesteBotao);
                    }
                }

                verificarPodeAbrirConfigCancelamento();
                bloquearBtnDiasNaoComunsFeriado();
            });
            bloquearBtnDiasNaoComunsFeriado();
        }
    }

    public void alterarBotoesValoresBancoFeriado(Month mes){
        for (Button btnDia: listaBtnDia){
            LocalDate dataDesteBotao = LocalDate.of
                    (anoSelecionado, mes, Integer.parseInt(btnDia.getText()));
            if (datasBloqueadasRecuperadasBanco.contains(dataDesteBotao)){
                if (!mapaBotaoPressionadoEstilo.containsKey(dataDesteBotao))
                    btnDia.setStyle(laranjaCheio);
                else {
                    btnDia.setStyle(laranjaBorda);
                }
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

    @FXML
    public void handleCancelamentoSelecaoMes(){
        String mesSelecionadoCbMes = cbMes.getValue();

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("MMMM")
                .withLocale(Locale.of("pt", "BR"));
        mesSelecionadoTipoMonth = Month.from(formatador.parse(mesSelecionadoCbMes.toLowerCase()));

        gerarBotoesDia(mesSelecionadoTipoMonth);
        alterarBotoesValoresBancoFeriado(mesSelecionadoTipoMonth);
        verificarEstadoBotoesClick(mesSelecionadoTipoMonth);
    }

    @FXML
    public void handleDesfazerSelecao(){
        mapaBotaoPressionadoEstilo.clear();
        bloquearBtnDiasNaoComunsFeriado();
        verificarPodeAbrirConfigCancelamento();
        alterarBotoesValoresBancoFeriado(mesSelecionadoTipoMonth);
    }

    @FXML
    public void handleFeriados(){

    }

    @FXML
    public void handleSelecaoTurno(){

    }

    @FXML
    public void handleCancelarDatas() {

    }

    @FXML
    public void handleDeletarCancelamento() {

    }

    // --- METODOS UTILITARIOS GENERICOS ---

    private void exibirFeedback(Label label, String mensagem, boolean isErro) {
        label.setText(mensagem);
        label.setVisible(true);
        label.setManaged(true);
        label.setStyle(isErro ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}
